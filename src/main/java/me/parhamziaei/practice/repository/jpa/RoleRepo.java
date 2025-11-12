package me.parhamziaei.practice.repository.jpa;

import me.parhamziaei.practice.entity.jpa.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepo {

    void save(Role role);

    List<Role> findAll();

    Optional<Role> findByName(String roleName);

}
