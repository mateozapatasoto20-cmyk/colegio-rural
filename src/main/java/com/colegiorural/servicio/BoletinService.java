package com.colegiorural.servicio;

import com.colegiorural.dominio.Calificacion;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** Arma el boletín de un estudiante: notas, asistencia diaria y observaciones. */
@Service
public class BoletinService {
    public record Boletin(EstudianteService.Ficha ficha, AsistenciaService.ResumenAsistencia asistencia, List<Calificacion> porReforzar, String observacion) { }

    private final EstudianteService estudiantes;
    private final AsistenciaService asistencia;
    private final ReglasVigentes reglas;

    public BoletinService(EstudianteService estudiantes, AsistenciaService asistencia, ReglasVigentes reglas) {
        this.estudiantes = estudiantes; this.asistencia = asistencia; this.reglas = reglas;
    }

    public Optional<Boletin> de(long estudianteId) {
        return estudiantes.ficha(estudianteId).map(f -> {
            var as = asistencia.resumenEstudiante(estudianteId);
            double notaMin = reglas.notaMinima(), asisMin = reglas.asistenciaMinima();
            List<Calificacion> reforzar = f.notas().stream().filter(n -> n.nota() < notaMin).toList();
            boolean asistenciaBaja = as.porcentaje() != null && as.porcentaje() < asisMin;
            String obs;
            if (f.notas().isEmpty()) { obs = "Sin notas para evaluar todavía."; }
            else if (reforzar.isEmpty() && !asistenciaBaja) { obs = "Buen desempeño. Cumple con las notas y la asistencia mínimas."; }
            else {
                StringBuilder sb = new StringBuilder();
                if (!reforzar.isEmpty()) { sb.append("Necesita reforzamiento en: ").append(String.join(", ", reforzar.stream().map(Calificacion::curso).toList())).append(". "); }
                if (asistenciaBaja) { sb.append("Su asistencia (").append(Math.round(as.porcentaje())).append("%) está por debajo del mínimo de ").append(Math.round(asisMin)).append("%. "); }
                obs = sb.append("Se recomienda conversar con el apoderado.").toString();
            }
            return new Boletin(f, as, reforzar, obs);
        });
    }
}
