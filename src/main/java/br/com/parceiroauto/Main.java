package br.com.parceiroauto;

import br.com.parceiroauto.repository.UserRepository;
import br.com.parceiroauto.service.UserService;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        UserRepository repo = new UserRepository();
        UserService service = new UserService(repo);
        Scanner scanner = new Scanner(System.in);
        boolean logado = false;
        String user;
        String senha;

        while (!logado) {
            System.out.println("Digite o nome do usuario:");
            user = scanner.nextLine();
            System.out.println("Digite a senha do usuario:");
            senha = scanner.nextLine();

            logado = service.login(user, senha);

            if (!logado) {
                System.out.println("Usuário ou senha incorretos! Tente novamente.");
            }
        }
        // Selecao de empresa || Cadastro de nova empresa
    }
}
