package com.colegiorural.web;

import com.colegiorural.config.AppProperties;
import com.colegiorural.servicio.CalificacionService;
import com.colegiorural.servicio.EstudianteService;
import com.colegiorural.servicio.ValidacionException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CalificacionController {
    private final CalificacionService calificaciones;
    private final EstudianteService estudiantes;
    private final AppProperties app;

    public CalificacionController(CalificacionService calificaciones, EstudianteService estudiantes, AppProperties app) {
        this.calificaciones = calificaciones; this.estudiantes = estudiantes; this.app = app;
    }

    @GetMapping("/calificaciones")
    public String listar(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "") String curso,
                         @RequestParam(required = false) String riesgo, @RequestParam(defaultValue = "1") String p,
                         @RequestParam(required = false) String nuevo, @RequestParam(required = false) String est, Model m) {
        int pagina = P.pagina(p);
        m.addAttribute("r", calificaciones.listar(P.texto(q), curso, riesgo != null && !riesgo.isEmpty(), pagina, app.porPagina()));
        m.addAttribute("f", Map.of("q", P.texto(q), "curso", curso, "riesgo", riesgo != null && !riesgo.isEmpty()));
        m.addAttribute("pagina", pagina);
        m.addAttribute("porPagina", app.porPagina());
        m.addAttribute("cursos", calificaciones.nombresDeCursos());
        m.addAttribute("estudiantes", estudiantes.activos());
        if (nuevo != null && !m.containsAttribute("abrirDialogo")) {
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("abrir", "dlgNota");
            d.put("titulo", "Registrar nota y asistencia");
            if (est != null && !est.isBlank()) { d.put("datos", Map.of("estudiante_id", P.texto(est))); }
            m.addAttribute("abrirDialogo", d);
        }
        return "calificaciones";
    }

    @PostMapping("/calificaciones")
    public String accion(@RequestParam(defaultValue = "") String accion, @RequestParam(defaultValue = "") String id,
                         @RequestParam(name = "estudiante_id", defaultValue = "") String estudianteId, @RequestParam(defaultValue = "") String curso,
                         @RequestParam(defaultValue = "") String nota, @RequestParam(defaultValue = "") String asistencia,
                         HttpServletRequest req) {
        try {
            if ("guardar".equals(accion)) {
                // Primero se convierte lo escrito en números; si no es un número, ya se rechaza aquí.
                int n = calificaciones.registrar(P.entero(estudianteId, 0), curso, P.decimal(nota, "La nota"), P.decimal(asistencia, "La asistencia"));
                Sesion.ok(req, n > 0 ? "Nota guardada. Se generó " + n + (n > 1 ? " alertas." : " alerta.") : "Nota guardada. No se generaron alertas.");
            } else if ("eliminar".equals(accion)) {
                calificaciones.eliminar(P.entero(id, 0));
                Sesion.ok(req, "Registro eliminado.");
            }
        } catch (ValidacionException e) {
            Map<String, String> datos = new LinkedHashMap<>();
            datos.put("estudiante_id", estudianteId); datos.put("curso", curso); datos.put("nota", nota); datos.put("asistencia", asistencia);
            Sesion.dialogo(req, "dlgNota", "Registrar nota y asistencia", datos, e.getMessage());
        }
        return Sesion.volver(req);
    }
}
