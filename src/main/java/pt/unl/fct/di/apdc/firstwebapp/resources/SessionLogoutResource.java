package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
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
    private final Gson gson = new Gson();
    
    // Para armazenar tokens revogados, cria-se um KeyFactory para a entidade "RevokedToken".
    private static final com.google.cloud.datastore.KeyFactory revokedTokenKeyFactory =
            datastore.newKeyFactory().setKind("RevokedToken");

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sessionLogout(SessionLogoutData data) {
        if (data == null || data.authToken == null) {
            return Response.status(Status.BAD_REQUEST)
                           .entity("Dados insuficientes para a operação.").build();
        }
        
        // Converter o token recebido para objeto AuthToken
        AuthToken token;
        try {
            token = gson.fromJson(data.authToken, AuthToken.class);
        } catch(Exception e) {
            return Response.status(Status.BAD_REQUEST)
                           .entity("Token de autenticação inválido.").build();
        }
        
        // Registar o token como revogado, armazenando o tokenID e o timestamp de revogação.
        // Mesmo que o token ainda não esteja expirado, ele passa a ser considerado inválido.
        try {
            Key revokedKey = revokedTokenKeyFactory.newKey(token.getTokenID());
            Entity revokedToken = Entity.newBuilder(revokedKey)
                    .set("tokenID", token.getTokenID())
                    .set("revokedAt", Timestamp.now())
                    .build();
            datastore.put(revokedToken);
        } catch (DatastoreException e) {
            LOG.log(Level.SEVERE, "Erro ao revogar token: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                           .entity("Erro interno ao efetuar logout.").build();
        }
        
        LOG.info("Token revogado para o utilizador: " + token.getUsername());
        // Retorna mensagem de logout com sucesso.
        return Response.ok("{\"message\":\"Logout efetuado com sucesso.\"}").build();
    }
}
