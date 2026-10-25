package com.rentaherramientas.api.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentación Swagger: http://localhost:8080/swagger-ui.html (botón "Authorize" para pegar el JWT). */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA = "bearerAuth";

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API - Plataforma de Renta de Herramientas y Equipos de Construcción")
                        .version("1.0.0")
                        .description("""
                                API REST para gestionar herramientas, reservas, pagos, facturación y reportes.

                                **Autenticación:** haz POST a `/api/auth/login`, copia el campo `token` de la respuesta, \
                                pulsa **Authorize** y pega el token (sin la palabra Bearer). \
                                Roles: `ADMIN`, `PROVEEDOR`, `CLIENTE`.""")
                        .contact(new Contact().name("Óscar Abel Bonilla García")))
                .components(new Components().addSecuritySchemes(ESQUEMA, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA));
    }
}
