package com.tradeledger.cards.common;

import javax.validation.constraints.NotNull;

public final class Applicant {

	@NotNull
	private String name;

	@NotNull
	private String address;

	@NotNull
	private String email;

	public Applicant() {
	}

	public Applicant(String name, String email, String address) {

		this.name = name;
		this.email = email;
		this.address = address;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public String getEmail() {
		return email;
	}

	public String getKey() {
		return name + email + address;
	}

	public String requestString() {
		return "{\n" + " \"name\": \"" + name + "\",\n" + " \"address\": \"" + address + "\",\n" + " \"email\": \""
				+ email + "\"\n" + "}";
	}
}