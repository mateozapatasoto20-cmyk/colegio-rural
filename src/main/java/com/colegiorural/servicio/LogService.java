package com.colegiorural.servicio;

import com.colegiorural.dao.LogDao;
import com.colegiorural.dominio.Pagina;
import com.colegiorural.dominio.Registro;
import com.colegiorural.dominio.SesionUsuario;
import org.springframework.stereotype.Service;

/* SRP: solo escribe y lee la bitácora de actividad. */
@Service
public class LogService {
    private final LogDao dao;
    private final UsuarioActual usuario;

    public LogService(LogDao dao, UsuarioActual usuario) { this.dao = dao; this.usuario = usuario; }

    public void registrar(String accion) {
        dao.registrar(usuario.actual().map(SesionUsuario::username).orElse("—"), accion);
    }

    public Pagina<Registro> listar(int pagina, int porPagina) {
        return new Pagina<>(dao.listar(porPagina, (pagina - 1) * porPagina), dao.contar());
    }
}
