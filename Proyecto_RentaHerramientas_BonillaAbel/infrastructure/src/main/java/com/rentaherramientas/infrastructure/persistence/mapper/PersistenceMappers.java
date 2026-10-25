package com.rentaherramientas.infrastructure.persistence.mapper;

import com.rentaherramientas.domain.model.Categoria;
import com.rentaherramientas.domain.model.Cliente;
import com.rentaherramientas.domain.model.Factura;
import com.rentaherramientas.domain.model.Herramienta;
import com.rentaherramientas.domain.model.Pago;
import com.rentaherramientas.domain.model.Proveedor;
import com.rentaherramientas.domain.model.ReporteDanio;
import com.rentaherramientas.domain.model.Reserva;
import com.rentaherramientas.domain.model.Usuario;
import com.rentaherramientas.infrastructure.persistence.entity.CategoriaEntity;
import com.rentaherramientas.infrastructure.persistence.entity.ClienteEntity;
import com.rentaherramientas.infrastructure.persistence.entity.DanioEntity;
import com.rentaherramientas.infrastructure.persistence.entity.FacturaEntity;
import com.rentaherramientas.infrastructure.persistence.entity.HerramientaEntity;
import com.rentaherramientas.infrastructure.persistence.entity.PagoEntity;
import com.rentaherramientas.infrastructure.persistence.entity.ProveedorEntity;
import com.rentaherramientas.infrastructure.persistence.entity.ReservaEntity;
import com.rentaherramientas.infrastructure.persistence.entity.UsuarioEntity;

/**
 * Traduce entidades JPA a modelos de dominio. Debe usarse dentro de una transacción
 * porque recorre asociaciones perezosas (LAZY) para llenar nombres de apoyo.
 */
public final class PersistenceMappers {

    private PersistenceMappers() {
    }

    public static Usuario usuario(UsuarioEntity e) {
        return Usuario.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .email(e.getEmail())
                .password(e.getPassword())
                .telefono(e.getTelefono())
                .rol(e.getRol())
                .activo(e.isActivo())
                .fechaCreacion(e.getFechaCreacion())
                .build();
    }

    public static Proveedor proveedor(ProveedorEntity e) {
        return Proveedor.builder()
                .id(e.getId())
                .usuario(usuario(e.getUsuario()))
                .empresa(e.getEmpresa())
                .nit(e.getNit())
                .direccion(e.getDireccion())
                .ciudad(e.getCiudad())
                .build();
    }

    public static Cliente cliente(ClienteEntity e) {
        return Cliente.builder()
                .id(e.getId())
                .usuario(usuario(e.getUsuario()))
                .documento(e.getDocumento())
                .direccion(e.getDireccion())
                .ciudad(e.getCiudad())
                .build();
    }

    public static Categoria categoria(CategoriaEntity e) {
        return Categoria.builder().id(e.getId()).nombre(e.getNombre()).descripcion(e.getDescripcion()).build();
    }

    public static Herramienta herramienta(HerramientaEntity e) {
        return Herramienta.builder()
                .id(e.getId())
                .proveedorId(e.getProveedor().getId())
                .proveedorNombre(e.getProveedor().getEmpresa())
                .categoriaId(e.getCategoria().getId())
                .categoriaNombre(e.getCategoria().getNombre())
                .nombre(e.getNombre())
                .descripcion(e.getDescripcion())
                .marca(e.getMarca())
                .precioDia(e.getPrecioDia())
                .estado(e.getEstado())
                .imagenUrl(e.getImagenUrl())
                .reservaAutomatica(e.isReservaAutomatica())
                .fechaCreacion(e.getFechaCreacion())
                .build();
    }

    public static Reserva reserva(ReservaEntity e) {
        HerramientaEntity h = e.getHerramienta();
        return Reserva.builder()
                .id(e.getId())
                .clienteId(e.getCliente().getId())
                .clienteNombre(e.getCliente().getUsuario().getNombre())
                .herramientaId(h.getId())
                .herramientaNombre(h.getNombre())
                .proveedorId(h.getProveedor().getId())
                .proveedorNombre(h.getProveedor().getEmpresa())
                .fechaInicio(e.getFechaInicio())
                .fechaFin(e.getFechaFin())
                .dias(e.getDias())
                .precioDia(e.getPrecioDia())
                .total(e.getTotal())
                .estado(e.getEstado())
                .observaciones(e.getObservaciones())
                .motivoRechazo(e.getMotivoRechazo())
                .fechaEntrega(e.getFechaEntrega())
                .fechaDevolucion(e.getFechaDevolucion())
                .fechaCreacion(e.getFechaCreacion())
                .build();
    }

    public static Pago pago(PagoEntity e) {
        return Pago.builder()
                .id(e.getId())
                .reservaId(e.getReserva().getId())
                .monto(e.getMonto())
                .metodo(e.getMetodo())
                .estado(e.getEstado())
                .referencia(e.getReferencia())
                .fechaPago(e.getFechaPago())
                .build();
    }

    public static Factura factura(FacturaEntity e) {
        return Factura.builder()
                .id(e.getId())
                .numero(e.getNumero())
                .reserva(reserva(e.getReserva()))
                .pago(pago(e.getPago()))
                .subtotal(e.getSubtotal())
                .iva(e.getIva())
                .total(e.getTotal())
                .fechaEmision(e.getFechaEmision())
                .build();
    }

    public static ReporteDanio danio(DanioEntity e) {
        return ReporteDanio.builder()
                .id(e.getId())
                .reservaId(e.getReserva().getId())
                .herramientaId(e.getHerramienta().getId())
                .herramientaNombre(e.getHerramienta().getNombre())
                .proveedorId(e.getHerramienta().getProveedor().getId())
                .descripcion(e.getDescripcion())
                .costoEstimado(e.getCostoEstimado())
                .estado(e.getEstado())
                .fechaReporte(e.getFechaReporte())
                .fechaResolucion(e.getFechaResolucion())
                .build();
    }
}
