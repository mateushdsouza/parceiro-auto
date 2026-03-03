package br.com.parceiroauto.model;

import java.util.ArrayList;
import java.util.List;

public class Usuario {
    private String user;
    private String senha;
    private List<Empresa> empresas = new ArrayList<>();

    public void adicionarEmpresa(Empresa empresa) {
        this.empresas.add(empresa);
    }

    public void removerEmpresa(Empresa empresa) {
        this.empresas.remove(empresa);
    }

    public List<Empresa> getEmpresas() {
        return empresas;
    }

    public Usuario(String user, String senha) {
        this.user = user;
        this.senha = senha;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
