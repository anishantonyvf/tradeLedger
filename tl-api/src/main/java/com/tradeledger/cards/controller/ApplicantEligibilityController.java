package com.tradeledger.cards.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.tradeledger.cards.common.Applicant;
import com.tradeledger.cards.service.ApplicantEligibilityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApplicantEligibilityController {
	@Autowired
	private ApplicantEligibilityService service;

	@PostMapping(path = "CheckApplicantEligibility", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public String checkEligibility(@RequestBody Applicant applicant) {
		return service.checkEligibilty(applicant);

	}
}
