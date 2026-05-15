package br.com.parceiroauto.view.swing;

import br.com.parceiroauto.entity.BankAccount;
import br.com.parceiroauto.entity.Company;
import br.com.parceiroauto.entity.FrequencyType;
import br.com.parceiroauto.entity.RecurrenceRule;
import br.com.parceiroauto.entity.Transaction;
import br.com.parceiroauto.entity.TransactionCategory;
import br.com.parceiroauto.entity.TransactionForm;
import br.com.parceiroauto.entity.TransactionType;
import br.com.parceiroauto.entity.UserCompanyRole;
import br.com.parceiroauto.service.BankAccountService;
import br.com.parceiroauto.service.RecurrenceRuleService;
import br.com.parceiroauto.service.TransactionCategoryService;
import br.com.parceiroauto.service.TransactionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class TransactionsPanel extends JPanel {
    private static final String UI_FONT = "Segoe UI";
    private static final String INVESTMENT_CATEGORY_NAME = "INVESTIMENTO";
    private static final int SELECTION_PANEL_WIDTH = 290;
    private static final int FORM_PANEL_WIDTH = 420;

    private final Company company;
    private final UserCompanyRole role;
    private final BankAccountService bankAccountService;
    private final RecurrenceRuleService recurrenceRuleService;
    private final TransactionCategoryService transactionCategoryService;
    private final TransactionService transactionService;
    private final Runnable dataChangedListener;
    private final JComboBox<Transaction> transactionCombo = new JComboBox<>();
    private final JComboBox<BankAccount> bankAccountCombo = new JComboBox<>();
    private final JComboBox<TransactionType> typeCombo = new JComboBox<>(TransactionType.values());
    private final JComboBox<TransactionForm> formCombo = new JComboBox<>(TransactionForm.values());
    private final JComboBox<TransactionCategory> categoryCombo = new JComboBox<>();
    private final JComboBox<FrequencyType> frequencyCombo = new JComboBox<>(FrequencyType.values());
    private final JTextField descriptionField = new JTextField();
    private final JTextField valueField = new JTextField();
    private final JTextField dateField = new JTextField();
    private final JCheckBox recurringCheck = new JCheckBox("Recorrente");
    private final JButton saveButton = new JButton("Salvar");
    private final JButton removeButton = new JButton("Remover");
    private final JButton clearButton = new JButton("Novo");
    private final JButton refreshButton = new JButton("Atualizar opções");
    private final JButton createCategoryButton = new JButton("Nova categoria");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Transaction selectedTransaction;
    private boolean loadingTransactions;

    public TransactionsPanel(
            Company company,
            UserCompanyRole role,
            BankAccountService bankAccountService,
            RecurrenceRuleService recurrenceRuleService,
            TransactionCategoryService transactionCategoryService,
            TransactionService transactionService,
            Runnable dataChangedListener
    ) {
        this.company = company;
        this.role = role;
        this.bankAccountService = bankAccountService;
        this.recurrenceRuleService = recurrenceRuleService;
        this.transactionCategoryService = transactionCategoryService;
        this.transactionService = transactionService;
        this.dataChangedListener = dataChangedListener;

        setLayout(new BorderLayout(14, 14));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(18, 18, 18, 18));

        if (!canManageTransactions()) {
            add(createAccessDeniedPanel(), BorderLayout.CENTER);
            return;
        }

        add(createHeader(), BorderLayout.NORTH);
        add(createSelectionPanel(), BorderLayout.WEST);
        add(createFormPanel(), BorderLayout.CENTER);

        configureActions();
        configureRenderers();
        loadFormOptions();
        loadTransactions();
        clearForm();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Movimentações");
        title.setFont(new Font(UI_FONT, Font.BOLD, 26));

        JLabel subtitle = new JLabel(company == null ? "Nenhuma empresa selecionada" : company.getNomeFantasia());
        subtitle.setFont(new Font(UI_FONT, Font.PLAIN, 14));

        JPanel labels = new JPanel(new GridLayout(2, 1));
        labels.setOpaque(false);
        labels.add(title);
        labels.add(subtitle);

        header.add(labels, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);
        return header;
    }

    private JPanel createSelectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(SELECTION_PANEL_WIDTH, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel title = new JLabel("Editar ou remover");
        title.setFont(new Font(UI_FONT, Font.BOLD, 18));

        JLabel label = new JLabel("Movimentação");
        label.setFont(new Font(UI_FONT, Font.PLAIN, 13));

        JButton loadButton = new JButton("Carregar");
        loadButton.addActionListener(e -> loadSelectedTransaction());

        GridBagConstraints titleConstraints = constraints(0, 0);
        titleConstraints.gridwidth = 2;
        titleConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(title, titleConstraints);

        panel.add(label, constraints(1, 0));

        GridBagConstraints comboConstraints = constraints(2, 0);
        comboConstraints.gridwidth = 2;
        comboConstraints.fill = GridBagConstraints.HORIZONTAL;
        comboConstraints.weightx = 1;
        panel.add(transactionCombo, comboConstraints);

        GridBagConstraints buttonConstraints = constraints(3, 0);
        buttonConstraints.gridwidth = 2;
        buttonConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(loadButton, buttonConstraints);

        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        GridBagConstraints spacerConstraints = constraints(4, 0);
        spacerConstraints.gridwidth = 2;
        spacerConstraints.weighty = 1;
        panel.add(spacer, spacerConstraints);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(14, 14, 14, 14)
        ));

        frequencyCombo.setEnabled(false);
        typeCombo.setEnabled(role != UserCompanyRole.INVESTMENT_MANAGER);
        categoryCombo.setEnabled(role != UserCompanyRole.INVESTMENT_MANAGER);
        createCategoryButton.setEnabled(role != UserCompanyRole.INVESTMENT_MANAGER);

        int row = 0;
        row = addField(form, row, "Conta", bankAccountCombo);
        row = addField(form, row, "Tipo", typeCombo);
        row = addField(form, row, "Data", dateField);
        row = addField(form, row, "Descrição", descriptionField);
        row = addField(form, row, "Valor", valueField);
        row = addField(form, row, "Forma", formCombo);
        row = addField(form, row, "Categoria", categoryCombo);

        GridBagConstraints categoryButtonConstraints = constraints(row++, 1);
        categoryButtonConstraints.fill = GridBagConstraints.HORIZONTAL;
        form.add(createCategoryButton, categoryButtonConstraints);

        GridBagConstraints recurringConstraints = constraints(row++, 1);
        recurringConstraints.anchor = GridBagConstraints.WEST;
        form.add(recurringCheck, recurringConstraints);

        row = addField(form, row, "Frequencia", frequencyCombo);

        JPanel buttons = new JPanel(new GridLayout(1, 3, 8, 0));
        buttons.setOpaque(false);
        buttons.add(saveButton);
        buttons.add(removeButton);
        buttons.add(clearButton);

        GridBagConstraints buttonConstraints = constraints(row, 0);
        buttonConstraints.gridwidth = 2;
        buttonConstraints.fill = GridBagConstraints.HORIZONTAL;
        buttonConstraints.insets = new Insets(18, 0, 0, 0);
        form.add(buttons, buttonConstraints);

        Dimension preferredSize = form.getPreferredSize();
        form.setPreferredSize(new Dimension(FORM_PANEL_WIDTH, preferredSize.height));

        GridBagConstraints wrapperConstraints = new GridBagConstraints();
        wrapperConstraints.gridx = 0;
        wrapperConstraints.gridy = 0;
        wrapperConstraints.anchor = GridBagConstraints.NORTHWEST;
        wrapperConstraints.weightx = 1;
        wrapperConstraints.weighty = 1;
        wrapper.add(form, wrapperConstraints);
        return wrapper;
    }

    private int addField(JPanel panel, int row, String labelText, JComponent component) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(UI_FONT, Font.PLAIN, 13));

        panel.add(label, constraints(row, 0));

        GridBagConstraints fieldConstraints = constraints(row, 1);
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.weightx = 1;
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

        JLabel label = new JLabel("Você não pode registrar movimentações com seu perfil.");
        label.setFont(new Font(UI_FONT, Font.PLAIN, 22));
        panel.add(label);
        return panel;
    }

    private void configureActions() {
        typeCombo.addActionListener(e -> reloadCategories(null));
        recurringCheck.addActionListener(e -> frequencyCombo.setEnabled(recurringCheck.isSelected()));
        refreshButton.addActionListener(e -> {
            loadFormOptions();
            loadTransactions();
        });
        clearButton.addActionListener(e -> clearForm());
        saveButton.addActionListener(e -> saveTransaction());
        removeButton.addActionListener(e -> removeTransaction());
        createCategoryButton.addActionListener(e -> createCategory());
    }

    private void configureRenderers() {
        bankAccountCombo.setRenderer(new DefaultListCellRenderer() {
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
                }
                return this;
            }
        });

        categoryCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TransactionCategory category) {
                    setText(category.getName());
                }
                return this;
            }
        });

        transactionCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Transaction transaction) {
                    setText(formatTransactionOption(transaction));
                }
                return this;
            }
        });
    }


    private void loadFormOptions() {
        bankAccountCombo.removeAllItems();
        if (company != null && bankAccountService != null) {
            List<BankAccount> accounts = bankAccountService.findByCompany(company);
            BankAccount defaultAccount = bankAccountService.findDefaultByCompany(company);
            for (BankAccount account : accounts) {
                bankAccountCombo.addItem(account);
                if (defaultAccount != null && defaultAccount.getId().equals(account.getId())) {
                    bankAccountCombo.setSelectedItem(account);
                }
            }
        }

        if (role == UserCompanyRole.INVESTMENT_MANAGER) {
            typeCombo.setSelectedItem(TransactionType.SAIDA);
        }

        reloadCategories(null);
    }

    private void loadTransactions() {
        loadingTransactions = true;
        Transaction previouslySelected = selectedTransaction;
        boolean restoredSelection = false;
        transactionCombo.removeAllItems();

        if (company != null && transactionService != null) {
            List<Transaction> transactions = transactionService.findByCompany(company);
            for (Transaction transaction : transactions) {
                transactionCombo.addItem(transaction);
                if (previouslySelected != null && sameTransaction(transaction, previouslySelected)) {
                    transactionCombo.setSelectedItem(transaction);
                    restoredSelection = true;
                }
            }
        }

        if (!restoredSelection) {
            transactionCombo.setSelectedItem(null);
        }
        loadingTransactions = false;
    }

    private void reloadCategories(TransactionCategory selectedCategory) {
        categoryCombo.removeAllItems();

        if (role == UserCompanyRole.INVESTMENT_MANAGER) {
            categoryCombo.addItem(new CategoryPlaceholder(INVESTMENT_CATEGORY_NAME, TransactionType.SAIDA));
            return;
        }

        TransactionType selectedType = (TransactionType) typeCombo.getSelectedItem();
        if (company == null || selectedType == null || transactionCategoryService == null) {
            return;
        }

        List<TransactionCategory> categories = transactionCategoryService.findActiveByCompanyAndType(company, selectedType);
        for (TransactionCategory category : categories) {
            categoryCombo.addItem(category);
            if (selectedCategory != null && selectedCategory.getId().equals(category.getId())) {
                categoryCombo.setSelectedItem(category);
            }
        }
    }

    private void clearForm() {
        selectedTransaction = null;
        if (!loadingTransactions) {
            transactionCombo.setSelectedItem(null);
        }
        descriptionField.setText("");
        valueField.setText("");
        dateField.setText(LocalDate.now().format(dateFormatter));
        recurringCheck.setSelected(false);
        frequencyCombo.setEnabled(false);
        frequencyCombo.setSelectedItem(FrequencyType.MONTHLY);
        removeButton.setEnabled(false);

        if (role == UserCompanyRole.INVESTMENT_MANAGER) {
            typeCombo.setSelectedItem(TransactionType.SAIDA);
        } else {
            typeCombo.setSelectedItem(TransactionType.ENTRADA);
        }
        loadFormOptions();
    }

    private void loadSelectedTransaction() {
        if (loadingTransactions) {
            return;
        }

        Transaction transaction = (Transaction) transactionCombo.getSelectedItem();
        if (transaction != null) {
            loadTransactionIntoForm(transaction);
        }
    }

    private void loadTransactionIntoForm(Transaction transaction) {
        selectedTransaction = transaction;
        removeButton.setEnabled(true);

        selectComboItem(bankAccountCombo, transaction.getBankAccount());
        typeCombo.setSelectedItem(resolveTypeForRole(transaction.getTipo()));
        dateField.setText(formatDate(transaction.getData()));
        descriptionField.setText(transaction.getDescricao());
        valueField.setText(transaction.getValor() == null ? "" : transaction.getValor().toPlainString());
        formCombo.setSelectedItem(transaction.getForma());

        if (role == UserCompanyRole.INVESTMENT_MANAGER) {
            reloadCategories(null);
        } else {
            reloadCategories(transaction.getTransactionCategory());
        }

        RecurrenceRule rule = findRecurrenceRule(transaction);
        recurringCheck.setSelected(rule != null);
        frequencyCombo.setEnabled(rule != null);
        frequencyCombo.setSelectedItem(rule == null ? FrequencyType.MONTHLY : rule.getFrequencia());
    }

    private void saveTransaction() {
        if (bankAccountCombo.getItemCount() == 0) {
            showError("Não existem contas bancárias cadastradas para essa empresa.");
            return;
        }

        try {
            BankAccount bankAccount = (BankAccount) bankAccountCombo.getSelectedItem();
            TransactionType type = resolveTypeForRole((TransactionType) typeCombo.getSelectedItem());
            TransactionCategory category = resolveCategoryForRole(type);
            LocalDate transactionDate = parseDate();
            String description = descriptionField.getText();
            BigDecimal value = parseValue();
            TransactionForm form = (TransactionForm) formCombo.getSelectedItem();
            FrequencyType frequency = recurringCheck.isSelected() ? (FrequencyType) frequencyCombo.getSelectedItem() : null;

            Transaction transaction;
            if (selectedTransaction == null) {
                transaction = transactionService.createTransaction(
                        company,
                        bankAccount,
                        category,
                        type,
                        description,
                        value,
                        form,
                        transactionDate
                );
            } else {
                transaction = transactionService.updateTransaction(
                        selectedTransaction,
                        bankAccount,
                        category,
                        type,
                        description,
                        value,
                        form,
                        transactionDate
                );
            }

            recurrenceRuleService.replaceRecurrenceRule(
                    transaction,
                    frequency,
                    frequency == null ? null : transactionDate,
                    null
            );

            JOptionPane.showMessageDialog(this, "Movimentação salva com sucesso.");
            loadFormOptions();
            loadTransactions();
            notifyDataChanged();
            clearForm();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Não foi possível salvar a movimentação.");
        }
    }

    private void removeTransaction() {
        if (selectedTransaction == null) {
            return;
        }

        boolean confirmed = SwingDialogs.confirmYesNo(
                this,
                "Remover a movimentação selecionada?",
                "Confirmar remoção"
        );
        if (!confirmed) {
            return;
        }

        try {
            recurrenceRuleService.replaceRecurrenceRule(selectedTransaction, null, null, null);
            transactionService.deleteTransaction(selectedTransaction);
            JOptionPane.showMessageDialog(this, "Movimentação removida com sucesso.");
            loadFormOptions();
            loadTransactions();
            notifyDataChanged();
            clearForm();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Não foi possível remover a movimentação.");
        }
    }

    private void createCategory() {
        TransactionType type = (TransactionType) typeCombo.getSelectedItem();
        if (type == null) {
            showError("Selecione o tipo antes de criar uma categoria.");
            return;
        }

        String name = JOptionPane.showInputDialog(this, "Nome da nova categoria:");
        if (name == null) {
            return;
        }

        try {
            TransactionCategory category = transactionCategoryService.createCategory(company, name, type);
            reloadCategories(category);
            JOptionPane.showMessageDialog(this, "Categoria cadastrada com sucesso.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private TransactionType resolveTypeForRole(TransactionType requestedType) {
        if (role == UserCompanyRole.INVESTMENT_MANAGER) {
            return TransactionType.SAIDA;
        }

        return requestedType;
    }

    private TransactionCategory resolveCategoryForRole(TransactionType type) {
        if (role == UserCompanyRole.INVESTMENT_MANAGER) {
            return getOrCreateInvestmentCategory();
        }

        Object selectedCategory = categoryCombo.getSelectedItem();
        if (selectedCategory instanceof TransactionCategory category) {
            return category;
        }

        throw new IllegalArgumentException("Categoria não pode ser nula");
    }

    private TransactionCategory getOrCreateInvestmentCategory() {
        List<TransactionCategory> categories = transactionCategoryService.findByCompany(company);
        for (TransactionCategory category : categories) {
            if (!INVESTMENT_CATEGORY_NAME.equalsIgnoreCase(category.getName())) {
                continue;
            }

            if (category.getTipo() != TransactionType.SAIDA) {
                throw new IllegalArgumentException("A categoria INVESTIMENTO já existe, mas não está configurada como saída.");
            }

            if (!category.isActive()) {
                transactionCategoryService.updateCategory(company, category, category.getName(), TransactionType.SAIDA, true);
            }
            return category;
        }

        return transactionCategoryService.createCategory(company, INVESTMENT_CATEGORY_NAME, TransactionType.SAIDA);
    }

    private BigDecimal parseValue() {
        String value = valueField.getText().trim().replace(",", ".");
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Digite um valor numerico valido.");
        }
    }

    private LocalDate parseDate() {
        String value = dateField.getText().trim();
        try {
            return LocalDate.parse(value, dateFormatter);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Digite a data no formato DD/MM/AAAA.");
        }
    }

    private RecurrenceRule findRecurrenceRule(Transaction transaction) {
        if (transaction == null || recurrenceRuleService == null) {
            return null;
        }

        return recurrenceRuleService.findByTransaction(transaction);
    }

    private void notifyDataChanged() {
        if (dataChangedListener != null) {
            dataChangedListener.run();
        }
    }

    private boolean canManageTransactions() {
        return role == UserCompanyRole.OWNER
                || role == UserCompanyRole.MANAGER
                || role == UserCompanyRole.INVESTMENT_MANAGER;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private <T> void selectComboItem(JComboBox<T> combo, T selectedItem) {
        if (selectedItem == null) {
            combo.setSelectedItem(null);
            return;
        }

        for (int i = 0; i < combo.getItemCount(); i++) {
            T item = combo.getItemAt(i);
            if (sameEntity(item, selectedItem)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private boolean sameEntity(Object first, Object second) {
        if (first instanceof BankAccount firstAccount && second instanceof BankAccount secondAccount) {
            return firstAccount.getId() != null && firstAccount.getId().equals(secondAccount.getId());
        }

        if (first instanceof TransactionCategory firstCategory && second instanceof TransactionCategory secondCategory) {
            return firstCategory.getId() != null && firstCategory.getId().equals(secondCategory.getId());
        }

        return first != null && first.equals(second);
    }

    private boolean sameTransaction(Transaction first, Transaction second) {
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

    private String formatCategory(TransactionCategory category) {
        if (category == null) {
            return "sem categoria";
        }

        return category.getName();
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return LocalDate.now().format(dateFormatter);
        }

        return date.format(dateFormatter);
    }

    private String formatTransactionOption(Transaction transaction) {
        String date = transaction.getData() == null ? "sem data" : transaction.getData().format(dateFormatter);
        String description = transaction.getDescricao() == null ? "sem descrição" : transaction.getDescricao();
        String value = transaction.getValor() == null ? "sem valor" : transaction.getValor().toPlainString();
        return date + " | " + transaction.getTipo() + " | " + description + " | " + value;
    }

    private static class CategoryPlaceholder extends TransactionCategory {
        CategoryPlaceholder(String name, TransactionType type) {
            super(name, null, type, true);
        }
    }
}
