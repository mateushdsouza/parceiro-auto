package br.com.parceiroauto.entity;

public enum UserCompanyRole {
    OWNER("Proprietario"),
    MANAGER("Gerente"),
    INVESTMENT_MANAGER("Gerente de Investimentos"),
    VIEWER("Visualizador");

    private final String descricao;

    UserCompanyRole(String descricao) {
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
