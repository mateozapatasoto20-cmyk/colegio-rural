package com.colegiorural.regla;

import com.colegiorural.servicio.ReglasVigentes;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Alerta cuando el estudiante acumula N faltas seguidas en un curso. Es la regla que se agregó sin tocar AlertaService. */
@Component
public class ReglaFaltasSeguidas implements ReglaAlerta {

    private final ReglasVigentes reglas;

    public ReglaFaltasSeguidas(ReglasVigentes reglas) { this.reglas = reglas; }

    @Override
    public Optional<Resultado> evaluar(Evaluacion d) {
        int limite = reglas.actual().faltasSeguidas();
        if (d.faltasSeguidas() == null || d.faltasSeguidas() < limite) { return Optional.empty(); }
        return Optional.of(new Resultado("asistencia", limite + " faltas seguidas en " + d.curso()));
    }
}
