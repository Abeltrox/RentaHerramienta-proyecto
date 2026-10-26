import { RUTA_POR_ROL, api, session } from "../core/api.js";
import { $, html, mostrarError, mount } from "../core/ui.js";

const existente = session.user();
if (existente && session.token()) location.replace(RUTA_POR_ROL[existente.rol]);

const params = new URLSearchParams(location.search);
if (params.has("expirada")) mount($("#aviso"), html`<div class="alert bad">Tu sesión expiró. Inicia sesión nuevamente.</div>`);
if (params.has("registrado")) mount($("#aviso"), html`<div class="alert ok">Cuenta creada. Ya puedes iniciar sesión.</div>`);

const form = $("#form");
form.addEventListener("submit", async (e) => {
  e.preventDefault();
  const boton = $("button[type=submit]", form);
  boton.disabled = true;
  try {
    const r = await api.post("/api/auth/login", { email: form.email.value.trim(), password: form.password.value });
    session.set({ token: r.token, usuario: r.usuario });
    location.href = RUTA_POR_ROL[r.usuario.rol];
  } catch (err) {
    mostrarError(err);
    mount($("#aviso"), html`<div class="alert bad">${err.message}</div>`);
  } finally { boton.disabled = false; }
});

// Atajos para rellenar las cuentas de prueba (datos del script SQL inicial)
document.querySelectorAll("[data-demo]").forEach((b) =>
  b.addEventListener("click", () => {
    const [email, pass] = b.dataset.demo.split("|");
    form.email.value = email;
    form.password.value = pass;
  }));
