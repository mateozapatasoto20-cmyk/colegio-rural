package com.colegiorural.servicio;

import com.colegiorural.dao.RespaldoDao;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Copia de seguridad. La restauración solo acepta archivos hechos por este sistema: cada línea debe ser un DELETE o un
 *  INSERT simple de las tablas conocidas; cualquier otra cosa se rechaza y no se cambia nada. */
@Service
public class RespaldoService {
    private static final Logger LOG = LoggerFactory.getLogger(RespaldoService.class);
    public record Copia(String nombre, byte[] contenido) { }

    private static final String MARCA = "-- COLEGIO-RURAL-RESPALDO v1";
    private static final int MAX_BYTES = 5_000_000;
    private static final String VALOR = "(?:NULL|-?\\d+(?:\\.\\d+)?|\\?)";

    private final RespaldoDao dao;
    private final LogService log;

    public RespaldoService(RespaldoDao dao, LogService log) { this.dao = dao; this.log = log; }

    public Map<String, Integer> conteos() { return dao.conteos(); }

    public Copia exportar() {
        log.registrar("Descargó una copia de seguridad");
        LOG.info("Copia de seguridad descargada");
        StringBuilder sb = new StringBuilder(MARCA).append('\n').append("-- Generado: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append('\n');
        for (String l : dao.volcar()) { sb.append(l).append('\n'); }
        String nombre = "respaldo-colegio-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".sql";
        return new Copia(nombre, sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    public int restaurar(byte[] archivo) {
        if (archivo.length > MAX_BYTES) { throw new ValidacionException("El archivo es demasiado grande (máximo 5 MB)."); }
        String[] lineas = new String(archivo, StandardCharsets.UTF_8).trim().split("\\R");
        if (lineas.length == 0 || !lineas[0].equals(MARCA)) { throw new ValidacionException("Este archivo no es una copia de seguridad de este sistema."); }
        String tablas = String.join("|", dao.tablas());
        Pattern borrar = Pattern.compile("^DELETE FROM (" + tablas + ");$");
        Pattern insertar = Pattern.compile("^INSERT INTO (" + tablas + ") \\([a-z_]+(?:,[a-z_]+)*\\) VALUES \\(" + VALOR + "(?:," + VALOR + ")*\\);$");
        List<String> sentencias = new ArrayList<>();
        boolean hayAdmin = false;
        for (int n = 1; n < lineas.length; n++) {
            String l = lineas[n].trim();
            if (l.isEmpty() || l.startsWith("-- ")) { continue; }
            String esquema = esquema(l);
            if (esquema == null || !(borrar.matcher(esquema).matches() || insertar.matcher(esquema).matches())) {
                throw new ValidacionException("El archivo tiene una línea no permitida (línea " + (n + 1) + "). No se cambió nada.");
            }
            if (l.startsWith("INSERT INTO usuarios") && l.contains("'admin'")) { hayAdmin = true; }
            sentencias.add(l);
        }
        if (sentencias.isEmpty() || !hayAdmin) { throw new ValidacionException("La copia no incluye un usuario administrador. No se cambió nada."); }
        dao.restaurar(sentencias);
        log.registrar("Restauró una copia de seguridad");
        LOG.warn("Copia de seguridad restaurada: {} instrucciones", sentencias.size());
        return sentencias.size();
    }

    /** Cambia cada texto entre comillas por «?» para revisar solo la estructura de la línea (así no se cuela otra sentencia). */
    String esquema(String l) {
        boolean barra = dao.escapaConBarra();
        StringBuilder out = new StringBuilder();
        int i = 0, n = l.length();
        while (i < n) {
            if (l.charAt(i) != '\'') { out.append(l.charAt(i++)); continue; }
            i++;
            boolean cerrado = false;
            while (i < n) {
                char c = l.charAt(i);
                if (barra && c == '\\') { i += 2; continue; }
                if (c == '\'') {
                    if (i + 1 < n && l.charAt(i + 1) == '\'') { i += 2; continue; }
                    cerrado = true; i++; break;
                }
                i++;
            }
            if (!cerrado) { return null; }
            out.append('?');
        }
        return out.toString();
    }
}
