// Resumen del panel y reportes (ingresos, ranking de equipos, clientes frecuentes y disponibilidad).
import { api } from "../core/api.js";
import { badge, html, money, mount, raw, tabla } from "../core/ui.js";

function barras(datos) {
  if (!datos.length) return html`<div class="empty">Aún no hay pagos registrados.</div>`;
  const max = Math.max(...datos.map((d) => Number(d.total)), 1);
  return html`<div class="bars" role="img" aria-label="Ingresos por mes">${datos.map((d) => html`
    <div class="bar" title="${d.periodo}: ${money(d.total)} (${d.cantidadPagos} pagos)">
      <span class="v">${compacto(d.total)}</span>
      <div class="col" ${raw(`style="height:${Math.max(2, (Number(d.total) / max) * 100)}%"`)}></div>
      <span class="l">${d.periodo.slice(5)}/${d.periodo.slice(2, 4)}</span>
    </div>`)}</div>`;
}
const compacto = (n) => new Intl.NumberFormat("es-CO", { notation: "compact", maximumFractionDigits: 1 }).format(Number(n));

export async function renderResumen(cont, { rol }) {
  const [r, ingresos] = await Promise.all([api.get("/api/reportes/resumen"), api.get("/api/reportes/ingresos")]);
  const cards = [
    ["Ingresos totales", money(r.ingresosTotales)],
    ...(rol === "ADMIN" ? [["Proveedores activos", r.totalProveedores]] : []),
    ["Clientes", r.totalClientes],
    ["Herramientas", r.totalHerramientas],
    ["Reservas pendientes", r.reservasPendientes],
    ["Alquileres activos", r.reservasActivas],
    ["Devoluciones por recibir", r.devolucionesPendientes],
    ["Incidencias abiertas", r.danosPendientes],
  ];
  mount(cont, html`
    <div class="section-head"><h2>${rol === "ADMIN" ? "Resumen de la plataforma" : "Resumen de mi negocio"}</h2></div>
    <div class="cards">${cards.map(([l, v]) => html`<div class="stat"><div class="label">${l}</div><div class="value">${v}</div></div>`)}</div>
    <div class="card"><h3>Ingresos por mes (pagos aprobados, IVA incluido)</h3>${barras(ingresos)}</div>`);
}

export async function renderReportes(cont, { rol }) {
  const [ranking, clientes, disp] = await Promise.all([
    api.get("/api/reportes/herramientas-mas-alquiladas?limite=10"),
    api.get("/api/reportes/clientes-frecuentes?limite=10"),
    api.get("/api/reportes/disponibilidad"),
  ]);
  const libres = disp.filter((d) => d.libreHoy).length;

  mount(cont, html`
    <div class="section-head"><h2>Reportes y estadísticas</h2></div>
    <div class="two-col">
      <div class="card"><h3>Herramientas más alquiladas</h3>
        ${ranking.length ? html`<ol class="rank">${ranking.map((x, i) => html`<li><span class="n">${i + 1}</span>
          <div class="grow"><div><strong>${x.nombre}</strong></div><small class="muted">${x.vecesAlquilada} alquiler(es)</small></div>
          <strong>${money(x.ingresos)}</strong></li>`)}</ol>` : html`<div class="empty">Sin datos todavía.</div>`}</div>
      <div class="card"><h3>Clientes frecuentes</h3>
        ${clientes.length ? html`<ol class="rank">${clientes.map((x, i) => html`<li><span class="n">${i + 1}</span>
          <div class="grow"><div><strong>${x.nombre}</strong></div><small class="muted">${x.reservas} reserva(s)</small></div>
          <strong>${money(x.gastoTotal)}</strong></li>`)}</ol>` : html`<div class="empty">Sin datos todavía.</div>`}</div>
    </div>
    <div class="section-head" style="margin-top:1.5rem"><h2>Disponibilidad y estado de herramientas</h2><span class="spacer"></span>
      <span class="muted">${libres} de ${disp.length} libres hoy</span></div>
    ${tabla({
      filas: disp,
      columnas: [
        { titulo: "Herramienta", celda: (d) => d.nombre },
        { titulo: "Categoría", celda: (d) => d.categoria },
        ...(rol === "ADMIN" ? [{ titulo: "Proveedor", celda: (d) => d.proveedor }] : []),
        { titulo: "Estado", celda: (d) => badge(d.estado) },
        { titulo: "Hoy", celda: (d) => (d.libreHoy ? raw('<span class="badge ok">Libre</span>') : raw('<span class="badge warn">No disponible</span>')) },
      ],
    })}`);
}
