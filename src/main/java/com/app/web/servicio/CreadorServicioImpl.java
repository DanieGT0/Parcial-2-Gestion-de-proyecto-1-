package com.app.web.servicio;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.web.entidad.Creador;
import com.app.web.repositorio.CreadorRepositorio;

@Service
public class CreadorServicioImpl implements CreadorServicio {

    @Autowired
    private CreadorRepositorio repositorio;

    // Métodos existentes
    @Override
    public List<Creador> listarTodosLosCreadores() {
        return repositorio.findAll();
    }

    @Override
    public Creador guardarCreador(Creador creador) {
        return repositorio.save(creador);
    }

    @Override
    public Creador obtenerCreadorPorId(Long id) {
        return repositorio.findById(id).orElse(null);
    }

    @Override
    public Creador actualizarCreador(Creador creador) {
        return repositorio.save(creador);
    }

    @Override
    public void eliminarCreador(Long id) {
        repositorio.deleteById(id);
    }

    // 🔧 CORREGIDOS: Métodos de filtro para PostgreSQL
    @Override
    public List<Creador> filtrarPorRol(String rol) {
        if (rol == null || rol.trim().isEmpty()) {
            return listarTodosLosCreadores();
        }
        return repositorio.findByRolContainingIgnoreCase(rol.trim());
    }

    @Override
    public List<Creador> filtrarPorProyecto(Long proyectoId) {
        if (proyectoId == null) {
            return listarTodosLosCreadores();
        }
        return repositorio.findByProyectoId(proyectoId);
    }

    @Override
    public List<Creador> filtrarPorRolYProyecto(String rol, Long proyectoId) {
        if ((rol == null || rol.trim().isEmpty()) && proyectoId == null) {
            return listarTodosLosCreadores();
        }
        
        if (rol == null || rol.trim().isEmpty()) {
            return filtrarPorProyecto(proyectoId);
        }
        
        if (proyectoId == null) {
            return filtrarPorRol(rol);
        }
        
        return repositorio.findByRolContainingIgnoreCaseAndProyectoId(rol.trim(), proyectoId);
    }

    @Override
    public List<Creador> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return listarTodosLosCreadores();
        }
        return repositorio.findByNombreCompletoContainingIgnoreCase(nombre.trim());
    }

    // 🆕 NUEVA IMPLEMENTACIÓN: Lógica mejorada para filtros combinados sin problemas de NULL
    @Override
    public List<Creador> filtrarConCriteriosCombinados(String rol, Long proyectoId, String nombre) {
        // Normalizar parámetros
        boolean tieneRol = rol != null && !rol.trim().isEmpty();
        boolean tieneProyecto = proyectoId != null;
        boolean tieneNombre = nombre != null && !nombre.trim().isEmpty();
        
        // Si no hay filtros, devolver todos
        if (!tieneRol && !tieneProyecto && !tieneNombre) {
            return listarTodosLosCreadores();
        }
        
        // Filtros individuales
        if (tieneRol && !tieneProyecto && !tieneNombre) {
            return repositorio.findByRolOnly(rol.trim());
        }
        
        if (!tieneRol && tieneProyecto && !tieneNombre) {
            return repositorio.findByProyectoOnly(proyectoId);
        }
        
        if (!tieneRol && !tieneProyecto && tieneNombre) {
            return repositorio.findByNombreOnly(nombre.trim());
        }
        
        // Filtros combinados de dos
        if (tieneRol && tieneProyecto && !tieneNombre) {
            return repositorio.findByRolAndProyecto(rol.trim(), proyectoId);
        }
        
        if (tieneRol && !tieneProyecto && tieneNombre) {
            return repositorio.findByRolAndNombre(rol.trim(), nombre.trim());
        }
        
        if (!tieneRol && tieneProyecto && tieneNombre) {
            return repositorio.findByProyectoAndNombre(proyectoId, nombre.trim());
        }
        
        // Filtro completo (todos los tres)
        if (tieneRol && tieneProyecto && tieneNombre) {
            return repositorio.findByRolAndProyectoAndNombre(rol.trim(), proyectoId, nombre.trim());
        }
        
        // Fallback (no debería llegar aquí)
        return listarTodosLosCreadores();
    }

    @Override
    public List<String> obtenerRolesUnicos() {
        List<String> rolesRaw = repositorio.findDistinctRoles();
        
        // Procesar roles que pueden estar separados por comas
        return rolesRaw.stream()
            .filter(rol -> rol != null && !rol.trim().isEmpty())
            .flatMap(rol -> {
                // Separar roles por coma y limpiar espacios
                String[] rolesSeparados = rol.split(",");
                return java.util.Arrays.stream(rolesSeparados)
                    .map(String::trim)
                    .filter(r -> !r.isEmpty());
            })
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
}
