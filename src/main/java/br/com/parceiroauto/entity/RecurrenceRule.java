package br.com.parceiroauto.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "recurrence_rule")
public class RecurrenceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "fk_id_transaction", nullable = false, unique = true)
    private Transaction transaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FrequencyType frequencia;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column
    private LocalDate dataFim;

    @Column(nullable = false)
    private LocalDate ultimaExecucao;

    public RecurrenceRule() {
    }

    public RecurrenceRule(Transaction transaction, FrequencyType frequencia,
                          LocalDate dataInicio, LocalDate dataFim) {
        setTransaction(transaction);
        setFrequencia(frequencia);
        setDataInicio(dataInicio);
        setDataFim(dataFim);
        setUltimaExecucao(dataInicio);
    }

    public Long getId() {
        return id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("transaction nao pode ser nula");
        }
        this.transaction = transaction;
    }

    public FrequencyType getFrequencia() {
        return frequencia;
    }

    public void setFrequencia(FrequencyType frequencia) {
        if (frequencia == null) {
            throw new IllegalArgumentException("frequencia nao pode ser nula");
        }
        this.frequencia = frequencia;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        if (dataInicio == null) {
            throw new IllegalArgumentException("dataInicio nao pode ser nula");
        }
        if (this.dataFim != null && dataInicio.isAfter(this.dataFim)) {
            throw new IllegalArgumentException("dataInicio nao pode ser maior que dataFim");
        }
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        if (dataFim != null && this.dataInicio != null && dataFim.isBefore(this.dataInicio)) {
            throw new IllegalArgumentException("dataFim nao pode ser menor que dataInicio");
        }
        this.dataFim = dataFim;
    }

    public LocalDate getUltimaExecucao() {
        return ultimaExecucao;
    }

    public void setUltimaExecucao(LocalDate ultimaExecucao) {
        if (ultimaExecucao == null) {
            throw new IllegalArgumentException("ultimaExecucao nao pode ser nula");
        }
        if (this.dataInicio != null && ultimaExecucao.isBefore(this.dataInicio)) {
            throw new IllegalArgumentException("ultimaExecucao nao pode ser menor que dataInicio");
        }
        this.ultimaExecucao = ultimaExecucao;
    }
}
