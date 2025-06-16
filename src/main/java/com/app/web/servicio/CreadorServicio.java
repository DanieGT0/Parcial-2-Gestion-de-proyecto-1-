package com.app.web.servicio;

import java.util.List;

import com.app.web.entidad.Creador;

public interface CreadorServicio {

    // Métodos existentes
    public List<Creador> listarTodosLosCreadores();
    public Creador guardarCreador(Creador creador);
    public Creador obtenerCreadorPorId(Long id);
    public Creador actualizarCreador(Creador creador);
    public void eliminarCreador(Long id);
    
    // 🆕 NUEVOS MÉTODOS: Para filtros por rol SCRUM y proyecto
    public List<Creador> filtrarPorRol(String rol);
    public List<Creador> filtrarPorProyecto(Long proyectoId);
    public List<Creador> filtrarPorRolYProyecto(String rol, Long proyectoId);
    public List<Creador> buscarPorNombre(String nombre);
    public List<Creador> filtrarConCriteriosCombinados(String rol, Long proyectoId, String nombre);
    
    // 🆕 NUEVOS MÉTODOS: Para obtener datos únicos para los filtros
    public List<String> obtenerRolesUnicos();
}
