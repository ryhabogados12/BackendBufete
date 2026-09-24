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


-- Triggers para updated_at
CREATE TRIGGER trg_usuario_updated_at BEFORE UPDATE ON usuario
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    
CREATE TRIGGER trg_cliente_updated_at BEFORE UPDATE ON cliente
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    
CREATE TRIGGER trg_proceso_updated_at BEFORE UPDATE ON proceso
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    
CREATE TRIGGER trg_expediente_updated_at BEFORE UPDATE ON expediente
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    
CREATE TRIGGER trg_documento_contable_updated_at BEFORE UPDATE ON documento_contable
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    
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


CREATE TRIGGER trg_maintain_node_closure
  AFTER INSERT OR UPDATE ON node
  FOR EACH ROW
  EXECUTE FUNCTION maintain_node_closure();

