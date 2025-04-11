package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeAccountAttributesData {
	public String authToken;
	public String targetUser;
	
	//Atributos a atualizar
	public String newPassword;
	public String newPhone;
	public String newProfile;
	public String newName;
	public String newEmail;
	public String newRole;
	public String newStatus;
	//Atributos opcionais adicionais
	public String newCardNumber;
	public String newNIF;
	public String newEmployer;
	public String newFunction;
	public String newAddress;
	public String newEmployerNIF;
	
	public ChangeAccountAttributesData(){
		
	}
	
	public ChangeAccountAttributesData(String authToken, String targetUser) {
		this.authToken = authToken;
		this.targetUser = targetUser;
	}

}
