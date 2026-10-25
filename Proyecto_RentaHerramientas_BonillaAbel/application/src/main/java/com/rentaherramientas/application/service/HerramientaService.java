package com.rentaherramientas.application.service;

import com.rentaherramientas.application.port.in.HerramientaUseCase;
import com.rentaherramientas.domain.exception.OperacionNoPermitidaException;
import com.rentaherramientas.domain.exception.RecursoNoEncontradoException;
import com.rentaherramientas.domain.exception.ReglaNegocioException;
import com.rentaherramientas.domain.model.Actor;
import com.rentaherramientas.domain.model.Categoria;
import com.rentaherramientas.domain.model.EstadoHerramienta;
import com.rentaherramientas.domain.model.FiltroHerramienta;
import com.rentaherramientas.domain.model.Herramienta;
import com.rentaherramientas.domain.model.Proveedor;
import com.rentaherramientas.domain.port.out.CategoriaRepositoryPort;
import com.rentaherramientas.domain.port.out.FileStoragePort;
import com.rentaherramientas.domain.port.out.HerramientaRepositoryPort;
import com.rentaherramientas.domain.port.out.ProveedorRepositoryPort;
import com.rentaherramientas.domain.port.out.ReservaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class HerramientaService implements HerramientaUseCase {

    private static final Set<String> TIPOS_IMAGEN = Set.of("image/jpeg", "image/png", "image/webp");
    private static final int MAX_BYTES_IMAGEN = 5 * 1024 * 1024;

    private final HerramientaRepositoryPort herramientas;
    private final ProveedorRepositoryPort proveedores;
    private final CategoriaRepositoryPort categorias;
    private final ReservaRepositoryPort reservas;
    private final FileStoragePort almacenamiento;

    @Override
    public Herramienta crear(HerramientaComando c, Actor actor) {
        if (actor.esCliente()) {
            throw new OperacionNoPermitidaException("Los clientes no pueden registrar herramientas");
        }
        validar(c);
        Long proveedorId = actor.esAdmin() ? c.proveedorId() : actor.usuarioId();
        if (proveedorId == null) {
            throw new ReglaNegocioException("Debes indicar el proveedor dueño de la herramienta");
        }
        Proveedor proveedor = proveedores.buscarPorId(proveedorId)
                .orElseThrow(() -> RecursoNoEncontradoException.de("Proveedor", proveedorId));
        Categoria categoria = categorias.buscarPorId(c.categoriaId())
                .orElseThrow(() -> RecursoNoEncontradoException.de("Categoría", c.categoriaId()));

        Herramienta herramienta = Herramienta.builder()
                .proveedorId(proveedor.getId())
                .proveedorNombre(proveedor.getEmpresa())
                .categoriaId(categoria.getId())
                .categoriaNombre(categoria.getNombre())
                .nombre(c.nombre().trim())
                .descripcion(c.descripcion())
                .marca(c.marca())
                .precioDia(c.precioDia())
                .estado(c.estado() == null ? EstadoHerramienta.DISPONIBLE : c.estado())
                .reservaAutomatica(c.reservaAutomatica())
                .build();
        return herramientas.guardar(herramienta);
    }

    @Override
    public Herramienta actualizar(Long id, HerramientaComando c, Actor actor) {
        validar(c);
        Herramienta herramienta = cargar(id);
        exigirPropietario(herramienta, actor);
        if (!herramienta.getCategoriaId().equals(c.categoriaId())) {
            Categoria categoria = categorias.buscarPorId(c.categoriaId())
                    .orElseThrow(() -> RecursoNoEncontradoException.de("Categoría", c.categoriaId()));
            herramienta.setCategoriaId(categoria.getId());
            herramienta.setCategoriaNombre(categoria.getNombre());
        }
        herramienta.setNombre(c.nombre().trim());
        herramienta.setDescripcion(c.descripcion());
        herramienta.setMarca(c.marca());
        herramienta.setPrecioDia(c.precioDia());
        herramienta.setReservaAutomatica(c.reservaAutomatica());
        if (c.estado() != null) {
            herramienta.setEstado(c.estado());
        }
        return herramientas.guardar(herramienta);
    }

    @Override
    public void eliminar(Long id, Actor actor) {
        Herramienta herramienta = cargar(id);
        exigirPropietario(herramienta, actor);
        if (reservas.herramientaTieneReservasActivas(id)) {
            throw new ReglaNegocioException("La herramienta tiene reservas activas y no se puede eliminar");
        }
        herramienta.setEstado(EstadoHerramienta.INACTIVA); // eliminación lógica
        herramientas.guardar(herramienta);
    }

    @Override
    @Transactional(readOnly = true)
    public Herramienta obtener(Long id) {
        return cargar(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Herramienta> buscar(FiltroHerramienta filtro) {
        return herramientas.buscar(filtro);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Herramienta> listarGestionables(Actor actor) {
        if (actor.esAdmin()) {
            return herramientas.buscar(new FiltroHerramienta(null, null, null, null, null, false));
        }
        if (actor.esProveedor()) {
            return herramientas.listarPorProveedor(actor.usuarioId());
        }
        throw new OperacionNoPermitidaException("Los clientes no gestionan inventario");
    }

    @Override
    public Herramienta subirImagen(Long id, String contentType, byte[] contenido, Actor actor) {
        Herramienta herramienta = cargar(id);
        exigirPropietario(herramienta, actor);
        if (contenido == null || contenido.length == 0) {
            throw new ReglaNegocioException("El archivo está vacío");
        }
        if (contenido.length > MAX_BYTES_IMAGEN) {
            throw new ReglaNegocioException("La imagen supera el máximo de 5 MB");
        }
        if (contentType == null || !TIPOS_IMAGEN.contains(contentType.toLowerCase())) {
            throw new ReglaNegocioException("Formato no permitido. Usa JPG, PNG o WEBP");
        }
        String anterior = herramienta.getImagenUrl();
        herramienta.setImagenUrl(almacenamiento.guardarImagen(contentType.toLowerCase(), contenido));
        Herramienta guardada = herramientas.guardar(herramienta);
        if (anterior != null) {
            almacenamiento.eliminar(anterior);
        }
        return guardada;
    }

    // ----------------------------------------------------------------- helpers

    private Herramienta cargar(Long id) {
        Herramienta h = herramientas.buscarPorId(id).orElseThrow(() -> RecursoNoEncontradoException.de("Herramienta", id));
        if (h.getEstado() == EstadoHerramienta.INACTIVA) {
            throw RecursoNoEncontradoException.de("Herramienta", id);
        }
        return h;
    }

    private void exigirPropietario(Herramienta h, Actor actor) {
        if (actor.esCliente() || !actor.puedeGestionar(h.getProveedorId())) {
            throw new OperacionNoPermitidaException("Solo el proveedor dueño o un administrador pueden modificar esta herramienta");
        }
    }

    private void validar(HerramientaComando c) {
        if (c.precioDia() == null || c.precioDia().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El precio por día debe ser mayor a cero");
        }
        if (c.estado() == EstadoHerramienta.INACTIVA) {
            throw new ReglaNegocioException("Para retirar una herramienta usa la opción eliminar");
        }
    }
}
