package br.com.parceiroauto.service;

import br.com.parceiroauto.entity.Empresa;
import br.com.parceiroauto.entity.TransactionForm;
import br.com.parceiroauto.entity.Transaction;
import br.com.parceiroauto.entity.TransactionType;

public class TransacaoService {

    public void registrarEntrada(Empresa empresa, double valor, String descricao, TransactionForm forma){

        TransactionType tipo = TransactionType.ENTRADA;
        Transaction transaction = new Transaction(tipo, descricao, valor, forma);
        empresa.getTransacoes().add(transaction);
    }

    public void registrarSaida(Empresa empresa, double valor, String descricao, TransactionForm forma){

        TransactionType tipo = TransactionType.SAIDA;
        Transaction transaction = new Transaction(tipo, descricao, valor, forma);
        empresa.getTransacoes().add(transaction);
    }
}