package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeAccountAttributesData {
	//Token de autenticacao em JSON enviado pelo cliente
	public String authToken;
	// Identificador da conta a modificar
	public String targetUser;
	
	//Atributos a atualizar (opcionais)
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
