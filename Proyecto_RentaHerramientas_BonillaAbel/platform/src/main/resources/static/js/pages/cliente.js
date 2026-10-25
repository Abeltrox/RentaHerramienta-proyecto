import { iniciarPanel } from "../core/panel.js";
import { renderCatalogo } from "../views/catalogo.js";
import { renderPagos } from "../views/pagos.js";
import { renderPerfil } from "../views/perfil.js";
import { renderReservas } from "../views/reservas.js";

iniciarPanel({
  rol: "CLIENTE",
  secciones: [
    { id: "explorar", icono: "🔎", titulo: "Explorar herramientas", render: renderCatalogo },
    { id: "reservas", icono: "📅", titulo: "Mis reservas", render: renderReservas },
    { id: "pagos", icono: "💳", titulo: "Pagos y facturas", render: renderPagos },
    { id: "perfil", icono: "👤", titulo: "Mi perfil", render: renderPerfil },
  ],
});
