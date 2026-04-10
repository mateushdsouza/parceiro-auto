package br.com.parceiroauto.repository;

import br.com.parceiroauto.entity.Company;
import br.com.parceiroauto.entity.User;
import br.com.parceiroauto.entity.UserCompany;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;

public class UserCompanyRepository {

    private EntityManager em;

    public UserCompanyRepository(EntityManager em) {
        this.em = em;
    }

    public void save(UserCompany userCompany) {
        em.getTransaction().begin();
        try {
            em.persist(userCompany);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public UserCompany findById(Long id) {
        return em.find(UserCompany.class, id);
    }

    public UserCompany findByUserAndCompany(User user, Company company) {
        try {
            return em.createQuery(
                    "SELECT uc FROM UserCompany uc WHERE uc.user = :user AND uc.company = :company", UserCompany.class)
                    .setParameter("user", user)
                    .setParameter("company", company)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<UserCompany> findByUser(User user) {
        return em.createQuery(
                        "SELECT uc FROM UserCompany uc WHERE uc.user = :user", UserCompany.class)
                .setParameter("user", user)
                .getResultList();
    }

    public List<UserCompany> findByCompany(Company company) {
        return em.createQuery(
                        "SELECT uc FROM UserCompany uc WHERE uc.company = :company",
                        UserCompany.class
                )
                .setParameter("company", company)
                .getResultList();
    }

    public void deleteByCompany(Company company) {
        em.getTransaction().begin();
        try {
            em.createQuery("DELETE FROM UserCompany uc WHERE uc.company = :company")
                    .setParameter("company", company)
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
    public void update(UserCompany userCompany) {
        em.getTransaction().begin();
        try {
            em.merge(userCompany);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public void delete(UserCompany userCompany) {
        em.getTransaction().begin();
        try {
            UserCompany managedUserCompany = em.find(UserCompany.class, userCompany.getId());

            if (managedUserCompany != null) {
                em.remove(managedUserCompany);
            }

            em.getTransaction().commit();

        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
