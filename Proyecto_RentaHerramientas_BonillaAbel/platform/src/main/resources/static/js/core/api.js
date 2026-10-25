// Cliente HTTP + sesión (JWT). Todo el frontend habla con la API a través de este módulo.

// Si el frontend se abre con Live Server (puerto 5500/5173/3000) la API está en :8080; si lo sirve Spring, es el mismo origen.
const BASE = window.API_BASE ?? (["5500", "5173", "3000"].includes(location.port) ? "http://localhost:8080" : "");

const KEY = "rh_session";

export const session = {
  get() {
    try { return JSON.parse(localStorage.getItem(KEY)); } catch { return null; }
  },
  set(data) {
    try { localStorage.setItem(KEY, JSON.stringify(data)); } catch { /* modo privado: la sesión vive solo en memoria */ }
    memory = data;
  },
  clear() {
    try { localStorage.removeItem(KEY); } catch { /* nada */ }
    memory = null;
  },
  current() { return memory ?? this.get(); },
  token() { return this.current()?.token ?? null; },
  user() { return this.current()?.usuario ?? null; },
};
let memory = null;

export class ApiError extends Error {
  constructor(status, message, details = []) {
    super(message);
    this.status = status;
    this.details = details;
  }
}

async function request(method, path, { body, form, raw } = {}) {
  const headers = { Accept: "application/json" };
  const token = session.token();
  if (token) headers.Authorization = `Bearer ${token}`;
  let payload;
  if (form) {
    payload = form; // el navegador agrega el boundary de multipart
  } else if (body !== undefined) {
    headers["Content-Type"] = "application/json";
    payload = JSON.stringify(body);
  }

  let res;
  try {
    res = await fetch(BASE + path, { method, headers, body: payload });
  } catch {
    throw new ApiError(0, "No se pudo conectar con el servidor. ¿Está encendido el backend?");
  }

  if (res.status === 401 && token) {
    // Token vencido o inválido: se cierra la sesión y se manda al login.
    session.clear();
    if (!location.pathname.endsWith("login.html")) location.href = "/pages/login.html?expirada=1";
  }
  if (!res.ok) {
    let err = {};
    try { err = await res.json(); } catch { /* sin cuerpo JSON */ }
    throw new ApiError(res.status, err.message || `Error ${res.status}`, err.details || []);
  }
  if (raw) return res;
  if (res.status === 204) return null;
  return res.json();
}

export const api = {
  get: (path) => request("GET", path),
  post: (path, body) => request("POST", path, { body }),
  put: (path, body) => request("PUT", path, { body }),
  patch: (path, body) => request("PATCH", path, { body }),
  del: (path) => request("DELETE", path),
  upload: (path, formData) => request("POST", path, { form: formData }),
  /** Descarga un archivo protegido (PDF) enviando el JWT, y lo guarda con el nombre indicado. */
  async download(path, filename) {
    const res = await request("GET", path, { raw: true });
    const blob = await res.blob();
    const url = URL.createObjectURL(blob);
    const a = Object.assign(document.createElement("a"), { href: url, download: filename });
    document.body.appendChild(a);
    a.click();
    a.remove();
    setTimeout(() => URL.revokeObjectURL(url), 2000);
  },
};

export const assetUrl = (p) => (p ? BASE + p : null);

export const RUTA_POR_ROL = {
  ADMIN: "/pages/admin.html",
  PROVEEDOR: "/pages/proveedor.html",
  CLIENTE: "/pages/cliente.html",
};
