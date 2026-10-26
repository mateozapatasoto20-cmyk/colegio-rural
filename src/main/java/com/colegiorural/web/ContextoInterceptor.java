package com.colegiorural.web;

import com.colegiorural.dominio.SesionUsuario;
import com.colegiorural.servicio.AlertaService;
import com.colegiorural.servicio.ReglasVigentes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/** Antes de dibujar cualquier página agrega lo que el menú y la barra superior necesitan. Así ningún controlador lo repite. */
@Component
public class ContextoInterceptor implements HandlerInterceptor {
    private final AlertaService alertas;
    private final ReglasVigentes reglas;

    public ContextoInterceptor(AlertaService alertas, ReglasVigentes reglas) { this.alertas = alertas; this.reglas = reglas; }

    @Override
    public void postHandle(HttpServletRequest req, HttpServletResponse res, Object handler, ModelAndView mv) {
        if (mv == null || mv.getViewName() == null || mv.getViewName().startsWith("redirect:")) { return; }
        Optional<SesionUsuario> u = Sesion.usuario(req);
        mv.addObject("u", u.orElse(null));
        mv.addObject("csrf", Sesion.csrf(req));
        mv.addObject("avisos", Sesion.tomarAvisos(req));
        mv.addObject("ruta", req.getRequestURI());
        Map<String, String> consulta = new LinkedHashMap<>();
        req.getParameterMap().forEach((k, v) -> { if (v.length > 0 && !k.equals("_csrf")) { consulta.put(k, v[0]); } });
        mv.addObject("consulta", consulta);
        Map<String, Object> dialogo = Sesion.tomarDialogo(req);
        if (dialogo != null) { mv.addObject("abrirDialogo", dialogo); mv.addObject("errorDialogo", dialogo.get("error")); }
        mv.addObject("notaMin", reglas.notaMinima());
        mv.addObject("asisMin", reglas.asistenciaMinima());
        if (u.isPresent()) {
            mv.addObject("menu", ItemMenu.para(u.get().rol()));
            mv.addObject("alertasActivas", "alumno".equals(u.get().rol()) ? 0 : alertas.activas());
        }
    }
}
