package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.StructuredQuery;
import com.google.cloud.datastore.QueryResults;
import com.google.gson.Gson;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.ListUsersData;

@Path("/listusers")
public class ListUsersResource {

    private static final Logger LOG = Logger.getLogger(ListUsersResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private final Gson gson = new Gson();

    // Função auxiliar para obter propriedade como String ou "NOT DEFINED"
    private String getStringProp(Entity user, String propName) {
        return user.contains(propName) ? user.getString(propName) : "NOT DEFINED";
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUsers(ListUsersData data) {
        // Validação dos dados de entrada
        if (data == null || data.authToken == null) {
            return Response.status(Status.BAD_REQUEST)
                           .entity("Dados insuficientes para a operação.").build();
        }
        
        // Converter o token recebido para objeto AuthToken
        AuthToken callerToken;
        try {
            callerToken = gson.fromJson(data.authToken, AuthToken.class);
        } catch(Exception e) {
            return Response.status(Status.BAD_REQUEST)
                           .entity("Token de autenticação inválido.").build();
        }
        
        // Verificar se o token está expirado
        long now = System.currentTimeMillis();
        if (now > callerToken.getValidity().getValidTo()) {
            return Response.status(Status.FORBIDDEN)
                           .entity("Token expirado.").build();
        }
        
        String callerRole = callerToken.getRole();
        
        // Para simplificar, vamos buscar todas as entidades "User"
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("User")
                .build();
        QueryResults<Entity> results = datastore.run(query);
        
        List<Object> filteredList = new ArrayList<>();
        
        while (results.hasNext()) {
            Entity user = results.next();
            
            // Obter os atributos comuns
            String username = getStringProp(user, "user_username");
            String email = getStringProp(user, "user_email");
            String name = getStringProp(user, "user_name");
            String role = getStringProp(user, "user_role");
            String profile = getStringProp(user, "user_profile");
            String status = getStringProp(user, "user_status");
            
            if (callerRole.equalsIgnoreCase("ENDUSER")) {
                // ENDUSER: listar apenas utilizadores com role ENDUSER,
                // perfil "público" e estado "ATIVADA"
                if (role.equalsIgnoreCase("ENDUSER") &&
                    profile.equalsIgnoreCase("público") &&
                    status.equalsIgnoreCase("ATIVADA")) {
                    
                    filteredList.add(new SimpleUser(username, email, name));
                }
            } else if (callerRole.equalsIgnoreCase("BACKOFFICE")) {
                // BACKOFFICE: listar todos os usuários com role ENDUSER, sem restrição de perfil/estado
                if (role.equalsIgnoreCase("ENDUSER")) {
                    filteredList.add(new FullUser(user));
                }
            } else if (callerRole.equalsIgnoreCase("ADMIN")) {
                // ADMIN: listar todos os usuários, independentemente de role, perfil ou estado
                filteredList.add(new FullUser(user));
            }
        }
        
        return Response.ok(gson.toJson(filteredList)).build();
    }
    
    // Classe auxiliar para utilizadores simples – para quem tem role ENDUSER
    public class SimpleUser {
        public String username;
        public String email;
        public String name;
        
        public SimpleUser(String username, String email, String name) {
            this.username = username;
            this.email = email;
            this.name = name;
        }
    }
    
    // Classe auxiliar para representar a listagem completa dos atributos (excetuando password e outros dados sensíveis)
    public class FullUser {
        public String username;
        public String email;
        public String name;
        public String phone;
        public String profile;
        public String role;
        public String status;
        public String creationTime;
        
        public FullUser(Entity user) {
            this.username = getStringProp(user, "user_username");
            this.email = getStringProp(user, "user_email");
            this.name = getStringProp(user, "user_name");
            this.phone = getStringProp(user, "user_phone");
            this.profile = getStringProp(user, "user_profile");
            this.role = getStringProp(user, "user_role");
            this.status = getStringProp(user, "user_status");
            this.creationTime = user.contains("user_creation_time")
                    ? user.getTimestamp("user_creation_time").toString()
                    : "NOT DEFINED";
        }
    }
}
