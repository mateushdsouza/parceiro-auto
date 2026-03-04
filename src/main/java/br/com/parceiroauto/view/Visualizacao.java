package br.com.parceiroauto.view;

import br.com.parceiroauto.model.Empresa;
import br.com.parceiroauto.model.Usuario;
import br.com.parceiroauto.repository.UserRepository;
import br.com.parceiroauto.service.EmpresaService;
import br.com.parceiroauto.service.UserService;

import java.util.Scanner;

public class Visualizacao {

    public static Usuario menuLogin() {
        UserRepository repo = new UserRepository();
        UserService userService = new UserService(repo);
        Scanner scanner = new Scanner(System.in);
        Usuario usuarioLogado = null;
        String user;
        String senha;

        while (usuarioLogado == null) {
            System.out.println("Digite o nome do usuario:");
            user = scanner.nextLine();
            System.out.println("Digite a senha do usuario:");
            senha = scanner.nextLine();

            usuarioLogado = userService.fazerLogin(user, senha);

            if (usuarioLogado == null) {
                System.out.println("Usuário ou senha incorretos! Tente novamente.");
            }
        }

        System.out.println("Bem-vindo, " + usuarioLogado.getUser() + "!");
        return usuarioLogado;


    }

    public static void menuPrincipal() {
        EmpresaService empresaService = new EmpresaService();
        Scanner scanner = new Scanner(System.in);
        Usuario usuarioLogado = menuLogin();

        int escolha = -1;
        while (escolha != 0) {
            System.out.println("\n--- SELECIONE UMA EMPRESA ---");
            int i;
            for (i = 0; i < usuarioLogado.getEmpresas().size(); i++) {
                System.out.println((i + 1) + " - " + usuarioLogado.getEmpresas().get(i).getNome());
            }

            int opcaoCadastrar = i + 1;
            int opcaoRemover = i + 2;
            int opcaoSair = i + 3;

            System.out.println(opcaoCadastrar + " - Cadastrar nova empresa");
            System.out.println(opcaoRemover + " - Remover empresa");
            System.out.println(opcaoSair + " - Sair");
            System.out.print("Escolha: ");

            escolha = scanner.nextInt();
            scanner.nextLine(); // Limpar buffer

            if (escolha > 0 && escolha <= usuarioLogado.getEmpresas().size()) {
                Empresa empresaAtiva = usuarioLogado.getEmpresas().get(escolha - 1);
                System.out.println("Entrando na empresa: " + empresaAtiva.getNome());

                int opcaoContabil = -1;
                while (opcaoContabil != 4) {
                    System.out.println("\n--- " + empresaAtiva.getNome().toUpperCase() + " ---");
                    System.out.println("1 - Registro de Entrada");
                    System.out.println("2 - Registro de Saida");
                    System.out.println("3 - Relatorio");
                    System.out.println("4 - Voltar ao menu de empresas");
                    System.out.println("5 - Sair");
                    System.out.print("Escolha: ");

                    opcaoContabil = scanner.nextInt();
                    scanner.nextLine(); // Limpar buffer

                    switch (opcaoContabil) {
                        case 1:
                            System.out.println("Registrando Entrada...");
                            // FALTA FAZER
                            break;
                        case 2:
                            System.out.println("Registrando Saída...");
                            // FALTA FAZER
                            break;
                        case 3:
                            System.out.println("--- RELATÓRIO FINANCEIRO ---");
                            // FALTA FAZER
                            break;
                        case 4:
                            System.out.println("Voltando...");
                            break;
                        case 5:
                            System.out.println("Encerrando sistema...");
                            System.exit(0);
                            break;
                        default:
                            System.out.println("Opção inválida!");
                    }
                }
            } else if (escolha == opcaoCadastrar) {
                System.out.print("Nome da Empresa: ");
                String nomeEmp = scanner.nextLine();
                System.out.print("CNPJ: ");
                String cnpjEmp = scanner.nextLine();

                empresaService.cadastrarEmpresa(usuarioLogado, nomeEmp, cnpjEmp);

            } else if (escolha == opcaoRemover) {
                int idRemover;
                for (i = 0; i < usuarioLogado.getEmpresas().size(); i++) {
                    System.out.println((i + 1) + " - " + usuarioLogado.getEmpresas().get(i).getNome());
                }
                System.out.println("Qual empresa deseja remover?(digite o id): ");
                idRemover = scanner.nextInt();
                scanner.nextLine(); // limpar buffer

                if (idRemover <= 0 || idRemover > usuarioLogado.getEmpresas().size()) {
                    System.out.println("ID inválido!");
                } else {


                    System.out.println("Digite o CNPJ para confirmar a remoção: ");
                    String cnpjConfirmacao = scanner.nextLine();

                    empresaService.removerEmpresa(usuarioLogado, cnpjConfirmacao);
                }

            } else if (escolha == opcaoSair) {
                System.out.println("Saindo...");
                break;
            } else {
                System.out.println("Opção inválida!");
            }
        }
    }
}
