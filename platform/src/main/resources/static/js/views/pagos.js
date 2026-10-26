// Pagos en línea, historial de pagos y facturas (con descarga en PDF).
import { api } from "../core/api.js";
import { acciones, badge, btn, campo, fechaHora, html, leerForm, modal, money, mostrarError, mount, tabla, toast, $ } from "../core/ui.js";

const METODOS = [
  { value: "TARJETA_CREDITO", label: "Tarjeta de crédito" },
  { value: "TARJETA_DEBITO", label: "Tarjeta débito" },
  { value: "PSE", label: "PSE" },
  { value: "NEQUI", label: "Nequi" },
];

/** Flujo de pago de una reserva APROBADA (pasarela simulada). */
export function pagarReserva(reserva, alTerminar) {
  modal({
    titulo: `Pagar reserva #${reserva.id}`,
    cuerpo: html`
      <dl class="kv">
        <dt>Herramienta</dt><dd>${reserva.herramienta}</dd>
        <dt>Periodo</dt><dd>${reserva.fechaInicio} → ${reserva.fechaFin} (${reserva.dias} días)</dd>
        <dt>Subtotal</dt><dd>${money(reserva.subtotal)}</dd>
        <dt>IVA (19 %)</dt><dd>${money(reserva.iva)}</dd>
        <dt>Total a pagar</dt><dd>${money(reserva.totalConIva)}</dd>
      </dl>
      <form id="formPago">
        ${campo({ name: "metodo", label: "Método de pago", options: METODOS, required: true })}
        ${campo({ name: "ultimosDigitos", label: "Últimos 4 dígitos de tu tarjeta / cuenta", attrs: 'inputmode="numeric" pattern="\\d{4}" maxlength="4" placeholder="4242"', hint: "Pasarela de PRUEBA: no se cobra dinero real. Usa 0000 para simular un pago rechazado." })}
      </form>`,
    acciones: [
      { texto: "Cancelar", clase: "ghost", onClick: (c) => c() },
      { texto: `Pagar ${money(reserva.totalConIva)}`, clase: "accent", onClick: async (cerrar, el) => {
          const f = $("#formPago", el);
          if (!f.reportValidity()) return;
          const d = leerForm(f);
          try {
            const r = await api.post("/api/pagos", { reservaId: reserva.id, metodo: d.metodo, ultimosDigitos: d.ultimosDigitos });
            cerrar();
            toast(`Pago aprobado. Factura ${r.factura.numero} generada.`, "ok");
            alTerminar?.();
          } catch (e) { mostrarError(e); }
        } },
    ],
  });
}

export async function descargarFacturaDeReserva(reservaId) {
  const f = await api.get(`/api/facturas/reserva/${reservaId}`);
  await api.download(`/api/facturas/${f.id}/pdf`, `${f.numero}.pdf`);
}

export async function renderPagos(cont, { rol }) {
  const [facturas, pagos] = await Promise.all([api.get("/api/facturas"), api.get("/api/pagos")]);
  const mostrarCliente = rol !== "CLIENTE";
  const mostrarProveedor = rol !== "PROVEEDOR";
  const total = facturas.reduce((s, f) => s + Number(f.total), 0);

  mount(cont, html`
    <div class="cards">
      <div class="stat"><div class="label">Facturas emitidas</div><div class="value">${facturas.length}</div></div>
      <div class="stat"><div class="label">${rol === "CLIENTE" ? "Total pagado" : "Total facturado"}</div><div class="value">${money(total)}</div></div>
    </div>
    <div class="section-head"><h2>Facturas</h2></div>
    ${tabla({
      vacio: "Aún no hay facturas. Se generan automáticamente al pagar una reserva.",
      filas: facturas,
      columnas: [
        { titulo: "Número", celda: (f) => f.numero },
        { titulo: "Reserva", celda: (f) => "#" + f.reservaId },
        { titulo: "Herramienta", celda: (f) => f.herramienta },
        ...(mostrarCliente ? [{ titulo: "Cliente", celda: (f) => f.cliente }] : []),
        ...(mostrarProveedor ? [{ titulo: "Proveedor", celda: (f) => f.proveedor }] : []),
        { titulo: "Subtotal", clase: "right", celda: (f) => money(f.subtotal) },
        { titulo: "IVA", clase: "right", celda: (f) => money(f.iva) },
        { titulo: "Total", clase: "right", celda: (f) => money(f.total) },
        { titulo: "Emitida", celda: (f) => fechaHora(f.fechaEmision) },
        { titulo: "", clase: "actions", celda: (f) => btn("pdf", f.id, "⬇ PDF", "ghost") },
      ],
    })}
    <div class="section-head" style="margin-top:1.5rem"><h2>Historial de pagos</h2></div>
    ${tabla({
      vacio: "Sin pagos registrados.",
      filas: pagos,
      columnas: [
        { titulo: "Referencia", celda: (p) => p.referencia },
        { titulo: "Reserva", celda: (p) => "#" + p.reservaId },
        { titulo: "Método", celda: (p) => p.metodo.replaceAll("_", " ") },
        { titulo: "Monto (con IVA)", clase: "right", celda: (p) => money(p.monto) },
        { titulo: "Estado", celda: (p) => badge(p.estado) },
        { titulo: "Fecha", celda: (p) => fechaHora(p.fechaPago) },
      ],
    })}`);

  acciones(cont, {
    pdf: async (id) => {
      const f = facturas.find((x) => String(x.id) === id);
      await api.download(`/api/facturas/${id}/pdf`, `${f.numero}.pdf`);
    },
  });
}
