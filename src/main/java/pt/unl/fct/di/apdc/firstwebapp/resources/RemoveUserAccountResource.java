package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.apdc.firstwebapp.util.RemoveUserAccountData;
import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;

@Path("/removeuseraccount")
public class RemoveUserAccountResource {
	
	private static final Logger LOG = Logger.getLogger(RemoveUserAccountResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	//A keyfactory para a entidade "User", indexada pelo username
	private static final com.google.cloud.datastore.KeyFactory userKeyFactory =
			datastore.newKeyFactory().setKind("User");
	private final Gson gson = new Gson();
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeUserAccount(RemoveUserAccountData data) {
		//Verificar se os dados enviados estao completos
		if (data == null || data.authToken == null || data.targetUser == null) {
			return Response.status(Status.BAD_REQUEST)
					.entity("Dados insuficientes para a operação.").build();
		}
		
		//Converter o token enviado para objeto AuthToken
		AuthToken callerToken;
		try {
			callerToken = gson.fromJson(data.authToken, AuthToken.class);
		} catch(Exception e) {
			return Response.status(Status.BAD_REQUEST)
					.entity("Token de autenticação inválido").build();
		}
		
		//Verificar se o token expirou
		long now = System.currentTimeMillis();
		if (now > callerToken.getValidity().getValidTo()) {
			return Response.status(Status.FORBIDDEN)
					.entity("Token expirado.").build();
		}
		
		String callerRole = callerToken.getRole();
		//Apenas ADMIN e BACKOFFICE podem realizar a remoção
		if (!(callerRole.equalsIgnoreCase("ADMIN") || callerRole.equalsIgnoreCase("BACKOFFICE"))) {
			return Response.status(Status.FORBIDDEN)
					.entity("Utilizador sem permissões para remoção de contas.").build();
		}
		
		// Obter conta a ser removida
		Key targetKey = userKeyFactory.newKey(data.targetUser);
		Entity targetUser;
		try {
			targetUser = datastore.get(targetKey);
			if (targetUser == null) {
				return Response.status(Status.NOT_FOUND)
						.entity("Utilizador alvo não encontrado.").build();
			}
		} catch(DatastoreException e) {
			LOG.log(Level.SEVERE, "Erro ao aceder ao datastore " + e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Erro interno no servidor.").build();
		}
		
		//Se o caller é BACKOFFICE, só pode remover contas com role ENDUSER ou PARTNER
		if (callerRole.equalsIgnoreCase("BACKOFFICE")) {
			String targetRole = targetUser.contains("user_role") ? targetUser.getString("user_role") : "ENDUSER";
			if (!(targetRole.equalsIgnoreCase("ENDUSER") || targetRole.equalsIgnoreCase("PARTNER"))) {
				return Response.status(Status.FORBIDDEN)
						.entity("Utilizador BACKOFFICE não pode remover contas com role " + targetRole + ".")
						.build();
			}
		}
		
		//Remover a conta usando uma transação para garantir integridade
		Transaction txn = datastore.newTransaction();
		try {
			txn.delete(targetKey);
			txn.commit();
			String successMsg = String.format("Conta %s removida com sucesso", data.targetUser);
			LOG.info(successMsg);
			return Response.ok("{\"message\":\"" + successMsg + "\"}").build();
		} catch(DatastoreException e ) {
			txn.rollback();
			LOG.log(Level.SEVERE, "Erro ao remover a conta: " + e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Erro interno ao remover a conta.").build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
		
		
	}
}
