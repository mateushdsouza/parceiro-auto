package br.com.parceiroauto.model.service;

import br.com.parceiroauto.model.entity.User;
import br.com.parceiroauto.model.repository.UserRepository;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String login, String password) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Login nao pode ser vazio");
        }

        String normalizedLogin = login.trim();

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Senha nao pode ser vazia");
        }

        User existingUser = userRepository.findByLogin(normalizedLogin);
        if (existingUser != null) {
            throw new IllegalArgumentException("Ja existe um usuario com esse login");
        }

        User user = new User(normalizedLogin, password);
        userRepository.save(user);
        return user;
    }

    public User authenticate(String login, String password) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Login nao pode ser vazio");
        }

        String normalizedLogin = login.trim();

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Senha nao pode ser vazia");
        }

        User user = userRepository.findByLogin(normalizedLogin);
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
        if (login == null || login.isBlank()) {
            return null;
        }

        return userRepository.findByLogin(login.trim());
    }
}
