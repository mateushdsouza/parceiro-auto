package br.com.parceiroauto.model;

import java.util.ArrayList;
import java.util.List;

public class Empresa {
    private String nome;
    private String cnpj;
    private List<Transacao> transacoes = new ArrayList<>();

    public Empresa(String nome, String cnpj) {
        this.nome = nome;
        this.cnpj = cnpj;
    }

    public String getNome() { return nome; }
    public String getCnpj() { return cnpj; }
    public List<Transacao> getTransacoes() { return transacoes; }
}
