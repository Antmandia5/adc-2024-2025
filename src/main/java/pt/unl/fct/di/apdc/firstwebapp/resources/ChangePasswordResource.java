package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;
import java.util.logging.Logger;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangePasswordData;

@Path("/changepassword")
public class ChangePasswordResource {

	private static final Logger LOG = Logger.getLogger(ChangePasswordResource.class.getName());
	private static final String USER_PWD = "user_pwd";

	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final com.google.cloud.datastore.KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	private static final com.google.cloud.datastore.KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("AuthToken");
	private final Gson gson = new Gson();

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response changePassword(ChangePasswordData data) {
		if (data == null || data.authToken == null || data.currentPassword == null ||
				data.newPassword == null || data.newPasswordConfirmation == null) {
			return Response.status(Status.BAD_REQUEST)
					.entity("Dados insuficientes para a operação.").build();
		}

		// Converter o token de autenticação a partir do JSON
		AuthToken callerToken;
		try {
			callerToken = gson.fromJson(data.authToken, AuthToken.class);
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST)
					.entity("Token de autenticação inválido.").build();
		}

		// Verificar se o token está registado no Datastore
		Key tokenKey = tokenKeyFactory.newKey(callerToken.getTokenID());
		Entity tokenEntity = datastore.get(tokenKey);
		if (tokenEntity == null) {
			return Response.status(Status.FORBIDDEN)
					.entity("Token inválido ou sessão inexistente.").build();
		}

		// Verificar se o token expirou
		long now = System.currentTimeMillis();
		if (now > callerToken.getValidUntil()) {
			return Response.status(Status.FORBIDDEN)
					.entity("Token expirado.").build();
		}

		String callerUsername = callerToken.getUsername();
		if (callerUsername == null) {
			return Response.status(Status.BAD_REQUEST)
					.entity("Token sem informação de utilizador.").build();
		}

		if (!data.newPassword.equals(data.newPasswordConfirmation)) {
			return Response.status(Status.BAD_REQUEST)
					.entity("Nova password e confirmação não coincidem.").build();
		}

		Key userKey = userKeyFactory.newKey(callerUsername);
		Entity userEntity = datastore.get(userKey);

		if (userEntity == null) {
			return Response.status(Status.NOT_FOUND)
					.entity("Conta do utilizador não encontrada.").build();
		}

		// Verificar se a password atual fornecida corresponde à armazenada (após hash)
		String storedHash = userEntity.getString(USER_PWD);
		if (!storedHash.equals(DigestUtils.sha512Hex(data.currentPassword))) {
			return Response.status(Status.FORBIDDEN)
					.entity("Password atual incorreta.").build();
		}

		// Atualizar com nova password
		Transaction txn = datastore.newTransaction();
		try {
			Entity updatedUser = Entity.newBuilder(userEntity)
					.set(USER_PWD, DigestUtils.sha512Hex(data.newPassword))
					.build();
			txn.put(updatedUser);
			txn.commit();
			LOG.info("Password alterada com sucesso para o utilizador: " + callerUsername);
			return Response.ok("{\"message\":\"Password atualizada com sucesso.\"}").build();
		} catch(Exception e) {
			txn.rollback();
			LOG.severe("Erro ao atualizar password: " + e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Erro interno ao atualizar password.").build();
		} finally {
			if(txn.isActive()){
				txn.rollback();
			}
		}
	}
}
