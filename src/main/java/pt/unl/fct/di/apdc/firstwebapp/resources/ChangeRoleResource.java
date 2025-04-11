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
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeRoleData;
import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;

@Path("/changerole")
public class ChangeRoleResource {

    private static final Logger LOG = Logger.getLogger(ChangeRoleResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final com.google.cloud.datastore.KeyFactory userKeyFactory = 
            datastore.newKeyFactory().setKind("User");
    
    private final Gson gson = new Gson();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeRole(ChangeRoleData data) {
        if (data == null || data.authToken == null || data.targetUsername == null || data.newRole == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Dados insuficientes para a operação.").build();
        }

        AuthToken callerToken;
        try {
            callerToken = gson.fromJson(data.authToken, AuthToken.class);
        } catch(Exception e) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Token de autenticação inválido.").build();
        }
        
        // Verificar se o token expirou
        long now = System.currentTimeMillis();
        if(now > callerToken.getValidity().getValidTo()){
            return Response.status(Status.FORBIDDEN)
                    .entity("Token expirado.").build();
        }
        
        String callerRole = callerToken.getRole();
        
        if(callerRole.equalsIgnoreCase("ENDUSER")){
            return Response.status(Status.FORBIDDEN)
                    .entity("Utilizador sem permissões para alteração de roles.").build();
        }
        
        Key targetKey = userKeyFactory.newKey(data.targetUsername);
        Entity targetUser;
        try {
            targetUser = datastore.get(targetKey);
            if (targetUser == null) {
                return Response.status(Status.NOT_FOUND)
                        .entity("Utilizador alvo não encontrado.").build();
            }
        } catch (DatastoreException e) {
            LOG.log(Level.SEVERE, "Erro ao aceder ao datastore: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Erro interno no servidor.").build();
        }
        
        String currentRole = targetUser.contains("user_role") ? targetUser.getString("user_role") : "enduser";
        
        if(callerRole.equalsIgnoreCase("BACKOFFICE")){
            if( !( (currentRole.equalsIgnoreCase("ENDUSER") && data.newRole.equalsIgnoreCase("PARTNER"))
                || (currentRole.equalsIgnoreCase("PARTNER") && data.newRole.equalsIgnoreCase("ENDUSER")) ) ){
                return Response.status(Status.FORBIDDEN)
                        .entity("Operação não permitida para um utilizador BACKOFFICE.").build();
            }
        }
        
        Transaction txn = datastore.newTransaction();
        try {
            Entity updatedUser = Entity.newBuilder(targetUser)
                    .set("user_role", data.newRole.toUpperCase())
                    .build();
            txn.put(updatedUser);
            txn.commit();
            String successMsg = String.format("O role do utilizador %s foi alterado para %s com sucesso.", 
                                    data.targetUsername, data.newRole.toUpperCase());
            LOG.info(successMsg);
            return Response.ok("{\"message\":\"" + successMsg + "\"}").build();
        } catch (DatastoreException e) {
            txn.rollback();
            LOG.log(Level.SEVERE, "Erro durante a atualização da conta: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Erro interno no servidor ao atualizar o utilizador.").build();
        } finally {
            if(txn.isActive()){
                txn.rollback();
            }
        }
    }
}
