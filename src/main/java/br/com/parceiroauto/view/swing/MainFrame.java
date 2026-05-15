package br.com.parceiroauto.view.swing;

import br.com.parceiroauto.confg.AppContext;
import br.com.parceiroauto.entity.*;
import br.com.parceiroauto.service.RecurrenceRuleService;
import br.com.parceiroauto.service.TransactionService;
import br.com.parceiroauto.view.swing.chart.TransactionPieChartPanel;
import jiconfont.icons.elusive.Elusive;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class MainFrame extends JFrame {
    private static final String APP_NAME = "ParceiroAuto";
    private static final String UI_FONT = "Segoe UI";
    private static final String HOME_CARD = "home";
    private static final String BANK_ACCOUNT_CARD = "bankAccount";
    private static final String TRANSACTIONS_CARD = "transactions";
    private static final String REPORTS_CARD = "reports";
    private static final String EMPLOYEES_CARD = "employees";
    private static final int SIDE_PANEL_ITEMS_LIMIT = 3;
    private static final int HOME_SIDE_PANEL_WIDTH = 330;

    private final User user;
    private final UserCompany userCompany;
    private final AppContext context;
    private final CardLayout contentLayout;
    private final JPanel contentPanel;
    private JPanel homePanel;
    private ReportsPanel reportsPanel;
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

        setTitle(APP_NAME);
        setMinimumSize(new Dimension(900, 520));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.BLACK);

        add(createTopMenu(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);
        add(createUserInfoPanel(), BorderLayout.SOUTH);

        showContent(getInitialCard());
        setVisible(true);
    }

    private JPanel createTopMenu() {
        JPanel topMenu = new JPanel(new BorderLayout(12, 0));
        topMenu.setBackground(Color.BLACK);
        topMenu.setBorder(new EmptyBorder(18, 24, 0, 18));

        IconFontSwing.register(Elusive.getIconFont());

        JButton btnUserOptions = new JButton("OPÇÕES DO USUÁRIO");
        Icon icon = IconFontSwing.buildIcon(Elusive.USER, 15);
        btnUserOptions.setIcon(icon);


        btnUserOptions.setOpaque(true);
        btnUserOptions.setBackground(Color.WHITE);
        btnUserOptions.setBorder(new EmptyBorder(0, 12, 0, 12));
        btnUserOptions.setFont(new Font(UI_FONT, Font.BOLD, 22));

        JPopupMenu popupMenu = createPopupMenu();

        btnUserOptions.addActionListener(e -> {

            popupMenu.show(
                    btnUserOptions,
                    0,
                    btnUserOptions.getHeight()
            );
        });

        topMenu.add(btnUserOptions, BorderLayout.WEST);

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 0, 4, 0));
        buttonsPanel.setBackground(Color.BLACK);
        if (canAccessHome()) {
            buttonsPanel.add(createMenuButton(APP_NAME.toUpperCase(), HOME_CARD));
        }
        if (canManageBankAccounts()) {
            buttonsPanel.add(createMenuButton("<html>CONTA<br>BANCÁRIA</html>", BANK_ACCOUNT_CARD));
        }
        if (canManageTransactions()) {
            buttonsPanel.add(createMenuButton("MOVIMENTAÇÕES", TRANSACTIONS_CARD));
        }
        if (canAccessReports()) {
            buttonsPanel.add(createMenuButton("RELATÓRIOS", REPORTS_CARD));
        }
        if (canManageEmployees()) {
            buttonsPanel.add(createMenuButton("FUNCIONÁRIOS", EMPLOYEES_CARD));
        }

        topMenu.add(buttonsPanel, BorderLayout.CENTER);
        return topMenu;
    }

    private JPopupMenu createPopupMenu() {

        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem logout = new JMenuItem("Sair");
        logout.setOpaque(true);
        logout.setBackground(Color.WHITE);
        logout.setBorder(new EmptyBorder(0, 12, 0, 12));
        logout.setFont(new Font(UI_FONT, Font.PLAIN, 24));

        JMenuItem trocarEmpresa = new JMenuItem("Trocar empresa");
        trocarEmpresa.setOpaque(true);
        trocarEmpresa.setBackground(Color.WHITE);
        trocarEmpresa.setBorder(new EmptyBorder(0, 12, 0, 12));
        trocarEmpresa.setFont(new Font(UI_FONT, Font.PLAIN, 24));


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
        button.setFont(new Font(UI_FONT, Font.BOLD, 16));
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        button.addActionListener(e -> showContent(cardName));
        return button;
    }

    private JPanel createContentPanel() {
        contentPanel.setBackground(Color.BLACK);
        contentPanel.setBorder(new EmptyBorder(0, 24, 0, 18));

        if (canAccessHome()) {
            homePanel = createHomePanel();
            contentPanel.add(homePanel, HOME_CARD);
        }
        if (canManageBankAccounts()) {
            contentPanel.add(createBankAccountPanel(), BANK_ACCOUNT_CARD);
        }
        if (canManageTransactions()) {
            contentPanel.add(createTransactionsPanel(), TRANSACTIONS_CARD);
        }
        if (canAccessReports()) {
            reportsPanel = createReportsPanel();
            contentPanel.add(reportsPanel, REPORTS_CARD);
        }
        if (canManageEmployees()) {
            contentPanel.add(createEmployeesPanel(), EMPLOYEES_CARD);
        }

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
                userCompany == null ? null : userCompany.getRole(),
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

    private ReportsPanel createReportsPanel() {
        return new ReportsPanel(
                getSelectedCompany(),
                userCompany == null ? null : userCompany.getRole(),
                context.getBankAccountService(),
                context.getTransactionCategoryService(),
                context.getTransactionService()
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
        JPanel wrapper = new JPanel(new BorderLayout(10, 12));
        wrapper.setBackground(Color.BLACK);

        JPanel chartPanel = TransactionPieChartPanel.createPanel(loadTransactionsForChart());

        JPanel sidePanel = new JPanel(new GridLayout(2, 1, 0, 0));
        sidePanel.setPreferredSize(new Dimension(HOME_SIDE_PANEL_WIDTH, 0));
        sidePanel.setBackground(Color.BLACK);
        sidePanel.add(createSummarySection("Próximas recorrentes", loadUpcomingRecurrences()));
        sidePanel.add(createSummarySection("Últimas movimentações", loadLastTransactions()));

        JLabel brandLabel = new JLabel(APP_NAME);
        brandLabel.setOpaque(true);
        brandLabel.setBackground(Color.WHITE);
        brandLabel.setForeground(Color.BLACK);
        brandLabel.setFont(new Font(UI_FONT, Font.BOLD, 30));
        brandLabel.setBorder(new EmptyBorder(10, 16, 10, 16));

        JPanel content = new JPanel(new BorderLayout(10, 0));
        content.setBackground(Color.BLACK);
        content.add(chartPanel, BorderLayout.CENTER);
        content.add(sidePanel, BorderLayout.EAST);

        wrapper.add(brandLabel, BorderLayout.NORTH);
        wrapper.add(content, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createSummarySection(String title, DefaultListModel<String> model) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel label = new JLabel(title);
        label.setFont(new Font(UI_FONT, Font.BOLD, 20));
        label.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel itemsPanel = new JPanel();
        itemsPanel.setOpaque(false);
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));

        for (int i = 0; i < model.size(); i++) {
            JLabel itemLabel = new JLabel(model.get(i));
            itemLabel.setFont(new Font(UI_FONT, Font.PLAIN, 14));
            itemLabel.setVerticalAlignment(SwingConstants.TOP);
            itemLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            itemLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    new EmptyBorder(6, 8, 6, 8)
            ));
            itemsPanel.add(itemLabel);
            if (i < model.size() - 1) {
                itemsPanel.add(Box.createVerticalStrut(8));
            }
        }

        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
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
            model.addElement("Sem recorrências futuras");
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
            model.addElement("Sem movimentações cadastradas");
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

    private UserCompanyRole getCurrentRole() {
        return userCompany == null ? null : userCompany.getRole();
    }

    private String getInitialCard() {
        if (canAccessHome()) {
            return HOME_CARD;
        }
        if (canAccessReports()) {
            return REPORTS_CARD;
        }
        if (canManageTransactions()) {
            return TRANSACTIONS_CARD;
        }
        if (canManageBankAccounts()) {
            return BANK_ACCOUNT_CARD;
        }
        if (canManageEmployees()) {
            return EMPLOYEES_CARD;
        }
        return HOME_CARD;
    }

    private boolean canAccessHome() {
        return getCurrentRole() == UserCompanyRole.OWNER
                || getCurrentRole() == UserCompanyRole.MANAGER
                || getCurrentRole() == UserCompanyRole.INVESTMENT_MANAGER;
    }

    private boolean canManageBankAccounts() {
        return getCurrentRole() == UserCompanyRole.OWNER
                || getCurrentRole() == UserCompanyRole.MANAGER;
    }

    private boolean canManageTransactions() {
        return getCurrentRole() == UserCompanyRole.OWNER
                || getCurrentRole() == UserCompanyRole.MANAGER
                || getCurrentRole() == UserCompanyRole.INVESTMENT_MANAGER;
    }

    private boolean canAccessReports() {
        return getCurrentRole() == UserCompanyRole.OWNER
                || getCurrentRole() == UserCompanyRole.MANAGER
                || getCurrentRole() == UserCompanyRole.INVESTMENT_MANAGER
                || getCurrentRole() == UserCompanyRole.VIEWER;
    }

    private boolean canManageEmployees() {
        return getCurrentRole() == UserCompanyRole.OWNER
                || getCurrentRole() == UserCompanyRole.MANAGER;
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "sem data";
        }

        return date.format(dateFormatter);
    }

    private String formatRecurrenceDetails(RecurrenceRule rule, Transaction transaction, LocalDate nextExecution) {
        return "<html>"
                + "<b>Proxima:</b> " + escapeHtml(formatDate(nextExecution))
                + " | <b>Frequencia:</b> " + escapeHtml(String.valueOf(rule.getFrequencia())) + "<br>"
                + formatCompactTransactionRows(transaction)
                + "</html>";
    }

    private String formatTransactionDetails(LocalDate date, Transaction transaction) {
        return "<html>"
                + "<b>Data:</b> " + escapeHtml(formatDate(date)) + "<br>"
                + formatCompactTransactionRows(transaction)
                + "</html>";
    }

    private String formatCompactTransactionRows(Transaction transaction) {
        if (transaction == null) {
            return "movimentação não informada";
        }

        String value = transaction.getValor() == null ? "sem valor" : moneyFormatter.format(transaction.getValor());
        TransactionCategory category = transaction.getTransactionCategory();
        String categoryName = category == null ? "sem categoria" : category.getName();

        return "<b>Descrição:</b> " + escapeHtml(transaction.getDescricao()) + "<br>"
                + "<b>Tipo:</b> " + escapeHtml(String.valueOf(transaction.getTipo()))
                + " | <b>Categoria:</b> " + escapeHtml(categoryName) + "<br>"
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
        label.setFont(new Font(UI_FONT, Font.PLAIN, 32));
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
        label.setFont(new Font(UI_FONT, Font.PLAIN, 22));
        label.setBorder(new EmptyBorder(4, 18, 4, 18));

        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private String buildUserInfoText() {
        if (user == null || userCompany == null) {
            return "informações do usuário";
        }

        Company company = userCompany.getCompany();
        String companyName = company == null ? "empresa não informada" : company.getNomeFantasia();
        return "usuário: " + user.getLogin() + " | empresa: " + companyName + " | perfil: " + userCompany.getRole();
    }

    private void showContent(String cardName) {
        if (!canAccessCard(cardName)) {
            cardName = getInitialCard();
        }

        if (HOME_CARD.equals(cardName)) {
            refreshHomePanel();
        } else if (REPORTS_CARD.equals(cardName) && reportsPanel != null) {
            reportsPanel.refreshData();
        }
        contentLayout.show(contentPanel, cardName);
    }

    private boolean canAccessCard(String cardName) {
        return switch (cardName) {
            case HOME_CARD -> canAccessHome();
            case BANK_ACCOUNT_CARD -> canManageBankAccounts();
            case TRANSACTIONS_CARD -> canManageTransactions();
            case REPORTS_CARD -> canAccessReports();
            case EMPLOYEES_CARD -> canManageEmployees();
            default -> false;
        };
    }
}
