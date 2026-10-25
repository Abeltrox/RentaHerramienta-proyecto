// Utilidades de interfaz. IMPORTANTE (seguridad): todo texto que viene de la API pasa por `html`/`esc`,
// que escapa HTML, para evitar XSS al construir el DOM con plantillas.

const ENTIDADES = { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" };
export const esc = (v) => String(v ?? "").replace(/[&<>"']/g, (c) => ENTIDADES[c]);

class Raw { constructor(s) { this.s = s; } toString() { return this.s; } }
/** Marca un fragmento como HTML ya seguro (resultado de otro `html`). */
export const raw = (s) => new Raw(s);

/** Plantilla etiquetada: html`<b>${dato}</b>` escapa cada valor; arrays se unen; raw() no se escapa. */
export function html(strings, ...values) {
  const parte = (v) => (v instanceof Raw ? v.s : Array.isArray(v) ? v.map(parte).join("") : esc(v));
  return raw(strings.reduce((acc, s, i) => acc + s + (i < values.length ? parte(values[i]) : ""), ""));
}

export function mount(container, template) {
  container.innerHTML = template.toString();
  return container;
}

export const $ = (sel, root = document) => root.querySelector(sel);
export const $$ = (sel, root = document) => [...root.querySelectorAll(sel)];

// ---------- formato ----------
const COP = new Intl.NumberFormat("es-CO", { style: "currency", currency: "COP", maximumFractionDigits: 0 });
export const money = (n) => COP.format(Number(n ?? 0));
export const fecha = (s) => {
  if (!s) return "—";
  const d = new Date(s.length === 10 ? s + "T00:00:00" : s);
  return d.toLocaleDateString("es-CO", { day: "2-digit", month: "short", year: "numeric" });
};
export const fechaHora = (s) => (s ? new Date(s).toLocaleString("es-CO", { dateStyle: "medium", timeStyle: "short" }) : "—");
export const hoyISO = () => {
  const d = new Date();
  return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
};
export const sumarDias = (iso, n) => {
  const d = new Date(iso + "T00:00:00");
  d.setDate(d.getDate() + n);
  return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
};
export const diasEntre = (a, b) => Math.round((new Date(b) - new Date(a)) / 86400000) + 1;

const TONOS = {
  DISPONIBLE: "ok", APROBADA: "info", PAGADA: "ok", ENTREGADA: "info", DEVUELTA: "ok", RESUELTO: "ok", APROBADO: "ok",
  PENDIENTE: "warn", EN_MANTENIMIENTO: "warn", EN_REVISION: "warn", REPORTADO: "warn",
  RECHAZADA: "bad", CANCELADA: "bad", VENCIDA: "bad", DANADA: "bad", INACTIVA: "bad", RECHAZADO: "bad",
};
export const badge = (estado) => html`<span class="badge ${TONOS[estado] ?? ""}">${String(estado ?? "").replaceAll("_", " ")}</span>`;

// ---------- feedback ----------
export function toast(mensaje, tipo = "") {
  let cont = $("#toasts");
  if (!cont) {
    cont = Object.assign(document.createElement("div"), { id: "toasts" });
    cont.setAttribute("role", "status");
    document.body.appendChild(cont);
  }
  const t = Object.assign(document.createElement("div"), { className: `toast ${tipo}`, textContent: mensaje });
  cont.appendChild(t);
  setTimeout(() => t.remove(), 4500);
}

/** Muestra un error de la API (incluye detalles de validación si vienen). */
export function mostrarError(e) {
  const extra = e?.details?.length ? " — " + e.details.join("; ") : "";
  toast((e?.message ?? "Ocurrió un error") + extra, "bad");
}

export const spinner = () => '<div class="spinner" aria-label="Cargando"></div>';

// ---------- modal ----------
/**
 * Abre un modal. `cuerpo` es HTML seguro (resultado de html``). Devuelve { el, cerrar }.
 * `acciones`: [{ texto, clase, onClick(cerrar, el) }]
 */
export function modal({ titulo, cuerpo, acciones = [], ancho }) {
  const overlay = document.createElement("div");
  overlay.className = "overlay";
  overlay.innerHTML = html`
    <div class="modal" role="dialog" aria-modal="true" aria-label="${titulo}" ${raw(ancho ? `style="max-width:${Number(ancho)}px"` : "")}>
      <header><h3>${titulo}</h3><button class="x" type="button" aria-label="Cerrar">&times;</button></header>
      <div class="body">${cuerpo}</div>
      ${acciones.length ? html`<footer>${acciones.map((a, i) => html`<button type="button" class="btn ${a.clase ?? ""}" data-i="${i}">${a.texto}</button>`)}</footer>` : ""}
    </div>`.toString();
  document.body.appendChild(overlay);

  const cerrar = () => { overlay.remove(); document.removeEventListener("keydown", onKey); };
  const onKey = (e) => { if (e.key === "Escape") cerrar(); };
  document.addEventListener("keydown", onKey);
  overlay.addEventListener("mousedown", (e) => { if (e.target === overlay) cerrar(); });
  $(".x", overlay).addEventListener("click", cerrar);
  $$("footer [data-i]", overlay).forEach((b) =>
    b.addEventListener("click", async () => {
      b.disabled = true;
      try { await acciones[Number(b.dataset.i)].onClick(cerrar, overlay); } finally { if (overlay.isConnected) b.disabled = false; }
    }));
  const primero = $("input:not([type=hidden]), select, textarea", overlay);
  if (primero) primero.focus();
  return { el: overlay, cerrar };
}

export function confirmar(mensaje, { titulo = "Confirmar", textoOk = "Aceptar", clase = "danger" } = {}) {
  return new Promise((resolve) => {
    let resuelto = false;
    const m = modal({
      titulo, cuerpo: html`<p>${mensaje}</p>`,
      acciones: [
        { texto: "Cancelar", clase: "ghost", onClick: (c) => { resuelto = true; c(); resolve(false); } },
        { texto: textoOk, clase, onClick: (c) => { resuelto = true; c(); resolve(true); } },
      ],
    });
    // si se cierra con X / Esc / clic fuera, cuenta como cancelar
    new MutationObserver((_, obs) => { if (!m.el.isConnected) { obs.disconnect(); if (!resuelto) resolve(false); } })
      .observe(document.body, { childList: true });
  });
}

/** Lee un <form> como objeto; "" → null, checkboxes → boolean, data-num → número. */
export function leerForm(form) {
  const out = {};
  for (const el of form.elements) {
    if (!el.name) continue;
    if (el.type === "checkbox") out[el.name] = el.checked;
    else if (el.type === "file") continue;
    else if (el.value === "") out[el.name] = null;
    else out[el.name] = el.dataset.num !== undefined || el.type === "number" ? Number(el.value) : el.value;
  }
  return out;
}

export function campo({ name, label, type = "text", value = "", required = false, hint = "", attrs = "", full = false, options }) {
  const id = `f_${name}`;
  let control;
  if (options) {
    control = html`<select id="${id}" name="${name}" ${raw(required ? "required" : "")} ${raw(attrs)}>
      ${options.map((o) => html`<option value="${o.value}" ${raw(String(o.value) === String(value) ? "selected" : "")}>${o.label}</option>`)}</select>`;
  } else if (type === "textarea") {
    control = html`<textarea id="${id}" name="${name}" ${raw(required ? "required" : "")} ${raw(attrs)}>${value ?? ""}</textarea>`;
  } else {
    control = html`<input id="${id}" name="${name}" type="${type}" value="${value ?? ""}" ${raw(required ? "required" : "")} ${raw(attrs)}>`;
  }
  return html`<div class="field ${full ? "full" : ""}"><label for="${id}">${label}${required ? " *" : ""}</label>${control}${hint ? html`<span class="hint">${hint}</span>` : ""}</div>`;
}

export function tabla({ columnas, filas, vacio = "No hay registros para mostrar" }) {
  if (!filas.length) return html`<div class="table-wrap"><div class="empty">${vacio}</div></div>`;
  return html`<div class="table-wrap"><table>
    <thead><tr>${columnas.map((c) => html`<th class="${c.clase ?? ""}">${c.titulo}</th>`)}</tr></thead>
    <tbody>${filas.map((f) => html`<tr>${columnas.map((c) => html`<td class="${c.clase ?? ""}">${c.celda(f)}</td>`)}</tr>`)}</tbody>
  </table></div>`;
}

/** Delegación de eventos: data-accion="x" data-id="1" → handlers.x(id, elemento) */
export function acciones(contenedor, handlers) {
  contenedor.addEventListener("click", async (e) => {
    const b = e.target.closest("[data-accion]");
    if (!b || !contenedor.contains(b)) return;
    const fn = handlers[b.dataset.accion];
    if (!fn) return;
    b.disabled = true;
    try { await fn(b.dataset.id, b); } catch (err) { mostrarError(err); } finally { b.disabled = false; }
  });
}

export const btn = (accion, id, texto, clase = "") =>
  html`<button type="button" class="btn sm ${clase}" data-accion="${accion}" data-id="${id}">${texto}</button>`;
