// Incidencias por equipos dañados. El proveedor (y el admin) reportan; el admin gestiona el estado.
import { api } from "../core/api.js";
import { acciones, badge, btn, campo, fechaHora, html, leerForm, modal, money, mostrarError, mount, tabla, toast, $ } from "../core/ui.js";

export async function renderDanios(cont, { rol }) {
  const lista = await api.get("/api/danios");

  mount(cont, html`
    <div class="section-head"><h2>Incidencias de equipos dañados</h2><span class="spacer"></span>
      <button class="btn accent" data-accion="nuevo" type="button">+ Reportar daño</button></div>
    <div id="tabla"></div>`);

  const pintar = () => mount($("#tabla", cont), tabla({
    vacio: "No hay incidencias registradas. 🎉",
    filas: lista,
    columnas: [
      { titulo: "#", celda: (d) => d.id },
      { titulo: "Herramienta", celda: (d) => d.herramienta },
      { titulo: "Reserva", celda: (d) => "#" + d.reservaId },
      { titulo: "Descripción", celda: (d) => d.descripcion },
      { titulo: "Costo estimado", clase: "right", celda: (d) => money(d.costoEstimado) },
      { titulo: "Estado", celda: (d) => badge(d.estado) },
      { titulo: "Reportado", celda: (d) => fechaHora(d.fechaReporte) },
      ...(rol === "ADMIN" ? [{ titulo: "Gestión", clase: "actions", celda: (d) => [
        d.estado === "REPORTADO" ? btn("revision", d.id, "Pasar a revisión", "ghost") : "",
        d.estado !== "RESUELTO" ? btn("resolver", d.id, "Marcar resuelto", "ok") : "",
      ] }] : []),
    ],
  }));

  async function recargar() {
    lista.splice(0, lista.length, ...(await api.get("/api/danios")));
    pintar();
  }

  async function cambiar(id, estado, mensaje) {
    await api.patch(`/api/danios/${id}/estado`, { estado });
    toast(mensaje, "ok");
    await recargar();
  }

  acciones(cont, {
    revision: (id) => cambiar(id, "EN_REVISION", "Incidencia en revisión"),
    resolver: (id) => cambiar(id, "RESUELTO", "Incidencia resuelta: la herramienta vuelve a estar disponible"),
    nuevo: async () => {
      const reservas = (await api.get("/api/reservas")).filter((r) => r.estado === "ENTREGADA" || r.estado === "DEVUELTA");
      if (!reservas.length) { toast("No hay reservas entregadas o devueltas sobre las que reportar daños", "bad"); return; }
      modal({
        titulo: "Reportar daño",
        cuerpo: html`<form id="fDanio">
          ${campo({ name: "reservaId", label: "Reserva", required: true, attrs: "data-num",
            options: reservas.map((r) => ({ value: r.id, label: `#${r.id} · ${r.herramienta} · ${r.cliente}` })) })}
          ${campo({ name: "descripcion", label: "Descripción del daño", type: "textarea", required: true, attrs: 'maxlength="500"' })}
          ${campo({ name: "costoEstimado", label: "Costo estimado (COP)", type: "number", value: 0, attrs: 'min="0" step="1000"' })}
        </form>`,
        acciones: [
          { texto: "Cancelar", clase: "ghost", onClick: (c) => c() },
          { texto: "Reportar", clase: "accent", onClick: async (cerrar, el) => {
              const f = $("#fDanio", el);
              if (!f.reportValidity()) return;
              try { await api.post("/api/danios", leerForm(f)); cerrar(); toast("Daño reportado. La herramienta quedó marcada como dañada.", "ok"); await recargar(); }
              catch (e) { mostrarError(e); }
            } },
        ],
      });
    },
  });
  pintar();
}
