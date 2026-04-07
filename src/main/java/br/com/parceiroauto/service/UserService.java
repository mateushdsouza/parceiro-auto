package br.com.parceiroauto.service;

import br.com.parceiroauto.entity.User;
import br.com.parceiroauto.repository.UserRepository;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String login, String password) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Login nao pode ser vazio");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Senha nao pode ser vazia");
        }

        User existingUser = userRepository.findByLogin(login);
        if (existingUser != null) {
            throw new IllegalArgumentException("Ja existe um usuario com esse login");
        }

        User user = new User(login, password);
        userRepository.save(user);
        return user;
    }

    public User authenticate(String login, String password) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Login nao pode ser vazio");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Senha nao pode ser vazia");
        }

        User user = userRepository.findByLogin(login);
        if (user == null) {
            return null;
        }

        if (!user.getPassword().equals(password)) {
            return null;
        }

        return user;
    }

    public User findById(Long id) {
        return userRepository.findById(id);
    }

    public User findByLogin(String login) {
        return userRepository.findByLogin(login);
    }
}
