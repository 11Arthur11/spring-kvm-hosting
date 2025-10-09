package me.parhamziaei.practice.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.entity.User;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepo {

    private final EntityManager em;

    @Transactional
    public void save(User user) {
        em.persist(user);
    }

    @Transactional
    public void update(User user) {
        em.merge(user);
    }

    @Transactional
    public void delete(User user) {
        em.remove(user);
    }

    public User findById(Long id) {
        return em.find(User.class, id);
    }

    public List<User> findAll() {
        return em.createQuery("select u from User u", User.class).getResultList();
    }


    public User findByEmail(String email) {
        try {
            return em.createQuery("select u from User u where u.email = :email", User.class).setParameter("email", email).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean existsByEmail(String email) {
        return em.createQuery("select COUNT(u) from User u where u.email = :email", Long.class).setParameter("email", email).getSingleResult() > 0;
    }

}
