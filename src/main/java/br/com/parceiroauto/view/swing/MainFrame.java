package br.com.parceiroauto.view.swing;

import br.com.parceiroauto.entity.Company;
import br.com.parceiroauto.entity.User;
import br.com.parceiroauto.entity.UserCompany;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {
    private static final String HOME_CARD = "home";
    private static final String BANK_ACCOUNT_CARD = "bankAccount";
    private static final String TRANSACTIONS_CARD = "transactions";
    private static final String REPORTS_CARD = "reports";
    private static final String EMPLOYEES_CARD = "employees";

    private final User user;
    private final UserCompany userCompany;
    private final CardLayout contentLayout;
    private final JPanel contentPanel;

    public MainFrame() {
        this(null, null);
    }

    public MainFrame(User user, UserCompany userCompany) {
        this.user = user;
        this.userCompany = userCompany;
        this.contentLayout = new CardLayout();
        this.contentPanel = new JPanel(contentLayout);

        setTitle("Parceiro Auto");
        setSize(1050, 560);
        setMinimumSize(new Dimension(900, 520));
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

        JLabel userOptions = new JLabel("opcoes do usuario");
        userOptions.setOpaque(true);
        userOptions.setBackground(Color.WHITE);
        userOptions.setBorder(new EmptyBorder(0, 12, 0, 12));
        userOptions.setFont(new Font("Arial", Font.PLAIN, 26));
        topMenu.add(userOptions, BorderLayout.WEST);

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

        contentPanel.add(createHomePanel(), HOME_CARD);
        contentPanel.add(createPlaceholderPanel("Conta bancaria"), BANK_ACCOUNT_CARD);
        contentPanel.add(createPlaceholderPanel("Movimentacoes"), TRANSACTIONS_CARD);
        contentPanel.add(createPlaceholderPanel("Relatorios"), REPORTS_CARD);
        contentPanel.add(createPlaceholderPanel("Funcionarios"), EMPLOYEES_CARD);

        return contentPanel;
    }

    private JPanel createHomePanel() {
        JPanel wrapper = new JPanel(new BorderLayout(10, 0));
        wrapper.setBackground(Color.BLACK);

        JPanel chartPanel = new JPanel(new GridBagLayout());
        chartPanel.setBackground(Color.WHITE);
        chartPanel.add(new ChartPlaceholderPanel());

        JPanel sidePanel = new JPanel(new GridLayout(2, 1, 0, 0));
        sidePanel.setPreferredSize(new Dimension(265, 0));
        sidePanel.setBackground(Color.BLACK);
        sidePanel.add(wrapSideArea("proximas\nmovimentacoes\nrecorrentes"));
        sidePanel.add(wrapSideArea("ultimas 3\nmovimentacoes"));

        wrapper.add(chartPanel, BorderLayout.CENTER);
        wrapper.add(sidePanel, BorderLayout.EAST);
        return wrapper;
    }

    private JPanel wrapSideArea(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(30, 8, 10, 8));

        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setOpaque(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 28));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        panel.add(textArea, BorderLayout.NORTH);
        return panel;
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
        contentLayout.show(contentPanel, cardName);
    }

    private static class ChartPlaceholderPanel extends JPanel {
        private ChartPlaceholderPanel() {
            setPreferredSize(new Dimension(280, 280));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(12));
            g2.drawOval(25, 25, 230, 230);
            g2.setFont(new Font("Arial", Font.PLAIN, 28));
            FontMetrics metrics = g2.getFontMetrics();
            String text = "JFreeChart";
            int x = (getWidth() - metrics.stringWidth(text)) / 2;
            int y = (getHeight() + metrics.getAscent()) / 2 - 10;
            g2.drawString(text, x, y);
            g2.dispose();
        }
    }
}
