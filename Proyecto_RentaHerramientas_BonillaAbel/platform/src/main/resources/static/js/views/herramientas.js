// Inventario de herramientas (CRUD). ADMIN ve y edita todo; PROVEEDOR solo lo suyo.
import { api, assetUrl } from "../core/api.js";
import { acciones, badge, btn, campo, confirmar, html, leerForm, modal, money, mostrarError, mount, raw, tabla, toast, $ } from "../core/ui.js";

const ESTADOS = ["DISPONIBLE", "EN_MANTENIMIENTO", "DANADA"].map((e) => ({ value: e, label: e.replaceAll("_", " ") }));
const MAX_MB = 5;

export async function renderInventario(cont, { rol }) {
  const [lista, categorias, proveedores] = await Promise.all([
    api.get("/api/herramientas/gestion"),
    api.get("/api/categorias"),
    rol === "ADMIN" ? api.get("/api/proveedores") : Promise.resolve([]),
  ]);

  mount(cont, html`
    <div class="section-head"><h2>Inventario de herramientas</h2><span class="spacer"></span>
      <button class="btn accent" data-accion="nueva" type="button">+ Nueva herramienta</button></div>
    <div id="tabla"></div>`);

  const pintar = () => mount($("#tabla", cont), tabla({
    vacio: "Todavía no hay herramientas. Agrega la primera con «Nueva herramienta».",
    filas: lista,
    columnas: [
      { titulo: "", celda: (h) => h.imagenUrl ? raw(`<img alt="" style="width:54px;height:40px;object-fit:cover;border-radius:6px" src="${encodeURI(assetUrl(h.imagenUrl))}">`) : "🛠" },
      { titulo: "Herramienta", celda: (h) => html`<strong>${h.nombre}</strong><br><small class="muted">${h.marca ?? ""}</small>` },
      { titulo: "Categoría", celda: (h) => h.categoria },
      ...(rol === "ADMIN" ? [{ titulo: "Proveedor", celda: (h) => h.proveedor }] : []),
      { titulo: "Precio/día", clase: "right", celda: (h) => money(h.precioDia) },
      { titulo: "Estado", celda: (h) => badge(h.estado) },
      { titulo: "Reserva auto.", celda: (h) => (h.reservaAutomatica ? "Sí" : "No") },
      { titulo: "", clase: "actions", celda: (h) => [btn("editar", h.id, "Editar", "ghost"), btn("eliminar", h.id, "Eliminar", "danger")] },
    ],
  }));

  async function recargar() {
    lista.splice(0, lista.length, ...(await api.get("/api/herramientas/gestion")));
    pintar();
  }

  function formulario(h) {
    const editando = !!h;
    modal({
      titulo: editando ? `Editar «${h.nombre}»` : "Nueva herramienta",
      ancho: 700,
      cuerpo: html`<form id="fHerr"><div class="form-grid">
        ${rol === "ADMIN" && !editando ? campo({ name: "proveedorId", label: "Proveedor", required: true, attrs: "data-num",
          options: [{ value: "", label: "Selecciona…" }, ...proveedores.filter((p) => p.activo).map((p) => ({ value: p.id, label: p.empresa }))] }) : ""}
        ${campo({ name: "nombre", label: "Nombre", value: h?.nombre, required: true, attrs: 'maxlength="150"' })}
        ${campo({ name: "marca", label: "Marca", value: h?.marca, attrs: 'maxlength="80"' })}
        ${campo({ name: "categoriaId", label: "Categoría", value: h?.categoriaId, required: true, attrs: "data-num",
          options: [{ value: "", label: "Selecciona…" }, ...categorias.map((c) => ({ value: c.id, label: c.nombre }))] })}
        ${campo({ name: "precioDia", label: "Precio por día (COP, sin IVA)", type: "number", value: h?.precioDia, required: true, attrs: 'min="1" step="1000"' })}
        ${campo({ name: "estado", label: "Estado", value: h?.estado ?? "DISPONIBLE", options: ESTADOS })}
        <div class="field"><label>&nbsp;</label><div class="check"><input type="checkbox" id="f_auto" name="reservaAutomatica" ${raw(h?.reservaAutomatica ? "checked" : "")}><label for="f_auto">Aprobar reservas automáticamente</label></div></div>
        ${campo({ name: "descripcion", label: "Descripción", type: "textarea", value: h?.descripcion, full: true, attrs: 'maxlength="1000"' })}
        <div class="field full"><label for="f_img">Imagen (JPG, PNG o WEBP · máx. ${MAX_MB} MB)</label><input id="f_img" type="file" accept="image/jpeg,image/png,image/webp"></div>
      </div></form>`,
      acciones: [
        { texto: "Cancelar", clase: "ghost", onClick: (c) => c() },
        { texto: "Guardar", clase: "accent", onClick: async (cerrar, el) => {
            const f = $("#fHerr", el);
            if (!f.reportValidity()) return;
            const archivo = $("#f_img", el).files[0];
            if (archivo && archivo.size > MAX_MB * 1024 * 1024) { toast(`La imagen supera ${MAX_MB} MB`, "bad"); return; }
            const datos = leerForm(f);
            try {
              const guardada = editando ? await api.put(`/api/herramientas/${h.id}`, datos) : await api.post("/api/herramientas", datos);
              if (archivo) {
                const fd = new FormData();
                fd.append("archivo", archivo);
                await api.upload(`/api/herramientas/${guardada.id}/imagen`, fd);
              }
              cerrar();
              toast(editando ? "Herramienta actualizada" : "Herramienta creada", "ok");
              await recargar();
            } catch (e) { mostrarError(e); }
          } },
      ],
    });
  }

  acciones(cont, {
    nueva: () => formulario(null),
    editar: (id) => formulario(lista.find((x) => String(x.id) === id)),
    eliminar: async (id) => {
      const h = lista.find((x) => String(x.id) === id);
      if (await confirmar(`¿Eliminar «${h.nombre}»? Dejará de aparecer en el catálogo (el historial se conserva).`, { titulo: "Eliminar herramienta", textoOk: "Eliminar" })) {
        await api.del(`/api/herramientas/${id}`);
        toast("Herramienta eliminada", "ok");
        await recargar();
      }
    },
  });
  pintar();
}
