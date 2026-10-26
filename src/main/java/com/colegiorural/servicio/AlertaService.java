package com.colegiorural.servicio;

import com.colegiorural.dao.AlertaDao;
import com.colegiorural.dao.EstudianteDao;
import com.colegiorural.dao.FiltroAlerta;
import com.colegiorural.dominio.AlertaFila;
import com.colegiorural.dominio.Pagina;
import com.colegiorural.regla.ReglaAlerta;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/* DIP: depende de interfaces (AlertaDao, ReglaAlerta), nunca de clases concretas.
   OCP: recibe TODAS las reglas que existan (List<ReglaAlerta>); una regla nueva no obliga a cambiar este archivo. */
@Service
public class AlertaService {
    private final AlertaDao dao;
    private final EstudianteDao estudiantes;
    private final LogService log;
    private final List<ReglaAlerta> reglas;

    public AlertaService(AlertaDao dao, EstudianteDao estudiantes, LogService log, List<ReglaAlerta> reglas) {
        this.dao = dao; this.estudiantes = estudiantes; this.log = log; this.reglas = reglas;
    }

    /** Evalúa todas las reglas y crea las alertas que correspondan. Devuelve cuántas creó. No repite alertas activas iguales. */
    public int evaluar(long estudianteId, ReglaAlerta.Evaluacion datos) {
        int creadas = 0;
        for (ReglaAlerta regla : reglas) {
            var r = regla.evaluar(datos);
            if (r.isPresent() && !dao.existeActiva(estudianteId, r.get().tipo(), r.get().mensaje())) {
                dao.crear(estudianteId, r.get().mensaje(), r.get().tipo());
                creadas++;
            }
        }
        return creadas;
    }

    public Pagina<AlertaFila> listar(FiltroAlerta filtro, int pagina, int porPagina) {
        return new Pagina<>(dao.listar(filtro, porPagina, (pagina - 1) * porPagina), dao.contar(filtro));
    }

    public Map<String, Integer> conteos() {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("todas", dao.contar(FiltroAlerta.sinFiltro()));
        m.put("activa", dao.contar(new FiltroAlerta("activa", "", "")));
        m.put("resuelta", dao.contar(new FiltroAlerta("resuelta", "", "")));
        return m;
    }

    public int activas() { return dao.contar(new FiltroAlerta("activa", "", "")); }

    public void resolver(long id) { dao.resolver(id); log.registrar("Resolvió alerta #" + id); }

    public void crearManual(long estudianteId, String mensaje) {
        String m = mensaje == null ? "" : mensaje.trim();
        if (estudiantes.buscarPorId(estudianteId).isEmpty()) { throw new ValidacionException("Elige un estudiante válido."); }
        if (m.isEmpty() || m.length() > 200) { throw new ValidacionException("Escribe un mensaje de hasta 200 caracteres."); }
        dao.crear(estudianteId, m, "manual");
        log.registrar("Creó alerta manual");
    }
}
