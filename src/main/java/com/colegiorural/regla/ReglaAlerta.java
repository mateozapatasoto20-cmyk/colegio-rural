package com.colegiorural.regla;

import java.util.Optional;

/* OCP (abierto/cerrado): para agregar una regla nueva se crea UNA clase que implemente esta interfaz y se marca con @Component.
   Spring la encuentra solo y AlertaService no se modifica. */
public interface ReglaAlerta {

    /** Datos que se evalúan. Lo que no aplica va en null (ej.: en el pase de lista no hay nota). */
    record Evaluacion(String curso, Double nota, Double asistencia, Integer faltasSeguidas) { }

    /** Resultado de una regla que se cumplió: tipo (nota, asistencia, manual) y mensaje para el coordinador. */
    record Resultado(String tipo, String mensaje) { }

    Optional<Resultado> evaluar(Evaluacion datos);
}
