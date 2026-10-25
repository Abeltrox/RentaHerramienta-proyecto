// Perfil propio: PROVEEDOR y CLIENTE pueden editar sus datos; el ADMIN solo los consulta.
import { api, session } from "../core/api.js";
import { campo, html, leerForm, mostrarError, mount, toast, $ } from "../core/ui.js";

export async function renderPerfil(cont, { rol, usuario }) {
  if (rol === "ADMIN") {
    mount(cont, html`<div class="section-head"><h2>Mi perfil</h2></div>
      <div class="card"><dl class="kv"><dt>Nombre</dt><dd>${usuario.nombre}</dd><dt>Correo</dt><dd>${usuario.email}</dd><dt>Rol</dt><dd>Administrador</dd></dl></div>`);
    return;
  }
  const ruta = rol === "PROVEEDOR" ? `/api/proveedores/${usuario.id}` : `/api/clientes/${usuario.id}`;
  const p = await api.get(ruta);

  mount(cont, html`
    <div class="section-head"><h2>Mi perfil</h2></div>
    <form class="card" id="fPerfil" style="max-width:720px"><div class="form-grid">
      ${campo({ name: "nombre", label: "Nombre", value: p.nombre, required: true, attrs: 'maxlength="120"' })}
      ${campo({ name: "email", label: "Correo", type: "email", value: p.email, required: true })}
      ${rol === "PROVEEDOR"
        ? [campo({ name: "empresa", label: "Empresa", value: p.empresa, required: true }), campo({ name: "nit", label: "NIT", value: p.nit, required: true })]
        : campo({ name: "documento", label: "Documento", value: p.documento, required: true, full: true })}
      ${campo({ name: "telefono", label: "Teléfono", value: p.telefono })}
      ${campo({ name: "ciudad", label: "Ciudad", value: p.ciudad })}
      ${campo({ name: "direccion", label: "Dirección", value: p.direccion, full: true })}
      ${campo({ name: "password", label: "Nueva contraseña (opcional)", type: "password", full: true, attrs: 'minlength="8" maxlength="72" autocomplete="new-password"', hint: "Déjala vacía para conservar la actual" })}
    </div><button class="btn accent" type="submit">Guardar cambios</button></form>`);

  $("#fPerfil", cont).addEventListener("submit", async (e) => {
    e.preventDefault();
    try {
      const actualizado = await api.put(ruta, leerForm(e.target));
      const s = session.current();
      session.set({ ...s, usuario: { ...s.usuario, nombre: actualizado.nombre, email: actualizado.email } });
      toast("Perfil actualizado", "ok");
    } catch (err) { mostrarError(err); }
  });
}
