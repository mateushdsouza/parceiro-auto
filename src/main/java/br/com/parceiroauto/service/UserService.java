package br.com.parceiroauto.service;

import br.com.parceiroauto.entity.Usuario;
import br.com.parceiroauto.repository.UserRepository;

public class UserService {
    private UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public Usuario fazerLogin(String userDigitado, String senhaDigitada) {
        Usuario userEncontrado = repo.buscarPorUser(userDigitado);

        if (userEncontrado != null && userEncontrado.getSenha().equals(senhaDigitada)) {
            return userEncontrado;
        }

        return null;
    }
}
