package br.com.parceiroauto.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_id_company", nullable = false)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "fk_id_bank_account")
    private BankAccount bankAccount;

    @ManyToOne
    @JoinColumn(name = "fk_id_transaction_category")
    private TransactionCategory transactionCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType tipo;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate data;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionForm forma;

    public Transaction() {
    }

    public Transaction(Company company, BankAccount bankAccount, TransactionCategory transactionCategory,
                       TransactionType tipo, String descricao, BigDecimal valor, TransactionForm forma) {
        this.company = company;
        this.bankAccount = bankAccount;
        this.transactionCategory = transactionCategory;
        this.tipo = tipo;
        this.descricao = descricao;
        setValor(valor);
        this.data = LocalDate.now();
        this.forma = forma;
    }

    public Long getId() { return id; }
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
    public BankAccount getBankAccount() { return bankAccount; }
    public void setBankAccount(BankAccount bankAccount) { this.bankAccount = bankAccount; }
    public TransactionCategory getTransactionCategory() { return transactionCategory; }
    public void setTransactionCategory(TransactionCategory transactionCategory) { this.transactionCategory = transactionCategory; }
    public TransactionType getTipo() { return tipo; }
    public void setTipo(TransactionType tipo) { this.tipo = tipo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) {
        if (valor == null) {
            throw new IllegalArgumentException("valor nao pode ser nulo");
        }
        this.valor = valor;
    }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public TransactionForm getForma() { return forma; }
    public void setForma(TransactionForm forma) { this.forma = forma; }
}
