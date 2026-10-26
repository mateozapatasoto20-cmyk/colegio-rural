package com.colegiorural.regla;

import com.colegiorural.servicio.ReglasVigentes;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Alerta cuando la nota es menor al mínimo aprobatorio. */
@Component
public class ReglaNotaBaja implements ReglaAlerta {

    private final ReglasVigentes reglas;

    public ReglaNotaBaja(ReglasVigentes reglas) { this.reglas = reglas; }

    @Override
    public Optional<Resultado> evaluar(Evaluacion d) {
        double minimo = reglas.actual().notaMinima();
        if (d.nota() == null || d.nota() >= minimo) { return Optional.empty(); }
        return Optional.of(new Resultado("nota", "Nota " + Formato.numero(d.nota()) + " en " + d.curso() + " (menor a " + Formato.numero(minimo) + ")"));
    }
}
