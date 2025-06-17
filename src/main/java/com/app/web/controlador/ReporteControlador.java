// 📁 RUTA: src/main/java/com/app/web/controlador/ReporteControlador.java

package com.app.web.controlador;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.web.entidad.Creador;
import com.app.web.entidad.Proyecto;
import com.app.web.servicio.CreadorServicio;
import com.app.web.servicio.ProyectoServicio;
import com.app.web.servicio.ReporteServicio;

@Controller
@RequestMapping("/reportes")
public class ReporteControlador {

    @Autowired
    private ReporteServicio reporteServicio;
    
    @Autowired
    private ProyectoServicio proyectoServicio;
    
    @Autowired
    private CreadorServicio creadorServicio;

    // 📄 REPORTES DE PROYECTOS

    @GetMapping("/proyectos/pdf")
    public ResponseEntity<byte[]> descargarReporteProyectosPdf() {
        try {
            List<Proyecto> proyectos = proyectoServicio.listarTodosLosProyectos();
            byte[] pdf = reporteServicio.generarReporteProyectosPdf(proyectos);
            
            String filename = "Reporte_Proyectos_" + 
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdf.length);
            
            System.out.println("📄 Generando reporte PDF de proyectos: " + filename);
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            System.err.println("❌ Error al generar reporte PDF de proyectos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/proyectos/excel")
    public ResponseEntity<byte[]> descargarReporteProyectosExcel() {
        try {
            List<Proyecto> proyectos = proyectoServicio.listarTodosLosProyectos();
            byte[] excel = reporteServicio.generarReporteProyectosExcel(proyectos);
            
            String filename = "Reporte_Proyectos_" + 
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excel.length);
            
            System.out.println("📊 Generando reporte Excel de proyectos: " + filename);
            return new ResponseEntity<>(excel, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            System.err.println("❌ Error al generar reporte Excel de proyectos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 👥 REPORTES DE EQUIPO/CREADORES

    @GetMapping("/equipo/pdf")
    public ResponseEntity<byte[]> descargarReporteEquipoPdf(
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) Long proyectoId,
            @RequestParam(required = false) String nombre) {
        try {
            List<Creador> creadores;
            
            // Aplicar filtros si están presentes
            if (rol != null || proyectoId != null || nombre != null) {
                creadores = creadorServicio.filtrarConCriteriosCombinados(rol, proyectoId, nombre);
                System.out.println("🔍 Reporte con filtros - Rol: " + rol + ", Proyecto: " + proyectoId + ", Nombre: " + nombre);
            } else {
                creadores = creadorServicio.listarTodosLosCreadores();
            }
            
            byte[] pdf = reporteServicio.generarReporteEquipoPdf(creadores);
            
            String filtrosStr = construirStringFiltros(rol, proyectoId, nombre);
            String filename = "Reporte_Equipo" + filtrosStr + "_" + 
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdf.length);
            
            System.out.println("👥 Generando reporte PDF de equipo: " + filename);
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            System.err.println("❌ Error al generar reporte PDF de equipo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/equipo/excel")
    public ResponseEntity<byte[]> descargarReporteEquipoExcel(
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) Long proyectoId,
            @RequestParam(required = false) String nombre) {
        try {
            List<Creador> creadores;
            
            // Aplicar filtros si están presentes
            if (rol != null || proyectoId != null || nombre != null) {
                creadores = creadorServicio.filtrarConCriteriosCombinados(rol, proyectoId, nombre);
            } else {
                creadores = creadorServicio.listarTodosLosCreadores();
            }
            
            byte[] excel = reporteServicio.generarReporteEquipoExcel(creadores);
            
            String filtrosStr = construirStringFiltros(rol, proyectoId, nombre);
            String filename = "Reporte_Equipo" + filtrosStr + "_" + 
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excel.length);
            
            System.out.println("👥 Generando reporte Excel de equipo: " + filename);
            return new ResponseEntity<>(excel, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            System.err.println("❌ Error al generar reporte Excel de equipo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 📊 REPORTES DE PROYECTO ESPECÍFICO CON SU EQUIPO

    @GetMapping("/proyecto/{id}/pdf")
    public ResponseEntity<byte[]> descargarReporteProyectoConEquipoPdf(@PathVariable Long id) {
        try {
            byte[] pdf = reporteServicio.generarReporteProyectoConEquipoPdf(id);
            
            // Obtener título del proyecto para el nombre del archivo
            Proyecto proyecto = proyectoServicio.obtenerProyectoPorId(id);
            String tituloLimpio = proyecto.getTitulo().replaceAll("[^a-zA-Z0-9_-]", "_");
            
            String filename = "Reporte_Proyecto_" + tituloLimpio + "_" + 
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdf.length);
            
            System.out.println("📊 Generando reporte PDF de proyecto específico: " + filename);
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            System.err.println("❌ Error al generar reporte PDF de proyecto " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/proyecto/{id}/excel")
    public ResponseEntity<byte[]> descargarReporteProyectoConEquipoExcel(@PathVariable Long id) {
        try {
            byte[] excel = reporteServicio.generarReporteProyectoConEquipoExcel(id);
            
            // Obtener título del proyecto para el nombre del archivo
            Proyecto proyecto = proyectoServicio.obtenerProyectoPorId(id);
            String tituloLimpio = proyecto.getTitulo().replaceAll("[^a-zA-Z0-9_-]", "_");
            
            String filename = "Reporte_Proyecto_" + tituloLimpio + "_" + 
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excel.length);
            
            System.out.println("📊 Generando reporte Excel de proyecto específico: " + filename);
            return new ResponseEntity<>(excel, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            System.err.println("❌ Error al generar reporte Excel de proyecto " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 📈 REPORTES DE ESTADÍSTICAS GENERALES

    @GetMapping("/estadisticas/pdf")
    public ResponseEntity<byte[]> descargarReporteEstadisticasPdf() {
        try {
            byte[] pdf = reporteServicio.generarReporteEstadisticasPdf();
            
            String filename = "Reporte_Estadisticas_" + 
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdf.length);
            
            System.out.println("📈 Generando reporte PDF de estadísticas: " + filename);
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            System.err.println("❌ Error al generar reporte PDF de estadísticas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/estadisticas/excel")
    public ResponseEntity<byte[]> descargarReporteEstadisticasExcel() {
        try {
            byte[] excel = reporteServicio.generarReporteEstadisticasExcel();
            
            String filename = "Reporte_Estadisticas_" + 
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excel.length);
            
            System.out.println("📈 Generando reporte Excel de estadísticas: " + filename);
            return new ResponseEntity<>(excel, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            System.err.println("❌ Error al generar reporte Excel de estadísticas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 🛠️ MÉTODOS AUXILIARES

    /**
     * Construye un string para identificar filtros aplicados en el nombre del archivo
     */
    private String construirStringFiltros(String rol, Long proyectoId, String nombre) {
        StringBuilder filtros = new StringBuilder();
        
        if (rol != null && !rol.trim().isEmpty()) {
            filtros.append("_Rol-").append(rol.replaceAll("[^a-zA-Z0-9]", ""));
        }
        
        if (proyectoId != null) {
            filtros.append("_Proyecto-").append(proyectoId);
        }
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            filtros.append("_Nombre-").append(nombre.replaceAll("[^a-zA-Z0-9]", ""));
        }
        
        return filtros.toString();
    }

    // 🆕 ENDPOINTS ADICIONALES PARA VISTAS DE REPORTES

    /**
     * Endpoint para mostrar vista de selección de reportes
     */
    @GetMapping("/")
    public String mostrarVistaReportes() {
        return "reportes"; // Retorna reportes.html
    }

    /**
     * Endpoint para obtener información de reportes disponibles (AJAX)
     */
    @GetMapping("/info")
    public ResponseEntity<String> obtenerInfoReportes() {
        try {
            int totalProyectos = proyectoServicio.listarTodosLosProyectos().size();
            int totalCreadores = creadorServicio.listarTodosLosCreadores().size();
            
            String info = String.format(
                "{\"totalProyectos\": %d, \"totalCreadores\": %d, \"fecha\": \"%s\"}", 
                totalProyectos, 
                totalCreadores, 
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            );
            
            return ResponseEntity.ok(info);
            
        } catch (Exception e) {
            System.err.println("❌ Error al obtener info de reportes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Error al obtener información\"}");
        }
    }
}
