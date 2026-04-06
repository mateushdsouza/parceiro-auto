package br.com.parceiroauto.repository;

import br.com.parceiroauto.model.Company;
import jakarta.persistence.EntityManager;

public class CompanyRepository {

    private EntityManager em;

    public CompanyRepository(EntityManager em) { this.em = em; }

    public void save(Company cp) {
        em.getTransaction().begin();
        try {
            em.persist(cp);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public Company searchById (Long id) {return em.find(Company.class, id);}

    public void update(Company cp) {
        em.getTransaction().begin();
        try {
            em.merge(cp);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
    public void delete(Company cp) {
        em.getTransaction().begin();
        try {
            Company company = em.find(Company.class, cp.getId());

            if (company != null) {
                em.remove(company);
            }

            em.getTransaction().commit();

        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
    }
}
