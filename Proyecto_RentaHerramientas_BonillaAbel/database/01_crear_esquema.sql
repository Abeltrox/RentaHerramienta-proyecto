-- =====================================================================
--  Plataforma de Renta de Herramientas y Equipos de Construcción
--  V1: Creación del esquema (PostgreSQL 13+)
--  Flyway lo ejecuta automáticamente al arrancar la aplicación.
--  También puedes correrlo a mano:  psql -U postgres -d renta_herramientas -f V1__crear_esquema.sql
-- =====================================================================

-- ---------- Usuarios (credenciales y rol) ----------
CREATE TABLE usuarios (
    id             BIGSERIAL    PRIMARY KEY,
    nombre         VARCHAR(120) NOT NULL,
    email          VARCHAR(160) NOT NULL,
    password       VARCHAR(100) NOT NULL,              -- hash BCrypt, nunca texto plano
    telefono       VARCHAR(30),
    rol            VARCHAR(20)  NOT NULL,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,  -- eliminación lógica
    fecha_creacion TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_usuarios_email UNIQUE (email),
    CONSTRAINT ck_usuarios_rol   CHECK (rol IN ('ADMIN', 'PROVEEDOR', 'CLIENTE'))
);

-- ---------- Proveedores y clientes (comparten PK con usuarios: relación 1 a 1) ----------
CREATE TABLE proveedores (
    id        BIGINT       PRIMARY KEY,
    empresa   VARCHAR(150) NOT NULL,
    nit       VARCHAR(30)  NOT NULL,
    direccion VARCHAR(200),
    ciudad    VARCHAR(80),
    CONSTRAINT fk_proveedores_usuario FOREIGN KEY (id) REFERENCES usuarios (id),
    CONSTRAINT uq_proveedores_nit UNIQUE (nit)
);

CREATE TABLE clientes (
    id        BIGINT      PRIMARY KEY,
    documento VARCHAR(30) NOT NULL,
    direccion VARCHAR(200),
    ciudad    VARCHAR(80),
    CONSTRAINT fk_clientes_usuario FOREIGN KEY (id) REFERENCES usuarios (id),
    CONSTRAINT uq_clientes_documento UNIQUE (documento)
);

-- ---------- Catálogo ----------
CREATE TABLE categorias (
    id          BIGSERIAL    PRIMARY KEY,
    nombre      VARCHAR(80)  NOT NULL,
    descripcion VARCHAR(300),
    CONSTRAINT uq_categorias_nombre UNIQUE (nombre)
);

CREATE TABLE herramientas (
    id                 BIGSERIAL      PRIMARY KEY,
    proveedor_id       BIGINT         NOT NULL,
    categoria_id       BIGINT         NOT NULL,
    nombre             VARCHAR(150)   NOT NULL,
    descripcion        VARCHAR(1000),
    marca              VARCHAR(80),
    precio_dia         NUMERIC(12, 2) NOT NULL,
    estado             VARCHAR(20)    NOT NULL DEFAULT 'DISPONIBLE',
    imagen_url         VARCHAR(300),
    reserva_automatica BOOLEAN        NOT NULL DEFAULT FALSE,
    fecha_creacion     TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_herramientas_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedores (id),
    CONSTRAINT fk_herramientas_categoria FOREIGN KEY (categoria_id) REFERENCES categorias (id),
    CONSTRAINT ck_herramientas_precio CHECK (precio_dia > 0),
    CONSTRAINT ck_herramientas_estado CHECK (estado IN ('DISPONIBLE', 'EN_MANTENIMIENTO', 'DANADA', 'INACTIVA'))
);

-- ---------- Reservas ----------
CREATE TABLE reservas (
    id               BIGSERIAL      PRIMARY KEY,
    cliente_id       BIGINT         NOT NULL,
    herramienta_id   BIGINT         NOT NULL,
    fecha_inicio     DATE           NOT NULL,
    fecha_fin        DATE           NOT NULL,
    dias             INTEGER        NOT NULL,
    precio_dia       NUMERIC(12, 2) NOT NULL,   -- precio congelado al momento de reservar
    total            NUMERIC(12, 2) NOT NULL,   -- subtotal sin IVA
    estado           VARCHAR(20)    NOT NULL,
    observaciones    VARCHAR(500),
    motivo_rechazo   VARCHAR(300),
    fecha_entrega    TIMESTAMP,
    fecha_devolucion TIMESTAMP,
    fecha_creacion   TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_reservas_cliente     FOREIGN KEY (cliente_id)     REFERENCES clientes (id),
    CONSTRAINT fk_reservas_herramienta FOREIGN KEY (herramienta_id) REFERENCES herramientas (id),
    CONSTRAINT ck_reservas_fechas CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT ck_reservas_dias   CHECK (dias > 0),
    CONSTRAINT ck_reservas_estado CHECK (estado IN
        ('PENDIENTE', 'APROBADA', 'PAGADA', 'ENTREGADA', 'DEVUELTA', 'RECHAZADA', 'CANCELADA', 'VENCIDA'))
);

-- ---------- Pagos y facturas ----------
CREATE TABLE pagos (
    id         BIGSERIAL      PRIMARY KEY,
    reserva_id BIGINT         NOT NULL,
    monto      NUMERIC(12, 2) NOT NULL,   -- total con IVA
    metodo     VARCHAR(20)    NOT NULL,
    estado     VARCHAR(20)    NOT NULL,
    referencia VARCHAR(60)    NOT NULL,
    fecha_pago TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_pagos_reserva FOREIGN KEY (reserva_id) REFERENCES reservas (id),
    CONSTRAINT uq_pagos_referencia UNIQUE (referencia),
    CONSTRAINT uq_pagos_reserva UNIQUE (reserva_id),          -- una reserva, un pago
    CONSTRAINT ck_pagos_monto  CHECK (monto > 0),
    CONSTRAINT ck_pagos_metodo CHECK (metodo IN ('TARJETA_CREDITO', 'TARJETA_DEBITO', 'PSE', 'NEQUI')),
    CONSTRAINT ck_pagos_estado CHECK (estado IN ('APROBADO', 'RECHAZADO'))
);

CREATE SEQUENCE seq_factura_numero START WITH 1 INCREMENT BY 1;

CREATE TABLE facturas (
    id            BIGSERIAL      PRIMARY KEY,
    numero        VARCHAR(30)    NOT NULL,
    reserva_id    BIGINT         NOT NULL,
    pago_id       BIGINT         NOT NULL,
    subtotal      NUMERIC(12, 2) NOT NULL,
    iva           NUMERIC(12, 2) NOT NULL,
    total         NUMERIC(12, 2) NOT NULL,
    fecha_emision TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_facturas_reserva FOREIGN KEY (reserva_id) REFERENCES reservas (id),
    CONSTRAINT fk_facturas_pago    FOREIGN KEY (pago_id)    REFERENCES pagos (id),
    CONSTRAINT uq_facturas_numero  UNIQUE (numero),
    CONSTRAINT uq_facturas_reserva UNIQUE (reserva_id),       -- una reserva, una factura
    CONSTRAINT ck_facturas_total   CHECK (total = subtotal + iva)
);

-- ---------- Incidencias por daños ----------
CREATE TABLE reportes_danio (
    id               BIGSERIAL      PRIMARY KEY,
    reserva_id       BIGINT         NOT NULL,
    herramienta_id   BIGINT         NOT NULL,
    descripcion      VARCHAR(500)   NOT NULL,
    costo_estimado   NUMERIC(12, 2) NOT NULL DEFAULT 0,
    estado           VARCHAR(20)    NOT NULL,
    fecha_reporte    TIMESTAMP      NOT NULL DEFAULT NOW(),
    fecha_resolucion TIMESTAMP,
    CONSTRAINT fk_danios_reserva     FOREIGN KEY (reserva_id)     REFERENCES reservas (id),
    CONSTRAINT fk_danios_herramienta FOREIGN KEY (herramienta_id) REFERENCES herramientas (id),
    CONSTRAINT ck_danios_costo  CHECK (costo_estimado >= 0),
    CONSTRAINT ck_danios_estado CHECK (estado IN ('REPORTADO', 'EN_REVISION', 'RESUELTO'))
);

-- ---------- Índices (consultas frecuentes: catálogo, disponibilidad, historial y reportes) ----------
CREATE INDEX idx_herramientas_proveedor  ON herramientas (proveedor_id);
CREATE INDEX idx_herramientas_categoria  ON herramientas (categoria_id);
CREATE INDEX idx_herramientas_estado     ON herramientas (estado);
CREATE INDEX idx_reservas_cliente        ON reservas (cliente_id);
CREATE INDEX idx_reservas_estado         ON reservas (estado);
CREATE INDEX idx_reservas_disponibilidad ON reservas (herramienta_id, fecha_inicio, fecha_fin);
CREATE INDEX idx_pagos_fecha             ON pagos (fecha_pago);
CREATE INDEX idx_danios_herramienta      ON reportes_danio (herramienta_id);
CREATE INDEX idx_danios_estado           ON reportes_danio (estado);
