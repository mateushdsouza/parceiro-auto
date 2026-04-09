package br.com.parceiroauto.view;

import br.com.parceiroauto.confg.JPAUtil;
import br.com.parceiroauto.entity.Company;
import br.com.parceiroauto.entity.User;
import br.com.parceiroauto.entity.UserCompany;
import br.com.parceiroauto.entity.UserCompanyRole;
import br.com.parceiroauto.repository.CompanyRepository;
import br.com.parceiroauto.repository.UserCompanyRepository;
import br.com.parceiroauto.repository.UserRepository;
import br.com.parceiroauto.service.CompanyService;
import br.com.parceiroauto.service.UserCompanyService;
import br.com.parceiroauto.service.UserService;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Scanner;

public class View {

    public static void start() {
        EntityManager em = JPAUtil.getEntityManager();
        Scanner sc = new Scanner(System.in);

        try {
            UserRepository userRepository = new UserRepository(em);
            CompanyRepository companyRepository = new CompanyRepository(em);
            UserCompanyRepository userCompanyRepository = new UserCompanyRepository(em);

            UserService userService = new UserService(userRepository);
            CompanyService companyService = new CompanyService(companyRepository);
            UserCompanyService userCompanyService = new UserCompanyService(userCompanyRepository);

            while (true) {
                User loggedUser = menuLogin(sc, userService);
                if (loggedUser == null) {
                    return;
                }

                MenuResult result = menuEmpresas(
                        sc,
                        loggedUser,
                        companyService,
                        companyRepository,
                        userCompanyService,
                        userCompanyRepository,
                        userService
                );

                if (result == MenuResult.SAIR) {
                    return;
                }
            }
        } finally {
            em.close();
        }
    }

    private static User menuLogin(Scanner sc, UserService userService) {
        while (true) {
            System.out.println();
            System.out.println("=== Login ===");
            System.out.println("1 - Fazer login");
            System.out.println("2 - Fazer cadastro");
            System.out.println("3 - Sair");
            int opcao = readInt(sc);

            switch (opcao) {
                case 1: {
                    System.out.println("Digite seu login:");
                    String login = sc.nextLine().trim();

                    System.out.println("Digite sua senha:");
                    String senha = sc.nextLine().trim();

                    try {
                        User user = userService.authenticate(login, senha);
                        if (user != null) {
                            System.out.println("Login realizado com sucesso!");
                            return user;
                        }
                        System.out.println("Login ou senha incorretos!");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                }

                case 2: {
                    System.out.println("Digite um login:");
                    String novoLogin = sc.nextLine().trim();

                    System.out.println("Digite uma senha:");
                    String novaSenha = sc.nextLine().trim();

                    try {
                        userService.createUser(novoLogin, novaSenha);
                        System.out.println("Usuario cadastrado com sucesso!");
                        System.out.println("Agora faca login para continuar.");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                }

                case 3:
                    System.out.println("Saindo...");
                    return null;

                default:
                    System.out.println("Digite uma opcao valida!");
            }
        }
    }

    private static MenuResult menuEmpresas(
            Scanner sc,
            User user,
            CompanyService companyService,
            CompanyRepository companyRepository,
            UserCompanyService userCompanyService,
            UserCompanyRepository userCompanyRepository,
            UserService userService
    ) {
        while (true) {
            System.out.println();
            System.out.println("=== Empresas (usuario: " + user.getLogin() + ") ===");

            List<UserCompany> links = userCompanyRepository.findByUser(user);
            if (links.isEmpty()) {
                System.out.println("Nenhuma empresa vinculada.");
            } else {
                for (int i = 0; i < links.size(); i++) {
                    Company company = links.get(i).getCompany();
                    System.out.println((i + 1) + " - " + company.getNomeFantasia() + " (CNPJ: " + company.getCnpj() + ")"
                            + " [" + links.get(i).getRole() + "]");
                }
            }

            System.out.println();
            System.out.println("1 - Entrar em uma empresa");
            System.out.println("2 - Cadastrar empresa");
            System.out.println("3 - Remover empresa");
            System.out.println("4 - Logout");
            System.out.println("5 - Sair");
            int opcao = readInt(sc);

            switch (opcao) {
                case 1: {
                    if (links.isEmpty()) {
                        System.out.println("Voce ainda nao tem empresas para entrar.");
                        break;
                    }

                    System.out.println("Digite o numero da empresa:");
                    int idx = readInt(sc) - 1;
                    if (idx < 0 || idx >= links.size()) {
                        System.out.println("Empresa invalida.");
                        break;
                    }

                    Company company = links.get(idx).getCompany();
                    UserCompanyRole role = links.get(idx).getRole();
                    menuEmpresa(sc, user, company, role, userCompanyRepository, userService);
                    break;
                }

                case 2: {
                    System.out.println("CNPJ (somente numeros):");
                    String cnpj = sc.nextLine().trim();

                    System.out.println("Razao Social:");
                    String razaoSocial = sc.nextLine().trim();

                    System.out.println("Nome Fantasia:");
                    String nomeFantasia = sc.nextLine().trim();

                    try {
                        Company company = companyService.createCompany(cnpj, razaoSocial, nomeFantasia);
                        userCompanyService.linkUserToCompany(user, company, UserCompanyRole.OWNER);
                        System.out.println("Empresa cadastrada com sucesso! Voce foi vinculado como OWNER.");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                }

                case 3: {
                    if (links.isEmpty()) {
                        System.out.println("Voce nao tem empresas para remover.");
                        break;
                    }

                    System.out.println("Digite o numero da empresa para remover:");
                    int idx = readInt(sc) - 1;
                    if (idx < 0 || idx >= links.size()) {
                        System.out.println("Empresa invalida.");
                        break;
                    }

                    Company company = links.get(idx).getCompany();
                    UserCompanyRole role = links.get(idx).getRole();

                    if (role != UserCompanyRole.OWNER) {
                        System.out.println("Voce nao pode remover essa empresa (somente OWNER).");
                        break;
                    }

                    try {
                        userCompanyRepository.deleteByCompany(company);
                        companyRepository.delete(company);
                        System.out.println("Empresa removida com sucesso.");
                    } catch (Exception e) {
                        System.out.println("Nao foi possivel remover a empresa. Verifique se existem contas/transacoes vinculadas.");
                    }

                    break;
                }

                case 4:
                    return MenuResult.LOGOUT;

                case 5:
                    return MenuResult.SAIR;

                default:
                    System.out.println("Digite uma opcao valida!");
            }
        }
    }

    private static void menuEmpresa(
            Scanner sc,
            User user,
            Company company,
            UserCompanyRole role,
            UserCompanyRepository userCompanyRepository,
            UserService userService
    ) {
        while (true) {
            System.out.println();
            System.out.println("=== Empresa: " + company.getNomeFantasia() + " (" + role + ") ===");
            System.out.println("1 - Contas bancarias");
            System.out.println("2 - Funcionarios");
            System.out.println("3 - Relatorios");
            System.out.println("4 - Voltar");

            int opcao = readInt(sc);
            switch (opcao) {
                case 1:
                    if (!canAccessBankAccounts(role)) {
                        System.out.println("Voce nao pode acessar contas bancarias com seu perfil.");
                        break;
                    }
                    System.out.println("Em construcao: menu de contas bancarias.");
                    break;

                case 2:
                    if (!canManageEmployees(role)) {
                        System.out.println("Voce nao pode acessar funcionarios com seu perfil.");
                        break;
                    }
                    System.out.println("Em construcao: menu de funcionarios.");
                    break;

                case 3:
                    if (!canAccessReports(role)) {
                        System.out.println("Voce nao pode acessar relatorios com seu perfil.");
                        break;
                    }
                    System.out.println("Em construcao: menu de relatorios.");
                    break;

                case 4:
                    return;

                default:
                    System.out.println("Digite uma opcao valida!");
            }
        }
    }

    private static boolean canManageEmployees(UserCompanyRole role) {
        return role == UserCompanyRole.OWNER || role == UserCompanyRole.MANAGER;
    }

    private static boolean canAccessBankAccounts(UserCompanyRole role) {
        return role == UserCompanyRole.OWNER
                || role == UserCompanyRole.MANAGER
                || role == UserCompanyRole.INVESTMENT_MANAGER;
    }

    private static boolean canAccessReports(UserCompanyRole role) {
        return role == UserCompanyRole.OWNER
                || role == UserCompanyRole.MANAGER
                || role == UserCompanyRole.INVESTMENT_MANAGER
                || role == UserCompanyRole.VIEWER;
    }

    private static int readInt(Scanner sc) {
        while (true) {
            try {
                int value = sc.nextInt();
                sc.nextLine();
                return value;
            } catch (java.util.InputMismatchException e) {
                sc.nextLine();
                System.out.println("Digite um numero valido:");
            }
        }
    }

    private enum MenuResult {
        LOGOUT,
        SAIR
    }
}
