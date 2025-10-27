package me.parhamziaei.practice.repository.jpa;

import me.parhamziaei.practice.entity.jpa.RefreshToken;

public interface RefreshTokenRepo {

    void save(RefreshToken refreshToken);

    void update(RefreshToken refreshToken);

    void delete(RefreshToken refreshToken);

    RefreshToken findByToken(String token);

}
