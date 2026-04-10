package br.com.parceiroauto.repository;

import br.com.parceiroauto.entity.Company;
import br.com.parceiroauto.entity.TransactionCategory;
import br.com.parceiroauto.entity.TransactionType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;

public class TransactionCategoryRepository {

    private final EntityManager em;

    public TransactionCategoryRepository(EntityManager em) {
        this.em = em;
    }

    public void save(TransactionCategory transactionCategory) {
        em.getTransaction().begin();
        try {
            em.persist(transactionCategory);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public List<TransactionCategory> findByCompany(Company company) {
        return em.createQuery(
                        "SELECT tc FROM TransactionCategory tc WHERE tc.company = :company ORDER BY tc.id",
                        TransactionCategory.class
                )
                .setParameter("company", company)
                .getResultList();
    }

    public List<TransactionCategory> findActiveByCompanyAndType(Company company, TransactionType tipo) {
        return em.createQuery(
                        "SELECT tc FROM TransactionCategory tc WHERE tc.company = :company AND tc.tipo = :tipo " +
                                "AND tc.active = true ORDER BY tc.id",
                        TransactionCategory.class
                )
                .setParameter("company", company)
                .setParameter("tipo", tipo)
                .getResultList();
    }

    public TransactionCategory findByCompanyAndName(Company company, String name) {
        try {
            return em.createQuery(
                            "SELECT tc FROM TransactionCategory tc WHERE tc.company = :company AND tc.name = :name",
                            TransactionCategory.class
                    )
                    .setParameter("company", company)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void update(TransactionCategory transactionCategory) {
        em.getTransaction().begin();
        try {
            em.merge(transactionCategory);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public void delete(TransactionCategory transactionCategory) {
        em.getTransaction().begin();
        try {
            TransactionCategory managedCategory = em.find(TransactionCategory.class, transactionCategory.getId());

            if (managedCategory != null) {
                em.remove(managedCategory);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
