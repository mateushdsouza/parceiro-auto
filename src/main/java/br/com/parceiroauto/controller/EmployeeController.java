package br.com.parceiroauto.controller;

import br.com.parceiroauto.entity.Company;
import br.com.parceiroauto.entity.User;
import br.com.parceiroauto.entity.UserCompany;
import br.com.parceiroauto.entity.UserCompanyRole;
import br.com.parceiroauto.service.UserCompanyService;
import br.com.parceiroauto.service.UserService;

import java.util.ArrayList;
import java.util.List;

public class EmployeeController {
    private final UserService userService;
    private final UserCompanyService userCompanyService;

    public EmployeeController(UserService userService, UserCompanyService userCompanyService) {
        this.userService = userService;
        this.userCompanyService = userCompanyService;
    }

    public List<UserCompany> listarFuncionarios(Company company) {
        List<UserCompany> links = userCompanyService.findByCompany(company);
        List<UserCompany> employees = new ArrayList<>();

        for (UserCompany link : links) {
            if (link.getRole() != UserCompanyRole.OWNER) {
                employees.add(link);
            }
        }

        return employees;
    }

    public List<UserCompany> filtrarFuncionarios(Company company, String loginFiltro, UserCompanyRole roleFiltro) {
        List<UserCompany> employees = listarFuncionarios(company);
        List<UserCompany> filteredEmployees = new ArrayList<>();
        String normalizedLoginFilter = loginFiltro == null ? "" : loginFiltro.trim().toLowerCase();

        for (UserCompany employee : employees) {
            boolean loginMatches = normalizedLoginFilter.isBlank()
                    || employee.getUser().getLogin().toLowerCase().contains(normalizedLoginFilter);
            boolean roleMatches = roleFiltro == null || employee.getRole() == roleFiltro;

            if (loginMatches && roleMatches) {
                filteredEmployees.add(employee);
            }
        }

        return filteredEmployees;
    }

    public UserCompany cadastrarFuncionario(
            Company company,
            String login,
            String password,
            UserCompanyRole role
    ) {
        validarRoleFuncionario(role);
        User user = userService.createUser(login, password);
        return userCompanyService.linkUserToCompany(user, company, role);
    }

    public UserCompany vincularUsuarioExistente(
            Company company,
            String login,
            UserCompanyRole role
    ) {
        validarRoleFuncionario(role);
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Login nao pode ser vazio");
        }

        User user = userService.findByLogin(login.trim());
        if (user == null) {
            throw new IllegalArgumentException("Usuario nao encontrado");
        }

        return userCompanyService.linkUserToCompany(user, company, role);
    }

    public UserCompany atualizarFuncao(
            Company company,
            UserCompany employee,
            User loggedUser,
            UserCompanyRole newRole
    ) {
        validarFuncionario(employee);
        validarRoleFuncionario(newRole);

        if (loggedUser != null && employee.getUser().getId().equals(loggedUser.getId())) {
            throw new IllegalArgumentException("Voce nao pode alterar o proprio papel");
        }

        return userCompanyService.updateRole(employee.getUser(), company, newRole);
    }

    public void removerFuncionario(Company company, UserCompany employee, User loggedUser) {
        validarFuncionario(employee);

        if (loggedUser != null && employee.getUser().getId().equals(loggedUser.getId())) {
            throw new IllegalArgumentException("Voce nao pode remover o proprio usuario da empresa");
        }

        userCompanyService.removeLink(employee.getUser(), company);
    }

    private void validarFuncionario(UserCompany employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Funcionario nao pode ser nulo");
        }

        if (employee.getRole() == UserCompanyRole.OWNER) {
            throw new IllegalArgumentException("Proprietario nao pode ser gerenciado como funcionario");
        }
    }

    private void validarRoleFuncionario(UserCompanyRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Funcao nao pode ser nula");
        }

        if (role == UserCompanyRole.OWNER) {
            throw new IllegalArgumentException("Proprietario nao pode ser atribuido pelo menu de funcionarios");
        }
    }
}
