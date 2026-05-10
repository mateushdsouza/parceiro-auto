package br.com.parceiroauto;

import br.com.parceiroauto.confg.FlyWayconfg;
import br.com.parceiroauto.confg.JPAUtil;
import br.com.parceiroauto.controller.LoginCompanyController;
import br.com.parceiroauto.controller.LoginController;
import br.com.parceiroauto.controller.RegisterCompanyController;
import br.com.parceiroauto.controller.RegisterController;
import br.com.parceiroauto.repository.CompanyRepository;
import br.com.parceiroauto.repository.UserCompanyRepository;
import br.com.parceiroauto.repository.UserRepository;
import br.com.parceiroauto.service.CompanyService;
import br.com.parceiroauto.service.UserCompanyService;
import br.com.parceiroauto.service.UserService;
import br.com.parceiroauto.view.swing.LoginFrame;
import jakarta.persistence.EntityManager;

public class Main {
    public static void main(String[] args) {

        FlyWayconfg.migrate();

        EntityManager em = JPAUtil.getEntityManager();

        UserRepository repository = new UserRepository(em);

        UserService service = new UserService(repository);

        LoginController loginController = new LoginController(service);

        RegisterController registerController = new RegisterController(service);

        UserCompanyRepository userCompanyRepository = new UserCompanyRepository(em);

        UserCompanyService userCompanyService = new UserCompanyService(userCompanyRepository);

        LoginCompanyController loginCompanyController = new LoginCompanyController(userCompanyService);

        CompanyRepository companyRepository = new CompanyRepository(em);

        CompanyService companyService = new CompanyService(companyRepository);

        RegisterCompanyController registerCompanyController =
                new RegisterCompanyController(companyService, userCompanyService);

        new LoginFrame(loginController, registerController, loginCompanyController, registerCompanyController);


    }
}
