package br.com.parceiroauto.view.swing;

import br.com.parceiroauto.confg.AppContext;
import br.com.parceiroauto.controller.RegisterController;

import javax.swing.*;

public class RegisterFrame extends JFrame {

    private final AppContext context;

    public RegisterFrame(AppContext context) {
        this.context = context;
        RegisterController controller = context.getRegisterController();

        // =========================
        // CONFIGURACAO DA JANELA
        // =========================

        setTitle("ParceiroAuto - Cadastro");

        setSize(420, 340);

        setLocationRelativeTo(null);

        setResizable(false);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(null);

        // =========================
        // LOGIN
        // =========================

        JLabel lblLogin = new JLabel("Digite seu login");

        JTextField txtLogin = new JTextField();

        lblLogin.setBounds(
                40,
                30,
                140,
                25
        );

        txtLogin.setBounds(
                40,
                55,
                320,
                30
        );

        add(lblLogin);

        add(txtLogin);

        // =========================
        // SENHA
        // =========================

        JLabel lblPassword = new JLabel("Digite sua senha");

        JPasswordField txtPassword = new JPasswordField();

        lblPassword.setBounds(
                40,
                100,
                140,
                25
        );

        txtPassword.setBounds(
                40,
                125,
                320,
                30
        );

        add(lblPassword);

        add(txtPassword);

        // =========================
        // CONFIRMAR SENHA
        // =========================

        JLabel lblConfirmPassword = new JLabel("Confirme sua senha");

        JPasswordField txtConfirmPassword = new JPasswordField();

        lblConfirmPassword.setBounds(
                40,
                170,
                160,
                25
        );

        txtConfirmPassword.setBounds(
                40,
                195,
                320,
                30
        );

        add(lblConfirmPassword);

        add(txtConfirmPassword);

        // =========================
        // BOTAO CADASTRAR
        // =========================

        JButton btnRegister = new JButton("Cadastrar");

        btnRegister.setBounds(
                80,
                250,
                120,
                30
        );

        add(btnRegister);

        // =========================
        // BOTAO VOLTAR
        // =========================

        JButton btnBack = new JButton("Voltar");

        btnBack.setBounds(
                220,
                250,
                120,
                30
        );

        add(btnBack);

        // =========================
        // EXIBIR
        // =========================

        setVisible(true);

        btnRegister.addActionListener(e -> {
            String login = txtLogin.getText().trim();
            String password = new String(txtPassword.getPassword()).trim();
            String confirmPassword = new String(txtConfirmPassword.getPassword()).trim();

            if (login.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Digite seu login"
                );

                return;
            }

            if (password.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Digite sua senha"
                );

                return;
            }

            if (password.length() < 6 || password.length() > 12) {

                JOptionPane.showMessageDialog(
                        null,
                        "Senha deve ter entre 6 e 12 caracteres"
                );

                return;
            }

            if (!password.equals(confirmPassword)) {

                JOptionPane.showMessageDialog(
                        this,
                        "As senhas não coincidem"
                );

                return;
            }

            try {

                controller.register(login, password);

            } catch (IllegalArgumentException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage()
                );

                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Usuário cadastrado com sucesso"
            );

            dispose();

            new LoginFrame(context);

        });

        btnBack.addActionListener(e -> {
            new LoginFrame(context);
            dispose();
        });
    }
}
