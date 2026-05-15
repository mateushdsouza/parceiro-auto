package br.com.parceiroauto.view.swing;

import br.com.parceiroauto.confg.AppContext;
import br.com.parceiroauto.controller.RegisterController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RegisterFrame extends JFrame {
    private static final String UI_FONT = "Segoe UI";

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

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(22, 32, 22, 32));
        formPanel.setBackground(Color.WHITE);
        setContentPane(formPanel);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;

        JLabel lblLogin = new JLabel("Digite seu login");
        JTextField txtLogin = new JTextField();
        styleLabel(lblLogin);
        addField(formPanel, constraints, lblLogin, 0);
        addField(formPanel, constraints, txtLogin, 1);

        JLabel lblPassword = new JLabel("Digite sua senha");
        JPasswordField txtPassword = new JPasswordField();
        styleLabel(lblPassword);
        addField(formPanel, constraints, lblPassword, 2);
        addField(formPanel, constraints, txtPassword, 3);

        JLabel lblConfirmPassword = new JLabel("Confirme sua senha");
        JPasswordField txtConfirmPassword = new JPasswordField();
        styleLabel(lblConfirmPassword);
        addField(formPanel, constraints, lblConfirmPassword, 4);
        addField(formPanel, constraints, txtConfirmPassword, 5);

        JButton btnRegister = new JButton("Cadastrar");
        styleButton(btnRegister);

        JButton btnBack = new JButton("Voltar");
        styleButton(btnBack);

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(btnRegister);
        buttonsPanel.add(btnBack);
        constraints.gridy = 6;
        constraints.insets = new Insets(18, 0, 0, 0);
        formPanel.add(buttonsPanel, constraints);

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

    private void addField(JPanel panel, GridBagConstraints constraints, JComponent component, int row) {
        constraints.gridy = row;
        constraints.insets = row % 2 == 0 ? new Insets(6, 0, 0, 0) : new Insets(4, 0, 12, 0);
        panel.add(component, constraints);
    }

    private void styleLabel(JLabel label) {
        label.setFont(new Font(UI_FONT, Font.BOLD, 13));
    }

    private void styleButton(JButton button) {
        button.setFont(new Font(UI_FONT, Font.BOLD, 13));
        button.setFocusPainted(false);
    }
}
