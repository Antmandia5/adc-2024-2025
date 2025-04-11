package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeAccountStateData {
    public String authToken;
    public String targetUsername;
    public String newState;
    
    public ChangeAccountStateData() { }
    
    public ChangeAccountStateData(String authToken, String targetUsername, String newState) {
        this.authToken = authToken;
        this.targetUsername = targetUsername;
        this.newState = newState;
    }
}
