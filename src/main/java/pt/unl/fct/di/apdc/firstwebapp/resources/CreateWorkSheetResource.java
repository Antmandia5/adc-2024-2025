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
import pt.unl.fct.di.apdc.firstwebapp.util.CreateWorkSheetData;

@Path("/createworksheet")
public class CreateWorkSheetResource {

    private static final Logger LOG = Logger.getLogger(CreateWorkSheetResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    // Usaremos a referência da obra como chave (única) na entidade WorkSheet
    private static final com.google.cloud.datastore.KeyFactory worksheetKeyFactory =
            datastore.newKeyFactory().setKind("WorkSheet");
    private final Gson gson = new Gson();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createOrUpdateWorkSheet(CreateWorkSheetData data) {
        if (data == null || data.authToken == null || data.reference == null ||
            data.description == null || data.targetType == null || data.adjudicationStatus == null) {
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
        if(now > callerToken.getValidUntil()){
            return Response.status(Status.FORBIDDEN)
                    .entity("Token expirado.").build();
        }
        
        String callerRole = callerToken.getRole();
        
        boolean isCreation = false;
        Key worksheetKey = worksheetKeyFactory.newKey(data.reference);
        Entity worksheetEntity = datastore.get(worksheetKey);
        
        if (worksheetEntity == null) {
            isCreation = true;
            if (!callerRole.equalsIgnoreCase("BACKOFFICE")) {
                return Response.status(Status.FORBIDDEN)
                        .entity("Apenas utilizadores BACKOFFICE podem criar novas folhas de obra.").build();
            }
        }
        
        Transaction txn = datastore.newTransaction();
        try {
            Entity.Builder wsBuilder;
            if (isCreation) {
                // Criar nova folha com os atributos obrigatórios
                wsBuilder = Entity.newBuilder(worksheetKey)
                        .set("reference", data.reference)
                        .set("description", data.description)
                        .set("targetType", data.targetType)
                        .set("adjudicationStatus", data.adjudicationStatus.toUpperCase());
            } else {
                // Atualizar a folha existente
                wsBuilder = Entity.newBuilder(worksheetEntity);
                if (callerRole.equalsIgnoreCase("BACKOFFICE")) {
                    wsBuilder.set("description", data.description)
                             .set("targetType", data.targetType)
                             .set("adjudicationStatus", data.adjudicationStatus.toUpperCase());
                }
            }
            
            // Se a folha de obra for adjudicada, preenche os atributos adicionais.
            if (data.adjudicationStatus.equalsIgnoreCase("ADJUDICADO")) {
                if (callerRole.equalsIgnoreCase("BACKOFFICE")) {
                    if (data.adjudicationDate != null) {
                        wsBuilder.set("adjudicationDate", data.adjudicationDate);
                    }
                    if (data.expectedStartDate != null) {
                        wsBuilder.set("expectedStartDate", data.expectedStartDate);
                    }
                    if (data.expectedCompletionDate != null) {
                        wsBuilder.set("expectedCompletionDate", data.expectedCompletionDate);
                    }
                    if (data.partnerAccount != null) {
                        wsBuilder.set("partnerAccount", data.partnerAccount);
                    }
                    if (data.adjudicationEntity != null) {
                        wsBuilder.set("adjudicationEntity", data.adjudicationEntity);
                    }
                    if (data.companyNIF != null) {
                        wsBuilder.set("companyNIF", data.companyNIF);
                    }
                    if (data.workState != null) {
                        wsBuilder.set("workState", data.workState.toUpperCase());
                    }
                    if (data.observations != null) {
                        wsBuilder.set("observations", data.observations);
                    }
                } else if (callerRole.equalsIgnoreCase("PARTNER")) {
                    // Se o utilizador for PARTNER, ele só pode atualizar o estado da obra
                    String assignedPartner = worksheetEntity != null && worksheetEntity.contains("partnerAccount")
                            ? worksheetEntity.getString("partnerAccount")
                            : null;
                    
                    if (assignedPartner == null || !assignedPartner.equalsIgnoreCase(callerToken.getUsername())) {
                        return Response.status(Status.FORBIDDEN)
                                .entity("Folha de obra não atribuída à conta PARTNER atual, ou não existe.")
                                .build();
                    }
                    // Atualizar apenas o estado da obra e/ou observações se forem fornecidos
                    if (data.workState != null) {
                        wsBuilder.set("workState", data.workState.toUpperCase());
                    }
                    if (data.observations != null) {
                        wsBuilder.set("observations", data.observations);
                    }
                }
            } else {
                // Se não está adjudicada, os atributos adicionais não devem ser preenchidos.
                wsBuilder.set("adjudicationDate", "")
                         .set("expectedStartDate", "")
                         .set("expectedCompletionDate", "")
                         .set("partnerAccount", "")
                         .set("adjudicationEntity", "")
                         .set("companyNIF", "")
                         .set("workState", "")
                         .set("observations", "");
            }
            
            Entity wsEntity = wsBuilder.build();
            txn.put(wsEntity);
            txn.commit();
            
            String msg = isCreation
                    ? "Folha de obra criada com sucesso."
                    : "Folha de obra atualizada com sucesso.";
            LOG.info(msg + " Ref: " + data.reference);
            return Response.ok("{\"message\":\"" + msg + "\"}").build();
        } catch(Exception e) {
            txn.rollback();
            LOG.severe("Erro ao registar/atualizar folha de obra: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Erro interno ao processar folha de obra.").build();
        } finally {
            if(txn.isActive()){
                txn.rollback();
            }
        }
    }
}
