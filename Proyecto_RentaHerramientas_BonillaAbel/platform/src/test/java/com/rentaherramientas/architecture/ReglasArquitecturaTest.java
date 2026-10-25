package com.rentaherramientas.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reglas de la arquitectura hexagonal (las dependencias siempre apuntan hacia adentro):
 *
 * <pre>
 *   platform ─▶ api ─────────┐
 *      │                      ▼
 *      └────▶ infrastructure ─▶ application ─▶ domain
 * </pre>
 *
 * Se usa el API core de ArchUnit ({@code regla.check(...)}), que funciona con cualquier versión de JUnit 5.
 */
class ReglasArquitecturaTest {

    private static final JavaClasses CLASES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.rentaherramientas");

    @Test
    @DisplayName("Se importan las clases reales de todos los módulos")
    void importaLasClasesRealesDelProyecto() {
        assertThat(CLASES)
                .extracting(JavaClass::getSimpleName)
                .contains("RentaHerramientasApplication", "Reserva", "ReservaService",
                        "ReservaPersistenceAdapter", "ReservaController", "SecurityConfig");
    }

    @Test
    @DisplayName("domain no depende de Spring, JPA, HTTP ni de otros módulos")
    void domainSoloDependeDeJava() {
        classes().that().resideInAPackage("com.rentaherramientas.domain..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage("com.rentaherramientas.domain..", "java..", "javax..", "jdk..")
                .check(CLASES);
    }

    @Test
    @DisplayName("application solo depende de domain, Java y las anotaciones @Service/@Transactional de Spring")
    void applicationSoloDependeDeDomain() {
        classes().that().resideInAPackage("com.rentaherramientas.application..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage("com.rentaherramientas.application..", "com.rentaherramientas.domain..", "org.springframework.stereotype..",
                        "org.springframework.transaction..", "java..", "javax..", "jdk..")
                .check(CLASES);
    }

    @Test
    @DisplayName("infrastructure no depende de api ni de platform")
    void infrastructureNoDependeDeCapasExternas() {
        classes().that().resideInAPackage("com.rentaherramientas.infrastructure..")
                .should().onlyDependOnClassesThat()
                .resideOutsideOfPackages("com.rentaherramientas.api..", "com.rentaherramientas.platform..")
                .check(CLASES);
    }

    @Test
    @DisplayName("api no depende de infrastructure ni de platform")
    void apiNoDependeDeInfraestructura() {
        classes().that().resideInAPackage("com.rentaherramientas.api..")
                .should().onlyDependOnClassesThat()
                .resideOutsideOfPackages("com.rentaherramientas.infrastructure..", "com.rentaherramientas.platform..")
                .check(CLASES);
    }

    @Test
    @DisplayName("Los controladores hablan con casos de uso, nunca con los puertos de salida")
    void controladoresNoUsanPuertosDeSalida() {
        classes().that().resideInAPackage("com.rentaherramientas.api.controller..")
                .should().onlyDependOnClassesThat()
                .resideOutsideOfPackage("com.rentaherramientas.domain.port.out..")
                .check(CLASES);
    }

    @Test
    @DisplayName("Las entidades JPA solo existen en infrastructure.persistence.entity")
    void entidadesJpaSoloEnInfraestructura() {
        classes().that().areAnnotatedWith(Entity.class)
                .should().resideInAPackage("com.rentaherramientas.infrastructure.persistence.entity..")
                .check(CLASES);
    }

    @Test
    @DisplayName("No hay dependencias cíclicas entre los módulos")
    void noHayCiclosEntreModulos() {
        slices().matching("com.rentaherramientas.(*)..")
                .should().beFreeOfCycles()
                .check(CLASES);
    }
}
