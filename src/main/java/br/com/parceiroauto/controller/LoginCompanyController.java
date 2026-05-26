package br.com.parceiroauto.controller;

import br.com.parceiroauto.model.entity.User;
import br.com.parceiroauto.model.entity.UserCompany;
import br.com.parceiroauto.model.service.UserCompanyService;

import java.util.List;

public class LoginCompanyController {
    private final UserCompanyService userCompanyService;

    public LoginCompanyController(UserCompanyService userCompanyService) {
        this.userCompanyService = userCompanyService;
    }

    public List<UserCompany> buscarEmpresasDoUsuario(User user) {
        return userCompanyService.findByUser(user);
    }
}