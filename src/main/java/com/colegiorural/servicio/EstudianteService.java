package com.colegiorural.servicio;

import com.colegiorural.config.AppProperties;
import com.colegiorural.dao.AlertaDao;
import com.colegiorural.dao.CalificacionDao;
import com.colegiorural.dao.EstudianteDao;
import com.colegiorural.dao.FiltroEstudiante;
import com.colegiorural.dominio.Alerta;
import com.colegiorural.dominio.Calificacion;
import com.colegiorural.dominio.Estudiante;
import com.colegiorural.dominio.Pagina;
import com.colegiorural.dominio.Resumen;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class EstudianteService {
    public record FilaEstudiante(Estudiante estudiante, Double promedio, Double asistencia, int alertas) { }
    public record Ficha(Estudiante estudiante, List<Calificacion> notas, List<Alerta> alertas, Double promedio, Double asistencia, int activas) { }
    public record ResultadoImportacion(int creados, List<String> omitidos) { }

    private final EstudianteDao dao;
    private final CalificacionDao califs;
    private final AlertaDao alertas;
    private final LogService log;
    private final AppProperties app;

    public EstudianteService(EstudianteDao dao, CalificacionDao califs, AlertaDao alertas, LogService log, AppProperties app) {
        this.dao = dao; this.califs = califs; this.alertas = alertas; this.log = log; this.app = app;
    }

    public Pagina<FilaEstudiante> listar(FiltroEstudiante filtro, int pagina, int porPagina) {
        Map<Long, Resumen> res = califs.resumenPorEstudiante();
        Map<Long, Integer> al = alertas.conteoActivasPorEstudiante();
        List<FilaEstudiante> filas = new ArrayList<>();
        for (Estudiante e : dao.listar(filtro, porPagina, (pagina - 1) * porPagina)) {
            Resumen r = res.get(e.id());
            filas.add(new FilaEstudiante(e, r == null ? null : r.promedio(), r == null ? null : r.asistencia(), al.getOrDefault(e.id(), 0)));
        }
        return new Pagina<>(filas, dao.contar(filtro));
    }

    public java.util.Optional<Ficha> ficha(long id) {
        return dao.buscarPorId(id).map(e -> {
            List<Calificacion> notas = califs.porEstudiante(id);
            Double prom = notas.isEmpty() ? null : notas.stream().mapToDouble(Calificacion::nota).average().orElse(0);
            Double asis = notas.isEmpty() ? null : notas.stream().mapToDouble(Calificacion::asistencia).average().orElse(0);
            List<Alerta> al = alertas.porEstudiante(id);
            return new Ficha(e, notas, al, prom, asis, (int) al.stream().filter(Alerta::activa).count());
        });
    }

    /** Crea (id null) o edita un estudiante, validando cada dato. */
    public void guardar(Long id, String nombres, String apellidos, String grado, String seccion, String dni) {
        String n = StringUtils.trimToEmpty(nombres), a = StringUtils.trimToEmpty(apellidos), d = StringUtils.trimToEmpty(dni);
        if (n.isEmpty() || a.isEmpty() || n.length() > 60 || a.length() > 60) { throw new ValidacionException("Completa nombres y apellidos (máximo 60 caracteres)."); }
        if (!app.grados().contains(grado) || !app.secciones().contains(seccion)) { throw new ValidacionException("Elige un grado y una sección válidos."); }
        if (!d.matches("\\d{8}")) { throw new ValidacionException("El DNI debe tener 8 dígitos."); }
        var otro = dao.buscarPorDni(d);
        if (otro.isPresent() && (id == null || otro.get().id() != id)) { throw new ValidacionException("Ya existe un estudiante con ese DNI."); }

        if (id != null && id > 0) { dao.actualizar(new Estudiante(id, n, a, grado, seccion, d, true)); log.registrar("Editó estudiante " + d); }
        else { dao.crear(new Estudiante(0, n, a, grado, seccion, d, true)); log.registrar("Registró estudiante " + d); }
    }

    public void cambiarEstado(long id) {
        Estudiante e = dao.buscarPorId(id).orElseThrow(() -> new ValidacionException("El estudiante no existe."));
        dao.cambiarEstado(id, !e.activo());
        log.registrar((e.activo() ? "Desactivó" : "Activó") + " estudiante " + e.dni());
    }

    public void eliminar(long id) { dao.eliminar(id); log.registrar("Eliminó estudiante #" + id); }

    public List<Estudiante> activos() { return dao.listar(new FiltroEstudiante("", "", "1", "apellidos"), 500, 0); }
    public List<Estudiante> todos() { return dao.listar(FiltroEstudiante.todos(), 500, 0); }
    public List<String> grados() { return app.grados(); }

    /** Importa estudiantes desde un CSV (nombres, apellidos, grado, seccion, dni). Sirve la coma o el punto y coma.
     *  Apache Commons CSV se encarga de comillas, separadores y saltos de línea: no se parte el texto a mano. */
    public ResultadoImportacion importarCsv(InputStream entrada) {
        String texto;
        try { texto = IOUtils.toString(entrada, StandardCharsets.UTF_8); }
        catch (IOException e) { throw new ValidacionException("No se pudo leer el archivo."); }
        texto = StringUtils.removeStart(texto, "\uFEFF");
        String primera = StringUtils.substringBefore(texto, "\n");
        char sep = StringUtils.countMatches(primera, ';') > StringUtils.countMatches(primera, ',') ? ';' : ',';
        CSVFormat formato = CSVFormat.DEFAULT.builder().setDelimiter(sep).setIgnoreEmptyLines(true).setTrim(true).build();

        List<CSVRecord> registros;
        try (CSVParser lector = formato.parse(new StringReader(texto))) { registros = lector.getRecords(); }
        catch (IOException | UncheckedIOException e) { throw new ValidacionException("El archivo CSV tiene un formato que no se puede leer."); }

        int creados = 0;
        List<String> omitidos = new ArrayList<>();
        for (CSVRecord c : registros) {
            long fila = c.getRecordNumber();
            if (fila == 1 && dato(c, 0).equalsIgnoreCase("nombres")) { continue; }
            if (creados + omitidos.size() >= 500) { omitidos.add("Se procesaron las primeras 500 filas."); break; }
            try {
                guardar(null, dato(c, 0), dato(c, 1), igualarGrado(dato(c, 2)), dato(c, 3).toUpperCase(), dato(c, 4));
                creados++;
            } catch (ValidacionException e) { omitidos.add("Fila " + fila + ": " + e.getMessage()); }
        }
        log.registrar("Importó estudiantes desde CSV (" + creados + " nuevos)");
        return new ResultadoImportacion(creados, omitidos);
    }

    private static String dato(CSVRecord c, int i) { return i < c.size() ? StringUtils.trimToEmpty(c.get(i)) : ""; }
    private String igualarGrado(String g) { return app.grados().stream().filter(x -> x.equalsIgnoreCase(g)).findFirst().orElse(g); }
}
