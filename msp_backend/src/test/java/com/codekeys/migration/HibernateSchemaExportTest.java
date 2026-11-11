package com.codekeys.migration;

import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

class HibernateSchemaExportTest {
    @Test
    @EnabledIfSystemProperty(named = "msp.export-schema", matches = "true")
    void exportCurrentMappingsForMigrationReview() throws Exception {
        Path output = Path.of("target", "hibernate-schema.sql").toAbsolutePath();
        Files.deleteIfExists(output);
        Map<String, Object> settings = new HashMap<>();
        settings.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        settings.put("hibernate.boot.allow_jdbc_metadata_access", false);
        settings.put("jakarta.persistence.schema-generation.database.action", "none");
        settings.put("jakarta.persistence.schema-generation.scripts.action", "create");
        settings.put("jakarta.persistence.schema-generation.scripts.create-target", output.toString());
        settings.put("hibernate.hbm2ddl.delimiter", ";");
        settings.put("hibernate.format_sql", true);
        var registry = new StandardServiceRegistryBuilder().applySettings(settings).build();
        try {
            SchemaManagementToolCoordinator.process(HibernateSchemaSupport.metadata(registry),
                    registry, settings, null);
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
