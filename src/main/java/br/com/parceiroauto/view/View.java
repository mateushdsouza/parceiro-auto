package br.com.parceiroauto.view;

import br.com.parceiroauto.entity.User;
import br.com.parceiroauto.repository.UserRepository;
import br.com.parceiroauto.service.UserService;

import java.util.Scanner;

public class View {

    public static User menuLogin() {
        UserService userService = new UserService(new UserRepository());
        Scanner sc = new Scanner(System.in);

        int opcaoMenuLogin;

        do {
            System.out.println("1 - Fazer login");
            System.out.println("2 - Fazer cadastro");
            System.out.println("3 - Sair");
            opcaoMenuLogin = sc.nextInt();

            switch (opcaoMenuLogin) {
                case 1:
                    System.out.println("Digite seu login:");
                    String login = sc.next();

                    System.out.println("Digite sua senha:");
                    String senha = sc.next();

                    try {
                        User user = userService.authenticate(login, senha);

                        if (user != null) {
                            System.out.println("Login realizado com sucesso!");
                            return user;
                        } else {
                            System.out.println("Login ou senha incorretos!");
                        }

                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }

                    break;

                case 2:
                    System.out.println("Digite um login:");
                    String novoLogin = sc.next();

                    System.out.println("Digite uma senha:");
                    String novaSenha = sc.next();

                    try {
                        User novoUser = userService.createUser(novoLogin, novaSenha);

                        System.out.println("Usuario cadastrado com sucesso!");
                        System.out.println("Bem-vindo, " + novoUser.getLogin() + "!");

                        return novoUser;

                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }

                    break;

                case 3:
                    System.out.println("Saindo...");
                    return null;

                default:
                    System.out.println("Digite uma opção válida!");
            }

        } while (true);
    }
}