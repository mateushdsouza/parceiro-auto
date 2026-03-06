package br.com.parceiroauto.model;

import java.util.Date;

public class Transacao {
    private TipoTransacao tipo;
    private String descricao;
    private double valor;
    private Date data;
    private FormaDeTransacao forma;

    public Transacao(TipoTransacao tipo, String descricao, double valor, FormaDeTransacao forma) {
        this.tipo = tipo;
        this.descricao = descricao;
        this.valor = valor;
        this.data = new Date();
        this.forma = forma;
    }

    public TipoTransacao getTipo() { return tipo; }
    public String getDescricao() { return descricao; }
    public double getValor() { return valor; }
    public Date getData() { return data; }
    public FormaDeTransacao getForma() {return forma;}
}
