// Página pública: catálogo abierto a visitantes.
import { RUTA_POR_ROL, session } from "../core/api.js";
import { $, html, mount } from "../core/ui.js";
import { renderCatalogo } from "../views/catalogo.js";

const u = session.user();
mount($("#acciones"), u
  ? html`<a class="btn accent" href="${RUTA_POR_ROL[u.rol]}">Ir a mi panel</a>`
  : html`<a class="btn ghost" href="/pages/login.html">Iniciar sesión</a><a class="btn accent" href="/pages/register.html">Crear cuenta</a>`);

renderCatalogo($("#catalogo"), { rol: u?.rol ?? null }).catch((e) => {
  $("#catalogo").innerHTML = '<div class="card empty">No se pudo cargar el catálogo. Verifica que el servidor esté encendido.</div>';
  console.error(e);
});
