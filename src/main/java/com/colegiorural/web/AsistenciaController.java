package com.colegiorural.web;

import com.colegiorural.dominio.EstadoAsistencia;
import com.colegiorural.servicio.AsistenciaService;
import com.colegiorural.servicio.ReglasVigentes;
import com.colegiorural.servicio.ValidacionException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AsistenciaController {
    private final AsistenciaService asistencia;
    private final ReglasVigentes reglas;

    public AsistenciaController(AsistenciaService asistencia, ReglasVigentes reglas) { this.asistencia = asistencia; this.reglas = reglas; }

    private static LocalDate fecha(String f) {
        if (f == null || f.isBlank()) { return LocalDate.now(); }
        try { return LocalDate.parse(f); } catch (DateTimeParseException e) { throw new ValidacionException("La fecha no es válida."); }
    }

    @GetMapping("/asistencia")
    public String ver(@RequestParam(defaultValue = "") String curso, @RequestParam(defaultValue = "") String fecha, Model m) {
        long cursoId = P.entero(curso, 0);
        m.addAttribute("cursos", asistencia.cursos());
        m.addAttribute("cursoId", cursoId);
        m.addAttribute("hoy", LocalDate.now());
        m.addAttribute("fechaTexto", fecha.isBlank() ? LocalDate.now().toString() : fecha);
        m.addAttribute("estados", EstadoAsistencia.values());
        m.addAttribute("faltasSeguidas", reglas.faltasSeguidas());
        if (cursoId != 0) {
            try { m.addAttribute("p", asistencia.planilla(cursoId, fecha(fecha))); }
            catch (ValidacionException e) { m.addAttribute("error", e.getMessage()); }
        }
        return "asistencia";
    }

    /** Los radios llegan como estado[12]=falta, estado[13]=presente... */
    @PostMapping("/asistencia")
    public String guardar(@RequestParam(defaultValue = "") String curso, @RequestParam(defaultValue = "") String fecha,
                          @RequestParam Map<String, String> todos, HttpServletRequest req) {
        Map<Long, EstadoAsistencia> estados = new LinkedHashMap<>();
        todos.forEach((k, v) -> {
            if (k.startsWith("estado[") && k.endsWith("]")) {
                long id = P.entero(k.substring(7, k.length() - 1), 0);
                EstadoAsistencia e = EstadoAsistencia.desde(v);
                if (id != 0 && e != null) { estados.put(id, e); }
            }
        });
        try {
            int n = asistencia.registrar(P.entero(curso, 0), fecha(fecha), estados);
            Sesion.ok(req, n > 0 ? "Asistencia guardada. Se generó " + n + (n > 1 ? " alertas" : " alerta") + " por faltas." : "Asistencia guardada.");
        } catch (ValidacionException e) {
            Sesion.error(req, e.getMessage());
        }
        return Sesion.volver(req);
    }
}
