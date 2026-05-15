package br.com.parceiroauto.view.swing;

import br.com.parceiroauto.controller.BankAccountController;
import br.com.parceiroauto.entity.BankAccount;
import br.com.parceiroauto.entity.Company;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BankAccountPanel extends JPanel {
    private static final int FORM_PANEL_WIDTH = 300;

    private final Company company;
    private final BankAccountController bankAccountController;
    private final Runnable dataChangedListener;
    private final NumberFormat moneyFormatter = NumberFormat.getCurrencyInstance(
            new Locale.Builder().setLanguage("pt").setRegion("BR").build()
    );

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Banco", "Agencia", "Conta", "Tipo", "Saldo", "Padrao"},
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable accountTable = new JTable(tableModel);
    private final JTextField bankNameField = new JTextField();
    private final JTextField agencyField = new JTextField();
    private final JTextField accountNumberField = new JTextField();
    private final JTextField accountTypeField = new JTextField();
    private final JCheckBox defaultAccountCheckBox = new JCheckBox("Conta padrao");
    private final JButton saveButton = new JButton("Salvar");
    private final JButton clearButton = new JButton("Novo");
    private final JButton deleteButton = new JButton("Remover");
    private final JButton defaultButton = new JButton("Definir padrao");
    private final JButton refreshButton = new JButton("Atualizar");

    private List<BankAccount> loadedAccounts = new ArrayList<>();

    public BankAccountPanel(Company company, BankAccountController bankAccountController, Runnable dataChangedListener
    ) {
        this.company = company;
        this.bankAccountController = bankAccountController;
        this.dataChangedListener = dataChangedListener;

        setLayout(new BorderLayout(14, 14));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(18, 18, 18, 18));

        if (company == null || bankAccountController == null) {
            add(createUnavailablePanel(), BorderLayout.CENTER);
            return;
        }

        add(createHeader(), BorderLayout.NORTH);
        add(createListPanel(), BorderLayout.CENTER);
        add(createFormPanel(), BorderLayout.EAST);

        configureActions();
        loadAccounts();
        clearForm();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Contas bancarias");
        title.setFont(new Font("Arial", Font.BOLD, 26));

        JLabel subtitle = new JLabel(company.getNomeFantasia());
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel labels = new JPanel(new GridLayout(2, 1));
        labels.setOpaque(false);
        labels.add(title);
        labels.add(subtitle);

        header.add(labels, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);
        return header;
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        accountTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountTable.setRowHeight(28);
        accountTable.getTableHeader().setReorderingAllowed(false);
        configureTableColumns();
        accountTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedAccountIntoForm();
            }
        });

        panel.add(new JScrollPane(accountTable), BorderLayout.CENTER);
        return panel;
    }

    private void configureTableColumns() {
        accountTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        TableColumnModel columns = accountTable.getColumnModel();
        columns.getColumn(0).setPreferredWidth(180);
        columns.getColumn(1).setPreferredWidth(70);
        columns.getColumn(1).setMaxWidth(90);
        columns.getColumn(2).setPreferredWidth(130);
        columns.getColumn(3).setPreferredWidth(110);
        columns.getColumn(4).setPreferredWidth(120);
        columns.getColumn(5).setPreferredWidth(70);
        columns.getColumn(5).setMaxWidth(85);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(FORM_PANEL_WIDTH, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel title = new JLabel("Dados da conta");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);

        int row = 0;
        row = addField(fields, row, "Banco", bankNameField);
        row = addField(fields, row, "Agencia", agencyField);
        row = addField(fields, row, "Numero da conta", accountNumberField);
        row = addField(fields, row, "Tipo da conta", accountTypeField);

        defaultAccountCheckBox.setOpaque(false);
        GridBagConstraints checkConstraints = constraints(row, 1);
        checkConstraints.anchor = GridBagConstraints.WEST;
        fields.add(defaultAccountCheckBox, checkConstraints);

        panel.add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(2, 2, 8, 8));
        buttons.setOpaque(false);
        buttons.add(saveButton);
        buttons.add(clearButton);
        buttons.add(deleteButton);
        buttons.add(defaultButton);

        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private int addField(JPanel panel, int row, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 13));

        panel.add(label, constraints(row, 0));

        GridBagConstraints fieldConstraints = constraints(row, 1);
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.weightx = 1;
        field.setFont(new Font("Arial", Font.PLAIN, 16));
        panel.add(field, fieldConstraints);
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

        JLabel label = new JLabel("Empresa ou controller de conta nao disponivel.");
        label.setFont(new Font("Arial", Font.PLAIN, 22));
        panel.add(label);
        return panel;
    }

    private void configureActions() {
        refreshButton.addActionListener(e -> loadAccounts());
        clearButton.addActionListener(e -> clearForm());
        saveButton.addActionListener(e -> saveAccount());
        deleteButton.addActionListener(e -> deleteAccount());
        defaultButton.addActionListener(e -> defineDefaultAccount());
    }

    private void loadAccounts() {
        tableModel.setRowCount(0);
        loadedAccounts = new ArrayList<>();

        try {
            loadedAccounts = bankAccountController.buscarContasBancarias(company);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        }

        for (BankAccount account : loadedAccounts) {
            tableModel.addRow(new Object[]{
                    account.getBanco(),
                    account.getAgencia(),
                    account.getNumeroConta(),
                    account.getTipoConta(),
                    moneyFormatter.format(account.getSaldo()),
                    account.isContaPadrao() ? "Sim" : "Nao"
            });
        }
    }

    private void loadSelectedAccountIntoForm() {
        BankAccount selectedAccount = getSelectedAccount();
        if (selectedAccount == null) {
            return;
        }

        bankNameField.setText(selectedAccount.getBanco());
        agencyField.setText(selectedAccount.getAgencia());
        accountNumberField.setText(selectedAccount.getNumeroConta());
        accountTypeField.setText(selectedAccount.getTipoConta());
        defaultAccountCheckBox.setSelected(selectedAccount.isContaPadrao());
    }

    private void saveAccount() {
        String bankName = bankNameField.getText().trim();
        String agency = agencyField.getText().trim();
        String accountNumber = accountNumberField.getText().trim();
        String accountType = accountTypeField.getText().trim();
        boolean defaultAccount = defaultAccountCheckBox.isSelected();
        BankAccount selectedAccount = getSelectedAccount();

        try {
            if (selectedAccount == null) {
                bankAccountController.cadastrarContaBancaria(
                        company,
                        bankName,
                        agency,
                        accountNumber,
                        accountType,
                        defaultAccount
                );
                JOptionPane.showMessageDialog(this, "Conta cadastrada com sucesso.");
            } else {
                bankAccountController.atualizarContaBancaria(
                        company,
                        selectedAccount,
                        bankName,
                        agency,
                        accountNumber,
                        accountType,
                        defaultAccount
                );
                JOptionPane.showMessageDialog(this, "Conta atualizada com sucesso.");
            }
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        } catch (Exception ex) {
            showError("Nao foi possivel salvar a conta bancaria.");
            return;
        }

        loadAccounts();
        notifyDataChanged();
        clearForm();
    }

    private void deleteAccount() {
        BankAccount selectedAccount = getSelectedAccount();
        if (selectedAccount == null) {
            showError("Selecione uma conta para remover.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Remover a conta selecionada?",
                "Confirmar remocao",
                JOptionPane.YES_NO_OPTION
        );
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            bankAccountController.removerContaBancaria(company, selectedAccount);
            JOptionPane.showMessageDialog(this, "Conta removida com sucesso.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        } catch (Exception ex) {
            showError("Nao foi possivel remover a conta. Verifique se existem movimentacoes vinculadas.");
            return;
        }

        loadAccounts();
        notifyDataChanged();
        clearForm();
    }

    private void defineDefaultAccount() {
        BankAccount selectedAccount = getSelectedAccount();
        if (selectedAccount == null) {
            showError("Selecione uma conta para definir como padrao.");
            return;
        }

        try {
            bankAccountController.definirContaPadrao(company, selectedAccount);
            JOptionPane.showMessageDialog(this, "Conta padrao atualizada.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        }

        loadAccounts();
        notifyDataChanged();
        clearForm();
    }

    private void clearForm() {
        accountTable.clearSelection();
        bankNameField.setText("");
        agencyField.setText("");
        accountNumberField.setText("");
        accountTypeField.setText("");
        defaultAccountCheckBox.setSelected(false);
    }

    private BankAccount getSelectedAccount() {
        int selectedRow = accountTable.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }

        int modelRow = accountTable.convertRowIndexToModel(selectedRow);
        if (modelRow >= loadedAccounts.size()) {
            return null;
        }

        return loadedAccounts.get(modelRow);
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
