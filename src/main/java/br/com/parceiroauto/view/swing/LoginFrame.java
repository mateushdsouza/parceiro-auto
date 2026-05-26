package br.com.parceiroauto.view.swing;

import br.com.parceiroauto.confg.AppContext;
import br.com.parceiroauto.controller.LoginController;
import br.com.parceiroauto.model.entity.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {
    private static final String UI_FONT = "Segoe UI";

    private final AppContext context;

    public LoginFrame(AppContext context) {
        this.context = context;
        LoginController controller = context.getLoginController();

        setTitle("ParceiroAuto - Login");
        setSize(420, 260);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(22, 32, 22, 32));
        formPanel.setBackground(Color.WHITE);
        setContentPane(formPanel);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 0, 6, 0);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridwidth = 2;

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

        JButton btnLogin = new JButton("Entrar");
        styleButton(btnLogin);

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

                new LoginCompanyFrame(user, context);

                dispose();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Login ou senha inválidos");
            }
        });

        JButton btnCadastrar = new JButton("Cadastrar");
        styleButton(btnCadastrar);

        btnCadastrar.addActionListener(e -> {

            new RegisterFrame(context);

            dispose();
        });

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(btnLogin);
        buttonsPanel.add(btnCadastrar);
        constraints.gridy = 4;
        constraints.insets = new Insets(18, 0, 0, 0);
        formPanel.add(buttonsPanel, constraints);

        setVisible(true);
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
