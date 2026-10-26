package com.colegiorural.web;

import com.colegiorural.dominio.Barra;
import com.colegiorural.dominio.EstadoAsistencia;
import com.colegiorural.regla.Formato;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Pequeños ayudantes de presentación que las plantillas usan como ${@vista.xxx(...)}. No tienen reglas de negocio. */
@Component("vista")
public class Vista {
    private static final DateTimeFormatter DMA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DMA_HM = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Map<String, String> ICONOS = new LinkedHashMap<>();
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String LOGO_MENU = "<svg viewBox=\"0 0 200 90\" aria-hidden=\"true\"><ellipse cx=\"100\" cy=\"80\" rx=\"95\" ry=\"12\" fill=\"#a9c48a\"/><rect x=\"72\" y=\"30\" width=\"50\" height=\"46\" fill=\"#f8f7f3\"/><polygon points=\"66,32 97,10 128,32\" fill=\"#7a5233\"/><rect x=\"92\" y=\"52\" width=\"14\" height=\"24\" fill=\"#7a5233\"/><line x1=\"97\" y1=\"10\" x2=\"97\" y2=\"0\" stroke=\"#7a5233\" stroke-width=\"2\"/><polygon points=\"97,0 112,4 97,8\" fill=\"#a9c48a\"/><circle cx=\"160\" cy=\"45\" r=\"14\" fill=\"#6f9a5a\"/><rect x=\"158\" y=\"55\" width=\"4\" height=\"22\" fill=\"#7a5233\"/></svg>";
    private static final String LOGO_LOGIN = "<svg viewBox=\"0 0 200 90\" aria-hidden=\"true\"><ellipse cx=\"100\" cy=\"80\" rx=\"95\" ry=\"12\" fill=\"#a9c48a\"/><path d=\"M10 78c20-26 45-34 70-30 18 3 30 12 45 12s45-8 65 18z\" fill=\"#6f9a5a\" opacity=\".55\"/><rect x=\"72\" y=\"30\" width=\"50\" height=\"46\" fill=\"#f8f7f3\"/><polygon points=\"66,32 97,10 128,32\" fill=\"#7a5233\"/><rect x=\"92\" y=\"52\" width=\"14\" height=\"24\" fill=\"#7a5233\"/><rect x=\"78\" y=\"42\" width=\"10\" height=\"10\" fill=\"#7a5233\"/><line x1=\"97\" y1=\"10\" x2=\"97\" y2=\"0\" stroke=\"#7a5233\" stroke-width=\"2\"/><polygon points=\"97,0 112,4 97,8\" fill=\"#a9c48a\"/><circle cx=\"160\" cy=\"45\" r=\"14\" fill=\"#6f9a5a\"/><rect x=\"158\" y=\"55\" width=\"4\" height=\"22\" fill=\"#7a5233\"/></svg>";

    static {
        ICONOS.put("inicio", "<path d=\"M3 11l9-8 9 8v10a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z\"/>");
        ICONOS.put("alumnos", "<circle cx=\"9\" cy=\"8\" r=\"3.2\"/><path d=\"M3 20c0-3.3 2.7-6 6-6s6 2.7 6 6\"/><circle cx=\"17\" cy=\"9\" r=\"2.4\"/><path d=\"M17 14c2.5 0 4 2 4 4.5\"/>");
        ICONOS.put("docentes", "<path d=\"M2 9l10-5 10 5-10 5z\"/><path d=\"M6 11.5V16c0 1.5 3 3 6 3s6-1.5 6-3v-4.5\"/>");
        ICONOS.put("cursos", "<path d=\"M4 4h10a4 4 0 0 1 4 4v12H8a4 4 0 0 1-4-4z\"/><path d=\"M4 16a4 4 0 0 1 4-4h10\"/>");
        ICONOS.put("notas", "<rect x=\"5\" y=\"4\" width=\"14\" height=\"17\" rx=\"2\"/><path d=\"M9 4h6v3H9z\"/><path d=\"M8.5 13l2 2 4-4\"/>");
        ICONOS.put("alerta", "<path d=\"M6 17v-6a6 6 0 0 1 12 0v6l1.5 2h-15z\"/><path d=\"M10 21h4\"/>");
        ICONOS.put("reportes", "<path d=\"M4 20V4\"/><path d=\"M4 20h16\"/><rect x=\"8\" y=\"11\" width=\"3\" height=\"6\"/><rect x=\"13\" y=\"7\" width=\"3\" height=\"10\"/>");
        ICONOS.put("logs", "<path d=\"M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6z\"/>");
        ICONOS.put("perfil", "<circle cx=\"12\" cy=\"8\" r=\"4\"/><path d=\"M4 21c0-4.4 3.6-7 8-7s8 2.6 8 7\"/>");
        ICONOS.put("usuarios", "<circle cx=\"8\" cy=\"15\" r=\"4\"/><path d=\"M11 12l9-9\"/><path d=\"M16 7l3 3\"/>");
        ICONOS.put("luna", "<path d=\"M20 14.5A8 8 0 0 1 9.5 4 8 8 0 1 0 20 14.5z\"/>");
        ICONOS.put("menu", "<path d=\"M4 7h16M4 12h16M4 17h16\"/>");
        ICONOS.put("buscar", "<circle cx=\"11\" cy=\"11\" r=\"6\"/><path d=\"M20 20l-4.5-4.5\"/>");
        ICONOS.put("mas", "<path d=\"M12 5v14M5 12h14\"/>");
        ICONOS.put("editar", "<path d=\"M4 20l4-1 11-11-3-3L5 16z\"/>");
        ICONOS.put("borrar", "<path d=\"M5 7h14M9 7V4h6v3M7 7l1 13h8l1-13\"/>");
        ICONOS.put("ver", "<path d=\"M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12z\"/><circle cx=\"12\" cy=\"12\" r=\"3\"/>");
        ICONOS.put("ok", "<path d=\"M5 12.5l4.5 4.5L19 7.5\"/>");
        ICONOS.put("bajar", "<path d=\"M12 4v11\"/><path d=\"M7 11l5 5 5-5\"/><path d=\"M5 20h14\"/>");
        ICONOS.put("salir", "<path d=\"M10 4H5v16h5\"/><path d=\"M15 8l4 4-4 4\"/><path d=\"M19 12H9\"/>");
        ICONOS.put("lista", "<rect x=\"5\" y=\"4\" width=\"14\" height=\"17\" rx=\"2\"/><path d=\"M9 4h6v3H9z\"/><path d=\"M8.5 11h7M8.5 14.5h7M8.5 18h4\"/>");
        ICONOS.put("mega", "<path d=\"M3 11v3l12 5V6z\"/><path d=\"M15 9.5a3 3 0 0 1 0 5\"/><path d=\"M6 15l1.5 5h2.5l-1-4.5\"/>");
        ICONOS.put("config", "<circle cx=\"12\" cy=\"12\" r=\"3\"/><path d=\"M12 3v3M12 18v3M3 12h3M18 12h3M5.6 5.6l2.1 2.1M16.3 16.3l2.1 2.1M18.4 5.6l-2.1 2.1M7.7 16.3l-2.1 2.1\"/>");
        ICONOS.put("respaldo", "<ellipse cx=\"12\" cy=\"6\" rx=\"8\" ry=\"3\"/><path d=\"M4 6v6c0 1.7 3.6 3 8 3s8-1.3 8-3V6\"/><path d=\"M4 12v6c0 1.7 3.6 3 8 3s8-1.3 8-3v-6\"/>");
        ICONOS.put("imprimir", "<path d=\"M7 9V3h10v6\"/><rect x=\"4\" y=\"9\" width=\"16\" height=\"8\" rx=\"2\"/><path d=\"M7 14h10v7H7z\"/>");
        ICONOS.put("subir", "<path d=\"M12 16V5\"/><path d=\"M7 9l5-5 5 5\"/><path d=\"M5 20h14\"/>");
        ICONOS.put("llave", "<rect x=\"4\" y=\"10\" width=\"16\" height=\"10\" rx=\"2\"/><path d=\"M8 10V7a4 4 0 0 1 8 0v3\"/>");
    }

    /** 8.0 se muestra 8; vacío se muestra como raya. */
    public String num(Double n) { return n == null ? "—" : Formato.numero(n); }

    public String num(Double n, int decimales) {
        if (n == null) { return "—"; }
        if (decimales == 0) { return String.valueOf(Math.round(n)); }
        return Formato.numero(n);
    }

    public String iniciales(String nombres, String apellidos) {
        return ((nombres.isEmpty() ? "" : nombres.substring(0, 1)) + (apellidos.isEmpty() ? "" : apellidos.substring(0, 1))).toUpperCase();
    }

    public String fecha(LocalDate f) { return f == null ? "—" : f.format(DMA); }
    public String fechaHora(LocalDateTime f) { return f == null ? "—" : f.format(DMA_HM); }
    public String dia(LocalDateTime f) { return f == null ? "—" : f.format(DMA); }

    public String primerNombre(String nombre) {
        String p = nombre.split(" ")[0];
        return "Prof.".equals(p) ? nombre : p;
    }

    public int paginas(int total, int porPagina) { return Math.max(1, (int) Math.ceil(total / (double) porPagina)); }

    /** Mantiene los filtros actuales de la URL y cambia solo la clave indicada. */
    public String urlCon(Map<String, String> actual, String clave, Object valor) {
        Map<String, String> q = new LinkedHashMap<>(actual);
        q.put(clave, String.valueOf(valor));
        StringBuilder sb = new StringBuilder();
        q.forEach((k, v) -> {
            if (v != null && !v.isEmpty()) {
                sb.append(sb.length() == 0 ? '?' : '&').append(URLEncoder.encode(k, StandardCharsets.UTF_8)).append('=').append(URLEncoder.encode(v, StandardCharsets.UTF_8));
            }
        });
        return sb.length() == 0 ? "?" : sb.toString();
    }

    /** Ancho (0-100) de una barra respecto a la más larga del gráfico. */
    public int ancho(Barra b, List<Barra> todas) {
        double max = todas.stream().mapToDouble(Barra::valor).max().orElse(1);
        if (max < 1) { max = 1; }
        return b.valor() > 0 ? (int) Math.max(3, Math.round(b.valor() / max * 100)) : 0;
    }

    public String icono(String nombre) {
        return "<svg class=\"ico\" viewBox=\"0 0 24 24\" aria-hidden=\"true\">" + ICONOS.getOrDefault(nombre, "") + "</svg>";
    }

    /** Cuántos días hay de un estado ("presente", "falta"...) en un conteo de asistencia. */
    public int n(Map<EstadoAsistencia, Integer> conteo, String valor) {
        EstadoAsistencia e = EstadoAsistencia.desde(valor);
        Integer x = e == null ? null : conteo.get(e);
        return x == null ? 0 : x;
    }

    public String logo() { return LOGO_MENU; }
    public String logoLogin() { return LOGO_LOGIN; }

    /** Pares clave/valor a JSON para los botones "Editar" (data-fill). */
    public String json(Object... pares) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < pares.length; i += 2) { m.put(String.valueOf(pares[i]), pares[i + 1]); }
        try { return JSON.writeValueAsString(m); } catch (JsonProcessingException e) { return "{}"; }
    }
}
