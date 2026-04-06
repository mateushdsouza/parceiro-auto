package br.com.parceiroauto.repository;

import br.com.parceiroauto.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

public class UserRepository {
    private EntityManager em;

    public UserRepository(EntityManager em) {this.em = em; }

    public void save (User user) {
        em.getTransaction().begin();
        try {
            em.persist(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public User findById(Long id) {
        return em.find(User.class, id);
    }

    public User findByLogin(String login) {
        try {
            return em.createQuery(
                    "SELECT u FROM User u WHERE u.login = :login", User.class)
                    .setParameter("login", login)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void update(User user) {
        em.getTransaction().begin();
        try {
            em.merge(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public void delete(User user) {
        em.getTransaction().begin();
        try {
            User managedUser = em.find(User.class, user.getId());

            if (managedUser != null) {
                em.remove(managedUser);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}


