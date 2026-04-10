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

        String bancoNormalizado = validarBanco(banco);
        String agenciaNormalizada = validarAgencia(agencia);
        String numeroContaNormalizado = validarNumeroConta(numeroConta);
        String tipoContaNormalizado = validarTipoConta(tipoConta);

        BankAccount existingAccount = bankAccountRepository.findByCompanyAndData(
                company,
                bancoNormalizado,
                agenciaNormalizada,
                numeroContaNormalizado
        );
        if (existingAccount != null) {
            throw new IllegalArgumentException("Ja existe uma conta com esse banco, agencia e numero para essa empresa");
        }

        BankAccount bankAccount = new BankAccount(
                bancoNormalizado,
                agenciaNormalizada,
                numeroContaNormalizado,
                tipoContaNormalizado,
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

        String bancoNormalizado = validarBanco(banco);
        String agenciaNormalizada = validarAgencia(agencia);
        String numeroContaNormalizado = validarNumeroConta(numeroConta);
        String tipoContaNormalizado = validarTipoConta(tipoConta);

        BankAccount existingAccount = bankAccountRepository.findByCompanyAndData(
                company,
                bancoNormalizado,
                agenciaNormalizada,
                numeroContaNormalizado
        );
        if (existingAccount != null && !existingAccount.getId().equals(bankAccount.getId())) {
            throw new IllegalArgumentException("Ja existe uma conta com esse banco, agencia e numero para essa empresa");
        }

        if (contaPadrao) {
            bankAccountRepository.clearDefaultByCompany(company);
        }

        bankAccount.setBanco(bancoNormalizado);
        bankAccount.setAgencia(agenciaNormalizada);
        bankAccount.setNumeroConta(numeroContaNormalizado);
        bankAccount.setTipoConta(tipoContaNormalizado);
        bankAccount.setContaPadrao(contaPadrao);
        bankAccountRepository.update(bankAccount);
        return bankAccount;
    }

    private String validarBanco(String banco) {
        if (banco == null || banco.isBlank()) {
            throw new IllegalArgumentException("Banco nao pode ser vazio");
        }

        return banco.trim();
    }

    private String validarTipoConta(String tipoConta) {
        if (tipoConta == null || tipoConta.isBlank()) {
            throw new IllegalArgumentException("Tipo da conta nao pode ser vazio");
        }

        return tipoConta.trim();
    }

    private String validarAgencia(String agencia) {
        if (agencia == null || agencia.isBlank()) {
            throw new IllegalArgumentException("Agencia nao pode ser vazia");
        }

        String agenciaNormalizada = agencia.trim();
        if (!agenciaNormalizada.matches("\\d{4}")) {
            throw new IllegalArgumentException("Agencia deve ter exatamente 4 digitos");
        }

        return agenciaNormalizada;
    }

    private String validarNumeroConta(String numeroConta) {
        if (numeroConta == null || numeroConta.isBlank()) {
            throw new IllegalArgumentException("Numero da conta nao pode ser vazio");
        }

        String numeroContaNormalizado = numeroConta.trim();
        if (!numeroContaNormalizado.matches("\\d{4,13}")) {
            throw new IllegalArgumentException("Numero da conta deve ter entre 4 e 13 digitos");
        }

        return numeroContaNormalizado;
    }
}
