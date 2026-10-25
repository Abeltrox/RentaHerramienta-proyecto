package com.rentaherramientas.domain.value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Objeto de valor inmutable para montos en pesos colombianos (COP) con 2 decimales. */
public final class Dinero {

    public static final String MONEDA_COP = "COP";
    public static final int ESCALA = 2;

    private final BigDecimal monto;

    private Dinero(BigDecimal monto) {
        this.monto = Objects.requireNonNull(monto, "El monto es obligatorio").setScale(ESCALA, RoundingMode.HALF_UP);
    }

    public static Dinero cop(BigDecimal monto) {
        return new Dinero(monto);
    }

    public static Dinero cero() {
        return new Dinero(BigDecimal.ZERO);
    }

    public Dinero sumar(Dinero otro) {
        return new Dinero(monto.add(otro.monto));
    }

    public Dinero restar(Dinero otro) {
        return new Dinero(monto.subtract(otro.monto));
    }

    public Dinero multiplicar(BigDecimal factor) {
        return new Dinero(monto.multiply(factor));
    }

    public Dinero multiplicar(int factor) {
        return new Dinero(monto.multiply(BigDecimal.valueOf(factor)));
    }

    public boolean esMayorQue(Dinero otro) {
        return monto.compareTo(otro.monto) > 0;
    }

    public boolean esMenorQue(Dinero otro) {
        return monto.compareTo(otro.monto) < 0;
    }

    public boolean esCero() {
        return monto.signum() == 0;
    }

    public boolean esPositivo() {
        return monto.signum() > 0;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getMoneda() {
        return MONEDA_COP;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Dinero otro && monto.equals(otro.monto));
    }

    @Override
    public int hashCode() {
        return monto.hashCode();
    }

    @Override
    public String toString() {
        return monto.toPlainString() + " " + MONEDA_COP;
    }
}
