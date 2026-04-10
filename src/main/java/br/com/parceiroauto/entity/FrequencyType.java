package br.com.parceiroauto.entity;

public enum FrequencyType {
    DAILY("Diaria"),
    WEEKLY("Semanal"),
    MONTHLY("Mensal"),
    YEARLY("Anual");

    private final String descricao;

    FrequencyType(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
