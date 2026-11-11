package com.codekeys.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Mama Bino DAOU",
                        email = "codekeys.dev@gmail.com",
                        url = "https://codekeys.dev/courses"
                ),
                description = "Cette application permet de gérer la mutuelle des surveillants de prison du Mali",
                title = "Mutuelle des surveillants de prison du Mali - Mama Bino DAOU",
                version = "1.0",
                license = @License(
                        name = "Licence de codekeys",
                        url = "https://codekeys.dev/courses"
                ),
                termsOfService = "PV, Ordre de mission, Dépense, Adhérent"
        ),
        servers = {
                @Server(
                        description = "Local ENV",
                        url = "http://localhost:8088/api/v1"
                ),
                @Server(
                        description = "PROD ENV",
                        url = "https://codekeys.dev/courses"
                )
        },
        security = {
                @SecurityRequirement(
                        name = "bearerAuth"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Authentification avec jwt en fournissant le login et le mot de passe",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfiguration {
}
