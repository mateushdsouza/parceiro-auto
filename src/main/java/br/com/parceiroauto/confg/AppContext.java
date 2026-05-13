package br.com.parceiroauto.confg;

import br.com.parceiroauto.controller.BankAccountController;
import br.com.parceiroauto.controller.LoginCompanyController;
import br.com.parceiroauto.controller.LoginController;
import br.com.parceiroauto.controller.RegisterCompanyController;
import br.com.parceiroauto.controller.RegisterController;
import br.com.parceiroauto.service.RecurrenceRuleService;
import br.com.parceiroauto.service.TransactionService;

public class AppContext {
    private final LoginController loginController;
    private final RegisterController registerController;
    private final LoginCompanyController loginCompanyController;
    private final RegisterCompanyController registerCompanyController;
    private final BankAccountController bankAccountController;
    private final TransactionService transactionService;
    private final RecurrenceRuleService recurrenceRuleService;

    public AppContext(
            LoginController loginController,
            RegisterController registerController,
            LoginCompanyController loginCompanyController,
            RegisterCompanyController registerCompanyController,
            BankAccountController bankAccountController,
            TransactionService transactionService,
            RecurrenceRuleService recurrenceRuleService
    ) {
        this.loginController = loginController;
        this.registerController = registerController;
        this.loginCompanyController = loginCompanyController;
        this.registerCompanyController = registerCompanyController;
        this.bankAccountController = bankAccountController;
        this.transactionService = transactionService;
        this.recurrenceRuleService = recurrenceRuleService;
    }

    public LoginController getLoginController() {
        return loginController;
    }

    public RegisterController getRegisterController() {
        return registerController;
    }

    public LoginCompanyController getLoginCompanyController() {
        return loginCompanyController;
    }

    public RegisterCompanyController getRegisterCompanyController() {
        return registerCompanyController;
    }

    public BankAccountController getBankAccountController() {
        return bankAccountController;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public RecurrenceRuleService getRecurrenceRuleService() {
        return recurrenceRuleService;
    }
}
