package pt.unl.fct.di.apdc.firstwebapp.util;

public class RegisterData {
	
	public String username;
	public String password;
	public String confirmation;
	public String email;
	public String name;
	public String phone;
	public String profile;
	
	public RegisterData() {
	}
	
	public RegisterData(String username, String password, String confirmation, String email, String name, String phone, String profile) {
		this.username = username;
		this.password = password;
		this.confirmation = confirmation;
		this.email = email;
		this.name = name;
		this.phone = phone;
		this.profile = profile;
	}
	
	private boolean nonEmptyOrBlankField(String field) {
		return field != null && !field.isBlank();
	}
	
	public boolean validRegistration() {
		return nonEmptyOrBlankField(username) &&
			   nonEmptyOrBlankField(password) &&
			   nonEmptyOrBlankField(confirmation) &&
			   nonEmptyOrBlankField(email) &&
			   nonEmptyOrBlankField(name) &&
			   nonEmptyOrBlankField(phone) &&
			   nonEmptyOrBlankField(profile) &&
			   email.contains("@") &&
			   password.equals(confirmation);
	}
}