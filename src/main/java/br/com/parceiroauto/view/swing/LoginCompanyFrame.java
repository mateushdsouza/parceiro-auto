package br.com.parceiroauto.view.swing;

import br.com.parceiroauto.confg.AppContext;
import br.com.parceiroauto.controller.LoginCompanyController;
import br.com.parceiroauto.entity.User;
import br.com.parceiroauto.entity.UserCompany;

import javax.swing.*;
import java.util.List;

public class LoginCompanyFrame extends JFrame {
    private final User user;
    private final AppContext context;

    public LoginCompanyFrame(User user, AppContext context) {
        this.user = user;
        this.context = context;
        LoginCompanyController controller = context.getLoginCompanyController();

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
        btnEntrar.setBounds(80, 130, 120, 30);
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

        add(btnEntrar);

        // Botao Cadastrar
        JButton btnCadastrar = new JButton("Cadastrar");

        btnCadastrar.setBounds(220, 130, 120, 30);

        btnCadastrar.addActionListener(e -> {

            new RegisterCompanyFrame(user, context);

            dispose();
        });

        add(btnCadastrar);


        setVisible(true);
    }
}
