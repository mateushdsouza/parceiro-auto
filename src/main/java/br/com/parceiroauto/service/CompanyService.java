package br.com.parceiroauto.service;

import br.com.parceiroauto.entity.Company;
import br.com.parceiroauto.repository.CompanyRepository;

public class CompanyService {
    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company createCompany(String cnpj, String razaoSocial, String nomeFantasia) {
        if (cnpj == null || cnpj.isBlank()) {
            throw new IllegalArgumentException("CNPJ nao pode ser vazio");
        }

        if (razaoSocial == null || razaoSocial.isBlank()) {
            throw new IllegalArgumentException("Razao Social nao pode ser vazia");
        }

        if (nomeFantasia == null || nomeFantasia.isBlank()) {
            throw new IllegalArgumentException("Nome Fantasia nao pode ser vazio");
        }

        Company existingCompany = companyRepository.findByCnpj(cnpj);
        if (existingCompany != null) {
            throw new IllegalArgumentException("Ja existe uma empresa com esse CNPJ");
        }

        Company company = new Company(cnpj, razaoSocial, nomeFantasia);
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

        if (cnpj == null || cnpj.isBlank()) {
            throw new IllegalArgumentException("CNPJ nao pode ser vazio");
        }

        if (razaoSocial == null || razaoSocial.isBlank()) {
            throw new IllegalArgumentException("Razao Social nao pode ser vazia");
        }

        if (nomeFantasia == null || nomeFantasia.isBlank()) {
            throw new IllegalArgumentException("Nome Fantasia nao pode ser vazio");
        }

        Company existingCompany = companyRepository.findByCnpj(cnpj);
        if (existingCompany != null && !existingCompany.getId().equals(company.getId())) {
            throw new IllegalArgumentException("Ja existe uma empresa com esse CNPJ");
        }

        company.setCnpj(cnpj);
        company.setRazaoSocial(razaoSocial);
        company.setNomeFantasia(nomeFantasia);
        companyRepository.update(company);
        return company;
    }
}
