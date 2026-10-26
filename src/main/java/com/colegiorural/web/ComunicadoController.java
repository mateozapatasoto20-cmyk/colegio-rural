package com.colegiorural.web;

import com.colegiorural.dominio.SesionUsuario;
import com.colegiorural.servicio.AnuncioService;
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
public class ComunicadoController {
    private final AnuncioService anuncios;

    public ComunicadoController(AnuncioService anuncios) { this.anuncios = anuncios; }

    private static boolean puedePublicar(SesionUsuario u) { return "admin".equals(u.rol()) || "coordinador".equals(u.rol()); }

    @GetMapping("/comunicados")
    public String listar(HttpServletRequest req, Model m) {
        SesionUsuario u = Sesion.usuario(req).orElseThrow();
        m.addAttribute("lista", anuncios.visibles(u.rol(), 50));
        m.addAttribute("puedePublicar", puedePublicar(u));
        m.addAttribute("audiencias", AnuncioService.AUDIENCIAS);
        return "comunicados";
    }

    @PostMapping("/comunicados")
    public String accion(@RequestParam(defaultValue = "") String accion, @RequestParam(defaultValue = "") String id,
                         @RequestParam(defaultValue = "") String titulo, @RequestParam(defaultValue = "") String mensaje,
                         @RequestParam(defaultValue = "") String audiencia, HttpServletRequest req) {
        SesionUsuario u = Sesion.usuario(req).orElseThrow();
        if (!puedePublicar(u)) { return Sesion.volver(req); }
        try {
            if ("publicar".equals(accion)) { anuncios.publicar(titulo, mensaje, audiencia, u.nombre()); Sesion.ok(req, "Comunicado publicado."); }
            if ("eliminar".equals(accion)) { anuncios.eliminar(P.entero(id, 0)); Sesion.ok(req, "Comunicado eliminado."); }
        } catch (ValidacionException e) {
            Map<String, String> datos = new LinkedHashMap<>();
            datos.put("titulo", titulo); datos.put("mensaje", mensaje); datos.put("audiencia", audiencia);
            Sesion.dialogo(req, "dlgCom", "Nuevo comunicado", datos, e.getMessage());
        }
        return Sesion.volver(req);
    }
}
