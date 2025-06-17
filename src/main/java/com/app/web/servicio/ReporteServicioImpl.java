
package com.app.web.servicio;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 📋 IMPORTS SEPARADOS POR LIBRERÍA

// ✅ Apache POI - Solo para Excel
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// ✅ iText - Solo para PDF
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.web.entidad.Creador;
import com.app.web.entidad.Proyecto;

@Service
public class ReporteServicioImpl implements ReporteServicio {

    @Autowired
    private ProyectoServicio proyectoServicio;
    
    @Autowired
    private CreadorServicio creadorServicio;

    // 📄 REPORTES DE PROYECTOS EN PDF
    @Override
    public byte[] generarReporteProyectosPdf(List<Proyecto> proyectos) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Título del reporte
        Paragraph titulo = new Paragraph("📊 REPORTE DE PROYECTOS")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold();
        document.add(titulo);

        // Fecha de generación
        Paragraph fecha = new Paragraph("Fecha de generación: " + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10);
        document.add(fecha);

        // Espacio
        document.add(new Paragraph("\n"));

        // Estadísticas generales
        Paragraph estadisticas = new Paragraph("📈 ESTADÍSTICAS GENERALES")
                .setFontSize(14)
                .setBold();
        document.add(estadisticas);

        document.add(new Paragraph("• Total de proyectos: " + proyectos.size()));
        
        long proyectosConEquipo = proyectos.stream()
                .mapToLong(p -> p.getCreadores() != null ? p.getCreadores().size() : 0)
                .sum();
        
        document.add(new Paragraph("• Total de miembros en equipos: " + proyectosConEquipo));

        // Tabla de proyectos
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2, 2, 1}))
                .setWidth(UnitValue.createPercentValue(100));

        // Headers de la tabla
        table.addHeaderCell(crearCeldaHeaderPdf("ID"));
        table.addHeaderCell(crearCeldaHeaderPdf("Título"));
        table.addHeaderCell(crearCeldaHeaderPdf("Tecnologías"));
        table.addHeaderCell(crearCeldaHeaderPdf("Fecha Publicación"));
        table.addHeaderCell(crearCeldaHeaderPdf("Miembros"));

        // Datos de la tabla
        for (Proyecto proyecto : proyectos) {
            table.addCell(proyecto.getId().toString());
            table.addCell(proyecto.getTitulo());
            table.addCell(proyecto.getTecnologias() != null ? proyecto.getTecnologias() : "N/A");
            table.addCell(proyecto.getFechaPublicacion() != null ? 
                    proyecto.getFechaPublicacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A");
            table.addCell(String.valueOf(proyecto.getCreadores() != null ? proyecto.getCreadores().size() : 0));
        }

        document.add(table);
        document.close();

        return baos.toByteArray();
    }

    // 📊 REPORTES DE PROYECTOS EN EXCEL
    @Override
    public byte[] generarReporteProyectosExcel(List<Proyecto> proyectos) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reporte de Proyectos");

        // Estilos
        CellStyle headerStyle = crearEstiloHeader(workbook);
        CellStyle dataStyle = crearEstiloData(workbook);
        CellStyle titleStyle = crearEstiloTitle(workbook);

        int rowNum = 0;

        // Título principal
        Row titleRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("📊 REPORTE DE PROYECTOS");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        // Fecha
        Row fechaRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell fechaCell = fechaRow.createCell(0);
        fechaCell.setCellValue("Fecha de generación: " + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

        // Espacio
        rowNum++;

        // Estadísticas
        Row statsHeaderRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell statsHeaderCell = statsHeaderRow.createCell(0);
        statsHeaderCell.setCellValue("📈 ESTADÍSTICAS GENERALES");
        statsHeaderCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 4));

        Row statsRow1 = sheet.createRow(rowNum++);
        statsRow1.createCell(0).setCellValue("Total de proyectos:");
        statsRow1.createCell(1).setCellValue(proyectos.size());

        long totalMiembros = proyectos.stream()
                .mapToLong(p -> p.getCreadores() != null ? p.getCreadores().size() : 0)
                .sum();

        Row statsRow2 = sheet.createRow(rowNum++);
        statsRow2.createCell(0).setCellValue("Total de miembros:");
        statsRow2.createCell(1).setCellValue(totalMiembros);

        // Espacio
        rowNum++;

        // Headers de la tabla
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"ID", "Título", "Tecnologías", "Fecha Publicación", "Miembros"};
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Datos de la tabla
        for (Proyecto proyecto : proyectos) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(proyecto.getId());
            row.createCell(1).setCellValue(proyecto.getTitulo());
            row.createCell(2).setCellValue(proyecto.getTecnologias() != null ? proyecto.getTecnologias() : "N/A");
            row.createCell(3).setCellValue(proyecto.getFechaPublicacion() != null ? 
                    proyecto.getFechaPublicacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A");
            row.createCell(4).setCellValue(proyecto.getCreadores() != null ? proyecto.getCreadores().size() : 0);

            // Aplicar estilo a las celdas de datos
            for (int i = 0; i < headers.length; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        // Ajustar ancho de columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return baos.toByteArray();
    }

    // 📊 REPORTES DE EQUIPO EN PDF (adaptado a la interfaz)
    @Override
    public byte[] generarReporteEquipoPdf(List<Creador> creadores) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Título del reporte
        Paragraph titulo = new Paragraph("👥 REPORTE DEL EQUIPO")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold();
        document.add(titulo);

        // Fecha de generación
        Paragraph fecha = new Paragraph("Fecha de generación: " + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10);
        document.add(fecha);

        // Espacio
        document.add(new Paragraph("\n"));

        // Estadísticas del equipo
        Paragraph estadisticas = new Paragraph("📈 ESTADÍSTICAS DEL EQUIPO")
                .setFontSize(14)
                .setBold();
        document.add(estadisticas);

        document.add(new Paragraph("• Total de miembros: " + creadores.size()));
        
        Map<String, Long> rolesCounts = creadores.stream()
                .collect(Collectors.groupingBy(Creador::getRol, Collectors.counting()));
        
        document.add(new Paragraph("• Roles únicos: " + rolesCounts.size()));
        
        long proyectosUnicos = creadores.stream()
                .filter(c -> c.getProyecto() != null)
                .map(c -> c.getProyecto().getId())
                .distinct()
                .count();
        document.add(new Paragraph("• Proyectos con equipo asignado: " + proyectosUnicos));

        // Espacio
        document.add(new Paragraph("\n"));

        // Distribución por roles
        Paragraph rolesTitle = new Paragraph("🏷️ DISTRIBUCIÓN POR ROLES")
                .setFontSize(14)
                .setBold();
        document.add(rolesTitle);

        for (Map.Entry<String, Long> entry : rolesCounts.entrySet()) {
            document.add(new Paragraph("• " + entry.getKey() + ": " + entry.getValue() + " miembro(s)"));
        }

        // Espacio
        document.add(new Paragraph("\n"));

        // Tabla de miembros
        Paragraph tablaTitle = new Paragraph("📋 DETALLE DEL EQUIPO")
                .setFontSize(14)
                .setBold();
        document.add(tablaTitle);

        // Crear tabla con 6 columnas
        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 3, 3, 2, 3, 2}))
                .setWidth(UnitValue.createPercentValue(100));

        // Headers de la tabla
        table.addHeaderCell(crearCeldaHeaderPdf("ID"));
        table.addHeaderCell(crearCeldaHeaderPdf("Nombre Completo"));
        table.addHeaderCell(crearCeldaHeaderPdf("Email"));
        table.addHeaderCell(crearCeldaHeaderPdf("Rol"));
        table.addHeaderCell(crearCeldaHeaderPdf("Proyecto"));
        table.addHeaderCell(crearCeldaHeaderPdf("Fecha Ing."));

        // Datos de la tabla
        for (Creador creador : creadores) {
            table.addCell(creador.getId().toString());
            table.addCell(creador.getNombres() + " " + creador.getApellidos());
            table.addCell(creador.getCorreo());
            table.addCell(creador.getRol());
            table.addCell(creador.getProyecto() != null ? creador.getProyecto().getTitulo() : "Sin asignar");
            table.addCell(creador.getFechaVinculacion() != null ? 
                    creador.getFechaVinculacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A");
        }

        document.add(table);
        document.close();

        return baos.toByteArray();
    }

    // 📊 REPORTES DE EQUIPO EN EXCEL (adaptado a la interfaz)
    @Override
    public byte[] generarReporteEquipoExcel(List<Creador> creadores) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reporte del Equipo");

        CellStyle headerStyle = crearEstiloHeader(workbook);
        CellStyle dataStyle = crearEstiloData(workbook);
        CellStyle titleStyle = crearEstiloTitle(workbook);

        int rowNum = 0;

        // Título principal
        Row titleRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("👥 REPORTE DEL EQUIPO");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        // Fecha
        Row fechaRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell fechaCell = fechaRow.createCell(0);
        fechaCell.setCellValue("Fecha de generación: " + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

        // Espacio
        rowNum++;

        // Estadísticas
        Row statsHeaderRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell statsHeaderCell = statsHeaderRow.createCell(0);
        statsHeaderCell.setCellValue("📈 ESTADÍSTICAS DEL EQUIPO");
        statsHeaderCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 5));

        Row statsRow1 = sheet.createRow(rowNum++);
        statsRow1.createCell(0).setCellValue("Total de miembros:");
        statsRow1.createCell(1).setCellValue(creadores.size());

        // Distribución por roles
        Map<String, Long> roleStats = creadores.stream()
                .collect(Collectors.groupingBy(Creador::getRol, Collectors.counting()));

        Row rolesHeaderRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell rolesHeaderCell = rolesHeaderRow.createCell(0);
        rolesHeaderCell.setCellValue("🏷️ DISTRIBUCIÓN POR ROLES");
        rolesHeaderCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 5));

        for (Map.Entry<String, Long> entry : roleStats.entrySet()) {
            Row roleRow = sheet.createRow(rowNum++);
            roleRow.createCell(0).setCellValue(entry.getKey() + ":");
            roleRow.createCell(1).setCellValue(entry.getValue());
        }

        // Espacio
        rowNum++;

        // Headers de la tabla
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"ID", "Nombre Completo", "Email", "Rol", "Proyecto", "Fecha Ing."};
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Datos de la tabla
        for (Creador creador : creadores) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(creador.getId());
            row.createCell(1).setCellValue(creador.getNombres() + " " + creador.getApellidos());
            row.createCell(2).setCellValue(creador.getCorreo());
            row.createCell(3).setCellValue(creador.getRol());
            row.createCell(4).setCellValue(creador.getProyecto() != null ? creador.getProyecto().getTitulo() : "Sin asignar");
            row.createCell(5).setCellValue(creador.getFechaVinculacion() != null ? 
                    creador.getFechaVinculacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A");

            // Aplicar estilo a las celdas de datos
            for (int i = 0; i < headers.length; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        // Ajustar ancho de columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return baos.toByteArray();
    }

    // 📊 REPORTE DE PROYECTO CON SU EQUIPO (PDF)
    @Override
    public byte[] generarReporteProyectoConEquipoPdf(Long proyectoId) throws Exception {
        Proyecto proyecto = proyectoServicio.obtenerProyectoPorId(proyectoId);
        if (proyecto == null) {
            throw new Exception("Proyecto no encontrado con ID: " + proyectoId);
        }

        List<Creador> equipo = creadorServicio.filtrarPorProyecto(proyectoId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Título del reporte
        Paragraph titulo = new Paragraph("📊 REPORTE: " + proyecto.getTitulo().toUpperCase())
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold();
        document.add(titulo);

        // Fecha de generación
        Paragraph fecha = new Paragraph("Fecha de generación: " + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10);
        document.add(fecha);

        // Espacio
        document.add(new Paragraph("\n"));

        // Información del proyecto
        Paragraph infoTitle = new Paragraph("📋 INFORMACIÓN DEL PROYECTO")
                .setFontSize(14)
                .setBold();
        document.add(infoTitle);

        document.add(new Paragraph("• ID: " + proyecto.getId()));
        document.add(new Paragraph("• Título: " + proyecto.getTitulo()));
        document.add(new Paragraph("• Descripción: " + proyecto.getDescripcion()));
        document.add(new Paragraph("• Tecnologías: " + (proyecto.getTecnologias() != null ? proyecto.getTecnologias() : "N/A")));
        document.add(new Paragraph("• Fecha publicación: " + (proyecto.getFechaPublicacion() != null ?
                proyecto.getFechaPublicacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A")));
        document.add(new Paragraph("• GitHub: " + (proyecto.getEnlaceGithub() != null ? proyecto.getEnlaceGithub() : "N/A")));

        // Espacio
        document.add(new Paragraph("\n"));

        // Información del equipo
        Paragraph equipoTitle = new Paragraph("👥 EQUIPO ASIGNADO (" + equipo.size() + " miembros)")
                .setFontSize(14)
                .setBold();
        document.add(equipoTitle);

        if (equipo.isEmpty()) {
            document.add(new Paragraph("❌ No hay miembros asignados a este proyecto."));
        } else {
            // Tabla del equipo
            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 4, 4, 3, 2}))
                    .setWidth(UnitValue.createPercentValue(100));

            // Headers
            table.addHeaderCell(crearCeldaHeaderPdf("ID"));
            table.addHeaderCell(crearCeldaHeaderPdf("Nombre Completo"));
            table.addHeaderCell(crearCeldaHeaderPdf("Email"));
            table.addHeaderCell(crearCeldaHeaderPdf("Rol"));
            table.addHeaderCell(crearCeldaHeaderPdf("Fecha Ing."));

            // Datos del equipo
            for (Creador creador : equipo) {
                table.addCell(creador.getId().toString());
                table.addCell(creador.getNombres() + " " + creador.getApellidos());
                table.addCell(creador.getCorreo());
                table.addCell(creador.getRol());
                table.addCell(creador.getFechaVinculacion() != null ? 
                        creador.getFechaVinculacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A");
            }

            document.add(table);
        }

        document.close();
        return baos.toByteArray();
    }

    // 📊 REPORTE DE PROYECTO CON SU EQUIPO (EXCEL)
    @Override
    public byte[] generarReporteProyectoConEquipoExcel(Long proyectoId) throws Exception {
        Proyecto proyecto = proyectoServicio.obtenerProyectoPorId(proyectoId);
        if (proyecto == null) {
            throw new Exception("Proyecto no encontrado con ID: " + proyectoId);
        }

        List<Creador> equipo = creadorServicio.filtrarPorProyecto(proyectoId);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Proyecto - " + proyecto.getTitulo());

        CellStyle headerStyle = crearEstiloHeader(workbook);
        CellStyle dataStyle = crearEstiloData(workbook);
        CellStyle titleStyle = crearEstiloTitle(workbook);

        int rowNum = 0;

        // Título principal
        Row titleRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("📊 REPORTE: " + proyecto.getTitulo().toUpperCase());
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        // Fecha
        Row fechaRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell fechaCell = fechaRow.createCell(0);
        fechaCell.setCellValue("Fecha de generación: " + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

        // Espacio
        rowNum++;

        // Información del proyecto
        Row infoHeaderRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell infoHeaderCell = infoHeaderRow.createCell(0);
        infoHeaderCell.setCellValue("📋 INFORMACIÓN DEL PROYECTO");
        infoHeaderCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 4));

        // Detalles del proyecto
        String[][] proyectoInfo = {
            {"ID:", proyecto.getId().toString()},
            {"Título:", proyecto.getTitulo()},
            {"Descripción:", proyecto.getDescripcion()},
            {"Tecnologías:", proyecto.getTecnologias() != null ? proyecto.getTecnologias() : "N/A"},
            {"Fecha publicación:", proyecto.getFechaPublicacion() != null ? 
                proyecto.getFechaPublicacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A"},
            {"GitHub:", proyecto.getEnlaceGithub() != null ? proyecto.getEnlaceGithub() : "N/A"}
        };

        for (String[] info : proyectoInfo) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(info[0]);
            row.createCell(1).setCellValue(info[1]);
        }

        // Espacio
        rowNum++;

        // Información del equipo
        Row equipoHeaderRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell equipoHeaderCell = equipoHeaderRow.createCell(0);
        equipoHeaderCell.setCellValue("👥 EQUIPO ASIGNADO (" + equipo.size() + " miembros)");
        equipoHeaderCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 4));

        if (equipo.isEmpty()) {
            Row emptyRow = sheet.createRow(rowNum++);
            emptyRow.createCell(0).setCellValue("❌ No hay miembros asignados a este proyecto.");
            sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 4));
        } else {
            // Headers del equipo
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"ID", "Nombre Completo", "Email", "Rol", "Fecha Ing."};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos del equipo
            for (Creador creador : equipo) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(creador.getId());
                row.createCell(1).setCellValue(creador.getNombres() + " " + creador.getApellidos());
                row.createCell(2).setCellValue(creador.getCorreo());
                row.createCell(3).setCellValue(creador.getRol());
                row.createCell(4).setCellValue(creador.getFechaVinculacion() != null ?
                        creador.getFechaVinculacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A");

                // Aplicar estilo a las celdas de datos
                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
        }

        // Ajustar ancho de columnas
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return baos.toByteArray();
    }

    // 📊 REPORTE DE ESTADÍSTICAS (PDF)
    @Override
    public byte[] generarReporteEstadisticasPdf() throws Exception {
        List<Proyecto> proyectos = proyectoServicio.listarTodosLosProyectos();
        List<Creador> creadores = creadorServicio.listarTodosLosCreadores();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Título del reporte
        Paragraph titulo = new Paragraph("📈 REPORTE DE ESTADÍSTICAS")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold();
        document.add(titulo);

        // Fecha de generación
        Paragraph fecha = new Paragraph("Fecha de generación: " + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10);
        document.add(fecha);

        // Espacio
        document.add(new Paragraph("\n"));

        // Estadísticas generales
        Paragraph estadisticas = new Paragraph("📊 ESTADÍSTICAS GENERALES")
                .setFontSize(14)
                .setBold();
        document.add(estadisticas);

        document.add(new Paragraph("• Total de proyectos: " + proyectos.size()));
        document.add(new Paragraph("• Total de creadores: " + creadores.size()));

        long totalMiembros = proyectos.stream()
                .mapToLong(p -> p.getCreadores() != null ? p.getCreadores().size() : 0)
                .sum();
        document.add(new Paragraph("• Total de asignaciones: " + totalMiembros));

        double promedio = proyectos.isEmpty() ? 0 : (double) totalMiembros / proyectos.size();
        document.add(new Paragraph("• Promedio de miembros por proyecto: " + String.format("%.1f", promedio)));

        // Espacio
        document.add(new Paragraph("\n"));

        // Distribución por roles
        Map<String, Long> roleStats = creadores.stream()
                .collect(Collectors.groupingBy(Creador::getRol, Collectors.counting()));

        Paragraph rolesTitle = new Paragraph("👨‍💼 DISTRIBUCIÓN POR ROLES")
                .setFontSize(14)
                .setBold();
        document.add(rolesTitle);

        Table rolesTable = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100));

        rolesTable.addHeaderCell(crearCeldaHeaderPdf("Rol"));
        rolesTable.addHeaderCell(crearCeldaHeaderPdf("Cantidad"));
        rolesTable.addHeaderCell(crearCeldaHeaderPdf("Porcentaje"));

        for (Map.Entry<String, Long> entry : roleStats.entrySet()) {
            rolesTable.addCell(entry.getKey());
            rolesTable.addCell(entry.getValue().toString());
            double percentage = (entry.getValue() * 100.0) / creadores.size();
            rolesTable.addCell(String.format("%.1f%%", percentage));
        }

        document.add(rolesTable);
        document.close();

        return baos.toByteArray();
    }

    // 📊 REPORTE DE ESTADÍSTICAS (EXCEL)
    @Override
    public byte[] generarReporteEstadisticasExcel() throws Exception {
        List<Proyecto> proyectos = proyectoServicio.listarTodosLosProyectos();
        List<Creador> creadores = creadorServicio.listarTodosLosCreadores();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Estadísticas");

        CellStyle headerStyle = crearEstiloHeader(workbook);
        CellStyle dataStyle = crearEstiloData(workbook);
        CellStyle titleStyle = crearEstiloTitle(workbook);

        int rowNum = 0;

        // Título principal
        Row titleRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("📈 REPORTE DE ESTADÍSTICAS");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        // Fecha
        Row fechaRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell fechaCell = fechaRow.createCell(0);
        fechaCell.setCellValue("Fecha de generación: " + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

        // Espacio
        rowNum++;

        // Estadísticas generales
        Row generalHeaderRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell generalHeaderCell = generalHeaderRow.createCell(0);
        generalHeaderCell.setCellValue("📊 ESTADÍSTICAS GENERALES");
        generalHeaderCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 4));

        // Datos estadísticos
        String[][] stats = {
            {"Total de proyectos:", String.valueOf(proyectos.size())},
            {"Total de creadores:", String.valueOf(creadores.size())},
            {"Total de asignaciones:", String.valueOf(proyectos.stream()
                    .mapToLong(p -> p.getCreadores() != null ? p.getCreadores().size() : 0).sum())},
            {"Promedio miembros/proyecto:", String.format("%.1f", 
                    proyectos.isEmpty() ? 0 : 
                    (double) proyectos.stream()
                            .mapToLong(p -> p.getCreadores() != null ? p.getCreadores().size() : 0)
                            .sum() / proyectos.size())}
        };

        for (String[] stat : stats) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(stat[0]);
            row.createCell(1).setCellValue(stat[1]);
        }

        // Espacio
        rowNum++;

        // Distribución por roles
        Map<String, Long> roleStats = creadores.stream()
                .collect(Collectors.groupingBy(Creador::getRol, Collectors.counting()));

        Row rolesHeaderRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell rolesHeaderCell = rolesHeaderRow.createCell(0);
        rolesHeaderCell.setCellValue("👨‍💼 DISTRIBUCIÓN POR ROLES");
        rolesHeaderCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 4));

        // Headers de roles
        Row roleHeaderRow = sheet.createRow(rowNum++);
        String[] roleHeaders = {"Rol", "Cantidad", "Porcentaje"};
        for (int i = 0; i < roleHeaders.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = roleHeaderRow.createCell(i);
            cell.setCellValue(roleHeaders[i]);
            cell.setCellStyle(headerStyle);
        }

        // Datos de roles
        for (Map.Entry<String, Long> entry : roleStats.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
            double percentage = (entry.getValue() * 100.0) / creadores.size();
            row.createCell(2).setCellValue(String.format("%.1f%%", percentage));

            // Aplicar estilo
            for (int i = 0; i < 3; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        // Ajustar ancho de columnas
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return baos.toByteArray();
    }

    // 🛠️ MÉTODOS AUXILIARES PARA ESTILOS

    // ✅ Método para crear celdas de header en PDF (iText)
    private com.itextpdf.layout.element.Cell crearCeldaHeaderPdf(String text) {
        com.itextpdf.layout.element.Cell cell = new com.itextpdf.layout.element.Cell();
        cell.add(new Paragraph(text).setBold().setFontSize(10));
        cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
        cell.setTextAlignment(TextAlignment.CENTER);
        return cell;
    }

    // ✅ Métodos para crear estilos en Excel (Apache POI)
    private CellStyle crearEstiloHeader(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle crearEstiloData(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle crearEstiloTitle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}
