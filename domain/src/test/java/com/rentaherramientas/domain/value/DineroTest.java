package com.rentaherramientas.domain.value;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DineroTest {

    @Test
    @DisplayName("Redondea a 2 decimales (HALF_UP)")
    void redondea() {
        assertThat(Dinero.cop(new BigDecimal("100.005")).getMonto()).isEqualByComparingTo("100.01");
        assertThat(Dinero.cop(new BigDecimal("100")).getMonto().scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("Suma, resta y multiplica sin perder precisión")
    void operaciones() {
        Dinero a = Dinero.cop(new BigDecimal("100000"));
        Dinero b = Dinero.cop(new BigDecimal("19000"));

        assertThat(a.sumar(b).getMonto()).isEqualByComparingTo("119000");
        assertThat(a.restar(b).getMonto()).isEqualByComparingTo("81000");
        assertThat(a.multiplicar(3).getMonto()).isEqualByComparingTo("300000");
        assertThat(a.multiplicar(new BigDecimal("0.19")).getMonto()).isEqualByComparingTo("19000");
    }

    @Test
    @DisplayName("Compara montos y detecta cero / positivo")
    void comparaciones() {
        Dinero cinco = Dinero.cop(new BigDecimal("5"));
        Dinero diez = Dinero.cop(new BigDecimal("10"));

        assertThat(diez.esMayorQue(cinco)).isTrue();
        assertThat(cinco.esMenorQue(diez)).isTrue();
        assertThat(Dinero.cero().esCero()).isTrue();
        assertThat(cinco.esPositivo()).isTrue();
    }

    @Test
    @DisplayName("Dos montos con el mismo valor son iguales aunque tengan distinta escala")
    void igualdad() {
        assertThat(Dinero.cop(new BigDecimal("10"))).isEqualTo(Dinero.cop(new BigDecimal("10.00")));
        assertThat(Dinero.cop(new BigDecimal("10")).hashCode()).isEqualTo(Dinero.cop(new BigDecimal("10.00")).hashCode());
    }

    @Test
    @DisplayName("No acepta un monto nulo")
    void montoNulo() {
        assertThatThrownBy(() -> Dinero.cop(null)).isInstanceOf(NullPointerException.class);
    }
}
