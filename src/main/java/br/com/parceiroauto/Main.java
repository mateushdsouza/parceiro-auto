package br.com.parceiroauto;

import br.com.parceiroauto.confg.AppContext;
import br.com.parceiroauto.confg.FlyWayconfg;
import br.com.parceiroauto.confg.JPAUtil;
import br.com.parceiroauto.controller.BankAccountController;
import br.com.parceiroauto.controller.EmployeeController;
import br.com.parceiroauto.controller.LoginCompanyController;
import br.com.parceiroauto.controller.LoginController;
import br.com.parceiroauto.controller.RegisterCompanyController;
import br.com.parceiroauto.controller.RegisterController;
import br.com.parceiroauto.repository.BankAccountRepository;
import br.com.parceiroauto.repository.CompanyRepository;
import br.com.parceiroauto.repository.RecurrenceRuleRepository;
import br.com.parceiroauto.repository.TransactionCategoryRepository;
import br.com.parceiroauto.repository.TransactionRepository;
import br.com.parceiroauto.repository.UserCompanyRepository;
import br.com.parceiroauto.repository.UserRepository;
import br.com.parceiroauto.service.BankAccountService;
import br.com.parceiroauto.service.CompanyService;
import br.com.parceiroauto.service.RecurrenceRuleService;
import br.com.parceiroauto.service.TransactionCategoryService;
import br.com.parceiroauto.service.TransactionService;
import br.com.parceiroauto.service.UserCompanyService;
import br.com.parceiroauto.service.UserService;
import br.com.parceiroauto.view.swing.LoginFrame;
import br.com.parceiroauto.view.swing.SwingDialogs;
import jakarta.persistence.EntityManager;

public class Main {
    public static void main(String[] args) {
        SwingDialogs.configurePortugueseDefaults();

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

        EmployeeController employeeController = new EmployeeController(service, userCompanyService);

        BankAccountRepository bankAccountRepository = new BankAccountRepository(em);
        BankAccountService bankAccountService = new BankAccountService(bankAccountRepository);
        BankAccountController bankAccountController = new BankAccountController(bankAccountService);

        TransactionCategoryRepository transactionCategoryRepository = new TransactionCategoryRepository(em);

        TransactionCategoryService transactionCategoryService =
                new TransactionCategoryService(transactionCategoryRepository);

        TransactionRepository transactionRepository = new TransactionRepository(em);

        TransactionService transactionService = new TransactionService(transactionRepository, bankAccountRepository);

        RecurrenceRuleRepository recurrenceRuleRepository = new RecurrenceRuleRepository(em);

        RecurrenceRuleService recurrenceRuleService =
                new RecurrenceRuleService(recurrenceRuleRepository, transactionService);

        AppContext context = new AppContext(
                loginController,
                registerController,
                loginCompanyController,
                registerCompanyController,
                bankAccountController,
                employeeController,
                bankAccountService,
                transactionCategoryService,
                transactionService,
                recurrenceRuleService
        );

        new LoginFrame(context);

    }
}
