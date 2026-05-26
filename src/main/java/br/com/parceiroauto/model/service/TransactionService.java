package br.com.parceiroauto.model.service;

import br.com.parceiroauto.model.entity.BankAccount;
import br.com.parceiroauto.model.entity.Company;
import br.com.parceiroauto.model.entity.Transaction;
import br.com.parceiroauto.model.entity.TransactionCategory;
import br.com.parceiroauto.model.entity.TransactionForm;
import br.com.parceiroauto.model.entity.TransactionType;
import br.com.parceiroauto.model.repository.BankAccountRepository;
import br.com.parceiroauto.model.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;

    public TransactionService(TransactionRepository transactionRepository, BankAccountRepository bankAccountRepository) {
        this.transactionRepository = transactionRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    public Transaction createTransaction(
            Company company,
            BankAccount bankAccount,
            TransactionCategory transactionCategory,
            TransactionType tipo,
            String descricao,
            BigDecimal valor,
            TransactionForm forma
    ) {
        return createTransaction(company, bankAccount, transactionCategory, tipo, descricao, valor, forma, LocalDate.now());
    }

    public Transaction createTransaction(
            Company company,
            BankAccount bankAccount,
            TransactionCategory transactionCategory,
            TransactionType tipo,
            String descricao,
            BigDecimal valor,
            TransactionForm forma,
            LocalDate data
    ) {
        if (company == null) {
            throw new IllegalArgumentException("Empresa nao pode ser nula");
        }

        if (bankAccount == null) {
            throw new IllegalArgumentException("Conta bancaria nao pode ser nula");
        }

        if (transactionCategory == null) {
            throw new IllegalArgumentException("Categoria nao pode ser nula");
        }

        if (tipo == null) {
            throw new IllegalArgumentException("Tipo da movimentacao nao pode ser nulo");
        }

        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Descricao nao pode ser vazia");
        }

        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero");
        }

        if (forma == null) {
            throw new IllegalArgumentException("Forma nao pode ser nula");
        }

        if (data == null) {
            throw new IllegalArgumentException("Data nao pode ser nula");
        }

        if (transactionCategory.getTipo() != tipo) {
            throw new IllegalArgumentException("A categoria escolhida nao corresponde ao tipo da movimentacao");
        }

        BigDecimal saldoAtual = bankAccount.getSaldo() == null ? BigDecimal.ZERO : bankAccount.getSaldo();
        BigDecimal novoSaldo;

        if (tipo == TransactionType.ENTRADA) {
            novoSaldo = saldoAtual.add(valor);
        } else {
            novoSaldo = saldoAtual.subtract(valor);
        }

        Transaction transaction = new Transaction(
                company,
                bankAccount,
                transactionCategory,
                tipo,
                descricao.trim(),
                valor,
                forma
        );
        transaction.setData(data);

        transactionRepository.save(transaction);
        bankAccount.setSaldo(novoSaldo);
        bankAccountRepository.update(bankAccount);
        return transaction;
    }

    public List<Transaction> findByBankAccount(BankAccount bankAccount) {
        if (bankAccount == null) {
            throw new IllegalArgumentException("Conta bancaria nao pode ser nula");
        }

        return transactionRepository.findByBankAccount(bankAccount);
    }

    public List<Transaction> findByCompany(Company company) {
        if (company == null) {
            throw new IllegalArgumentException("Empresa nao pode ser nula");
        }

        return transactionRepository.findByCompany(company);
    }

    public List<Transaction> findLastByCompany(Company company, int limit) {
        return findByCompany(company)
                .stream()
                .limit(limit)
                .toList();
    }

    public Transaction updateTransaction(
            Transaction transaction,
            BankAccount newBankAccount,
            TransactionCategory newCategory,
            TransactionType newTipo,
            String newDescricao,
            BigDecimal newValor,
            TransactionForm newForma
    ) {
        return updateTransaction(
                transaction,
                newBankAccount,
                newCategory,
                newTipo,
                newDescricao,
                newValor,
                newForma,
                transaction == null ? null : transaction.getData()
        );
    }

    public Transaction updateTransaction(
            Transaction transaction,
            BankAccount newBankAccount,
            TransactionCategory newCategory,
            TransactionType newTipo,
            String newDescricao,
            BigDecimal newValor,
            TransactionForm newForma,
            LocalDate newData
    ) {
        if (transaction == null) {
            throw new IllegalArgumentException("Movimentacao nao pode ser nula");
        }

        if (newBankAccount == null) {
            throw new IllegalArgumentException("Conta bancaria nao pode ser nula");
        }

        if (newCategory == null) {
            throw new IllegalArgumentException("Categoria nao pode ser nula");
        }

        if (newTipo == null) {
            throw new IllegalArgumentException("Tipo da movimentacao nao pode ser nulo");
        }

        if (newDescricao == null || newDescricao.isBlank()) {
            throw new IllegalArgumentException("Descricao nao pode ser vazia");
        }

        if (newValor == null || newValor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero");
        }

        if (newForma == null) {
            throw new IllegalArgumentException("Forma nao pode ser nula");
        }

        if (newData == null) {
            throw new IllegalArgumentException("Data nao pode ser nula");
        }

        if (newCategory.getTipo() != newTipo) {
            throw new IllegalArgumentException("A categoria escolhida nao corresponde ao tipo da movimentacao");
        }

        reverseTransactionEffect(transaction);
        applyTransactionEffect(newBankAccount, newTipo, newValor);

        transaction.setBankAccount(newBankAccount);
        transaction.setTransactionCategory(newCategory);
        transaction.setTipo(newTipo);
        transaction.setDescricao(newDescricao.trim());
        transaction.setValor(newValor);
        transaction.setForma(newForma);
        transaction.setData(newData);
        transactionRepository.update(transaction);
        return transaction;
    }

    public void deleteTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Movimentacao nao pode ser nula");
        }

        reverseTransactionEffect(transaction);
        transactionRepository.delete(transaction);
    }

    private void reverseTransactionEffect(Transaction transaction) {
        BankAccount bankAccount = transaction.getBankAccount();
        BigDecimal saldoAtual = bankAccount.getSaldo() == null ? BigDecimal.ZERO : bankAccount.getSaldo();

        if (transaction.getTipo() == TransactionType.ENTRADA) {
            bankAccount.setSaldo(saldoAtual.subtract(transaction.getValor()));
        } else {
            bankAccount.setSaldo(saldoAtual.add(transaction.getValor()));
        }

        bankAccountRepository.update(bankAccount);
    }

    private void applyTransactionEffect(BankAccount bankAccount, TransactionType tipo, BigDecimal valor) {
        BigDecimal saldoAtual = bankAccount.getSaldo() == null ? BigDecimal.ZERO : bankAccount.getSaldo();

        if (tipo == TransactionType.ENTRADA) {
            bankAccount.setSaldo(saldoAtual.add(valor));
        } else {
            bankAccount.setSaldo(saldoAtual.subtract(valor));
        }

        bankAccountRepository.update(bankAccount);
    }
}
