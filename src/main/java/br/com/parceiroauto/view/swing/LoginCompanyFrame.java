package br.com.parceiroauto.view.swing;

import br.com.parceiroauto.controller.LoginCompanyController;
import br.com.parceiroauto.controller.LoginController;
import br.com.parceiroauto.controller.RegisterCompanyController;
import br.com.parceiroauto.controller.RegisterController;
import br.com.parceiroauto.entity.User;
import br.com.parceiroauto.entity.UserCompany;
import br.com.parceiroauto.service.BankAccountService;
import br.com.parceiroauto.service.RecurrenceRuleService;
import br.com.parceiroauto.service.TransactionCategoryService;
import br.com.parceiroauto.service.TransactionService;

import javax.swing.*;
import java.util.List;

public class LoginCompanyFrame extends JFrame {
    private final User user;
    private final LoginCompanyController controller;
    private final RegisterCompanyController registerCompanyController;
    private final BankAccountService bankAccountService;
    private final TransactionCategoryService transactionCategoryService;
    private final TransactionService transactionService;
    private final RecurrenceRuleService recurrenceRuleService;
    private final LoginController loginController;
    private final RegisterController registerController;

    public LoginCompanyFrame(
            User user,
            LoginCompanyController controller,
            RegisterCompanyController registerCompanyController,
            LoginController loginController,
            RegisterController registerController
    ) {
        this(user, controller, registerCompanyController, loginController, registerController, null, null, null, null);
    }

    public LoginCompanyFrame(
            User user,
            LoginCompanyController controller,
            RegisterCompanyController registerCompanyController,
            LoginController loginController,
            RegisterController registerController,
            BankAccountService bankAccountService,
            TransactionCategoryService transactionCategoryService,
            TransactionService transactionService,
            RecurrenceRuleService recurrenceRuleService
    ) {
        this.user = user;
        this.controller = controller;
        this.registerCompanyController = registerCompanyController;
        this.bankAccountService = bankAccountService;
        this.transactionCategoryService = transactionCategoryService;
        this.transactionService = transactionService;
        this.recurrenceRuleService = recurrenceRuleService;
        this.loginController = loginController;
        this.registerController = registerController;

        setTitle("Selecionar Empresa");
        setSize(420, 260);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel lblEmpresa = new JLabel("Selecione a empresa:");
        lblEmpresa.setBounds(40, 30, 200, 25);
        add(lblEmpresa);

        JComboBox<UserCompany> comboEmpresas = new JComboBox<>();
        comboEmpresas.setBounds(40, 60, 320, 30);
        add(comboEmpresas);

        List<UserCompany> empresas = controller.buscarEmpresasDoUsuario(user);

        for (UserCompany userCompany : empresas) {
            comboEmpresas.addItem(userCompany);
        }

        JButton btnEntrar = new JButton("Entrar");
        btnEntrar.setBounds(80, 130, 120, 30);

        btnEntrar.addActionListener(e -> {
            UserCompany selecionado = (UserCompany) comboEmpresas.getSelectedItem();

            if (selecionado == null) {
                JOptionPane.showMessageDialog(this, "Selecione uma empresa");
                return;
            }

            new MainFrame(
                    user,
                    selecionado,
                    bankAccountService,
                    transactionCategoryService,
                    transactionService,
                    recurrenceRuleService,
                    loginController,
                    registerController,
                    controller,
                    registerCompanyController
            );
            dispose();
        });

        add(btnEntrar);

        // Botao Cadastrar
        JButton btnCadastrar = new JButton("Cadastrar");

        btnCadastrar.setBounds(220, 130, 120, 30);

        btnCadastrar.addActionListener(e -> {

            new RegisterCompanyFrame(
                    user,
                    registerCompanyController,
                    controller,
                    loginController,
                    registerController,
                    bankAccountService,
                    transactionCategoryService,
                    transactionService,
                    recurrenceRuleService
            );

            dispose();
        });

        add(btnCadastrar);


        setVisible(true);
    }
}
