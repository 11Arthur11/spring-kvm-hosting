package me.parhamziaei.practice.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.entity.RefreshToken;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepo {

    private final EntityManager em;

    @Transactional
    public void save(RefreshToken refreshToken) {
        em.persist(refreshToken);
    }

    @Transactional
    public void update(RefreshToken refreshToken) {
        em.merge(refreshToken);
    }

    @Transactional
    public void delete(RefreshToken refreshToken) {
        em.remove(refreshToken);
    }

    public RefreshToken findByToken(String token) {
        try {
            return em.createQuery("SELECT r FROM RefreshToken r WHERE r.token = :token", RefreshToken.class)
                    .setParameter("token", token)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }



}
