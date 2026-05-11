package br.com.parceiroauto.view.swing;

import br.com.parceiroauto.controller.LoginCompanyController;
import br.com.parceiroauto.controller.LoginController;
import br.com.parceiroauto.controller.RegisterCompanyController;
import br.com.parceiroauto.controller.RegisterController;
import br.com.parceiroauto.entity.User;
import br.com.parceiroauto.service.RecurrenceRuleService;
import br.com.parceiroauto.service.TransactionService;

import javax.swing.*;

public class LoginFrame extends JFrame {
    private final LoginController controller;
    private final RegisterController registerController;
    private final LoginCompanyController loginCompanyController;
    private final RegisterCompanyController registerCompanyController;
    private final TransactionService transactionService;
    private final RecurrenceRuleService recurrenceRuleService;

    public LoginFrame(
            LoginController controller,
            RegisterController registerController,
            LoginCompanyController loginCompanyController,
            RegisterCompanyController registerCompanyController
    ) {
        this(controller, registerController, loginCompanyController, registerCompanyController, null, null);
    }

    public LoginFrame(
            LoginController controller,
            RegisterController registerController,
            LoginCompanyController loginCompanyController,
            RegisterCompanyController registerCompanyController,
            TransactionService transactionService,
            RecurrenceRuleService recurrenceRuleService
    ) {
        this.controller = controller;
        this.registerController = registerController;
        this.loginCompanyController = loginCompanyController;
        this.registerCompanyController = registerCompanyController;
        this.transactionService = transactionService;
        this.recurrenceRuleService = recurrenceRuleService;

        setTitle("Login");
        setSize(420, 260);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(null);

        // Login
        JLabel lblLogin = new JLabel("Digite seu login");
        JTextField txtLogin = new JTextField();

        lblLogin.setBounds(40, 30, 120, 25);
        txtLogin.setBounds(40, 55, 320, 30);

        add(lblLogin);
        add(txtLogin);

        // Senha
        JLabel lblPassword = new JLabel("Digite sua senha");
        JPasswordField txtPassword = new JPasswordField();

        lblPassword.setBounds(40, 100, 120, 25);
        txtPassword.setBounds(40, 125, 320, 30);

        add(lblPassword);
        add(txtPassword);

        // Botao Login
        JButton btnLogin = new JButton("Entrar");

        btnLogin.setBounds(80, 180, 120, 30);

        btnLogin.addActionListener(e -> {
            String login = txtLogin.getText().trim();
            String senha = new String(txtPassword.getPassword()).trim();

            if (login.isEmpty()) {

                JOptionPane.showMessageDialog(
                        null,
                        "Digite seu login"
                );

                return;
            }

            if (senha.isEmpty()) {

                JOptionPane.showMessageDialog(
                        null,
                        "Digite sua senha"
                );

                return;
            }

            if (senha.length() < 6 || senha.length() > 12) {

                JOptionPane.showMessageDialog(
                        null,
                        "Senha deve ter entre 6 e 12 caracteres"
                );

                return;
            }

            User user = controller.login(login, senha);

            if (user != null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Bem-vindo " + user.getLogin()
                );

                new LoginCompanyFrame(
                        user,
                        loginCompanyController,
                        registerCompanyController,
                        transactionService,
                        recurrenceRuleService
                );

                dispose();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Login ou senha inválidos");
            }
        });

        add(btnLogin);

        // Botao Cadastrar
        JButton btnCadastrar = new JButton("Cadastrar");

        btnCadastrar.setBounds(220, 180, 120, 30);

        btnCadastrar.addActionListener(e -> {

            new RegisterFrame(
                    registerController,
                    controller,
                    loginCompanyController,
                    registerCompanyController,
                    transactionService,
                    recurrenceRuleService
            );

            dispose();
        });

        add(btnCadastrar);

        setVisible(true);
    }
}
