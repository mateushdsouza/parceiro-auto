package br.com.parceiroauto.view.swing;

import br.com.parceiroauto.confg.AppContext;
import br.com.parceiroauto.controller.LoginCompanyController;
import br.com.parceiroauto.entity.User;
import br.com.parceiroauto.entity.UserCompany;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class LoginCompanyFrame extends JFrame {
    private static final String UI_FONT = "Segoe UI";

    private final User user;
    private final AppContext context;

    public LoginCompanyFrame(User user, AppContext context) {
        this.user = user;
        this.context = context;
        LoginCompanyController controller = context.getLoginCompanyController();

        setTitle("ParceiroAuto - Selecionar Empresa");
        setSize(420, 260);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(24, 32, 24, 32));
        formPanel.setBackground(Color.WHITE);
        setContentPane(formPanel);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;

        JLabel lblEmpresa = new JLabel("Selecione a empresa:");
        lblEmpresa.setFont(new Font(UI_FONT, Font.BOLD, 13));
        constraints.gridy = 0;
        constraints.insets = new Insets(0, 0, 6, 0);
        formPanel.add(lblEmpresa, constraints);

        JComboBox<UserCompany> comboEmpresas = new JComboBox<>();
        constraints.gridy = 1;
        constraints.insets = new Insets(0, 0, 20, 0);
        formPanel.add(comboEmpresas, constraints);

        List<UserCompany> empresas;
        try {
            empresas = controller.buscarEmpresasDoUsuario(user);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
            empresas = List.of();
        }

        for (UserCompany userCompany : empresas) {
            comboEmpresas.addItem(userCompany);
        }

        JButton btnEntrar = new JButton("Entrar");
        styleButton(btnEntrar);
        btnEntrar.setEnabled(!empresas.isEmpty());

        if (empresas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhuma empresa cadastrada. Cadastre uma empresa para continuar.");
        }

        btnEntrar.addActionListener(e -> {
            UserCompany selecionado = (UserCompany) comboEmpresas.getSelectedItem();

            if (selecionado == null) {
                JOptionPane.showMessageDialog(this, "Selecione uma empresa");
                return;
            }

            new MainFrame(user, selecionado, context);
            dispose();
        });

        JButton btnCadastrar = new JButton("Cadastrar");
        styleButton(btnCadastrar);

        btnCadastrar.addActionListener(e -> {

            new RegisterCompanyFrame(user, context);

            dispose();
        });

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(btnEntrar);
        buttonsPanel.add(btnCadastrar);
        constraints.gridy = 2;
        constraints.insets = new Insets(0, 0, 0, 0);
        formPanel.add(buttonsPanel, constraints);

        setVisible(true);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font(UI_FONT, Font.BOLD, 13));
        button.setFocusPainted(false);
    }
}
