package com.bufete.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bufete.backend.model.Node;

@Repository
public interface NodeRepository extends JpaRepository<Node, UUID>, JpaSpecificationExecutor<Node> {
    
    @Query("SELECT n FROM Node n WHERE n.parent.id = :parentId AND n.isDeleted = false ORDER BY n.type, n.name")
    List<Node> findByParentIdAndIsDeletedFalse(@Param("parentId") UUID parentId);
    
    @Query("SELECT n FROM Node n WHERE n.expediente.id = :expedienteId AND n.parent IS NULL AND n.isDeleted = false")
    Optional<Node> findRootNodeByExpedienteId(@Param("expedienteId") Long expedienteId);

    List<Node> findByExpedienteIdAndIsDeletedFalseOrderByParentIdAscNameAsc(Long expedienteId);
    
    @Query("SELECT n FROM Node n WHERE n.contable.id = :contableId AND n.parent IS NULL AND n.isDeleted = false")
    Optional<Node> findRootNodeByContableId(@Param("contableId") Long contableId);
    
    @Query("SELECT n FROM Node n WHERE n.modulo = :modulo AND n.parent IS NULL AND n.expediente IS NULL AND n.contable IS NULL AND n.isDeleted = false")
    List<Node> findRootNodesByModulo(@Param("modulo") Node.Modulo modulo);
    
    @Query("SELECT COUNT(n) FROM Node n WHERE n.parent.id = :parentId AND n.isDeleted = false")
    int countChildrenByParentId(@Param("parentId") UUID parentId);
    
    @Query("SELECT COALESCE(SUM(n.sizeBytes), 0) FROM Node n WHERE n.parent.id = :parentId AND n.isDeleted = false")
    long getTotalSizeByParentId(@Param("parentId") UUID parentId);
    
    @Query("SELECT n FROM Node n WHERE n.name = :name AND n.parent.id = :parentId AND n.isDeleted = false")
    Optional<Node> findByNameAndParentId(@Param("name") String name, @Param("parentId") UUID parentId);

    Node findByName(String name);

    // Consulta para obtener los nombres de los ancestros ordenados por profundidad descendente
    @Query("SELECT ancestor.name FROM NodeClosure nc " +
           "JOIN nc.ancestor ancestor " +
           "WHERE nc.descendant.id = :nodeId AND nc.depth > 0 " +
           "ORDER BY nc.depth DESC")
    String getNodePath(@Param("nodeId") UUID nodeId);
}