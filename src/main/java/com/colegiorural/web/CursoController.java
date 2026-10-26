package com.colegiorural.web;

import com.colegiorural.config.AppProperties;
import com.colegiorural.servicio.CursoService;
import com.colegiorural.servicio.DocenteService;
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
public class CursoController {
    private final CursoService cursos;
    private final DocenteService docentes;
    private final AppProperties app;

    public CursoController(CursoService cursos, DocenteService docentes, AppProperties app) { this.cursos = cursos; this.docentes = docentes; this.app = app; }

    @GetMapping("/cursos")
    public String listar(Model m) {
        m.addAttribute("cursos", cursos.listar());
        m.addAttribute("docentes", docentes.todos());
        m.addAttribute("grados", app.grados());
        m.addAttribute("secciones", app.secciones());
        m.addAttribute("periodo", app.periodo());
        return "cursos";
    }

    @PostMapping("/cursos")
    public String accion(@RequestParam(defaultValue = "") String accion, @RequestParam(defaultValue = "") String id,
                         @RequestParam(name = "curso_id", defaultValue = "") String cursoId, @RequestParam(name = "docente_id", defaultValue = "") String docenteId,
                         @RequestParam(defaultValue = "") String nombre, @RequestParam(defaultValue = "") String grado,
                         @RequestParam(defaultValue = "") String seccion, @RequestParam(defaultValue = "") String periodo, HttpServletRequest req) {
        try {
            switch (accion) {
                case "crear" -> { cursos.crear(nombre, grado, seccion, periodo); Sesion.ok(req, "Curso creado."); }
                case "asignar" -> { cursos.asignar(P.entero(cursoId, 0), P.entero(docenteId, 0)); Sesion.ok(req, "Docente asignado."); }
                case "eliminar" -> { cursos.eliminar(P.entero(id, 0)); Sesion.ok(req, "Curso eliminado."); }
                default -> { }
            }
        } catch (ValidacionException e) {
            Map<String, String> datos = new LinkedHashMap<>();
            datos.put("nombre", nombre); datos.put("grado", grado); datos.put("seccion", seccion); datos.put("periodo", periodo);
            Sesion.dialogo(req, "dlgCurso", "Nuevo curso", datos, e.getMessage());
        }
        return Sesion.volver(req);
    }
}
