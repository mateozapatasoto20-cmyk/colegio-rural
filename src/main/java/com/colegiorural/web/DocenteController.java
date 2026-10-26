package com.colegiorural.web;

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
public class DocenteController {
    private final DocenteService docentes;

    public DocenteController(DocenteService docentes) { this.docentes = docentes; }

    @GetMapping("/docentes")
    public String listar(@RequestParam(defaultValue = "") String q, Model m) {
        m.addAttribute("q", P.texto(q));
        m.addAttribute("lista", docentes.listar(P.texto(q)));
        return "docentes";
    }

    @PostMapping("/docentes")
    public String accion(@RequestParam(defaultValue = "") String accion, @RequestParam(defaultValue = "") String id,
                         @RequestParam(defaultValue = "") String nombres, @RequestParam(defaultValue = "") String apellidos,
                         @RequestParam(defaultValue = "") String especialidad, HttpServletRequest req) {
        try {
            if ("crear".equals(accion)) { docentes.crear(nombres, apellidos, especialidad); Sesion.ok(req, "Docente registrado."); }
            if ("eliminar".equals(accion)) { docentes.eliminar(P.entero(id, 0)); Sesion.ok(req, "Docente eliminado."); }
        } catch (ValidacionException e) {
            Map<String, String> datos = new LinkedHashMap<>();
            datos.put("nombres", nombres); datos.put("apellidos", apellidos); datos.put("especialidad", especialidad);
            Sesion.dialogo(req, "dlgDoc", "Nuevo docente", datos, e.getMessage());
        }
        return Sesion.volver(req);
    }
}
