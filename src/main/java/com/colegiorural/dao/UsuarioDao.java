package com.colegiorural.dao;

import com.colegiorural.dominio.Usuario;
import com.colegiorural.dominio.UsuarioDetalle;
import java.util.List;
import java.util.Optional;

public interface UsuarioDao {
    Optional<Usuario> buscarPorId(long id);
    Optional<Usuario> buscarPorUsername(String username);
    List<UsuarioDetalle> listar();
    long crear(Usuario datos);
    void cambiarPassword(long id, String hash);
    void cambiarEstado(long id, boolean activo);
}
