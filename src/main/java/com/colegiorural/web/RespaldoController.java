package com.colegiorural.web;

import com.colegiorural.servicio.RespaldoService;
import com.colegiorural.servicio.ValidacionException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class RespaldoController {
    private static final Map<String, String> NOMBRES = new LinkedHashMap<>();
    static {
        NOMBRES.put("estudiantes", "Estudiantes");
        NOMBRES.put("docentes", "Docentes");
        NOMBRES.put("cursos", "Cursos");
        NOMBRES.put("docente_cursos", "Asignaciones docente-curso");
        NOMBRES.put("usuarios", "Usuarios");
        NOMBRES.put("calificaciones", "Calificaciones");
        NOMBRES.put("alertas", "Alertas");
        NOMBRES.put("asistencias", "Registros de asistencia");
        NOMBRES.put("anuncios", "Comunicados");
        NOMBRES.put("configuracion", "Reglas guardadas");
        NOMBRES.put("logs", "Registros de actividad");
    }

    private final RespaldoService respaldo;

    public RespaldoController(RespaldoService respaldo) { this.respaldo = respaldo; }

    @GetMapping("/respaldo")
    public String ver(Model m) {
        m.addAttribute("conteos", respaldo.conteos());
        m.addAttribute("nombres", NOMBRES);
        return "respaldo";
    }

    @PostMapping(value = "/respaldo", params = "accion=descargar")
    public ResponseEntity<byte[]> descargar() {
        var copia = respaldo.exportar();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/sql; charset=utf-8")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + copia.nombre() + "\"")
                .body(copia.contenido());
    }

    @PostMapping(value = "/respaldo", params = "accion=restaurar")
    public String restaurar(@RequestParam(required = false) MultipartFile archivo, HttpServletRequest req) throws IOException {
        try {
            if (archivo == null || archivo.isEmpty()) { throw new ValidacionException("Elige el archivo .sql de la copia de seguridad."); }
            int n = respaldo.restaurar(archivo.getBytes());
            Sesion.ok(req, "Copia restaurada (" + n + " instrucciones). Si no ves los cambios, vuelve a iniciar sesión.");
        } catch (ValidacionException e) {
            Sesion.error(req, e.getMessage());
        }
        return Sesion.volver(req);
    }
}
