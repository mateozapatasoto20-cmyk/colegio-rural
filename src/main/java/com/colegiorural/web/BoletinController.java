package com.colegiorural.web;

import com.colegiorural.config.AppProperties;
import com.colegiorural.dominio.SesionUsuario;
import com.colegiorural.servicio.BoletinService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class BoletinController {
    private final BoletinService boletines;
    private final AppProperties app;

    public BoletinController(BoletinService boletines, AppProperties app) { this.boletines = boletines; this.app = app; }

    /** El alumno entra a /boletin y solo ve el suyo. */
    @GetMapping("/boletin")
    public String propio(HttpServletRequest req, Model m) {
        SesionUsuario u = Sesion.usuario(req).orElseThrow();
        long id = u.estudianteId() == null ? 0 : u.estudianteId();
        return mostrar(id, u, req, m);
    }

    @GetMapping("/boletin/{id:\\d+}")
    public String de(@PathVariable long id, HttpServletRequest req, Model m) {
        SesionUsuario u = Sesion.usuario(req).orElseThrow();
        // Un alumno nunca puede pedir el boletín de otro: se ignora el id de la URL.
        long real = "alumno".equals(u.rol()) ? (u.estudianteId() == null ? 0 : u.estudianteId()) : id;
        return mostrar(real, u, req, m);
    }

    private String mostrar(long id, SesionUsuario u, HttpServletRequest req, Model m) {
        Optional<BoletinService.Boletin> b = boletines.de(id);
        if (b.isEmpty()) {
            Sesion.error(req, "No se encontró al estudiante.");
            return "redirect:" + ("alumno".equals(u.rol()) ? "/portal" : "/dashboard");
        }
        m.addAttribute("b", b.get());
        m.addAttribute("periodo", app.periodo());
        m.addAttribute("hoy", LocalDate.now());
        m.addAttribute("activa", "alumno".equals(u.rol()) ? "/portal" : "/estudiantes");
        return "boletin";
    }
}
