// Estructura común de los paneles (Administrador, Proveedor y Cliente): barra lateral, cabecera y navegación por #hash.
import { RUTA_POR_ROL, session } from "./api.js";
import { $, $$, html, mostrarError, mount, spinner } from "./ui.js";

const NOMBRE_ROL = { ADMIN: "Administrador", PROVEEDOR: "Proveedor", CLIENTE: "Cliente" };

/**
 * @param rol       rol que puede ver este panel
 * @param secciones [{ id, icono, titulo, render(contenedor, ctx) }]
 */
export function iniciarPanel({ rol, secciones }) {
  const usuario = session.user();
  // Guardia de acceso (la seguridad real la hace el backend; esto solo evita pantallas vacías).
  if (!session.token() || !usuario) { location.replace("/pages/login.html"); return; }
  if (usuario.rol !== rol) { location.replace(RUTA_POR_ROL[usuario.rol] ?? "/index.html"); return; }

  const ctx = { rol, usuario };
  const app = $("#app");
  mount(app, html`
    <div class="shell">
      <aside class="sidebar" id="sidebar" aria-label="Menú principal">
        <a class="brand" href="/index.html"><span class="mark">⚙</span> RentaHerramientas</a>
        <div class="role">${NOMBRE_ROL[rol]}</div>
        <nav>${secciones.map((s) => html`<a href="#${s.id}" data-sec="${s.id}"><span aria-hidden="true">${s.icono}</span> ${s.titulo}</a>`)}</nav>
      </aside>
      <div class="main">
        <div class="main-top">
          <button class="btn ghost sm menu-toggle" id="menuToggle" aria-label="Abrir menú">☰</button>
          <strong id="tituloSeccion"></strong>
          <div class="who"><div>${usuario.nombre}</div><small>${usuario.email}</small></div>
          <button class="btn ghost sm" id="salir">Salir</button>
        </div>
        <main class="content" id="contenido"></main>
      </div>
    </div>`);

  const sidebar = $("#sidebar");
  $("#menuToggle").addEventListener("click", () => sidebar.classList.toggle("open"));
  $("#salir").addEventListener("click", () => { session.clear(); location.href = "/index.html"; });

  let token = 0; // evita que una respuesta lenta pise a una sección más reciente
  async function mostrar() {
    const id = (location.hash || "#" + secciones[0].id).slice(1);
    const sec = secciones.find((s) => s.id === id) ?? secciones[0];
    sidebar.classList.remove("open");
    $$("[data-sec]", sidebar).forEach((a) => a.classList.toggle("active", a.dataset.sec === sec.id));
    $("#tituloSeccion").textContent = sec.titulo;
    document.title = `${sec.titulo} · ${NOMBRE_ROL[rol]} · RentaHerramientas`;
    const cont = $("#contenido");
    const mio = ++token;
    cont.innerHTML = spinner();
    const destino = document.createElement("div");
    try {
      await sec.render(destino, ctx);
      if (mio === token) { cont.replaceChildren(destino); }
    } catch (e) {
      if (mio === token) { cont.replaceChildren(); mostrarError(e); cont.innerHTML = '<div class="card empty">No se pudo cargar esta sección.</div>'; }
    }
  }
  window.addEventListener("hashchange", mostrar);
  mostrar();
}
