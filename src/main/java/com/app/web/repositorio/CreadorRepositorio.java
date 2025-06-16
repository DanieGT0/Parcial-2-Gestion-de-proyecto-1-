package com.app.web.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.web.entidad.Creador;

@Repository
public interface CreadorRepositorio extends JpaRepository<Creador, Long> {

    // 🆕 NUEVO: Filtrar por rol (usando LIKE para permitir múltiples roles separados por coma)
    @Query("SELECT c FROM Creador c WHERE LOWER(c.rol) LIKE LOWER(CONCAT('%', :rol, '%'))")
    List<Creador> findByRolContainingIgnoreCase(@Param("rol") String rol);
    
    // 🆕 NUEVO: Filtrar por proyecto ID
    List<Creador> findByProyectoId(Long proyectoId);
    
    // 🆕 NUEVO: Filtrar por rol Y proyecto (combinado)
    @Query("SELECT c FROM Creador c WHERE LOWER(c.rol) LIKE LOWER(CONCAT('%', :rol, '%')) AND c.proyecto.id = :proyectoId")
    List<Creador> findByRolContainingIgnoreCaseAndProyectoId(@Param("rol") String rol, @Param("proyectoId") Long proyectoId);
    
    // 🆕 NUEVO: Obtener todos los roles únicos (para el dropdown de filtros)
    @Query("SELECT DISTINCT c.rol FROM Creador c WHERE c.rol IS NOT NULL ORDER BY c.rol")
    List<String> findDistinctRoles();
    
    // 🆕 NUEVO: Buscar por nombre o apellido (para búsqueda general)
    @Query("SELECT c FROM Creador c WHERE LOWER(CONCAT(c.nombres, ' ', c.apellidos)) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Creador> findByNombreCompletoContainingIgnoreCase(@Param("nombre") String nombre);
    
    // 🆕 NUEVO: Filtros combinados avanzados
    @Query("SELECT c FROM Creador c WHERE " +
           "(:rol IS NULL OR LOWER(c.rol) LIKE LOWER(CONCAT('%', :rol, '%'))) AND " +
           "(:proyectoId IS NULL OR c.proyecto.id = :proyectoId) AND " +
           "(:nombre IS NULL OR LOWER(CONCAT(c.nombres, ' ', c.apellidos)) LIKE LOWER(CONCAT('%', :nombre, '%')))")
    List<Creador> findByFiltrosCombinados(
        @Param("rol") String rol, 
        @Param("proyectoId") Long proyectoId, 
        @Param("nombre") String nombre
    );
}
