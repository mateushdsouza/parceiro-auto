package br.com.parceiroauto.controller;

import br.com.parceiroauto.entity.BankAccount;
import br.com.parceiroauto.entity.Company;
import br.com.parceiroauto.service.BankAccountService;

import java.util.List;

public class BankAccountController {
    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    public BankAccount cadastrarContaBancaria(
            Company company,
            String banco,
            String agencia,
            String numeroConta,
            String tipoConta,
            boolean contaPadrao
    ) {
        return bankAccountService.createBankAccount(
                company,
                banco,
                agencia,
                numeroConta,
                tipoConta,
                contaPadrao
        );
    }

    public List<BankAccount> buscarContasBancarias(Company company) {
        return bankAccountService.findByCompany(company);
    }

    public List<BankAccount> buscarContaBancaria(BankAccount bankAccount) {
        return bankAccountService.findByCompany(bankAccount.getCompany());
    }

    public BankAccount buscarContaPadrao(Company company) {
        return bankAccountService.findDefaultByCompany(company);
    }

    public void definirContaPadrao(Company company, BankAccount bankAccount) {
        bankAccountService.defineDefaultAccount(company, bankAccount);
    }

    public BankAccount atualizarContaBancaria(
            Company company,
            BankAccount bankAccount,
            String banco,
            String agencia,
            String numeroConta,
            String tipoConta,
            boolean contaPadrao
    ) {
        return bankAccountService.updateBankAccount(
                company,
                bankAccount,
                banco,
                agencia,
                numeroConta,
                tipoConta,
                contaPadrao
        );
    }

    public void removerContaBancaria(Company company, BankAccount bankAccount) {
        bankAccountService.deleteBankAccount(company, bankAccount);
    }
}
