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