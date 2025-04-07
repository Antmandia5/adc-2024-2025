package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.UUID;

public class AuthToken {

    public static final long EXPIRATION_TIME = 1000 * 60 * 60 * 2; // 2 horas

    private String username;
    private String tokenID;
    private long issuedAt;
    private long expirationTime;
    private String role;

    // Construtor vazio para frameworks de serialização
    public AuthToken() {
    }

    // Construtor que recebe username e role
    public AuthToken(String username) {
        this.username = username;
        this.tokenID = UUID.randomUUID().toString();
        this.issuedAt = System.currentTimeMillis();
        this.expirationTime = this.issuedAt + EXPIRATION_TIME;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getTokenID() {
        return tokenID;
    }

    public long getIssuedAt() {
        return issuedAt;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public String getRole() {
        return role;
    }

    /**
     * Verifica se o token está expirado.
     * @return true se o token estiver expirado; false caso contrário.
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }

    /**
     * Representa o token em formato JSON.
     * Exemplo: {"username":"user1", "tokenID":"...", "issuedAt":1610000000000, "expirationTime":1610076000000, "role":"admin"}
     * @return String no formato JSON representando o token.
     */
    public String toJSON() {
        return String.format("{\"username\":\"%s\", \"tokenID\":\"%s\", \"issuedAt\":%d, \"expirationTime\":%d, \"role\":\"%s\"}",
                username, tokenID, issuedAt, expirationTime, role);
    }
}