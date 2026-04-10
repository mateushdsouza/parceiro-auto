package br.com.parceiroauto.repository;

import br.com.parceiroauto.entity.RecurrenceRule;
import br.com.parceiroauto.entity.Transaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;

public class RecurrenceRuleRepository {

    private final EntityManager em;

    public RecurrenceRuleRepository(EntityManager em) {
        this.em = em;
    }

    public void save(RecurrenceRule recurrenceRule) {
        em.getTransaction().begin();
        try {
            em.persist(recurrenceRule);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public RecurrenceRule findByTransaction(Transaction transaction) {
        try {
            return em.createQuery(
                            "SELECT rr FROM RecurrenceRule rr WHERE rr.transaction = :transaction",
                            RecurrenceRule.class
                    )
                    .setParameter("transaction", transaction)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<RecurrenceRule> findAll() {
        em.clear();
        return em.createQuery(
                        "SELECT rr FROM RecurrenceRule rr ORDER BY rr.id",
                        RecurrenceRule.class
                )
                .getResultList();
    }

    public void update(RecurrenceRule recurrenceRule) {
        em.getTransaction().begin();
        try {
            em.merge(recurrenceRule);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public void delete(RecurrenceRule recurrenceRule) {
        em.getTransaction().begin();
        try {
            RecurrenceRule managedRule = em.find(RecurrenceRule.class, recurrenceRule.getId());

            if (managedRule != null) {
                em.remove(managedRule);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
