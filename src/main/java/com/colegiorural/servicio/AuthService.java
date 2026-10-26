package com.colegiorural.servicio;

import com.colegiorural.dao.UsuarioDao;
import com.colegiorural.dominio.SesionUsuario;
import com.colegiorural.dominio.Usuario;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/* SRP: solo credenciales, bloqueo por intentos y cambio de contraseña. La sesión HTTP la maneja la capa web. */
@Service
public class AuthService {
    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);
    private static final int MAX_INTENTOS = 5;
    private static final long BLOQUEO_MS = 60_000;

    private record Intentos(int cantidad, long bloqueadoHasta) { }

    private final UsuarioDao usuarios;
    private final Hasher hasher;
    private final LogService log;
    private final Map<String, Intentos> intentos = new ConcurrentHashMap<>();

    public AuthService(UsuarioDao usuarios, Hasher hasher, LogService log) { this.usuarios = usuarios; this.hasher = hasher; this.log = log; }

    /** Valida usuario y clave. "origen" identifica de dónde viene el intento (para el bloqueo). */
    public SesionUsuario ingresar(String username, String clave, String origen) {
        if (username == null || username.isBlank() || clave == null || clave.isEmpty()) { throw new ValidacionException("Completa usuario y contraseña."); }
        Intentos i = intentos.getOrDefault(origen, new Intentos(0, 0));
        long ahora = System.currentTimeMillis();
        if (i.bloqueadoHasta() > ahora) {
            LOG.warn("Intento durante el bloqueo desde {}", origen); throw new ValidacionException("Demasiados intentos. Espera " + ((i.bloqueadoHasta() - ahora) / 1000 + 1) + " segundos."); }

        Usuario u = usuarios.buscarPorUsername(username.trim()).orElse(null);
        if (u == null || !u.activo() || !hasher.coincide(clave, u.password())) {
            int n = i.cantidad() + 1;
            LOG.warn("Ingreso fallido del usuario '{}' desde {} (intento {})", limpiar(username), origen, n);
            if (n >= MAX_INTENTOS) { LOG.warn("Origen {} bloqueado {} segundos", origen, BLOQUEO_MS / 1000); }
            intentos.put(origen, n >= MAX_INTENTOS ? new Intentos(0, ahora + BLOQUEO_MS) : new Intentos(n, 0));
            throw new ValidacionException("Usuario o contraseña incorrectos.");
        }
        intentos.remove(origen);
        LOG.info("Ingreso correcto: {} ({})", u.username(), u.rol());
        SesionUsuario s = new SesionUsuario(u.id(), u.username(), u.nombre(), u.rol(), u.estudianteId());
        return s;
    }

    /** Se llama justo después de crear la sesión, para que quede el usuario en la bitácora. */
    public void registrarIngreso() { log.registrar("Inicio de sesión"); }
    public void registrarSalida() { log.registrar("Cierre de sesión"); }

    /** Quita saltos de línea para que nadie falsifique líneas del registro escribiendo un usuario con \n. */
    private static String limpiar(String texto) { return texto.replaceAll("[\\r\\n\\t]", "_"); }

    public static String inicio(String rol) { return "alumno".equals(rol) ? "/portal" : "/dashboard"; }

    public void cambiarPassword(long id, String actual, String nueva) {
        Usuario u = usuarios.buscarPorId(id).orElseThrow(() -> new ValidacionException("La contraseña actual no es correcta."));
        if (!hasher.coincide(actual == null ? "" : actual, u.password())) { throw new ValidacionException("La contraseña actual no es correcta."); }
        if (nueva == null || nueva.length() < 6) { throw new ValidacionException("La nueva contraseña debe tener al menos 6 caracteres."); }
        usuarios.cambiarPassword(id, hasher.cifrar(nueva));
        log.registrar("Cambió su contraseña");
    }
}
