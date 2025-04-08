package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangePasswordData {
	public String authToken;
	//A password atual (não hasheada) fornecida pelo utilizador
	public String currentPassword;
	//A nova password pretendida
	public String newPassword;
	//Confirmaçao da nova password
	public String newPasswordConfirmation;
	
	public ChangePasswordData() {
		
	}
	
	public ChangePasswordData(String authToken, String currentPassword, String newPassword, String newPasswordConfirmation) {
		this.authToken = authToken;
		this.currentPassword = currentPassword;
		this.newPassword = newPassword;
		this.newPasswordConfirmation = newPasswordConfirmation;
	}

}
