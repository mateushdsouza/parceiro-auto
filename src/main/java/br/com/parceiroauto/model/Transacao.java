package br.com.parceiroauto.model;

import java.util.Date;

public class Transacao {
    private String tipo;
    private String descricao;
    private double valor;
    private Date data;

    public Transacao(String tipo, String descricao, double valor, Date data) {
        this.tipo = tipo;
        this.descricao = descricao;
        this.valor = valor;
        this.data = new Date();;
    }

    public String getTipo() { return tipo; }
    public String getDescricao() { return descricao; }
    public double getValor() { return valor; }
    public Date getData() { return data; }
}
