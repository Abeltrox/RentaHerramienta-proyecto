import { iniciarPanel } from "../core/panel.js";
import { renderDanios } from "../views/danios.js";
import { renderInventario } from "../views/herramientas.js";
import { renderPagos } from "../views/pagos.js";
import { renderPerfil } from "../views/perfil.js";
import { renderReportes, renderResumen } from "../views/reportes.js";
import { renderReservas } from "../views/reservas.js";

iniciarPanel({
  rol: "PROVEEDOR",
  secciones: [
    { id: "resumen", icono: "📊", titulo: "Resumen", render: renderResumen },
    { id: "herramientas", icono: "🛠", titulo: "Mis herramientas", render: renderInventario },
    { id: "reservas", icono: "📅", titulo: "Solicitudes y alquileres", render: renderReservas },
    { id: "facturacion", icono: "🧾", titulo: "Facturación", render: renderPagos },
    { id: "incidencias", icono: "⚠️", titulo: "Daños", render: renderDanios },
    { id: "reportes", icono: "📈", titulo: "Reportes", render: renderReportes },
    { id: "perfil", icono: "👤", titulo: "Mi perfil", render: renderPerfil },
  ],
});
