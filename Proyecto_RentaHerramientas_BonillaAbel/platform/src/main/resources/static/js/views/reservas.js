// Reservas: el cliente solicita/paga/cancela; el proveedor (o admin) aprueba, entrega y recibe.
import { api } from "../core/api.js";
import { acciones, badge, btn, campo, confirmar, fecha, html, leerForm, modal, money, mostrarError, mount, tabla, toast, $ } from "../core/ui.js";
import { descargarFacturaDeReserva, pagarReserva } from "./pagos.js";

const ESTADOS = ["", "PENDIENTE", "APROBADA", "PAGADA", "ENTREGADA", "DEVUELTA", "RECHAZADA", "CANCELADA", "VENCIDA"];

export async function renderReservas(cont, { rol }) {
  const reservas = await api.get("/api/reservas");
  const gestiona = rol === "ADMIN" || rol === "PROVEEDOR";
  let filtro = "";

  mount(cont, html`
    <div class="section-head">
      <h2>${rol === "CLIENTE" ? "Mis reservas e historial de alquileres" : "Reservas y alquileres"}</h2><span class="spacer"></span>
      <div class="field" style="margin:0"><label class="muted" for="fEstado">Estado</label>
        <select id="fEstado">${ESTADOS.map((e) => html`<option value="${e}">${e || "Todos"}</option>`)}</select></div>
    </div>
    <div id="lista"></div>`);

  function pintar() {
    const filas = reservas.filter((r) => !filtro || r.estado === filtro);
    mount($("#lista", cont), tabla({
      vacio: rol === "CLIENTE" ? "Aún no tienes reservas. Explora el catálogo para alquilar tu primera herramienta." : "No hay reservas con este filtro.",
      filas,
      columnas: [
        { titulo: "#", celda: (r) => r.id },
        { titulo: "Herramienta", celda: (r) => r.herramienta },
        ...(rol !== "CLIENTE" ? [{ titulo: "Cliente", celda: (r) => r.cliente }] : []),
        ...(rol !== "PROVEEDOR" ? [{ titulo: "Proveedor", celda: (r) => r.proveedor }] : []),
        { titulo: "Periodo", celda: (r) => html`<span class="nowrap">${fecha(r.fechaInicio)} → ${fecha(r.fechaFin)}</span><br><small class="muted">${r.dias} día(s)</small>` },
        { titulo: "Total c/IVA", clase: "right", celda: (r) => money(r.totalConIva) },
        { titulo: "Estado", celda: (r) => html`${badge(r.estado)}${r.motivoRechazo ? html`<br><small class="muted">${r.motivoRechazo}</small>` : ""}` },
        { titulo: "Acciones", clase: "actions", celda: (r) => botones(r) },
      ],
    }));
  }

  function botones(r) {
    const b = [];
    if (gestiona) {
      if (r.estado === "PENDIENTE") b.push(btn("aprobar", r.id, "Aprobar", "ok"));
      if (r.estado === "PENDIENTE" || r.estado === "APROBADA") b.push(btn("rechazar", r.id, "Rechazar", "danger"));
      if (r.estado === "PAGADA") b.push(btn("entregar", r.id, "Registrar entrega", "accent"));
      if (r.estado === "ENTREGADA") b.push(btn("devolver", r.id, "Confirmar devolución", "accent"));
    }
    if (rol === "CLIENTE" || rol === "ADMIN") {
      if (r.estado === "PENDIENTE" || r.estado === "APROBADA") b.push(btn("cancelar", r.id, "Cancelar", "ghost"));
    }
    if (rol === "CLIENTE" && r.estado === "APROBADA") b.push(btn("pagar", r.id, "Pagar", "accent"));
    if (["PAGADA", "ENTREGADA", "DEVUELTA"].includes(r.estado)) b.push(btn("factura", r.id, "⬇ Factura", "ghost"));
    return b;
  }

  async function recargar() {
    const nuevas = await api.get("/api/reservas");
    reservas.splice(0, reservas.length, ...nuevas);
    pintar();
  }

  const reserva = (id) => reservas.find((r) => String(r.id) === id);

  acciones(cont, {
    aprobar: async (id) => { await api.patch(`/api/reservas/${id}/aprobar`); toast("Reserva aprobada. El cliente ya puede pagar.", "ok"); await recargar(); },
    rechazar: (id) => new Promise((resolve) => {
      modal({
        titulo: `Rechazar reserva #${id}`,
        cuerpo: html`<form id="fRechazo">${campo({ name: "motivo", label: "Motivo del rechazo", type: "textarea", required: true, attrs: 'maxlength="300"' })}</form>`,
        acciones: [
          { texto: "Volver", clase: "ghost", onClick: (c) => { c(); resolve(); } },
          { texto: "Rechazar", clase: "danger", onClick: async (c, el) => {
              const f = $("#fRechazo", el);
              if (!f.reportValidity()) return;
              try { await api.patch(`/api/reservas/${id}/rechazar`, { motivo: leerForm(f).motivo }); c(); toast("Reserva rechazada", "ok"); await recargar(); resolve(); }
              catch (e) { mostrarError(e); }
            } },
        ],
      });
    }),
    cancelar: async (id) => {
      if (await confirmar("¿Seguro que quieres cancelar esta reserva?", { titulo: "Cancelar reserva", textoOk: "Sí, cancelar" })) {
        await api.patch(`/api/reservas/${id}/cancelar`); toast("Reserva cancelada", "ok"); await recargar();
      }
    },
    entregar: async (id) => {
      if (await confirmar("¿Confirmas que entregaste la herramienta al cliente?", { titulo: "Registrar entrega", textoOk: "Confirmar entrega", clase: "ok" })) {
        await api.patch(`/api/reservas/${id}/entregar`); toast("Entrega registrada", "ok"); await recargar();
      }
    },
    devolver: (id) => new Promise((resolve) => {
      modal({
        titulo: `Confirmar devolución · reserva #${id}`,
        cuerpo: html`<form id="fDev">
          <div class="check"><input type="checkbox" id="chkDanio" name="hayDanio"><label for="chkDanio">La herramienta regresó con daños</label></div>
          <div id="datosDanio" class="hidden">
            ${campo({ name: "descripcionDanio", label: "Describe el daño", type: "textarea", attrs: 'maxlength="500"' })}
            ${campo({ name: "costoEstimado", label: "Costo estimado de reparación (COP)", type: "number", attrs: 'min="0" step="1000"' })}
          </div></form>`,
        acciones: [
          { texto: "Volver", clase: "ghost", onClick: (c) => { c(); resolve(); } },
          { texto: "Confirmar devolución", clase: "accent", onClick: async (c, el) => {
              const d = leerForm($("#fDev", el));
              if (d.hayDanio && !d.descripcionDanio) { toast("Describe el daño encontrado", "bad"); return; }
              try { await api.patch(`/api/reservas/${id}/devolver`, d); c(); toast(d.hayDanio ? "Devolución registrada y daño reportado" : "Devolución registrada", "ok"); await recargar(); resolve(); }
              catch (e) { mostrarError(e); }
            } },
        ],
      });
      const chk = $("#chkDanio");
      chk.addEventListener("change", () => $("#datosDanio").classList.toggle("hidden", !chk.checked));
    }),
    pagar: (id) => pagarReserva(reserva(id), recargar),
    factura: (id) => descargarFacturaDeReserva(id),
  });

  $("#fEstado", cont).addEventListener("change", (e) => { filtro = e.target.value; pintar(); });
  pintar();
}
