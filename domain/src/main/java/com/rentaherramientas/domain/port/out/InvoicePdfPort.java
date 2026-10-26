package com.rentaherramientas.domain.port.out;

import com.rentaherramientas.domain.model.Factura;

/** Puerto de salida: renderizado de la factura en PDF. */
public interface InvoicePdfPort {

    byte[] generar(Factura factura);
}
