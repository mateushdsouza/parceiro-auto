package br.com.parceiroauto.service;

import br.com.parceiroauto.entity.Company;
import br.com.parceiroauto.entity.User;
import br.com.parceiroauto.entity.UserCompany;
import br.com.parceiroauto.entity.UserCompanyRole;
import br.com.parceiroauto.repository.UserCompanyRepository;

import java.util.List;

public class UserCompanyService {
    private final UserCompanyRepository userCompanyRepository;

    public UserCompanyService(UserCompanyRepository userCompanyRepository) {
        this.userCompanyRepository = userCompanyRepository;
    }

    public UserCompany linkUserToCompany(User user, Company company, UserCompanyRole role) {
        if (user == null) {
            throw new IllegalArgumentException("Usuario nao pode ser nulo");
        }

        if (company == null) {
            throw new IllegalArgumentException("Empresa nao pode ser nula");
        }

        if (role == null) {
            throw new IllegalArgumentException("Papel nao pode ser nulo");
        }

        UserCompany existingLink = userCompanyRepository.findByUserAndCompany(user, company);
        if (existingLink != null) {
            throw new IllegalArgumentException("Esse usuario ja esta vinculado a essa empresa");
        }

        UserCompany userCompany = new UserCompany(user, company, role);
        userCompanyRepository.save(userCompany);
        return userCompany;
    }

    public UserCompany findById(Long id) {
        return userCompanyRepository.findById(id);
    }

    public UserCompany findByUserAndCompany(User user, Company company) {
        return userCompanyRepository.findByUserAndCompany(user, company);
    }

    public List<UserCompany> findByUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Usuario nao pode ser nulo");
        }

        return userCompanyRepository.findByUser(user);
    }

    public UserCompany updateRole(User user, Company company, UserCompanyRole newRole) {
        if (user == null) {
            throw new IllegalArgumentException("Usuario nao pode ser nulo");
        }

        if (company == null) {
            throw new IllegalArgumentException("Empresa nao pode ser nula");
        }

        if (newRole == null) {
            throw new IllegalArgumentException("Novo papel nao pode ser nulo");
        }

        UserCompany userCompany = userCompanyRepository.findByUserAndCompany(user, company);
        if (userCompany == null) {
            throw new IllegalArgumentException("Vinculo entre usuario e empresa nao encontrado");
        }

        userCompany.setRole(newRole);
        userCompanyRepository.update(userCompany);
        return userCompany;
    }

    public void removeLink(User user, Company company) {
        if (user == null) {
            throw new IllegalArgumentException("Usuario nao pode ser nulo");
        }

        if (company == null) {
            throw new IllegalArgumentException("Empresa nao pode ser nula");
        }

        UserCompany userCompany = userCompanyRepository.findByUserAndCompany(user, company);
        if (userCompany == null) {
            throw new IllegalArgumentException("Vinculo entre usuario e empresa nao encontrado");
        }

        userCompanyRepository.delete(userCompany);
    }
}
