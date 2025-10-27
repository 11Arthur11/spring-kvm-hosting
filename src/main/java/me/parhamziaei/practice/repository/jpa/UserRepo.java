package me.parhamziaei.practice.repository.jpa;

import me.parhamziaei.practice.entity.jpa.User;

import java.util.List;

public interface UserRepo {

    List<User> findAll();

    void save(User user);

    void update(User user);

    void delete(User user);

    User findById(Long id);

    User findByEmail(String email);

    boolean existsByEmail(String email);

}
