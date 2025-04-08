package pt.unl.fct.di.apdc.firstwebapp.util;

public class RemoveUserAccountData {
	
	//Token de autenticacao enviado pelo user (json)
	public String authToken;
	
	//Identificador da conta a ser removida (username ou email)
	public String targetUser;
	
	public RemoveUserAccountData() {
		
	}
	
	public RemoveUserAccountData(String authToken, String targetUser) {
		this.authToken = authToken;
		this.targetUser = targetUser;
	}

}
