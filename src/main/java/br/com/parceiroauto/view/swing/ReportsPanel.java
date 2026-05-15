package br.com.parceiroauto.view.swing;

import br.com.parceiroauto.entity.BankAccount;
import br.com.parceiroauto.entity.Company;
import br.com.parceiroauto.entity.Transaction;
import br.com.parceiroauto.entity.TransactionCategory;
import br.com.parceiroauto.entity.TransactionForm;
import br.com.parceiroauto.entity.TransactionType;
import br.com.parceiroauto.entity.UserCompanyRole;
import br.com.parceiroauto.service.BankAccountService;
import br.com.parceiroauto.service.TransactionCategoryService;
import br.com.parceiroauto.service.TransactionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportsPanel extends JPanel {
    private static final Object ALL_OPTION = "Todos";

    private final Company company;
    private final UserCompanyRole role;
    private final BankAccountService bankAccountService;
    private final TransactionCategoryService transactionCategoryService;
    private final TransactionService transactionService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NumberFormat moneyFormatter = NumberFormat.getCurrencyInstance(
            new Locale.Builder().setLanguage("pt").setRegion("BR").build()
    );

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Data", "Tipo", "Descricao", "Valor", "Forma", "Categoria", "Banco", "Agencia", "Conta"},
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable transactionTable = new JTable(tableModel);
    private final JComboBox<Object> typeCombo = new JComboBox<>();
    private final JComboBox<Object> accountCombo = new JComboBox<>();
    private final JComboBox<Object> categoryCombo = new JComboBox<>();
    private final JComboBox<Object> formCombo = new JComboBox<>();
    private final JTextField startDateField = new JTextField();
    private final JTextField endDateField = new JTextField();
    private final JTextField descriptionField = new JTextField();
    private final JButton filterButton = new JButton("Filtrar");
    private final JButton clearButton = new JButton("Limpar");
    private final JButton exportButton = new JButton("Exportar");
    private final JButton refreshButton = new JButton("Atualizar");
    private final JLabel quantityLabel = new JLabel();
    private final JLabel incomeLabel = new JLabel();
    private final JLabel expenseLabel = new JLabel();
    private final JLabel balanceLabel = new JLabel();

    private List<Transaction> loadedTransactions = new ArrayList<>();
    private List<Transaction> displayedTransactions = new ArrayList<>();

    public ReportsPanel(
            Company company,
            UserCompanyRole role,
            BankAccountService bankAccountService,
            TransactionCategoryService transactionCategoryService,
            TransactionService transactionService
    ) {
        this.company = company;
        this.role = role;
        this.bankAccountService = bankAccountService;
        this.transactionCategoryService = transactionCategoryService;
        this.transactionService = transactionService;

        setLayout(new BorderLayout(14, 14));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(18, 18, 18, 18));

        if (company == null || transactionService == null || bankAccountService == null || transactionCategoryService == null) {
            add(createUnavailablePanel(), BorderLayout.CENTER);
            return;
        }

        if (!canAccessReports()) {
            add(createAccessDeniedPanel(), BorderLayout.CENTER);
            return;
        }

        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createFiltersPanel(), BorderLayout.EAST);

        configureActions();
        configureRenderers();
        refreshData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel title = new JLabel("Relatorios");
        title.setFont(new Font("Arial", Font.BOLD, 26));

        JLabel subtitle = new JLabel(company.getNomeFantasia());
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel labels = new JPanel(new GridLayout(2, 1));
        labels.setOpaque(false);
        labels.add(title);
        labels.add(subtitle);

        JPanel summary = new JPanel(new GridLayout(1, 4, 8, 0));
        summary.setOpaque(false);
        summary.add(createSummaryItem("Movimentacoes", quantityLabel));
        summary.add(createSummaryItem("Entradas", incomeLabel));
        summary.add(createSummaryItem("Saidas", expenseLabel));
        summary.add(createSummaryItem("Saldo geral", balanceLabel));

        header.add(labels, BorderLayout.WEST);
        header.add(summary, BorderLayout.CENTER);
        header.add(refreshButton, BorderLayout.EAST);
        return header;
    }

    private JPanel createSummaryItem(String title, JLabel valueLabel) {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(6, 8, 6, 8)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        valueLabel.setFont(new Font("Arial", Font.BOLD, 14));

        panel.add(titleLabel);
        panel.add(valueLabel);
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.setRowHeight(28);
        transactionTable.getTableHeader().setReorderingAllowed(false);
        transactionTable.setAutoCreateRowSorter(true);

        panel.add(new JScrollPane(transactionTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFiltersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(340, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel title = new JLabel("Filtros");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);

        int row = 0;
        row = addField(fields, row, "Tipo", typeCombo);
        row = addField(fields, row, "Conta", accountCombo);
        row = addField(fields, row, "Categoria", categoryCombo);
        row = addField(fields, row, "Forma", formCombo);
        row = addField(fields, row, "Data inicial", startDateField);
        row = addField(fields, row, "Data final", endDateField);
        row = addField(fields, row, "Descricao", descriptionField);

        panel.add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(3, 1, 0, 8));
        buttons.setOpaque(false);
        buttons.add(filterButton);
        buttons.add(clearButton);
        buttons.add(exportButton);

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

    private JPanel createUnavailablePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel("Empresa ou servicos de relatorio nao disponiveis.");
        label.setFont(new Font("Arial", Font.PLAIN, 22));
        panel.add(label);
        return panel;
    }

    private JPanel createAccessDeniedPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel("Voce nao pode acessar relatorios com seu perfil.");
        label.setFont(new Font("Arial", Font.PLAIN, 22));
        panel.add(label);
        return panel;
    }

    private void configureActions() {
        filterButton.addActionListener(e -> applyFilters());
        clearButton.addActionListener(e -> clearFilters());
        exportButton.addActionListener(e -> exportDisplayedReport());
        refreshButton.addActionListener(e -> refreshData());
    }

    private void configureRenderers() {
        DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof BankAccount account) {
                    setText(formatAccount(account));
                } else if (value instanceof TransactionCategory category) {
                    setText(category.getName());
                }

                return this;
            }
        };

        accountCombo.setRenderer(renderer);
        categoryCombo.setRenderer(renderer);
    }

    public void refreshData() {
        try {
            loadedTransactions = transactionService.findByCompany(company);
            loadFilterOptions();
            applyFilters();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Nao foi possivel carregar os relatorios.");
        }
    }

    private void loadFilterOptions() {
        Object selectedType = typeCombo.getSelectedItem();
        Object selectedAccount = accountCombo.getSelectedItem();
        Object selectedCategory = categoryCombo.getSelectedItem();
        Object selectedForm = formCombo.getSelectedItem();

        typeCombo.removeAllItems();
        typeCombo.addItem(ALL_OPTION);
        for (TransactionType type : TransactionType.values()) {
            typeCombo.addItem(type);
        }
        restoreSelection(typeCombo, selectedType);

        accountCombo.removeAllItems();
        accountCombo.addItem(ALL_OPTION);
        for (BankAccount account : bankAccountService.findByCompany(company)) {
            accountCombo.addItem(account);
        }
        restoreSelection(accountCombo, selectedAccount);

        categoryCombo.removeAllItems();
        categoryCombo.addItem(ALL_OPTION);
        for (TransactionCategory category : transactionCategoryService.findByCompany(company)) {
            categoryCombo.addItem(category);
        }
        restoreSelection(categoryCombo, selectedCategory);

        formCombo.removeAllItems();
        formCombo.addItem(ALL_OPTION);
        for (TransactionForm form : TransactionForm.values()) {
            formCombo.addItem(form);
        }
        restoreSelection(formCombo, selectedForm);
    }

    private void restoreSelection(JComboBox<Object> combo, Object selectedItem) {
        if (selectedItem == null) {
            combo.setSelectedItem(ALL_OPTION);
            return;
        }

        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            if (sameFilterItem(item, selectedItem)) {
                combo.setSelectedIndex(i);
                return;
            }
        }

        combo.setSelectedItem(ALL_OPTION);
    }

    private void applyFilters() {
        try {
            LocalDate startDate = parseOptionalDate(startDateField.getText());
            LocalDate endDate = parseOptionalDate(endDateField.getText());
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Data inicial nao pode ser maior que a data final.");
            }

            String descriptionFilter = descriptionField.getText().trim().toLowerCase();
            List<Transaction> filtered = new ArrayList<>();

            for (Transaction transaction : loadedTransactions) {
                if (!matchesType(transaction)) {
                    continue;
                }
                if (!matchesAccount(transaction)) {
                    continue;
                }
                if (!matchesCategory(transaction)) {
                    continue;
                }
                if (!matchesForm(transaction)) {
                    continue;
                }
                if (!matchesDate(transaction, startDate, endDate)) {
                    continue;
                }
                if (!matchesDescription(transaction, descriptionFilter)) {
                    continue;
                }

                filtered.add(transaction);
            }

            populateTable(filtered);
            updateSummary(filtered);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private boolean matchesType(Transaction transaction) {
        Object selected = typeCombo.getSelectedItem();
        return selected == ALL_OPTION || transaction.getTipo() == selected;
    }

    private boolean matchesAccount(Transaction transaction) {
        Object selected = accountCombo.getSelectedItem();
        if (selected == ALL_OPTION) {
            return true;
        }

        return selected instanceof BankAccount account
                && sameBankAccount(transaction.getBankAccount(), account);
    }

    private boolean matchesCategory(Transaction transaction) {
        Object selected = categoryCombo.getSelectedItem();
        if (selected == ALL_OPTION) {
            return true;
        }

        return selected instanceof TransactionCategory category
                && sameCategory(transaction.getTransactionCategory(), category);
    }

    private boolean matchesForm(Transaction transaction) {
        Object selected = formCombo.getSelectedItem();
        return selected == ALL_OPTION || transaction.getForma() == selected;
    }

    private boolean matchesDate(Transaction transaction, LocalDate startDate, LocalDate endDate) {
        LocalDate date = transaction.getData();
        if (date == null) {
            return startDate == null && endDate == null;
        }

        if (startDate != null && date.isBefore(startDate)) {
            return false;
        }

        return endDate == null || !date.isAfter(endDate);
    }

    private boolean matchesDescription(Transaction transaction, String filter) {
        if (filter.isBlank()) {
            return true;
        }

        String description = transaction.getDescricao() == null ? "" : transaction.getDescricao().toLowerCase();
        return description.contains(filter);
    }

    private void populateTable(List<Transaction> transactions) {
        displayedTransactions = new ArrayList<>(transactions);
        exportButton.setEnabled(!displayedTransactions.isEmpty());
        tableModel.setRowCount(0);

        for (Transaction transaction : transactions) {
            BankAccount account = transaction.getBankAccount();
            TransactionCategory category = transaction.getTransactionCategory();

            tableModel.addRow(new Object[]{
                    formatDate(transaction.getData()),
                    transaction.getTipo(),
                    transaction.getDescricao(),
                    formatMoney(transaction.getValor()),
                    transaction.getForma(),
                    category == null ? "sem categoria" : category.getName(),
                    account == null ? "sem banco" : account.getBanco(),
                    account == null ? "" : account.getAgencia(),
                    account == null ? "" : account.getNumeroConta()
            });
        }
    }

    private void updateSummary(List<Transaction> transactions) {
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            BigDecimal value = transaction.getValor() == null ? BigDecimal.ZERO : transaction.getValor();
            if (transaction.getTipo() == TransactionType.ENTRADA) {
                totalIncome = totalIncome.add(value);
            } else if (transaction.getTipo() == TransactionType.SAIDA) {
                totalExpense = totalExpense.add(value);
            }
        }

        BigDecimal generalBalance = BigDecimal.ZERO;
        for (BankAccount account : bankAccountService.findByCompany(company)) {
            generalBalance = generalBalance.add(account.getSaldo() == null ? BigDecimal.ZERO : account.getSaldo());
        }

        quantityLabel.setText(String.valueOf(transactions.size()));
        incomeLabel.setText(formatMoney(totalIncome));
        expenseLabel.setText(formatMoney(totalExpense));
        balanceLabel.setText(formatMoney(generalBalance));
    }

    private void clearFilters() {
        typeCombo.setSelectedItem(ALL_OPTION);
        accountCombo.setSelectedItem(ALL_OPTION);
        categoryCombo.setSelectedItem(ALL_OPTION);
        formCombo.setSelectedItem(ALL_OPTION);
        startDateField.setText("");
        endDateField.setText("");
        descriptionField.setText("");
        applyFilters();
    }

    private void exportDisplayedReport() {
        if (displayedTransactions.isEmpty()) {
            showError("Nao existem movimentacoes para exportar.");
            return;
        }

        // Gancho para implementar a exportacao com Apache POI.
        JOptionPane.showMessageDialog(
                this,
                "Exportacao ainda nao implementada. O relatorio atual tem "
                        + displayedTransactions.size()
                        + " movimentacao(oes)."
        );
    }

    private LocalDate parseOptionalDate(String value) {
        String trimmedValue = value == null ? "" : value.trim();
        if (trimmedValue.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(trimmedValue, dateFormatter);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Digite as datas no formato DD/MM/AAAA.");
        }
    }

    private boolean canAccessReports() {
        return role == UserCompanyRole.OWNER
                || role == UserCompanyRole.MANAGER
                || role == UserCompanyRole.INVESTMENT_MANAGER
                || role == UserCompanyRole.VIEWER;
    }

    private boolean sameFilterItem(Object first, Object second) {
        if (first == ALL_OPTION && second == ALL_OPTION) {
            return true;
        }

        if (first instanceof BankAccount firstAccount && second instanceof BankAccount secondAccount) {
            return sameBankAccount(firstAccount, secondAccount);
        }

        if (first instanceof TransactionCategory firstCategory && second instanceof TransactionCategory secondCategory) {
            return sameCategory(firstCategory, secondCategory);
        }

        return first != null && first.equals(second);
    }

    private boolean sameBankAccount(BankAccount first, BankAccount second) {
        return first != null
                && second != null
                && first.getId() != null
                && first.getId().equals(second.getId());
    }

    private boolean sameCategory(TransactionCategory first, TransactionCategory second) {
        return first != null
                && second != null
                && first.getId() != null
                && first.getId().equals(second.getId());
    }

    private String formatAccount(BankAccount account) {
        if (account == null) {
            return "sem conta";
        }

        return account.getBanco() + " - " + account.getNumeroConta();
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "sem data";
        }

        return date.format(dateFormatter);
    }

    private String formatMoney(BigDecimal value) {
        return moneyFormatter.format(value == null ? BigDecimal.ZERO : value);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
