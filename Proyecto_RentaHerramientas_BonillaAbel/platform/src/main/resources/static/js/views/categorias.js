// CRUD de categorías. Solo ADMIN.
import { api } from "../core/api.js";
import { acciones, btn, campo, confirmar, html, leerForm, modal, mostrarError, mount, tabla, toast, $ } from "../core/ui.js";

export async function renderCategorias(cont) {
  const lista = await api.get("/api/categorias");

  mount(cont, html`
    <div class="section-head"><h2>Categorías de herramientas</h2><span class="spacer"></span>
      <button class="btn accent" data-accion="nueva" type="button">+ Nueva categoría</button></div>
    <div id="tabla"></div>`);

  const pintar = () => mount($("#tabla", cont), tabla({
    filas: lista,
    columnas: [
      { titulo: "Nombre", celda: (c) => html`<strong>${c.nombre}</strong>` },
      { titulo: "Descripción", celda: (c) => c.descripcion ?? "—" },
      { titulo: "", clase: "actions", celda: (c) => [btn("editar", c.id, "Editar", "ghost"), btn("eliminar", c.id, "Eliminar", "danger")] },
    ],
  }));

  async function recargar() { lista.splice(0, lista.length, ...(await api.get("/api/categorias"))); pintar(); }

  function formulario(c) {
    modal({
      titulo: c ? "Editar categoría" : "Nueva categoría",
      cuerpo: html`<form id="fCat">
        ${campo({ name: "nombre", label: "Nombre", value: c?.nombre, required: true, attrs: 'maxlength="80"' })}
        ${campo({ name: "descripcion", label: "Descripción", type: "textarea", value: c?.descripcion, attrs: 'maxlength="300"' })}
      </form>`,
      acciones: [
        { texto: "Cancelar", clase: "ghost", onClick: (x) => x() },
        { texto: "Guardar", clase: "accent", onClick: async (cerrar, el) => {
            const f = $("#fCat", el);
            if (!f.reportValidity()) return;
            try {
              const d = leerForm(f);
              if (c) await api.put(`/api/categorias/${c.id}`, d); else await api.post("/api/categorias", d);
              cerrar(); toast("Categoría guardada", "ok"); await recargar();
            } catch (e) { mostrarError(e); }
          } },
      ],
    });
  }

  acciones(cont, {
    nueva: () => formulario(null),
    editar: (id) => formulario(lista.find((x) => String(x.id) === id)),
    eliminar: async (id) => {
      const c = lista.find((x) => String(x.id) === id);
      if (await confirmar(`¿Eliminar la categoría «${c.nombre}»?`, { titulo: "Eliminar categoría" })) {
        await api.del(`/api/categorias/${id}`); toast("Categoría eliminada", "ok"); await recargar();
      }
    },
  });
  pintar();
}
