package br.com.parceiroauto.view.swing;

import br.com.parceiroauto.controller.EmployeeController;
import br.com.parceiroauto.model.entity.Company;
import br.com.parceiroauto.model.entity.User;
import br.com.parceiroauto.model.entity.UserCompany;
import br.com.parceiroauto.model.entity.UserCompanyRole;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeesPanel extends JPanel {
    private static final String UI_FONT = "Segoe UI";
    private static final int FORM_PANEL_WIDTH = 300;

    private final Company company;
    private final User loggedUser;
    private final UserCompanyRole currentRole;
    private final EmployeeController employeeController;
    private final Runnable dataChangedListener;

    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Login", "Função"}, 0) {
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
    private final JCheckBox existingUserCheckBox = new JCheckBox("Vincular usuário existente");
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

        if (company == null || employeeController == null) {
            add(createUnavailablePanel(), BorderLayout.CENTER);
            return;
        }

        if (!canManageEmployees()) {
            add(createAccessDeniedPanel(), BorderLayout.CENTER);
            return;
        }

        add(createHeader(), BorderLayout.NORTH);
        add(createListPanel(), BorderLayout.CENTER);
        add(createFormPanel(), BorderLayout.EAST);

        configureActions();
        loadEmployees(null);
        clearForm();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel title = new JLabel("Funcionários");
        title.setFont(new Font(UI_FONT, Font.BOLD, 26));

        JLabel subtitle = new JLabel(company.getNomeFantasia());
        subtitle.setFont(new Font(UI_FONT, Font.PLAIN, 14));

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
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeTable.setRowHeight(28);
        employeeTable.setFont(new Font(UI_FONT, Font.PLAIN, 14));
        employeeTable.getTableHeader().setFont(new Font(UI_FONT, Font.BOLD, 14));
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
        panel.setPreferredSize(new Dimension(FORM_PANEL_WIDTH, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel title = new JLabel("Dados do funcionário");
        title.setFont(new Font(UI_FONT, Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);

        int row = 0;
        row = addField(fields, row, "Login", loginField);
        row = addField(fields, row, "Senha", passwordField);
        row = addField(fields, row, "Função", roleCombo);

        existingUserCheckBox.setOpaque(false);
        GridBagConstraints checkConstraints = constraints(row, 1);
        checkConstraints.anchor = GridBagConstraints.WEST;
        fields.add(existingUserCheckBox, checkConstraints);

        panel.add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 3, 8, 0));
        buttons.setOpaque(false);
        buttons.add(saveButton);
        buttons.add(clearButton);
        buttons.add(removeButton);

        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private int addField(JPanel panel, int row, String labelText, JComponent component) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(UI_FONT, Font.PLAIN, 13));
        panel.add(label, constraints(row, 0));

        GridBagConstraints fieldConstraints = constraints(row, 1);
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.weightx = 1;
        component.setFont(new Font(UI_FONT, Font.PLAIN, 16));
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

    private JPanel createUnavailablePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel("Empresa ou serviço de funcionário não disponível.");
        label.setFont(new Font(UI_FONT, Font.PLAIN, 22));
        panel.add(label);
        return panel;
    }

    private JPanel createAccessDeniedPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel("Você não pode gerenciar funcionários com seu perfil.");
        label.setFont(new Font(UI_FONT, Font.PLAIN, 22));
        panel.add(label);
        return panel;
    }

    private void configureActions() {
        refreshButton.addActionListener(e -> loadEmployees(null));
        filterButton.addActionListener(e -> filterEmployees());
        clearButton.addActionListener(e -> clearForm());
        saveButton.addActionListener(e -> saveEmployee());
        removeButton.addActionListener(e -> removeEmployee());
        existingUserCheckBox.addActionListener(e -> passwordField.setEnabled(!existingUserCheckBox.isSelected()));
    }

    private void filterEmployees() {

        setLoading(true);

        String filter = filterField.getText();

        SwingWorker<List<UserCompany>, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected List<UserCompany> doInBackground() {

                        return employeeController.filtrarFuncionarios(
                                company,
                                filter,
                                null
                        );
                    }

                    @Override
                    protected void done() {

                        try {

                            loadedEmployees = get();

                            populateTable(loadedEmployees);

                        } catch (Exception ex) {

                            showError("Não foi possível filtrar os funcionários.");

                            ex.printStackTrace();

                        } finally {

                            setLoading(false);
                        }
                    }
                };

        worker.execute();
    }

    private void populateTable(List<UserCompany> employees) {

        tableModel.setRowCount(0);
        employeeTable.clearSelection();

        for (UserCompany employee : employees) {

            tableModel.addRow(new Object[]{
                    employee.getUser().getLogin(),
                    employee.getRole()
            });
        }
    }

    private void setLoading(boolean loading) {

        refreshButton.setEnabled(!loading);
        filterButton.setEnabled(!loading);
        saveButton.setEnabled(!loading);
        clearButton.setEnabled(!loading);
        removeButton.setEnabled(!loading);

        employeeTable.setEnabled(!loading);

        setCursor(
                loading
                        ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                        : Cursor.getDefaultCursor()
        );
    }

    private void loadEmployees(Runnable afterLoad) {

        setLoading(true);

        SwingWorker<List<UserCompany>, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected List<UserCompany> doInBackground() {

                        return employeeController.listarFuncionarios(company);
                    }

                    @Override
                    protected void done() {

                        try {

                            loadedEmployees = get();

                            populateTable(loadedEmployees);
                            if (afterLoad != null) {
                                afterLoad.run();
                            }

                        } catch (Exception ex) {

                            showError("Não foi possível carregar os funcionários.");

                            ex.printStackTrace();

                        } finally {

                            setLoading(false);
                        }
                    }
                };

        worker.execute();
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
        String login = loginField.getText().trim();
        String password = new String(passwordField.getPassword());

        try {
            if (selectedEmployee == null) {
                if (existingUserCheckBox.isSelected()) {
                    employeeController.vincularUsuarioExistente(company, login, selectedRole);
                } else {
                    employeeController.cadastrarFuncionario(
                            company,
                            login,
                            password,
                            selectedRole
                    );
                }
                JOptionPane.showMessageDialog(this, "Funcionário cadastrado com sucesso.");
            } else {
                employeeController.atualizarFuncao(company, selectedEmployee, loggedUser, selectedRole);
                JOptionPane.showMessageDialog(this, "Função atualizada com sucesso.");
            }
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        } catch (Exception ex) {
            showError("Não foi possível salvar o funcionário.");
            return;
        }

        notifyDataChanged();
        loadEmployees(this::clearForm);
    }

    private void removeEmployee() {
        UserCompany selectedEmployee = getSelectedEmployee();
        if (selectedEmployee == null) {
            showError("Selecione um funcionário para remover.");
            return;
        }

        boolean confirmed = SwingDialogs.confirmYesNo(
                this,
                "Remover o funcionário selecionado?",
                "Confirmar remoção"
        );
        if (!confirmed) {
            return;
        }

        try {
            employeeController.removerFuncionario(company, selectedEmployee, loggedUser);
            JOptionPane.showMessageDialog(this, "Funcionário removido com sucesso.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        } catch (Exception ex) {
            showError("Não foi possível remover o funcionário.");
            return;
        }

        notifyDataChanged();
        loadEmployees(this::clearForm);
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
