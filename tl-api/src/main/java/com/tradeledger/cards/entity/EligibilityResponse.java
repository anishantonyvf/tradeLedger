package com.tradeledger.cards.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table(value = "EligibilityResponse")
public class EligibilityResponse {

	@PrimaryKeyColumn(name = "eligibilityidentifier", type = PrimaryKeyType.PARTITIONED)
	@Id
	private String eligibilityidentifier;
	private String response;

	public String getResponse() {
		return response;
	}

	public EligibilityResponse(String eligibilityidentifier, String response) {

		this.eligibilityidentifier = eligibilityidentifier;
		this.response = response;
	}

}