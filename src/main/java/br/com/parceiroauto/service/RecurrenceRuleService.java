package br.com.parceiroauto.service;

import br.com.parceiroauto.entity.FrequencyType;
import br.com.parceiroauto.entity.RecurrenceRule;
import br.com.parceiroauto.entity.Transaction;
import br.com.parceiroauto.repository.RecurrenceRuleRepository;

import java.time.LocalDate;

public class RecurrenceRuleService {

    private final RecurrenceRuleRepository recurrenceRuleRepository;

    public RecurrenceRuleService(RecurrenceRuleRepository recurrenceRuleRepository) {
        this.recurrenceRuleRepository = recurrenceRuleRepository;
    }

    public RecurrenceRule createRecurrenceRule(
            Transaction transaction,
            FrequencyType frequencia,
            int diaExecucao,
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

        RecurrenceRule recurrenceRule = new RecurrenceRule(transaction, frequencia, diaExecucao, dataInicio, dataFim);
        recurrenceRuleRepository.save(recurrenceRule);
        return recurrenceRule;
    }

    public void replaceRecurrenceRule(
            Transaction transaction,
            FrequencyType frequencia,
            Integer diaExecucao,
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

        if (frequencia != null && diaExecucao != null) {
            createRecurrenceRule(transaction, frequencia, diaExecucao, dataInicio, dataFim);
        }
    }
}
