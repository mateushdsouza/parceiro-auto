package br.com.parceiroauto.view.swing;

import br.com.parceiroauto.controller.EmployeeController;
import br.com.parceiroauto.entity.Company;
import br.com.parceiroauto.entity.User;
import br.com.parceiroauto.entity.UserCompany;
import br.com.parceiroauto.entity.UserCompanyRole;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeesPanel extends JPanel {
    private final Company company;
    private final User loggedUser;
    private final UserCompanyRole currentRole;
    private final EmployeeController employeeController;
    private final Runnable dataChangedListener;

    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Login", "Funcao"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable employeeTable = new JTable(tableModel);
    private final JTextField filterField = new JTextField();
    private final JTextField loginField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JComboBox<UserCompanyRole> roleCombo = new JComboBox<>(
            new UserCompanyRole[]{
                    UserCompanyRole.MANAGER,
                    UserCompanyRole.INVESTMENT_MANAGER,
                    UserCompanyRole.VIEWER
            }
    );
    private final JCheckBox existingUserCheckBox = new JCheckBox("Vincular usuario existente");
    private final JButton saveButton = new JButton("Salvar");
    private final JButton clearButton = new JButton("Novo");
    private final JButton removeButton = new JButton("Remover");
    private final JButton refreshButton = new JButton("Atualizar");
    private final JButton filterButton = new JButton("Filtrar");

    private List<UserCompany> loadedEmployees = new ArrayList<>();

    public EmployeesPanel(
            Company company,
            User loggedUser,
            UserCompanyRole currentRole,
            EmployeeController employeeController,
            Runnable dataChangedListener
    ) {
        this.company = company;
        this.loggedUser = loggedUser;
        this.currentRole = currentRole;
        this.employeeController = employeeController;
        this.dataChangedListener = dataChangedListener;

        setLayout(new BorderLayout(14, 14));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(18, 18, 18, 18));

        if (!canManageEmployees()) {
            add(createAccessDeniedPanel(), BorderLayout.CENTER);
            return;
        }

        add(createHeader(), BorderLayout.NORTH);
        add(createListPanel(), BorderLayout.CENTER);
        add(createFormPanel(), BorderLayout.EAST);

        configureActions();
        loadEmployees();
        clearForm();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel title = new JLabel("Funcionarios");
        title.setFont(new Font("Arial", Font.BOLD, 26));

        JLabel subtitle = new JLabel(company == null ? "Nenhuma empresa selecionada" : company.getNomeFantasia());
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel labels = new JPanel(new GridLayout(2, 1));
        labels.setOpaque(false);
        labels.add(title);
        labels.add(subtitle);

        JPanel filterPanel = new JPanel(new BorderLayout(8, 0));
        filterPanel.setOpaque(false);
        filterField.setColumns(18);
        filterPanel.add(filterField, BorderLayout.CENTER);
        filterPanel.add(filterButton, BorderLayout.EAST);

        JPanel right = new JPanel(new BorderLayout(8, 0));
        right.setOpaque(false);
        right.add(filterPanel, BorderLayout.CENTER);
        right.add(refreshButton, BorderLayout.EAST);

        header.add(labels, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeTable.setRowHeight(28);
        employeeTable.getTableHeader().setReorderingAllowed(false);
        employeeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedEmployeeIntoForm();
            }
        });

        panel.add(new JScrollPane(employeeTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(340, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel title = new JLabel("Dados do funcionario");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);

        int row = 0;
        row = addField(fields, row, "Login", loginField);
        row = addField(fields, row, "Senha", passwordField);
        row = addField(fields, row, "Funcao", roleCombo);

        existingUserCheckBox.setOpaque(false);
        GridBagConstraints checkConstraints = constraints(row, 1);
        checkConstraints.anchor = GridBagConstraints.WEST;
        fields.add(existingUserCheckBox, checkConstraints);

        panel.add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 3, 8, 0));
        buttons.setOpaque(false);
        buttons.add(saveButton);
        buttons.add(removeButton);
        buttons.add(clearButton);

        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private int addField(JPanel panel, int row, String labelText, JComponent component) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(label, constraints(row, 0));

        GridBagConstraints fieldConstraints = constraints(row, 1);
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.weightx = 1;
        component.setFont(new Font("Arial", Font.PLAIN, 16));
        panel.add(component, fieldConstraints);
        return row + 1;
    }

    private GridBagConstraints constraints(int row, int column) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = column;
        constraints.gridy = row;
        constraints.insets = new Insets(0, 0, 10, column == 0 ? 10 : 0);
        constraints.anchor = GridBagConstraints.NORTHWEST;
        return constraints;
    }

    private JPanel createAccessDeniedPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel("Voce nao pode gerenciar funcionarios com seu perfil.");
        label.setFont(new Font("Arial", Font.PLAIN, 22));
        panel.add(label);
        return panel;
    }

    private void configureActions() {
        refreshButton.addActionListener(e -> loadEmployees());
        filterButton.addActionListener(e -> filterEmployees());
        clearButton.addActionListener(e -> clearForm());
        saveButton.addActionListener(e -> saveEmployee());
        removeButton.addActionListener(e -> removeEmployee());
        existingUserCheckBox.addActionListener(e -> passwordField.setEnabled(!existingUserCheckBox.isSelected()));
    }

    private void loadEmployees() {
        if (company == null || employeeController == null) {
            return;
        }

        try {
            loadedEmployees = employeeController.listarFuncionarios(company);
            fillTable(loadedEmployees);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void filterEmployees() {
        if (company == null || employeeController == null) {
            return;
        }

        try {
            loadedEmployees = employeeController.filtrarFuncionarios(company, filterField.getText(), null);
            fillTable(loadedEmployees);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void fillTable(List<UserCompany> employees) {
        tableModel.setRowCount(0);
        for (UserCompany employee : employees) {
            tableModel.addRow(new Object[]{employee.getUser().getLogin(), employee.getRole()});
        }
    }

    private void loadSelectedEmployeeIntoForm() {
        UserCompany selectedEmployee = getSelectedEmployee();
        if (selectedEmployee == null) {
            return;
        }

        loginField.setText(selectedEmployee.getUser().getLogin());
        loginField.setEnabled(false);
        passwordField.setText("");
        passwordField.setEnabled(false);
        existingUserCheckBox.setSelected(true);
        existingUserCheckBox.setEnabled(false);
        roleCombo.setSelectedItem(selectedEmployee.getRole());
        removeButton.setEnabled(true);
    }

    private void saveEmployee() {
        UserCompany selectedEmployee = getSelectedEmployee();
        UserCompanyRole selectedRole = (UserCompanyRole) roleCombo.getSelectedItem();

        try {
            if (selectedEmployee == null) {
                if (existingUserCheckBox.isSelected()) {
                    employeeController.vincularUsuarioExistente(company, loginField.getText(), selectedRole);
                } else {
                    employeeController.cadastrarFuncionario(
                            company,
                            loginField.getText(),
                            new String(passwordField.getPassword()),
                            selectedRole
                    );
                }
                JOptionPane.showMessageDialog(this, "Funcionario cadastrado com sucesso.");
            } else {
                employeeController.atualizarFuncao(company, selectedEmployee, loggedUser, selectedRole);
                JOptionPane.showMessageDialog(this, "Funcao atualizada com sucesso.");
            }
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        } catch (Exception ex) {
            showError("Nao foi possivel salvar o funcionario.");
            return;
        }

        loadEmployees();
        notifyDataChanged();
        clearForm();
    }

    private void removeEmployee() {
        UserCompany selectedEmployee = getSelectedEmployee();
        if (selectedEmployee == null) {
            showError("Selecione um funcionario para remover.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Remover o funcionario selecionado?",
                "Confirmar remocao",
                JOptionPane.YES_NO_OPTION
        );
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            employeeController.removerFuncionario(company, selectedEmployee, loggedUser);
            JOptionPane.showMessageDialog(this, "Funcionario removido com sucesso.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        } catch (Exception ex) {
            showError("Nao foi possivel remover o funcionario.");
            return;
        }

        loadEmployees();
        notifyDataChanged();
        clearForm();
    }

    private void clearForm() {
        employeeTable.clearSelection();
        loginField.setText("");
        loginField.setEnabled(true);
        existingUserCheckBox.setSelected(false);
        existingUserCheckBox.setEnabled(true);
        passwordField.setText("");
        passwordField.setEnabled(true);
        roleCombo.setSelectedItem(UserCompanyRole.VIEWER);
        removeButton.setEnabled(false);
    }

    private UserCompany getSelectedEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }

        int modelRow = employeeTable.convertRowIndexToModel(selectedRow);
        if (modelRow >= loadedEmployees.size()) {
            return null;
        }

        return loadedEmployees.get(modelRow);
    }

    private boolean canManageEmployees() {
        return currentRole == UserCompanyRole.OWNER || currentRole == UserCompanyRole.MANAGER;
    }

    private void notifyDataChanged() {
        if (dataChangedListener != null) {
            dataChangedListener.run();
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
