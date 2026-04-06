package br.com.parceiroauto.service;

import br.com.parceiroauto.entity.Empresa;
import br.com.parceiroauto.entity.Usuario;
import br.com.parceiroauto.util.ValidadorCNPJ;
import br.com.parceiroauto.repository.UserRepository;


public class EmpresaService {
    private UserRepository repo;

    public boolean cadastrarEmpresa(Usuario usuario, String nome, String cnpj) {

        if (!ValidadorCNPJ.isCNPJ(cnpj)) {
            System.out.println("Erro: CNPJ inválido!");
            return false;
        }

        Empresa novaEmpresa = new Empresa(nome, cnpj);

        usuario.adicionarEmpresa(novaEmpresa);

        return true;
    }

    public boolean removerEmpresa(Usuario usuario, String cnpj) {

        if (!ValidadorCNPJ.isCNPJ(cnpj)) {
            System.out.println("Erro: CNPJ inválido!");
            return false;
        }

        Empresa empresaEncontrada = null;

        for (Empresa e : usuario.getEmpresas()) {
            if (e.getCnpj().equals(cnpj)) {
                empresaEncontrada = e;
                break;
            }
        }

        if (empresaEncontrada == null) {
            System.out.println("Empresa não encontrada!");
            return false;
        }

        usuario.getEmpresas().remove(empresaEncontrada);

        System.out.println("Empresa removida com sucesso!");
        return true;
    }
}