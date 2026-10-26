package com.colegiorural.web;

import com.colegiorural.dominio.SesionUsuario;
import com.colegiorural.servicio.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Un solo lugar decide: ¿hay sesión?, ¿el rol puede entrar a esta ruta?, ¿el formulario trae el token CSRF correcto? */
@Component
public class AccesoInterceptor implements HandlerInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(AccesoInterceptor.class);
    private record Regla(Pattern ruta, List<String> roles) { }

    private static final List<String> TODOS = List.of("admin", "coordinador", "docente", "alumno");
    private static final List<String> STAFF = List.of("admin", "coordinador", "docente");
    private static final List<String> ADMIN_DOCENTE = List.of("admin", "docente");
    private static final List<String> ADMIN_COORD = List.of("admin", "coordinador");
    private static final List<String> SOLO_ADMIN = List.of("admin");

    /** Mismas reglas que tenía la versión PHP en cada página. El orden importa: la primera que coincide gana. */
    private static final List<Regla> REGLAS = List.of(
        new Regla(Pattern.compile("^/dashboard$"), STAFF),
        new Regla(Pattern.compile("^/portal$"), List.of("alumno")),
        new Regla(Pattern.compile("^/estudiantes/\\d+$"), STAFF),
        new Regla(Pattern.compile("^/estudiantes(/.*)?$"), ADMIN_DOCENTE),
        new Regla(Pattern.compile("^/calificaciones$"), ADMIN_DOCENTE),
        new Regla(Pattern.compile("^/asistencia$"), ADMIN_DOCENTE),
        new Regla(Pattern.compile("^/alertas$"), STAFF),
        new Regla(Pattern.compile("^/(docentes|cursos)$"), ADMIN_COORD),
        new Regla(Pattern.compile("^/(usuarios|reglas|respaldo|logs)$"), SOLO_ADMIN),
        new Regla(Pattern.compile("^/reportes(/csv|/excel)?$"), STAFF),
        new Regla(Pattern.compile("^/(perfil|comunicados|boletin(/\\d+)?|logout)$"), TODOS));

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String ruta = req.getRequestURI();
        if ("POST".equals(req.getMethod()) && !tokenValido(req)) {
            LOG.warn("Formulario rechazado por token CSRF inválido: POST {}", ruta);
            res.sendError(419, "La sesión expiró o el formulario no es válido. Vuelve atrás y recarga la página.");
            return false;
        }
        Optional<SesionUsuario> u = Sesion.usuario(req);
        boolean publica = ruta.equals("/") || ruta.equals("/login");
        if (publica) {
            if (u.isPresent() && "GET".equals(req.getMethod())) { res.sendRedirect(AuthService.inicio(u.get().rol())); return false; }
            return true;
        }
        if (u.isEmpty()) { res.sendRedirect("/login"); return false; }
        for (Regla r : REGLAS) {
            if (r.ruta().matcher(ruta).matches()) {
                if (!r.roles().contains(u.get().rol())) {
                    LOG.warn("Acceso denegado: {} ({}) intentó entrar a {}", u.get().username(), u.get().rol(), ruta);
                    res.sendRedirect(AuthService.inicio(u.get().rol()));
                    return false;
                }
                break;
            }
        }
        return true;
    }

    private static boolean tokenValido(HttpServletRequest req) {
        String enviado = req.getParameter("_csrf");
        String esperado = req.getSession(false) == null ? null : (String) req.getSession(false).getAttribute("csrf");
        if (enviado == null || esperado == null) { return false; }
        return MessageDigest.isEqual(enviado.getBytes(StandardCharsets.UTF_8), esperado.getBytes(StandardCharsets.UTF_8));
    }
}
