package com.colegiorural.web;

import com.colegiorural.config.AppProperties;
import com.colegiorural.dao.FiltroEstudiante;
import com.colegiorural.servicio.AsistenciaService;
import com.colegiorural.servicio.EstudianteService;
import com.colegiorural.servicio.ValidacionException;
import jakarta.servlet.http.HttpServletRequest;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class EstudianteController {
    private final EstudianteService estudiantes;
    private final AsistenciaService asistencia;
    private final AppProperties app;

    public EstudianteController(EstudianteService estudiantes, AsistenciaService asistencia, AppProperties app) {
        this.estudiantes = estudiantes; this.asistencia = asistencia; this.app = app;
    }

    @GetMapping("/estudiantes")
    public String listar(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "") String grado,
                         @RequestParam(defaultValue = "") String estado, @RequestParam(defaultValue = "apellidos") String orden,
                         @RequestParam(defaultValue = "1") String p, @RequestParam(required = false) String nuevo, Model m) {
        int pagina = P.pagina(p);
        var r = estudiantes.listar(new FiltroEstudiante(P.texto(q), grado, estado, orden), pagina, app.porPagina());
        m.addAttribute("r", r);
        m.addAttribute("f", Map.of("q", P.texto(q), "grado", grado, "estado", estado, "orden", orden));
        m.addAttribute("pagina", pagina);
        m.addAttribute("porPagina", app.porPagina());
        m.addAttribute("grados", app.grados());
        m.addAttribute("secciones", app.secciones());
        if (nuevo != null && !m.containsAttribute("abrirDialogo")) {
            m.addAttribute("abrirDialogo", Map.of("abrir", "dlgEst", "titulo", "Nuevo estudiante"));
        }
        return "estudiantes";
    }

    @PostMapping("/estudiantes")
    public String accion(@RequestParam(defaultValue = "") String accion, @RequestParam(defaultValue = "") String id,
                         @RequestParam(defaultValue = "") String nombres, @RequestParam(defaultValue = "") String apellidos,
                         @RequestParam(defaultValue = "") String grado, @RequestParam(defaultValue = "") String seccion,
                         @RequestParam(defaultValue = "") String dni, @RequestParam(required = false) MultipartFile archivo,
                         HttpServletRequest req) throws IOException {
        long idNum = P.entero(id, 0);
        try {
            switch (accion) {
                case "guardar" -> {
                    estudiantes.guardar(idNum == 0 ? null : idNum, nombres, apellidos, grado, seccion, dni);
                    Sesion.ok(req, idNum == 0 ? "Estudiante registrado." : "Estudiante actualizado.");
                }
                case "importar" -> {
                    if (archivo == null || archivo.isEmpty()) { throw new ValidacionException("Elige un archivo CSV."); }
                    var r = estudiantes.importarCsv(archivo.getInputStream());
                    String omit = r.omitidos().isEmpty() ? "." : ", " + r.omitidos().size() + " omitidos (" + Joiner.on(" | ").join(Iterables.limit(r.omitidos(), 3)) + ").";
                    Sesion.ok(req, "Importación lista: " + r.creados() + " nuevos" + omit);
                }
                case "estado" -> { estudiantes.cambiarEstado(idNum); Sesion.ok(req, "Estado actualizado."); }
                case "eliminar" -> { estudiantes.eliminar(idNum); Sesion.ok(req, "Estudiante eliminado."); }
                default -> { }
            }
        } catch (ValidacionException e) {
            if ("guardar".equals(accion)) {
                Map<String, String> datos = new LinkedHashMap<>();
                datos.put("id", id); datos.put("nombres", nombres); datos.put("apellidos", apellidos);
                datos.put("grado", grado); datos.put("seccion", seccion); datos.put("dni", dni);
                Sesion.dialogo(req, "dlgEst", idNum == 0 ? "Nuevo estudiante" : "Editar estudiante", datos, e.getMessage());
            } else {
                Sesion.error(req, e.getMessage());
            }
        }
        return Sesion.volver(req);
    }

    @GetMapping("/estudiantes/plantilla")
    public ResponseEntity<byte[]> plantilla() {
        String csv = "﻿nombres,apellidos,grado,seccion,dni\nJuan,Pérez Gómez,1ro Secundaria,A,70000099\n";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=utf-8")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"plantilla-estudiantes.csv\"")
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/estudiantes/{id:\\d+}")
    public String ficha(@PathVariable long id, @RequestParam(required = false) String mes, @RequestParam(defaultValue = "") String curso,
                        HttpServletRequest req, Model m) {
        var ficha = estudiantes.ficha(id);
        if (ficha.isEmpty()) {
            Sesion.error(req, "No se encontró al estudiante.");
            return "redirect:/dashboard";
        }
        long cursoId = P.entero(curso, 0);
        m.addAttribute("ficha", ficha.get());
        m.addAttribute("as", asistencia.resumenEstudiante(id));
        m.addAttribute("cal", asistencia.calendario(id, mes == null ? java.time.YearMonth.now().toString() : mes, cursoId == 0 ? null : cursoId));
        m.addAttribute("cursoCal", cursoId);
        m.addAttribute("puedeEditar", Sesion.usuario(req).map(u -> u.rol().equals("admin") || u.rol().equals("docente")).orElse(false));
        return "estudiante";
    }
}
