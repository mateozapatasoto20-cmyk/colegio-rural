package com.colegiorural.web;

import com.colegiorural.servicio.ReglasService;
import com.colegiorural.servicio.ReglasVigentes;
import com.colegiorural.servicio.ValidacionException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReglasController {
    private final ReglasService reglasService;
    private final ReglasVigentes vigentes;

    public ReglasController(ReglasService reglasService, ReglasVigentes vigentes) { this.reglasService = reglasService; this.vigentes = vigentes; }

    @GetMapping("/reglas")
    public String ver(Model m) {
        m.addAttribute("r", vigentes.actual());
        return "reglas";
    }

    @PostMapping("/reglas")
    public String accion(@RequestParam(defaultValue = "") String accion,
                         @RequestParam(name = "nota_minima", defaultValue = "") String notaMinima,
                         @RequestParam(name = "asistencia_minima", defaultValue = "") String asistenciaMinima,
                         @RequestParam(name = "faltas_seguidas", defaultValue = "") String faltas, HttpServletRequest req) {
        try {
            if ("guardar".equals(accion)) {
                reglasService.guardar(P.decimal(notaMinima, "La nota mínima"), P.decimal(asistenciaMinima, "La asistencia mínima"), P.entero(faltas, "Las faltas seguidas"));
                Sesion.ok(req, "Reglas guardadas. Se aplican desde ahora.");
            }
            if ("recalcular".equals(accion)) {
                int n = reglasService.recalcular();
                Sesion.ok(req, n > 0 ? "Se crearon " + n + (n > 1 ? " alertas" : " alerta") + " nuevas." : "Revisado: no hay alertas nuevas.");
            }
        } catch (ValidacionException e) {
            Sesion.error(req, e.getMessage());
        }
        return Sesion.volver(req);
    }
}
