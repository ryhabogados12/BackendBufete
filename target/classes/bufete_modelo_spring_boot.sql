CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- MODELO OBJETIVO - SISTEMA DE GESTIÓN DOCUMENTAL DEL BUFETE
-- PostgreSQL + Spring Boot / JPA
-- ============================================================
-- IMPORTANTE:
-- 1. Este archivo representa el MODELO OBJETIVO.
-- 2. No es una migración automática de datos existentes.
-- 3. Las validaciones de formularios se manejarán principalmente
--    en DTOs de Spring Boot y Angular.
-- 4. Se mantienen restricciones de integridad técnica/relacional.
-- 5. FileBlob es reutilizable: FileVersion -> ManyToOne -> FileBlob.
-- ============================================================

BEGIN;

-- ============================================================
-- 1. ROLES Y USUARIOS
-- ============================================================

CREATE TABLE IF NOT EXISTS rol (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS permiso (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT,
    modulo VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS rol_permiso (
    rol_id INTEGER NOT NULL REFERENCES rol(id) ON DELETE CASCADE,
    permiso_id INTEGER NOT NULL REFERENCES permiso(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT now(),
    PRIMARY KEY (rol_id, permiso_id)
);

CREATE TABLE IF NOT EXISTS usuario (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100),
    apellido VARCHAR(100),
    email VARCHAR(200) UNIQUE,
    identificacion VARCHAR(100) UNIQUE,
    "contraseña" VARCHAR(255),
    telefono VARCHAR(20),
    direccion TEXT,
    rol_id INTEGER REFERENCES rol(id),
    activo BOOLEAN DEFAULT TRUE,
    nuevo_usuario BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    last_login TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_usuario_rol ON usuario(rol_id);
CREATE INDEX IF NOT EXISTS idx_usuario_activo ON usuario(activo);

-- ============================================================
-- 2. CLIENTES
-- ============================================================

CREATE TABLE IF NOT EXISTS cliente (
    id SERIAL PRIMARY KEY,
    tipo_cliente VARCHAR(20) CHECK (tipo_cliente IN ('NATURAL', 'JURIDICA')),
    nombre VARCHAR(200),
    apellido VARCHAR(200),
    razon_social VARCHAR(300),
    identificacion VARCHAR(100) UNIQUE,
    tipo_documento VARCHAR(20),
    email VARCHAR(200),
    telefono VARCHAR(20),
    direccion TEXT,
    ciudad VARCHAR(100),
    departamento VARCHAR(100),
    pais VARCHAR(100) DEFAULT 'Colombia',
    fecha_nacimiento DATE,
    representante_legal VARCHAR(200),
    activo BOOLEAN DEFAULT TRUE,
    observaciones TEXT,
    created_by INTEGER REFERENCES usuario(id),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_cliente_identificacion ON cliente(identificacion);
CREATE INDEX IF NOT EXISTS idx_cliente_nombre ON cliente(nombre);
CREATE INDEX IF NOT EXISTS idx_cliente_activo ON cliente(activo);

-- ============================================================
-- 3. CATEGORÍAS GENERALES
-- ============================================================

CREATE TABLE IF NOT EXISTS categoria (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    tipo VARCHAR(20) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE (nombre, tipo)
);

-- ============================================================
-- 4. CATÁLOGOS JURÍDICOS
-- ============================================================

CREATE TABLE IF NOT EXISTS jurisdiccion (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(80) UNIQUE NOT NULL,
    nombre VARCHAR(200) UNIQUE NOT NULL,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS asunto (
    id SERIAL PRIMARY KEY,
    jurisdiccion_id INTEGER NOT NULL REFERENCES jurisdiccion(id),
    codigo VARCHAR(100) UNIQUE NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE (jurisdiccion_id, nombre)
);

CREATE INDEX IF NOT EXISTS idx_asunto_jurisdiccion ON asunto(jurisdiccion_id);

CREATE TABLE IF NOT EXISTS etapa_proceso (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(100) UNIQUE NOT NULL,
    nombre VARCHAR(200) UNIQUE NOT NULL,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    orden INTEGER DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS tipo_procedimiento (
    id SERIAL PRIMARY KEY,
    asunto_id INTEGER NOT NULL REFERENCES asunto(id),
    etapa_id INTEGER REFERENCES etapa_proceso(id),
    codigo VARCHAR(120) UNIQUE NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE (asunto_id, nombre)
);

CREATE INDEX IF NOT EXISTS idx_tipo_procedimiento_asunto ON tipo_procedimiento(asunto_id);
CREATE INDEX IF NOT EXISTS idx_tipo_procedimiento_etapa ON tipo_procedimiento(etapa_id);

-- ============================================================
-- 5. PROCESOS
-- ============================================================

CREATE TABLE IF NOT EXISTS proceso (
    id SERIAL PRIMARY KEY,

    -- Datos heredados / compatibilidad con el sistema actual
    numero_proceso VARCHAR(100) UNIQUE,
    nombre VARCHAR(255),
    descripcion TEXT,
    tipo_proceso VARCHAR(100),
    estado VARCHAR(50) DEFAULT 'ACTIVO',
    fecha_creacion TIMESTAMPTZ DEFAULT now(),
    fecha_inicio DATE,
    fecha_cierre DATE,

    -- Relaciones del nuevo modelo jurídico
    jurisdiccion_id INTEGER REFERENCES jurisdiccion(id),
    asunto_id INTEGER REFERENCES asunto(id),
    etapa_actual_id INTEGER REFERENCES etapa_proceso(id),
    tipo_procedimiento_id INTEGER REFERENCES tipo_procedimiento(id),

    -- Información general existente
    cliente_id INTEGER REFERENCES cliente(id),
    abogado_responsable_id INTEGER REFERENCES usuario(id),
    juzgado VARCHAR(200),
    radicado VARCHAR(100),
    demandante VARCHAR(300),
    demandado VARCHAR(300),
    cuantia DECIMAL(15,2),
    observaciones TEXT,
    activo BOOLEAN DEFAULT TRUE,
    created_by INTEGER REFERENCES usuario(id),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),

    CONSTRAINT chk_proceso_estado CHECK (
        estado IN ('ACTIVO', 'SUSPENDIDO', 'CERRADO', 'ARCHIVADO')
    )
);

CREATE INDEX IF NOT EXISTS idx_proceso_jurisdiccion ON proceso(jurisdiccion_id);
CREATE INDEX IF NOT EXISTS idx_proceso_asunto ON proceso(asunto_id);
CREATE INDEX IF NOT EXISTS idx_proceso_etapa ON proceso(etapa_actual_id);
CREATE INDEX IF NOT EXISTS idx_proceso_procedimiento ON proceso(tipo_procedimiento_id);
CREATE INDEX IF NOT EXISTS idx_proceso_cliente ON proceso(cliente_id);
CREATE INDEX IF NOT EXISTS idx_proceso_abogado ON proceso(abogado_responsable_id);
CREATE INDEX IF NOT EXISTS idx_proceso_estado ON proceso(estado);

-- ============================================================
-- 6. INFORMACIÓN ESPECÍFICA DE PROCESOS PENALES
-- ============================================================

CREATE TABLE IF NOT EXISTS proceso_penal (
    proceso_id INTEGER PRIMARY KEY REFERENCES proceso(id) ON DELETE CASCADE,
    nunc VARCHAR(100),
    competencia_municipal VARCHAR(255),
    competencia_circuito VARCHAR(255),
    competencia_circuito_especializado VARCHAR(255),
    hubo_victimas BOOLEAN,
    imputacion_descripcion TEXT,
    fecha_creacion TIMESTAMPTZ DEFAULT now(),
    fecha_actualizacion TIMESTAMPTZ DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_proceso_penal_nunc
ON proceso_penal(nunc)
WHERE nunc IS NOT NULL;

-- ============================================================
-- 7. PERSONAS / PARTICIPANTES
-- ============================================================

CREATE TABLE IF NOT EXISTS persona (
    id SERIAL PRIMARY KEY,
    nombres VARCHAR(200),
    apellidos VARCHAR(200),
    numero_documento VARCHAR(100),
    tipo_documento VARCHAR(30),
    lugar_residencia TEXT,
    correo_electronico VARCHAR(200),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_persona_documento ON persona(numero_documento);
CREATE INDEX IF NOT EXISTS idx_persona_nombre ON persona(nombres, apellidos);

CREATE TABLE IF NOT EXISTS proceso_persona (
    id SERIAL PRIMARY KEY,
    proceso_id INTEGER NOT NULL REFERENCES proceso(id) ON DELETE CASCADE,
    persona_id INTEGER NOT NULL REFERENCES persona(id),
    rol VARCHAR(50) NOT NULL,
    privado_libertad BOOLEAN,
    tipo_detencion VARCHAR(100),
    establecimiento_o_residencia TEXT,
    observaciones TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),

    CONSTRAINT chk_proceso_persona_rol CHECK (
        rol IN ('IMPUTADO', 'VICTIMA', 'DEMANDANTE', 'DEMANDADO', 'OTRO')
    ),
    CONSTRAINT chk_tipo_detencion CHECK (
        tipo_detencion IS NULL OR tipo_detencion IN (
            'RESIDENCIA',
            'PENITENCIARIO'
        )
    ),
    UNIQUE (proceso_id, persona_id, rol)
);

CREATE INDEX IF NOT EXISTS idx_proceso_persona_proceso ON proceso_persona(proceso_id);
CREATE INDEX IF NOT EXISTS idx_proceso_persona_persona ON proceso_persona(persona_id);

-- ============================================================
-- 8. DELITOS Y BIENES JURÍDICOS
-- ============================================================

CREATE TABLE IF NOT EXISTS delito (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(100) UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS proceso_penal_delito (
    proceso_penal_id INTEGER NOT NULL REFERENCES proceso_penal(proceso_id) ON DELETE CASCADE,
    delito_id INTEGER NOT NULL REFERENCES delito(id),
    observaciones TEXT,
    PRIMARY KEY (proceso_penal_id, delito_id)
);

CREATE TABLE IF NOT EXISTS bien_juridico (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(100) UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS proceso_penal_bien_juridico (
    proceso_penal_id INTEGER NOT NULL REFERENCES proceso_penal(proceso_id) ON DELETE CASCADE,
    bien_juridico_id INTEGER NOT NULL REFERENCES bien_juridico(id),
    PRIMARY KEY (proceso_penal_id, bien_juridico_id)
);

-- ============================================================
-- 9. FISCALÍA
-- ============================================================

CREATE TABLE IF NOT EXISTS proceso_fiscal (
    id SERIAL PRIMARY KEY,
    proceso_id INTEGER NOT NULL REFERENCES proceso(id) ON DELETE CASCADE,
    nombre_fiscal VARCHAR(255),
    unidad VARCHAR(50),
    numero_fiscalia VARCHAR(100),
    correo_electronico VARCHAR(200),
    contacto_telefonico VARCHAR(50),
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),

    CONSTRAINT chk_fiscal_unidad CHECK (
        unidad IS NULL OR unidad IN ('SECCIONAL', 'LOCAL', 'ESPECIALIZADA')
    )
);

CREATE INDEX IF NOT EXISTS idx_proceso_fiscal_proceso ON proceso_fiscal(proceso_id);

-- ============================================================
-- 10. JUZGADOS
-- ============================================================

CREATE TABLE IF NOT EXISTS proceso_juzgado (
    id SERIAL PRIMARY KEY,
    proceso_id INTEGER NOT NULL REFERENCES proceso(id) ON DELETE CASCADE,
    tipo_juzgado VARCHAR(50),
    nombre_juzgado VARCHAR(255),
    observaciones TEXT,
    fecha_inicio_vinculacion DATE,
    fecha_fin_vinculacion DATE,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),

    CONSTRAINT chk_tipo_juzgado CHECK (
        tipo_juzgado IS NULL OR tipo_juzgado IN ('JUZGADO_GARANTIAS', 'JUZGADO_CONOCIMIENTO')
    )
);

CREATE INDEX IF NOT EXISTS idx_proceso_juzgado_proceso ON proceso_juzgado(proceso_id);

-- ============================================================
-- 11. EXPEDIENTES
-- ============================================================

CREATE TABLE IF NOT EXISTS expediente (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(255),
    descripcion TEXT,
    estado VARCHAR(50) DEFAULT 'ACTIVO',
    fecha_creacion TIMESTAMPTZ DEFAULT now(),
    fecha_cierre TIMESTAMPTZ,
    proceso_id INTEGER NOT NULL REFERENCES proceso(id) ON DELETE CASCADE,
    root_node_id UUID UNIQUE,
    orden INTEGER DEFAULT 1,
    created_by INTEGER REFERENCES usuario(id),
    updated_at TIMESTAMPTZ DEFAULT now(),
    is_deleted BOOLEAN DEFAULT FALSE,

    CONSTRAINT chk_expediente_estado CHECK (
        estado IN ('ACTIVO', 'ARCHIVADO', 'CERRADO')
    )
);

CREATE INDEX IF NOT EXISTS idx_expediente_proceso ON expediente(proceso_id) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_expediente_estado ON expediente(estado) WHERE is_deleted = FALSE;

-- ============================================================
-- 12. SISTEMA DOCUMENTAL
-- ============================================================

CREATE TABLE IF NOT EXISTS node (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    expediente_id INTEGER REFERENCES expediente(id) ON DELETE CASCADE,
    contable_id INTEGER,
    parent_id UUID REFERENCES node(id) ON DELETE CASCADE,
    type VARCHAR(10) NOT NULL CHECK (type IN ('FOLDER', 'FILE')),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    modulo VARCHAR(20) NOT NULL DEFAULT 'DOCUMENTAL',
    created_by INTEGER REFERENCES usuario(id),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    is_deleted BOOLEAN DEFAULT FALSE,
    current_version_id UUID,
    size_bytes BIGINT DEFAULT 0,
    item_count INTEGER DEFAULT 0,
    last_accessed TIMESTAMPTZ
);

-- Unicidad solamente entre nodos activos y hermanos del mismo contexto.
CREATE UNIQUE INDEX IF NOT EXISTS uq_node_sibling_name_active
ON node (
    COALESCE(parent_id, '00000000-0000-0000-0000-000000000000'::uuid),
    name,
    COALESCE(expediente_id, 0),
    COALESCE(contable_id, 0),
    modulo
)
WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_node_parent ON node(parent_id) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_node_expediente ON node(expediente_id) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_node_modulo ON node(modulo) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_node_type ON node(type) WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS node_closure (
    ancestor_id UUID NOT NULL REFERENCES node(id) ON DELETE CASCADE,
    descendant_id UUID NOT NULL REFERENCES node(id) ON DELETE CASCADE,
    depth INTEGER NOT NULL CHECK (depth >= 0),
    PRIMARY KEY (ancestor_id, descendant_id)
);

CREATE INDEX IF NOT EXISTS idx_node_closure_ancestor ON node_closure(ancestor_id, depth);
CREATE INDEX IF NOT EXISTS idx_node_closure_descendant ON node_closure(descendant_id, depth);

-- ============================================================
-- 13. ARCHIVOS Y VERSIONES
-- ============================================================

CREATE TABLE IF NOT EXISTS file_blob (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    storage_key VARCHAR(500) NOT NULL,
    bucket_name VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    checksum_sha256 VARCHAR(64) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    original_name VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT now(),
    is_image BOOLEAN DEFAULT FALSE,
    thumbnail_key VARCHAR(500),

    -- Deduplificación del contenido físico.
    -- NO impide que el Blob tenga varias FileVersion.
    UNIQUE (checksum_sha256, size_bytes)
);

CREATE INDEX IF NOT EXISTS idx_file_blob_storage_key ON file_blob(storage_key);
CREATE INDEX IF NOT EXISTS idx_file_blob_checksum ON file_blob(checksum_sha256);

CREATE TABLE IF NOT EXISTS file_version (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    node_id UUID NOT NULL REFERENCES node(id) ON DELETE CASCADE,

    -- MUY IMPORTANTE:
    -- Un mismo FileBlob puede tener MUCHAS FileVersion.
    file_blob_id UUID NOT NULL REFERENCES file_blob(id) ON DELETE RESTRICT,

    version_num INTEGER NOT NULL,
    uploaded_by INTEGER REFERENCES usuario(id),
    uploaded_at TIMESTAMPTZ DEFAULT now(),
    note TEXT,
    is_current BOOLEAN DEFAULT TRUE,

    UNIQUE (node_id, version_num)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_file_version_current
ON file_version(node_id)
WHERE is_current = TRUE;

CREATE INDEX IF NOT EXISTS idx_file_version_node ON file_version(node_id);
CREATE INDEX IF NOT EXISTS idx_file_version_blob ON file_version(file_blob_id);
CREATE INDEX IF NOT EXISTS idx_file_version_uploaded_by ON file_version(uploaded_by);

ALTER TABLE node
    ADD CONSTRAINT fk_node_current_version
    FOREIGN KEY (current_version_id)
    REFERENCES file_version(id);

-- ============================================================
-- 14. PLANTILLAS DOCUMENTALES
-- ============================================================

CREATE TABLE IF NOT EXISTS plantilla (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    categoria_id INTEGER REFERENCES categoria(id),
    descripcion TEXT,
    file_blob_id UUID REFERENCES file_blob(id),
    fecha_creacion TIMESTAMPTZ DEFAULT now(),
    responsable_id INTEGER REFERENCES usuario(id),
    activo BOOLEAN DEFAULT TRUE,
    version VARCHAR(20) DEFAULT '1.0',
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_plantilla_categoria ON plantilla(categoria_id);
CREATE INDEX IF NOT EXISTS idx_plantilla_activo ON plantilla(activo);

-- ============================================================
-- 15. PLANTILLA DE ESTRUCTURA DEL EXPEDIENTE
-- ============================================================

CREATE TABLE IF NOT EXISTS plantilla_expediente (
    id SERIAL PRIMARY KEY,
    asunto_id INTEGER NOT NULL REFERENCES asunto(id),
    etapa_id INTEGER REFERENCES etapa_proceso(id),
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE (asunto_id, etapa_id, nombre)
);

CREATE TABLE IF NOT EXISTS plantilla_expediente_item (
    id SERIAL PRIMARY KEY,
    plantilla_expediente_id INTEGER NOT NULL REFERENCES plantilla_expediente(id) ON DELETE CASCADE,
    parent_item_id INTEGER REFERENCES plantilla_expediente_item(id) ON DELETE CASCADE,
    codigo VARCHAR(150) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    tipo VARCHAR(20) NOT NULL DEFAULT 'FOLDER',
    orden INTEGER DEFAULT 1,
    activo BOOLEAN DEFAULT TRUE,

    CONSTRAINT chk_plantilla_item_tipo CHECK (tipo IN ('FOLDER', 'FILE')),
    UNIQUE (plantilla_expediente_id, codigo),
    UNIQUE (plantilla_expediente_id, parent_item_id, nombre)
);

CREATE INDEX IF NOT EXISTS idx_plantilla_item_parent ON plantilla_expediente_item(parent_item_id);
CREATE INDEX IF NOT EXISTS idx_plantilla_item_template ON plantilla_expediente_item(plantilla_expediente_id);

-- Permite identificar la plantilla que dio origen al Node.
ALTER TABLE node
    ADD COLUMN IF NOT EXISTS plantilla_item_id INTEGER;

ALTER TABLE node
    ADD CONSTRAINT fk_node_plantilla_item
    FOREIGN KEY (plantilla_item_id)
    REFERENCES plantilla_expediente_item(id);

CREATE INDEX IF NOT EXISTS idx_node_plantilla_item ON node(plantilla_item_id);

-- ============================================================
-- 16. ACTUACIONES
-- ============================================================

CREATE TABLE IF NOT EXISTS tipo_actuacion (
    id SERIAL PRIMARY KEY,
    asunto_id INTEGER REFERENCES asunto(id),
    etapa_id INTEGER REFERENCES etapa_proceso(id),
    parent_id INTEGER REFERENCES tipo_actuacion(id),
    codigo VARCHAR(120) UNIQUE NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    genera_termino BOOLEAN DEFAULT FALSE,
    activo BOOLEAN DEFAULT TRUE,
    orden INTEGER DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_tipo_actuacion_asunto ON tipo_actuacion(asunto_id);
CREATE INDEX IF NOT EXISTS idx_tipo_actuacion_etapa ON tipo_actuacion(etapa_id);
CREATE INDEX IF NOT EXISTS idx_tipo_actuacion_parent ON tipo_actuacion(parent_id);

CREATE TABLE IF NOT EXISTS proceso_actuacion (
    id BIGSERIAL PRIMARY KEY,
    proceso_id INTEGER NOT NULL REFERENCES proceso(id) ON DELETE CASCADE,
    tipo_actuacion_id INTEGER NOT NULL REFERENCES tipo_actuacion(id),
    fecha DATE,
    observaciones TEXT,
    contexto TEXT,
    usuario_registro_id INTEGER REFERENCES usuario(id),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_proceso_actuacion_proceso ON proceso_actuacion(proceso_id);
CREATE INDEX IF NOT EXISTS idx_proceso_actuacion_tipo ON proceso_actuacion(tipo_actuacion_id);
CREATE INDEX IF NOT EXISTS idx_proceso_actuacion_fecha ON proceso_actuacion(fecha);

CREATE TABLE IF NOT EXISTS actuacion_documento (
    id BIGSERIAL PRIMARY KEY,
    proceso_actuacion_id BIGINT NOT NULL REFERENCES proceso_actuacion(id) ON DELETE CASCADE,
    node_id UUID NOT NULL REFERENCES node(id) ON DELETE RESTRICT,
    file_version_id UUID REFERENCES file_version(id) ON DELETE RESTRICT,
    principal BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE (proceso_actuacion_id, node_id)
);

CREATE INDEX IF NOT EXISTS idx_actuacion_documento_actuacion ON actuacion_documento(proceso_actuacion_id);
CREATE INDEX IF NOT EXISTS idx_actuacion_documento_node ON actuacion_documento(node_id);

-- ============================================================
-- 17. RECURSO EXTRAORDINARIO
-- ============================================================

CREATE TABLE IF NOT EXISTS recurso_extraordinario (
    id BIGSERIAL PRIMARY KEY,
    expediente_id INTEGER NOT NULL REFERENCES expediente(id) ON DELETE CASCADE,
    proceso_id INTEGER REFERENCES proceso(id) ON DELETE CASCADE,
    abogado_id INTEGER REFERENCES usuario(id),
    contexto TEXT,
    fecha_presentacion DATE,
    node_id UUID REFERENCES node(id) ON DELETE RESTRICT,
    file_version_id UUID REFERENCES file_version(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),

    -- Regla del negocio: máximo un recurso extraordinario por expediente.
    UNIQUE (expediente_id)
);

CREATE INDEX IF NOT EXISTS idx_recurso_extraordinario_proceso ON recurso_extraordinario(proceso_id);
CREATE INDEX IF NOT EXISTS idx_recurso_extraordinario_abogado ON recurso_extraordinario(abogado_id);

-- ============================================================
-- 18. REGLAS DE TÉRMINOS
-- ============================================================

CREATE TABLE IF NOT EXISTS regla_termino (
    id SERIAL PRIMARY KEY,
    tipo_procedimiento_id INTEGER NOT NULL REFERENCES tipo_procedimiento(id),
    tipo_actuacion_id INTEGER NOT NULL REFERENCES tipo_actuacion(id),
    dias INTEGER NOT NULL CHECK (dias >= 0),
    activo BOOLEAN DEFAULT TRUE,
    descripcion TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE (tipo_procedimiento_id, tipo_actuacion_id)
);

CREATE INDEX IF NOT EXISTS idx_regla_termino_procedimiento ON regla_termino(tipo_procedimiento_id);
CREATE INDEX IF NOT EXISTS idx_regla_termino_actuacion ON regla_termino(tipo_actuacion_id);

CREATE TABLE IF NOT EXISTS termino_proceso (
    id BIGSERIAL PRIMARY KEY,
    proceso_id INTEGER NOT NULL REFERENCES proceso(id) ON DELETE CASCADE,
    proceso_actuacion_id BIGINT NOT NULL REFERENCES proceso_actuacion(id) ON DELETE CASCADE,
    regla_termino_id INTEGER REFERENCES regla_termino(id),
    fecha_inicio DATE,
    dias INTEGER,
    fecha_vencimiento DATE,
    estado VARCHAR(30) DEFAULT 'PENDIENTE',
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),

    CONSTRAINT chk_termino_estado CHECK (
        estado IN ('PENDIENTE', 'CUMPLIDO', 'VENCIDO', 'CANCELADO')
    ),
    UNIQUE (proceso_actuacion_id)
);

CREATE INDEX IF NOT EXISTS idx_termino_proceso_proceso ON termino_proceso(proceso_id);
CREATE INDEX IF NOT EXISTS idx_termino_proceso_vencimiento ON termino_proceso(fecha_vencimiento);
CREATE INDEX IF NOT EXISTS idx_termino_proceso_estado ON termino_proceso(estado);

-- ============================================================
-- 19. CALENDARIO
-- ============================================================

CREATE TABLE IF NOT EXISTS tipo_evento (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(80) UNIQUE NOT NULL,
    color VARCHAR(7) DEFAULT '#3498db',
    activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS evento (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    tipo_evento_id INTEGER NOT NULL REFERENCES tipo_evento(id),
    fecha_inicio TIMESTAMPTZ NOT NULL,
    fecha_fin TIMESTAMPTZ,
    all_day BOOLEAN NOT NULL DEFAULT FALSE,
    proceso_id INTEGER REFERENCES proceso(id),
    expediente_id INTEGER REFERENCES expediente(id),
    termino_proceso_id BIGINT REFERENCES termino_proceso(id) ON DELETE SET NULL,
    responsable_id INTEGER REFERENCES usuario(id),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_evento_proceso ON evento(proceso_id) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_evento_expediente ON evento(expediente_id) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_evento_fecha ON evento(fecha_inicio) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_evento_termino ON evento(termino_proceso_id);

-- ============================================================
-- 20. DOCUMENTOS CONTABLES
-- ============================================================

CREATE TABLE IF NOT EXISTS documento_contable (
    id SERIAL PRIMARY KEY,
    numero_documento VARCHAR(100),
    nombre VARCHAR(255),
    descripcion TEXT,
    categoria_id INTEGER REFERENCES categoria(id),
    cliente_id INTEGER REFERENCES cliente(id),
    proceso_id INTEGER REFERENCES proceso(id),
    fecha_documento DATE,
    root_node_id UUID UNIQUE,
    created_by INTEGER REFERENCES usuario(id),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_doc_contable_categoria ON documento_contable(categoria_id);
CREATE INDEX IF NOT EXISTS idx_doc_contable_cliente ON documento_contable(cliente_id);
CREATE INDEX IF NOT EXISTS idx_doc_contable_proceso ON documento_contable(proceso_id);
CREATE INDEX IF NOT EXISTS idx_doc_contable_fecha ON documento_contable(fecha_documento);

ALTER TABLE node
    ADD CONSTRAINT fk_node_contable
    FOREIGN KEY (contable_id) REFERENCES documento_contable(id) ON DELETE CASCADE;

-- ============================================================
-- 21. SENTENCIAS
-- ============================================================

CREATE TABLE IF NOT EXISTS sentencia (
    id SERIAL PRIMARY KEY,
    numero_sentencia VARCHAR(100),
    nombre VARCHAR(255),
    descripcion TEXT,
    tipo_sentencia VARCHAR(50),
    fecha_sentencia DATE,
    fecha_notificacion DATE,
    juzgado VARCHAR(200),
    magistrado VARCHAR(200),
    proceso_id INTEGER REFERENCES proceso(id),
    expediente_id INTEGER REFERENCES expediente(id),
    cliente_id INTEGER REFERENCES cliente(id),
    abogado_id INTEGER REFERENCES usuario(id),
    estado VARCHAR(30) DEFAULT 'PRIMERA_INSTANCIA',
    es_favorable BOOLEAN,
    observaciones TEXT,
    file_blob_id UUID REFERENCES file_blob(id),
    created_by INTEGER REFERENCES usuario(id),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    is_deleted BOOLEAN DEFAULT FALSE,

    CONSTRAINT chk_sentencia_estado CHECK (
        estado IN (
            'PRIMERA_INSTANCIA',
            'SEGUNDA_INSTANCIA',
            'CASACION',
            'EJECUTORIADA',
            'APELADA',
            'CUMPLIDA'
        )
    )
);

CREATE INDEX IF NOT EXISTS idx_sentencia_proceso ON sentencia(proceso_id);
CREATE INDEX IF NOT EXISTS idx_sentencia_expediente ON sentencia(expediente_id);
CREATE INDEX IF NOT EXISTS idx_sentencia_cliente ON sentencia(cliente_id);
CREATE INDEX IF NOT EXISTS idx_sentencia_abogado ON sentencia(abogado_id);
CREATE INDEX IF NOT EXISTS idx_sentencia_fecha ON sentencia(fecha_sentencia);

-- ============================================================
-- 22. PERMISOS DE NODOS
-- ============================================================

CREATE TABLE IF NOT EXISTS node_permission (
    id SERIAL PRIMARY KEY,
    node_id UUID NOT NULL REFERENCES node(id) ON DELETE CASCADE,
    usuario_id INTEGER NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    permission VARCHAR(20) NOT NULL CHECK (permission IN ('read', 'write', 'admin')),
    granted_by INTEGER REFERENCES usuario(id),
    granted_at TIMESTAMPTZ DEFAULT now(),
    expires_at TIMESTAMPTZ,
    UNIQUE (node_id, usuario_id)
);

CREATE INDEX IF NOT EXISTS idx_node_permission_node ON node_permission(node_id);
CREATE INDEX IF NOT EXISTS idx_node_permission_usuario ON node_permission(usuario_id);

-- ============================================================
-- 23. AUDITORÍA
-- ============================================================

CREATE TABLE IF NOT EXISTS audit_log (
    id SERIAL PRIMARY KEY,
    table_name VARCHAR(100) NOT NULL,
    record_id VARCHAR(100) NOT NULL,
    action VARCHAR(20) NOT NULL CHECK (action IN ('INSERT', 'UPDATE', 'DELETE')),
    old_values JSONB,
    new_values JSONB,
    changed_by INTEGER REFERENCES usuario(id),
    changed_at TIMESTAMPTZ DEFAULT now(),
    ip_address INET,
    user_agent TEXT,
    modulo VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_audit_table_record ON audit_log(table_name, record_id);
CREATE INDEX IF NOT EXISTS idx_audit_changed_by ON audit_log(changed_by);
CREATE INDEX IF NOT EXISTS idx_audit_changed_at ON audit_log(changed_at);
CREATE INDEX IF NOT EXISTS idx_audit_modulo ON audit_log(modulo);

-- ============================================================
-- 24. TRIGGERS - updated_at
-- ============================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_usuario_updated_at ON usuario;
CREATE TRIGGER trg_usuario_updated_at
BEFORE UPDATE ON usuario
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_cliente_updated_at ON cliente;
CREATE TRIGGER trg_cliente_updated_at
BEFORE UPDATE ON cliente
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_proceso_updated_at ON proceso;
CREATE TRIGGER trg_proceso_updated_at
BEFORE UPDATE ON proceso
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_expediente_updated_at ON expediente;
CREATE TRIGGER trg_expediente_updated_at
BEFORE UPDATE ON expediente
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_node_updated_at ON node;
CREATE TRIGGER trg_node_updated_at
BEFORE UPDATE ON node
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_plantilla_updated_at ON plantilla;
CREATE TRIGGER trg_plantilla_updated_at
BEFORE UPDATE ON plantilla
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_proceso_penal_updated_at ON proceso_penal;
CREATE TRIGGER trg_proceso_penal_updated_at
BEFORE UPDATE ON proceso_penal
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_proceso_fiscal_updated_at ON proceso_fiscal;
CREATE TRIGGER trg_proceso_fiscal_updated_at
BEFORE UPDATE ON proceso_fiscal
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_proceso_juzgado_updated_at ON proceso_juzgado;
CREATE TRIGGER trg_proceso_juzgado_updated_at
BEFORE UPDATE ON proceso_juzgado
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_proceso_actuacion_updated_at ON proceso_actuacion;
CREATE TRIGGER trg_proceso_actuacion_updated_at
BEFORE UPDATE ON proceso_actuacion
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_recurso_extraordinario_updated_at ON recurso_extraordinario;
CREATE TRIGGER trg_recurso_extraordinario_updated_at
BEFORE UPDATE ON recurso_extraordinario
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_termino_proceso_updated_at ON termino_proceso;
CREATE TRIGGER trg_termino_proceso_updated_at
BEFORE UPDATE ON termino_proceso
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_evento_updated_at ON evento;
CREATE TRIGGER trg_evento_updated_at
BEFORE UPDATE ON evento
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- 25. TRIGGER - NODE CLOSURE TABLE
-- ============================================================

CREATE OR REPLACE FUNCTION maintain_node_closure()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO node_closure (ancestor_id, descendant_id, depth)
        VALUES (NEW.id, NEW.id, 0)
        ON CONFLICT DO NOTHING;

        IF NEW.parent_id IS NOT NULL THEN
            INSERT INTO node_closure (ancestor_id, descendant_id, depth)
            SELECT ancestor_id, NEW.id, depth + 1
            FROM node_closure
            WHERE descendant_id = NEW.parent_id
            ON CONFLICT DO NOTHING;
        END IF;

    ELSIF TG_OP = 'UPDATE' AND OLD.parent_id IS DISTINCT FROM NEW.parent_id THEN
        -- No permitir ciclos en el árbol.
        IF NEW.parent_id = NEW.id THEN
            RAISE EXCEPTION 'Un nodo no puede ser su propio padre';
        END IF;

        IF EXISTS (
            SELECT 1
            FROM node_closure
            WHERE ancestor_id = NEW.id
              AND descendant_id = NEW.parent_id
        ) THEN
            RAISE EXCEPTION 'Movimiento de nodo inválido: generaría un ciclo';
        END IF;

        DELETE FROM node_closure
        WHERE descendant_id = NEW.id
          AND ancestor_id <> NEW.id;

        IF NEW.parent_id IS NOT NULL THEN
            INSERT INTO node_closure (ancestor_id, descendant_id, depth)
            SELECT ancestor_id, NEW.id, depth + 1
            FROM node_closure
            WHERE descendant_id = NEW.parent_id
            ON CONFLICT DO NOTHING;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_maintain_node_closure ON node;
CREATE TRIGGER trg_maintain_node_closure
AFTER INSERT OR UPDATE OF parent_id ON node
FOR EACH ROW EXECUTE FUNCTION maintain_node_closure();

-- ============================================================
-- 26. TRIGGER - CREACIÓN DE ESTRUCTURA DEL EXPEDIENTE
-- ============================================================
-- El código Java podrá crear el expediente y luego llamar al servicio
-- de plantillas; la BD también permite automatizar la creación si se
-- desea. Por defecto el trigger se deja preparado pero NO duplica la
-- responsabilidad del servicio de Spring Boot.
--
-- Recomendación: Spring Boot debe controlar este proceso para tener
-- trazabilidad y manejo de errores más claro. La plantilla funciona
-- como dato maestro, no como lógica hardcodeada.
-- ============================================================

-- ============================================================
-- 27. DATOS MAESTROS - ROLES
-- ============================================================

INSERT INTO rol (nombre, descripcion) VALUES
('ADMIN', 'Administrador del sistema con todos los permisos'),
('ABOGADO', 'Abogado con acceso a gestión de casos y documentos'),
('SECRETARIO', 'Secretario con acceso limitado a documentos'),
('CONTADOR', 'Contador con acceso a gestión contable'),
('CLIENTE', 'Cliente con acceso de solo lectura a sus casos')
ON CONFLICT (nombre) DO NOTHING;

-- ============================================================
-- 28. DATOS MAESTROS - PERMISOS
-- ============================================================

INSERT INTO permiso (nombre, descripcion, modulo) VALUES
('MANAGE_USERS', 'Gestionar usuarios del sistema', 'USUARIOS'),
('MANAGE_PROCESOS', 'Gestionar procesos del sistema', 'DOCUMENTAL'),
('VIEW_PROCESO', 'Ver procesos', 'DOCUMENTAL'),
('MANAGE_EXPEDIENTES', 'Gestionar expedientes del sistema', 'DOCUMENTAL'),
('VIEW_EXPEDIENTE', 'Ver expedientes', 'DOCUMENTAL'),
('MANAGE_FILES', 'Gestionar archivos del sistema', 'DOCUMENTAL'),
('DOWNLOAD_FILE', 'Descargar archivos', 'DOCUMENTAL'),
('MANAGE_PLANTILLAS', 'Gestionar plantillas del sistema', 'PLANTILLAS'),
('VIEW_PLANTILLA', 'Ver plantillas', 'PLANTILLAS'),
('MANAGE_CONTABLES', 'Gestionar documentos contables', 'CONTABLE'),
('VIEW_DOC_CONTABLE', 'Ver documentos contables', 'CONTABLE'),
('MANAGE_SENTENCIAS', 'Gestionar sentencias', 'SENTENCIAS'),
('VIEW_SENTENCIA', 'Ver sentencias', 'SENTENCIAS'),
('MANAGE_CLIENTES', 'Gestionar clientes', 'CLIENTES'),
('VIEW_CLIENTE', 'Ver clientes', 'CLIENTES'),
('VIEW_AUDIT', 'Ver logs de auditoría', 'AUDITORIA')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r CROSS JOIN permiso p
WHERE r.nombre = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r CROSS JOIN permiso p
WHERE r.nombre = 'ABOGADO'
  AND p.nombre IN (
      'MANAGE_PROCESOS',
      'VIEW_PROCESO',
      'MANAGE_EXPEDIENTES',
      'VIEW_EXPEDIENTE',
      'MANAGE_FILES',
      'DOWNLOAD_FILE',
      'MANAGE_PLANTILLAS',
      'VIEW_PLANTILLA',
      'MANAGE_SENTENCIAS',
      'VIEW_SENTENCIA',
      'MANAGE_CLIENTES',
      'VIEW_CLIENTE'
  )
ON CONFLICT DO NOTHING;

-- ============================================================
-- 29. DATOS MAESTROS - CATEGORÍAS
-- ============================================================

INSERT INTO categoria (nombre, descripcion, tipo) VALUES
('Familia', 'Documentos de derecho de familia', 'PLANTILLA'),
('Civil', 'Documentos de derecho civil', 'PLANTILLA'),
('Penal', 'Documentos de derecho penal', 'PLANTILLA'),
('Laboral', 'Documentos de derecho laboral', 'PLANTILLA'),
('Comercial', 'Documentos de derecho comercial', 'PLANTILLA'),
('Administrativo', 'Documentos de derecho administrativo', 'PLANTILLA'),
('Declaración', 'Declaraciones tributarias', 'CONTABLE'),
('Balance', 'Balances contables', 'CONTABLE'),
('Factura', 'Facturas y documentos de venta', 'CONTABLE'),
('Comprobante', 'Comprobantes contables', 'CONTABLE'),
('Nómina', 'Documentos de nómina', 'CONTABLE'),
('Impuestos', 'Documentos tributarios', 'CONTABLE'),
('Civil', 'Procesos civiles', 'PROCESO'),
('Penal', 'Procesos penales', 'PROCESO'),
('Laboral', 'Procesos laborales', 'PROCESO'),
('Familia', 'Procesos de familia', 'PROCESO'),
('Comercial', 'Procesos comerciales', 'PROCESO'),
('Administrativo', 'Procesos administrativos', 'PROCESO')
ON CONFLICT (nombre, tipo) DO NOTHING;

-- ============================================================
-- 30. JURISDICCIONES
-- ============================================================

INSERT INTO jurisdiccion (codigo, nombre) VALUES
('ORDINARIA', 'JURISDICCION ORDINARIA'),
('CONTENCIOSA_ADMINISTRATIVA', 'JURISDICCION CONTENCIOSA ADMINISTRATIVA'),
('ESPECIALES_TRANSITORIA', 'JURISDICCION ESPECIALES Y TRANSITORIA')
ON CONFLICT (codigo) DO NOTHING;

-- ============================================================
-- 31. ASUNTOS
-- ============================================================

INSERT INTO asunto (jurisdiccion_id, codigo, nombre)
SELECT j.id, v.codigo, v.nombre
FROM jurisdiccion j
JOIN (
    VALUES
      ('ORDINARIA', 'PENALES', 'ASUNTOS PENALES'),
      ('ORDINARIA', 'CORPORATIVOS', 'ASUNTOS CORPORATIVOS'),
      ('ORDINARIA', 'CIVILES_COMERCIALES', 'ASUNTOS CIVILES/COMERCIALES'),
      ('CONTENCIOSA_ADMINISTRATIVA', 'REPARACION_DIRECTA', 'ASUNTOS DE REPARACION DIRECTA'),
      ('CONTENCIOSA_ADMINISTRATIVA', 'CONTRATACION_ESTATAL', 'ASUNTOS DE CONTRATACION ESTATAL'),
      ('CONTENCIOSA_ADMINISTRATIVA', 'DISCIPLINARIOS', 'ASUNTOS DISCIPLINARIOS'),
      ('ESPECIALES_TRANSITORIA', 'JUSTICIA_TRANSICIONAL', 'ASUNTOS DE LA JUSTICIA TRANSICIONAL'),
      ('ESPECIALES_TRANSITORIA', 'PENAL_MILITAR', 'ASUNTOS JURISDICCION PENAL MILITAR'),
      ('ESPECIALES_TRANSITORIA', 'ESPECIAL_INDIGENA', 'ASUNTOS JURISDICCION ESPECIAL INDIGENA')
) AS v(jurisdiccion_codigo, codigo, nombre)
  ON j.codigo = v.jurisdiccion_codigo
ON CONFLICT (codigo) DO NOTHING;

-- ============================================================
-- 32. ETAPAS
-- ============================================================

INSERT INTO etapa_proceso (codigo, nombre, orden) VALUES
('ETAPA_CONOCIMIENTO', 'ETAPA DE CONOCIMIENTO', 1),
('RECURSO_EXTRAORDINARIO', 'RECURSO EXTRAORDINARIO', 2),
('PENITENCIARIO_EJECUCION_PENA', 'PENITENCIARIO – EJECUCION DE LA PENA', 3)
ON CONFLICT (codigo) DO NOTHING;

-- ============================================================
-- 33. PROCEDIMIENTOS PENALES
-- ============================================================

INSERT INTO tipo_procedimiento (asunto_id, etapa_id, codigo, nombre)
SELECT a.id, e.id, v.codigo, v.nombre
FROM asunto a
JOIN etapa_proceso e ON e.codigo = 'ETAPA_CONOCIMIENTO'
JOIN (
    VALUES
      ('LEY_906_2004', 'LEY 906 DE 2004 - PROCEDIMIENTO PENAL ACUSATORIO'),
      ('LEY_1908_2018', 'LEY 1908 DE 2018 - PROCEDIMIENTO ESPECIAL GAO, GAOR Y GDO'),
      ('LEY_1826_2017', 'LEY 1826 DE 2017 - PROCEDIMIENTO ESPECIAL ABREVIADO'),
      ('LEY_1098_2006', 'LEY 1098 DE 2006 - PROCEDIMIENTO DE INFANCIA Y ADOLECENCIA'),
      ('REPRESENTACION_VICTIMAS', 'REPRESENTACION DE VICTIMAS')
) AS v(codigo, nombre)
  ON a.codigo = 'PENALES'
ON CONFLICT (codigo) DO NOTHING;

-- ============================================================
-- 34. ACTUACIONES PENALES
-- ============================================================

INSERT INTO tipo_actuacion (asunto_id, etapa_id, codigo, nombre, genera_termino, orden)
SELECT a.id, e.id, v.codigo, v.nombre, v.genera_termino, v.orden
FROM asunto a
JOIN etapa_proceso e ON e.codigo = 'ETAPA_CONOCIMIENTO'
JOIN (
    VALUES
      ('LEGALIZACION_CAPTURA', 'LEGALIZACION DE CAPTURA', FALSE, 1),
      ('FORMULACION_IMPUTACION', 'FORMULACION DE IMPUTACION', TRUE, 2),
      ('MEDIDA_ASEGURAMIENTO', 'MEDIDA DE ASEGURAMIENTO', FALSE, 3),
      ('FORMULACION_ACUSACION', 'FORMULACION DE ACUSACION', FALSE, 4),
      ('ACUSACION_FORMAL', 'ACUSACION FORMAL', TRUE, 5),
      ('PREACUERDO', 'PREACUERDO', FALSE, 6),
      ('DESCUBRIMIENTO_PROBATORIO', 'DESCUBRIMIENTO PROBATORIO', FALSE, 7),
      ('AUDIENCIA_PREPARATORIA', 'AUDIENCIA PREPARATORIA', FALSE, 8),
      ('INICIO_JUICIO_ORAL', 'INICIO DE JUICIO ORAL', TRUE, 9)
) AS v(codigo, nombre, genera_termino, orden)
  ON a.codigo = 'PENALES'
ON CONFLICT (codigo) DO NOTHING;

-- Actuaciones de ejecución de pena
INSERT INTO tipo_actuacion (etapa_id, codigo, nombre, genera_termino, orden)
SELECT e.id, v.codigo, v.nombre, FALSE, v.orden
FROM etapa_proceso e
JOIN (
    VALUES
      ('CONSTANCIA_VISITA_PENITENCIARIA', 'CONSTANCIA DE VISITA PENITENCIARIA', 1),
      ('SOLICITUD_SALUD_PPL', 'SOLICITUDES DE SALUD DE PPL', 2),
      ('MECANISMO_LIBERTAD', 'MECANISMOS DE LIBERTAD', 3),
      ('REDENCION_PENAS_FAVORABILIDAD', 'REDENCION DE PENAS Y APLICACIÓN DE FAVORABILIDAD', 4),
      ('SOLICITUD_TRASLADO', 'SOLICITUD DE TRASLADOS DE ESTABLECIMIENTOS PENITENCIARIOS', 5)
) AS v(codigo, nombre, orden)
  ON e.codigo = 'PENITENCIARIO_EJECUCION_PENA'
ON CONFLICT (codigo) DO NOTHING;

-- ============================================================
-- 35. TIPOS DE EVENTO
-- ============================================================

INSERT INTO tipo_evento (nombre, color) VALUES
('Audiencia', '#e74c3c'),
('Reunión Cliente', '#3498db'),
('Vencimiento', '#f39c12'),
('Recordatorio', '#2ecc71'),
('Entrega Documentos', '#9b59b6')
ON CONFLICT (nombre) DO NOTHING;

-- ============================================================
-- 36. REGLAS DE TÉRMINOS PENALES
-- ============================================================

INSERT INTO regla_termino (tipo_procedimiento_id, tipo_actuacion_id, dias, descripcion)
SELECT tp.id, ta.id, v.dias, v.descripcion
FROM tipo_procedimiento tp
JOIN (
    VALUES
      ('LEY_906_2004', 'FORMULACION_IMPUTACION', 60, 'Ley 906 de 2004 - imputación'),
      ('LEY_906_2004', 'ACUSACION_FORMAL', 120, 'Ley 906 de 2004 - acusación'),
      ('LEY_906_2004', 'INICIO_JUICIO_ORAL', 150, 'Ley 906 de 2004 - juicio oral'),
      ('LEY_1826_2017', 'ACUSACION_FORMAL', 70, 'Ley 1826 de 2017 - acusación'),
      ('LEY_1826_2017', 'INICIO_JUICIO_ORAL', 75, 'Ley 1826 de 2017 - juicio oral'),
      ('LEY_1908_2018', 'FORMULACION_IMPUTACION', 400, 'Ley 1908 de 2018 - imputación'),
      ('LEY_1908_2018', 'ACUSACION_FORMAL', 500, 'Ley 1908 de 2018 - acusación'),
      ('LEY_1908_2018', 'INICIO_JUICIO_ORAL', 500, 'Ley 1908 de 2018 - juicio oral')
) AS v(codigo_procedimiento, codigo_actuacion, dias, descripcion)
  ON TRUE
JOIN tipo_actuacion ta ON ta.codigo = v.codigo_actuacion
  AND ta.asunto_id = (SELECT a2.id FROM asunto a2 WHERE a2.codigo = 'PENALES' LIMIT 1)
ON CONFLICT (tipo_procedimiento_id, tipo_actuacion_id) DO UPDATE
SET dias = EXCLUDED.dias,
    descripcion = EXCLUDED.descripcion,
    updated_at = now();

-- ============================================================
-- 37. PLANTILLA PENAL - ESTRUCTURA DEL EXPEDIENTE
-- ============================================================

INSERT INTO plantilla_expediente (asunto_id, etapa_id, nombre, descripcion)
SELECT a.id, e.id, 'PLANTILLA EXPEDIENTE PENAL', 'Estructura documental inicial para asuntos penales'
FROM asunto a
JOIN etapa_proceso e ON e.codigo = 'ETAPA_CONOCIMIENTO'
WHERE a.codigo = 'PENALES'
ON CONFLICT (asunto_id, etapa_id, nombre) DO NOTHING;

-- Items raíz
INSERT INTO plantilla_expediente_item
(plantilla_expediente_id, parent_item_id, codigo, nombre, tipo, orden)
SELECT pe.id, NULL, v.codigo, v.nombre, 'FOLDER', v.orden
FROM plantilla_expediente pe
JOIN (
    VALUES
      ('LEGALIZACION_CAPTURA', 'LEGALIZACION DE CAPTURA', 1),
      ('FORMULACION_IMPUTACION', 'FORMULACION DE IMPUTACION', 2),
      ('MEDIDA_ASEGURAMIENTO', 'MEDIDA DE ASEGURAMIENTO', 3),
      ('REVOCATORIA_SUSTITUCION_MEDIDA', 'REVOCATORIA O SUSTITUCION DE LA MEDIDA DE ASEGURAMIENTO', 4),
      ('ACTOS_INVESTIGACION_POSTERIOR_IMPUTACION', 'ACTOS DE INVESTIGACION ADELANTADOS POSTERIOR A LA IMPUTACION', 5),
      ('FORMULACION_ACUSACION', 'FORMULACION DE ACUSACION', 6),
      ('DESCUBRIMIENTO_PROBATORIO', 'DESCUBRIMIENTO PROBATORIO', 7),
      ('AUDIENCIA_PREPARATORIA', 'AUDIENCIA PREPARATORIA', 8),
      ('JUICIO_ORAL', 'JUICIO ORAL', 9)
) AS v(codigo, nombre, orden)
  ON pe.nombre = 'PLANTILLA EXPEDIENTE PENAL'
ON CONFLICT (plantilla_expediente_id, codigo) DO NOTHING;

-- Subcarpetas de LEGALIZACION DE CAPTURA
INSERT INTO plantilla_expediente_item
(plantilla_expediente_id, parent_item_id, codigo, nombre, tipo, orden)
SELECT pe.id, parent.id, v.codigo, v.nombre, 'FOLDER', v.orden
FROM plantilla_expediente pe
JOIN plantilla_expediente_item parent
  ON parent.plantilla_expediente_id = pe.id
 AND parent.codigo = 'LEGALIZACION_CAPTURA'
JOIN (
    VALUES
      ('INFORMES', 'Informes', 1),
      ('INDIVIDUALIZACION_VICTIMARIO_DERECHOS', 'Individualización del victimario y sus derechos', 2),
      ('INCAUTACION', 'Incautación', 3)
) AS v(codigo, nombre, orden)
  ON TRUE
ON CONFLICT (plantilla_expediente_id, codigo) DO NOTHING;

-- Subcarpeta de MEDIDA DE ASEGURAMIENTO
INSERT INTO plantilla_expediente_item
(plantilla_expediente_id, parent_item_id, codigo, nombre, tipo, orden)
SELECT pe.id, parent.id, 'ELEMENTOS_MATERIALES_PROBATORIOS', 'Elementos Materiales Probatorios', 'FOLDER', 1
FROM plantilla_expediente pe
JOIN plantilla_expediente_item parent
  ON parent.plantilla_expediente_id = pe.id
 AND parent.codigo = 'MEDIDA_ASEGURAMIENTO'
WHERE NOT EXISTS (
    SELECT 1 FROM plantilla_expediente_item x
    WHERE x.plantilla_expediente_id = pe.id
      AND x.codigo = 'ELEMENTOS_MATERIALES_PROBATORIOS'
);

-- Subcarpetas de REVOCATORIA/SUSTITUCION
INSERT INTO plantilla_expediente_item
(plantilla_expediente_id, parent_item_id, codigo, nombre, tipo, orden)
SELECT pe.id, parent.id, v.codigo, v.nombre, 'FOLDER', v.orden
FROM plantilla_expediente pe
JOIN plantilla_expediente_item parent
  ON parent.plantilla_expediente_id = pe.id
 AND parent.codigo = 'REVOCATORIA_SUSTITUCION_MEDIDA'
JOIN (
    VALUES
      ('REVOCATORIA_MEDIDA', 'Revocatoria de la medida de aseguramiento', 1),
      ('SUSTITUCION_MEDIDA', 'Sustitución de la medida de aseguramiento', 2)
) AS v(codigo, nombre, orden)
  ON TRUE
ON CONFLICT (plantilla_expediente_id, codigo) DO NOTHING;

-- Subcarpetas de FORMULACION DE ACUSACION
INSERT INTO plantilla_expediente_item
(plantilla_expediente_id, parent_item_id, codigo, nombre, tipo, orden)
SELECT pe.id, parent.id, v.codigo, v.nombre, 'FOLDER', v.orden
FROM plantilla_expediente pe
JOIN plantilla_expediente_item parent
  ON parent.plantilla_expediente_id = pe.id
 AND parent.codigo = 'FORMULACION_ACUSACION'
JOIN (
    VALUES
      ('ACUSACION_FORMAL', 'Acusación Formal', 1),
      ('APRUEBA_VERIFICACION_PREACUERDO', 'Aprueba y verificación de PreAcuerdo', 2),
      ('IMPRUEBA_PREACUERDO', 'Imprueba de PreAcuerdo', 3)
) AS v(codigo, nombre, orden)
  ON TRUE
ON CONFLICT (plantilla_expediente_id, codigo) DO NOTHING;

-- Subcarpetas de DESCUBRIMIENTO PROBATORIO
INSERT INTO plantilla_expediente_item
(plantilla_expediente_id, parent_item_id, codigo, nombre, tipo, orden)
SELECT pe.id, parent.id, v.codigo, v.nombre, 'FOLDER', v.orden
FROM plantilla_expediente pe
JOIN plantilla_expediente_item parent
  ON parent.plantilla_expediente_id = pe.id
 AND parent.codigo = 'DESCUBRIMIENTO_PROBATORIO'
JOIN (
    VALUES
      ('DOCUMENTALES', 'Documentales', 1),
      ('TESTIMONIALES', 'Testimoniales', 2),
      ('PERICIALES', 'Periciales', 3)
) AS v(codigo, nombre, orden)
  ON TRUE
ON CONFLICT (plantilla_expediente_id, codigo) DO NOTHING;

-- ============================================================
-- 38. VALIDACIONES TÉCNICAS ÚTILES
-- ============================================================

CREATE OR REPLACE FUNCTION validate_file_version_current()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.is_current = TRUE THEN
        UPDATE file_version
        SET is_current = FALSE
        WHERE node_id = NEW.node_id
          AND id <> NEW.id
          AND is_current = TRUE;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_validate_file_version_current ON file_version;
CREATE TRIGGER trg_validate_file_version_current
BEFORE INSERT OR UPDATE ON file_version
FOR EACH ROW EXECUTE FUNCTION validate_file_version_current();

-- ============================================================
-- 39. FK EXPEDIENTE -> NODE RAÍZ
-- ============================================================

ALTER TABLE expediente
    ADD CONSTRAINT fk_expediente_root_node
    FOREIGN KEY (root_node_id) REFERENCES node(id);

COMMIT;

-- ============================================================
-- FIN DEL MODELO
-- ============================================================
