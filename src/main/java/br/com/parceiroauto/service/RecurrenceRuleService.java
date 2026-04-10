package br.com.parceiroauto.service;

import br.com.parceiroauto.entity.FrequencyType;
import br.com.parceiroauto.entity.RecurrenceRule;
import br.com.parceiroauto.entity.Transaction;
import br.com.parceiroauto.repository.RecurrenceRuleRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class RecurrenceRuleService {

    private final RecurrenceRuleRepository recurrenceRuleRepository;
    private final TransactionService transactionService;

    public RecurrenceRuleService(
            RecurrenceRuleRepository recurrenceRuleRepository,
            TransactionService transactionService
    ) {
        this.recurrenceRuleRepository = recurrenceRuleRepository;
        this.transactionService = transactionService;
    }

    public RecurrenceRule createRecurrenceRule(
            Transaction transaction,
            FrequencyType frequencia,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        if (transaction == null) {
            throw new IllegalArgumentException("Movimentacao nao pode ser nula");
        }

        if (frequencia == null) {
            throw new IllegalArgumentException("Frequencia nao pode ser nula");
        }

        if (dataInicio == null) {
            throw new IllegalArgumentException("Data de inicio nao pode ser nula");
        }

        RecurrenceRule recurrenceRule = new RecurrenceRule(transaction, frequencia, dataInicio, dataFim);
        recurrenceRuleRepository.save(recurrenceRule);
        return recurrenceRule;
    }

    public void replaceRecurrenceRule(
            Transaction transaction,
            FrequencyType frequencia,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        if (transaction == null) {
            throw new IllegalArgumentException("Movimentacao nao pode ser nula");
        }

        RecurrenceRule existingRule = recurrenceRuleRepository.findByTransaction(transaction);
        if (existingRule != null) {
            recurrenceRuleRepository.delete(existingRule);
        }

        if (frequencia != null) {
            createRecurrenceRule(transaction, frequencia, dataInicio, dataFim);
        }
    }

    public void processPendingRecurrenceRules() {
        List<RecurrenceRule> regras = recurrenceRuleRepository.findAll();
        LocalDate hoje = LocalDate.now();

        for (RecurrenceRule regra : regras) {
            processPendingRecurrenceRule(regra, hoje);
        }
    }

    private void processPendingRecurrenceRule(RecurrenceRule regra, LocalDate hoje) {
        if (regra == null) {
            return;
        }

        LocalDate dataFim = regra.getDataFim();
        LocalDate proximaExecucao = calcularProximaExecucao(regra);

        while (proximaExecucao != null
                && !proximaExecucao.isAfter(hoje)
                && (dataFim == null || !proximaExecucao.isAfter(dataFim))) {
            criarMovimentacaoRecorrente(regra, proximaExecucao);
            regra.setUltimaExecucao(proximaExecucao);
            recurrenceRuleRepository.update(regra);
            proximaExecucao = calcularProximaExecucao(regra);
        }
    }

    private void criarMovimentacaoRecorrente(RecurrenceRule regra, LocalDate dataExecucao) {
        Transaction modelo = regra.getTransaction();

        transactionService.createTransaction(
                modelo.getCompany(),
                modelo.getBankAccount(),
                modelo.getTransactionCategory(),
                modelo.getTipo(),
                modelo.getDescricao(),
                modelo.getValor(),
                modelo.getForma(),
                dataExecucao
        );
    }

    private LocalDate calcularProximaExecucao(RecurrenceRule regra) {
        LocalDate ultimaExecucao = regra.getUltimaExecucao();

        return switch (regra.getFrequencia()) {
            case DAILY -> ultimaExecucao.plusDays(1);
            case WEEKLY -> ultimaExecucao.plusWeeks(1);
            case MONTHLY -> ajustarDiaDoMes(ultimaExecucao.plusMonths(1), regra.getDataInicio().getDayOfMonth());
            case YEARLY -> ajustarDiaDoMes(LocalDate.of(
                    ultimaExecucao.plusYears(1).getYear(),
                    regra.getDataInicio().getMonth(),
                    1
            ), regra.getDataInicio().getDayOfMonth());
        };
    }

    private LocalDate ajustarDiaDoMes(LocalDate dataBase, int diaExecucao) {
        YearMonth anoMes = YearMonth.from(dataBase);
        int diaValido = Math.min(diaExecucao, anoMes.lengthOfMonth());
        return anoMes.atDay(diaValido);
    }
}
