package br.com.parceiroauto.repository;

import br.com.parceiroauto.entity.Company;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

public class CompanyRepository {

    private EntityManager em;

    public CompanyRepository(EntityManager em) { this.em = em; }

    public void save(Company company) {
        em.getTransaction().begin();
        try {
            em.persist(company);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public Company findById(Long id) {
        return em.find(Company.class, id);
    }

    public Company findByCnpj(String cnpj) {
        try {
            return em.createQuery(
                    "SELECT c FROM Company c WHERE c.cnpj = :cnpj", Company.class)
                    .setParameter("cnpj", cnpj)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void update(Company company) {
        em.getTransaction().begin();
        try {
            em.merge(company);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public void delete(Company company) {
        em.getTransaction().begin();
        try {
            Company managedCompany = em.find(Company.class, company.getId());

            if (managedCompany != null) {
                em.remove(managedCompany);
            }

            em.getTransaction().commit();

        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
