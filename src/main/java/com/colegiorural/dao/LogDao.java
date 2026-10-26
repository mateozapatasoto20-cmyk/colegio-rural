package com.colegiorural.dao;

import com.colegiorural.dominio.Registro;
import java.util.List;

public interface LogDao {
    void registrar(String usuario, String accion);
    List<Registro> listar(int limite, int offset);
    int contar();
}
