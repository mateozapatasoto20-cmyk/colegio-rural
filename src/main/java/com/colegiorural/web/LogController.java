package com.colegiorural.web;

import com.colegiorural.servicio.LogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LogController {
    private static final int POR_PAGINA = 12;
    private final LogService logs;

    public LogController(LogService logs) { this.logs = logs; }

    @GetMapping("/logs")
    public String ver(@RequestParam(defaultValue = "1") String p, Model m) {
        int pagina = P.pagina(p);
        m.addAttribute("r", logs.listar(pagina, POR_PAGINA));
        m.addAttribute("pagina", pagina);
        m.addAttribute("porPagina", POR_PAGINA);
        return "logs";
    }
}
