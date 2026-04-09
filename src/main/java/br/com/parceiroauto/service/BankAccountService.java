package br.com.parceiroauto.service;

import br.com.parceiroauto.entity.BankAccount;
import br.com.parceiroauto.entity.Company;
import br.com.parceiroauto.repository.BankAccountRepository;

import java.util.List;

public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;

    public BankAccountService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    public BankAccount createBankAccount(
            Company company,
            String banco,
            String agencia,
            String numeroConta,
            String tipoConta,
            boolean contaPadrao
    ) {
        if (company == null) {
            throw new IllegalArgumentException("Empresa nao pode ser nula");
        }

        if (banco == null || banco.isBlank()) {
            throw new IllegalArgumentException("Banco nao pode ser vazio");
        }

        if (agencia == null || agencia.isBlank()) {
            throw new IllegalArgumentException("Agencia nao pode ser vazia");
        }

        if (numeroConta == null || numeroConta.isBlank()) {
            throw new IllegalArgumentException("Numero da conta nao pode ser vazio");
        }

        if (tipoConta == null || tipoConta.isBlank()) {
            throw new IllegalArgumentException("Tipo da conta nao pode ser vazio");
        }

        BankAccount existingAccount = bankAccountRepository.findByCompanyAndData(company, banco, agencia, numeroConta);
        if (existingAccount != null) {
            throw new IllegalArgumentException("Ja existe uma conta com esse banco, agencia e numero para essa empresa");
        }

        BankAccount bankAccount = new BankAccount(
                banco.trim(),
                agencia.trim(),
                numeroConta.trim(),
                tipoConta.trim(),
                contaPadrao,
                company
        );

        if (contaPadrao) {
            bankAccountRepository.clearDefaultByCompany(company);
        }

        bankAccountRepository.save(bankAccount);
        return bankAccount;
    }

    public List<BankAccount> findByCompany(Company company) {
        if (company == null) {
            throw new IllegalArgumentException("Empresa nao pode ser nula");
        }

        return bankAccountRepository.findByCompany(company);
    }

    public BankAccount findDefaultByCompany(Company company) {
        if (company == null) {
            throw new IllegalArgumentException("Empresa nao pode ser nula");
        }

        return bankAccountRepository.findDefaultByCompany(company);
    }

    public void defineDefaultAccount(Company company, BankAccount bankAccount) {
        if (company == null) {
            throw new IllegalArgumentException("Empresa nao pode ser nula");
        }

        if (bankAccount == null) {
            throw new IllegalArgumentException("Conta bancaria nao pode ser nula");
        }

        if (!bankAccount.getCompany().getId().equals(company.getId())) {
            throw new IllegalArgumentException("A conta bancaria nao pertence a essa empresa");
        }

        bankAccountRepository.clearDefaultByCompany(company);
        bankAccount.setContaPadrao(true);
        bankAccountRepository.update(bankAccount);
    }

    public void deleteBankAccount(Company company, BankAccount bankAccount) {
        if (company == null) {
            throw new IllegalArgumentException("Empresa nao pode ser nula");
        }

        if (bankAccount == null) {
            throw new IllegalArgumentException("Conta bancaria nao pode ser nula");
        }

        if (!bankAccount.getCompany().getId().equals(company.getId())) {
            throw new IllegalArgumentException("A conta bancaria nao pertence a essa empresa");
        }

        bankAccountRepository.delete(bankAccount);
    }

    public BankAccount updateBankAccount(
            Company company,
            BankAccount bankAccount,
            String banco,
            String agencia,
            String numeroConta,
            String tipoConta,
            boolean contaPadrao
    ) {
        if (company == null) {
            throw new IllegalArgumentException("Empresa nao pode ser nula");
        }

        if (bankAccount == null) {
            throw new IllegalArgumentException("Conta bancaria nao pode ser nula");
        }

        if (banco == null || banco.isBlank()) {
            throw new IllegalArgumentException("Banco nao pode ser vazio");
        }

        if (agencia == null || agencia.isBlank()) {
            throw new IllegalArgumentException("Agencia nao pode ser vazia");
        }

        if (numeroConta == null || numeroConta.isBlank()) {
            throw new IllegalArgumentException("Numero da conta nao pode ser vazio");
        }

        if (tipoConta == null || tipoConta.isBlank()) {
            throw new IllegalArgumentException("Tipo da conta nao pode ser vazio");
        }

        BankAccount existingAccount = bankAccountRepository.findByCompanyAndData(company, banco, agencia, numeroConta);
        if (existingAccount != null && !existingAccount.getId().equals(bankAccount.getId())) {
            throw new IllegalArgumentException("Ja existe uma conta com esse banco, agencia e numero para essa empresa");
        }

        if (contaPadrao) {
            bankAccountRepository.clearDefaultByCompany(company);
        }

        bankAccount.setBanco(banco.trim());
        bankAccount.setAgencia(agencia.trim());
        bankAccount.setNumeroConta(numeroConta.trim());
        bankAccount.setTipoConta(tipoConta.trim());
        bankAccount.setContaPadrao(contaPadrao);
        bankAccountRepository.update(bankAccount);
        return bankAccount;
    }
}
