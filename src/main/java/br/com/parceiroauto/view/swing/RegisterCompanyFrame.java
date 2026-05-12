package br.com.parceiroauto.view.swing;

import br.com.parceiroauto.controller.LoginCompanyController;
import br.com.parceiroauto.controller.RegisterCompanyController;
import br.com.parceiroauto.entity.User;
import br.com.parceiroauto.service.RecurrenceRuleService;
import br.com.parceiroauto.service.TransactionService;
import br.com.parceiroauto.controller.LoginController;
import br.com.parceiroauto.controller.RegisterController;

import javax.swing.*;

public class RegisterCompanyFrame extends JFrame {
    private final User user;
    private final RegisterCompanyController registerCompanyController;
    private final LoginCompanyController loginCompanyController;
    private final TransactionService transactionService;
    private final RecurrenceRuleService recurrenceRuleService;
    private final LoginController loginController;
    private final RegisterController registerController;

    public RegisterCompanyFrame(
            User user,
            RegisterCompanyController registerCompanyController,
            LoginCompanyController loginCompanyController,
            LoginController loginController,
            RegisterController registerController
    ) {
        this(user, registerCompanyController, loginCompanyController, loginController, registerController, null, null);
    }

    public RegisterCompanyFrame(
            User user,
            RegisterCompanyController registerCompanyController,
            LoginCompanyController loginCompanyController,
            LoginController loginController,
            RegisterController registerController,
            TransactionService transactionService,
            RecurrenceRuleService recurrenceRuleService
    ) {
        this.loginController = loginController;
        this.registerController = registerController;
        this.user = user;
        this.registerCompanyController = registerCompanyController;
        this.loginCompanyController = loginCompanyController;
        this.transactionService = transactionService;
        this.recurrenceRuleService = recurrenceRuleService;

        setTitle("Cadastro de Empresa");
        setSize(420, 390);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel lblCnpj = new JLabel("Digite o CNPJ");
        JTextField txtCnpj = new JTextField();
        lblCnpj.setBounds(40, 30, 140, 25);
        txtCnpj.setBounds(40, 55, 320, 30);
        add(lblCnpj);
        add(txtCnpj);

        JLabel lblRazaoSocial = new JLabel("Digite a razao social");
        JTextField txtRazaoSocial = new JTextField();
        lblRazaoSocial.setBounds(40, 100, 180, 25);
        txtRazaoSocial.setBounds(40, 125, 320, 30);
        add(lblRazaoSocial);
        add(txtRazaoSocial);

        JLabel lblNomeFantasia = new JLabel("Digite o nome fantasia");
        JTextField txtNomeFantasia = new JTextField();
        lblNomeFantasia.setBounds(40, 170, 180, 25);
        txtNomeFantasia.setBounds(40, 195, 320, 30);
        add(lblNomeFantasia);
        add(txtNomeFantasia);

        JButton btnCadastrar = new JButton("Cadastrar");
        btnCadastrar.setBounds(80, 270, 120, 30);
        add(btnCadastrar);

        JButton btnVoltar = new JButton("Voltar");
        btnVoltar.setBounds(220, 270, 120, 30);
        add(btnVoltar);

        btnCadastrar.addActionListener(e -> {
            String cnpj = txtCnpj.getText().trim();
            String razaoSocial = txtRazaoSocial.getText().trim();
            String nomeFantasia = txtNomeFantasia.getText().trim();

            try {
                registerCompanyController.register(user, cnpj, razaoSocial, nomeFantasia);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
                return;
            }

            JOptionPane.showMessageDialog(this, "Empresa cadastrada com sucesso");
            new LoginCompanyFrame(
                    user,
                    loginCompanyController,
                    registerCompanyController,
                    loginController,
                    registerController,
                    transactionService,
                    recurrenceRuleService
            );
            dispose();
        });

        btnVoltar.addActionListener(e -> {
            new LoginCompanyFrame(
                    user,
                    loginCompanyController,
                    registerCompanyController,
                    loginController,
                    registerController,
                    transactionService,
                    recurrenceRuleService
            );
            dispose();
        });

        setVisible(true);
    }
}
