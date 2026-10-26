package com.colegiorural.web;

import com.colegiorural.config.AppProperties;
import com.colegiorural.servicio.ReporteService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReporteController {
    private final ReporteService reportes;
    private final AppProperties app;

    public ReporteController(ReporteService reportes, AppProperties app) { this.reportes = reportes; this.app = app; }

    @GetMapping("/reportes")
    public String ver(@RequestParam(defaultValue = "") String grado, @RequestParam(required = false) String riesgo, Model m) {
        var filas = reportes.filas(grado, riesgo != null && !riesgo.isEmpty());
        m.addAttribute("filas", filas);
        m.addAttribute("enRiesgo", filas.stream().filter(ReporteService.FilaReporte::riesgo).count());
        m.addAttribute("f", java.util.Map.of("grado", grado, "riesgo", riesgo != null && !riesgo.isEmpty()));
        m.addAttribute("grados", app.grados());
        return "reportes";
    }

    @GetMapping("/reportes/excel")
    public ResponseEntity<byte[]> excel(@RequestParam(defaultValue = "") String grado) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_academico.xlsx\"")
                .body(reportes.excel(grado));
    }

    @GetMapping("/reportes/csv")
    public ResponseEntity<byte[]> csv(@RequestParam(defaultValue = "") String grado) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=utf-8")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_academico.csv\"")
                .body(reportes.csv(grado));
    }
}
