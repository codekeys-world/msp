package com.codekeys.configuration.seed;

import com.codekeys.common.Genre;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;
import java.util.UUID;

/** Migration Java enregistree par Spring : aucun secret dans les scripts SQL. */
@Component
public class V30__seed_administrateur extends BaseJavaMigration {
    private final Environment environment;

    public V30__seed_administrateur(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void migrate(Context context) throws Exception {
        try (var statement = context.getConnection().prepareStatement("""
                select exists (
                    select 1 from _utilisateur u join _role r on r.id = u.role_id
                    where r.nom = 'ADMINISTRATEUR'
                )
                """); var result = statement.executeQuery()) {
            result.next();
            if (result.getBoolean(1)) return;
        }

        String telephone = required("MSP_ADMIN_TELEPHONE").trim();
        String password = required("MSP_ADMIN_PASSWORD");
        String genre = Genre.valueOf(environment.getProperty("MSP_ADMIN_GENRE", "HOMME").trim()).name();
        String passwordHash = new BCryptPasswordEncoder().encode(password);

        try (PreparedStatement statement = context.getConnection().prepareStatement("""
                insert into _utilisateur (
                    date_creation, slug, nom, prenom, telephone_principal, email, password,
                    matricule, numero_inscription, genre, pere, mere,
                    account_locked, account_non_expired, enabled, creer_par, role_id
                )
                values (current_timestamp, ?, ?, ?, ?, ?, ?, ?, ?, ?, '', '',
                        false, false, true, 'FLYWAY',
                        (select id from _role where nom = 'ADMINISTRATEUR'))
                """)) {
            statement.setString(1, UUID.randomUUID().toString());
            statement.setString(2, environment.getProperty("MSP_ADMIN_NOM", "ADMINISTRATEUR"));
            statement.setString(3, environment.getProperty("MSP_ADMIN_PRENOM", "MSP"));
            statement.setString(4, telephone);
            statement.setString(5, environment.getProperty("MSP_ADMIN_EMAIL"));
            statement.setString(6, passwordHash);
            statement.setString(7, environment.getProperty("MSP_ADMIN_MATRICULE", "ADMIN"));
            statement.setString(8, "ADMINISTRATEUR");
            statement.setString(9, genre);
            statement.executeUpdate();
        }
    }

    private String required(String name) {
        String value = environment.getProperty(name);
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("Configurer " + name
                    + " pour creer le premier administrateur (migration V30).");
        }
        return value;
    }
}
