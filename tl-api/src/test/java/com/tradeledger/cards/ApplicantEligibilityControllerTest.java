package com.tradeledger.cards;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradeledger.cards.common.Applicant;
import com.tradeledger.cards.entity.EligibilityResponse;
import com.tradeledger.cards.repository.EligibilityRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class ApplicantEligibilityControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private RestTemplate restTemplate;
	@MockBean
	private EligibilityRepository eligibilityRepository;

	@Autowired
	private ApplicationContext context;

	@Test
	public void getEligibilityExistingResponse() throws Exception {
		context.getBean(CircuitBreaker.class).reset();
		when(restTemplate.postForObject(anyString(), any(), any()))
				.thenReturn("{\n" + " \"eligibleCards\": [\n" + " \"C1\",\n" + " \"C2\"\n" + " ]\n" + "}");

		EligibilityResponse eligibilityResponse = new EligibilityResponse("TestIdentifier", "TestResponse");
		when(eligibilityRepository.findById(anyString())).thenReturn(Optional.of(eligibilityResponse));
		when(eligibilityRepository.save(any())).thenReturn(eligibilityRepository);
		List<String> response = new ArrayList<>();
		response.add("C1");
		response.add("C2");
		mvc.perform(MockMvcRequestBuilders.post("/check")
				.content(new ObjectMapper().writeValueAsString(new Applicant("testName", "testEmail", "testAddress")))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.jsonPath("$.eligibleCards").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$.eligibleCards").value(response));
	}

	@Test
	public void getEligibilityNewRequest() throws Exception {

		context.getBean(CircuitBreaker.class).reset();
		when(restTemplate.postForObject(anyString(), any(), any()))
				.thenReturn("{\n" + " \"eligibleCards\": [\n" + " \"C1\",\n" + " \"C2\"\n" + " ]\n" + "}");

		when(eligibilityRepository.findById(anyString())).thenReturn(Optional.empty());
		when(eligibilityRepository.save(any())).thenReturn(eligibilityRepository);
		List<String> response = new ArrayList<>();
		response.add("C1");
		response.add("C2");
		mvc.perform(MockMvcRequestBuilders.post("/check")
				.content(new ObjectMapper().writeValueAsString(new Applicant("testName", "testEmail", "testAddress")))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.jsonPath("$.eligibleCards").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$.eligibleCards").value(response));
	}

	@Test
	public void getEligibilityException() throws Exception {
		when(restTemplate.postForObject(anyString(), any(), any())).thenThrow(new RuntimeException());
		when(eligibilityRepository.findById(anyString())).thenReturn(Optional.empty());
		when(eligibilityRepository.save(any())).thenReturn(eligibilityRepository);
		mvc.perform(MockMvcRequestBuilders.post("/check")
				.content(new ObjectMapper().writeValueAsString(new Applicant("testName", "testEmail", "testAddress")))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.error").exists()).andExpect(MockMvcResultMatchers
						.jsonPath("$.error").value("Unable to process request. Please try after sometime."));
		mvc.perform(MockMvcRequestBuilders.post("/check")
				.content(new ObjectMapper().writeValueAsString(new Applicant("testName", "testEmail", "testAddress")))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is2xxSuccessful()).andExpect(MockMvcResultMatchers.jsonPath("$.error").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$.error")
						.value("Unable to process request. Please try after sometime."));
	}

}