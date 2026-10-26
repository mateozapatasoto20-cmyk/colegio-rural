package com.colegiorural.memoria;

import com.colegiorural.dao.AlertaDao;
import com.colegiorural.dao.FiltroAlerta;
import com.colegiorural.dominio.Alerta;
import com.colegiorural.dominio.AlertaFila;
import com.colegiorural.dominio.EnRiesgo;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MemoriaAlertaDao implements AlertaDao {
    public final List<Alerta> datos = new ArrayList<>();
    private final MemoriaEstudianteDao estudiantes;
    private long sig = 1;

    public MemoriaAlertaDao(MemoriaEstudianteDao estudiantes) { this.estudiantes = estudiantes; }

    private boolean pasa(Alerta a, FiltroAlerta f) { return (f.estado().isEmpty() || f.estado().equals(a.estado())) && (f.tipo().isEmpty() || f.tipo().equals(a.tipo())); }

    @Override public List<AlertaFila> listar(FiltroAlerta f, int limite, int offset) { return datos.stream().filter(a -> pasa(a, f)).skip(offset).limit(limite).map(a -> new AlertaFila(a, estudiantes.datos.get(a.estudianteId()))).toList(); }
    @Override public int contar(FiltroAlerta f) { return (int) datos.stream().filter(a -> pasa(a, f)).count(); }
    @Override public long crear(long estId, String mensaje, String tipo) { long id = sig++; datos.add(new Alerta(id, estId, mensaje, tipo, "activa", LocalDateTime.now())); return id; }
    @Override public boolean existeActiva(long estId, String tipo, String mensaje) { return datos.stream().anyMatch(a -> a.estudianteId() == estId && a.tipo().equals(tipo) && a.mensaje().equals(mensaje) && a.activa()); }
    @Override public void resolver(long id) { for (int i = 0; i < datos.size(); i++) { Alerta a = datos.get(i); if (a.id() == id) { datos.set(i, new Alerta(a.id(), a.estudianteId(), a.mensaje(), a.tipo(), "resuelta", a.creada())); } } }
    @Override public Optional<Alerta> buscarPorId(long id) { return datos.stream().filter(a -> a.id() == id).findFirst(); }
    @Override public List<Alerta> porEstudiante(long id) { return datos.stream().filter(a -> a.estudianteId() == id).toList(); }
    @Override public Map<String, Integer> contarPorTipo(String estado) { Map<String, Integer> m = new HashMap<>(); datos.stream().filter(a -> a.estado().equals(estado)).forEach(a -> m.merge(a.tipo(), 1, Integer::sum)); return m; }
    @Override public Map<Long, Integer> conteoActivasPorEstudiante() { Map<Long, Integer> m = new HashMap<>(); datos.stream().filter(Alerta::activa).forEach(a -> m.merge(a.estudianteId(), 1, Integer::sum)); return m; }
    @Override public List<EnRiesgo> estudiantesEnRiesgo(int limite) { List<EnRiesgo> r = new ArrayList<>(); conteoActivasPorEstudiante().forEach((id, n) -> r.add(new EnRiesgo(estudiantes.datos.get(id), n))); return r; }
}
