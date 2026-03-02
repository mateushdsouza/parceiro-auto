package br.com.parceiroauto.service;

import br.com.parceiroauto.model.Empresa;
import br.com.parceiroauto.model.Usuario;
import br.com.parceiroauto.util.ValidadorCNPJ;

public class EmpresaService {
    public boolean cadastrarEmpresa(Usuario usuario, String nome, String cnpj) {

        if (!ValidadorCNPJ.isCNPJ(cnpj)) {
            System.out.println("Erro: CNPJ inválido!");
            return false;
        }

        Empresa novaEmpresa = new Empresa(nome, cnpj);

        usuario.adicionarEmpresa(novaEmpresa);

        return true;
    }
}