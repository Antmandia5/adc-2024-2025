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
import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeAccountAttributesData;
import org.apache.commons.codec.digest.DigestUtils;

@Path("/changeaccountattributes")
public class ChangeAccountAttributesResource {

    private static final Logger LOG = Logger.getLogger(ChangeAccountAttributesResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final com.google.cloud.datastore.KeyFactory userKeyFactory =
            datastore.newKeyFactory().setKind("User");
    private final Gson gson = new Gson();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeAccountAttributes(ChangeAccountAttributesData data) {
        if (data == null || data.authToken == null || data.targetUser == null) {
            return Response.status(Status.BAD_REQUEST)
                           .entity("Dados insuficientes para a operação.").build();
        }
        
        // Converter o token enviado (JSON) para um objeto AuthToken
        AuthToken callerToken;
        try {
            callerToken = gson.fromJson(data.authToken, AuthToken.class);
        } catch(Exception e) {
            return Response.status(Status.BAD_REQUEST)
                           .entity("Token de autenticação inválido.").build();
        }
        
        // Verificar se o token expirou
        long now = System.currentTimeMillis();
        if(now > callerToken.getValidity().getValidTo()) {
            return Response.status(Status.FORBIDDEN)
                           .entity("Token expirado.").build();
        }
        
        String callerRole = callerToken.getRole();
        
        // Se o caller for ENDUSER, só pode modificar a própria conta.
        if(callerRole.equalsIgnoreCase("ENDUSER") && !callerToken.getUsername().equals(data.targetUser)) {
            return Response.status(Status.FORBIDDEN)
                           .entity("Utilizadores ENDUSER só podem modificar a sua própria conta.").build();
        }
        
        // Se o caller for BACKOFFICE, só pode modificar contas de utilizadores ENDUSER ou PARTNER.
        Key targetKey = userKeyFactory.newKey(data.targetUser);
        Entity targetUserEntity = datastore.get(targetKey);
        if (targetUserEntity == null) {
            return Response.status(Status.NOT_FOUND)
                           .entity("Conta alvo não encontrada.").build();
        }
        
        String targetRole = targetUserEntity.contains("user_role") ?
                targetUserEntity.getString("user_role") : "ENDUSER"; // valor padrão se não definido
        
        if (callerRole.equalsIgnoreCase("BACKOFFICE")) {
            if (!targetRole.equalsIgnoreCase("ENDUSER") && !targetRole.equalsIgnoreCase("PARTNER")) {
                return Response.status(Status.FORBIDDEN)
                           .entity("Utilizadores BACKOFFICE só podem modificar contas com role ENDUSER ou PARTNER.").build();
            }
        }
        
        // Iniciar a transação para atualizar a entidade
        Transaction txn = datastore.newTransaction();
        try {
            Entity.Builder userBuilder = Entity.newBuilder(targetUserEntity);

            // Atualizações permitidas para ENDUSER / PARTNER
            if ( callerRole.equalsIgnoreCase("ENDUSER") || callerRole.equalsIgnoreCase("PARTNER") ) {
                if(data.newPassword != null) {
                    userBuilder.set("user_pwd", DigestUtils.sha512Hex(data.newPassword));
                }
                if(data.newPhone != null) {
                    userBuilder.set("user_phone", data.newPhone);
                }
                if(data.newProfile != null) {
                    userBuilder.set("user_profile", data.newProfile);
                }
                // Atualizar atributos opcionais (permitido modificar ou acrescentar caso não definidos)
                if(data.newCardNumber != null) {
                    userBuilder.set("user_card", data.newCardNumber);
                }
                if(data.newNIF != null) {
                    userBuilder.set("user_nif", data.newNIF);
                }
                if(data.newEmployer != null) {
                    userBuilder.set("user_employer", data.newEmployer);
                }
                if(data.newFunction != null) {
                    userBuilder.set("user_function", data.newFunction);
                }
                if(data.newAddress != null) {
                    userBuilder.set("user_address", data.newAddress);
                }
                if(data.newEmployerNIF != null) {
                    userBuilder.set("user_employer_nif", data.newEmployerNIF);
                }
            }
            // BACKOFFICE
            else if (callerRole.equalsIgnoreCase("BACKOFFICE")) {
                if(data.newPassword != null) {
                    userBuilder.set("user_pwd", DigestUtils.sha512Hex(data.newPassword));
                }
                if(data.newPhone != null) {
                    userBuilder.set("user_phone", data.newPhone);
                }
                if(data.newProfile != null) {
                    userBuilder.set("user_profile", data.newProfile);
                }
                if(data.newName != null) {
                    userBuilder.set("user_name", data.newName);
                }
                if(data.newCardNumber != null) {
                    userBuilder.set("user_card", data.newCardNumber);
                }
                if(data.newNIF != null) {
                    userBuilder.set("user_nif", data.newNIF);
                }
                if(data.newEmployer != null) {
                    userBuilder.set("user_employer", data.newEmployer);
                }
                if(data.newFunction != null) {
                    userBuilder.set("user_function", data.newFunction);
                }
                if(data.newAddress != null) {
                    userBuilder.set("user_address", data.newAddress);
                }
                if(data.newEmployerNIF != null) {
                    userBuilder.set("user_employer_nif", data.newEmployerNIF);
                }
            }
            // ADMIN
            else if (callerRole.equalsIgnoreCase("ADMIN")) {
                if(data.newPassword != null) {
                    userBuilder.set("user_pwd", DigestUtils.sha512Hex(data.newPassword));
                }
                if(data.newPhone != null) {
                    userBuilder.set("user_phone", data.newPhone);
                }
                if(data.newProfile != null) {
                    userBuilder.set("user_profile", data.newProfile);
                }
                if(data.newName != null) {
                    userBuilder.set("user_name", data.newName);
                }
                if(data.newEmail != null) {
                    userBuilder.set("user_email", data.newEmail);
                }
                if(data.newRole != null) {
                    userBuilder.set("user_role", data.newRole.toUpperCase());
                }
                if(data.newStatus != null) {
                    userBuilder.set("user_status", data.newStatus.toUpperCase());
                }
                if(data.newCardNumber != null) {
                    userBuilder.set("user_card", data.newCardNumber);
                }
                if(data.newNIF != null) {
                    userBuilder.set("user_nif", data.newNIF);
                }
                if(data.newEmployer != null) {
                    userBuilder.set("user_employer", data.newEmployer);
                }
                if(data.newFunction != null) {
                    userBuilder.set("user_function", data.newFunction);
                }
                if(data.newAddress != null) {
                    userBuilder.set("user_address", data.newAddress);
                }
                if(data.newEmployerNIF != null) {
                    userBuilder.set("user_employer_nif", data.newEmployerNIF);
                }
            }

            // Atualizar a entidade no Datastore
            Entity updatedUser = userBuilder.build();
            txn.put(updatedUser);
            txn.commit();
            
            String successMsg = String.format("Conta %s atualizada com sucesso.", data.targetUser);
            LOG.info(successMsg);
            return Response.ok("{\"message\":\"" + successMsg + "\"}").build();
        }
        catch(Exception e) {
            txn.rollback();
            LOG.severe("Erro ao atualizar atributos da conta: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                           .entity("Erro interno ao atualizar conta.").build();
        }
        finally {
            if(txn.isActive()){
                txn.rollback();
            }
        }
    }
}
