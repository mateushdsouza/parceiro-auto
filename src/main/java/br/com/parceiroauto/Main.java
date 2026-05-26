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
import br.com.parceiroauto.model.repository.BankAccountRepository;
import br.com.parceiroauto.model.repository.CompanyRepository;
import br.com.parceiroauto.model.repository.RecurrenceRuleRepository;
import br.com.parceiroauto.model.repository.TransactionCategoryRepository;
import br.com.parceiroauto.model.repository.TransactionRepository;
import br.com.parceiroauto.model.repository.UserCompanyRepository;
import br.com.parceiroauto.model.repository.UserRepository;
import br.com.parceiroauto.model.service.BankAccountService;
import br.com.parceiroauto.model.service.CompanyService;
import br.com.parceiroauto.model.service.RecurrenceRuleService;
import br.com.parceiroauto.model.service.TransactionCategoryService;
import br.com.parceiroauto.model.service.TransactionService;
import br.com.parceiroauto.model.service.UserCompanyService;
import br.com.parceiroauto.model.service.UserService;
import br.com.parceiroauto.view.swing.LoginFrame;
import br.com.parceiroauto.view.swing.SwingDialogs;
import jakarta.persistence.EntityManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final LocalTime RECURRENCE_CRON_TIME = LocalTime.of(0, 5);

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

        ScheduledExecutorService recurrenceCron = startDailyRecurrenceCron();
        Runtime.getRuntime().addShutdownHook(new Thread(recurrenceCron::shutdownNow));

        new LoginFrame(context);

    }

    private static ScheduledExecutorService startDailyRecurrenceCron() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "recurrence-rule-cron");
            thread.setDaemon(true);
            return thread;
        });

        scheduler.scheduleAtFixedRate(
                Main::processPendingRecurrences,
                secondsUntilNextExecution(),
                TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS
        );

        processPendingRecurrences();
        return scheduler;
    }

    private static long secondsUntilNextExecution() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextExecution = now.toLocalDate().atTime(RECURRENCE_CRON_TIME);

        if (!nextExecution.isAfter(now)) {
            nextExecution = nextExecution.plusDays(1);
        }

        return Duration.between(now, nextExecution).toSeconds();
    }

    private static void processPendingRecurrences() {
        EntityManager cronEntityManager = JPAUtil.getEntityManager();

        try {
            BankAccountRepository bankAccountRepository = new BankAccountRepository(cronEntityManager);
            TransactionRepository transactionRepository = new TransactionRepository(cronEntityManager);
            TransactionService transactionService = new TransactionService(transactionRepository, bankAccountRepository);
            RecurrenceRuleRepository recurrenceRuleRepository = new RecurrenceRuleRepository(cronEntityManager);
            RecurrenceRuleService recurrenceRuleService =
                    new RecurrenceRuleService(recurrenceRuleRepository, transactionService);

            recurrenceRuleService.processPendingRecurrenceRules();
        } catch (Exception ex) {
            System.err.println("Falha ao processar movimentacoes recorrentes: " + ex.getMessage());
        } finally {
            cronEntityManager.close();
        }
    }
}
