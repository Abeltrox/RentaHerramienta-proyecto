package com.rentaherramientas.domain.value;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NitTest {

    @Test
    @DisplayName("Acepta NIT con dígito de verificación correcto")
    void aceptaValido() {
        assertThat(Nit.of("800197268-4").getValor()).isEqualTo("800197268-4");
        assertThat(Nit.esValido("900123456-8")).isTrue();
    }

    @Test
    @DisplayName("Rechaza un dígito de verificación incorrecto")
    void rechazaDvIncorrecto() {
        assertThatThrownBy(() -> Nit.of("800197268-5")).isInstanceOf(IllegalArgumentException.class);
        assertThat(Nit.esValido("900123456-7")).isFalse();
    }

    @Test
    @DisplayName("Rechaza formatos inválidos")
    void rechazaFormato() {
        assertThatThrownBy(() -> Nit.of(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Nit.of("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Nit.of("800197268")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Nit.of("ABC-1")).isInstanceOf(IllegalArgumentException.class);
    }
}
