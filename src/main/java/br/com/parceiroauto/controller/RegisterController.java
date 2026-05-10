package br.com.parceiroauto.controller;

import br.com.parceiroauto.entity.User;
import br.com.parceiroauto.service.UserService;

public class RegisterController {
    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    public User register(String login, String password) {
        return userService.createUser(login, password);
    }
}
