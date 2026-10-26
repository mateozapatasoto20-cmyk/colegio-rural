package com.colegiorural.servicio;

import com.colegiorural.dao.AnuncioDao;
import com.google.common.collect.ImmutableMap;
import com.colegiorural.dominio.Anuncio;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Comunicados del colegio: cada rol ve solo los que le corresponden. */
@Service
public class AnuncioService {
    public static final Map<String, String> AUDIENCIAS = ImmutableMap.of("todos", "Todos", "docentes", "Solo docentes", "alumnos", "Solo estudiantes");

    private static List<String> visiblesPara(String rol) {
        return switch (rol) {
            case "admin", "coordinador" -> List.of("todos", "docentes", "alumnos");
            case "docente" -> List.of("todos", "docentes");
            case "alumno" -> List.of("todos", "alumnos");
            default -> List.of("todos");
        };
    }

    private final AnuncioDao dao;
    private final LogService log;

    public AnuncioService(AnuncioDao dao, LogService log) { this.dao = dao; this.log = log; }

    public List<Anuncio> visibles(String rol, int limite) { return dao.listar(visiblesPara(rol), limite); }

    public void publicar(String titulo, String mensaje, String audiencia, String autor) {
        String t = titulo == null ? "" : titulo.trim(), m = mensaje == null ? "" : mensaje.trim();
        if (t.isEmpty() || t.length() > 80) { throw new ValidacionException("Escribe un título de hasta 80 caracteres."); }
        if (m.isEmpty() || m.length() > 600) { throw new ValidacionException("Escribe un mensaje de hasta 600 caracteres."); }
        if (!AUDIENCIAS.containsKey(audiencia)) { throw new ValidacionException("Elige a quién va dirigido."); }
        dao.crear(t, m, audiencia, autor);
        log.registrar("Publicó comunicado «" + t + "»");
    }

    public void eliminar(long id) { dao.eliminar(id); log.registrar("Eliminó comunicado #" + id); }
}
