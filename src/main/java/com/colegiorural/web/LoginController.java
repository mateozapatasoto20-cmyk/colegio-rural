package com.colegiorural.web;

import com.colegiorural.dominio.SesionUsuario;
import com.colegiorural.servicio.AuthService;
import com.colegiorural.servicio.ValidacionException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    private final AuthService auth;

    public LoginController(AuthService auth) { this.auth = auth; }

    @GetMapping("/")
    public String raiz() { return "redirect:/login"; }

    @GetMapping("/login")
    public String formulario() { return "login"; }

    @PostMapping("/login")
    public String ingresar(@RequestParam(defaultValue = "") String usuario, @RequestParam(defaultValue = "") String clave,
                           HttpServletRequest req, Model m) {
        try {
            SesionUsuario s = auth.ingresar(P.texto(usuario), clave, req.getRemoteAddr());
            Sesion.iniciar(req, s);
            auth.registrarIngreso();
            return "redirect:" + AuthService.inicio(s.rol());
        } catch (ValidacionException e) {
            m.addAttribute("error", e.getMessage());
            m.addAttribute("usuario", P.texto(usuario));
            return "login";
        }
    }

    @PostMapping("/logout")
    public String salir(HttpServletRequest req) {
        auth.registrarSalida();
        Sesion.cerrar(req);
        return "redirect:/login";
    }
}
