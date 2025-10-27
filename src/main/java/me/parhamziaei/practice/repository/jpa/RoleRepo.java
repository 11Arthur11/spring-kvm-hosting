package me.parhamziaei.practice.repository.jpa;

import me.parhamziaei.practice.entity.jpa.Role;

import java.util.List;

public interface RoleRepo {

    void save(Role role);

    List<Role> findAll();

    Role findByName(String roleName);

}
