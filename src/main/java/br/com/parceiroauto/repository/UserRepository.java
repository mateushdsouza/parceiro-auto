package br.com.parceiroauto.repository;
import br.com.parceiroauto.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private List<Usuario> usuarios = new ArrayList<>();

    public UserRepository() {
        this.usuarios = new ArrayList<>();
        this.usuarios.add(new Usuario("Carlos", "Carlos123"));
        this.usuarios.add(new Usuario("admin", "1"));
    }

    public Usuario buscarPorUser(String user) {
        for (Usuario u : usuarios) {
            if (u.getUser().equals(user)) {
                return u;
            }
        }
        return null;
    }
}
