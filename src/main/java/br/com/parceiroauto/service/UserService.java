package br.com.parceiroauto.service;

import br.com.parceiroauto.model.Usuario;
import br.com.parceiroauto.repository.UserRepository;

public class UserService {
    private UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public boolean login(String userDigitado, String senhaDigitada) {
        Usuario userEncontrado = repo.buscarPorUser(userDigitado);

        if (userEncontrado == null) {
            return false;
        }

        return userEncontrado.getSenha().equals(senhaDigitada);
    }
}
