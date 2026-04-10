package br.com.parceiroauto.view;

import br.com.parceiroauto.confg.JPAUtil;
import br.com.parceiroauto.entity.BankAccount;
import br.com.parceiroauto.entity.Company;
import br.com.parceiroauto.entity.FrequencyType;
import br.com.parceiroauto.entity.TransactionCategory;
import br.com.parceiroauto.entity.TransactionForm;
import br.com.parceiroauto.entity.TransactionType;
import br.com.parceiroauto.entity.User;
import br.com.parceiroauto.entity.UserCompany;
import br.com.parceiroauto.entity.UserCompanyRole;
import br.com.parceiroauto.repository.BankAccountRepository;
import br.com.parceiroauto.repository.CompanyRepository;
import br.com.parceiroauto.repository.RecurrenceRuleRepository;
import br.com.parceiroauto.repository.TransactionCategoryRepository;
import br.com.parceiroauto.repository.TransactionRepository;
import br.com.parceiroauto.repository.UserCompanyRepository;
import br.com.parceiroauto.repository.UserRepository;
import br.com.parceiroauto.service.BankAccountService;
import br.com.parceiroauto.service.CompanyService;
import br.com.parceiroauto.service.RecurrenceRuleService;
import br.com.parceiroauto.service.TransactionCategoryService;
import br.com.parceiroauto.service.TransactionService;
import br.com.parceiroauto.service.UserCompanyService;
import br.com.parceiroauto.service.UserService;
import br.com.parceiroauto.util.ValidadorCNPJ;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;

public class View {
    private static final DateTimeFormatter FORMATO_DATA_BR = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static void start() {
        EntityManager em = JPAUtil.getEntityManager();
        Scanner sc = new Scanner(System.in);

        try {
            UserRepository userRepository = new UserRepository(em);
            CompanyRepository companyRepository = new CompanyRepository(em);
            UserCompanyRepository userCompanyRepository = new UserCompanyRepository(em);
            BankAccountRepository bankAccountRepository = new BankAccountRepository(em);
            RecurrenceRuleRepository recurrenceRuleRepository = new RecurrenceRuleRepository(em);
            TransactionCategoryRepository transactionCategoryRepository = new TransactionCategoryRepository(em);
            TransactionRepository transactionRepository = new TransactionRepository(em);

            UserService userService = new UserService(userRepository);
            CompanyService companyService = new CompanyService(companyRepository);
            UserCompanyService userCompanyService = new UserCompanyService(userCompanyRepository);
            BankAccountService bankAccountService = new BankAccountService(bankAccountRepository);
            TransactionService transactionService = new TransactionService(transactionRepository, bankAccountRepository);
            RecurrenceRuleService recurrenceRuleService = new RecurrenceRuleService(recurrenceRuleRepository, transactionService);
            TransactionCategoryService transactionCategoryService = new TransactionCategoryService(transactionCategoryRepository);

            while (true) {
                recurrenceRuleService.processPendingRecurrenceRules();

                User loggedUser = menuLogin(sc, userService);
                if (loggedUser == null) {
                    return;
                }

                MenuResult resultado = menuEmpresas(
                        sc,
                        loggedUser,
                        companyService,
                        companyRepository,
                        userCompanyService,
                        userCompanyRepository,
                        userService,
                        bankAccountService,
                        recurrenceRuleService,
                        transactionCategoryService,
                        transactionService
                );

                if (resultado == MenuResult.SAIR) {
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
                    String novoLogin = readNewLogin(sc, userService);
                    String novaSenha = readRequiredText(sc, "Digite uma senha:", "Senha nao pode ser vazia");

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
            UserService userService,
            BankAccountService bankAccountService,
            RecurrenceRuleService recurrenceRuleService,
            TransactionCategoryService transactionCategoryService,
            TransactionService transactionService
    ) {
        while (true) {
            System.out.println();
            System.out.println("=== Empresas (usuario: " + user.getLogin() + ") ===");

            List<UserCompany> links = userCompanyRepository.findByUser(user);
            boolean podeGerenciarEmpresas = podeGerenciarEmpresas(links);
            if (links.isEmpty()) {
                System.out.println("Nenhuma empresa vinculada.");
            } else {
                for (int i = 0; i < links.size(); i++) {
                    Company empresa = links.get(i).getCompany();
                    System.out.println((i + 1) + " - " + empresa.getNomeFantasia() + " (CNPJ: " + empresa.getCnpj() + ")"
                            + " [" + links.get(i).getRole() + "]");
                }
            }

            int proximaOpcao = links.size() + 1;
            int opcaoCadastrar = podeGerenciarEmpresas ? proximaOpcao++ : -1;
            int opcaoAtualizar = podeGerenciarEmpresas ? proximaOpcao++ : -1;
            int opcaoRemover = podeGerenciarEmpresas ? proximaOpcao++ : -1;
            int opcaoLogout = proximaOpcao++;
            int opcaoSair = proximaOpcao;

            System.out.println();
            if (podeGerenciarEmpresas) {
                System.out.println(opcaoCadastrar + " - Cadastrar empresa");
                System.out.println(opcaoAtualizar + " - Atualizar empresa");
                System.out.println(opcaoRemover + " - Remover empresa");
            }
            System.out.println(opcaoLogout + " - Logout");
            System.out.println(opcaoSair + " - Sair");
            int opcao = readInt(sc);

            if (opcao >= 1 && opcao <= links.size()) {
                Company empresa = links.get(opcao - 1).getCompany();
                UserCompanyRole papel = links.get(opcao - 1).getRole();
                menuEmpresa(
                        sc,
                        user,
                        empresa,
                        papel,
                        userCompanyService,
                        userCompanyRepository,
                        userService,
                        bankAccountService,
                        recurrenceRuleService,
                        transactionCategoryService,
                        transactionService
                );
                continue;
            }

            if (opcao == opcaoCadastrar) {
                String cnpj = readValidCnpj(sc, companyService, null);
                String razaoSocial = readRequiredText(sc, "Razao Social:", "Razao Social nao pode ser vazia");
                String nomeFantasia = readRequiredText(sc, "Nome Fantasia:", "Nome Fantasia nao pode ser vazia");

                try {
                    Company empresa = companyService.createCompany(cnpj, razaoSocial, nomeFantasia);
                    userCompanyService.linkUserToCompany(user, empresa, UserCompanyRole.OWNER);
                    System.out.println("Empresa cadastrada com sucesso! Voce foi vinculado como Proprietario.");
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            } else if (opcao == opcaoAtualizar) {
                if (links.isEmpty()) {
                    System.out.println("Voce nao tem empresas para atualizar.");
                    continue;
                }

                System.out.println("Digite o numero da empresa para atualizar:");
                int idx = readInt(sc) - 1;
                if (idx < 0 || idx >= links.size()) {
                    System.out.println("Empresa invalida.");
                    continue;
                }

                Company empresa = links.get(idx).getCompany();
                UserCompanyRole papel = links.get(idx).getRole();
                if (papel != UserCompanyRole.OWNER) {
                    System.out.println("Voce nao pode atualizar essa empresa (somente Proprietario).");
                    continue;
                }

                String cnpj = askToKeepOrUpdate(sc, "CNPJ", empresa.getCnpj());
                String razaoSocial = askToKeepOrUpdate(sc, "Razao Social", empresa.getRazaoSocial());
                String nomeFantasia = askToKeepOrUpdate(sc, "Nome Fantasia", empresa.getNomeFantasia());

                try {
                    companyService.updateCompany(empresa, cnpj, razaoSocial, nomeFantasia);
                    System.out.println("Empresa atualizada com sucesso.");
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            } else if (opcao == opcaoRemover) {
                if (links.isEmpty()) {
                    System.out.println("Voce nao tem empresas para remover.");
                    continue;
                }

                System.out.println("Digite o numero da empresa para remover:");
                int idx = readInt(sc) - 1;
                if (idx < 0 || idx >= links.size()) {
                    System.out.println("Empresa invalida.");
                    continue;
                }

                Company empresa = links.get(idx).getCompany();
                UserCompanyRole papel = links.get(idx).getRole();

                if (papel != UserCompanyRole.OWNER) {
                    System.out.println("Voce nao pode remover essa empresa (somente Proprietario).");
                    continue;
                }

                try {
                    userCompanyRepository.deleteByCompany(empresa);
                    companyRepository.delete(empresa);
                    System.out.println("Empresa removida com sucesso.");
                } catch (Exception e) {
                    System.out.println("Nao foi possivel remover a empresa. Verifique se existem contas/transacoes vinculadas.");
                }
            } else if (opcao == opcaoLogout) {
                return MenuResult.LOGOUT;
            } else if (opcao == opcaoSair) {
                return MenuResult.SAIR;
            } else {
                System.out.println("Digite uma opcao valida!");
            }
        }
    }

    private static void menuEmpresa(
            Scanner sc,
            User user,
            Company empresa,
            UserCompanyRole papel,
            UserCompanyService userCompanyService,
            UserCompanyRepository userCompanyRepository,
            UserService userService,
            BankAccountService bankAccountService,
            RecurrenceRuleService recurrenceRuleService,
            TransactionCategoryService transactionCategoryService,
            TransactionService transactionService
    ) {
        while (true) {
            System.out.println();
            System.out.println("=== Empresa: " + empresa.getNomeFantasia() + " (" + papel + ") ===");
            System.out.println("1 - Contas bancarias");
            System.out.println("2 - Movimentacoes");
            System.out.println("3 - Funcionarios");
            System.out.println("4 - Relatorios");
            System.out.println("5 - Voltar");

            int opcao = readInt(sc);
            switch (opcao) {
                case 1:
                    if (!canAccessBankAccounts(papel)) {
                        System.out.println("Voce nao pode acessar contas bancarias com seu perfil.");
                        break;
                    }
                    menuContasBancarias(
                            sc,
                            empresa,
                            papel,
                            bankAccountService,
                            recurrenceRuleService,
                            transactionCategoryService,
                            transactionService
                    );
                    break;

                case 2:
                    if (!canManageTransactions(papel)) {
                        System.out.println("Voce nao pode registrar movimentacoes com seu perfil.");
                        break;
                    }
                    menuMovimentacoesEmpresa(
                            sc,
                            empresa,
                            papel,
                            bankAccountService,
                            recurrenceRuleService,
                            transactionCategoryService,
                            transactionService
                    );
                    break;

                case 3:
                    if (!canManageEmployees(papel)) {
                        System.out.println("Voce nao pode acessar funcionarios com seu perfil.");
                        break;
                    }
                    menuFuncionario(sc, user, empresa, userService, userCompanyService, userCompanyRepository);
                    break;

                case 4:
                    if (!canAccessReports(papel)) {
                        System.out.println("Voce nao pode acessar relatorios com seu perfil.");
                        break;
                    }
                    exibirRelatorioGeralEmpresa(sc, empresa, bankAccountService, transactionService);
                    break;

                case 5:
                    return;

                default:
                    System.out.println("Digite uma opcao valida!");
            }
        }
    }

    private static boolean canManageEmployees(UserCompanyRole papel) {
        return papel == UserCompanyRole.OWNER;
    }

    private static void menuFuncionario(
            Scanner sc,
            User loggedUser,
            Company empresa,
            UserService userService,
            UserCompanyService userCompanyService,
            UserCompanyRepository userCompanyRepository
    ) {
        while (true) {
            System.out.println();
            System.out.println("=== Funcionarios da empresa: " + empresa.getNomeFantasia() + " ===");
            System.out.println("1 - Ver funcionarios");
            System.out.println("2 - Filtrar funcionarios");
            System.out.println("3 - Cadastrar funcionario");
            System.out.println("4 - Atualizar funcao");
            System.out.println("5 - Remover funcionario");
            System.out.println("6 - Voltar");
            int opcao = readInt(sc);

            switch (opcao) {
                case 1:
                    listarFuncionarios(empresa, userCompanyRepository);
                    break;

                case 2:
                    filtrarFuncionarios(sc, empresa, userCompanyRepository);
                    break;

                case 3:
                    cadastrarFuncionario(sc, empresa, userService, userCompanyService);
                    break;

                case 4:
                    atualizarFuncionario(sc, loggedUser, empresa, userCompanyService, userCompanyRepository);
                    break;

                case 5:
                    removerFuncionario(sc, empresa, userCompanyService, userCompanyRepository);
                    break;

                case 6:
                    return;

                default:
                    System.out.println("Digite uma opcao valida!");
            }
        }
    }

    private static boolean canAccessBankAccounts(UserCompanyRole papel) {
        return papel == UserCompanyRole.OWNER
                || papel == UserCompanyRole.INVESTMENT_MANAGER;
    }

    private static boolean canManageTransactions(UserCompanyRole papel) {
        return papel == UserCompanyRole.OWNER
                || papel == UserCompanyRole.MANAGER
                || papel == UserCompanyRole.INVESTMENT_MANAGER;
    }

    private static void listarFuncionarios(Company empresa, UserCompanyRepository userCompanyRepository) {
        System.out.println();
        System.out.println("== Funcionarios ==");

        List<UserCompany> links = getManagedEmployees(empresa, userCompanyRepository);
        if (links.isEmpty()) {
            System.out.println("Nenhum funcionario cadastrado.");
            return;
        }

        printFuncionarios(links);
    }

    private static void filtrarFuncionarios(
            Scanner sc,
            Company empresa,
            UserCompanyRepository userCompanyRepository
    ) {
        List<UserCompany> links = getManagedEmployees(empresa, userCompanyRepository);
        if (links.isEmpty()) {
            System.out.println("Nenhum funcionario cadastrado.");
            return;
        }

        System.out.println("Filtrar por:");
        System.out.println("1 - Login");
        System.out.println("2 - Funcao");
        int opcao = readInt(sc);

        List<UserCompany> filtrados = new ArrayList<>();

        if (opcao == 1) {
            System.out.println("Digite o login para filtrar:");
            String loginFiltro = sc.nextLine().trim().toLowerCase();

            for (UserCompany vinculo : links) {
                if (vinculo.getUser().getLogin().toLowerCase().contains(loginFiltro)) {
                    filtrados.add(vinculo);
                }
            }
        } else if (opcao == 2) {
            UserCompanyRole roleFiltro;
            try {
                roleFiltro = readUserCompanyRole(sc);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                return;
            }

            for (UserCompany vinculo : links) {
                if (vinculo.getRole() == roleFiltro) {
                    filtrados.add(vinculo);
                }
            }
        } else {
            System.out.println("Opcao de filtro invalida.");
            return;
        }

        System.out.println();
        System.out.println("== Funcionarios filtrados ==");
        if (filtrados.isEmpty()) {
            System.out.println("Nenhum funcionario encontrado para esse filtro.");
            return;
        }

        printFuncionarios(filtrados);
    }

    private static void printFuncionarios(List<UserCompany> links) {
        for (int i = 0; i < links.size(); i++) {
            UserCompany userCompany = links.get(i);
            System.out.println(
                    (i + 1) + " - "
                            + userCompany.getUser().getLogin()
                            + " | Funcao: " + userCompany.getRole()
            );
        }
    }

    private static boolean podeGerenciarEmpresas(List<UserCompany> links) {
        if (links.isEmpty()) {
            return true;
        }

        for (UserCompany link : links) {
            if (link.getRole() == UserCompanyRole.OWNER) {
                return true;
            }
        }

        return false;
    }

    private static void cadastrarFuncionario(
            Scanner sc,
            Company empresa,
            UserService userService,
            UserCompanyService userCompanyService
    ) {
        String login = readNewLogin(sc, userService);
        String senha = readRequiredText(sc, "Senha do funcionario:", "Senha nao pode ser vazia");

        UserCompanyRole role;
        try {
            role = readUserCompanyRole(sc, false);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }

        try {
            User user = userService.createUser(login, senha);
            userCompanyService.linkUserToCompany(user, empresa, role);
            System.out.println("Funcionario cadastrado com sucesso.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void atualizarFuncionario(
            Scanner sc,
            User loggedUser,
            Company empresa,
            UserCompanyService userCompanyService,
            UserCompanyRepository userCompanyRepository
    ) {
        List<UserCompany> links = getManagedEmployees(empresa, userCompanyRepository);
        if (links.isEmpty()) {
            System.out.println("Nenhum funcionario cadastrado para atualizar.");
            return;
        }

        listarFuncionarios(empresa, userCompanyRepository);
        System.out.println("Digite o numero do funcionario para atualizar:");
        int idx = readInt(sc) - 1;
        if (idx < 0 || idx >= links.size()) {
            System.out.println("Funcionario invalido.");
            return;
        }

        UserCompany selectedLink = links.get(idx);
        if (selectedLink.getUser().getId().equals(loggedUser.getId())) {
            System.out.println("Voce nao pode alterar o proprio papel.");
            return;
        }

        UserCompanyRole newRole;
        try {
            newRole = readUserCompanyRole(sc, false);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }

        try {
            userCompanyService.updateRole(selectedLink.getUser(), empresa, newRole);
            System.out.println("Funcao atualizada com sucesso.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void removerFuncionario(
            Scanner sc,
            Company empresa,
            UserCompanyService userCompanyService,
            UserCompanyRepository userCompanyRepository
    ) {
        List<UserCompany> links = getManagedEmployees(empresa, userCompanyRepository);
        if (links.isEmpty()) {
            System.out.println("Nenhum funcionario cadastrado para remover.");
            return;
        }

        listarFuncionarios(empresa, userCompanyRepository);
        System.out.println("Digite o numero do funcionario para remover:");
        int idx = readInt(sc) - 1;
        if (idx < 0 || idx >= links.size()) {
            System.out.println("Funcionario invalido.");
            return;
        }

        UserCompany selectedLink = links.get(idx);
        try {
            userCompanyService.removeLink(selectedLink.getUser(), empresa);
            System.out.println("Funcionario removido da empresa com sucesso.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private static List<UserCompany> getManagedEmployees(
            Company empresa,
            UserCompanyRepository userCompanyRepository
    ) {
        List<UserCompany> links = userCompanyRepository.findByCompany(empresa);
        List<UserCompany> employees = new ArrayList<>();

        for (UserCompany link : links) {
            if (link.getRole() != UserCompanyRole.OWNER) {
                employees.add(link);
            }
        }

        return employees;
    }

    private static UserCompanyRole readUserCompanyRole(Scanner sc) {
        return readUserCompanyRole(sc, true);
    }

    private static UserCompanyRole readUserCompanyRole(Scanner sc, boolean allowOwner) {
        System.out.println("Funcao do funcionario:");
        if (allowOwner) {
            System.out.println("1 - Proprietario");
            System.out.println("2 - Gerente");
            System.out.println("3 - Gerente de Investimentos");
            System.out.println("4 - Visualizador");
        } else {
            System.out.println("1 - Gerente");
            System.out.println("2 - Gerente de Investimentos");
            System.out.println("3 - Visualizador");
        }
        int opcao = readInt(sc);

        if (allowOwner && opcao == 1) {
            return UserCompanyRole.OWNER;
        }

        if (allowOwner && opcao == 2) {
            return UserCompanyRole.MANAGER;
        }
        if (allowOwner && opcao == 3) {
            return UserCompanyRole.INVESTMENT_MANAGER;
        }
        if (allowOwner && opcao == 4) {
            return UserCompanyRole.VIEWER;
        }

        if (!allowOwner && opcao == 1) {
            return UserCompanyRole.MANAGER;
        }
        if (!allowOwner && opcao == 2) {
            return UserCompanyRole.INVESTMENT_MANAGER;
        }
        if (!allowOwner && opcao == 3) {
            return UserCompanyRole.VIEWER;
        }

        throw new IllegalArgumentException(allowOwner
                ? "Funcao invalida"
                : "Funcao invalida. Proprietario nao pode ser atribuido pelo menu de funcionarios");
    }

    private static boolean canAccessReports(UserCompanyRole papel) {
        return papel == UserCompanyRole.OWNER
                || papel == UserCompanyRole.MANAGER
                || papel == UserCompanyRole.INVESTMENT_MANAGER
                || papel == UserCompanyRole.VIEWER;
    }

    private static void menuContasBancarias(
            Scanner sc,
            Company company,
            UserCompanyRole role,
            BankAccountService bankAccountService,
            RecurrenceRuleService recurrenceRuleService,
            TransactionCategoryService transactionCategoryService,
            TransactionService transactionService
    ) {
        while (true) {
            System.out.println();
            System.out.println("=== Contas bancarias da empresa: " + company.getNomeFantasia() + " ===");

            List<BankAccount> contas = bankAccountService.findByCompany(company);
            if (contas.isEmpty()) {
                System.out.println("Nenhuma conta bancaria cadastrada.");
            } else {
                for (int i = 0; i < contas.size(); i++) {
                    BankAccount conta = contas.get(i);
                    BigDecimal saldo = conta.getSaldo() == null ? BigDecimal.ZERO : conta.getSaldo();
                    String contaPadrao = conta.isContaPadrao() ? " [PADRAO]" : "";
                    System.out.println(
                            (i + 1) + " - "
                                    + conta.getBanco()
                                    + " | Agencia: " + conta.getAgencia()
                                    + " | Conta: " + conta.getNumeroConta()
                                    + " | Tipo: " + conta.getTipoConta()
                                    + " | Saldo: " + saldo
                                    + contaPadrao
                    );
                }
            }

            int opcaoCadastrar = contas.size() + 1;
            int opcaoAtualizar = contas.size() + 2;
            int opcaoRemover = contas.size() + 3;
            int opcaoVoltar = contas.size() + 4;

            System.out.println();
            System.out.println(opcaoCadastrar + " - Cadastrar conta bancaria");
            System.out.println(opcaoAtualizar + " - Atualizar conta bancaria");
            System.out.println(opcaoRemover + " - Remover conta bancaria");
            System.out.println(opcaoVoltar + " - Voltar");
            int opcao = readInt(sc);

            if (opcao >= 1 && opcao <= contas.size()) {
                BankAccount contaSelecionada = contas.get(opcao - 1);
                menuContaBancaria(
                        sc,
                        company,
                        contaSelecionada,
                        role,
                        bankAccountService,
                        recurrenceRuleService,
                        transactionCategoryService,
                        transactionService
                );
                continue;
            }

            if (opcao == opcaoCadastrar) {
                String banco = readRequiredText(sc, "Banco:", "Banco nao pode ser vazio");
                String agencia = readValidAgency(sc);
                String numeroConta = readValidAccountNumber(sc);
                String tipoConta = readRequiredText(sc, "Tipo da conta:", "Tipo da conta nao pode ser vazio");
                boolean contaPadrao = readYesNo(sc, "Essa e a conta padrao? 1 - Sim / 2 - Nao");

                try {
                    bankAccountService.createBankAccount(
                            company,
                            banco,
                            agencia,
                            numeroConta,
                            tipoConta,
                            contaPadrao
                    );
                    System.out.println("Conta bancaria cadastrada com sucesso.");
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            } else if (opcao == opcaoAtualizar) {
                if (contas.isEmpty()) {
                    System.out.println("Nenhuma conta bancaria cadastrada para atualizar.");
                    continue;
                }

                System.out.println("Digite o numero da conta bancaria para atualizar:");
                int idx = readInt(sc) - 1;
                if (idx < 0 || idx >= contas.size()) {
                    System.out.println("Conta bancaria invalida.");
                    continue;
                }

                BankAccount conta = contas.get(idx);
                String banco = askToKeepOrUpdate(sc, "Banco", conta.getBanco());
                String agencia = askToKeepOrUpdate(sc, "Agencia", conta.getAgencia());
                String numeroConta = askToKeepOrUpdate(sc, "Numero da conta", conta.getNumeroConta());
                String tipoConta = askToKeepOrUpdate(sc, "Tipo da conta", conta.getTipoConta());
                boolean contaPadrao = askToKeepOrToggleBoolean(sc, "Conta padrao", conta.isContaPadrao());

                try {
                    bankAccountService.updateBankAccount(
                            company,
                            conta,
                            banco,
                            agencia,
                            numeroConta,
                            tipoConta,
                            contaPadrao
                    );
                    System.out.println("Conta bancaria atualizada com sucesso.");
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            } else if (opcao == opcaoRemover) {
                if (contas.isEmpty()) {
                    System.out.println("Nenhuma conta bancaria cadastrada para remover.");
                    continue;
                }

                System.out.println("Digite o numero da conta bancaria para remover:");
                int idx = readInt(sc) - 1;
                if (idx < 0 || idx >= contas.size()) {
                    System.out.println("Conta bancaria invalida.");
                    continue;
                }

                try {
                    bankAccountService.deleteBankAccount(company, contas.get(idx));
                    System.out.println("Conta bancaria removida com sucesso.");
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                } catch (Exception e) {
                    System.out.println("Nao foi possivel remover a conta bancaria. Verifique se existem movimentacoes vinculadas.");
                }
            } else if (opcao == opcaoVoltar) {
                return;
            } else {
                System.out.println("Digite uma opcao valida!");
            }
        }
    }

    private static void menuContaBancaria(
            Scanner sc,
            Company company,
            BankAccount bankAccount,
            UserCompanyRole role,
            BankAccountService bankAccountService,
            RecurrenceRuleService recurrenceRuleService,
            TransactionCategoryService transactionCategoryService,
            TransactionService transactionService
    ) {
        while (true) {
            System.out.println();
            System.out.println("=== Conta bancaria: " + bankAccount.getBanco() + " / " + bankAccount.getNumeroConta() + " ===");
            System.out.println("1 - Categorias");
            System.out.println("2 - Movimentacoes");
            System.out.println("3 - Dados da conta");
            System.out.println("4 - Relatorio da conta");
            System.out.println("5 - Definir como conta padrao");
            System.out.println("6 - Voltar");

            int opcao = readInt(sc);
            switch (opcao) {
                case 1:
                    menuCategorias(sc, company, transactionCategoryService);
                    break;

                case 2:
                    menuMovimentacoesEmpresa(
                            sc,
                            company,
                            role,
                            bankAccountService,
                            recurrenceRuleService,
                            transactionCategoryService,
                            transactionService
                    );
                    break;

                case 3:
                    if (!canViewBankAccountDetails(role)) {
                        System.out.println("Voce nao pode visualizar os dados da conta com seu perfil.");
                        break;
                    }
                    exibirDadosDaConta(bankAccount);
                    break;

                case 4:
                    exibirRelatorioConta(sc, bankAccount, transactionService);
                    break;

                case 5:
                    if (!canViewBankAccountDetails(role)) {
                        System.out.println("Voce nao pode alterar a conta padrao com seu perfil.");
                        break;
                    }
                    try {
                        bankAccountService.defineDefaultAccount(company, bankAccount);
                        bankAccount.setContaPadrao(true);
                        System.out.println("Conta padrao atualizada com sucesso.");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                    break;

                case 6:
                    return;

                default:
                    System.out.println("Digite uma opcao valida!");
            }
        }
    }

    private static void exibirRelatorioConta(
            Scanner sc,
            BankAccount bankAccount,
            TransactionService transactionService
    ) {
        List<br.com.parceiroauto.entity.Transaction> movimentacoes = transactionService.findByBankAccount(bankAccount);
        BigDecimal totalEntradas = BigDecimal.ZERO;
        BigDecimal totalSaidas = BigDecimal.ZERO;

        for (br.com.parceiroauto.entity.Transaction movimentacao : movimentacoes) {
            if (movimentacao.getTipo() == TransactionType.ENTRADA) {
                totalEntradas = totalEntradas.add(movimentacao.getValor());
            } else {
                totalSaidas = totalSaidas.add(movimentacao.getValor());
            }
        }

        System.out.println();
        System.out.println("=== Relatorio da conta ===");
        System.out.println("Quantidade de movimentacoes: " + movimentacoes.size());
        System.out.println("Total de entradas: " + totalEntradas);
        System.out.println("Total de saidas: " + totalSaidas);
        System.out.println("Saldo atual: " + bankAccount.getSaldo());
        System.out.println();
        System.out.println("1 - Relatorio completo das movimentacoes");
        System.out.println("2 - Filtrar por entrada");
        System.out.println("3 - Filtrar por saida");
        System.out.println("4 - Filtrar por data");

        int opcao = readInt(sc);
        List<br.com.parceiroauto.entity.Transaction> movimentacoesFiltradas = new ArrayList<>();

        if (opcao == 1) {
            movimentacoesFiltradas = movimentacoes;
        } else if (opcao == 2) {
            for (br.com.parceiroauto.entity.Transaction movimentacao : movimentacoes) {
                if (movimentacao.getTipo() == TransactionType.ENTRADA) {
                    movimentacoesFiltradas.add(movimentacao);
                }
            }
        } else if (opcao == 3) {
            for (br.com.parceiroauto.entity.Transaction movimentacao : movimentacoes) {
                if (movimentacao.getTipo() == TransactionType.SAIDA) {
                    movimentacoesFiltradas.add(movimentacao);
                }
            }
        } else if (opcao == 4) {
            LocalDate dataFiltro = readDate(sc);
            if (dataFiltro == null) {
                return;
            }

            for (br.com.parceiroauto.entity.Transaction movimentacao : movimentacoes) {
                if (movimentacao.getData().equals(dataFiltro)) {
                    movimentacoesFiltradas.add(movimentacao);
                }
            }
        } else {
            System.out.println("Opcao invalida.");
            return;
        }

        if (movimentacoesFiltradas.isEmpty()) {
            System.out.println("Nenhuma movimentacao encontrada para esse filtro.");
            return;
        }

        printMovimentacoes(movimentacoesFiltradas);
    }

    private static void exibirRelatorioGeralEmpresa(
            Scanner sc,
            Company company,
            BankAccountService bankAccountService,
            TransactionService transactionService
    ) {
        List<br.com.parceiroauto.entity.Transaction> movimentacoes = transactionService.findByCompany(company);
        List<BankAccount> contas = bankAccountService.findByCompany(company);
        BigDecimal totalEntradas = BigDecimal.ZERO;
        BigDecimal totalSaidas = BigDecimal.ZERO;
        BigDecimal saldoGeral = BigDecimal.ZERO;

        for (br.com.parceiroauto.entity.Transaction movimentacao : movimentacoes) {
            if (movimentacao.getTipo() == TransactionType.ENTRADA) {
                totalEntradas = totalEntradas.add(movimentacao.getValor());
            } else {
                totalSaidas = totalSaidas.add(movimentacao.getValor());
            }
        }

        for (BankAccount conta : contas) {
            BigDecimal saldoConta = conta.getSaldo() == null ? BigDecimal.ZERO : conta.getSaldo();
            saldoGeral = saldoGeral.add(saldoConta);
        }

        System.out.println();
        System.out.println("=== Balanco geral da empresa ===");
        System.out.println("Quantidade de movimentacoes: " + movimentacoes.size());
        System.out.println("Total de entradas: " + totalEntradas);
        System.out.println("Total de saidas: " + totalSaidas);
        System.out.println("Saldo geral: " + saldoGeral);
        System.out.println();
        System.out.println("1 - Filtro por entrada");
        System.out.println("2 - Filtro por saida");
        System.out.println("3 - Filtro por banco");
        System.out.println("4 - Filtro por data");

        int opcao = readInt(sc);
        List<br.com.parceiroauto.entity.Transaction> movimentacoesFiltradas = new ArrayList<>();

        if (opcao == 1) {
            for (br.com.parceiroauto.entity.Transaction movimentacao : movimentacoes) {
                if (movimentacao.getTipo() == TransactionType.ENTRADA) {
                    movimentacoesFiltradas.add(movimentacao);
                }
            }
        } else if (opcao == 2) {
            for (br.com.parceiroauto.entity.Transaction movimentacao : movimentacoes) {
                if (movimentacao.getTipo() == TransactionType.SAIDA) {
                    movimentacoesFiltradas.add(movimentacao);
                }
            }
        } else if (opcao == 3) {
            String bancoFiltro = chooseBankName(sc, contas);
            if (bancoFiltro == null) {
                return;
            }

            for (br.com.parceiroauto.entity.Transaction movimentacao : movimentacoes) {
                if (movimentacao.getBankAccount().getBanco().equalsIgnoreCase(bancoFiltro)) {
                    movimentacoesFiltradas.add(movimentacao);
                }
            }
        } else if (opcao == 4) {
            LocalDate dataFiltro = readDate(sc);
            if (dataFiltro == null) {
                return;
            }

            for (br.com.parceiroauto.entity.Transaction movimentacao : movimentacoes) {
                if (movimentacao.getData().equals(dataFiltro)) {
                    movimentacoesFiltradas.add(movimentacao);
                }
            }
        } else {
            System.out.println("Opcao invalida.");
            return;
        }

        if (movimentacoesFiltradas.isEmpty()) {
            System.out.println("Nenhuma movimentacao encontrada para esse filtro.");
            return;
        }

        printMovimentacoes(movimentacoesFiltradas);
    }

    private static void menuMovimentacoesEmpresa(
            Scanner sc,
            Company company,
            UserCompanyRole papel,
            BankAccountService bankAccountService,
            RecurrenceRuleService recurrenceRuleService,
            TransactionCategoryService transactionCategoryService,
            TransactionService transactionService
    ) {
        while (true) {
            System.out.println();
            System.out.println("=== Movimentacoes da empresa " + company.getNomeFantasia() + " ===");
            System.out.println("1 - Cadastrar movimentacao");
            System.out.println("2 - Atualizar movimentacao");
            System.out.println("3 - Remover movimentacao");
            System.out.println("4 - Voltar");
            int opcao = readInt(sc);

            switch (opcao) {
                case 1:
                    registrarMovimentacaoEmpresa(
                            sc,
                            company,
                            papel,
                            bankAccountService,
                            recurrenceRuleService,
                            transactionCategoryService,
                            transactionService
                    );
                    break;

                case 2:
                    atualizarMovimentacaoEmpresa(
                            sc,
                            company,
                            papel,
                            bankAccountService,
                            recurrenceRuleService,
                            transactionCategoryService,
                            transactionService
                    );
                    break;

                case 3:
                    removerMovimentacaoEmpresa(sc, company, recurrenceRuleService, transactionService);
                    break;

                case 4:
                    return;

                default:
                    System.out.println("Digite uma opcao valida!");
            }
        }
    }

    private static void registrarMovimentacaoEmpresa(
            Scanner sc,
            Company company,
            UserCompanyRole papel,
            BankAccountService bankAccountService,
            RecurrenceRuleService recurrenceRuleService,
            TransactionCategoryService transactionCategoryService,
            TransactionService transactionService
    ) {
        List<BankAccount> contas = bankAccountService.findByCompany(company);
        if (contas.isEmpty()) {
            System.out.println("Nao existem contas bancarias cadastradas para essa empresa.");
            return;
        }

        BankAccount bankAccount = bankAccountService.findDefaultByCompany(company);
        if (bankAccount == null) {
            System.out.println("Nenhuma conta padrao definida. Escolha a conta para a movimentacao:");
            for (int i = 0; i < contas.size(); i++) {
                BankAccount conta = contas.get(i);
                System.out.println((i + 1) + " - " + conta.getBanco() + " | Conta: " + conta.getNumeroConta());
            }
            System.out.println("0 - Cancelar");

            int opcaoConta = readInt(sc);
            if (opcaoConta == 0) {
                System.out.println("Cadastro de movimentacao cancelado.");
                return;
            }

            int idxConta = opcaoConta - 1;
            if (idxConta < 0 || idxConta >= contas.size()) {
                System.out.println("Conta bancaria invalida.");
                return;
            }

            bankAccount = contas.get(idxConta);
        } else {
            System.out.println("Conta padrao usada: " + bankAccount.getBanco() + " | Conta: " + bankAccount.getNumeroConta());
            System.out.println("Deseja usar outra conta? 1 - Sim / 2 - Nao / 0 - Cancelar");
            int opcaoOutraConta = readInt(sc);

            if (opcaoOutraConta == 0) {
                System.out.println("Cadastro de movimentacao cancelado.");
                return;
            }

            if (opcaoOutraConta == 1) {
                for (int i = 0; i < contas.size(); i++) {
                    BankAccount conta = contas.get(i);
                    System.out.println((i + 1) + " - " + conta.getBanco() + " | Conta: " + conta.getNumeroConta());
                }
                System.out.println("0 - Cancelar");

                int opcaoConta = readInt(sc);
                if (opcaoConta == 0) {
                    System.out.println("Cadastro de movimentacao cancelado.");
                    return;
                }

                int idxConta = opcaoConta - 1;
                if (idxConta < 0 || idxConta >= contas.size()) {
                    System.out.println("Conta bancaria invalida.");
                    return;
                }

                bankAccount = contas.get(idxConta);
            } else if (opcaoOutraConta != 2) {
                System.out.println("Opcao invalida.");
                return;
            }
        }

        TransactionType tipo = escolherTipoMovimentacao(sc, papel);
        if (tipo == null) {
            return;
        }

        String descricao = readRequiredTextOrCancel(sc, "Descricao:", "Descricao nao pode ser vazia");
        if (descricao == null) {
            System.out.println("Cadastro de movimentacao cancelado.");
            return;
        }

        BigDecimal valor = readPositiveBigDecimalOrCancel(sc, "Valor:");
        if (valor == null) {
            System.out.println("Cadastro de movimentacao cancelado.");
            return;
        }

        System.out.println("Forma da movimentacao: 1 - PIX / 2 - CARTAO / 3 - DINHEIRO / 0 - Cancelar");
        int opcaoForma = readInt(sc);
        TransactionForm forma;
        if (opcaoForma == 0) {
            System.out.println("Cadastro de movimentacao cancelado.");
            return;
        } else if (opcaoForma == 1) {
            forma = TransactionForm.PIX;
        } else if (opcaoForma == 2) {
            forma = TransactionForm.CARTAO;
        } else if (opcaoForma == 3) {
            forma = TransactionForm.DINHEIRO;
        } else {
            System.out.println("Forma invalida.");
            return;
        }

        TransactionCategory categoria = escolherCategoriaParaMovimentacao(
                sc,
                company,
                papel,
                tipo,
                transactionCategoryService
        );
        if (categoria == null) {
            return;
        }

        System.out.println("A movimentacao sera recorrente? 1 - Sim / 2 - Nao / 0 - Cancelar");
        int opcaoRecorrencia = readInt(sc);

        FrequencyType frequencia = null;
        if (opcaoRecorrencia == 0) {
            System.out.println("Cadastro de movimentacao cancelado.");
            return;
        } else if (opcaoRecorrencia == 1) {
            try {
                frequencia = readFrequencyType(sc);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                return;
            }
        } else if (opcaoRecorrencia != 2) {
            System.out.println("Opcao invalida para recorrencia.");
            return;
        }

        try {
            br.com.parceiroauto.entity.Transaction transaction = transactionService.createTransaction(
                    company,
                    bankAccount,
                    categoria,
                    tipo,
                    descricao,
                    valor,
                    forma
            );

            if (opcaoRecorrencia == 1) {
                recurrenceRuleService.createRecurrenceRule(
                        transaction,
                        frequencia,
                        LocalDate.now(),
                        null
                );
                System.out.println("Movimentacao recorrente registrada com sucesso.");
            } else {
                System.out.println("Movimentacao registrada com sucesso.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void menuCategorias(
            Scanner sc,
            Company company,
            TransactionCategoryService transactionCategoryService
    ) {
        while (true) {
            System.out.println();
            System.out.println("=== Categorias da empresa: " + company.getNomeFantasia() + " ===");

            List<TransactionCategory> categorias = transactionCategoryService.findByCompany(company);
            if (categorias.isEmpty()) {
                System.out.println("Nenhuma categoria cadastrada.");
            } else {
                for (int i = 0; i < categorias.size(); i++) {
                    TransactionCategory categoria = categorias.get(i);
                    System.out.println(
                            (i + 1) + " - "
                                    + categoria.getName()
                                    + " | Tipo: " + categoria.getTipo()
                                    + " | Ativa: " + categoria.isActive()
                    );
                }
            }

            System.out.println();
            System.out.println("1 - Cadastrar categoria");
            System.out.println("2 - Atualizar categoria");
            System.out.println("3 - Remover categoria");
            System.out.println("4 - Voltar");
            int opcao = readInt(sc);

            switch (opcao) {
                case 1: {
                    String nome = readRequiredText(sc, "Nome da categoria:", "Nome da categoria nao pode ser vazio");

                    System.out.println("Tipo da categoria: 1 - ENTRADA / 2 - SAIDA");
                    int opcaoTipo = readInt(sc);

                    TransactionType tipo;
                    if (opcaoTipo == 1) {
                        tipo = TransactionType.ENTRADA;
                    } else if (opcaoTipo == 2) {
                        tipo = TransactionType.SAIDA;
                    } else {
                        System.out.println("Tipo invalido.");
                        break;
                    }

                    try {
                        transactionCategoryService.createCategory(company, nome, tipo);
                        System.out.println("Categoria cadastrada com sucesso.");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                }

                case 2:
                    if (categorias.isEmpty()) {
                        System.out.println("Nenhuma categoria cadastrada para atualizar.");
                        break;
                    }

                    System.out.println("Digite o numero da categoria para atualizar:");
                    int idxAtualizar = readInt(sc) - 1;
                    if (idxAtualizar < 0 || idxAtualizar >= categorias.size()) {
                        System.out.println("Categoria invalida.");
                        break;
                    }

                    TransactionCategory categoriaAtualizar = categorias.get(idxAtualizar);
                    String nome = askToKeepOrUpdate(sc, "Nome da categoria", categoriaAtualizar.getName());
                    TransactionType tipo = askToKeepOrUpdateType(sc, categoriaAtualizar.getTipo());
                    boolean active = askToKeepOrToggleBoolean(sc, "Categoria ativa", categoriaAtualizar.isActive());

                    try {
                        transactionCategoryService.updateCategory(company, categoriaAtualizar, nome, tipo, active);
                        System.out.println("Categoria atualizada com sucesso.");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                    break;

                case 3:
                    if (categorias.isEmpty()) {
                        System.out.println("Nenhuma categoria cadastrada para remover.");
                        break;
                    }

                    System.out.println("Digite o numero da categoria para remover:");
                    int idx = readInt(sc) - 1;
                    if (idx < 0 || idx >= categorias.size()) {
                        System.out.println("Categoria invalida.");
                        break;
                    }

                    try {
                        transactionCategoryService.deleteCategory(company, categorias.get(idx));
                        System.out.println("Categoria removida com sucesso.");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    } catch (Exception e) {
                        System.out.println("Nao foi possivel remover a categoria. Verifique se existem movimentacoes vinculadas.");
                    }
                    break;

                case 4:
                    return;

                default:
                    System.out.println("Digite uma opcao valida!");
            }
        }
    }

    private static void atualizarMovimentacaoEmpresa(
            Scanner sc,
            Company company,
            UserCompanyRole papel,
            BankAccountService bankAccountService,
            RecurrenceRuleService recurrenceRuleService,
            TransactionCategoryService transactionCategoryService,
            TransactionService transactionService
    ) {
        List<br.com.parceiroauto.entity.Transaction> movimentacoes = transactionService.findByCompany(company);
        if (movimentacoes.isEmpty()) {
            System.out.println("Nenhuma movimentacao cadastrada para atualizar.");
            return;
        }

        printMovimentacoes(movimentacoes);
        System.out.println("Digite o numero da movimentacao para atualizar:");
        int idx = readInt(sc) - 1;
        if (idx < 0 || idx >= movimentacoes.size()) {
            System.out.println("Movimentacao invalida.");
            return;
        }

        br.com.parceiroauto.entity.Transaction transaction = movimentacoes.get(idx);
        BankAccount bankAccount = chooseBankAccountForUpdate(sc, company, bankAccountService, transaction.getBankAccount());
        if (bankAccount == null) {
            return;
        }

        TransactionType tipo = escolherTipoMovimentacaoParaAtualizacao(sc, papel, transaction.getTipo());
        if (tipo == null) {
            return;
        }
        String descricao = askToKeepOrUpdate(sc, "Descricao", transaction.getDescricao());
        BigDecimal valor = askToKeepOrUpdateBigDecimal(sc, "Valor", transaction.getValor());
        TransactionForm forma = askToKeepOrUpdateForm(sc, transaction.getForma());
        TransactionCategory categoria = escolherCategoriaParaAtualizacaoMovimentacao(
                sc,
                company,
                papel,
                tipo,
                transaction.getTransactionCategory(),
                transactionCategoryService
        );
        if (categoria == null) {
            return;
        }

        FrequencyType frequencia = null;
        System.out.println("A movimentacao sera recorrente? 1 - Sim / 2 - Nao");
        int opcaoRecorrencia = readInt(sc);
        if (opcaoRecorrencia == 1) {
            try {
                frequencia = readFrequencyType(sc);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                return;
            }
        } else if (opcaoRecorrencia != 2) {
            System.out.println("Opcao invalida para recorrencia.");
            return;
        }

        try {
            transactionService.updateTransaction(transaction, bankAccount, categoria, tipo, descricao, valor, forma);
            recurrenceRuleService.replaceRecurrenceRule(transaction, frequencia, LocalDate.now(), null);
            System.out.println("Movimentacao atualizada com sucesso.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void removerMovimentacaoEmpresa(
            Scanner sc,
            Company company,
            RecurrenceRuleService recurrenceRuleService,
            TransactionService transactionService
    ) {
        List<br.com.parceiroauto.entity.Transaction> movimentacoes = transactionService.findByCompany(company);
        if (movimentacoes.isEmpty()) {
            System.out.println("Nenhuma movimentacao cadastrada para remover.");
            return;
        }

        printMovimentacoes(movimentacoes);
        System.out.println("Digite o numero da movimentacao para remover:");
        int idx = readInt(sc) - 1;
        if (idx < 0 || idx >= movimentacoes.size()) {
            System.out.println("Movimentacao invalida.");
            return;
        }

        br.com.parceiroauto.entity.Transaction transaction = movimentacoes.get(idx);
        try {
            recurrenceRuleService.replaceRecurrenceRule(transaction, null, null, null);
            transactionService.deleteTransaction(transaction);
            System.out.println("Movimentacao removida com sucesso.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Nao foi possivel remover a movimentacao.");
        }
    }

    private static void printMovimentacoes(List<br.com.parceiroauto.entity.Transaction> movimentacoes) {
        for (int i = 0; i < movimentacoes.size(); i++) {
            br.com.parceiroauto.entity.Transaction transaction = movimentacoes.get(i);
            System.out.println(
                    (i + 1) + " - "
                            + formatDate(transaction.getData())
                            + " | " + transaction.getTipo()
                            + " | " + transaction.getDescricao()
                            + " | " + transaction.getValor()
                            + " | Conta: " + transaction.getBankAccount().getNumeroConta()
            );
        }
    }

    private static BankAccount chooseBankAccountForUpdate(
            Scanner sc,
            Company company,
            BankAccountService bankAccountService,
            BankAccount currentBankAccount
    ) {
        System.out.println("Conta atual: " + currentBankAccount.getBanco() + " / " + currentBankAccount.getNumeroConta());
        System.out.println("Deseja manter? 1 - Sim / 2 - Nao");
        int opcao = readInt(sc);

        if (opcao == 1) {
            return currentBankAccount;
        }

        if (opcao != 2) {
            System.out.println("Opcao invalida.");
            return null;
        }

        List<BankAccount> contas = bankAccountService.findByCompany(company);
        for (int i = 0; i < contas.size(); i++) {
            System.out.println((i + 1) + " - " + contas.get(i).getBanco() + " | Conta: " + contas.get(i).getNumeroConta());
        }

        int idx = readInt(sc) - 1;
        if (idx < 0 || idx >= contas.size()) {
            System.out.println("Conta bancaria invalida.");
            return null;
        }

        return contas.get(idx);
    }

    private static TransactionCategory chooseCategory(
            Scanner sc,
            List<TransactionCategory> categorias,
            TransactionCategory categoriaAtual
    ) {
        System.out.println("Categoria atual: " + categoriaAtual.getName());
        System.out.println("Deseja manter? 1 - Sim / 2 - Nao");
        int opcao = readInt(sc);

        if (opcao == 1) {
            return categoriaAtual;
        }

        if (opcao != 2) {
            System.out.println("Opcao invalida.");
            return null;
        }

        for (int i = 0; i < categorias.size(); i++) {
            System.out.println((i + 1) + " - " + categorias.get(i).getName());
        }

        int idx = readInt(sc) - 1;
        if (idx < 0 || idx >= categorias.size()) {
            System.out.println("Categoria invalida.");
            return null;
        }

        return categorias.get(idx);
    }

    private static TransactionCategory chooseOrCreateCategoryForTransaction(
            Scanner sc,
            Company company,
            TransactionType tipo,
            TransactionCategoryService transactionCategoryService
    ) {
        while (true) {
            List<TransactionCategory> categorias = transactionCategoryService.findActiveByCompanyAndType(company, tipo);

            System.out.println("Escolha a categoria:");
            for (int i = 0; i < categorias.size(); i++) {
                System.out.println((i + 1) + " - " + categorias.get(i).getName());
            }
            System.out.println((categorias.size() + 1) + " - Cadastrar categoria agora");
            System.out.println("0 - Cancelar");

            int opcao = readInt(sc);
            if (opcao == 0) {
                return null;
            }
            if (opcao >= 1 && opcao <= categorias.size()) {
                return categorias.get(opcao - 1);
            }

            if (opcao == categorias.size() + 1) {
                TransactionCategory novaCategoria = createCategoryOnTheFly(sc, company, tipo, transactionCategoryService);
                if (novaCategoria != null) {
                    return novaCategoria;
                }
                continue;
            }

            System.out.println("Categoria invalida.");
        }
    }

    private static TransactionCategory chooseOrCreateCategoryForTransactionUpdate(
            Scanner sc,
            Company company,
            TransactionType tipo,
            TransactionCategory categoriaAtual,
            TransactionCategoryService transactionCategoryService
    ) {
        System.out.println("Categoria atual: " + categoriaAtual.getName());
        System.out.println("Deseja manter? 1 - Sim / 2 - Nao");
        int opcao = readInt(sc);

        if (opcao == 1 && categoriaAtual.getTipo() == tipo && categoriaAtual.isActive()) {
            return categoriaAtual;
        }

        if (opcao != 1 && opcao != 2) {
            System.out.println("Opcao invalida.");
            return null;
        }

        return chooseOrCreateCategoryForTransaction(sc, company, tipo, transactionCategoryService);
    }

    private static TransactionCategory createCategoryOnTheFly(
            Scanner sc,
            Company company,
            TransactionType tipo,
            TransactionCategoryService transactionCategoryService
    ) {
        String nome = readRequiredTextOrCancel(sc, "Nome da nova categoria:", "Nome da categoria nao pode ser vazio");
        if (nome == null) {
            System.out.println("Cadastro de categoria cancelado.");
            return null;
        }

        try {
            TransactionCategory categoria = transactionCategoryService.createCategory(company, nome, tipo);
            System.out.println("Categoria cadastrada com sucesso.");
            return categoria;
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private static void exibirDadosDaConta(BankAccount bankAccount) {
        System.out.println();
        System.out.println("Banco: " + bankAccount.getBanco());
        System.out.println("Agencia: " + bankAccount.getAgencia());
        System.out.println("Numero da conta: " + bankAccount.getNumeroConta());
        System.out.println("Tipo da conta: " + bankAccount.getTipoConta());
        System.out.println("Conta padrao: " + bankAccount.isContaPadrao());
        System.out.println("Saldo atual: " + bankAccount.getSaldo());
    }

    private static boolean canViewBankAccountDetails(UserCompanyRole papel) {
        return papel == UserCompanyRole.OWNER || papel == UserCompanyRole.MANAGER;
    }

    private static String askToKeepOrUpdate(Scanner sc, String label, String currentValue) {
        System.out.println(label + " atual: " + currentValue);
        System.out.println("Deseja manter? 1 - Sim / 2 - Nao");
        int opcao = readInt(sc);

        if (opcao == 1) {
            return currentValue;
        }

        if (opcao == 2) {
            System.out.println("Digite o novo valor para " + label + ":");
            return sc.nextLine().trim();
        }

        System.out.println("Opcao invalida. Mantendo valor atual.");
        return currentValue;
    }

    private static boolean askToKeepOrToggleBoolean(Scanner sc, String label, boolean currentValue) {
        System.out.println(label + " atual: " + currentValue);
        System.out.println("Deseja manter? 1 - Sim / 2 - Nao");
        int opcao = readInt(sc);

        if (opcao == 1) {
            return currentValue;
        }

        if (opcao == 2) {
            System.out.println("Novo valor: 1 - Sim / 2 - Nao");
            int novoValor = readInt(sc);
            if (novoValor == 1) {
                return true;
            }
            if (novoValor == 2) {
                return false;
            }
        }

        System.out.println("Opcao invalida. Mantendo valor atual.");
        return currentValue;
    }

    private static BigDecimal askToKeepOrUpdateBigDecimal(Scanner sc, String label, BigDecimal currentValue) {
        System.out.println(label + " atual: " + currentValue);
        System.out.println("Deseja manter? 1 - Sim / 2 - Nao");
        int opcao = readInt(sc);

        if (opcao == 1) {
            return currentValue;
        }

        if (opcao == 2) {
            System.out.println("Digite o novo valor para " + label + ":");
            return readBigDecimal(sc);
        }

        System.out.println("Opcao invalida. Mantendo valor atual.");
        return currentValue;
    }

    private static TransactionType askToKeepOrUpdateType(Scanner sc, TransactionType currentValue) {
        System.out.println("Tipo atual: " + currentValue);
        System.out.println("Deseja manter? 1 - Sim / 2 - Nao");
        int opcao = readInt(sc);

        if (opcao == 1) {
            return currentValue;
        }

        if (opcao == 2) {
            System.out.println("Novo tipo: 1 - ENTRADA / 2 - SAIDA");
            int novoTipo = readInt(sc);
            if (novoTipo == 1) {
                return TransactionType.ENTRADA;
            }
            if (novoTipo == 2) {
                return TransactionType.SAIDA;
            }
        }

        System.out.println("Opcao invalida. Mantendo valor atual.");
        return currentValue;
    }

    private static TransactionForm askToKeepOrUpdateForm(Scanner sc, TransactionForm currentValue) {
        System.out.println("Forma atual: " + currentValue);
        System.out.println("Deseja manter? 1 - Sim / 2 - Nao");
        int opcao = readInt(sc);

        if (opcao == 1) {
            return currentValue;
        }

        if (opcao == 2) {
            System.out.println("Nova forma: 1 - PIX / 2 - CARTAO / 3 - DINHEIRO");
            int novaForma = readInt(sc);
            if (novaForma == 1) {
                return TransactionForm.PIX;
            }
            if (novaForma == 2) {
                return TransactionForm.CARTAO;
            }
            if (novaForma == 3) {
                return TransactionForm.DINHEIRO;
            }
        }

        System.out.println("Opcao invalida. Mantendo valor atual.");
        return currentValue;
    }

    private static FrequencyType readFrequencyType(Scanner sc) {
        System.out.println("Frequencia: 1 - Diaria / 2 - Semanal / 3 - Mensal / 4 - Anual");
        int opcao = readInt(sc);

        if (opcao == 1) {
            return FrequencyType.DAILY;
        } else if (opcao == 2) {
            return FrequencyType.WEEKLY;
        } else if (opcao == 3) {
            return FrequencyType.MONTHLY;
        } else if (opcao == 4) {
            return FrequencyType.YEARLY;
        }

        throw new IllegalArgumentException("Frequencia invalida");
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

    private static BigDecimal readBigDecimal(Scanner sc) {
        while (true) {
            try {
                String valor = sc.nextLine().trim().replace(",", ".");
                return new BigDecimal(valor);
            } catch (NumberFormatException e) {
                System.out.println("Digite um valor numerico valido:");
            }
        }
    }

    private static BigDecimal readPositiveBigDecimal(Scanner sc, String mensagem) {
        while (true) {
            System.out.println(mensagem);
            BigDecimal valor = readBigDecimal(sc);
            if (valor.compareTo(BigDecimal.ZERO) > 0) {
                return valor;
            }
            System.out.println("Valor deve ser maior que zero.");
        }
    }

    private static BigDecimal readPositiveBigDecimalOrCancel(Scanner sc, String mensagem) {
        while (true) {
            System.out.println(mensagem + " (ou 0 para cancelar)");
            String valorDigitado = sc.nextLine().trim();

            if ("0".equals(valorDigitado)) {
                return null;
            }

            try {
                BigDecimal valor = new BigDecimal(valorDigitado.replace(",", "."));
                if (valor.compareTo(BigDecimal.ZERO) > 0) {
                    return valor;
                }
                System.out.println("Valor deve ser maior que zero.");
            } catch (NumberFormatException e) {
                System.out.println("Digite um valor numerico valido:");
            }
        }
    }

    private static String readRequiredText(Scanner sc, String mensagem, String mensagemErro) {
        while (true) {
            System.out.println(mensagem);
            String valor = sc.nextLine().trim();
            if (!valor.isBlank()) {
                return valor;
            }
            System.out.println(mensagemErro);
        }
    }

    private static String readRequiredTextOrCancel(Scanner sc, String mensagem, String mensagemErro) {
        while (true) {
            System.out.println(mensagem + " (ou 0 para cancelar)");
            String valor = sc.nextLine().trim();
            if ("0".equals(valor)) {
                return null;
            }
            if (!valor.isBlank()) {
                return valor;
            }
            System.out.println(mensagemErro);
        }
    }

    private static String readNewLogin(Scanner sc, UserService userService) {
        while (true) {
            System.out.println("Digite um login:");
            String login = sc.nextLine().trim();

            if (login.isBlank()) {
                System.out.println("Login nao pode ser vazio");
                continue;
            }

            if (userService.findByLogin(login) != null) {
                System.out.println("Ja existe um usuario com esse login");
                continue;
            }

            return login;
        }
    }

    private static String readValidCnpj(Scanner sc, CompanyService companyService, Company empresaAtual) {
        while (true) {
            System.out.println("CNPJ (somente numeros):");
            String cnpj = sc.nextLine().trim();

            if (cnpj.isBlank()) {
                System.out.println("CNPJ nao pode ser vazio");
                continue;
            }

            String cnpjNormalizado = cnpj.replaceAll("[^0-9]", "");
            if (!ValidadorCNPJ.isCNPJ(cnpjNormalizado)) {
                System.out.println("CNPJ invalido");
                continue;
            }

            Company empresaExistente = companyService.findByCnpj(cnpjNormalizado);
            if (empresaExistente != null && (empresaAtual == null || !empresaExistente.getId().equals(empresaAtual.getId()))) {
                System.out.println("Ja existe uma empresa com esse CNPJ");
                continue;
            }

            return cnpjNormalizado;
        }
    }

    private static String readValidAgency(Scanner sc) {
        while (true) {
            System.out.println("Agencia:");
            String agencia = sc.nextLine().trim();

            if (!agencia.matches("\\d{4}")) {
                System.out.println("Agencia deve ter exatamente 4 digitos");
                continue;
            }

            return agencia;
        }
    }

    private static String readValidAccountNumber(Scanner sc) {
        while (true) {
            System.out.println("Numero da conta:");
            String numeroConta = sc.nextLine().trim();

            if (!numeroConta.matches("\\d{4,13}")) {
                System.out.println("Numero da conta deve ter entre 4 e 13 digitos");
                continue;
            }

            return numeroConta;
        }
    }

    private static boolean readYesNo(Scanner sc, String mensagem) {
        while (true) {
            System.out.println(mensagem);
            int opcao = readInt(sc);
            if (opcao == 1) {
                return true;
            }
            if (opcao == 2) {
                return false;
            }
            System.out.println("Opcao invalida.");
        }
    }

    private static String chooseBankName(Scanner sc, List<BankAccount> contas) {
        LinkedHashSet<String> bancosUnicos = new LinkedHashSet<>();
        for (BankAccount conta : contas) {
            bancosUnicos.add(conta.getBanco());
        }

        if (bancosUnicos.isEmpty()) {
            System.out.println("Nenhum banco cadastrado para a empresa.");
            return null;
        }

        List<String> bancos = new ArrayList<>(bancosUnicos);
        System.out.println("Escolha o banco:");
        for (int i = 0; i < bancos.size(); i++) {
            System.out.println((i + 1) + " - " + bancos.get(i));
        }

        int idx = readInt(sc) - 1;
        if (idx < 0 || idx >= bancos.size()) {
            System.out.println("Banco invalido.");
            return null;
        }

        return bancos.get(idx);
    }

    private static LocalDate readDate(Scanner sc) {
        System.out.println("Digite a data no formato DD-MM-AAAA:");
        try {
            return LocalDate.parse(sc.nextLine().trim(), FORMATO_DATA_BR);
        } catch (Exception e) {
            System.out.println("Data invalida.");
            return null;
        }
    }

    private static TransactionType escolherTipoMovimentacao(Scanner sc, UserCompanyRole papel) {
        if (papel == UserCompanyRole.INVESTMENT_MANAGER) {
            System.out.println("Como Gerente de Investimentos, voce so pode registrar SAIDA para a categoria INVESTIMENTO.");
            return TransactionType.SAIDA;
        }

        System.out.println("Tipo da movimentacao: 1 - ENTRADA / 2 - SAIDA / 0 - Cancelar");
        int opcaoTipo = readInt(sc);

        if (opcaoTipo == 0) {
            System.out.println("Cadastro de movimentacao cancelado.");
            return null;
        }
        if (opcaoTipo == 1) {
            return TransactionType.ENTRADA;
        }
        if (opcaoTipo == 2) {
            return TransactionType.SAIDA;
        }

        System.out.println("Tipo invalido.");
        return null;
    }

    private static TransactionType escolherTipoMovimentacaoParaAtualizacao(
            Scanner sc,
            UserCompanyRole papel,
            TransactionType tipoAtual
    ) {
        if (papel == UserCompanyRole.INVESTMENT_MANAGER) {
            if (tipoAtual != TransactionType.SAIDA) {
                System.out.println("Como Gerente de Investimentos, a movimentacao sera ajustada para SAIDA.");
            } else {
                System.out.println("Como Gerente de Investimentos, o tipo permanece SAIDA.");
            }
            return TransactionType.SAIDA;
        }

        return askToKeepOrUpdateType(sc, tipoAtual);
    }

    private static TransactionCategory escolherCategoriaParaMovimentacao(
            Scanner sc,
            Company company,
            UserCompanyRole papel,
            TransactionType tipo,
            TransactionCategoryService transactionCategoryService
    ) {
        if (papel == UserCompanyRole.INVESTMENT_MANAGER) {
            return obterOuCriarCategoriaInvestimento(company, transactionCategoryService);
        }

        return chooseOrCreateCategoryForTransaction(sc, company, tipo, transactionCategoryService);
    }

    private static TransactionCategory escolherCategoriaParaAtualizacaoMovimentacao(
            Scanner sc,
            Company company,
            UserCompanyRole papel,
            TransactionType tipo,
            TransactionCategory categoriaAtual,
            TransactionCategoryService transactionCategoryService
    ) {
        if (papel == UserCompanyRole.INVESTMENT_MANAGER) {
            return obterOuCriarCategoriaInvestimento(company, transactionCategoryService);
        }

        return chooseOrCreateCategoryForTransactionUpdate(
                sc,
                company,
                tipo,
                categoriaAtual,
                transactionCategoryService
        );
    }

    private static TransactionCategory obterOuCriarCategoriaInvestimento(
            Company company,
            TransactionCategoryService transactionCategoryService
    ) {
        List<TransactionCategory> categorias = transactionCategoryService.findByCompany(company);
        for (TransactionCategory categoria : categorias) {
            if (!categoria.getName().equalsIgnoreCase("INVESTIMENTO")) {
                continue;
            }

            if (categoria.getTipo() != TransactionType.SAIDA) {
                System.out.println("A categoria INVESTIMENTO ja existe, mas nao esta configurada como SAIDA.");
                return null;
            }

            if (!categoria.isActive()) {
                transactionCategoryService.updateCategory(
                        company,
                        categoria,
                        categoria.getName(),
                        TransactionType.SAIDA,
                        true
                );
            }

            System.out.println("Categoria usada: " + categoria.getName());
            return categoria;
        }

        try {
            TransactionCategory categoria = transactionCategoryService.createCategory(
                    company,
                    "INVESTIMENTO",
                    TransactionType.SAIDA
            );
            System.out.println("Categoria INVESTIMENTO criada automaticamente.");
            return categoria;
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private static String formatDate(LocalDate data) {
        return data == null ? "" : data.format(FORMATO_DATA_BR);
    }

    private enum MenuResult {
        LOGOUT,
        SAIR
    }
}
