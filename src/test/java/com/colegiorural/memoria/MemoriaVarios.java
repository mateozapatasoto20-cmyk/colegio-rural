package com.colegiorural.memoria;

import com.colegiorural.dao.AnuncioDao;
import com.colegiorural.dao.ConfigDao;
import com.colegiorural.dao.LogDao;
import com.colegiorural.dao.RespaldoDao;
import com.colegiorural.dao.UsuarioDao;
import com.colegiorural.dominio.Anuncio;
import com.colegiorural.dominio.Registro;
import com.colegiorural.dominio.Usuario;
import com.colegiorural.dominio.UsuarioDetalle;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** DAO falsos pequeños para las pruebas. */
public final class MemoriaVarios {
    private MemoriaVarios() { }

    public static class Config implements ConfigDao {
        public final Map<String, String> datos = new HashMap<>();
        @Override public Map<String, String> todos() { return new HashMap<>(datos); }
        @Override public void guardar(String k, String v) { datos.put(k, v); }
    }

    public static class Logs implements LogDao {
        public final List<Registro> datos = new ArrayList<>();
        @Override public void registrar(String u, String a) { datos.add(new Registro(datos.size() + 1, u, a, LocalDateTime.now())); }
        @Override public List<Registro> listar(int l, int o) { return datos; }
        @Override public int contar() { return datos.size(); }
    }

    public static class Usuarios implements UsuarioDao {
        public final Map<Long, Usuario> datos = new HashMap<>();
        @Override public Optional<Usuario> buscarPorId(long id) { return Optional.ofNullable(datos.get(id)); }
        @Override public Optional<Usuario> buscarPorUsername(String u) { return datos.values().stream().filter(x -> x.username().equals(u)).findFirst(); }
        @Override public List<UsuarioDetalle> listar() { return datos.values().stream().map(u -> new UsuarioDetalle(u, null)).toList(); }
        @Override public long crear(Usuario u) { long id = datos.size() + 1; datos.put(id, new Usuario(id, u.username(), u.password(), u.rol(), u.nombre(), true, u.estudianteId())); return id; }
        @Override public void cambiarPassword(long id, String h) { Usuario u = datos.get(id); datos.put(id, new Usuario(id, u.username(), h, u.rol(), u.nombre(), u.activo(), u.estudianteId())); }
        @Override public void cambiarEstado(long id, boolean a) { Usuario u = datos.get(id); datos.put(id, new Usuario(id, u.username(), u.password(), u.rol(), u.nombre(), a, u.estudianteId())); }
    }

    public static class Anuncios implements AnuncioDao {
        public final List<Anuncio> datos = new ArrayList<>();
        @Override public List<Anuncio> listar(List<String> aud, int l) { return datos.stream().filter(a -> aud.contains(a.audiencia())).limit(l).toList(); }
        @Override public long crear(String t, String m, String a, String autor) { datos.add(new Anuncio(datos.size() + 1, t, m, a, autor, LocalDateTime.now())); return datos.size(); }
        @Override public void eliminar(long id) { datos.removeIf(a -> a.id() == id); }
    }

    public static class Respaldo implements RespaldoDao {
        public List<String> ejecutadas = null;
        public boolean barra = false;
        @Override public List<String> tablas() { return List.of("estudiantes", "usuarios", "logs"); }
        @Override public List<String> volcar() { return List.of("DELETE FROM logs;", "INSERT INTO usuarios (id,username,rol) VALUES (1,'admin','admin');"); }
        @Override public void restaurar(List<String> s) { ejecutadas = s; }
        @Override public Map<String, Integer> conteos() { return Map.of(); }
        @Override public boolean escapaConBarra() { return barra; }
    }
}
