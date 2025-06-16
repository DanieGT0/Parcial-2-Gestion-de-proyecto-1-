package com.app.web.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.web.entidad.Creador;

@Repository
public interface CreadorRepositorio extends JpaRepository<Creador, Long> {

    // 🔧 CORREGIDO: Filtrar por rol (compatible con PostgreSQL)
    @Query("SELECT c FROM Creador c WHERE LOWER(c.rol) LIKE LOWER(CONCAT('%', :rol, '%'))")
    List<Creador> findByRolContainingIgnoreCase(@Param("rol") String rol);
    
    // 🔧 CORREGIDO: Filtrar por proyecto ID (compatible con PostgreSQL)
    @Query("SELECT c FROM Creador c WHERE c.proyecto.id = :proyectoId")
    List<Creador> findByProyectoId(@Param("proyectoId") Long proyectoId);
    
    // 🔧 CORREGIDO: Filtrar por rol Y proyecto (compatible con PostgreSQL)
    @Query("SELECT c FROM Creador c WHERE LOWER(c.rol) LIKE LOWER(CONCAT('%', :rol, '%')) AND c.proyecto.id = :proyectoId")
    List<Creador> findByRolContainingIgnoreCaseAndProyectoId(@Param("rol") String rol, @Param("proyectoId") Long proyectoId);
    
    // ✅ CORRECTO: Obtener todos los roles únicos
    @Query("SELECT DISTINCT c.rol FROM Creador c WHERE c.rol IS NOT NULL ORDER BY c.rol")
    List<String> findDistinctRoles();
    
    // 🔧 CORREGIDO: Buscar por nombre completo (compatible con PostgreSQL)
    @Query("SELECT c FROM Creador c WHERE LOWER(CONCAT(c.nombres, ' ', c.apellidos)) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Creador> findByNombreCompletoContainingIgnoreCase(@Param("nombre") String nombre);
    
    // 🆕 NUEVA IMPLEMENTACIÓN: Filtros combinados SIN parámetros NULL problemáticos
    // Dividimos en múltiples consultas más simples para evitar problemas con PostgreSQL
    
    // Solo filtro por rol
    @Query("SELECT c FROM Creador c WHERE LOWER(c.rol) LIKE LOWER(CONCAT('%', :rol, '%'))")
    List<Creador> findByRolOnly(@Param("rol") String rol);
    
    // Solo filtro por proyecto
    @Query("SELECT c FROM Creador c WHERE c.proyecto.id = :proyectoId")
    List<Creador> findByProyectoOnly(@Param("proyectoId") Long proyectoId);
    
    // Solo filtro por nombre
    @Query("SELECT c FROM Creador c WHERE LOWER(CONCAT(c.nombres, ' ', c.apellidos)) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Creador> findByNombreOnly(@Param("nombre") String nombre);
    
    // Filtro rol + proyecto
    @Query("SELECT c FROM Creador c WHERE LOWER(c.rol) LIKE LOWER(CONCAT('%', :rol, '%')) AND c.proyecto.id = :proyectoId")
    List<Creador> findByRolAndProyecto(@Param("rol") String rol, @Param("proyectoId") Long proyectoId);
    
    // Filtro rol + nombre
    @Query("SELECT c FROM Creador c WHERE LOWER(c.rol) LIKE LOWER(CONCAT('%', :rol, '%')) AND LOWER(CONCAT(c.nombres, ' ', c.apellidos)) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Creador> findByRolAndNombre(@Param("rol") String rol, @Param("nombre") String nombre);
    
    // Filtro proyecto + nombre
    @Query("SELECT c FROM Creador c WHERE c.proyecto.id = :proyectoId AND LOWER(CONCAT(c.nombres, ' ', c.apellidos)) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Creador> findByProyectoAndNombre(@Param("proyectoId") Long proyectoId, @Param("nombre") String nombre);
    
    // Filtro completo: rol + proyecto + nombre
    @Query("SELECT c FROM Creador c WHERE LOWER(c.rol) LIKE LOWER(CONCAT('%', :rol, '%')) AND c.proyecto.id = :proyectoId AND LOWER(CONCAT(c.nombres, ' ', c.apellidos)) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Creador> findByRolAndProyectoAndNombre(@Param("rol") String rol, @Param("proyectoId") Long proyectoId, @Param("nombre") String nombre);
}
