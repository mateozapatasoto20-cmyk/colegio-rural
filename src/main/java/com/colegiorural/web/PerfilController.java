package com.colegiorural.web;

import com.colegiorural.servicio.AuthService;
import com.colegiorural.servicio.ValidacionException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PerfilController {
    private final AuthService auth;

    public PerfilController(AuthService auth) { this.auth = auth; }

    @GetMapping("/perfil")
    public String ver() { return "perfil"; }

    @PostMapping("/perfil")
    public String cambiar(@RequestParam(defaultValue = "") String actual, @RequestParam(defaultValue = "") String nueva,
                          HttpServletRequest req, Model m) {
        try {
            auth.cambiarPassword(Sesion.usuario(req).orElseThrow().id(), actual, nueva);
            Sesion.ok(req, "Contraseña actualizada.");
            return "redirect:/perfil";
        } catch (ValidacionException e) {
            m.addAttribute("error", e.getMessage());
            return "perfil";
        }
    }
}
