package br.com.parceiroauto.service;

import br.com.parceiroauto.entity.Company;
import br.com.parceiroauto.repository.CompanyRepository;
import br.com.parceiroauto.util.ValidadorCNPJ;

public class CompanyService {
    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company createCompany(String cnpj, String razaoSocial, String nomeFantasia) {
        String cnpjNormalizado = validarCnpj(cnpj);
        String razaoSocialNormalizada = validarTextoObrigatorio(razaoSocial, "Razao Social nao pode ser vazia");
        String nomeFantasiaNormalizado = validarTextoObrigatorio(nomeFantasia, "Nome Fantasia nao pode ser vazio");

        Company existingCompany = companyRepository.findByCnpj(cnpjNormalizado);
        if (existingCompany != null) {
            throw new IllegalArgumentException("Ja existe uma empresa com esse CNPJ");
        }

        Company company = new Company(cnpjNormalizado, razaoSocialNormalizada, nomeFantasiaNormalizado);
        companyRepository.save(company);
        return company;
    }

    public Company findById(Long id) {
        return companyRepository.findById(id);
    }

    public Company findByCnpj(String cnpj) {
        return companyRepository.findByCnpj(cnpj);
    }

    public Company updateCompany(Company company, String cnpj, String razaoSocial, String nomeFantasia) {
        if (company == null) {
            throw new IllegalArgumentException("Empresa nao pode ser nula");
        }

        String cnpjNormalizado = validarCnpj(cnpj);
        String razaoSocialNormalizada = validarTextoObrigatorio(razaoSocial, "Razao Social nao pode ser vazia");
        String nomeFantasiaNormalizado = validarTextoObrigatorio(nomeFantasia, "Nome Fantasia nao pode ser vazio");

        Company existingCompany = companyRepository.findByCnpj(cnpjNormalizado);
        if (existingCompany != null && !existingCompany.getId().equals(company.getId())) {
            throw new IllegalArgumentException("Ja existe uma empresa com esse CNPJ");
        }

        company.setCnpj(cnpjNormalizado);
        company.setRazaoSocial(razaoSocialNormalizada);
        company.setNomeFantasia(nomeFantasiaNormalizado);
        companyRepository.update(company);
        return company;
    }

    private String validarCnpj(String cnpj) {
        if (cnpj == null || cnpj.isBlank()) {
            throw new IllegalArgumentException("CNPJ nao pode ser vazio");
        }

        String cnpjNormalizado = cnpj.replaceAll("[^0-9]", "");
        if (!ValidadorCNPJ.isCNPJ(cnpjNormalizado)) {
            throw new IllegalArgumentException("CNPJ invalido");
        }

        return cnpjNormalizado;
    }

    private String validarTextoObrigatorio(String valor, String mensagemErro) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagemErro);
        }

        return valor.trim();
    }
}
