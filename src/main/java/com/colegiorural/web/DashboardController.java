package com.colegiorural.web;

import com.colegiorural.dominio.SesionUsuario;
import com.colegiorural.servicio.AnuncioService;
import com.colegiorural.servicio.AsistenciaService;
import com.colegiorural.servicio.DashboardService;
import com.colegiorural.servicio.EstudianteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Inicio del personal (admin, coordinador, docente) y portal del alumno. El controlador solo pide datos al servicio y elige la vista. */
@Controller
public class DashboardController {
    private final DashboardService dashboard;
    private final EstudianteService estudiantes;
    private final AsistenciaService asistencia;
    private final AnuncioService anuncios;

    public DashboardController(DashboardService dashboard, EstudianteService estudiantes, AsistenciaService asistencia, AnuncioService anuncios) {
        this.dashboard = dashboard; this.estudiantes = estudiantes; this.asistencia = asistencia; this.anuncios = anuncios;
    }

    @GetMapping("/dashboard")
    public String inicio(HttpServletRequest req, Model m) {
        SesionUsuario u = Sesion.usuario(req).orElseThrow();
        m.addAttribute("d", dashboard.resumen(u.rol()));
        m.addAttribute("puedeNotas", "admin".equals(u.rol()) || "docente".equals(u.rol()));
        return "dashboard";
    }

    @GetMapping("/portal")
    public String portal(HttpServletRequest req, Model m) {
        SesionUsuario u = Sesion.usuario(req).orElseThrow();
        if (u.estudianteId() != null) {
            var ficha = estudiantes.ficha(u.estudianteId());
            if (ficha.isPresent()) {
                m.addAttribute("ficha", ficha.get());
                m.addAttribute("as", asistencia.resumenEstudiante(u.estudianteId()));
            }
        }
        m.addAttribute("coms", anuncios.visibles("alumno", 3));
        return "portal";
    }
}
