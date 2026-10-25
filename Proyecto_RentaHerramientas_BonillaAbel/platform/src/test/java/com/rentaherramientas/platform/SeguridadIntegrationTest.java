package com.rentaherramientas.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integración contra una base PostgreSQL REAL (Flyway crea el esquema y los datos de prueba).
 * Solo se ejecuta si defines las variables de entorno de la base, por ejemplo:
 *
 *   DB_URL=jdbc:postgresql://localhost:5432/renta_test DB_USER=postgres DB_PASSWORD=postgres mvn test
 *
 * Usa una base VACÍA (Flyway migra al arrancar).
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class SeguridadIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private String login(String email, String password) throws Exception {
        String cuerpo = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(cuerpo).get("token").asText();
    }

    @Test
    @DisplayName("Login correcto devuelve JWT y el rol")
    void loginOk() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@rentaherramientas.com\",\"password\":\"Admin123*\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.rol").value("ADMIN"));
    }

    @Test
    @DisplayName("Credenciales incorrectas → 401")
    void loginMalo() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@rentaherramientas.com\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Rutas protegidas sin token → 401")
    void sinToken() throws Exception {
        mvc.perform(get("/api/reservas")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Un CLIENTE no puede entrar a rutas de ADMIN → 403")
    void clienteNoEsAdmin() throws Exception {
        String token = login("cliente01@rentaherramientas.com", "Cliente123*");
        mvc.perform(get("/api/clientes").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
        mvc.perform(get("/api/reportes/resumen").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("El ADMIN consulta el resumen de la plataforma")
    void adminVeResumen() throws Exception {
        String token = login("admin@rentaherramientas.com", "Admin123*");
        mvc.perform(get("/api/reportes/resumen").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHerramientas").value(greaterThanOrEqualTo(20)));
    }

    @Test
    @DisplayName("El catálogo es público")
    void catalogoPublico() throws Exception {
        mvc.perform(get("/api/herramientas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(20)));
    }

    @Test
    @DisplayName("Un token falso → 401")
    void tokenFalso() throws Exception {
        mvc.perform(get("/api/reservas").header("Authorization", "Bearer abc.def.ghi")).andExpect(status().isUnauthorized());
    }
}
