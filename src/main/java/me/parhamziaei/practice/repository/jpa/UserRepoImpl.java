package me.parhamziaei.practice.repository.jpa;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.entity.jpa.User;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepoImpl implements UserRepo {

    private final EntityManager em;

    @Override
    @Transactional
    public void save(User user) {
        em.persist(user);
    }

    @Override
    @Transactional
    public void update(User user) {
        em.merge(user);
    }

    @Override
    @Transactional
    public void delete(User user) {
        em.remove(user);
    }

    @Override
    public User findById(Long id) {
        return em.find(User.class, id);
    }

    @Override
    public List<User> findAll() {
        return em.createQuery("select u from User u", User.class).getResultList();
    }

    @Override
    public User findByEmail(String email) {
        try {
            return em.createQuery("select u from User u where u.email = :email", User.class).setParameter("email", email).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        return em.createQuery("select COUNT(u) from User u where u.email = :email", Long.class).setParameter("email", email).getSingleResult() > 0;
    }

}
