package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangePasswordData {
	public String authToken;
	public String currentPassword;
	public String newPassword;
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
