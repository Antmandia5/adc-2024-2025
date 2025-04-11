package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import org.apache.commons.codec.digest.DigestUtils;

public class AppInit {

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    static {
        initRootUser();
    }

    private static void initRootUser() {
        Key rootKey = datastore.newKeyFactory().setKind("User").newKey("root");

        // Só cria se ainda não existir
        if (datastore.get(rootKey) == null) {
            Entity rootUser = Entity.newBuilder(rootKey)
            		.set("user_username", "root")
                    .set("user_name", "root")
                    .set("user_pwd", DigestUtils.sha512Hex("root"))
                    .set("user_email", "root@admin.pt")
                    .set("user_phone", "+351999999999")
                    .set("user_profile", "privado")
                    .set("user_creation_time", Timestamp.now())
                    .set("user_role", "ADMIN")
                    .set("user_status", "ATIVADA")
                    .build();

            datastore.put(rootUser);
            System.out.println("Utilizador root criado com sucesso.");
        } else {
            System.out.println("Utilizador root já existe.");
        }
    }
}
