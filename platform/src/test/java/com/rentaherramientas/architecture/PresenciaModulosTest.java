package com.rentaherramientas.architecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica que los cinco módulos Maven existen y que Flyway apunta a la ubicación acordada.
 * El directorio de trabajo de Surefire es la raíz del módulo {@code platform}; de ahí el {@code ..}.
 */
class PresenciaModulosTest {

    @Test
    @DisplayName("Existen los cinco módulos Maven con su pom.xml")
    void existenLosCincoModulos() {
        for (String modulo : new String[] {"domain", "application", "infrastructure", "api", "platform"}) {
            assertThat(Path.of("..", modulo, "pom.xml")).as("pom.xml del módulo %s", modulo).exists();
        }
    }

    @Test
    @DisplayName("Flyway apunta a classpath:db/migration y los scripts viven en infrastructure")
    void flywayYMigraciones() throws Exception {
        Path props = Path.of("src", "main", "resources", "application.properties");
        assertThat(props).exists();
        assertThat(Files.readString(props)).contains("classpath:db/migration");
        assertThat(Path.of("..", "infrastructure", "src", "main", "resources", "db", "migration", "V1__crear_esquema.sql")).exists();
    }
}
