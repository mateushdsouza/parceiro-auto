package br.com.parceiroauto.repository;

import br.com.parceiroauto.model.UserCompany;
import jakarta.persistence.EntityManager;

public class UserCompanyRepository {

    private EntityManager em;

    public UserCompanyRepository(EntityManager em) {
        this.em = em;
    }

    public void save(UserCompany ue) {
        em.getTransaction().begin();
        try {
            em.persist(ue);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public UserCompany buscarPorId(Long id) {
        return em.find(UserCompany.class, id);
    }

    public void update(UserCompany ue) {
        em.getTransaction().begin();
        try {
            em.merge(ue);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public void delete(UserCompany ue) {
        em.getTransaction().begin();
        try {
            UserCompany userCompany = em.find(UserCompany.class, ue.getId());

            if (userCompany != null) {
                em.remove(userCompany);
            }

            em.getTransaction().commit();

        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
    }
}