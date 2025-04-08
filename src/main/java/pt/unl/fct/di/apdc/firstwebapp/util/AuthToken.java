package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.UUID;

public class AuthToken {

	public static final long EXPIRATION_TIME = 1000*60*60*2;
	
	public String username;
	public String tokenID;
	public String role;
	private Validity validity;
	
	public static class Validity{
		private long validFrom;
		private long validTo;
		private String verifier;
		
		public Validity(long validFrom, long validTo) {
			this.validFrom = validFrom;
			this.validTo = validTo;
			this.verifier = UUID.randomUUID().toString();
		}
		
		public long getValidFrom() {
			return validFrom;
		}
		
		public long getValidTo() {
			return validTo;
		}
		
		public String getVerifier() {
			return verifier;
		}
	}
	
	public AuthToken() {

	}
	
	public AuthToken(String username, String role) {
		long now = System.currentTimeMillis();
		this.username = username;
		this.role = role;
		this.tokenID = UUID.randomUUID().toString();
		this.validity = new Validity(now, now+ EXPIRATION_TIME);
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
    
    public Validity getValidity() {
        return validity;
    }
    
    /**
     * Método opcional para serializar o token em JSON.
     * Se estiver a usar uma biblioteca como Gson, pode simplesmente converter a instância.
     */
    public String toJSON() {
        return String.format("{\"user\":\"%s\", \"role\":\"%s\", \"tokenID\":\"%s\", " +
                             "\"validity\":{\"validFrom\":%d, \"validTo\":%d, \"verifier\":\"%s\"}}",
                             username, role, tokenID, validity.getValidFrom(), validity.getValidTo(), validity.getVerifier());
    }
	
}
