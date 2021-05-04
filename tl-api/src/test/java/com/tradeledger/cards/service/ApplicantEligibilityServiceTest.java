package com.tradeledger.cards.service;

import java.util.concurrent.Executors;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class ApplicantEligibilityServiceTest {

	@Test
	public void destoryTest() {
		ApplicantEligibilityService applicantEligibilityService = new ApplicantEligibilityService();
		Whitebox.setInternalState(applicantEligibilityService, "asyncThreadPoolService",
				Executors.newFixedThreadPool(10));
		applicantEligibilityService.destroy();
	}
}