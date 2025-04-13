package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.Value;
import com.google.gson.Gson;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.servlet.http.HttpServletRequest;
import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	private static final String MESSAGE_INVALID_CREDENTIALS = "Incorrect username or password.";
	private static final String LOG_MESSAGE_LOGIN_ATTEMP = "Login attempt by user: ";
	private static final String LOG_MESSAGE_LOGIN_SUCCESSFUL = "Login successful by user: ";
	private static final String LOG_MESSAGE_WRONG_PASSWORD = "Wrong password for: ";
	private static final String USER_PWD = "user_pwd";
	private static final String USER_ROLE = "user_role";
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final com.google.cloud.datastore.KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	private static final com.google.cloud.datastore.KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("AuthToken");

	private final Gson g = new Gson();

	public LoginResource() {
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data,
			@Context HttpServletRequest request,
			@Context HttpHeaders headers) {
		LOG.fine(LOG_MESSAGE_LOGIN_ATTEMP + data.username);

		Key userKey = userKeyFactory.newKey(data.username);

		Transaction txn = datastore.newTransaction();
		try {
			Entity user = txn.get(userKey);
			if (user == null) {
				LOG.warning(LOG_MESSAGE_LOGIN_ATTEMP + data.username);
				return Response.status(Status.FORBIDDEN)
						.entity(MESSAGE_INVALID_CREDENTIALS)
						.build();
			}

			String hashedPWD = user.getString(USER_PWD);
			if (hashedPWD.equals(DigestUtils.sha512Hex(data.password))) {
				String role = user.contains(USER_ROLE) ? user.getString(USER_ROLE) : "ENDUSER";
				
				// Cria o token com os dados do user e o role
				String newTokenID = UUID.randomUUID().toString();
				AuthToken token = new AuthToken(data.username, role, newTokenID);
				
				// Adicionar tokenID a utilizador
				Entity updatedUser = Entity.newBuilder(user).set("tokenID", newTokenID).build();
				
				// Cria uma chave para o token
				Key tokenKey = tokenKeyFactory.newKey(newTokenID);
				Entity tokenEntity = Entity.newBuilder(tokenKey)
						.set("username", data.username)
						.set("role", role)
						.set("validUntil", token.getValidUntil())
						.set("tokenID", newTokenID)
						.build();
				txn.put(tokenEntity);
				txn.put(updatedUser);
				txn.commit();

				LOG.info(LOG_MESSAGE_LOGIN_SUCCESSFUL + data.username);

				return Response.ok(token.toJSON(), MediaType.TEXT_PLAIN).build();
			} else {
				txn.commit();
				LOG.warning(LOG_MESSAGE_WRONG_PASSWORD + data.username);
				return Response.status(Status.FORBIDDEN).entity(MESSAGE_INVALID_CREDENTIALS).build();
			}
		} catch (DatastoreException e) {
			txn.rollback();
			LOG.log(Level.SEVERE, e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}
}
