import { iniciarPanel } from "../core/panel.js";
import { renderCategorias } from "../views/categorias.js";
import { renderDanios } from "../views/danios.js";
import { renderInventario } from "../views/herramientas.js";
import { renderPagos } from "../views/pagos.js";
import { renderPerfil } from "../views/perfil.js";
import { renderReportes, renderResumen } from "../views/reportes.js";
import { renderReservas } from "../views/reservas.js";
import { renderUsuarios } from "../views/usuarios.js";

iniciarPanel({
  rol: "ADMIN",
  secciones: [
    { id: "resumen", icono: "📊", titulo: "Resumen", render: renderResumen },
    { id: "usuarios", icono: "👥", titulo: "Usuarios", render: renderUsuarios },
    { id: "herramientas", icono: "🛠", titulo: "Herramientas", render: renderInventario },
    { id: "categorias", icono: "🏷", titulo: "Categorías", render: renderCategorias },
    { id: "reservas", icono: "📅", titulo: "Alquileres", render: renderReservas },
    { id: "pagos", icono: "💳", titulo: "Pagos y facturación", render: renderPagos },
    { id: "incidencias", icono: "⚠️", titulo: "Incidencias", render: renderDanios },
    { id: "reportes", icono: "📈", titulo: "Reportes", render: renderReportes },
    { id: "perfil", icono: "👤", titulo: "Mi perfil", render: renderPerfil },
  ],
});
