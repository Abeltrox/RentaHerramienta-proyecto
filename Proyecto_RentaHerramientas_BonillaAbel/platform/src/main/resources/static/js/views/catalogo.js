// Catálogo: búsqueda por categoría / texto / fechas libres y reserva (solo CLIENTE).
import { api, assetUrl, session } from "../core/api.js";
import { badge, campo, diasEntre, hoyISO, html, leerForm, modal, money, mostrarError, mount, raw, sumarDias, toast, $ } from "../core/ui.js";

const IVA = 0.19; // solo informativo: el valor definitivo lo calcula el servidor

export async function renderCatalogo(cont, { rol = null } = {}) {
  const categorias = await api.get("/api/categorias");
  const hoy = hoyISO();

  mount(cont, html`
    <form class="filters" id="filtros">
      <div class="field"><label for="q">Buscar</label><input id="q" name="q" placeholder="Rotomartillo, planta eléctrica…"></div>
      <div class="field"><label for="cat">Categoría</label>
        <select id="cat" name="categoriaId"><option value="">Todas</option>${categorias.map((c) => html`<option value="${c.id}">${c.nombre}</option>`)}</select></div>
      <div class="field"><label for="desde">Libre desde</label><input id="desde" name="desde" type="date" min="${hoy}"></div>
      <div class="field"><label for="hasta">Libre hasta</label><input id="hasta" name="hasta" type="date" min="${hoy}"></div>
      <button class="btn accent" type="submit">Buscar</button>
    </form>
    <div id="resultado"></div>`);

  const form = $("#filtros", cont);
  const resultado = $("#resultado", cont);

  async function buscar() {
    const d = leerForm(form);
    if ((d.desde && !d.hasta) || (!d.desde && d.hasta)) { toast("Indica ambas fechas (desde y hasta) o ninguna", "bad"); return; }
    if (d.desde && d.hasta < d.desde) { toast("La fecha final no puede ser anterior a la inicial", "bad"); return; }
    const p = new URLSearchParams();
    if (d.q) p.set("q", d.q);
    if (d.categoriaId) p.set("categoriaId", d.categoriaId);
    if (d.desde) { p.set("desde", d.desde); p.set("hasta", d.hasta); }
    resultado.innerHTML = '<div class="spinner"></div>';
    try {
      const lista = await api.get("/api/herramientas?" + p);
      pintar(lista);
    } catch (e) { resultado.innerHTML = ""; mostrarError(e); }
  }

  function pintar(lista) {
    if (!lista.length) { mount(resultado, html`<div class="card empty">No encontramos herramientas con esos filtros.</div>`); return; }
    mount(resultado, html`<div class="grid">${lista.map((h) => html`
      <article class="tool">
        <div class="thumb">${h.imagenUrl ? raw(`<img loading="lazy" alt="" src="${encodeURI(assetUrl(h.imagenUrl))}">`) : "🛠"}</div>
        <div class="body">
          <div class="name">${h.nombre}</div>
          <div class="meta">${h.categoria} · ${h.proveedor}</div>
          <div>${badge(h.estado)}${h.reservaAutomatica ? raw(' <span class="badge info">Confirmación inmediata</span>') : ""}</div>
          <div class="price">${money(h.precioDia)} <small>/ día</small></div>
          <button class="btn block" data-id="${h.id}" type="button">Ver detalle</button>
        </div>
      </article>`)}</div>`);
    resultado.querySelectorAll("button[data-id]").forEach((b) =>
      b.addEventListener("click", () => detalle(lista.find((h) => String(h.id) === b.dataset.id), form)));
  }

  form.addEventListener("submit", (e) => { e.preventDefault(); buscar(); });
  await buscar();
}

function detalle(h, form) {
  const sesion = session.user();
  const esCliente = sesion?.rol === "CLIENTE";
  const f = leerForm(form);
  const inicio = f.desde ?? sumarDias(hoyISO(), 1);
  const fin = f.hasta ?? sumarDias(inicio, 2);

  const cuerpoReserva = esCliente && h.estado === "DISPONIBLE"
    ? html`<hr style="border:0;border-top:1px solid var(--line);margin:1rem 0">
        <h3>Reservar</h3>
        <form id="formReserva">
          <div class="form-grid">
            ${campo({ name: "fechaInicio", label: "Desde", type: "date", value: inicio, required: true, attrs: `min="${hoyISO()}"` })}
            ${campo({ name: "fechaFin", label: "Hasta", type: "date", value: fin, required: true, attrs: `min="${hoyISO()}"` })}
            ${campo({ name: "observaciones", label: "Observaciones (opcional)", type: "textarea", full: true, attrs: 'maxlength="500"' })}
          </div>
          <div id="calculo" class="muted"></div>
        </form>`
    : !sesion
      ? html`<p class="muted">Para reservar necesitas una cuenta de cliente.</p><a class="btn accent" href="/pages/login.html">Iniciar sesión</a> <a class="btn ghost" href="/pages/register.html">Crear cuenta</a>`
      : esCliente ? html`<p class="muted">Esta herramienta no está disponible para alquiler por ahora.</p>` : "";

  const acciones = esCliente && h.estado === "DISPONIBLE"
    ? [{ texto: "Cancelar", clase: "ghost", onClick: (c) => c() },
       { texto: "Solicitar reserva", clase: "accent", onClick: async (cerrar, el) => {
           const d = leerForm($("#formReserva", el));
           try {
             const r = await api.post("/api/reservas", { herramientaId: h.id, fechaInicio: d.fechaInicio, fechaFin: d.fechaFin, observaciones: d.observaciones });
             cerrar();
             toast(r.estado === "APROBADA" ? "Reserva confirmada. Ya puedes pagarla desde «Mis reservas»." : "Solicitud enviada. El proveedor la revisará pronto.", "ok");
           } catch (e) { mostrarError(e); }
         } }]
    : [{ texto: "Cerrar", clase: "ghost", onClick: (c) => c() }];

  const m = modal({
    titulo: h.nombre,
    ancho: 640,
    cuerpo: html`
      ${h.imagenUrl ? raw(`<img alt="" style="border-radius:10px;margin-bottom:1rem;max-height:260px;width:100%;object-fit:cover" src="${encodeURI(assetUrl(h.imagenUrl))}">`) : ""}
      <dl class="kv">
        <dt>Categoría</dt><dd>${h.categoria}</dd>
        <dt>Proveedor</dt><dd>${h.proveedor}</dd>
        <dt>Marca</dt><dd>${h.marca ?? "—"}</dd>
        <dt>Precio por día</dt><dd>${money(h.precioDia)} <span class="muted">(+ IVA 19 %)</span></dd>
        <dt>Estado</dt><dd>${badge(h.estado)}</dd>
      </dl>
      <p>${h.descripcion ?? "Sin descripción."}</p>
      ${cuerpoReserva}`,
    acciones,
  });

  const calculo = $("#calculo", m.el);
  if (calculo) {
    const actualizar = () => {
      const d = leerForm($("#formReserva", m.el));
      if (!d.fechaInicio || !d.fechaFin || d.fechaFin < d.fechaInicio) { calculo.textContent = "Revisa las fechas."; return; }
      const dias = diasEntre(d.fechaInicio, d.fechaFin);
      const sub = dias * Number(h.precioDia);
      calculo.innerHTML = html`<strong>${dias} día(s)</strong> · Subtotal ${money(sub)} · IVA ${money(sub * IVA)} · <strong>Total ${money(sub * (1 + IVA))}</strong>`.toString();
    };
    $("#formReserva", m.el).addEventListener("input", actualizar);
    actualizar();
  }
}
