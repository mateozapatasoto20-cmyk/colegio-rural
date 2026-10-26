package com.colegiorural.servicio;

import com.colegiorural.dao.EstudianteDao;
import com.google.common.collect.ImmutableMap;
import com.colegiorural.dao.UsuarioDao;
import com.colegiorural.dominio.Usuario;
import com.colegiorural.dominio.UsuarioDetalle;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
    /** Guava ImmutableMap: mantiene el orden y nadie puede modificarla por error. */
    public static final Map<String, String> ROLES = ImmutableMap.of(
            "admin", "Administrador", "coordinador", "Coordinador", "docente", "Docente", "alumno", "Alumno");

    private final UsuarioDao dao;
    private final EstudianteDao estudiantes;
    private final Hasher hasher;
    private final LogService log;

    public UsuarioService(UsuarioDao dao, EstudianteDao estudiantes, Hasher hasher, LogService log) {
        this.dao = dao; this.estudiantes = estudiantes; this.hasher = hasher; this.log = log;
    }

    public List<UsuarioDetalle> listar() { return dao.listar(); }

    public void crear(String username, String nombre, String rol, String password, long estudianteId) {
        String user = username == null ? "" : username.trim().toLowerCase();
        String nom = nombre == null ? "" : nombre.trim();
        String pass = password == null ? "" : password;
        if (!user.matches("^[a-z0-9_.]{3,30}$")) { throw new ValidacionException("El usuario debe tener de 3 a 30 letras minúsculas, números, punto o guion bajo."); }
        if (nom.isEmpty() || nom.length() > 80) { throw new ValidacionException("Escribe el nombre completo."); }
        if (!ROLES.containsKey(rol)) { throw new ValidacionException("Elige un rol válido."); }
        if (pass.length() < 6) { throw new ValidacionException("La contraseña debe tener al menos 6 caracteres."); }
        if (dao.buscarPorUsername(user).isPresent()) { throw new ValidacionException("Ese usuario ya existe."); }
        boolean alumno = "alumno".equals(rol);
        if (alumno && estudiantes.buscarPorId(estudianteId).isEmpty()) { throw new ValidacionException("Un usuario alumno debe estar vinculado a un estudiante."); }

        dao.crear(new Usuario(0, user, hasher.cifrar(pass), rol, nom, true, alumno ? estudianteId : null));
        log.registrar("Creó usuario " + user + " (" + rol + ")");
    }

    public void cambiarEstado(long id, long yoId) {
        Usuario u = dao.buscarPorId(id).orElseThrow(() -> new ValidacionException("El usuario no existe."));
        if (id == yoId) { throw new ValidacionException("No puedes desactivar tu propia cuenta."); }
        dao.cambiarEstado(id, !u.activo());
        log.registrar((u.activo() ? "Desactivó" : "Activó") + " usuario " + u.username());
    }

    /** Genera una contraseña temporal y la devuelve para mostrarla una sola vez. */
    public String restablecer(long id) {
        Usuario u = dao.buscarPorId(id).orElseThrow(() -> new ValidacionException("El usuario no existe."));
        String alfabeto = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        SecureRandom azar = new SecureRandom();
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < 8; i++) { tmp.append(alfabeto.charAt(azar.nextInt(alfabeto.length()))); }
        dao.cambiarPassword(id, hasher.cifrar(tmp.toString()));
        log.registrar("Restableció contraseña de " + u.username());
        return tmp.toString();
    }
}
