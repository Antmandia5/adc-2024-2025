package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;

@Path("/register")
public class RegisterResource {

    private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private final Gson g = new Gson();

    public RegisterResource() {
        // Construtor padrão
    }	

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerUser(RegisterData data) {
        LOG.fine("Attempt to register user: " + data.username);
		
        if (!data.validRegistration()) {
            return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();
        }
		
        try {
            // Cria a chave para a entidade User usando o username
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			
            // Cria a entidade com os campos obrigatórios e os valores por omissão para role e estado
            Entity user = Entity.newBuilder(userKey)
                    .set("user_name", data.name)
                    .set("user_pwd", DigestUtils.sha512Hex(data.password))
                    .set("user_email", data.email)
                    .set("user_phone", data.phone)
                    .set("user_profile", data.profile)
                    .set("user_creation_time", Timestamp.now())    
                    // Valores padrão para o registo de nova conta
                    .set("user_role", "enduser")
                    .set("user_status", "DESATIVADA")
                    .build();

            datastore.add(user);
            LOG.info("User registered " + data.username);
			
            return Response.ok().build();
        } catch (DatastoreException e) {
            LOG.log(Level.ALL, e.toString());
            return Response.status(Status.BAD_REQUEST).entity(e.getReason()).build();
        }
    }
}