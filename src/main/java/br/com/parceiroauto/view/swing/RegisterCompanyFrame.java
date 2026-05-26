package br.com.parceiroauto.view.swing;

import br.com.parceiroauto.confg.AppContext;
import br.com.parceiroauto.controller.RegisterCompanyController;
import br.com.parceiroauto.model.entity.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RegisterCompanyFrame extends JFrame {
    private static final String UI_FONT = "Segoe UI";

    private final User user;
    private final AppContext context;

    public RegisterCompanyFrame(User user, AppContext context) {
        this.user = user;
        this.context = context;
        RegisterCompanyController registerCompanyController = context.getRegisterCompanyController();

        setTitle("ParceiroAuto - Cadastro de Empresa");
        setSize(420, 390);
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

        JLabel lblCnpj = new JLabel("Digite o CNPJ");
        JTextField txtCnpj = new JTextField();
        styleLabel(lblCnpj);
        addField(formPanel, constraints, lblCnpj, 0);
        addField(formPanel, constraints, txtCnpj, 1);

        JLabel lblRazaoSocial = new JLabel("Digite a razao social");
        JTextField txtRazaoSocial = new JTextField();
        styleLabel(lblRazaoSocial);
        addField(formPanel, constraints, lblRazaoSocial, 2);
        addField(formPanel, constraints, txtRazaoSocial, 3);

        JLabel lblNomeFantasia = new JLabel("Digite o nome fantasia");
        JTextField txtNomeFantasia = new JTextField();
        styleLabel(lblNomeFantasia);
        addField(formPanel, constraints, lblNomeFantasia, 4);
        addField(formPanel, constraints, txtNomeFantasia, 5);

        JButton btnCadastrar = new JButton("Cadastrar");
        styleButton(btnCadastrar);

        JButton btnVoltar = new JButton("Voltar");
        styleButton(btnVoltar);

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(btnCadastrar);
        buttonsPanel.add(btnVoltar);
        constraints.gridy = 6;
        constraints.insets = new Insets(18, 0, 0, 0);
        formPanel.add(buttonsPanel, constraints);

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
            new LoginCompanyFrame(user, context);
            dispose();
        });

        btnVoltar.addActionListener(e -> {
            new LoginCompanyFrame(user, context);
            dispose();
        });

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
