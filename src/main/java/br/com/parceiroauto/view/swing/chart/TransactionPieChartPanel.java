package br.com.parceiroauto.view.swing.chart;

import br.com.parceiroauto.model.entity.Transaction;
import br.com.parceiroauto.model.entity.TransactionCategory;
import br.com.parceiroauto.model.entity.TransactionType;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.BasicStroke;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class TransactionPieChartPanel {
    private static final String CATEGORY_CARD = "category";
    private static final String OVERVIEW_CARD = "overview";
    private static final Color INCOME_COLOR = new Color(38, 166, 91);
    private static final Color EXPENSE_COLOR = new Color(220, 53, 69);
    private static final Color EMPTY_COLOR = new Color(210, 210, 210);
    private static final BasicStroke TYPE_OUTLINE_STROKE = new BasicStroke(4f);
    private static final NumberFormat MONEY_FORMATTER = NumberFormat.getCurrencyInstance(
            new Locale.Builder().setLanguage("pt").setRegion("BR").build()
    );
    private static final NumberFormat PERCENT_FORMATTER = NumberFormat.getPercentInstance(
            new Locale.Builder().setLanguage("pt").setRegion("BR").build()
    );

    private TransactionPieChartPanel() {
    }

    public static JPanel createPanel(List<Transaction> transactions) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        CardLayout cardLayout = new CardLayout();
        JPanel chartCards = new JPanel(cardLayout);
        chartCards.setOpaque(false);
        chartCards.add(createCategoryChart(transactions), CATEGORY_CARD);
        chartCards.add(createOverviewChart(transactions), OVERVIEW_CARD);

        JRadioButton categoryButton = createOptionButton("Por categoria", true);
        JRadioButton overviewButton = createOptionButton("Entradas x Saídas", false);

        ButtonGroup group = new ButtonGroup();
        group.add(categoryButton);
        group.add(overviewButton);

        categoryButton.addActionListener(e -> cardLayout.show(chartCards, CATEGORY_CARD));
        overviewButton.addActionListener(e -> cardLayout.show(chartCards, OVERVIEW_CARD));

        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        optionsPanel.setOpaque(false);
        optionsPanel.add(categoryButton);
        optionsPanel.add(overviewButton);

        panel.add(optionsPanel, BorderLayout.NORTH);
        panel.add(chartCards, BorderLayout.CENTER);
        return panel;
    }

    private static ChartPanel createCategoryChart(List<Transaction> transactions) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        Map<String, CategoryTotal> totalsByCategory = new LinkedHashMap<>();

        for (Transaction transaction : safeTransactions(transactions)) {
            String sectionName = getCategorySectionName(transaction);
            totalsByCategory.compute(sectionName, (category, currentTotal) -> {
                if (currentTotal == null) {
                    return new CategoryTotal(transaction.getTipo(), transaction.getValor());
                }

                currentTotal.add(transaction.getValor());
                return currentTotal;
            });
        }

        totalsByCategory.forEach((category, total) -> dataset.setValue(category, total.value()));
        boolean empty = totalsByCategory.isEmpty();
        if (empty) {
            dataset.setValue("Sem dados", 1);
        }

        JFreeChart chart = ChartFactory.createPieChart("Movimentações por categoria", dataset, true, true, false);
        chart.addSubtitle(new TextTitle("Borda verde: ENTRADA | Borda vermelha: SAÍDA"));
        PiePlot<String> plot = configurePlot(chart, empty);

        if (!empty) {
            totalsByCategory.forEach((category, total) -> {
                plot.setSectionPaint(category, colorForCategory(category));
                plot.setSectionOutlineStroke(category, TYPE_OUTLINE_STROKE);
                plot.setSectionOutlinePaint(category, colorForType(total.type()));
            });
        }

        return createChartPanel(chart);
    }

    private static ChartPanel createOverviewChart(List<Transaction> transactions) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;

        for (Transaction transaction : safeTransactions(transactions)) {
            if (transaction.getTipo() == TransactionType.ENTRADA) {
                income = income.add(transaction.getValor());
            } else if (transaction.getTipo() == TransactionType.SAIDA) {
                expense = expense.add(transaction.getValor());
            }
        }

        boolean empty = income.compareTo(BigDecimal.ZERO) == 0 && expense.compareTo(BigDecimal.ZERO) == 0;
        if (empty) {
            dataset.setValue("Sem dados", 1);
        } else {
            dataset.setValue("Entradas", income);
            dataset.setValue("Saídas", expense);
        }

        JFreeChart chart = ChartFactory.createPieChart("Entradas x Saídas", dataset, true, true, false);
        PiePlot<String> plot = configurePlot(chart, empty);

        if (!empty) {
            plot.setSectionPaint("Entradas", INCOME_COLOR);
            plot.setSectionPaint("Saídas", EXPENSE_COLOR);
        }

        return createChartPanel(chart);
    }

    private static PiePlot<String> configurePlot(JFreeChart chart, boolean empty) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 22));

        @SuppressWarnings("unchecked")
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 13));
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}: {1}",
                MONEY_FORMATTER,
                PERCENT_FORMATTER
        ));
        plot.setNoDataMessage("Sem movimentações cadastradas");
        plot.setShadowPaint(null);

        if (empty) {
            plot.setSectionPaint("Sem dados", EMPTY_COLOR);
            plot.setLabelGenerator(null);
        }

        return plot;
    }

    private static ChartPanel createChartPanel(JFreeChart chart) {
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setOpaque(false);
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createEmptyBorder());
        return chartPanel;
    }

    private static JRadioButton createOptionButton(String text, boolean selected) {
        JRadioButton button = new JRadioButton(text, selected);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        return button;
    }

    private static List<Transaction> safeTransactions(List<Transaction> transactions) {
        return transactions == null ? List.of() : transactions;
    }

    private static String getCategorySectionName(Transaction transaction) {
        TransactionCategory category = transaction.getTransactionCategory();
        String prefix = transaction.getTipo() == TransactionType.ENTRADA ? "Entrada | " : "Saída | ";
        if (category == null || category.getName() == null || category.getName().isBlank()) {
            return prefix + "Sem categoria";
        }

        return prefix + category.getName();
    }

    private static Color colorForCategory(String category) {
        float hue = Math.abs(category.hashCode() % 360) / 360f;
        return Color.getHSBColor(hue, 0.58f, 0.85f);
    }

    private static Color colorForType(TransactionType type) {
        return type == TransactionType.ENTRADA ? INCOME_COLOR : EXPENSE_COLOR;
    }

    private static final class CategoryTotal {
        private final TransactionType type;
        private BigDecimal value;

        private CategoryTotal(TransactionType type, BigDecimal value) {
            this.type = type;
            this.value = value == null ? BigDecimal.ZERO : value;
        }

        private void add(BigDecimal amount) {
            if (amount != null) {
                value = value.add(amount);
            }
        }

        private TransactionType type() {
            return type;
        }

        private BigDecimal value() {
            return value;
        }
    }
}
