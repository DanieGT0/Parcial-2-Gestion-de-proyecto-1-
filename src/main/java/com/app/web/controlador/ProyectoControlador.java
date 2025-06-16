package com.app.web.controlador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.app.web.entidad.Creador;
import com.app.web.entidad.Proyecto;
import com.app.web.servicio.CreadorServicio;
import com.app.web.servicio.ProyectoServicio;

@Controller
public class ProyectoControlador {

    @Autowired
    private ProyectoServicio servicio;
    
    // 🆕 NUEVO: Agregar servicio para obtener creadores
    @Autowired
    private CreadorServicio creadorServicio;

    @GetMapping({"/proyectos", "/proyectos/listar"})
    public String listarProyectos(Model modelo) {
        modelo.addAttribute("proyectos", servicio.listarTodosLosProyectos());
        return "proyectos"; // nos retorna al archivo proyectos.html
    }

    // 🆕 NUEVO MÉTODO: Ver equipo de un proyecto específico
    @GetMapping("/proyectos/{id}/equipo")
    @ResponseBody
    public String verEquipoProyecto(@PathVariable Long id) {
        try {
            // Obtener el proyecto
            Proyecto proyecto = servicio.obtenerProyectoPorId(id);
            if (proyecto == null) {
                return "{"
                    + "\"error\": \"Proyecto no encontrado\""
                    + "}";
            }
            
            // Obtener todos los creadores y filtrar por proyecto
            List<Creador> todosCreadores = creadorServicio.listarTodosLosCreadores();
            List<Creador> equipoProyecto = todosCreadores.stream()
                .filter(creador -> creador.getProyecto() != null && 
                                 creador.getProyecto().getId().equals(id))
                .toList();
            
            // Construir respuesta JSON manualmente
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{");
            jsonBuilder.append("\"proyecto\": \"").append(escaparJson(proyecto.getTitulo())).append("\",");
            jsonBuilder.append("\"descripcion\": \"").append(escaparJson(proyecto.getDescripcion())).append("\",");
            jsonBuilder.append("\"tecnologias\": \"").append(escaparJson(proyecto.getTecnologias() != null ? proyecto.getTecnologias() : "")).append("\",");
            jsonBuilder.append("\"fechaPublicacion\": \"").append(proyecto.getFechaPublicacion() != null ? proyecto.getFechaPublicacion().toString() : "").append("\",");
            jsonBuilder.append("\"totalMiembros\": ").append(equipoProyecto.size()).append(",");
            jsonBuilder.append("\"equipo\": [");
            
            for (int i = 0; i < equipoProyecto.size(); i++) {
                Creador creador = equipoProyecto.get(i);
                if (i > 0) jsonBuilder.append(",");
                
                jsonBuilder.append("{");
                jsonBuilder.append("\"id\": ").append(creador.getId()).append(",");
                jsonBuilder.append("\"nombres\": \"").append(escaparJson(creador.getNombres())).append("\",");
                jsonBuilder.append("\"apellidos\": \"").append(escaparJson(creador.getApellidos())).append("\",");
                jsonBuilder.append("\"nombreCompleto\": \"").append(escaparJson(creador.getNombres() + " " + creador.getApellidos())).append("\",");
                jsonBuilder.append("\"correo\": \"").append(escaparJson(creador.getCorreo())).append("\",");
                jsonBuilder.append("\"telefono\": \"").append(escaparJson(creador.getTelefono() != null ? creador.getTelefono() : "")).append("\",");
                jsonBuilder.append("\"rol\": \"").append(escaparJson(creador.getRol())).append("\",");
                jsonBuilder.append("\"fechaVinculacion\": \"").append(creador.getFechaVinculacion() != null ? creador.getFechaVinculacion().toString() : "").append("\",");
                jsonBuilder.append("\"iniciales\": \"").append(obtenerIniciales(creador.getNombres(), creador.getApellidos())).append("\"");
                jsonBuilder.append("}");
            }
            
            jsonBuilder.append("]");
            jsonBuilder.append("}");
            
            // Log para debugging
            System.out.println("🔍 Equipo proyecto ID " + id + ": " + equipoProyecto.size() + " miembros");
            for (Creador c : equipoProyecto) {
                System.out.println("   - " + c.getNombres() + " " + c.getApellidos() + " (" + c.getRol() + ")");
            }
            
            return jsonBuilder.toString();
            
        } catch (Exception e) {
            System.err.println("❌ Error al obtener equipo del proyecto " + id + ": " + e.getMessage());
            e.printStackTrace();
            return "{"
                + "\"error\": \"Error interno del servidor: " + escaparJson(e.getMessage()) + "\""
                + "}";
        }
    }
    
    // 🛠️ Método auxiliar para escapar caracteres especiales en JSON
    private String escaparJson(String texto) {
        if (texto == null) return "";
        return texto
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
    
    // 🛠️ Método auxiliar para obtener iniciales
    private String obtenerIniciales(String nombres, String apellidos) {
        String inicialNombre = (nombres != null && !nombres.isEmpty()) ? 
            nombres.substring(0, 1).toUpperCase() : "";
        String inicialApellido = (apellidos != null && !apellidos.isEmpty()) ? 
            apellidos.substring(0, 1).toUpperCase() : "";
        return inicialNombre + inicialApellido;
    }

    @GetMapping("/proyectos/nuevo")
    public String mostrarFormularioDeRegistrarProyecto(Model modelo) {
        Proyecto proyecto = new Proyecto();
        modelo.addAttribute("proyecto", proyecto);
        return "crear_proyecto";
    }

    @PostMapping("/proyectos")
    public String guardarProyecto(@ModelAttribute("proyecto") Proyecto proyecto) {
        servicio.guardarProyecto(proyecto);
        return "redirect:/proyectos";
    }

    @GetMapping("/proyectos/editar/{id}")
    public String mostrarFormularioDeEditar(@PathVariable Long id, Model modelo) {
        modelo.addAttribute("proyecto", servicio.obtenerProyectoPorId(id));
        return "editar_proyecto";
    }

    @PostMapping("/proyectos/{id}")
    public String actualizarProyecto(@PathVariable Long id, @ModelAttribute("proyecto") Proyecto proyecto,
            Model modelo) {
        Proyecto proyectoExistente = servicio.obtenerProyectoPorId(id);
        proyectoExistente.setId(id);
        proyectoExistente.setTitulo(proyecto.getTitulo());
        proyectoExistente.setDescripcion(proyecto.getDescripcion());
        proyectoExistente.setEnlaceGithub(proyecto.getEnlaceGithub());
        proyectoExistente.setFechaPublicacion(proyecto.getFechaPublicacion());
        proyectoExistente.setTecnologias(proyecto.getTecnologias());

        servicio.actualizarProyecto(proyectoExistente);
        return "redirect:/proyectos";
    }

    @GetMapping("/proyectos/{id}")
    public String eliminarProyecto(@PathVariable Long id) {
        servicio.eliminarProyecto(id);
        return "redirect:/proyectos";
    }
}
