package me.parhamziaei.practice.repository.jpa;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.entity.jpa.Role;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RoleRepoImpl implements RoleRepo {

    private final EntityManager em;

    @Transactional
    public void save(Role role) {
        em.persist(role);
    }

    public List<Role> findAll() {
        return em.createQuery("select r from Role r", Role.class).getResultList();
    }

    public Role findByName(String roleName) {
        try {
            return em.createQuery("SELECT r FROM Role r WHERE r.name = :roleName", Role.class).setParameter("roleName", roleName).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

}
