package com.app.web.controlador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.app.web.entidad.Creador;
import com.app.web.entidad.Proyecto;
import com.app.web.servicio.CreadorServicio;
import com.app.web.servicio.ProyectoServicio;

@Controller
public class CreadorControlador {
    
    @Autowired
    private CreadorServicio servicio;
    
    @Autowired
    private ProyectoServicio proyectoServicio;
    
    @GetMapping({"/creadores", "/creadores/listar"})
    public String listarCreadores(
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) Long proyectoId,
            @RequestParam(required = false) String nombre,
            Model modelo) {
        
        List<Creador> creadores;
        
        // 🆕 NUEVO: Aplicar filtros si están presentes
        if (rol != null || proyectoId != null || nombre != null) {
            creadores = servicio.filtrarConCriteriosCombinados(rol, proyectoId, nombre);
            System.out.println("🔍 Aplicando filtros - Rol: " + rol + ", Proyecto: " + proyectoId + ", Nombre: " + nombre);
            System.out.println("📊 Resultados filtrados: " + creadores.size() + " creadores");
        } else {
            creadores = servicio.listarTodosLosCreadores();
            System.out.println("📊 Mostrando todos los creadores: " + creadores.size());
        }
        
        // 🔍 DEBUGGING - Agregar logs para verificar los datos
        System.out.println("=== 🔍 DEBUG CREADORES ===");
        System.out.println("📊 Total creadores mostrados: " + creadores.size());
        
        if (creadores.isEmpty()) {
            System.out.println("⚠️  ¡NO HAY CREADORES CON LOS FILTROS APLICADOS!");
        } else {
            System.out.println("✅ Creadores encontrados:");
            for (Creador c : creadores) {
                System.out.println("   - ID: " + c.getId() + 
                                 " | Nombre: " + c.getNombres() + " " + c.getApellidos() + 
                                 " | Email: " + c.getCorreo() + 
                                 " | Rol: " + c.getRol());
                if (c.getProyecto() != null) {
                    System.out.println("     Proyecto: " + c.getProyecto().getTitulo());
                } else {
                    System.out.println("     ⚠️  Sin proyecto asignado");
                }
            }
        }
        System.out.println("========================");
        
        // Agregar datos al modelo
        modelo.addAttribute("creadores", creadores);
        
        // 🆕 NUEVO: Agregar datos para los filtros
        modelo.addAttribute("proyectos", proyectoServicio.listarTodosLosProyectos());
        modelo.addAttribute("rolesUnicos", servicio.obtenerRolesUnicos());
        
        // 🆕 NUEVO: Mantener valores de filtros en el formulario
        modelo.addAttribute("filtroRol", rol);
        modelo.addAttribute("filtroProyectoId", proyectoId);
        modelo.addAttribute("filtroNombre", nombre);
        
        // Calcular estadísticas para el template
        if (creadores != null && !creadores.isEmpty()) {
            // Contar proyectos únicos asignados
            long proyectosUnicos = creadores.stream()
                .filter(c -> c.getProyecto() != null && c.getProyecto().getTitulo() != null)
                .map(c -> c.getProyecto().getTitulo())
                .distinct()
                .count();
            
            // Contar roles únicos
            long rolesUnicosCount = creadores.stream()
                .filter(c -> c.getRol() != null)
                .map(c -> c.getRol())
                .distinct()
                .count();
            
            modelo.addAttribute("proyectosUnicos", proyectosUnicos);
            modelo.addAttribute("rolesUnicosCount", rolesUnicosCount);
            
            System.out.println("📊 Estadísticas calculadas:");
            System.out.println("- Total creadores: " + creadores.size());
            System.out.println("- Proyectos únicos: " + proyectosUnicos);
            System.out.println("- Roles únicos: " + rolesUnicosCount);
        } else {
            modelo.addAttribute("proyectosUnicos", 0);
            modelo.addAttribute("rolesUnicosCount", 0);
            System.out.println("📊 No hay creadores, estadísticas en 0");
        }
        
        return "creadores";
    }
    
    // 🆕 NUEVO: Endpoint AJAX para filtros dinámicos
    @GetMapping("/creadores/filtrar")
    @ResponseBody
    public String filtrarCreadores(
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) Long proyectoId,
            @RequestParam(required = false) String nombre) {
        
        try {
            List<Creador> creadores = servicio.filtrarConCriteriosCombinados(rol, proyectoId, nombre);
            
            // Construir respuesta JSON
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"totalResultados\": ").append(creadores.size()).append(",");
            json.append("\"creadores\": [");
            
            for (int i = 0; i < creadores.size(); i++) {
                Creador c = creadores.get(i);
                if (i > 0) json.append(",");
                
                json.append("{");
                json.append("\"id\": ").append(c.getId()).append(",");
                json.append("\"nombres\": \"").append(escaparJson(c.getNombres())).append("\",");
                json.append("\"apellidos\": \"").append(escaparJson(c.getApellidos())).append("\",");
                json.append("\"nombreCompleto\": \"").append(escaparJson(c.getNombres() + " " + c.getApellidos())).append("\",");
                json.append("\"correo\": \"").append(escaparJson(c.getCorreo())).append("\",");
                json.append("\"telefono\": \"").append(escaparJson(c.getTelefono() != null ? c.getTelefono() : "")).append("\",");
                json.append("\"rol\": \"").append(escaparJson(c.getRol())).append("\",");
                json.append("\"fechaVinculacion\": \"").append(c.getFechaVinculacion() != null ? c.getFechaVinculacion().toString() : "").append("\",");
                json.append("\"proyecto\": ");
                if (c.getProyecto() != null) {
                    json.append("{");
                    json.append("\"id\": ").append(c.getProyecto().getId()).append(",");
                    json.append("\"titulo\": \"").append(escaparJson(c.getProyecto().getTitulo())).append("\"");
                    json.append("}");
                } else {
                    json.append("null");
                }
                json.append(",\"iniciales\": \"").append(obtenerIniciales(c.getNombres(), c.getApellidos())).append("\"");
                json.append("}");
            }
            
            json.append("]}");
            
            System.out.println("🔍 Filtro AJAX - Resultados: " + creadores.size());
            return json.toString();
            
        } catch (Exception e) {
            System.err.println("❌ Error en filtro AJAX: " + e.getMessage());
            e.printStackTrace();
            return "{\"error\": \"" + escaparJson(e.getMessage()) + "\"}";
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
    
    // 🔍 Método de debugging para verificar datos
    @GetMapping("/creadores/debug")
    @ResponseBody
    public String debugCreadores() {
        List<Creador> creadores = servicio.listarTodosLosCreadores();
        StringBuilder sb = new StringBuilder();
        sb.append("=== 🔍 DEBUG CREADORES ===\n");
        sb.append("📊 Total en BD: ").append(creadores.size()).append("\n\n");
        
        if (creadores.isEmpty()) {
            sb.append("⚠️  ¡NO HAY DATOS EN LA TABLA 'creadores'!\n");
            sb.append("Ejecuta este SQL para insertar datos de prueba:\n\n");
            sb.append("INSERT INTO creadores (nombres, apellidos, correo, telefono, rol, proyecto_id, fecha_vinculacion) VALUES\n");
            sb.append("('Ana', 'García', 'ana.garcia@example.com', '70123456', 'Scrum Master', 1, '2024-01-15'),\n");
            sb.append("('Carlos', 'López', 'carlos.lopez@example.com', '70234567', 'Product Owner', 1, '2024-01-10'),\n");
            sb.append("('María', 'Rodríguez', 'maria.rodriguez@example.com', '70345678', 'Developer', 1, '2024-01-20'),\n");
            sb.append("('José', 'Martínez', 'jose.martinez@example.com', '70456789', 'Developer,QA Tester', 2, '2024-02-01'),\n");
            sb.append("('Laura', 'Hernández', 'laura.hernandez@example.com', '70567890', 'QA Tester', 2, '2024-02-05');\n");
        } else {
            for (Creador c : creadores) {
                sb.append("🔹 ID: ").append(c.getId()).append("\n");
                sb.append("   Nombre: ").append(c.getNombres()).append(" ").append(c.getApellidos()).append("\n");
                sb.append("   Email: ").append(c.getCorreo()).append("\n");
                sb.append("   Teléfono: ").append(c.getTelefono()).append("\n");
                sb.append("   Rol: ").append(c.getRol()).append("\n");
                sb.append("   Proyecto: ").append(c.getProyecto() != null ? 
                    c.getProyecto().getTitulo() : "❌ Sin proyecto").append("\n");
                sb.append("   Fecha vinculación: ").append(c.getFechaVinculacion()).append("\n");
                sb.append("---\n");
            }
        }
        
        return sb.toString();
    }
    
    @GetMapping("/creadores/nuevo")
    public String mostrarFormularioDeRegistrarCreador(Model modelo) {
        Creador creador = new Creador();
        modelo.addAttribute("creador", creador);
        modelo.addAttribute("proyectos", proyectoServicio.listarTodosLosProyectos());
        return "crear_creador";
    }
    
    @PostMapping("/creadores")
    public String guardarCreador(@ModelAttribute("creador") Creador creador) {
        System.out.println("💾 Guardando creador: " + creador.getNombres() + " " + creador.getApellidos());
        Creador guardado = servicio.guardarCreador(creador);
        System.out.println("✅ Creador guardado con ID: " + guardado.getId());
        return "redirect:/creadores";
    }
    
    @GetMapping("/creadores/editar/{id}")
    public String mostrarFormularioDeEditar(@PathVariable Long id, Model modelo) {
        Creador creador = servicio.obtenerCreadorPorId(id);
        if (creador != null) {
            modelo.addAttribute("creador", creador);
            modelo.addAttribute("proyectos", proyectoServicio.listarTodosLosProyectos());
            return "editar_creador";
        } else {
            System.out.println("❌ No se encontró creador con ID: " + id);
            return "redirect:/creadores";
        }
    }
    
    @PostMapping("/creadores/editar/{id}")
    public String actualizarCreador(@PathVariable Long id, @ModelAttribute("creador") Creador creador, Model modelo) {
        Creador creadorExistente = servicio.obtenerCreadorPorId(id);
        if (creadorExistente != null) {
            creadorExistente.setId(id);
            creadorExistente.setNombres(creador.getNombres());
            creadorExistente.setApellidos(creador.getApellidos());
            creadorExistente.setCorreo(creador.getCorreo());
            creadorExistente.setTelefono(creador.getTelefono());
            creadorExistente.setRol(creador.getRol());
            creadorExistente.setFechaVinculacion(creador.getFechaVinculacion());
            creadorExistente.setProyecto(creador.getProyecto());
            
            servicio.actualizarCreador(creadorExistente);
            System.out.println("✅ Creador actualizado: " + creadorExistente.getNombres());
        } else {
            System.out.println("❌ No se pudo actualizar, creador no encontrado con ID: " + id);
        }
        return "redirect:/creadores";
    }
    
    @GetMapping("/creadores/eliminar/{id}")
    public String eliminarCreador(@PathVariable Long id) {
        Creador creador = servicio.obtenerCreadorPorId(id);
        if (creador != null) {
            System.out.println("🗑️  Eliminando creador: " + creador.getNombres() + " " + creador.getApellidos());
            servicio.eliminarCreador(id);
            System.out.println("✅ Creador eliminado exitosamente");
        } else {
            System.out.println("❌ No se encontró creador para eliminar con ID: " + id);
        }
        return "redirect:/creadores";
    }
}
