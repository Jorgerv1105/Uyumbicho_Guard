-- ============================================================
-- UyumbichoGuard - Esquema inicial de base de datos
-- Corresponde 1:1 a las entidades JPA de las Partes 1 y 5B.
-- ============================================================

-- ===================== USUARIOS =====================
CREATE TABLE usuarios (
    id                  BIGSERIAL PRIMARY KEY,
    cedula              VARCHAR(10)  NOT NULL,
    nombres             VARCHAR(100) NOT NULL,
    apellidos           VARCHAR(100) NOT NULL,
    email               VARCHAR(150) NOT NULL,
    password            VARCHAR(255) NOT NULL,
    telefono            VARCHAR(15),
    rol                 VARCHAR(20)  NOT NULL,
    estado              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    intentos_fallidos   INTEGER      NOT NULL DEFAULT 0,
    fecha_bloqueo_hasta TIMESTAMP,
    ultimo_acceso       TIMESTAMP,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_usuarios_cedula UNIQUE (cedula),
    CONSTRAINT uk_usuarios_email  UNIQUE (email)
);

-- ===================== RESIDENTES =====================
CREATE TABLE residentes (
    id                    BIGSERIAL PRIMARY KEY,
    usuario_id            BIGINT UNIQUE REFERENCES usuarios(id),
    nombres_completos      VARCHAR(150) NOT NULL,
    cedula                VARCHAR(10)  NOT NULL,
    telefono_contacto     VARCHAR(15),
    manzana               VARCHAR(20)  NOT NULL,
    numero_casa           VARCHAR(20)  NOT NULL,
    direccion_referencia  VARCHAR(255),
    estado                VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_residentes_cedula ON residentes(cedula);

-- ===================== VEHÍCULOS =====================
CREATE TABLE vehiculos (
    id                BIGSERIAL PRIMARY KEY,
    placa             VARCHAR(10) NOT NULL,
    marca             VARCHAR(50),
    modelo            VARCHAR(50),
    color             VARCHAR(30),
    tipo              VARCHAR(20) NOT NULL,
    anio_fabricacion  INTEGER,
    residente_id      BIGINT REFERENCES residentes(id),
    es_visitante      BOOLEAN NOT NULL DEFAULT FALSE,
    activo            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_vehiculos_placa UNIQUE (placa)
);

CREATE INDEX idx_vehiculos_residente ON vehiculos(residente_id);

-- ===================== REGISTROS DE ACCESO (GARITA) =====================
CREATE TABLE registros_acceso (
    id              BIGSERIAL PRIMARY KEY,
    placa           VARCHAR(10) NOT NULL,
    vehiculo_id     BIGINT REFERENCES vehiculos(id),
    tipo_movimiento VARCHAR(10) NOT NULL,
    fecha_hora      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    vigilante_id    BIGINT      NOT NULL REFERENCES usuarios(id),
    foto_placa_url  VARCHAR(500),
    origen          VARCHAR(20) NOT NULL,
    confianza_ocr   DOUBLE PRECISION,
    observaciones   VARCHAR(255)
);

-- Índices críticos: esta tabla se consulta en CADA ingreso/salida y
-- alimenta el dashboard de vehículos activos (Parte 10).
CREATE INDEX idx_registros_placa ON registros_acceso(placa);
CREATE INDEX idx_registros_fecha_hora ON registros_acceso(fecha_hora DESC);
CREATE INDEX idx_registros_placa_fecha ON registros_acceso(placa, fecha_hora DESC);

-- ===================== LISTA NEGRA =====================
CREATE TABLE lista_negra (
    id                 BIGSERIAL PRIMARY KEY,
    placa              VARCHAR(10)  NOT NULL,
    motivo             VARCHAR(255) NOT NULL,
    registrado_por_id  BIGINT       NOT NULL REFERENCES usuarios(id),
    fecha_expiracion   TIMESTAMP,
    activo             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_lista_negra_placa ON lista_negra(placa) WHERE activo = TRUE;

-- ===================== ALERTAS DE SEGURIDAD =====================
CREATE TABLE alertas_seguridad (
    id                 BIGSERIAL PRIMARY KEY,
    tipo               VARCHAR(40)  NOT NULL,
    nivel              VARCHAR(10)  NOT NULL,
    descripcion        VARCHAR(500) NOT NULL,
    fecha_hora         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    registro_acceso_id BIGINT REFERENCES registros_acceso(id),
    atendida           BOOLEAN      NOT NULL DEFAULT FALSE,
    atendida_por_id    BIGINT REFERENCES usuarios(id),
    fecha_atencion     TIMESTAMP
);

CREATE INDEX idx_alertas_atendida ON alertas_seguridad(atendida) WHERE atendida = FALSE;

-- ===================== NOTIFICACIONES WHATSAPP =====================
CREATE TABLE notificaciones_whatsapp (
    id                   BIGSERIAL PRIMARY KEY,
    tipo                 VARCHAR(40)  NOT NULL,
    telefono_destino     VARCHAR(20)  NOT NULL,
    nombre_destino       VARCHAR(150),
    mensaje_resumen      VARCHAR(500),
    estado               VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    whatsapp_message_id  VARCHAR(100),
    error_detalle        VARCHAR(500),
    fecha_envio          TIMESTAMP,
    alerta_seguridad_id  BIGINT REFERENCES alertas_seguridad(id),
    registro_acceso_id   BIGINT REFERENCES registros_acceso(id),
    created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notificaciones_estado ON notificaciones_whatsapp(estado);