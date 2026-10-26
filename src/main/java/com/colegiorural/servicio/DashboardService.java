package com.colegiorural.servicio;

import com.colegiorural.dao.AlertaDao;
import com.colegiorural.dao.CalificacionDao;
import com.colegiorural.dao.CursoDao;
import com.colegiorural.dao.DocenteDao;
import com.colegiorural.dao.EstudianteDao;
import com.colegiorural.dominio.Anuncio;
import com.colegiorural.dominio.AlertaFila;
import com.colegiorural.dominio.Barra;
import com.colegiorural.dominio.EnRiesgo;
import com.colegiorural.dominio.Promedio;
import com.colegiorural.dominio.Resumen;
import com.colegiorural.dao.FiltroAlerta;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    public record Panel(int estudiantes, int activos, int docentes, int cursos, Double promedio, Double asistencia, int alertas,
                        List<Barra> distribucion, List<Barra> alertasPorTipo, List<Barra> porGrado, List<EnRiesgo> enRiesgo,
                        List<AlertaFila> ultimas, double notaMinima, List<Barra> tendencia, AsistenciaService.UltimoDia ultimoDia,
                        List<Promedio> honor, List<Anuncio> comunicados) { }

    private final EstudianteDao est;
    private final DocenteDao doc;
    private final CursoDao cur;
    private final CalificacionDao cal;
    private final AlertaDao al;
    private final ReglasVigentes reglas;
    private final AsistenciaService asistencia;
    private final AnuncioService anuncios;

    public DashboardService(EstudianteDao est, DocenteDao doc, CursoDao cur, CalificacionDao cal, AlertaDao al, ReglasVigentes reglas,
                            AsistenciaService asistencia, AnuncioService anuncios) {
        this.est = est; this.doc = doc; this.cur = cur; this.cal = cal; this.al = al; this.reglas = reglas; this.asistencia = asistencia; this.anuncios = anuncios;
    }

    public Panel resumen(String rol) {
        List<Double> notas = cal.todasLasNotas();
        double[][] rangos = {{0, 10.99}, {11, 13.99}, {14, 16.99}, {17, 20.01}};
        String[] etiquetas = {"0 a 10", "11 a 13", "14 a 16", "17 a 20"};
        List<Barra> dist = new ArrayList<>();
        for (int i = 0; i < rangos.length; i++) {
            final double a = rangos[i][0], b = rangos[i][1];
            dist.add(new Barra(etiquetas[i], notas.stream().filter(n -> n >= a && n < b).count(), i == 0 ? "crit" : "ok"));
        }
        Resumen general = cal.promedioGeneral().orElse(null);
        Map<String, Integer> tipos = al.contarPorTipo("activa");
        List<Barra> porGrado = new ArrayList<>();
        est.contarPorGrado().forEach((g, n) -> porGrado.add(new Barra(g, n, "ok")));
        return new Panel(est.contar(new com.colegiorural.dao.FiltroEstudiante("", "", "", "apellidos")), est.contarActivos(), doc.contar(), cur.contar(),
                general == null ? null : general.promedio(), general == null ? null : general.asistencia(),
                tipos.values().stream().mapToInt(Integer::intValue).sum(), dist,
                List.of(new Barra("Nota baja", tipos.getOrDefault("nota", 0), "crit"), new Barra("Asistencia baja", tipos.getOrDefault("asistencia", 0), "warn"),
                        new Barra("Manuales", tipos.getOrDefault("manual", 0), "neutro")),
                porGrado, al.estudiantesEnRiesgo(5), al.listar(new FiltroAlerta("activa", "", ""), 5, 0), reglas.notaMinima(),
                asistencia.tendencia(7), asistencia.ultimoDia(), cal.mejoresPromedios(5), anuncios.visibles(rol, 3));
    }
}
