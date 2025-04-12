package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.UUID;

public class AuthToken {

	public static final long EXPIRATION_TIME = 1000*60*60*2;

	public String username;
	public String tokenID;
	public String role;
	private long validUntil;
	private String verifier;

	public AuthToken() {

	}

	public AuthToken(String username, String role, String tokenID) {
		long now = System.currentTimeMillis();
		this.username = username;
		this.role = role;
		this.tokenID = tokenID;
		this.validUntil = now + EXPIRATION_TIME;
		this.verifier = UUID.randomUUID().toString();
	}

	public String getUsername() {
		return username;
	}

	public String getRole() {
		return role;
	}

	public String getTokenID() {
		return tokenID;
	}
	
	public long getValidUntil() {
		return validUntil;
	}

	public String getVerifier() {
		return verifier;
	}

	public String toJSON() {
		return String.format("{\"username\":\"%s\", \"role\":\"%s\", \"tokenID\":\"%s\", " +"\"validUntil\":%d}",
				username, role, tokenID, validUntil);
	}

}
