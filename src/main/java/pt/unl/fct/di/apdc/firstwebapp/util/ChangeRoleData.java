package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeRoleData {
    // O token deve ser o JSON gerado pelo AuthToken após o login.
    public String authToken;
    public String targetUsername;
    public String newRole;
    
    public ChangeRoleData() {
    }
    
    public ChangeRoleData(String authToken, String targetUsername, String newRole) {
        this.authToken = authToken;
        this.targetUsername = targetUsername;
        this.newRole = newRole;
    }
}