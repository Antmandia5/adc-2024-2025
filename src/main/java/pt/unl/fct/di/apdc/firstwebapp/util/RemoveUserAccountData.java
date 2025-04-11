package pt.unl.fct.di.apdc.firstwebapp.util;

public class RemoveUserAccountData {
	
	public String authToken;
	public String targetUser;
	
	public RemoveUserAccountData() {
		
	}
	
	public RemoveUserAccountData(String authToken, String targetUser) {
		this.authToken = authToken;
		this.targetUser = targetUser;
	}

}
