package br.com.parceiroauto.repository;

import br.com.parceiroauto.entity.BankAccount;
import br.com.parceiroauto.entity.Transaction;
import jakarta.persistence.EntityManager;

import java.util.List;

public class TransactionRepository {

    private final EntityManager em;

    public TransactionRepository(EntityManager em) {
        this.em = em;
    }

    public void save(Transaction transaction) {
        em.getTransaction().begin();
        try {
            em.persist(transaction);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public List<Transaction> findByBankAccount(BankAccount bankAccount) {
        return em.createQuery(
                        "SELECT t FROM Transaction t WHERE t.bankAccount = :bankAccount ORDER BY t.data DESC, t.id DESC",
                        Transaction.class
                )
                .setParameter("bankAccount", bankAccount)
                .getResultList();
    }

    public List<Transaction> findByCompany(br.com.parceiroauto.entity.Company company) {
        return em.createQuery(
                        "SELECT t FROM Transaction t WHERE t.company = :company ORDER BY t.data DESC, t.id DESC",
                        Transaction.class
                )
                .setParameter("company", company)
                .getResultList();
    }

    public void update(Transaction transaction) {
        em.getTransaction().begin();
        try {
            em.merge(transaction);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public void delete(Transaction transaction) {
        em.getTransaction().begin();
        try {
            Transaction managedTransaction = em.find(Transaction.class, transaction.getId());

            if (managedTransaction != null) {
                em.remove(managedTransaction);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
