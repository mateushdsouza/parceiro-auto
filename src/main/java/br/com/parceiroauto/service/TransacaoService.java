package br.com.parceiroauto.service;

import br.com.parceiroauto.model.Empresa;
import br.com.parceiroauto.model.FormaDeTransacao;
import br.com.parceiroauto.model.Transacao;
import br.com.parceiroauto.model.TipoTransacao;

public class TransacaoService {

    public void registrarEntrada(Empresa empresa, double valor, String descricao, FormaDeTransacao forma){

        TipoTransacao tipo = TipoTransacao.ENTRADA;
        Transacao transacao = new Transacao(tipo, descricao, valor, forma);
        empresa.getTransacoes().add(transacao);
    }

    public void registrarSaida(Empresa empresa, double valor, String descricao, FormaDeTransacao forma){

        TipoTransacao tipo = TipoTransacao.SAIDA;
        Transacao transacao = new Transacao(tipo, descricao, valor, forma);
        empresa.getTransacoes().add(transacao);
    }
}