package com.codekeys.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.TestConfiguration;
import com.codekeys.configuration.seed.V30__seed_administrateur;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Execute uniquement avec une base PostgreSQL de test explicitement fournie. */
@EnabledIfEnvironmentVariable(named = "MSP_MIGRATION_TEST_URL", matches = ".+")
class FlywayMigrationTest {
    private final String url = System.getenv("MSP_MIGRATION_TEST_URL");
    private final String user = System.getenv().getOrDefault("MSP_MIGRATION_TEST_USER", "postgres");
    private final String password = System.getenv().getOrDefault("MSP_MIGRATION_TEST_PASSWORD", "");

    @Test
    void springBootMigratesBeforeHibernateValidates() throws Exception {
        String schema = createTestSchema();
        try {
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(DataSourceAutoConfiguration.class,
                            FlywayAutoConfiguration.class, HibernateJpaAutoConfiguration.class))
                    .withUserConfiguration(EntityMappingConfiguration.class, V30__seed_administrateur.class)
                    .withPropertyValues("spring.datasource.url=" + url,
                            "spring.datasource.username=" + user, "spring.datasource.password=" + password,
                            "spring.flyway.default-schema=" + schema, "spring.flyway.schemas=" + schema,
                            "spring.jpa.properties.hibernate.default_schema=" + schema,
                            "spring.jpa.hibernate.ddl-auto=validate",
                            "MSP_ADMIN_TELEPHONE=70000001", "MSP_ADMIN_PASSWORD=test-admin-password")
                    .run(context -> {
                        assertNull(context.getStartupFailure());
                        assertTrue(context.getBean(EntityManagerFactory.class).isOpen());
                        assertEquals("30", context.getBean(Flyway.class).info().current().getVersion().toString());
                    });
        } finally {
            dropTestSchema(schema);
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EntityScan("com.codekeys")
    static class EntityMappingConfiguration { }

    @Test
    void emptyDatabaseMigratesAndMatchesEveryEntity() throws Exception {
        String schema = createTestSchema();
        try {
            Flyway flyway = flyway(schema);
            assertEquals(30, flyway.migrate().migrationsExecuted);
            flyway.validate();
            assertEquals(0, flyway.migrate().migrationsExecuted);
            validateHibernate(schema);
            assertEquals(26, scalar(schema, "select count(*) from information_schema.tables "
                    + "where table_schema = current_schema() and table_name like '\\_%' escape '\\'"));
            assertEquals(46, scalar(schema, "select count(*) from information_schema.table_constraints "
                    + "where constraint_schema = current_schema() and constraint_type = 'FOREIGN KEY'"));
            assertEquals(6, scalar(schema, "select count(*) from pg_indexes "
                    + "where schemaname = current_schema() and indexname like 'idx_%'"));
            assertEquals(9, scalar(schema, "select count(*) from _role"));
            assertEquals(91, scalar(schema, "select count(*) from _authorisation"));
            assertEquals(1, scalar(schema, "select count(*) from _utilisateur"));
            assertEquals(91, scalar(schema, "select count(*) from _role_authorisations ra "
                    + "join _role r on r.id = ra.roles_id where r.nom = 'ADMINISTRATEUR'"));
            assertEquals(0, scalar(schema, "select count(*) from _role_authorisations ra "
                    + "join _role r on r.id = ra.roles_id join _authorisation a on a.id = ra.authorisations_id "
                    + "where r.nom = 'PRESIDENT' and a.nom in ('ENGAGER_DEPENSE', 'SUPPRIMER_DEPENSE')"));
            for (String table : new String[]{"_courrier", "_plainte", "_convocation", "_pv",
                    "_journal_action", "_encaissement", "_depense"}) {
                assertEquals(0, scalar(schema, "select count(*) from " + table));
            }
            try (Connection connection = connection(schema); var statement = connection.createStatement();
                 var result = statement.executeQuery("select password, telephone_principal, enabled, "
                         + "account_locked, account_non_expired from _utilisateur")) {
                assertTrue(result.next());
                assertTrue(new BCryptPasswordEncoder().matches("test-admin-password", result.getString(1)));
                assertEquals("70000001", result.getString(2));
                assertTrue(result.getBoolean(3));
                assertFalse(result.getBoolean(4));
                assertFalse(result.getBoolean(5));
            }
        } finally {
            dropTestSchema(schema);
        }
    }

    @Test
    void existingHibernateSchemaRequiresExplicitBaselineAndPreservesData() throws Exception {
        String schema = createTestSchema();
        try {
            // Reproduit les tables et les noms de contraintes produits par Hibernate avant Flyway.
            StringWriter ddl = new StringWriter();
            Map<String, Object> settings = settings(schema);
            settings.put("jakarta.persistence.schema-generation.database.action", "none");
            settings.put("jakarta.persistence.schema-generation.scripts.action", "create");
            settings.put("jakarta.persistence.schema-generation.scripts.create-target", ddl);
            settings.put("hibernate.hbm2ddl.delimiter", ";");
            var registry = new StandardServiceRegistryBuilder().applySettings(settings).build();
            try {
                SchemaManagementToolCoordinator.process(HibernateSchemaSupport.metadata(registry),
                        registry, settings, null);
            } finally {
                StandardServiceRegistryBuilder.destroy(registry);
            }
            try (Connection connection = connection(schema); var statement = connection.createStatement()) {
                for (String sql : ddl.toString().split(";")) {
                    if (!sql.isBlank()) statement.execute(sql);
                }
                statement.execute("insert into _authorisation (date_creation, slug, nom) "
                        + "values (current_timestamp, 'preserved-slug', 'PRESERVED_PERMISSION')");
            }
            assertThrows(FlywayException.class, () -> flyway(schema).migrate());
            Flyway baseline = Flyway.configure().dataSource(url, user, password)
                    .schemas(schema).defaultSchema(schema).createSchemas(false)
                    .baselineVersion("27").javaMigrations(seedMigration()).load();
            baseline.baseline();
            assertEquals(3, baseline.migrate().migrationsExecuted);
            baseline.validate();
            validateHibernate(schema);
            assertEquals(1, scalar(schema, "select count(*) from _authorisation "
                    + "where slug = 'preserved-slug' and nom = 'PRESERVED_PERMISSION'"));
            assertEquals(2, scalar(schema, "select count(*) from information_schema.table_constraints "
                    + "where constraint_schema = current_schema() and table_name = '_cas_social' "
                    + "and constraint_name in ('ck_cas_social_type_cas', 'ck_cas_social_lien_defunt')"));
            assertEquals(0, baseline.migrate().migrationsExecuted);
        } finally {
            dropTestSchema(schema);
        }
    }

    private Flyway flyway(String schema) {
        return Flyway.configure().dataSource(url, user, password)
                .schemas(schema).defaultSchema(schema).createSchemas(false)
                .locations("classpath:db/migration").baselineOnMigrate(false).cleanDisabled(true)
                .javaMigrations(seedMigration()).load();
    }

    private V30__seed_administrateur seedMigration() {
        return new V30__seed_administrateur(new MockEnvironment()
                .withProperty("MSP_ADMIN_TELEPHONE", "70000001")
                .withProperty("MSP_ADMIN_PASSWORD", "test-admin-password"));
    }

    @Test
    void missingAdminCredentialsFailAndCanBeProvidedOnRetry() throws Exception {
        String schema = createTestSchema();
        try {
            Flyway unconfigured = Flyway.configure().dataSource(url, user, password)
                    .schemas(schema).defaultSchema(schema).createSchemas(false)
                    .javaMigrations(new V30__seed_administrateur(new MockEnvironment())).load();
            assertThrows(FlywayException.class, unconfigured::migrate);
            assertEquals(0, scalar(schema, "select count(*) from _utilisateur"));
            assertEquals(1, flyway(schema).migrate().migrationsExecuted);
            assertEquals(1, scalar(schema, "select count(*) from _utilisateur"));
        } finally {
            dropTestSchema(schema);
        }
    }

    @Test
    void existingAdministratorIsPreservedWithoutBootstrapCredentials() throws Exception {
        String schema = createTestSchema();
        try {
            Flyway beforeAdmin = Flyway.configure().dataSource(url, user, password)
                    .schemas(schema).defaultSchema(schema).createSchemas(false).target("29").load();
            beforeAdmin.migrate();
            try (Connection connection = connection(schema); var statement = connection.createStatement()) {
                statement.execute("""
                        insert into _utilisateur (date_creation, slug, nom, prenom, telephone_principal,
                            password, numero_inscription, genre, pere, mere, account_locked,
                            account_non_expired, enabled, role_id)
                        values (current_timestamp, 'existing-admin', 'Admin', 'Existant', '79999999',
                            'existing-password-hash', 'EXISTING-ADMIN', 'HOMME', '', '', false, false, true,
                            (select id from _role where nom = 'ADMINISTRATEUR'))
                        """);
            }
            Flyway unconfigured = Flyway.configure().dataSource(url, user, password)
                    .schemas(schema).defaultSchema(schema).createSchemas(false)
                    .javaMigrations(new V30__seed_administrateur(new MockEnvironment())).load();
            assertEquals(1, unconfigured.migrate().migrationsExecuted);
            assertEquals(1, scalar(schema, "select count(*) from _utilisateur "
                    + "where slug = 'existing-admin' and password = 'existing-password-hash'"));
            assertEquals(1, scalar(schema, "select count(*) from _utilisateur"));
        } finally {
            dropTestSchema(schema);
        }
    }

    private Map<String, Object> settings(String schema) {
        Map<String, Object> settings = new HashMap<>();
        settings.put("hibernate.connection.url", url);
        settings.put("hibernate.connection.username", user);
        settings.put("hibernate.connection.password", password);
        settings.put("hibernate.default_schema", schema);
        settings.put("hibernate.hbm2ddl.halt_on_error", true);
        return settings;
    }

    private void validateHibernate(String schema) throws Exception {
        var settings = settings(schema);
        settings.put("hibernate.hbm2ddl.auto", "validate");
        var registry = new StandardServiceRegistryBuilder().applySettings(settings).build();
        try {
            SchemaManagementToolCoordinator.process(HibernateSchemaSupport.metadata(registry),
                    registry, settings, null);
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    private Connection connection(String schema) throws Exception {
        Connection connection = DriverManager.getConnection(url, user, password);
        connection.setSchema(schema);
        return connection;
    }

    private String createTestSchema() throws Exception {
        String schema = "msp_flyway_test_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = DriverManager.getConnection(url, user, password);
             var statement = connection.createStatement()) {
            statement.execute("create schema " + schema);
        }
        return schema;
    }

    private void dropTestSchema(String schema) throws Exception {
        if (!schema.matches("msp_flyway_test_[a-f0-9]{32}")) {
            throw new IllegalArgumentException("Not a generated test schema");
        }
        try (Connection connection = DriverManager.getConnection(url, user, password);
             var statement = connection.createStatement()) {
            statement.execute("drop schema " + schema + " cascade");
        }
    }

    private long scalar(String schema, String sql) throws Exception {
        try (Connection connection = connection(schema); var statement = connection.createStatement();
             var result = statement.executeQuery(sql)) {
            assertTrue(result.next());
            return result.getLong(1);
        }
    }
}
