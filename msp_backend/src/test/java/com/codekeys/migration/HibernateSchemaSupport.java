package com.codekeys.migration;

import jakarta.persistence.Entity;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.PhysicalNamingStrategySnakeCaseImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.springframework.boot.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

final class HibernateSchemaSupport {
    private HibernateSchemaSupport() { }

    static Metadata metadata(StandardServiceRegistry registry) throws ClassNotFoundException {
        MetadataSources sources = new MetadataSources(registry);
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        for (var entity : scanner.findCandidateComponents("com.codekeys")) {
            sources.addAnnotatedClass(Class.forName(entity.getBeanClassName()));
        }
        return sources.getMetadataBuilder()
                .applyImplicitNamingStrategy(new SpringImplicitNamingStrategy())
                .applyPhysicalNamingStrategy(new PhysicalNamingStrategySnakeCaseImpl())
                .build();
    }
}
