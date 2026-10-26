package com.colegiorural.web;

import com.colegiorural.dominio.SesionUsuario;
import com.colegiorural.servicio.EstudianteService;
import com.colegiorural.servicio.UsuarioService;
import com.colegiorural.servicio.ValidacionException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UsuarioController {
    private final UsuarioService usuarios;
    private final EstudianteService estudiantes;

    public UsuarioController(UsuarioService usuarios, EstudianteService estudiantes) { this.usuarios = usuarios; this.estudiantes = estudiantes; }

    @GetMapping("/usuarios")
    public String listar(Model m) {
        m.addAttribute("lista", usuarios.listar());
        m.addAttribute("roles", UsuarioService.ROLES);
        m.addAttribute("estudiantes", estudiantes.todos());
        return "usuarios";
    }

    @PostMapping("/usuarios")
    public String accion(@RequestParam(defaultValue = "") String accion, @RequestParam(defaultValue = "") String id,
                         @RequestParam(defaultValue = "") String username, @RequestParam(defaultValue = "") String nombre,
                         @RequestParam(defaultValue = "") String rol, @RequestParam(defaultValue = "") String password,
                         @RequestParam(name = "estudiante_id", defaultValue = "0") String estudianteId, HttpServletRequest req) {
        SesionUsuario yo = Sesion.usuario(req).orElseThrow();
        try {
            switch (accion) {
                case "crear" -> { usuarios.crear(username, nombre, rol, password, P.entero(estudianteId, 0)); Sesion.ok(req, "Usuario creado."); }
                case "estado" -> { usuarios.cambiarEstado(P.entero(id, 0), yo.id()); Sesion.ok(req, "Estado actualizado."); }
                case "restablecer" -> Sesion.ok(req, "Contraseña temporal: " + usuarios.restablecer(P.entero(id, 0)) + " (anótala, no se vuelve a mostrar).");
                default -> { }
            }
        } catch (ValidacionException e) {
            if ("crear".equals(accion)) {
                Map<String, String> datos = new LinkedHashMap<>();
                datos.put("username", username); datos.put("nombre", nombre); datos.put("rol", rol); datos.put("estudiante_id", estudianteId);
                Sesion.dialogo(req, "dlgUsr", "Nuevo usuario", datos, e.getMessage());
            } else {
                Sesion.error(req, e.getMessage());
            }
        }
        return Sesion.volver(req);
    }
}
