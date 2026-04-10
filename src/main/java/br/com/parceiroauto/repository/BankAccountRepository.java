package br.com.parceiroauto.repository;

import br.com.parceiroauto.entity.BankAccount;
import br.com.parceiroauto.entity.Company;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;

public class BankAccountRepository {

    private final EntityManager em;

    public BankAccountRepository(EntityManager em) {
        this.em = em;
    }

    public void save(BankAccount bankAccount) {
        em.getTransaction().begin();
        try {
            em.persist(bankAccount);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public List<BankAccount> findByCompany(Company company) {
        em.clear();
        return em.createQuery(
                        "SELECT ba FROM BankAccount ba WHERE ba.company = :company ORDER BY ba.id",
                        BankAccount.class
                )
                .setParameter("company", company)
                .getResultList();
    }

    public BankAccount findById(Long id) {
        return em.find(BankAccount.class, id);
    }

    public BankAccount findByCompanyAndData(Company company, String banco, String agencia, String numeroConta) {
        try {
            return em.createQuery(
                            "SELECT ba FROM BankAccount ba WHERE ba.company = :company AND ba.banco = :banco " +
                                    "AND ba.agencia = :agencia AND ba.numeroConta = :numeroConta",
                            BankAccount.class
                    )
                    .setParameter("company", company)
                    .setParameter("banco", banco)
                    .setParameter("agencia", agencia)
                    .setParameter("numeroConta", numeroConta)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public BankAccount findDefaultByCompany(Company company) {
        em.clear();
        try {
            return em.createQuery(
                            "SELECT ba FROM BankAccount ba WHERE ba.company = :company AND ba.contaPadrao = true",
                            BankAccount.class
                    )
                    .setParameter("company", company)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void update(BankAccount bankAccount) {
        em.getTransaction().begin();
        try {
            em.merge(bankAccount);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public void clearDefaultByCompany(Company company) {
        em.getTransaction().begin();
        try {
            em.createQuery("UPDATE BankAccount ba SET ba.contaPadrao = false WHERE ba.company = :company")
                    .setParameter("company", company)
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public void delete(BankAccount bankAccount) {
        em.getTransaction().begin();
        try {
            BankAccount managedBankAccount = em.find(BankAccount.class, bankAccount.getId());

            if (managedBankAccount != null) {
                em.remove(managedBankAccount);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public BankAccount findManagedById(Long id) {
        return em.find(BankAccount.class, id);
    }
}
