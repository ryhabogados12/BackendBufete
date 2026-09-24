BEGIN;

-- ====================================
-- EXTENSIONES REQUERIDAS POR EL ESQUEMA
-- ====================================
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ====================================
-- ROLES Y USUARIOS
-- ====================================

CREATE TABLE IF NOT EXISTS rol (
  id     SERIAL  PRIMARY KEY,
  nombre VARCHAR(100) UNIQUE NOT NULL,
  descripcion TEXT,
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS permiso (
  id          SERIAL  PRIMARY KEY,
  nombre      VARCHAR(100) UNIQUE NOT NULL,
  descripcion TEXT,
  modulo      VARCHAR(50) NOT NULL, -- PLANTILLAS, DOCUMENTAL, CONTABLE, SENTENCIAS, USUARIOS, etc.
  created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS rol_permiso (
  rol_id     INTEGER NOT NULL REFERENCES rol(id) ON DELETE CASCADE,
  permiso_id INTEGER NOT NULL REFERENCES permiso(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ DEFAULT now(),
  PRIMARY KEY (rol_id, permiso_id)
);

CREATE TABLE IF NOT EXISTS usuario (
  id       SERIAL  PRIMARY KEY,
  nombre   VARCHAR(100) NOT NULL,
  apellido VARCHAR(100),
  email    VARCHAR(200) UNIQUE NOT NULL,
  identificacion VARCHAR(100) UNIQUE,
  contraseña VARCHAR(255) NOT NULL,
  telefono VARCHAR(20),
  direccion TEXT,
  rol_id   INTEGER NOT NULL REFERENCES rol(id),
  activo   BOOLEAN DEFAULT TRUE,
  nuevo_usuario BOOLEAN  DEFAULT TRUE,
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now(),
  last_login TIMESTAMPTZ
);

-- ====================================
-- CLIENTES
-- ====================================

CREATE TABLE IF NOT EXISTS cliente (
  id              SERIAL PRIMARY KEY,
  tipo_cliente    VARCHAR(20) CHECK (tipo_cliente IN ('NATURAL', 'JURIDICA')),
  nombre          VARCHAR(200) ,
  apellido        VARCHAR(200), -- Para personas naturales
  razon_social    VARCHAR(300), -- Para personas jurídicas
  identificacion  VARCHAR(100) UNIQUE,
  tipo_documento  VARCHAR(20) , -- CC, NIT, CE, PP, etc.
  email           VARCHAR(200),
  telefono        VARCHAR(20),
  direccion       TEXT,
  ciudad          VARCHAR(100),
  departamento    VARCHAR(100),
  pais            VARCHAR(100) DEFAULT 'Colombia',
  fecha_nacimiento DATE, -- Para personas naturales
  representante_legal VARCHAR(200), -- Para personas jurídicas
  activo          BOOLEAN  DEFAULT TRUE,
  observaciones   TEXT,
  created_by      INTEGER NOT NULL REFERENCES usuario(id),
  created_at      TIMESTAMPTZ DEFAULT now(),
  updated_at      TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_cliente_identificacion ON cliente(identificacion);
CREATE INDEX IF NOT EXISTS idx_cliente_nombre ON cliente(nombre);
CREATE INDEX IF NOT EXISTS idx_cliente_activo ON cliente(activo);

-- ====================================
-- CATEGORÍAS GENERALES
-- ====================================

CREATE TABLE IF NOT EXISTS categoria (
  id          SERIAL PRIMARY KEY,
  nombre      VARCHAR(100) NOT NULL,
  descripcion TEXT,
  tipo        VARCHAR(20) NOT NULL, -- PLANTILLA, CONTABLE, PROCESO
  activo      BOOLEAN NOT NULL DEFAULT TRUE,
  created_at  TIMESTAMPTZ  DEFAULT now(),
  updated_at  TIMESTAMPTZ  DEFAULT now(),
  
  UNIQUE(nombre, tipo)
);

-- ====================================
-- PROCESOS Y EXPEDIENTES
-- ====================================

CREATE TABLE IF NOT EXISTS proceso (
  id              SERIAL  PRIMARY KEY,
  numero_proceso  VARCHAR(100) UNIQUE, -- Número único del proceso
  nombre          VARCHAR(255),
  descripcion     TEXT,
  tipo_proceso    VARCHAR(100), -- Civil, Penal, Laboral, etc.
  estado          VARCHAR(50) NOT NULL DEFAULT 'ACTIVO',
  fecha_creacion  TIMESTAMPTZ NOT NULL DEFAULT now(),
  fecha_inicio    DATE,
  fecha_cierre DATE,
  cliente_id      INTEGER REFERENCES cliente(id),
  abogado_responsable_id INTEGER REFERENCES usuario(id),
  juzgado         VARCHAR(200),
  radicado        VARCHAR(100),
  demandante      VARCHAR(300),
  demandado       VARCHAR(300),
  cuantia         DECIMAL(15,2),
  observaciones   TEXT,
  activo          BOOLEAN DEFAULT TRUE,
  created_by      INTEGER NOT NULL REFERENCES usuario(id),
  created_at      TIMESTAMPTZ DEFAULT now(),
  updated_at      TIMESTAMPTZ DEFAULT now(),
  
  CONSTRAINT chk_proceso_estado CHECK (estado IN ('ACTIVO', 'SUSPENDIDO', 'CERRADO', 'ARCHIVADO'))
);

CREATE INDEX IF NOT EXISTS idx_proceso_cliente ON proceso(cliente_id);
CREATE INDEX IF NOT EXISTS idx_proceso_abogado ON proceso(abogado_responsable_id);
CREATE INDEX IF NOT EXISTS idx_proceso_estado ON proceso(estado);
CREATE INDEX IF NOT EXISTS idx_proceso_numero ON proceso(numero_proceso);

CREATE TABLE IF NOT EXISTS expediente (
  id              SERIAL  PRIMARY KEY,
  nombre          VARCHAR(255),
  descripcion     TEXT,
  estado          VARCHAR(50) DEFAULT 'ACTIVO',
  fecha_creacion  TIMESTAMPTZ NOT NULL DEFAULT now(),
  fecha_cierre    TIMESTAMPTZ,
  proceso_id      INTEGER REFERENCES proceso(id) ON DELETE CASCADE,
  root_node_id    UUID UNIQUE, -- Carpeta raíz asociada
  orden           INTEGER DEFAULT 1, -- Para ordenar expedientes dentro del proceso
  created_by      INTEGER NOT NULL REFERENCES usuario(id),
  updated_at      TIMESTAMPTZ DEFAULT now(),
  is_deleted      BOOLEAN DEFAULT FALSE,
  
  CONSTRAINT chk_expediente_estado CHECK (estado IN ('ACTIVO', 'ARCHIVADO', 'CERRADO'))
);

-- Índices para optimizar consultas
CREATE INDEX IF NOT EXISTS idx_expediente_proceso ON expediente(proceso_id) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_expediente_estado ON expediente(estado) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_expediente_created_by ON expediente(created_by);

-- ====================================
-- EVENTOS Y PLANTILLAS
-- ====================================

CREATE TABLE IF NOT EXISTS tipo_evento (
  id     SERIAL  PRIMARY KEY,
  nombre VARCHAR(50) UNIQUE NOT NULL,
  color  VARCHAR(7) DEFAULT '#3498db',
  activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS evento (
  id              SERIAL  PRIMARY KEY,
  titulo          VARCHAR(255) NOT NULL,
  descripcion     TEXT,
  tipo_evento_id  INTEGER NOT NULL REFERENCES tipo_evento(id),
  fecha_inicio    TIMESTAMPTZ NOT NULL,
  fecha_fin       TIMESTAMPTZ,
  all_day         BOOLEAN NOT NULL DEFAULT FALSE,
  proceso_id      INTEGER REFERENCES proceso(id), -- Opcional
  expediente_id   INTEGER REFERENCES expediente(id), -- Opcional
  responsable_id  INTEGER NOT NULL REFERENCES usuario(id),
  created_at      TIMESTAMPTZ DEFAULT now(),
  updated_at      TIMESTAMPTZ DEFAULT now(),
  is_deleted      BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_evento_proceso ON evento(proceso_id) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_evento_fecha ON evento(fecha_inicio) WHERE is_deleted = false;

-- ====================================
-- SISTEMA DE ARCHIVOS MEJORADO
-- ====================================

CREATE TABLE IF NOT EXISTS node (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  expediente_id  INTEGER REFERENCES expediente(id) ON DELETE CASCADE, -- Puede ser NULL para otros módulos
  contable_id    INTEGER, -- Para documentos contables (se definirá más adelante)
  parent_id      UUID REFERENCES node(id) ON DELETE CASCADE,
  type           VARCHAR(10) NOT NULL CHECK (type IN ('FOLDER','FILE')),
  name           VARCHAR(255) NOT NULL,
  description    TEXT,
  modulo         VARCHAR(20) NOT NULL DEFAULT 'DOCUMENTAL', -- DOCUMENTAL, CONTABLE, PLANTILLAS
  created_by     INTEGER NOT NULL REFERENCES usuario(id),
  created_at     TIMESTAMPTZ DEFAULT now(),
  updated_at     TIMESTAMPTZ DEFAULT now(),
  is_deleted     BOOLEAN DEFAULT FALSE,
  current_version_id UUID,
  
  -- Metadatos adicionales
  size_bytes     BIGINT DEFAULT 0,
  item_count     INTEGER DEFAULT 0,
  last_accessed  TIMESTAMPTZ 
);

CREATE INDEX IF NOT EXISTS idx_node_parent ON node(parent_id) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_node_expediente ON node(expediente_id) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_node_modulo ON node(modulo) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_node_type ON node(type) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_node_name ON node(name) WHERE is_deleted = false;

-- Árbol jerárquico (closure table)
CREATE TABLE IF NOT EXISTS node_closure (
  ancestor_id   UUID NOT NULL REFERENCES node(id) ON DELETE CASCADE,
  descendant_id UUID NOT NULL REFERENCES node(id) ON DELETE CASCADE,
  depth         INTEGER NOT NULL CHECK (depth >= 0),
  PRIMARY KEY (ancestor_id, descendant_id)
);

CREATE INDEX IF NOT EXISTS idx_closure_ancestor ON node_closure(ancestor_id, depth);
CREATE INDEX IF NOT EXISTS idx_closure_descendant ON node_closure(descendant_id, depth);

-- ====================================
-- ARCHIVOS Y VERSIONES MEJORADO
-- ====================================

CREATE TABLE IF NOT EXISTS file_blob (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  storage_key     VARCHAR(500) NOT NULL,
  bucket_name     VARCHAR(100) NOT NULL,
  size_bytes      BIGINT NOT NULL,
  checksum_sha256 VARCHAR(64) NOT NULL,
  mime_type       VARCHAR(100) NOT NULL,
  original_name   VARCHAR(255),
  created_at      TIMESTAMPTZ DEFAULT now(),
  
  -- Metadatos para optimización
  is_image        BOOLEAN DEFAULT FALSE,
  thumbnail_key   VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_blob_storage_key ON file_blob(storage_key);
CREATE INDEX IF NOT EXISTS idx_blob_mime_type ON file_blob(mime_type);

CREATE TABLE IF NOT EXISTS file_version (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  node_id       UUID NOT NULL REFERENCES node(id) ON DELETE CASCADE,
  file_blob_id  UUID NOT NULL REFERENCES file_blob(id) ON DELETE RESTRICT,
  version_num   INTEGER NOT NULL,
  uploaded_by   INTEGER NOT NULL REFERENCES usuario(id),
  uploaded_at   TIMESTAMPTZ  DEFAULT now(),
  note          TEXT,
  is_current    BOOLEAN DEFAULT TRUE,
  
  UNIQUE (node_id, version_num)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_one_current_version 
ON file_version(node_id) WHERE is_current = true;

CREATE INDEX IF NOT EXISTS idx_version_node ON file_version(node_id);

-- Importante para Spring Boot/JPA:
-- file_version.file_blob_id NO es UNIQUE. Un mismo file_blob puede estar referenciado
-- desde múltiples file_version, incluso en carpetas diferentes.
CREATE INDEX IF NOT EXISTS idx_version_file_blob ON file_version(file_blob_id);
CREATE INDEX IF NOT EXISTS idx_version_uploaded_by ON file_version(uploaded_by);

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'fk_node_current_version'
      AND conrelid = 'node'::regclass
  ) THEN
    ALTER TABLE node
      ADD CONSTRAINT fk_node_current_version
      FOREIGN KEY (current_version_id) REFERENCES file_version(id);
  END IF;
END $$;

-- ====================================
-- GESTIÓN DE PLANTILLAS
-- ====================================

CREATE TABLE IF NOT EXISTS plantilla (
  id              SERIAL  PRIMARY KEY,
  nombre          VARCHAR(255) NOT NULL,
  categoria_id    INTEGER NOT NULL REFERENCES categoria(id),
  descripcion     TEXT,
  file_blob_id    UUID NOT NULL REFERENCES file_blob(id), -- Referencia al archivo
  fecha_creacion  TIMESTAMPTZ DEFAULT now(),
  responsable_id  INTEGER NOT NULL REFERENCES usuario(id),
  activo          BOOLEAN DEFAULT TRUE,
  version         VARCHAR(10) DEFAULT '1.0',
  updated_at      TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_plantilla_categoria ON plantilla(categoria_id);
CREATE INDEX IF NOT EXISTS idx_plantilla_activo ON plantilla(activo);


-- ====================================
-- GESTIÓN CONTABLE
-- ====================================

CREATE TABLE IF NOT EXISTS documento_contable (
  id              SERIAL PRIMARY KEY,
  numero_documento VARCHAR(100),
  nombre          VARCHAR(255) NOT NULL,
  descripcion     TEXT,
  categoria_id    INTEGER NOT NULL REFERENCES categoria(id), -- Categoría contable
  cliente_id      INTEGER REFERENCES cliente(id), -- Opcional, puede ser documento general
  proceso_id      INTEGER REFERENCES proceso(id), -- Opcional, puede estar asociado a un proceso
  fecha_documento DATE ,
  root_node_id    UUID UNIQUE, -- Carpeta raíz para archivos de este documento
  created_by      INTEGER NOT NULL REFERENCES usuario(id),
  created_at      TIMESTAMPTZ DEFAULT now(),
  updated_at      TIMESTAMPTZ DEFAULT now(),
  is_deleted      BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_doc_contable_categoria ON documento_contable(categoria_id);
CREATE INDEX IF NOT EXISTS idx_doc_contable_cliente ON documento_contable(cliente_id);
CREATE INDEX IF NOT EXISTS idx_doc_contable_fecha ON documento_contable(fecha_documento);

-- Agregar referencia en node para documentos contables
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'fk_node_contable'
      AND conrelid = 'node'::regclass
  ) THEN
    ALTER TABLE node
      ADD CONSTRAINT fk_node_contable
      FOREIGN KEY (contable_id) REFERENCES documento_contable(id) ON DELETE CASCADE;
  END IF;
END $$;

-- ====================================
-- GESTIÓN DE SENTENCIAS
-- ====================================

CREATE TABLE IF NOT EXISTS sentencia (
  id              SERIAL PRIMARY KEY,
  numero_sentencia VARCHAR(100),
  nombre          VARCHAR(255) NOT NULL,
  descripcion     TEXT,
  tipo_sentencia  VARCHAR(50) NOT NULL, -- Absolutoria, Condenatoria, etc.
  fecha_sentencia DATE NOT NULL,
  fecha_notificacion DATE,
  juzgado         VARCHAR(200),
  magistrado      VARCHAR(200),
  proceso_id      INTEGER NOT NULL REFERENCES proceso(id),
  expediente_id   INTEGER REFERENCES expediente(id),
  cliente_id      INTEGER REFERENCES cliente(id),
  abogado_id      INTEGER NOT NULL REFERENCES usuario(id),
  estado          VARCHAR(30) DEFAULT 'PRIMERA_INSTANCIA',
  es_favorable    BOOLEAN,
  observaciones   TEXT,
  file_blob_id    UUID NOT NULL REFERENCES file_blob(id), -- Archivo PDF de la sentencia
  created_by      INTEGER NOT NULL REFERENCES usuario(id),
  created_at      TIMESTAMPTZ DEFAULT now(),
  updated_at      TIMESTAMPTZ DEFAULT now(),
  is_deleted      BOOLEAN DEFAULT FALSE,
  
  CONSTRAINT chk_sentencia_estado CHECK (estado IN (
    'PRIMERA_INSTANCIA', 'SEGUNDA_INSTANCIA', 'CASACION', 
    'EJECUTORIADA', 'APELADA', 'CUMPLIDA'
  ))
);

CREATE INDEX IF NOT EXISTS idx_sentencia_proceso ON sentencia(proceso_id);
CREATE INDEX IF NOT EXISTS idx_sentencia_cliente ON sentencia(cliente_id);
CREATE INDEX IF NOT EXISTS idx_sentencia_abogado ON sentencia(abogado_id);
CREATE INDEX IF NOT EXISTS idx_sentencia_fecha ON sentencia(fecha_sentencia);

-- ====================================
-- PERMISOS DE ARCHIVOS
-- ====================================

CREATE TABLE IF NOT EXISTS node_permission (
  id            SERIAL PRIMARY KEY,
  node_id       UUID NOT NULL REFERENCES node(id) ON DELETE CASCADE,
  usuario_id    INTEGER NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
  permission    VARCHAR(20) NOT NULL CHECK (permission IN ('read', 'write', 'admin')),
  granted_by    INTEGER NOT NULL REFERENCES usuario(id),
  granted_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  expires_at    TIMESTAMPTZ,
  
  UNIQUE (node_id, usuario_id)
);

-- ====================================
-- AUDITORÍA
-- ====================================

CREATE TABLE IF NOT EXISTS audit_log (
  id            SERIAL PRIMARY KEY,
  table_name    VARCHAR(50) NOT NULL,
  record_id     VARCHAR(100) NOT NULL,
  action        VARCHAR(20) NOT NULL CHECK (action IN ('INSERT', 'UPDATE', 'DELETE')),
  old_values    JSONB,
  new_values    JSONB,
  changed_by    INTEGER NOT NULL REFERENCES usuario(id),
  changed_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  ip_address    INET,
  user_agent    TEXT,
  modulo        VARCHAR(20) -- DOCUMENTAL, CONTABLE, PLANTILLAS, etc.
);

CREATE INDEX IF NOT EXISTS idx_audit_table_record ON audit_log(table_name, record_id);
CREATE INDEX IF NOT EXISTS idx_audit_changed_by ON audit_log(changed_by);
CREATE INDEX IF NOT EXISTS idx_audit_changed_at ON audit_log(changed_at);
CREATE INDEX IF NOT EXISTS idx_audit_modulo ON audit_log(modulo);

-- ====================================
-- TRIGGERS
-- ====================================

-- Función para actualizar timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers para updated_at
DROP TRIGGER IF EXISTS trg_usuario_updated_at ON usuario;
CREATE TRIGGER trg_usuario_updated_at BEFORE UPDATE ON usuario
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    
DROP TRIGGER IF EXISTS trg_cliente_updated_at ON cliente;
CREATE TRIGGER trg_cliente_updated_at BEFORE UPDATE ON cliente
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    
DROP TRIGGER IF EXISTS trg_proceso_updated_at ON proceso;
CREATE TRIGGER trg_proceso_updated_at BEFORE UPDATE ON proceso
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    
DROP TRIGGER IF EXISTS trg_expediente_updated_at ON expediente;
CREATE TRIGGER trg_expediente_updated_at BEFORE UPDATE ON expediente
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

    
DROP TRIGGER IF EXISTS trg_documento_contable_updated_at ON documento_contable;
CREATE TRIGGER trg_documento_contable_updated_at BEFORE UPDATE ON documento_contable
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    
DROP TRIGGER IF EXISTS trg_sentencia_updated_at ON sentencia;
CREATE TRIGGER trg_sentencia_updated_at BEFORE UPDATE ON sentencia
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();


-- Trigger para mantener closure table
CREATE OR REPLACE FUNCTION maintain_node_closure()
RETURNS TRIGGER AS $$
BEGIN
  IF TG_OP = 'INSERT' THEN
    -- Insertar relación consigo mismo
    INSERT INTO node_closure (ancestor_id, descendant_id, depth)
    VALUES (NEW.id, NEW.id, 0);
    
    -- Insertar relaciones con ancestros
    IF NEW.parent_id IS NOT NULL THEN
      INSERT INTO node_closure (ancestor_id, descendant_id, depth)
      SELECT ancestor_id, NEW.id, depth + 1
      FROM node_closure
      WHERE descendant_id = NEW.parent_id;
    END IF;
    
  ELSIF TG_OP = 'UPDATE' AND OLD.parent_id IS DISTINCT FROM NEW.parent_id THEN
    -- Eliminar relaciones existentes excepto consigo mismo
    DELETE FROM node_closure 
    WHERE descendant_id = NEW.id AND depth > 0;
    
    -- Recrear relaciones si tiene nuevo padre
    IF NEW.parent_id IS NOT NULL THEN
      INSERT INTO node_closure (ancestor_id, descendant_id, depth)
      SELECT ancestor_id, NEW.id, depth + 1
      FROM node_closure
      WHERE descendant_id = NEW.parent_id;
    END IF;
  END IF;
  
  RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_maintain_node_closure ON node;
CREATE TRIGGER trg_maintain_node_closure
  AFTER INSERT OR UPDATE ON node
  FOR EACH ROW
  EXECUTE FUNCTION maintain_node_closure();

-- ====================================
-- CONFIGURACIÓN JURÍDICA Y CLASIFICACIÓN DEL ASUNTO
-- ====================================

CREATE TABLE IF NOT EXISTS jurisdiccion (
  id          SERIAL PRIMARY KEY,
  codigo      VARCHAR(80) UNIQUE NOT NULL,
  nombre      VARCHAR(200) UNIQUE NOT NULL,
  descripcion TEXT,
  activo      BOOLEAN NOT NULL DEFAULT TRUE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS asunto (
  id              SERIAL PRIMARY KEY,
  jurisdiccion_id INTEGER NOT NULL REFERENCES jurisdiccion(id),
  codigo          VARCHAR(100) NOT NULL,
  nombre          VARCHAR(250) NOT NULL,
  descripcion     TEXT,
  activo          BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (jurisdiccion_id, codigo),
  UNIQUE (jurisdiccion_id, nombre)
);

CREATE INDEX IF NOT EXISTS idx_asunto_jurisdiccion ON asunto(jurisdiccion_id);

CREATE TABLE IF NOT EXISTS etapa_proceso (
  id          SERIAL PRIMARY KEY,
  codigo      VARCHAR(80) UNIQUE NOT NULL,
  nombre      VARCHAR(200) UNIQUE NOT NULL,
  descripcion TEXT,
  orden       INTEGER NOT NULL,
  activo      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS tipo_procedimiento (
  id          SERIAL PRIMARY KEY,
  asunto_id   INTEGER NOT NULL REFERENCES asunto(id),
  etapa_id    INTEGER NOT NULL REFERENCES etapa_proceso(id),
  codigo      VARCHAR(120) UNIQUE NOT NULL,
  nombre      VARCHAR(300) NOT NULL,
  norma       VARCHAR(150),
  descripcion TEXT,
  activo      BOOLEAN NOT NULL DEFAULT TRUE,
  UNIQUE (asunto_id, nombre)
);

CREATE INDEX IF NOT EXISTS idx_tipo_procedimiento_asunto
  ON tipo_procedimiento(asunto_id);

CREATE INDEX IF NOT EXISTS idx_tipo_procedimiento_etapa
  ON tipo_procedimiento(etapa_id);

-- Opciones que el frontend presenta al seleccionar un asunto.
CREATE TABLE IF NOT EXISTS opcion_gestion_asunto (
  id          SERIAL PRIMARY KEY,
  codigo      VARCHAR(80) UNIQUE NOT NULL,
  nombre      VARCHAR(200) UNIQUE NOT NULL,
  descripcion TEXT,
  orden       INTEGER NOT NULL,
  activo      BOOLEAN NOT NULL DEFAULT TRUE
);

-- ====================================
-- ACTUALIZACIÓN DEL MODELO DE PROCESO
-- ====================================

ALTER TABLE proceso
  ADD COLUMN IF NOT EXISTS jurisdiccion_id INTEGER,
  ADD COLUMN IF NOT EXISTS asunto_id INTEGER,
  ADD COLUMN IF NOT EXISTS etapa_actual_id INTEGER,
  ADD COLUMN IF NOT EXISTS tipo_procedimiento_id INTEGER;

CREATE INDEX IF NOT EXISTS idx_proceso_jurisdiccion ON proceso(jurisdiccion_id);
CREATE INDEX IF NOT EXISTS idx_proceso_asunto ON proceso(asunto_id);
CREATE INDEX IF NOT EXISTS idx_proceso_etapa_actual ON proceso(etapa_actual_id);
CREATE INDEX IF NOT EXISTS idx_proceso_tipo_procedimiento ON proceso(tipo_procedimiento_id);

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_proceso_jurisdiccion'
  ) THEN
    ALTER TABLE proceso
      ADD CONSTRAINT fk_proceso_jurisdiccion
      FOREIGN KEY (jurisdiccion_id) REFERENCES jurisdiccion(id);
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_proceso_asunto'
  ) THEN
    ALTER TABLE proceso
      ADD CONSTRAINT fk_proceso_asunto
      FOREIGN KEY (asunto_id) REFERENCES asunto(id);
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_proceso_etapa_actual'
  ) THEN
    ALTER TABLE proceso
      ADD CONSTRAINT fk_proceso_etapa_actual
      FOREIGN KEY (etapa_actual_id) REFERENCES etapa_proceso(id);
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_proceso_tipo_procedimiento'
  ) THEN
    ALTER TABLE proceso
      ADD CONSTRAINT fk_proceso_tipo_procedimiento
      FOREIGN KEY (tipo_procedimiento_id) REFERENCES tipo_procedimiento(id);
  END IF;
END $$;

-- Historial de etapas: permite saber cuándo entró/salió el proceso de cada etapa.
CREATE TABLE IF NOT EXISTS proceso_etapa (
  id           BIGSERIAL PRIMARY KEY,
  proceso_id   INTEGER NOT NULL REFERENCES proceso(id) ON DELETE CASCADE,
  etapa_id     INTEGER NOT NULL REFERENCES etapa_proceso(id),
  fecha_inicio DATE NOT NULL,
  fecha_fin    DATE,
  actual       BOOLEAN NOT NULL DEFAULT FALSE,
  observaciones TEXT,
  created_by   INTEGER NOT NULL REFERENCES usuario(id),
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT chk_proceso_etapa_fechas
    CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
);

CREATE INDEX IF NOT EXISTS idx_proceso_etapa_proceso
  ON proceso_etapa(proceso_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_proceso_etapa_actual
  ON proceso_etapa(proceso_id)
  WHERE actual = TRUE;

-- ====================================
-- DATOS ESPECÍFICOS DE ASUNTOS PENALES
-- ====================================

CREATE TABLE IF NOT EXISTS proceso_penal (
  proceso_id                 INTEGER PRIMARY KEY REFERENCES proceso(id) ON DELETE CASCADE,
  nunc                       VARCHAR(100) UNIQUE,
  competencia_municipal     VARCHAR(255),
  competencia_circuito      VARCHAR(255),
  competencia_circuito_especializado VARCHAR(255),
  hubo_victimas               BOOLEAN NOT NULL DEFAULT FALSE,
  created_at                  TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at                  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_proceso_penal_nunc ON proceso_penal(nunc);

CREATE TABLE IF NOT EXISTS persona (
  id                BIGSERIAL PRIMARY KEY,
  tipo_documento    VARCHAR(30),
  numero_documento  VARCHAR(100),
  nombres           VARCHAR(200),
  apellidos         VARCHAR(200),
  lugar_residencia  TEXT,
  email             VARCHAR(200),
  telefono          VARCHAR(30),
  fecha_nacimiento  DATE,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (tipo_documento, numero_documento)
);

-- Participantes del proceso. Permite varios imputados y varias víctimas.
CREATE TABLE IF NOT EXISTS proceso_persona (
  id                 BIGSERIAL PRIMARY KEY,
  proceso_id         INTEGER NOT NULL REFERENCES proceso(id) ON DELETE CASCADE,
  persona_id         BIGINT NOT NULL REFERENCES persona(id),
  rol                VARCHAR(30) NOT NULL
                     CHECK (rol IN ('IMPUTADO','VICTIMA','DEMANDANTE','DEMANDADO','OTRO')),
  orden               INTEGER NOT NULL DEFAULT 1,
  privado_libertad   BOOLEAN,
  tipo_detencion     VARCHAR(80)
                     CHECK (
                       tipo_detencion IS NULL
                       OR tipo_detencion IN (
                         'RESIDENCIA',
                         'PENITENCIARIO'
                       )
                     ),
  establecimiento_o_residencia TEXT,
  observaciones      TEXT,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (proceso_id, persona_id, rol)
);

CREATE INDEX IF NOT EXISTS idx_proceso_persona_proceso
  ON proceso_persona(proceso_id);

CREATE TABLE IF NOT EXISTS delito (
  id          SERIAL PRIMARY KEY,
  codigo      VARCHAR(100) UNIQUE,
  nombre      VARCHAR(300) NOT NULL,
  descripcion TEXT,
  activo      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS bien_juridico (
  id          SERIAL PRIMARY KEY,
  codigo      VARCHAR(100) UNIQUE,
  nombre      VARCHAR(300) NOT NULL,
  descripcion TEXT,
  activo      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS proceso_penal_delito (
  proceso_id      INTEGER NOT NULL REFERENCES proceso_penal(proceso_id) ON DELETE CASCADE,
  delito_id       INTEGER NOT NULL REFERENCES delito(id),
  descripcion     TEXT,
  PRIMARY KEY (proceso_id, delito_id)
);

CREATE TABLE IF NOT EXISTS proceso_penal_bien_juridico (
  proceso_id        INTEGER NOT NULL REFERENCES proceso_penal(proceso_id) ON DELETE CASCADE,
  bien_juridico_id  INTEGER NOT NULL REFERENCES bien_juridico(id),
  PRIMARY KEY (proceso_id, bien_juridico_id)
);

CREATE TABLE IF NOT EXISTS proceso_penal_imputacion (
  id             BIGSERIAL PRIMARY KEY,
  proceso_id     INTEGER NOT NULL REFERENCES proceso_penal(proceso_id) ON DELETE CASCADE,
  descripcion    TEXT,
  fecha_imputacion DATE,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (proceso_id)
);

-- ====================================
-- FISCALÍA Y JUZGADOS
-- ====================================

CREATE TABLE IF NOT EXISTS proceso_fiscal (
  id                    BIGSERIAL PRIMARY KEY,
  proceso_id            INTEGER NOT NULL REFERENCES proceso(id) ON DELETE CASCADE,
  nombre_fiscal         VARCHAR(255),
  unidad                VARCHAR(30) CHECK (unidad IN ('SECCIONAL','LOCAL','ESPECIALIZADA')),
  numero_fiscalia       VARCHAR(100),
  correo                VARCHAR(200),
  telefono              VARCHAR(30),
  created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (proceso_id)
);

CREATE TABLE IF NOT EXISTS proceso_juzgado (
  id              BIGSERIAL PRIMARY KEY,
  proceso_id      INTEGER NOT NULL REFERENCES proceso(id) ON DELETE CASCADE,
  tipo_juzgado    VARCHAR(30)
                  CHECK (tipo_juzgado IN ('GARANTIAS','CONOCIMIENTO')),
  nombre_juzgado  VARCHAR(300),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (proceso_id, tipo_juzgado)
);

-- ====================================
-- TIPOS DE ACTUACIÓN
-- ====================================

CREATE TABLE IF NOT EXISTS tipo_actuacion (
  id                    SERIAL PRIMARY KEY,
  asunto_id             INTEGER REFERENCES asunto(id),
  etapa_id              INTEGER NOT NULL REFERENCES etapa_proceso(id),
  codigo                VARCHAR(120) UNIQUE NOT NULL,
  nombre                VARCHAR(300) NOT NULL,
  grupo                 VARCHAR(40) NOT NULL
                        CHECK (grupo IN (
                          'PROCESO',
                          'RECURSO_EXTRAORDINARIO',
                          'EJECUCION_PENA'
                        )),
  requiere_fecha        BOOLEAN NOT NULL DEFAULT TRUE,
  permite_archivo       BOOLEAN NOT NULL DEFAULT TRUE,
  es_repetible          BOOLEAN NOT NULL DEFAULT FALSE,
  genera_termino        BOOLEAN NOT NULL DEFAULT FALSE,
  orden                 INTEGER NOT NULL DEFAULT 1,
  activo                BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_tipo_actuacion_asunto
  ON tipo_actuacion(asunto_id);

CREATE INDEX IF NOT EXISTS idx_tipo_actuacion_etapa
  ON tipo_actuacion(etapa_id);

CREATE TABLE IF NOT EXISTS proceso_actuacion (
  id                   BIGSERIAL PRIMARY KEY,
  proceso_id           INTEGER NOT NULL REFERENCES proceso(id) ON DELETE CASCADE,
  expediente_id        INTEGER NOT NULL REFERENCES expediente(id) ON DELETE CASCADE,
  tipo_actuacion_id    INTEGER NOT NULL REFERENCES tipo_actuacion(id),
  fecha_actuacion      DATE,
  contexto             TEXT,
  observaciones        TEXT,
  created_by            INTEGER NOT NULL REFERENCES usuario(id),
  created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_proceso_actuacion_proceso
  ON proceso_actuacion(proceso_id);

CREATE INDEX IF NOT EXISTS idx_proceso_actuacion_expediente
  ON proceso_actuacion(expediente_id);

CREATE INDEX IF NOT EXISTS idx_proceso_actuacion_tipo
  ON proceso_actuacion(tipo_actuacion_id);

CREATE TABLE IF NOT EXISTS actuacion_documento (
  id                BIGSERIAL PRIMARY KEY,
  actuacion_id      BIGINT NOT NULL REFERENCES proceso_actuacion(id) ON DELETE CASCADE,
  node_id           UUID NOT NULL REFERENCES node(id) ON DELETE RESTRICT,
  es_principal      BOOLEAN NOT NULL DEFAULT FALSE,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (actuacion_id, node_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_actuacion_documento_principal
  ON actuacion_documento(actuacion_id)
  WHERE es_principal = TRUE;

-- ====================================
-- RECURSO EXTRAORDINARIO
-- ====================================

CREATE TABLE IF NOT EXISTS recurso_extraordinario (
  id                    BIGSERIAL PRIMARY KEY,
  proceso_id            INTEGER NOT NULL REFERENCES proceso(id) ON DELETE CASCADE,
  expediente_id         INTEGER NOT NULL UNIQUE REFERENCES expediente(id) ON DELETE CASCADE,
  abogado_id             INTEGER NOT NULL REFERENCES usuario(id),
  contexto               TEXT,
  fecha_presentacion     DATE,
  documento_node_id      UUID REFERENCES node(id) ON DELETE RESTRICT,
  created_by             INTEGER NOT NULL REFERENCES usuario(id),
  created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_recurso_extraordinario_proceso
  ON recurso_extraordinario(proceso_id);

-- ====================================
-- REGLAS DE TÉRMINOS
-- ====================================

CREATE TABLE IF NOT EXISTS regla_termino (
  id                    SERIAL PRIMARY KEY,
  tipo_procedimiento_id INTEGER NOT NULL REFERENCES tipo_procedimiento(id) ON DELETE CASCADE,
  tipo_actuacion_id     INTEGER NOT NULL REFERENCES tipo_actuacion(id) ON DELETE CASCADE,
  dias                  INTEGER NOT NULL CHECK (dias > 0),
  unidad                 VARCHAR(20) NOT NULL DEFAULT 'DIAS'
                        CHECK (unidad IN ('DIAS','MESES')),
  titulo_evento         VARCHAR(255) NOT NULL,
  descripcion_evento    TEXT,
  activo                BOOLEAN NOT NULL DEFAULT TRUE,
  UNIQUE (tipo_procedimiento_id, tipo_actuacion_id)
);

CREATE TABLE IF NOT EXISTS termino_proceso (
  id                    BIGSERIAL PRIMARY KEY,
  actuacion_id          BIGINT NOT NULL REFERENCES proceso_actuacion(id) ON DELETE CASCADE,
  regla_termino_id      INTEGER NOT NULL REFERENCES regla_termino(id),
  fecha_inicio          DATE NOT NULL,
  dias_plazo            INTEGER NOT NULL,
  fecha_vencimiento     DATE NOT NULL,
  evento_id             INTEGER REFERENCES evento(id) ON DELETE SET NULL,
  calculado_automaticamente BOOLEAN NOT NULL DEFAULT TRUE,
  created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (actuacion_id, regla_termino_id)
);

CREATE INDEX IF NOT EXISTS idx_termino_proceso_vencimiento
  ON termino_proceso(fecha_vencimiento);

CREATE INDEX IF NOT EXISTS idx_termino_proceso_actuacion
  ON termino_proceso(actuacion_id);

-- ====================================
-- PLANTILLAS DE CARPETAS DEL EXPEDIENTE
-- ====================================

CREATE TABLE IF NOT EXISTS plantilla_expediente (
  id            SERIAL PRIMARY KEY,
  asunto_id     INTEGER NOT NULL REFERENCES asunto(id),
  nombre        VARCHAR(255) NOT NULL,
  descripcion   TEXT,
  activo        BOOLEAN NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (asunto_id, nombre)
);

CREATE TABLE IF NOT EXISTS plantilla_expediente_item (
  id                 BIGSERIAL PRIMARY KEY,
  plantilla_id       INTEGER NOT NULL REFERENCES plantilla_expediente(id) ON DELETE CASCADE,
  parent_item_id     BIGINT REFERENCES plantilla_expediente_item(id) ON DELETE CASCADE,
  codigo             VARCHAR(120) NOT NULL,
  nombre             VARCHAR(255) NOT NULL,
  orden              INTEGER NOT NULL DEFAULT 1,
  activo             BOOLEAN NOT NULL DEFAULT TRUE,
  UNIQUE (plantilla_id, codigo)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_plantilla_expediente_root
  ON plantilla_expediente_item(plantilla_id)
  WHERE parent_item_id IS NULL;

ALTER TABLE expediente
  ADD COLUMN IF NOT EXISTS plantilla_expediente_id INTEGER;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_expediente_plantilla'
  ) THEN
    ALTER TABLE expediente
      ADD CONSTRAINT fk_expediente_plantilla
      FOREIGN KEY (plantilla_expediente_id)
      REFERENCES plantilla_expediente(id);
  END IF;
END $$;

ALTER TABLE node
  ADD COLUMN IF NOT EXISTS plantilla_item_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_node_plantilla_item
  ON node(plantilla_item_id);

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_node_plantilla_item'
  ) THEN
    ALTER TABLE node
      ADD CONSTRAINT fk_node_plantilla_item
      FOREIGN KEY (plantilla_item_id)
      REFERENCES plantilla_expediente_item(id);
  END IF;
END $$;

-- La raíz del expediente ahora queda referenciada explícitamente.
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_expediente_root_node'
  ) THEN
    ALTER TABLE expediente
      ADD CONSTRAINT fk_expediente_root_node
      FOREIGN KEY (root_node_id)
      REFERENCES node(id)
      ON DELETE SET NULL;
  END IF;
END $$;

-- ====================================
-- TRIGGERS / FUNCIONES DE VALIDACIÓN
-- ====================================

CREATE OR REPLACE FUNCTION validar_proceso_actuacion()
RETURNS TRIGGER AS $$
DECLARE
  v_es_repetible BOOLEAN;
  v_proceso_expediente INTEGER;
  v_tipo_asunto INTEGER;
BEGIN
  SELECT ta.es_repetible
    INTO v_es_repetible
  FROM tipo_actuacion ta
  WHERE ta.id = NEW.tipo_actuacion_id;

  SELECT e.proceso_id
    INTO v_proceso_expediente
  FROM expediente e
  WHERE e.id = NEW.expediente_id;

  IF v_proceso_expediente IS DISTINCT FROM NEW.proceso_id THEN
    RAISE EXCEPTION
      'El expediente % no pertenece al proceso %',
      NEW.expediente_id, NEW.proceso_id;
  END IF;

  IF NOT v_es_repetible AND EXISTS (
    SELECT 1
    FROM proceso_actuacion pa
    JOIN tipo_actuacion ta2
      ON ta2.id = pa.tipo_actuacion_id
    WHERE pa.proceso_id = NEW.proceso_id
      AND pa.tipo_actuacion_id = NEW.tipo_actuacion_id
      AND pa.id <> COALESCE(NEW.id, 0)
  ) THEN
    RAISE EXCEPTION
      'La actuación % no puede registrarse más de una vez para el proceso %',
      NEW.tipo_actuacion_id, NEW.proceso_id;
  END IF;

  SELECT a.jurisdiccion_id
    INTO v_tipo_asunto
  FROM proceso p
  JOIN asunto a ON a.id = p.asunto_id
  WHERE p.id = NEW.proceso_id;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_validar_proceso_actuacion ON proceso_actuacion;
CREATE TRIGGER trg_validar_proceso_actuacion
BEFORE INSERT OR UPDATE ON proceso_actuacion
FOR EACH ROW EXECUTE FUNCTION validar_proceso_actuacion();

CREATE OR REPLACE FUNCTION validar_recurso_extraordinario()
RETURNS TRIGGER AS $$
DECLARE
  v_proceso_expediente INTEGER;
BEGIN
  SELECT e.proceso_id
    INTO v_proceso_expediente
  FROM expediente e
  WHERE e.id = NEW.expediente_id;

  IF v_proceso_expediente IS DISTINCT FROM NEW.proceso_id THEN
    RAISE EXCEPTION
      'El expediente % no pertenece al proceso %',
      NEW.expediente_id, NEW.proceso_id;
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_validar_recurso_extraordinario ON recurso_extraordinario;
CREATE TRIGGER trg_validar_recurso_extraordinario
BEFORE INSERT OR UPDATE ON recurso_extraordinario
FOR EACH ROW EXECUTE FUNCTION validar_recurso_extraordinario();

-- ====================================
-- CREACIÓN AUTOMÁTICA DE CARPETAS DEL EXPEDIENTE
-- ====================================

CREATE OR REPLACE FUNCTION crear_estructura_expediente(
  p_expediente_id INTEGER,
  p_plantilla_id INTEGER,
  p_usuario_id INTEGER
)
RETURNS VOID AS $$
DECLARE
  r RECORD;
  v_parent_node UUID;
  v_node UUID;
  v_map JSONB := '{}'::JSONB;
  v_root UUID;
BEGIN
  FOR r IN
    WITH RECURSIVE arbol AS (
      SELECT
        i.id,
        i.parent_item_id,
        i.codigo,
        i.nombre,
        i.orden,
        0 AS depth
      FROM plantilla_expediente_item i
      WHERE i.plantilla_id = p_plantilla_id
        AND i.parent_item_id IS NULL

      UNION ALL

      SELECT
        i.id,
        i.parent_item_id,
        i.codigo,
        i.nombre,
        i.orden,
        arbol.depth + 1
      FROM plantilla_expediente_item i
      JOIN arbol
        ON arbol.id = i.parent_item_id
    )
    SELECT *
    FROM arbol
    ORDER BY depth, orden, nombre
  LOOP
    IF r.parent_item_id IS NULL THEN
      v_parent_node := NULL;
    ELSE
      v_parent_node := (v_map ->> r.parent_item_id::TEXT)::UUID;
    END IF;

    INSERT INTO node (
      expediente_id,
      parent_id,
      type,
      name,
      modulo,
      created_by,
      plantilla_item_id
    )
    VALUES (
      p_expediente_id,
      v_parent_node,
      'FOLDER',
      r.nombre,
      'DOCUMENTAL',
      p_usuario_id,
      r.id
    )
    RETURNING id INTO v_node;

    v_map := v_map || jsonb_build_object(r.id::TEXT, v_node::TEXT);

    IF r.parent_item_id IS NULL THEN
      v_root := v_node;
    END IF;
  END LOOP;

  UPDATE expediente
     SET root_node_id = v_root,
         plantilla_expediente_id = p_plantilla_id
   WHERE id = p_expediente_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION seleccionar_plantilla_y_crear_expediente()
RETURNS TRIGGER AS $$
DECLARE
  v_asunto_id INTEGER;
  v_plantilla_id INTEGER;
BEGIN
  SELECT p.asunto_id
    INTO v_asunto_id
  FROM proceso p
  WHERE p.id = NEW.proceso_id;

  IF NEW.plantilla_expediente_id IS NOT NULL THEN
    v_plantilla_id := NEW.plantilla_expediente_id;
  ELSE
    SELECT pe.id
      INTO v_plantilla_id
    FROM plantilla_expediente pe
    WHERE pe.asunto_id = v_asunto_id
      AND pe.activo = TRUE
    ORDER BY pe.id
    LIMIT 1;
  END IF;

  IF v_plantilla_id IS NOT NULL THEN
    PERFORM crear_estructura_expediente(
      NEW.id,
      v_plantilla_id,
      NEW.created_by
    );
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_crear_estructura_expediente ON expediente;
CREATE TRIGGER trg_crear_estructura_expediente
AFTER INSERT ON expediente
FOR EACH ROW EXECUTE FUNCTION seleccionar_plantilla_y_crear_expediente();

-- ====================================
-- CÁLCULO AUTOMÁTICO DE TÉRMINOS Y CALENDARIO
-- ====================================

CREATE OR REPLACE FUNCTION generar_termino_desde_actuacion()
RETURNS TRIGGER AS $$
DECLARE
  v_regla regla_termino%ROWTYPE;
  v_fecha_vencimiento DATE;
  v_evento_id INTEGER;
  v_evento_tipo_id INTEGER;
  v_nombre_actuacion VARCHAR(300);
BEGIN
  IF NEW.fecha_actuacion IS NULL THEN
    RETURN NEW;
  END IF;

  SELECT rt.*
    INTO v_regla
  FROM regla_termino rt
  JOIN proceso p
    ON p.tipo_procedimiento_id = rt.tipo_procedimiento_id
  WHERE p.id = NEW.proceso_id
    AND rt.tipo_actuacion_id = NEW.tipo_actuacion_id
    AND rt.activo = TRUE
  LIMIT 1;

  IF NOT FOUND THEN
    RETURN NEW;
  END IF;

  IF v_regla.unidad = 'DIAS' THEN
    v_fecha_vencimiento := NEW.fecha_actuacion + v_regla.dias;
  ELSE
    v_fecha_vencimiento := (NEW.fecha_actuacion + (v_regla.dias || ' months')::INTERVAL)::DATE;
  END IF;

  SELECT id INTO v_evento_tipo_id
  FROM tipo_evento
  WHERE LOWER(nombre) = LOWER('Vencimiento')
  LIMIT 1;

  SELECT nombre INTO v_nombre_actuacion
  FROM tipo_actuacion
  WHERE id = NEW.tipo_actuacion_id;

  IF v_evento_tipo_id IS NOT NULL THEN
    SELECT tp.evento_id
      INTO v_evento_id
    FROM termino_proceso tp
    WHERE tp.actuacion_id = NEW.id
      AND tp.regla_termino_id = v_regla.id
    LIMIT 1;

    IF v_evento_id IS NULL THEN
      INSERT INTO evento (
        titulo,
        descripcion,
        tipo_evento_id,
        fecha_inicio,
        fecha_fin,
        all_day,
        proceso_id,
        expediente_id,
        responsable_id
      )
      SELECT
        v_regla.titulo_evento,
        COALESCE(v_regla.descripcion_evento, 'Término generado automáticamente por la actuación ' || v_nombre_actuacion),
        v_evento_tipo_id,
        v_fecha_vencimiento::TIMESTAMPTZ,
        v_fecha_vencimiento::TIMESTAMPTZ,
        TRUE,
        NEW.proceso_id,
        NEW.expediente_id,
        p.abogado_responsable_id
      FROM proceso p
      WHERE p.id = NEW.proceso_id
      RETURNING id INTO v_evento_id;
    ELSE
      UPDATE evento
         SET titulo = v_regla.titulo_evento,
             descripcion = COALESCE(v_regla.descripcion_evento, 'Término generado automáticamente por la actuación ' || v_nombre_actuacion),
             fecha_inicio = v_fecha_vencimiento::TIMESTAMPTZ,
             fecha_fin = v_fecha_vencimiento::TIMESTAMPTZ,
             proceso_id = NEW.proceso_id,
             expediente_id = NEW.expediente_id
       WHERE id = v_evento_id;
    END IF;
  END IF;

  INSERT INTO termino_proceso (
    actuacion_id,
    regla_termino_id,
    fecha_inicio,
    dias_plazo,
    fecha_vencimiento,
    evento_id
  )
  VALUES (
    NEW.id,
    v_regla.id,
    NEW.fecha_actuacion,
    v_regla.dias,
    v_fecha_vencimiento,
    v_evento_id
  )
  ON CONFLICT (actuacion_id, regla_termino_id)
  DO UPDATE SET
    fecha_inicio = EXCLUDED.fecha_inicio,
    dias_plazo = EXCLUDED.dias_plazo,
    fecha_vencimiento = EXCLUDED.fecha_vencimiento,
    evento_id = EXCLUDED.evento_id,
    updated_at = now();

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_generar_termino_desde_actuacion ON proceso_actuacion;
CREATE TRIGGER trg_generar_termino_desde_actuacion
AFTER INSERT OR UPDATE OF fecha_actuacion ON proceso_actuacion
FOR EACH ROW
EXECUTE FUNCTION generar_termino_desde_actuacion();

-- ====================================
-- REGLAS DE INTEGRIDAD PARA PROCEDIMIENTO
-- ====================================

CREATE OR REPLACE FUNCTION validar_tipo_procedimiento_proceso()
RETURNS TRIGGER AS $$
DECLARE
  v_asunto_id INTEGER;
  v_etapa_id INTEGER;
  v_asunto_procedimiento INTEGER;
  v_etapa_procedimiento INTEGER;
BEGIN
  IF NEW.tipo_procedimiento_id IS NULL THEN
    RETURN NEW;
  END IF;

  SELECT asunto_id, etapa_id
    INTO v_asunto_procedimiento, v_etapa_procedimiento
  FROM tipo_procedimiento
  WHERE id = NEW.tipo_procedimiento_id;

  IF v_asunto_procedimiento IS NULL THEN
    RAISE EXCEPTION 'El tipo de procedimiento % no existe', NEW.tipo_procedimiento_id;
  END IF;

  IF NEW.asunto_id IS DISTINCT FROM v_asunto_procedimiento THEN
    RAISE EXCEPTION
      'El procedimiento % no pertenece al asunto %',
      NEW.tipo_procedimiento_id, NEW.asunto_id;
  END IF;

  SELECT id INTO v_etapa_id
  FROM etapa_proceso
  WHERE codigo = 'CONOCIMIENTO';

  IF v_etapa_procedimiento IS DISTINCT FROM v_etapa_id THEN
    RAISE EXCEPTION
      'El tipo de procedimiento % no pertenece a la etapa de conocimiento',
      NEW.tipo_procedimiento_id;
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_validar_tipo_procedimiento_proceso ON proceso;
CREATE TRIGGER trg_validar_tipo_procedimiento_proceso
BEFORE INSERT OR UPDATE OF asunto_id, tipo_procedimiento_id
ON proceso
FOR EACH ROW
EXECUTE FUNCTION validar_tipo_procedimiento_proceso();

-- ====================================
-- DATOS INICIALES NUEVO MODELO
-- ====================================

INSERT INTO jurisdiccion (codigo, nombre, descripcion) VALUES
  ('ORDINARIA', 'JURISDICCION ORDINARIA', 'Jurisdicción ordinaria'),
  ('CONTENCIOSA_ADMINISTRATIVA', 'JURISDICCION CONTENCIOSA ADMINISTRATIVA', 'Jurisdicción contenciosa administrativa'),
  ('ESPECIALES_TRANSITORIA', 'JURISDICCION ESPECIALES Y TRANSITORIA', 'Jurisdicciones especiales y transitoria')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO asunto (jurisdiccion_id, codigo, nombre) VALUES
  ((SELECT id FROM jurisdiccion WHERE codigo = 'ORDINARIA'), 'PENALES', 'ASUNTOS PENALES'),
  ((SELECT id FROM jurisdiccion WHERE codigo = 'ORDINARIA'), 'CORPORATIVOS', 'ASUNTOS CORPORATIVOS'),
  ((SELECT id FROM jurisdiccion WHERE codigo = 'ORDINARIA'), 'CIVILES_COMERCIALES', 'ASUNTOS CIVILES/COMERCIALES'),
  ((SELECT id FROM jurisdiccion WHERE codigo = 'CONTENCIOSA_ADMINISTRATIVA'), 'REPARACION_DIRECTA', 'ASUNTOS DE REPARACION DIRECTA'),
  ((SELECT id FROM jurisdiccion WHERE codigo = 'CONTENCIOSA_ADMINISTRATIVA'), 'CONTRATACION_ESTATAL', 'ASUNTOS DE CONTRATACION ESTATAL'),
  ((SELECT id FROM jurisdiccion WHERE codigo = 'CONTENCIOSA_ADMINISTRATIVA'), 'DISCIPLINARIOS', 'ASUNTOS DISCIPLINARIOS'),
  ((SELECT id FROM jurisdiccion WHERE codigo = 'ESPECIALES_TRANSITORIA'), 'JUSTICIA_TRANSICIONAL', 'ASUNTOS DE LA JUSTICIA TRANSICIONAL'),
  ((SELECT id FROM jurisdiccion WHERE codigo = 'ESPECIALES_TRANSITORIA'), 'PENAL_MILITAR', 'ASUNTOS JURISDICCION PENAL MILITAR'),
  ((SELECT id FROM jurisdiccion WHERE codigo = 'ESPECIALES_TRANSITORIA'), 'ESPECIAL_INDIGENA', 'ASUNTOS JURISDICCION ESPECIAL INDIGENA')
ON CONFLICT (jurisdiccion_id, codigo) DO NOTHING;

INSERT INTO etapa_proceso (codigo, nombre, descripcion, orden) VALUES
  ('CONOCIMIENTO', 'ETAPA DE CONOCIMIENTO', 'Etapa de conocimiento del asunto', 1),
  ('RECURSO_EXTRAORDINARIO', 'RECURSO EXTRAORDINARIO', 'Recurso extraordinario asociado al expediente', 2),
  ('PENITENCIARIO_EJECUCION_PENA', 'PENITENCIARIO – EJECUCION DE LA PENA', 'Actuaciones asociadas a la ejecución de la pena', 3)
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO opcion_gestion_asunto (codigo, nombre, descripcion, orden) VALUES
  ('ASUNTO_NUEVO', 'ASUNTO NUEVO', 'Crear un nuevo asunto y su expediente', 1),
  ('MODIFICAR_ACTUACION', 'MODIFICAR O ADICIONAR ACTUACION DE ASUNTO CARGADO', 'Consultar un asunto existente y registrar actuaciones', 2),
  ('ESTADISTICA_AREA', 'ESTADISTICA DE AREA', 'Consultar estadísticas del área', 3)
ON CONFLICT (codigo) DO NOTHING;

-- Tipos de procedimiento para ASUNTOS PENALES.
INSERT INTO tipo_procedimiento (asunto_id, etapa_id, codigo, nombre, norma, descripcion) VALUES
  (
    (SELECT id FROM asunto WHERE codigo = 'PENALES'),
    (SELECT id FROM etapa_proceso WHERE codigo = 'CONOCIMIENTO'),
    'LEY_906_2004',
    'LEY 906 DE 2004 - PROCEDIMIENTO PENAL ACUSATORIO',
    'Ley 906 de 2004',
    'Procedimiento penal acusatorio'
  ),
  (
    (SELECT id FROM asunto WHERE codigo = 'PENALES'),
    (SELECT id FROM etapa_proceso WHERE codigo = 'CONOCIMIENTO'),
    'LEY_1908_2018',
    'LEY 1908 DE 2018 - PROCEDIMIENTO ESPECIAL GAO, GAOR Y GDO',
    'Ley 1908 de 2018',
    'Procedimiento especial GAO, GAOR y GDO'
  ),
  (
    (SELECT id FROM asunto WHERE codigo = 'PENALES'),
    (SELECT id FROM etapa_proceso WHERE codigo = 'CONOCIMIENTO'),
    'LEY_1826_2017',
    'LEY 1826 DE 2017 - PROCEDIMIENTO ESPECIAL ABREVIADO',
    'Ley 1826 de 2017',
    'Procedimiento especial abreviado'
  ),
  (
    (SELECT id FROM asunto WHERE codigo = 'PENALES'),
    (SELECT id FROM etapa_proceso WHERE codigo = 'CONOCIMIENTO'),
    'LEY_1098_2006',
    'LEY 1098 DE 2006 - PROCEDIMIENTO DE INFANCIA Y ADOLECENCIA',
    'Ley 1098 de 2006',
    'Procedimiento de infancia y adolescencia'
  ),
  (
    (SELECT id FROM asunto WHERE codigo = 'PENALES'),
    (SELECT id FROM etapa_proceso WHERE codigo = 'CONOCIMIENTO'),
    'REPRESENTACION_VICTIMAS',
    'REPRESENTACION DE VICTIMAS',
    NULL,
    'Representación de víctimas'
  )
ON CONFLICT (codigo) DO NOTHING;

-- Actuaciones de asuntos penales.
INSERT INTO tipo_actuacion
  (asunto_id, etapa_id, codigo, nombre, grupo, requiere_fecha, permite_archivo, es_repetible, genera_termino, orden)
VALUES
  ((SELECT id FROM asunto WHERE codigo = 'PENALES'), (SELECT id FROM etapa_proceso WHERE codigo = 'CONOCIMIENTO'),
   'LEGALIZACION_CAPTURA', 'LEGALIZACION DE CAPTURA', 'PROCESO', TRUE, TRUE, FALSE, FALSE, 1),
  ((SELECT id FROM asunto WHERE codigo = 'PENALES'), (SELECT id FROM etapa_proceso WHERE codigo = 'CONOCIMIENTO'),
   'FORMULACION_IMPUTACION', 'FORMULACION DE IMPUTACION', 'PROCESO', TRUE, TRUE, FALSE, TRUE, 2),
  ((SELECT id FROM asunto WHERE codigo = 'PENALES'), (SELECT id FROM etapa_proceso WHERE codigo = 'CONOCIMIENTO'),
   'MEDIDA_ASEGURAMIENTO', 'MEDIDA DE ASEGURAMIENTO', 'PROCESO', TRUE, TRUE, FALSE, FALSE, 3),
  ((SELECT id FROM asunto WHERE codigo = 'PENALES'), (SELECT id FROM etapa_proceso WHERE codigo = 'CONOCIMIENTO'),
   'REVOCATORIA_MEDIDA_ASEGURAMIENTO', 'REVOCATORIA O SUSTITUCION DE LA MEDIDA DE ASEGURAMIENTO', 'PROCESO', TRUE, TRUE, TRUE, FALSE, 4),
  ((SELECT id FROM asunto WHERE codigo = 'PENALES'), (SELECT id FROM etapa_proceso WHERE codigo = 'CONOCIMIENTO'),
   'ACUSACION_FORMAL', 'ACUSACION FORMAL', 'PROCESO', TRUE, TRUE, FALSE, TRUE, 5),
  ((SELECT id FROM asunto WHERE codigo = 'PENALES'), (SELECT id FROM etapa_proceso WHERE codigo = 'CONOCIMIENTO'),
   'PREACUERDO', 'PREACUERDO', 'PROCESO', TRUE, TRUE, FALSE, FALSE, 6),
  ((SELECT id FROM asunto WHERE codigo = 'PENALES'), (SELECT id FROM etapa_proceso WHERE codigo = 'CONOCIMIENTO'),
   'DESCUBRIMIENTO_PROBATORIO', 'DESCUBRIMIENTO PROBATORIO', 'PROCESO', TRUE, TRUE, FALSE, FALSE, 7),
  ((SELECT id FROM asunto WHERE codigo = 'PENALES'), (SELECT id FROM etapa_proceso WHERE codigo = 'CONOCIMIENTO'),
   'AUDIENCIA_PREPARATORIA', 'AUDIENCIA PREPARATORIA', 'PROCESO', TRUE, TRUE, FALSE, FALSE, 8),
  ((SELECT id FROM asunto WHERE codigo = 'PENALES'), (SELECT id FROM etapa_proceso WHERE codigo = 'CONOCIMIENTO'),
   'INICIO_JUICIO_ORAL', 'INICIO DE JUICIO ORAL', 'PROCESO', TRUE, TRUE, FALSE, TRUE, 9)
ON CONFLICT (codigo) DO NOTHING;

-- Actuaciones de ejecución de pena.
INSERT INTO tipo_actuacion
  (asunto_id, etapa_id, codigo, nombre, grupo, requiere_fecha, permite_archivo, es_repetible, genera_termino, orden)
VALUES
  ((SELECT id FROM asunto WHERE codigo = 'PENALES'), (SELECT id FROM etapa_proceso WHERE codigo = 'PENITENCIARIO_EJECUCION_PENA'),
   'CONSTANCIA_VISITA_PENITENCIARIA', 'CONSTANCIA DE VISITA PENITENCIARIA', 'EJECUCION_PENA', TRUE, TRUE, TRUE, FALSE, 1),
  ((SELECT id FROM asunto WHERE codigo = 'PENALES'), (SELECT id FROM etapa_proceso WHERE codigo = 'PENITENCIARIO_EJECUCION_PENA'),
   'SOLICITUD_SALUD_PPL', 'SOLICITUDES DE SALUD DE PPL', 'EJECUCION_PENA', TRUE, TRUE, TRUE, FALSE, 2),
  ((SELECT id FROM asunto WHERE codigo = 'PENALES'), (SELECT id FROM etapa_proceso WHERE codigo = 'PENITENCIARIO_EJECUCION_PENA'),
   'MECANISMO_LIBERTAD', 'MECANISMOS DE LIBERTAD (CONDICIONAL Y DOMICILIARIA)', 'EJECUCION_PENA', TRUE, TRUE, TRUE, FALSE, 3),
  ((SELECT id FROM asunto WHERE codigo = 'PENALES'), (SELECT id FROM etapa_proceso WHERE codigo = 'PENITENCIARIO_EJECUCION_PENA'),
   'REDENCION_PENAS_FAVORABILIDAD', 'REDENCION DE PENAS Y APLICACIÓN DE FAVORABILIDAD', 'EJECUCION_PENA', TRUE, TRUE, TRUE, FALSE, 4),
  ((SELECT id FROM asunto WHERE codigo = 'PENALES'), (SELECT id FROM etapa_proceso WHERE codigo = 'PENITENCIARIO_EJECUCION_PENA'),
   'SOLICITUD_TRASLADO_ESTABLECIMIENTO', 'SOLICITUD DE TRASLADOS DE ESTABLECIMIENTOS PENITENCIARIOS', 'EJECUCION_PENA', TRUE, TRUE, TRUE, FALSE, 5)
ON CONFLICT (codigo) DO NOTHING;

-- Recurso extraordinario genérico: el detalle del recurso se guarda en recurso_extraordinario.
INSERT INTO tipo_actuacion
  (asunto_id, etapa_id, codigo, nombre, grupo, requiere_fecha, permite_archivo, es_repetible, genera_termino, orden)
VALUES
  ((SELECT id FROM asunto WHERE codigo = 'PENALES'),
   (SELECT id FROM etapa_proceso WHERE codigo = 'RECURSO_EXTRAORDINARIO'),
   'RECURSO_EXTRAORDINARIO', 'RECURSO EXTRAORDINARIO', 'RECURSO_EXTRAORDINARIO',
   TRUE, TRUE, FALSE, FALSE, 1)
ON CONFLICT (codigo) DO NOTHING;

-- ====================================
-- ESTRUCTURA DOCUMENTAL PARA ASUNTOS PENALES
-- ====================================

INSERT INTO plantilla_expediente (asunto_id, nombre, descripcion)
VALUES (
  (SELECT id FROM asunto WHERE codigo = 'PENALES'),
  'ESTRUCTURA_PENAL',
  'Estructura base del expediente para asuntos penales'
)
ON CONFLICT (asunto_id, nombre) DO NOTHING;

-- Raíz
INSERT INTO plantilla_expediente_item (plantilla_id, parent_item_id, codigo, nombre, orden)
SELECT pe.id, NULL, 'ROOT_PENAL', 'EXPEDIENTE PENAL', 1
FROM plantilla_expediente pe
WHERE pe.nombre = 'ESTRUCTURA_PENAL'
ON CONFLICT (plantilla_id, codigo) DO NOTHING;

-- Carpetas principales.
INSERT INTO plantilla_expediente_item (plantilla_id, parent_item_id, codigo, nombre, orden)
SELECT pe.id, root.id, v.codigo, v.nombre, v.orden
FROM plantilla_expediente pe
JOIN plantilla_expediente_item root
  ON root.plantilla_id = pe.id
 AND root.codigo = 'ROOT_PENAL'
CROSS JOIN (
  VALUES
    ('LEGALIZACION_CAPTURA', 'LEGALIZACION DE CAPTURA', 1),
    ('FORMULACION_IMPUTACION', 'FORMULACION DE IMPUTACION', 2),
    ('MEDIDA_ASEGURAMIENTO', 'MEDIDA DE ASEGURAMIENTO', 3),
    ('REVOCATORIA_SUSTITUCION_MEDIDA', 'REVOCATORIA O SUSTITUCION DE LA MEDIDA DE ASEGURAMIENTO', 4),
    ('ACTOS_INVESTIGACION_POST_IMPUTACION', 'ACTOS DE INVESTIGACION ADELANTADOS POSTERIOR A LA IMPUTACION', 5),
    ('FORMULACION_ACUSACION', 'FORMULACION DE ACUSACION', 6),
    ('DESCUBRIMIENTO_PROBATORIO', 'DESCUBRIMIENTO PROBATORIO', 7),
    ('AUDIENCIA_PREPARATORIA', 'AUDIENCIA PREPARATORIA', 8),
    ('JUICIO_ORAL', 'JUICIO ORAL', 9)
) AS v(codigo, nombre, orden)
WHERE pe.nombre = 'ESTRUCTURA_PENAL'
ON CONFLICT (plantilla_id, codigo) DO NOTHING;

-- Subcarpetas.
INSERT INTO plantilla_expediente_item (plantilla_id, parent_item_id, codigo, nombre, orden)
SELECT pe.id, parent.id, v.codigo, v.nombre, v.orden
FROM plantilla_expediente pe
JOIN (
  VALUES
    ('LEGALIZACION_CAPTURA','LEGALIZACION_CAPTURA_INFORMES','Informes',1),
    ('LEGALIZACION_CAPTURA','LEGALIZACION_CAPTURA_INDIVIDUALIZACION','Individualización del victimario y sus derechos',2),
    ('LEGALIZACION_CAPTURA','LEGALIZACION_CAPTURA_INCAUTACION','Incautación',3),
    ('MEDIDA_ASEGURAMIENTO','MEDIDA_ASEGURAMIENTO_EMP','Elementos Materiales Probatorios',1),
    ('REVOCATORIA_SUSTITUCION_MEDIDA','REVOCATORIA_MEDIDA','Revocatoria de la medida de aseguramiento',1),
    ('REVOCATORIA_SUSTITUCION_MEDIDA','SUSTITUCION_MEDIDA','Sustitución de la medida de aseguramiento',2),
    ('FORMULACION_ACUSACION','ACUSACION_FORMAL','Acusación Formal',1),
    ('FORMULACION_ACUSACION','APRUEBA_PREACUERDO','Aprueba y verificación de PreAcuerdo',2),
    ('FORMULACION_ACUSACION','IMPRUEBA_PREACUERDO','Imprueba de PreAcuerdo',3),
    ('DESCUBRIMIENTO_PROBATORIO','DOCUMENTALES','Documentales',1),
    ('DESCUBRIMIENTO_PROBATORIO','TESTIMONIALES','Testimoniales',2),
    ('DESCUBRIMIENTO_PROBATORIO','PERICIALES','Periciales',3)
) AS v(parent_codigo,codigo,nombre,orden) ON TRUE
JOIN plantilla_expediente_item parent
  ON parent.plantilla_id = pe.id
 AND parent.codigo = v.parent_codigo
WHERE pe.nombre = 'ESTRUCTURA_PENAL'
ON CONFLICT (plantilla_id, codigo) DO NOTHING;

-- Reglas de términos para los procedimientos suministrados.
INSERT INTO regla_termino
  (tipo_procedimiento_id, tipo_actuacion_id, dias, unidad, titulo_evento, descripcion_evento)
VALUES
  (
    (SELECT id FROM tipo_procedimiento WHERE codigo = 'LEY_906_2004'),
    (SELECT id FROM tipo_actuacion WHERE codigo = 'FORMULACION_IMPUTACION'),
    60, 'DIAS',
    'Vencimiento término posterior a imputación - Ley 906',
    'Término configurado para Ley 906 de 2004 desde la formulación de imputación.'
  ),
  (
    (SELECT id FROM tipo_procedimiento WHERE codigo = 'LEY_906_2004'),
    (SELECT id FROM tipo_actuacion WHERE codigo = 'ACUSACION_FORMAL'),
    120, 'DIAS',
    'Vencimiento término posterior a acusación - Ley 906',
    'Término configurado para Ley 906 de 2004 desde la acusación.'
  ),
  (
    (SELECT id FROM tipo_procedimiento WHERE codigo = 'LEY_906_2004'),
    (SELECT id FROM tipo_actuacion WHERE codigo = 'INICIO_JUICIO_ORAL'),
    150, 'DIAS',
    'Vencimiento término posterior al inicio de juicio oral - Ley 906',
    'Término configurado para Ley 906 de 2004 desde el inicio del juicio oral.'
  ),
  (
    (SELECT id FROM tipo_procedimiento WHERE codigo = 'LEY_1826_2017'),
    (SELECT id FROM tipo_actuacion WHERE codigo = 'ACUSACION_FORMAL'),
    70, 'DIAS',
    'Vencimiento término posterior a acusación - Ley 1826',
    'Término configurado para Ley 1826 de 2017 desde la acusación.'
  ),
  (
    (SELECT id FROM tipo_procedimiento WHERE codigo = 'LEY_1826_2017'),
    (SELECT id FROM tipo_actuacion WHERE codigo = 'INICIO_JUICIO_ORAL'),
    75, 'DIAS',
    'Vencimiento término posterior al inicio de juicio oral - Ley 1826',
    'Término configurado para Ley 1826 de 2017 desde el inicio del juicio oral.'
  ),
  (
    (SELECT id FROM tipo_procedimiento WHERE codigo = 'LEY_1908_2018'),
    (SELECT id FROM tipo_actuacion WHERE codigo = 'FORMULACION_IMPUTACION'),
    400, 'DIAS',
    'Vencimiento término posterior a imputación - Ley 1908',
    'Término configurado para Ley 1908 de 2018 desde la imputación.'
  ),
  (
    (SELECT id FROM tipo_procedimiento WHERE codigo = 'LEY_1908_2018'),
    (SELECT id FROM tipo_actuacion WHERE codigo = 'ACUSACION_FORMAL'),
    500, 'DIAS',
    'Vencimiento término posterior a acusación - Ley 1908',
    'Término configurado para Ley 1908 de 2018 desde la acusación.'
  ),
  (
    (SELECT id FROM tipo_procedimiento WHERE codigo = 'LEY_1908_2018'),
    (SELECT id FROM tipo_actuacion WHERE codigo = 'INICIO_JUICIO_ORAL'),
    500, 'DIAS',
    'Vencimiento término posterior al inicio de juicio oral - Ley 1908',
    'Término configurado para Ley 1908 de 2018 desde el inicio del juicio oral.'
  )
ON CONFLICT (tipo_procedimiento_id, tipo_actuacion_id) DO UPDATE
SET dias = EXCLUDED.dias,
    unidad = EXCLUDED.unidad,
    titulo_evento = EXCLUDED.titulo_evento,
    descripcion_evento = EXCLUDED.descripcion_evento,
    activo = TRUE;

-- ====================================
-- TRIGGERS DE TIMESTAMP PARA NUEVAS TABLAS
-- ====================================

DROP TRIGGER IF EXISTS trg_jurisdiccion_updated_at ON jurisdiccion;
CREATE TRIGGER trg_jurisdiccion_updated_at
BEFORE UPDATE ON jurisdiccion
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_asunto_updated_at ON asunto;
CREATE TRIGGER trg_asunto_updated_at
BEFORE UPDATE ON asunto
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_proceso_penal_updated_at ON proceso_penal;
CREATE TRIGGER trg_proceso_penal_updated_at
BEFORE UPDATE ON proceso_penal
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_persona_updated_at ON persona;
CREATE TRIGGER trg_persona_updated_at
BEFORE UPDATE ON persona
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

DROP TRIGGER IF EXISTS trg_plantilla_expediente_updated_at ON plantilla_expediente;
CREATE TRIGGER trg_plantilla_expediente_updated_at
BEFORE UPDATE ON plantilla_expediente
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_termino_proceso_updated_at ON termino_proceso;
CREATE TRIGGER trg_termino_proceso_updated_at
BEFORE UPDATE ON termino_proceso
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ====================================
-- ÍNDICE CORREGIDO PARA NOMBRES DE NODOS
-- ====================================

ALTER TABLE node DROP CONSTRAINT IF EXISTS uq_sibling_name;

CREATE UNIQUE INDEX IF NOT EXISTS uq_node_sibling_name_active
ON node (COALESCE(parent_id, '00000000-0000-0000-0000-000000000000'::uuid), name, COALESCE(expediente_id, 0), COALESCE(contable_id, 0), modulo)
WHERE is_deleted = FALSE;


-- ====================================
-- DATOS INICIALES
-- ====================================

-- Roles básicos
INSERT INTO rol (nombre, descripcion) VALUES 
  ('ADMIN', 'Administrador del sistema con todos los permisos'),
  ('ABOGADO', 'Abogado con acceso a gestión de casos y documentos'),
  ('SECRETARIO', 'Secretario con acceso limitado a documentos'),
  ('CONTADOR', 'Contador con acceso a gestión contable'),
  ('CLIENTE', 'Cliente con acceso de solo lectura a sus casos')
ON CONFLICT (nombre) DO NOTHING;

-- Permisos por módulo
INSERT INTO permiso (nombre, descripcion, modulo) VALUES 
  -- Gestión de usuarios
  ('MANAGE_USERS', 'Gestionar usuarios del sistema', 'USUARIOS'),
  
  -- Gestión documental
  ('MANAGE_PROCESOS', 'Gestionar procesos del sistema', 'DOCUMENTAL'),
  ('VIEW_PROCESO', 'Ver procesos', 'DOCUMENTAL'),
  ('MANAGE_EXPEDIENTES', 'Gestionar expedientes del sistema', 'DOCUMENTAL'),
  ('VIEW_EXPEDIENTE', 'Ver expedientes', 'DOCUMENTAL'),
  ('MANAGE_FILES', 'Gestionar archivos del sistema', 'DOCUMENTAL'),
  ('DOWNLOAD_FILE', 'Descargar archivos', 'DOCUMENTAL'),
  
  -- Gestión de plantillas
  ('MANAGE_PLANTILLAS', 'Gestionar plantillas del sistema', 'PLANTILLAS'),
  ('VIEW_PLANTILLA', 'Ver plantillas', 'PLANTILLAS'),
  
  -- Gestión contable
  ('MANAGE_CONTABLES', 'Gestionar documentos contables del sistema', 'CONTABLE'),
  ('VIEW_DOC_CONTABLE', 'Ver documentos contables', 'CONTABLE'),
  
  -- Gestión de sentencias
  ('MANAGE_SENTENCIAS', 'Gestionar sentencias del sistema', 'SENTENCIAS'),
  ('VIEW_SENTENCIA', 'Ver sentencias', 'SENTENCIAS'),
  
  -- Gestión de clientes
   ('MANAGE_CLIENTES', 'Gestionar clientes del sistema', 'CLIENTES'),
  ('VIEW_CLIENTE', 'Ver clientes', 'CLIENTES'),
  
  -- Auditoría
  ('VIEW_AUDIT', 'Ver logs de auditoría', 'AUDITORIA')
ON CONFLICT (nombre) DO NOTHING;

-- Categorías por defecto
INSERT INTO categoria (nombre, descripcion, tipo) VALUES 
  -- Categorías para plantillas
  ('Familia', 'Documentos de derecho de familia', 'PLANTILLA'),
  ('Civil', 'Documentos de derecho civil', 'PLANTILLA'),
  ('Penal', 'Documentos de derecho penal', 'PLANTILLA'),
  ('Laboral', 'Documentos de derecho laboral', 'PLANTILLA'),
  ('Comercial', 'Documentos de derecho comercial', 'PLANTILLA'),
  ('Administrativo', 'Documentos de derecho administrativo', 'PLANTILLA'),
  
  -- Categorías para documentos contables
  ('Declaración', 'Declaraciones tributarias', 'CONTABLE'),
  ('Balance', 'Balances contables', 'CONTABLE'),
  ('Factura', 'Facturas y documentos de venta', 'CONTABLE'),
  ('Comprobante', 'Comprobantes contables', 'CONTABLE'),
  ('Nómina', 'Documentos de nómina', 'CONTABLE'),
  ('Impuestos', 'Documentos tributarios', 'CONTABLE'),
  
  -- Categorías para procesos
  ('Civil', 'Procesos civiles', 'PROCESO'),
  ('Penal', 'Procesos penales', 'PROCESO'),
  ('Laboral', 'Procesos laborales', 'PROCESO'),
  ('Familia', 'Procesos de familia', 'PROCESO'),
  ('Comercial', 'Procesos comerciales', 'PROCESO'),
  ('Administrativo', 'Procesos administrativos', 'PROCESO')
ON CONFLICT (nombre, tipo) DO NOTHING;

-- Tipos de evento básicos
INSERT INTO tipo_evento (nombre, color) VALUES 
  ('Audiencia', '#e74c3c'),
  ('Reunión Cliente', '#3498db'),
  ('Vencimiento', '#f39c12'),
  ('Recordatorio', '#2ecc71'),
  ('Entrega Documentos', '#9b59b6')
ON CONFLICT (nombre) DO NOTHING;

-- Asignar permisos al rol ADMIN (todos los permisos)
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id 
FROM rol r, permiso p 
WHERE r.nombre = 'ADMIN'
ON CONFLICT DO NOTHING;

-- Asignar permisos básicos al rol ABOGADO
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id 
FROM rol r, permiso p 
WHERE r.nombre = 'ABOGADO' 
  AND p.nombre IN (
    'MANAGE_PROCESOS', 'MANAGE_EXPEDIENTES', 'MANAGE_PLANTILLAS', 'MANAGE_FILES',
    'MANAGE_SENTENCIAS', 'MANAGE_CLIENTES'
  )
ON CONFLICT DO NOTHING;

COMMIT;
