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
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
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
            BankAccountRepository bankAccountRepository = new BankAccountRepository(em);
            RecurrenceRuleRepository recurrenceRuleRepository = new RecurrenceRuleRepository(em);
            TransactionCategoryRepository transactionCategoryRepository = new TransactionCategoryRepository(em);
            TransactionRepository transactionRepository = new TransactionRepository(em);

            UserService userService = new UserService(userRepository);
            CompanyService companyService = new CompanyService(companyRepository);
            UserCompanyService userCompanyService = new UserCompanyService(userCompanyRepository);
            BankAccountService bankAccountService = new BankAccountService(bankAccountRepository);
            RecurrenceRuleService recurrenceRuleService = new RecurrenceRuleService(recurrenceRuleRepository);
            TransactionCategoryService transactionCategoryService = new TransactionCategoryService(transactionCategoryRepository);
            TransactionService transactionService = new TransactionService(transactionRepository, bankAccountRepository);

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
                        userService,
                        bankAccountService,
                        recurrenceRuleService,
                        transactionCategoryService,
                        transactionService
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
            if (links.isEmpty()) {
                System.out.println("Nenhuma empresa vinculada.");
            } else {
                for (int i = 0; i < links.size(); i++) {
                    Company company = links.get(i).getCompany();
                    System.out.println((i + 1) + " - " + company.getNomeFantasia() + " (CNPJ: " + company.getCnpj() + ")"
                            + " [" + links.get(i).getRole() + "]");
                }
            }

            int opcaoCadastrar = links.size() + 1;
            int opcaoAtualizar = links.size() + 2;
            int opcaoRemover = links.size() + 3;
            int opcaoLogout = links.size() + 4;
            int opcaoSair = links.size() + 5;

            System.out.println();
            System.out.println(opcaoCadastrar + " - Cadastrar empresa");
            System.out.println(opcaoAtualizar + " - Atualizar empresa");
            System.out.println(opcaoRemover + " - Remover empresa");
            System.out.println(opcaoLogout + " - Logout");
            System.out.println(opcaoSair + " - Sair");
            int opcao = readInt(sc);

            if (opcao >= 1 && opcao <= links.size()) {
                Company company = links.get(opcao - 1).getCompany();
                UserCompanyRole role = links.get(opcao - 1).getRole();
                menuEmpresa(
                        sc,
                        user,
                        company,
                        role,
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

                Company company = links.get(idx).getCompany();
                String cnpj = askToKeepOrUpdate(sc, "CNPJ", company.getCnpj());
                String razaoSocial = askToKeepOrUpdate(sc, "Razao Social", company.getRazaoSocial());
                String nomeFantasia = askToKeepOrUpdate(sc, "Nome Fantasia", company.getNomeFantasia());

                try {
                    companyService.updateCompany(company, cnpj, razaoSocial, nomeFantasia);
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

                Company company = links.get(idx).getCompany();
                UserCompanyRole role = links.get(idx).getRole();

                if (role != UserCompanyRole.OWNER) {
                    System.out.println("Voce nao pode remover essa empresa (somente OWNER).");
                    continue;
                }

                try {
                    userCompanyRepository.deleteByCompany(company);
                    companyRepository.delete(company);
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
            Company company,
            UserCompanyRole role,
            UserCompanyRepository userCompanyRepository,
            UserService userService,
            BankAccountService bankAccountService,
            RecurrenceRuleService recurrenceRuleService,
            TransactionCategoryService transactionCategoryService,
            TransactionService transactionService
    ) {
        while (true) {
            System.out.println();
            System.out.println("=== Empresa: " + company.getNomeFantasia() + " (" + role + ") ===");
            System.out.println("1 - Contas bancarias");
            System.out.println("2 - Movimentacoes");
            System.out.println("3 - Funcionarios");
            System.out.println("4 - Relatorios");
            System.out.println("5 - Voltar");

            int opcao = readInt(sc);
            switch (opcao) {
                case 1:
                    if (!canAccessBankAccounts(role)) {
                        System.out.println("Voce nao pode acessar contas bancarias com seu perfil.");
                        break;
                    }
                    menuContasBancarias(
                            sc,
                            company,
                            role,
                            bankAccountService,
                            recurrenceRuleService,
                            transactionCategoryService,
                            transactionService
                    );
                    break;

                case 2:
                    if (!canAccessBankAccounts(role)) {
                        System.out.println("Voce nao pode registrar movimentacoes com seu perfil.");
                        break;
                    }
                    menuMovimentacoesEmpresa(
                            sc,
                            company,
                            bankAccountService,
                            recurrenceRuleService,
                            transactionCategoryService,
                            transactionService
                    );
                    break;

                case 3:
                    if (!canManageEmployees(role)) {
                        System.out.println("Voce nao pode acessar funcionarios com seu perfil.");
                        break;
                    }
                    System.out.println("Em construcao: menu de funcionarios.");
                    break;

                case 4:
                    if (!canAccessReports(role)) {
                        System.out.println("Voce nao pode acessar relatorios com seu perfil.");
                        break;
                    }
                    System.out.println("Em construcao: menu de relatorios.");
                    break;

                case 5:
                    return;

                default:
                    System.out.println("Digite uma opcao valida!");
            }
        }
    }

    private static boolean canManageEmployees(UserCompanyRole role) {
        return role == UserCompanyRole.OWNER || role == UserCompanyRole.MANAGER;
    }

    private static void menuFuncionario(
            Scanner sc,
            Company company,
            UserCompanyRepository userCompanyRepository
    ) {
        while (true) {
            System.out.println();
            System.out.println("== Funcionarios ==");

            List<UserCompany> links = userCompanyRepository.findByCompany(company);

            if (links.isEmpty()) {
                System.out.println("Nenhum funcionario cadastrado.");
            } else {
                for (int i = 0; i < links.size(); i++) {
                    UserCompany userCompany = links.get(i);
                    System.out.println(
                            (i + 1) + " - " +
                                    userCompany.getUser().getLogin() +
                                    " | Role: " + userCompany.getRole()
                    );
                }
            }

            System.out.println((links.size() + 1) + " - Voltar");
            int opcao = readInt(sc);
            if (opcao == links.size() + 1) {
                return;
            }

            int opcaoCadastrar = links.size() + 1;
            int opcaoAtualizar = links.size() + 2;
            int opcaoRemover = links.size() + 3;
            int opcaoVoltar = links.size() + 4;

            System.out.println();
            System.out.println(opcaoCadastrar + " - Cadastrar funcionario");
            System.out.println(opcaoAtualizar + " - Atualizar funcionario");
            System.out.println(opcaoRemover + " - Remover funcionario");
            System.out.println(opcaoVoltar + " - Voltar");
            int opcao = readInt(sc);


            if (eopcao == opcaoCadastrar){
                //cadastrar
            } else-if (opcao == opcaoAtualizar)

        }
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
                System.out.println("Banco:");
                String banco = sc.nextLine().trim();

                System.out.println("Agencia:");
                String agencia = sc.nextLine().trim();

                System.out.println("Numero da conta:");
                String numeroConta = sc.nextLine().trim();

                System.out.println("Tipo da conta:");
                String tipoConta = sc.nextLine().trim();

                System.out.println("Essa e a conta padrao? 1 - Sim / 2 - Nao");
                int opcaoContaPadrao = readInt(sc);
                if (opcaoContaPadrao != 1 && opcaoContaPadrao != 2) {
                    System.out.println("Opcao invalida para conta padrao.");
                    continue;
                }

                boolean contaPadrao = opcaoContaPadrao == 1;

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
            System.out.println("4 - Definir como conta padrao");
            System.out.println("5 - Voltar");

            int opcao = readInt(sc);
            switch (opcao) {
                case 1:
                    menuCategorias(sc, company, transactionCategoryService);
                    break;

                case 2:
                    menuMovimentacoesEmpresa(
                            sc,
                            company,
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

                case 5:
                    return;

                default:
                    System.out.println("Digite uma opcao valida!");
            }
        }
    }

    private static void menuMovimentacoesEmpresa(
            Scanner sc,
            Company company,
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

            int idxConta = readInt(sc) - 1;
            if (idxConta < 0 || idxConta >= contas.size()) {
                System.out.println("Conta bancaria invalida.");
                return;
            }

            bankAccount = contas.get(idxConta);
        } else {
            System.out.println("Conta padrao usada: " + bankAccount.getBanco() + " | Conta: " + bankAccount.getNumeroConta());
            System.out.println("Deseja usar outra conta? 1 - Sim / 2 - Nao");
            int opcaoOutraConta = readInt(sc);

            if (opcaoOutraConta == 1) {
                for (int i = 0; i < contas.size(); i++) {
                    BankAccount conta = contas.get(i);
                    System.out.println((i + 1) + " - " + conta.getBanco() + " | Conta: " + conta.getNumeroConta());
                }

                int idxConta = readInt(sc) - 1;
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

        System.out.println("Tipo da movimentacao: 1 - ENTRADA / 2 - SAIDA");
        int opcaoTipo = readInt(sc);

        TransactionType tipo;
        if (opcaoTipo == 1) {
            tipo = TransactionType.ENTRADA;
        } else if (opcaoTipo == 2) {
            tipo = TransactionType.SAIDA;
        } else {
            System.out.println("Tipo invalido.");
            return;
        }

        System.out.println("Descricao:");
        String descricao = sc.nextLine().trim();

        System.out.println("Valor:");
        BigDecimal valor = readBigDecimal(sc);

        System.out.println("Forma da movimentacao: 1 - PIX / 2 - CARTAO / 3 - DINHEIRO");
        int opcaoForma = readInt(sc);
        TransactionForm forma;
        if (opcaoForma == 1) {
            forma = TransactionForm.PIX;
        } else if (opcaoForma == 2) {
            forma = TransactionForm.CARTAO;
        } else if (opcaoForma == 3) {
            forma = TransactionForm.DINHEIRO;
        } else {
            System.out.println("Forma invalida.");
            return;
        }

        TransactionCategory categoria = chooseOrCreateCategoryForTransaction(sc, company, tipo, transactionCategoryService);
        if (categoria == null) {
            return;
        }

        System.out.println("A movimentacao sera recorrente? 1 - Sim / 2 - Nao");
        int opcaoRecorrencia = readInt(sc);

        FrequencyType frequencia = null;
        Integer diaExecucao = null;

        if (opcaoRecorrencia == 1) {
            try {
                frequencia = readFrequencyType(sc);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                return;
            }

            System.out.println("Dia de execucao (1 a 31):");
            diaExecucao = readInt(sc);
            if (diaExecucao < 1 || diaExecucao > 31) {
                System.out.println("Dia de execucao invalido.");
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
                        diaExecucao,
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
                    System.out.println("Nome da categoria:");
                    String nome = sc.nextLine().trim();

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

        TransactionType tipo = askToKeepOrUpdateType(sc, transaction.getTipo());
        String descricao = askToKeepOrUpdate(sc, "Descricao", transaction.getDescricao());
        BigDecimal valor = askToKeepOrUpdateBigDecimal(sc, "Valor", transaction.getValor());
        TransactionForm forma = askToKeepOrUpdateForm(sc, transaction.getForma());
        TransactionCategory categoria = chooseOrCreateCategoryForTransactionUpdate(
                sc,
                company,
                tipo,
                transaction.getTransactionCategory(),
                transactionCategoryService
        );
        if (categoria == null) {
            return;
        }

        FrequencyType frequencia = null;
        Integer diaExecucao = null;
        System.out.println("A movimentacao sera recorrente? 1 - Sim / 2 - Nao");
        int opcaoRecorrencia = readInt(sc);
        if (opcaoRecorrencia == 1) {
            try {
                frequencia = readFrequencyType(sc);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                return;
            }

            System.out.println("Dia de execucao (1 a 31):");
            diaExecucao = readInt(sc);
            if (diaExecucao < 1 || diaExecucao > 31) {
                System.out.println("Dia de execucao invalido.");
                return;
            }
        } else if (opcaoRecorrencia != 2) {
            System.out.println("Opcao invalida para recorrencia.");
            return;
        }

        try {
            transactionService.updateTransaction(transaction, bankAccount, categoria, tipo, descricao, valor, forma);
            recurrenceRuleService.replaceRecurrenceRule(transaction, frequencia, diaExecucao, LocalDate.now(), null);
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
            recurrenceRuleService.replaceRecurrenceRule(transaction, null, null, null, null);
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
                            + transaction.getTipo()
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

            int opcao = readInt(sc);
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
        System.out.println("Nome da nova categoria:");
        String nome = sc.nextLine().trim();

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

    private static boolean canViewBankAccountDetails(UserCompanyRole role) {
        return role == UserCompanyRole.OWNER || role == UserCompanyRole.MANAGER;
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
        System.out.println("Frequencia: 1 - DAILY / 2 - WEEKLY / 3 - MONTHLY / 4 - YEARLY");
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

    private enum MenuResult {
        LOGOUT,
        SAIR
    }
}
