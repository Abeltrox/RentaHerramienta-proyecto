// Administración de proveedores y clientes (CRUD). Solo ADMIN.
import { api } from "../core/api.js";
import { acciones, btn, campo, confirmar, html, leerForm, modal, mostrarError, mount, tabla, toast, $ } from "../core/ui.js";

const CONFIG = {
  proveedores: {
    singular: "proveedor",
    ruta: "/api/proveedores",
    columnas: [
      { titulo: "Empresa", celda: (p) => html`<strong>${p.empresa}</strong><br><small class="muted">NIT ${p.nit}</small>` },
      { titulo: "Contacto", celda: (p) => html`${p.nombre}<br><small class="muted">${p.email}</small>` },
      { titulo: "Teléfono", celda: (p) => p.telefono ?? "—" },
      { titulo: "Ciudad", celda: (p) => p.ciudad ?? "—" },
    ],
    campos: (p) => [
      campo({ name: "nombre", label: "Nombre del contacto", value: p?.nombre, required: true, attrs: 'maxlength="120"' }),
      campo({ name: "email", label: "Correo", type: "email", value: p?.email, required: true }),
      campo({ name: "empresa", label: "Empresa", value: p?.empresa, required: true, attrs: 'maxlength="150"' }),
      campo({ name: "nit", label: "NIT", value: p?.nit, required: true, attrs: 'maxlength="30"' }),
      campo({ name: "telefono", label: "Teléfono", value: p?.telefono, attrs: 'maxlength="30"' }),
      campo({ name: "ciudad", label: "Ciudad", value: p?.ciudad, attrs: 'maxlength="80"' }),
      campo({ name: "direccion", label: "Dirección", value: p?.direccion, full: true, attrs: 'maxlength="200"' }),
    ],
  },
  clientes: {
    singular: "cliente",
    ruta: "/api/clientes",
    columnas: [
      { titulo: "Nombre", celda: (c) => html`<strong>${c.nombre}</strong><br><small class="muted">${c.email}</small>` },
      { titulo: "Documento", celda: (c) => c.documento },
      { titulo: "Teléfono", celda: (c) => c.telefono ?? "—" },
      { titulo: "Ciudad", celda: (c) => c.ciudad ?? "—" },
    ],
    campos: (c) => [
      campo({ name: "nombre", label: "Nombre completo", value: c?.nombre, required: true, attrs: 'maxlength="120"' }),
      campo({ name: "email", label: "Correo", type: "email", value: c?.email, required: true }),
      campo({ name: "documento", label: "Documento", value: c?.documento, required: true, attrs: 'maxlength="30"' }),
      campo({ name: "telefono", label: "Teléfono", value: c?.telefono, attrs: 'maxlength="30"' }),
      campo({ name: "ciudad", label: "Ciudad", value: c?.ciudad, attrs: 'maxlength="80"' }),
      campo({ name: "direccion", label: "Dirección", value: c?.direccion, full: true, attrs: 'maxlength="200"' }),
    ],
  },
};

export async function renderUsuarios(cont) {
  let tipo = "proveedores";
  const datos = { proveedores: [], clientes: [] };

  mount(cont, html`
    <div class="section-head"><h2>Gestión de usuarios</h2><span class="spacer"></span>
      <button class="btn accent" data-accion="nuevo" type="button">+ Registrar</button></div>
    <div class="tabs" role="tablist">
      <button type="button" data-tab="proveedores" role="tab">Proveedores</button>
      <button type="button" data-tab="clientes" role="tab">Clientes</button>
    </div>
    <div id="tabla"></div>`);

  async function cargar() {
    datos[tipo] = await api.get(CONFIG[tipo].ruta);
    pintar();
  }

  function pintar() {
    const cfg = CONFIG[tipo];
    cont.querySelectorAll("[data-tab]").forEach((b) => b.classList.toggle("active", b.dataset.tab === tipo));
    $("[data-accion=nuevo]", cont).textContent = `+ Registrar ${cfg.singular}`;
    mount($("#tabla", cont), tabla({
      vacio: `No hay ${tipo} registrados`,
      filas: datos[tipo],
      columnas: [
        ...cfg.columnas,
        { titulo: "Estado", celda: (u) => html`<span class="badge ${u.activo ? "ok" : "bad"}">${u.activo ? "ACTIVO" : "INACTIVO"}</span>` },
        { titulo: "", clase: "actions", celda: (u) => [btn("editar", u.id, "Editar", "ghost"), u.activo ? btn("eliminar", u.id, "Desactivar", "danger") : ""] },
      ],
    }));
  }

  function formulario(u) {
    const cfg = CONFIG[tipo];
    const editando = !!u;
    modal({
      titulo: editando ? `Editar ${cfg.singular}` : `Registrar ${cfg.singular}`,
      ancho: 700,
      cuerpo: html`<form id="fUsuario"><div class="form-grid">
        ${cfg.campos(u)}
        ${campo({ name: "password", label: editando ? "Nueva contraseña (opcional)" : "Contraseña", type: "password", required: !editando, full: true,
          attrs: 'minlength="8" maxlength="72" autocomplete="new-password"', hint: "Mínimo 8 caracteres" })}
      </div></form>`,
      acciones: [
        { texto: "Cancelar", clase: "ghost", onClick: (c) => c() },
        { texto: "Guardar", clase: "accent", onClick: async (cerrar, el) => {
            const f = $("#fUsuario", el);
            if (!f.reportValidity()) return;
            try {
              const d = leerForm(f);
              if (editando) await api.put(`${cfg.ruta}/${u.id}`, d); else await api.post(cfg.ruta, d);
              cerrar();
              toast(editando ? "Cambios guardados" : `${cfg.singular[0].toUpperCase() + cfg.singular.slice(1)} registrado`, "ok");
              await cargar();
            } catch (e) { mostrarError(e); }
          } },
      ],
    });
  }

  cont.addEventListener("click", (e) => {
    const t = e.target.closest("[data-tab]");
    if (t) { tipo = t.dataset.tab; cargar().catch(mostrarError); }
  });

  acciones(cont, {
    nuevo: () => formulario(null),
    editar: (id) => formulario(datos[tipo].find((x) => String(x.id) === id)),
    eliminar: async (id) => {
      const u = datos[tipo].find((x) => String(x.id) === id);
      if (await confirmar(`¿Desactivar a «${u.empresa ?? u.nombre}»? No podrá iniciar sesión, pero se conserva su historial.`, { titulo: "Desactivar usuario", textoOk: "Desactivar" })) {
        await api.del(`${CONFIG[tipo].ruta}/${id}`);
        toast("Usuario desactivado", "ok");
        await cargar();
      }
    },
  });
  await cargar();
}
