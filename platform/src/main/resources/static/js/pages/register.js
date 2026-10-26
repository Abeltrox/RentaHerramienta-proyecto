import { RUTA_POR_ROL, api, session } from "../core/api.js";
import { $, html, leerForm, mount } from "../core/ui.js";

const form = $("#form");
form.addEventListener("submit", async (e) => {
  e.preventDefault();
  if (!form.reportValidity()) return;
  const d = leerForm(form);
  if (d.password !== d.confirmar) {
    mount($("#aviso"), html`<div class="alert bad">Las contraseñas no coinciden.</div>`);
    return;
  }
  delete d.confirmar;
  const boton = $("button[type=submit]", form);
  boton.disabled = true;
  try {
    const r = await api.post("/api/auth/registro", d);
    session.set({ token: r.token, usuario: r.usuario });
    location.href = RUTA_POR_ROL[r.usuario.rol];
  } catch (err) {
    const extra = err.details?.length ? " " + err.details.join("; ") : "";
    mount($("#aviso"), html`<div class="alert bad">${err.message + extra}</div>`);
  } finally { boton.disabled = false; }
});
