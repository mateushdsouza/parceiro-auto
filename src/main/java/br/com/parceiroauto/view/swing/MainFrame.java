package br.com.parceiroauto.view.swing;

import br.com.parceiroauto.confg.AppContext;
import br.com.parceiroauto.entity.*;
import br.com.parceiroauto.service.RecurrenceRuleService;
import br.com.parceiroauto.service.TransactionService;
import br.com.parceiroauto.view.swing.chart.TransactionPieChartPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class MainFrame extends JFrame {
    private static final String HOME_CARD = "home";
    private static final String BANK_ACCOUNT_CARD = "bankAccount";
    private static final String TRANSACTIONS_CARD = "transactions";
    private static final String REPORTS_CARD = "reports";
    private static final String EMPLOYEES_CARD = "employees";
    private static final int SIDE_PANEL_ITEMS_LIMIT = 3;

    private final User user;
    private final UserCompany userCompany;
    private final AppContext context;
    private final CardLayout contentLayout;
    private final JPanel contentPanel;
    private JPanel homePanel;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NumberFormat moneyFormatter = NumberFormat.getCurrencyInstance(
            new Locale.Builder().setLanguage("pt").setRegion("BR").build()
    );

    public MainFrame(User user, UserCompany userCompany, AppContext context) {
        this.user = user;
        this.userCompany = userCompany;
        this.context = context;
        this.contentLayout = new CardLayout();
        this.contentPanel = new JPanel(contentLayout);

        setTitle("Parceiro Auto");
        setMinimumSize(new Dimension(900, 520));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.BLACK);

        add(createTopMenu(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);
        add(createUserInfoPanel(), BorderLayout.SOUTH);

        showContent(HOME_CARD);
        setVisible(true);
    }

    private JPanel createTopMenu() {
        JPanel topMenu = new JPanel(new BorderLayout(12, 0));
        topMenu.setBackground(Color.BLACK);
        topMenu.setBorder(new EmptyBorder(18, 24, 0, 18));

        JButton btnUserOptions = new JButton("opcoes do usuario");

        btnUserOptions.setOpaque(true);
        btnUserOptions.setBackground(Color.WHITE);
        btnUserOptions.setBorder(new EmptyBorder(0, 12, 0, 12));
        btnUserOptions.setFont(new Font("Arial", Font.PLAIN, 26));

        JPopupMenu popupMenu = createPopupMenu();

        btnUserOptions.addActionListener(e -> {

            popupMenu.show(
                    btnUserOptions,
                    0,
                    btnUserOptions.getHeight()
            );
        });

        topMenu.add(btnUserOptions, BorderLayout.WEST);

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 5, 4, 0));
        buttonsPanel.setBackground(Color.BLACK);
        buttonsPanel.add(createMenuButton("home", HOME_CARD));
        buttonsPanel.add(createMenuButton("<html>conta<br>bancaria</html>", BANK_ACCOUNT_CARD));
        buttonsPanel.add(createMenuButton("movimentacoes", TRANSACTIONS_CARD));
        buttonsPanel.add(createMenuButton("relatorios", REPORTS_CARD));
        buttonsPanel.add(createMenuButton("funcionarios", EMPLOYEES_CARD));

        topMenu.add(buttonsPanel, BorderLayout.CENTER);
        return topMenu;
    }

    private JPopupMenu createPopupMenu() {

        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem logout = new JMenuItem("Logout");
        logout.setOpaque(true);
        logout.setBackground(Color.WHITE);
        logout.setBorder(new EmptyBorder(0, 12, 0, 12));
        logout.setFont(new Font("Arial", Font.PLAIN, 26));

        JMenuItem trocarEmpresa = new JMenuItem("Trocar empresa");
        trocarEmpresa.setOpaque(true);
        trocarEmpresa.setBackground(Color.WHITE);
        trocarEmpresa.setBorder(new EmptyBorder(0, 12, 0, 12));
        trocarEmpresa.setFont(new Font("Arial", Font.PLAIN, 26));


        popupMenu.add(trocarEmpresa);
        popupMenu.add(logout);

        // ação logout
        logout.addActionListener(e -> {

            dispose();

            new LoginFrame(context);
        });

        // ação trocar empresa
        trocarEmpresa.addActionListener(e -> {

            dispose();

            new LoginCompanyFrame(user, context);
        });


        return popupMenu;
    }


    private JButton createMenuButton(String text, String cardName) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Arial", Font.PLAIN, 18));
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        button.addActionListener(e -> showContent(cardName));
        return button;
    }

    private JPanel createContentPanel() {
        contentPanel.setBackground(Color.BLACK);
        contentPanel.setBorder(new EmptyBorder(0, 24, 0, 18));

        homePanel = createHomePanel();
        contentPanel.add(homePanel, HOME_CARD);
        contentPanel.add(createBankAccountPanel(), BANK_ACCOUNT_CARD);
        contentPanel.add(createTransactionsPanel(), TRANSACTIONS_CARD);
        contentPanel.add(createPlaceholderPanel("Relatorios"), REPORTS_CARD);
        contentPanel.add(createEmployeesPanel(), EMPLOYEES_CARD);

        return contentPanel;
    }

    private JPanel createTransactionsPanel() {
        return new TransactionsPanel(
                getSelectedCompany(),
                userCompany == null ? null : userCompany.getRole(),
                context.getBankAccountService(),
                context.getRecurrenceRuleService(),
                context.getTransactionCategoryService(),
                context.getTransactionService(),
                this::refreshHomePanel
        );
    }

    private JPanel createBankAccountPanel() {
        return new BankAccountPanel(
                getSelectedCompany(),
                context.getBankAccountController(),
                this::refreshHomePanel
        );
    }

    private JPanel createEmployeesPanel(){
        return new EmployeesPanel(
                getSelectedCompany(),
                user,
                userCompany == null ? null : userCompany.getRole(),
                context.getEmployeeController(),
                this::refreshHomePanel
        );
    }

    private void refreshHomePanel() {
        if (homePanel == null) {
            return;
        }

        contentPanel.remove(homePanel);
        homePanel = createHomePanel();
        contentPanel.add(homePanel, HOME_CARD);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createHomePanel() {
        JPanel wrapper = new JPanel(new BorderLayout(10, 0));
        wrapper.setBackground(Color.BLACK);

        JPanel chartPanel = TransactionPieChartPanel.createPanel(loadTransactionsForChart());

        JPanel sidePanel = new JPanel(new GridLayout(2, 1, 0, 0));
        sidePanel.setPreferredSize(new Dimension(360, 0));
        sidePanel.setBackground(Color.BLACK);
        sidePanel.add(createSummarySection("Proximas recorrentes", loadUpcomingRecurrences()));
        sidePanel.add(createSummarySection("Ultimas movimentacoes", loadLastTransactions()));

        wrapper.add(chartPanel, BorderLayout.CENTER);
        wrapper.add(sidePanel, BorderLayout.EAST);
        return wrapper;
    }

    private JPanel createSummarySection(String title, DefaultListModel<String> model) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(18, 14, 18, 14));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel itemsPanel = new JPanel();
        itemsPanel.setOpaque(false);
        itemsPanel.setLayout(new GridLayout(Math.max(model.size(), 1), 1, 0, 8));

        for (int i = 0; i < model.size(); i++) {
            JLabel itemLabel = new JLabel(model.get(i));
            itemLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            itemLabel.setVerticalAlignment(SwingConstants.TOP);
            itemLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    new EmptyBorder(8, 8, 8, 8)
            ));
            itemsPanel.add(itemLabel);
        }

        panel.add(label, BorderLayout.NORTH);
        panel.add(itemsPanel, BorderLayout.CENTER);
        return panel;
    }

    private DefaultListModel<String> loadUpcomingRecurrences() {
        DefaultListModel<String> model = new DefaultListModel<>();
        Company company = getSelectedCompany();

        RecurrenceRuleService recurrenceRuleService = context.getRecurrenceRuleService();
        if (company == null || recurrenceRuleService == null) {
            model.addElement("Nenhuma empresa selecionada");
            return model;
        }

        List<RecurrenceRule> rules = recurrenceRuleService.findUpcomingByCompany(company, SIDE_PANEL_ITEMS_LIMIT);

        if (rules.isEmpty()) {
            model.addElement("Sem recorrencias futuras");
            return model;
        }

        for (RecurrenceRule rule : rules) {
            Transaction transaction = rule.getTransaction();
            LocalDate nextExecution = recurrenceRuleService.calculateNextExecution(rule);
            model.addElement(formatRecurrenceDetails(rule, transaction, nextExecution));
        }

        return model;
    }

    private DefaultListModel<String> loadLastTransactions() {
        DefaultListModel<String> model = new DefaultListModel<>();
        Company company = getSelectedCompany();

        TransactionService transactionService = context.getTransactionService();
        if (company == null || transactionService == null) {
            model.addElement("Nenhuma empresa selecionada");
            return model;
        }

        List<Transaction> transactions = transactionService.findLastByCompany(company, SIDE_PANEL_ITEMS_LIMIT);

        if (transactions.isEmpty()) {
            model.addElement("Sem movimentacoes cadastradas");
            return model;
        }

        for (Transaction transaction : transactions) {
            model.addElement(formatTransactionDetails(transaction.getData(), transaction));
        }

        return model;
    }

    private List<Transaction> loadTransactionsForChart() {
        Company company = getSelectedCompany();

        TransactionService transactionService = context.getTransactionService();
        if (company == null || transactionService == null) {
            return List.of();
        }

        return transactionService.findByCompany(company);
    }

    private Company getSelectedCompany() {
        if (userCompany == null) {
            return null;
        }

        return userCompany.getCompany();
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "sem data";
        }

        return date.format(dateFormatter);
    }

    private String formatRecurrenceDetails(RecurrenceRule rule, Transaction transaction, LocalDate nextExecution) {
        return "<html>"
                + "<b>Proxima:</b> " + escapeHtml(formatDate(nextExecution)) + "<br>"
                + "<b>Frequencia:</b> " + escapeHtml(String.valueOf(rule.getFrequencia())) + "<br>"
                + formatTransactionRows(transaction)
                + "</html>";
    }

    private String formatTransactionDetails(LocalDate date, Transaction transaction) {
        return "<html>"
                + "<b>Data:</b> " + escapeHtml(formatDate(date)) + "<br>"
                + formatTransactionRows(transaction)
                + "</html>";
    }

    private String formatTransactionRows(Transaction transaction) {
        if (transaction == null) {
            return "movimentacao nao informada";
        }

        String value = transaction.getValor() == null ? "sem valor" : moneyFormatter.format(transaction.getValor());
        TransactionCategory category = transaction.getTransactionCategory();
        String categoryName = category == null ? "sem categoria" : category.getName();
        String bankAccount = transaction.getBankAccount() == null
                ? "sem conta"
                : transaction.getBankAccount().getBanco() + " - " + transaction.getBankAccount().getNumeroConta();

        return "<b>Descricao:</b> " + escapeHtml(transaction.getDescricao()) + "<br>"
                + "<b>Tipo:</b> " + escapeHtml(String.valueOf(transaction.getTipo())) + "<br>"
                + "<b>Forma:</b> " + escapeHtml(String.valueOf(transaction.getForma())) + "<br>"
                + "<b>Categoria:</b> " + escapeHtml(categoryName) + "<br>"
                + "<b>Conta:</b> " + escapeHtml(bankAccount) + "<br>"
                + "<b>Valor:</b> " + escapeHtml(value);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private JPanel createPlaceholderPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.PLAIN, 32));
        panel.add(label);

        return panel;
    }

    private JPanel createUserInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        panel.setBorder(new EmptyBorder(0, 24, 18, 18));

        JLabel label = new JLabel(buildUserInfoText());
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setForeground(Color.BLACK);
        label.setFont(new Font("Arial", Font.PLAIN, 24));
        label.setBorder(new EmptyBorder(4, 18, 4, 18));

        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private String buildUserInfoText() {
        if (user == null || userCompany == null) {
            return "informacoes do usuario";
        }

        Company company = userCompany.getCompany();
        String companyName = company == null ? "empresa nao informada" : company.getNomeFantasia();
        return "usuario: " + user.getLogin() + " | empresa: " + companyName + " | perfil: " + userCompany.getRole();
    }

    private void showContent(String cardName) {
        if (HOME_CARD.equals(cardName)) {
            refreshHomePanel();
        }
        contentLayout.show(contentPanel, cardName);
    }
}
