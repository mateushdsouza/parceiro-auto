package br.com.parceiroauto.view;

import br.com.parceiroauto.entity.User;
import br.com.parceiroauto.repository.UserRepository;
import br.com.parceiroauto.service.UserService;

import java.util.Scanner;

public class View {
    public static User menuLogin() {
        UserService userService = new UserService(new UserRepository());
        Scanner sc = new Scanner(System.in);

        System.out.println("1 - Fazer login");
        System.out.println("2 - Fazer cadastro");
        System.out.println("3 - Sair");
        int opcaoMenuLogin = sc.nextInt();

        do {
            switch (opcaoMenuLogin) {
                case 1:

                    break;

                case 2:
                    break;

                default:
                    System.out.println("Digite uma opcao valida!");
                    break;
            }
        } while (opcaoMenuLogin != 3);
    }
}
