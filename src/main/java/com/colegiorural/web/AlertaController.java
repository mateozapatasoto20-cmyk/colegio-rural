package com.colegiorural.web;

import com.colegiorural.config.AppProperties;
import com.colegiorural.dao.FiltroAlerta;
import com.colegiorural.servicio.AlertaService;
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

/** Solo el coordinador crea y resuelve alertas; el resto las puede ver. */
@Controller
public class AlertaController {
    private final AlertaService alertas;
    private final EstudianteService estudiantes;
    private final AppProperties app;

    public AlertaController(AlertaService alertas, EstudianteService estudiantes, AppProperties app) { this.alertas = alertas; this.estudiantes = estudiantes; this.app = app; }

    private static boolean esCoordinador(HttpServletRequest req) { return Sesion.usuario(req).map(u -> "coordinador".equals(u.rol())).orElse(false); }

    @GetMapping("/alertas")
    public String listar(@RequestParam(defaultValue = "") String estado, @RequestParam(defaultValue = "") String tipo,
                         @RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "1") String p,
                         HttpServletRequest req, Model m) {
        int pagina = P.pagina(p);
        boolean coord = esCoordinador(req);
        m.addAttribute("r", alertas.listar(new FiltroAlerta(estado, tipo, P.texto(q)), pagina, app.porPagina()));
        m.addAttribute("n", alertas.conteos());
        m.addAttribute("f", Map.of("estado", estado, "tipo", tipo, "q", P.texto(q)));
        m.addAttribute("pagina", pagina);
        m.addAttribute("porPagina", app.porPagina());
        m.addAttribute("esCoord", coord);
        m.addAttribute("estudiantes", coord ? estudiantes.todos() : java.util.List.of());
        return "alertas";
    }

    @PostMapping("/alertas")
    public String accion(@RequestParam(defaultValue = "") String accion, @RequestParam(defaultValue = "") String id,
                         @RequestParam(name = "estudiante_id", defaultValue = "") String estudianteId, @RequestParam(defaultValue = "") String mensaje,
                         HttpServletRequest req) {
        if (!esCoordinador(req)) { return Sesion.volver(req); }
        try {
            if ("resolver".equals(accion)) { alertas.resolver(P.entero(id, 0)); Sesion.ok(req, "Alerta resuelta."); }
            if ("crear".equals(accion)) { alertas.crearManual(P.entero(estudianteId, 0), mensaje); Sesion.ok(req, "Alerta creada."); }
        } catch (ValidacionException e) {
            Map<String, String> datos = new LinkedHashMap<>();
            datos.put("estudiante_id", estudianteId); datos.put("mensaje", mensaje);
            Sesion.dialogo(req, "dlgAlerta", "Nueva alerta manual", datos, e.getMessage());
        }
        return Sesion.volver(req);
    }
}
