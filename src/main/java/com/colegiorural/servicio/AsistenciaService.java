package com.colegiorural.servicio;

import com.colegiorural.dao.AsistenciaDao;
import com.colegiorural.dao.CursoDao;
import com.colegiorural.dao.EstudianteDao;
import com.colegiorural.dominio.Barra;
import com.colegiorural.dominio.Curso;
import com.colegiorural.dominio.EstadoAsistencia;
import com.colegiorural.dominio.Estudiante;
import com.colegiorural.regla.ReglaAlerta;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Pase de lista diario. Cuando alguien falta se evalúan las reglas (asistencia baja y faltas seguidas). */
@Service
public class AsistenciaService {
    private static final int MIN_REGISTROS = 3;

    public record FilaPlanilla(Estudiante estudiante, EstadoAsistencia estado, Map<EstadoAsistencia, Integer> conteo, Double porcentaje) { }
    public record Planilla(Curso curso, LocalDate fecha, List<FilaPlanilla> filas, boolean registrado, List<LocalDate> fechas) { }
    public record AsistenciaCurso(long cursoId, String nombre, Map<EstadoAsistencia, Integer> conteo, Double porcentaje) { }
    public record ResumenAsistencia(Map<EstadoAsistencia, Integer> total, Double porcentaje, List<AsistenciaCurso> cursos) { }
    public record DiaCalendario(String estado, String detalle) { }
    public record Calendario(String mes, String titulo, int dias, int inicio, Map<Integer, DiaCalendario> porDia,
                             Map<EstadoAsistencia, Integer> conteo, String anterior, String siguiente) { }
    public record UltimoDia(LocalDate fecha, Map<EstadoAsistencia, Integer> conteo) { }

    private static final String[] MESES = {"", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Setiembre", "Octubre", "Noviembre", "Diciembre"};

    private final AsistenciaDao dao;
    private final CursoDao cursos;
    private final EstudianteDao estudiantes;
    private final AlertaService alertas;
    private final LogService log;
    private final ReglasVigentes reglas;

    public AsistenciaService(AsistenciaDao dao, CursoDao cursos, EstudianteDao estudiantes, AlertaService alertas, LogService log, ReglasVigentes reglas) {
        this.dao = dao; this.cursos = cursos; this.estudiantes = estudiantes; this.alertas = alertas; this.log = log; this.reglas = reglas;
    }

    /** % de días que asistió: todo lo que no es falta cuenta como asistencia. null si no hay registros. */
    public static Double porcentaje(Map<EstadoAsistencia, Integer> conteo) {
        int total = conteo.values().stream().mapToInt(Integer::intValue).sum();
        return total == 0 ? null : (total - conteo.getOrDefault(EstadoAsistencia.FALTA, 0)) * 100.0 / total;
    }

    public static boolean fechaValida(LocalDate f) { return f != null && !f.isAfter(LocalDate.now()); }

    public Planilla planilla(long cursoId, LocalDate fecha) {
        Curso curso = cursos.buscarPorId(cursoId).orElseThrow(() -> new ValidacionException("Elige un curso."));
        if (!fechaValida(fecha)) { throw new ValidacionException("La fecha no es válida (no puede ser futura)."); }
        Map<Long, EstadoAsistencia> guardado = dao.porCursoFecha(cursoId, fecha);
        Map<Long, Map<EstadoAsistencia, Integer>> acumulado = dao.conteoPorCurso(cursoId);
        List<FilaPlanilla> filas = new ArrayList<>();
        for (Estudiante e : estudiantes.activosDeGrupo(curso.grado(), curso.seccion())) {
            Map<EstadoAsistencia, Integer> conteo = acumulado.getOrDefault(e.id(), ceros());
            filas.add(new FilaPlanilla(e, guardado.get(e.id()), conteo, porcentaje(conteo)));
        }
        return new Planilla(curso, fecha, filas, !guardado.isEmpty(), dao.fechasDeCurso(cursoId, 5));
    }

    /** Guarda el pase de lista. Devuelve cuántas alertas se crearon. */
    public int registrar(long cursoId, LocalDate fecha, Map<Long, EstadoAsistencia> estados) {
        Planilla p = planilla(cursoId, fecha);
        List<Long> validos = p.filas().stream().map(f -> f.estudiante().id()).toList();
        Map<Long, EstadoAsistencia> limpio = new LinkedHashMap<>();
        estados.forEach((id, estado) -> { if (validos.contains(id) && estado != null) { limpio.put(id, estado); } });
        if (limpio.isEmpty()) { throw new ValidacionException("Marca la asistencia de al menos un estudiante."); }
        dao.guardarLote(cursoId, fecha, limpio);

        int creadas = 0;
        for (var e : limpio.entrySet()) {
            if (e.getValue() == EstadoAsistencia.FALTA) { creadas += evaluarAlertas(e.getKey(), p.curso()); }
        }
        log.registrar("Pasó lista de " + p.curso().nombre() + " " + p.curso().grado() + " " + p.curso().seccion() + " (" + fecha + ")");
        return creadas;
    }

    private int evaluarAlertas(long estudianteId, Curso curso) {
        Map<EstadoAsistencia, Integer> conteo = dao.conteo(estudianteId, curso.id());
        int total = conteo.values().stream().mapToInt(Integer::intValue).sum();
        Double pct = total >= MIN_REGISTROS ? porcentaje(conteo) : null;
        int seguidas = 0;
        for (EstadoAsistencia s : dao.ultimosEstados(estudianteId, curso.id(), 15)) {
            if (s != EstadoAsistencia.FALTA) { break; }
            seguidas++;
        }
        return alertas.evaluar(estudianteId, new ReglaAlerta.Evaluacion(curso.nombre(), null, pct, seguidas));
    }

    public ResumenAsistencia resumenEstudiante(long estudianteId) {
        List<AsistenciaCurso> porCurso = new ArrayList<>();
        for (var c : dao.porEstudiantePorCurso(estudianteId)) { porCurso.add(new AsistenciaCurso(c.cursoId(), c.nombre(), c.conteo(), porcentaje(c.conteo()))); }
        Map<EstadoAsistencia, Integer> total = dao.conteo(estudianteId, null);
        return new ResumenAsistencia(total, porcentaje(total), porCurso);
    }

    /** Calendario del mes. Si un día hay varios cursos se muestra el peor estado (falta > tardanza > justificada > presente). */
    public Calendario calendario(long estudianteId, String mes, Long cursoId) {
        YearMonth ym;
        try { ym = YearMonth.parse(mes); } catch (RuntimeException e) { ym = YearMonth.now(); }
        Map<Integer, EstadoAsistencia> peor = new HashMap<>();
        Map<Integer, List<String>> detalle = new HashMap<>();
        for (var r : dao.delMes(estudianteId, ym.atDay(1), ym.atEndOfMonth(), cursoId)) {
            int d = r.fecha().getDayOfMonth();
            detalle.computeIfAbsent(d, k -> new ArrayList<>()).add(r.curso() + ": " + r.estado().etiqueta());
            peor.merge(d, r.estado(), (a, b) -> b.gravedad() > a.gravedad() ? b : a);
        }
        Map<Integer, DiaCalendario> porDia = new HashMap<>();
        Map<EstadoAsistencia, Integer> conteo = ceros();
        peor.forEach((d, est) -> { porDia.put(d, new DiaCalendario(est.valor(), String.join(" · ", detalle.get(d)))); conteo.merge(est, 1, Integer::sum); });
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM");
        return new Calendario(ym.format(f), MESES[ym.getMonthValue()] + " " + ym.getYear(), ym.lengthOfMonth(), ym.atDay(1).getDayOfWeek().getValue(),
                porDia, conteo, ym.minusMonths(1).format(f), ym.plusMonths(1).format(f));
    }

    /** Asistencia de los últimos días con registro, lista para el gráfico de barras. */
    public List<Barra> tendencia(int dias) {
        double minimo = reglas.asistenciaMinima();
        List<Barra> r = new ArrayList<>();
        for (var d : dao.tendencia(dias)) {
            long pct = Math.round(d.asistio() * 100.0 / d.total());
            r.add(new Barra(String.format("%02d/%02d", d.fecha().getDayOfMonth(), d.fecha().getMonthValue()), pct, pct >= minimo ? "ok" : "warn"));
        }
        return r;
    }

    public UltimoDia ultimoDia() {
        return dao.ultimaFecha().map(f -> new UltimoDia(f, dao.resumenFecha(f))).orElse(new UltimoDia(null, ceros()));
    }

    public List<Curso> cursos() { return cursos.listarConDocente().stream().map(c -> c.curso()).toList(); }

    private static Map<EstadoAsistencia, Integer> ceros() {
        Map<EstadoAsistencia, Integer> m = new EnumMap<>(EstadoAsistencia.class);
        for (EstadoAsistencia e : EstadoAsistencia.values()) { m.put(e, 0); }
        return m;
    }
}
