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

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.SessionLogoutData;

@Path("/sessionlogout")
public class SessionLogoutResource {

    private static final Logger LOG = Logger.getLogger(SessionLogoutResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final com.google.cloud.datastore.KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	private static final com.google.cloud.datastore.KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("AuthToken");
    private final Gson gson = new Gson();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sessionLogout(SessionLogoutData data) {
        if (data == null || data.authToken == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Dados insuficientes para a operação.").build();
        }

        AuthToken token;
        try {
            token = gson.fromJson(data.authToken, AuthToken.class);
        } catch (Exception e) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Token de autenticação inválido.").build();
        }

        Key tokenKey = tokenKeyFactory.newKey(token.getTokenID());
        Key userKey = userKeyFactory.newKey(token.getUsername());

        Transaction txn = datastore.newTransaction();
        try {
            Entity tokenEntity = datastore.get(tokenKey);
            if (tokenEntity == null) {
                return Response.status(Status.NOT_FOUND)
                        .entity("Token não encontrado ou já removido.").build();
            }
            
			// Remover tokenID a utilizador
            Entity user = txn.get(userKey);
			Entity updatedUser = Entity.newBuilder(user).set("tokenID", "").build();
			
            datastore.delete(tokenKey);
            
            txn.put(updatedUser);
			txn.commit();

            LOG.info("Token removido com sucesso: " + token.getTokenID());
            return Response.ok("{\"message\":\"Logout efetuado com sucesso.\"}").build();
        } catch (DatastoreException e) {
        	txn.rollback();
            LOG.log(Level.SEVERE, "Erro ao aceder ao datastore: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Erro interno no servidor.").build();
        }
    }
}
