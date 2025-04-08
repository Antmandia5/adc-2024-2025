package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeAccountStateData {
    // Token de autenticação em formato JSON (gerado pelo AuthToken)
    public String authToken;
    // Username da conta cujo estado se pretende alterar
    public String targetUsername;
    // Novo estado a ser atribuído (por exemplo, "ATIVADA" ou "DESATIVADA")
    public String newState;
    
    public ChangeAccountStateData() { }
    
    public ChangeAccountStateData(String authToken, String targetUsername, String newState) {
        this.authToken = authToken;
        this.targetUsername = targetUsername;
        this.newState = newState;
    }
}
