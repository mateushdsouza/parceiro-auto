package br.com.parceiroauto.controller;

import br.com.parceiroauto.entity.User;
import br.com.parceiroauto.service.UserService;

public class LoginController {
    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    public User login(String login, String senha) {
        return userService.authenticate(login, senha);
    }
}
