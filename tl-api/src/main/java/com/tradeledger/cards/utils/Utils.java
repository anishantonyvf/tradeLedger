package com.tradeledger.cards.utils;

import java.time.Instant;

public final class Utils {

	public static String createResponse(String response) {
		return "{\n" + " \"date\": \"" + Instant.now() + "\",\n" + response + "}";
	}

	public static String appendResponse(String oldResponse, String newResponse) {
		return newResponse + "," + oldResponse;
	}

}