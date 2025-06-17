// 📁 RUTA: src/main/java/com/app/web/servicio/ReporteServicio.java

package com.app.web.servicio;

import java.io.ByteArrayOutputStream;
import java.util.List;

import com.app.web.entidad.Creador;
import com.app.web.entidad.Proyecto;

/**
 * 🆕 SERVICIO DE REPORTES: Para generar PDF y Excel
 * Maneja la generación de reportes en diferentes formatos
 */
public interface ReporteServicio {
    
    // 📄 Métodos para reportes de PROYECTOS
    byte[] generarReporteProyectosPdf(List<Proyecto> proyectos) throws Exception;
    byte[] generarReporteProyectosExcel(List<Proyecto> proyectos) throws Exception;
    
    // 👥 Métodos para reportes de EQUIPO/CREADORES
    byte[] generarReporteEquipoPdf(List<Creador> creadores) throws Exception;
    byte[] generarReporteEquipoExcel(List<Creador> creadores) throws Exception;
    
    // 📊 Métodos para reportes COMBINADOS (Proyecto + su equipo)
    byte[] generarReporteProyectoConEquipoPdf(Long proyectoId) throws Exception;
    byte[] generarReporteProyectoConEquipoExcel(Long proyectoId) throws Exception;
    
    // 📈 Métodos para reportes de ESTADÍSTICAS
    byte[] generarReporteEstadisticasPdf() throws Exception;
    byte[] generarReporteEstadisticasExcel() throws Exception;
}
