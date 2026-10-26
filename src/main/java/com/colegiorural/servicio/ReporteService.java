package com.colegiorural.servicio;

import com.colegiorural.dao.AlertaDao;
import com.colegiorural.dao.CalificacionDao;
import com.colegiorural.dao.EstudianteDao;
import com.colegiorural.dao.FiltroEstudiante;
import com.colegiorural.dominio.Estudiante;
import com.colegiorural.dominio.Resumen;
import com.colegiorural.regla.Formato;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class ReporteService {
    public record FilaReporte(Estudiante estudiante, Double promedio, Double asistencia, int alertas, boolean riesgo) {
        public String nombre() { return estudiante.apellidosNombres(); }
    }

    private static final String[] ENCABEZADOS = {"Estudiante", "Grado", "Promedio", "Asistencia (%)", "Alertas activas", "Situación"};

    private final EstudianteDao est;
    private final CalificacionDao cal;
    private final AlertaDao al;
    private final ReglasVigentes reglas;
    private final LogService log;

    public ReporteService(EstudianteDao est, CalificacionDao cal, AlertaDao al, ReglasVigentes reglas, LogService log) {
        this.est = est; this.cal = cal; this.al = al; this.reglas = reglas; this.log = log;
    }

    public List<FilaReporte> filas(String grado, boolean soloRiesgo) {
        Map<Long, Resumen> res = cal.resumenPorEstudiante();
        Map<Long, Integer> alertas = al.conteoActivasPorEstudiante();
        List<FilaReporte> filas = new ArrayList<>();
        for (Estudiante e : est.listar(new FiltroEstudiante("", grado == null ? "" : grado, "", "grado"), 1000, 0)) {
            Resumen r = res.get(e.id());
            Double prom = r == null ? null : r.promedio(), asis = r == null ? null : r.asistencia();
            int n = alertas.getOrDefault(e.id(), 0);
            boolean riesgo = n > 0 || (prom != null && prom < reglas.notaMinima()) || (asis != null && asis < reglas.asistenciaMinima());
            if (!soloRiesgo || riesgo) { filas.add(new FilaReporte(e, prom, asis, n, riesgo)); }
        }
        return filas;
    }

    /** Reporte en CSV con BOM para que Excel muestre bien las tildes. Commons CSV pone las comillas donde hacen falta. */
    public byte[] csv(String grado) {
        StringBuilder sb = new StringBuilder("\uFEFF");
        try (CSVPrinter p = new CSVPrinter(sb, CSVFormat.DEFAULT)) {
            p.printRecord((Object[]) ENCABEZADOS);
            for (FilaReporte f : filas(grado, false)) {
                p.printRecord(f.nombre(), f.estudiante().gradoSeccion(), f.promedio() == null ? "" : Formato.numero(f.promedio()),
                        f.asistencia() == null ? "" : Math.round(f.asistencia()), f.alertas(), f.riesgo() ? "En riesgo" : "Normal");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        log.registrar("Exportó reporte CSV");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /** Reporte en Excel (.xlsx) con Apache POI: encabezado en negrita y las filas en riesgo marcadas en rojo. */
    public byte[] excel(String grado) {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Reporte académico");

            Font negrita = libro.createFont();
            negrita.setBold(true);
            CellStyle estiloCabecera = libro.createCellStyle();
            estiloCabecera.setFont(negrita);
            estiloCabecera.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            estiloCabecera.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Font roja = libro.createFont();
            roja.setColor(IndexedColors.RED.getIndex());
            CellStyle estiloRiesgo = libro.createCellStyle();
            estiloRiesgo.setFont(roja);

            Row cabecera = hoja.createRow(0);
            for (int i = 0; i < ENCABEZADOS.length; i++) {
                Cell c = cabecera.createCell(i);
                c.setCellValue(ENCABEZADOS[i]);
                c.setCellStyle(estiloCabecera);
            }
            int fila = 1;
            for (FilaReporte f : filas(grado, false)) {
                Row r = hoja.createRow(fila++);
                r.createCell(0).setCellValue(f.nombre());
                r.createCell(1).setCellValue(f.estudiante().gradoSeccion());
                if (f.promedio() != null) { r.createCell(2).setCellValue(Math.round(f.promedio() * 10) / 10.0); }
                if (f.asistencia() != null) { r.createCell(3).setCellValue(Math.round(f.asistencia())); }
                r.createCell(4).setCellValue(f.alertas());
                Cell situacion = r.createCell(5);
                situacion.setCellValue(f.riesgo() ? "En riesgo" : "Normal");
                if (f.riesgo()) { situacion.setCellStyle(estiloRiesgo); }
            }
            int[] anchos = {34, 22, 12, 16, 16, 14};
            for (int i = 0; i < anchos.length; i++) { hoja.setColumnWidth(i, anchos[i] * 256); }

            libro.write(salida);
            log.registrar("Exportó reporte Excel");
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
