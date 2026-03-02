package br.com.parceiroauto.repository;

import br.com.parceiroauto.model.Empresa;
import br.com.parceiroauto.model.Usuario;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private List<Usuario> usuarios = new ArrayList<>();

    public UserRepository() {
        Usuario carlos = new Usuario("Carlos", "1234");
        Usuario admin = new Usuario("admin", "1");

        Empresa empPadrao = new Empresa("PadariaFOZ", "18.792.916/0001-97");
        Empresa empPadrao2 = new Empresa("BibliotecaFOZ", "82.766.821/0001-06");

        admin.adicionarEmpresa(empPadrao);
        admin.adicionarEmpresa(empPadrao2);

        this.usuarios.add(carlos);
        this.usuarios.add(admin);
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
