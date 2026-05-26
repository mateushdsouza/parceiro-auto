package br.com.parceiroauto.controller;

import br.com.parceiroauto.model.entity.Company;
import br.com.parceiroauto.model.entity.User;
import br.com.parceiroauto.model.entity.UserCompanyRole;
import br.com.parceiroauto.model.service.CompanyService;
import br.com.parceiroauto.model.service.UserCompanyService;

public class RegisterCompanyController {
    private final CompanyService companyService;
    private final UserCompanyService userCompanyService;

    public RegisterCompanyController(CompanyService companyService, UserCompanyService userCompanyService) {
        this.companyService = companyService;
        this.userCompanyService = userCompanyService;
    }

    public Company register(User user, String cnpj, String razaoSocial, String nomeFantasia) {
        Company company = companyService.createCompany(cnpj, razaoSocial, nomeFantasia);
        userCompanyService.linkUserToCompany(user, company, UserCompanyRole.OWNER);
        return company;
    }
}
