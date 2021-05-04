package com.tradeledger.cards.service;

import com.tradeledger.cards.common.Applicant;
import com.tradeledger.cards.entity.EligibilityResponse;
import com.tradeledger.cards.repository.EligibilityRepository;
import com.tradeledger.cards.utils.Utils;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.PreDestroy;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApplicantEligibilityService {

	Logger logger = LoggerFactory.getLogger(ApplicantEligibilityService.class);
	@Autowired
	private RestTemplate restTemplate;
	@Value("${eligibility.url}")
	private String eligibilityURL;
	@Autowired
	private ExecutorService asyncThreadPoolService;
	@Autowired
	private EligibilityRepository eligibilityRepository;
	@Autowired
	CircuitBreaker circuitBreaker;

	public String checkEligibilty(Applicant applicant) {
		final String response;
		logger.info("Evaluating the request : {}", applicant.requestString());
		if (StringUtils.isBlank(applicant.getName()) || StringUtils.isBlank(applicant.getEmail())
				|| StringUtils.isBlank(applicant.getAddress())) {
			logger.error("Mandatory Attributes missing in request. {}", applicant.requestString());
			return "{\n" + " \"error\": Unable to process request. Mandatory Attributes missing in request.\n" + "}";
		}
		try {
			HttpEntity<Applicant> request = new HttpEntity<>(applicant);
			Supplier<String> eligibiltySupplier = () -> restTemplate.postForObject(eligibilityURL, request,
					String.class);
			Supplier<String> decoratedFlightsSupplier = circuitBreaker.decorateSupplier(eligibiltySupplier);
			response = decoratedFlightsSupplier.get();
			CompletableFuture.runAsync(() -> {
				Optional<EligibilityResponse> eligibilityResponseData = eligibilityRepository
						.findById(applicant.getKey());
				if (eligibilityResponseData.isPresent()) {
					String existingResponse = eligibilityResponseData.get().getResponse();
					eligibilityRepository.save(new EligibilityResponse(applicant.getKey(),
							Utils.appendResponse(existingResponse, Utils.createResponse(response))));
				} else {
					eligibilityRepository
							.save(new EligibilityResponse(applicant.getKey(), Utils.createResponse(response)));
				}
			}, asyncThreadPoolService).exceptionally(e -> {
				logger.error("Exception while saving the response {} : {}", applicant.requestString(), response);
				return null;
			});
			return response;
		} catch (CallNotPermittedException circuitOpen) {
			logger.error("Circuit in Open State for request : ", applicant.requestString());
			return checkEligibiltyFallBack(applicant);
		} catch (Exception ex) {
			logger.error("Error in evaluating the request : {}", applicant.requestString());
			return checkEligibiltyException(applicant);
		}

	}

	public String checkEligibiltyFallBack(Applicant applicant) {
		logger.error("Error in evaluating the request : {}", applicant.requestString());
		return "{\n" + " \"error\": Unable to process request. Please try after sometime.\n" + "}";
	}

	public String checkEligibiltyException(Applicant applicant) {
		logger.error("Exception in evaluating the request : {}", applicant.requestString());
		return "{\n" + " \"error\": Unable to process request. Please try after sometime.\n" + "}";
	}

	@PreDestroy
	public void destroy() {
		logger.info("Shutting down asyncThreadPools");
		if (asyncThreadPoolService != null) {
			asyncThreadPoolService.shutdown();
			try {
				if (!asyncThreadPoolService.awaitTermination(60, TimeUnit.SECONDS)) {
					asyncThreadPoolService.shutdownNow();
				}
			} catch (Exception ex) {
				logger.error("Exception while shutting down asyncThreadPools", ex);
			}
		}
	}
}