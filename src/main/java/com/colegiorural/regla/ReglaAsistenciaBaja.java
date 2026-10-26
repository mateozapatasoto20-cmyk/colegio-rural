package com.colegiorural.regla;

import com.colegiorural.servicio.ReglasVigentes;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Alerta cuando el porcentaje de asistencia es menor al mínimo. */
@Component
public class ReglaAsistenciaBaja implements ReglaAlerta {

    private final ReglasVigentes reglas;

    public ReglaAsistenciaBaja(ReglasVigentes reglas) { this.reglas = reglas; }

    @Override
    public Optional<Resultado> evaluar(Evaluacion d) {
        double minimo = reglas.actual().asistenciaMinima();
        if (d.asistencia() == null || d.asistencia() >= minimo) { return Optional.empty(); }
        return Optional.of(new Resultado("asistencia", "Asistencia " + Math.round(d.asistencia()) + "% en " + d.curso() + " (menor a " + Math.round(minimo) + "%)"));
    }
}
