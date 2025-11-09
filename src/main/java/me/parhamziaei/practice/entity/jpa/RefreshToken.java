package me.parhamziaei.practice.entity.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true ,nullable = false, updatable = false, name = "token")
    private String token;

    @Column(nullable = false, updatable = false, name = "token_owner")
    private String tokenOwner;

    @Column(name = "active")
    private boolean active = true;

    public RefreshToken(String token, String tokenOwner) {
        this.token = token;
        this.tokenOwner = tokenOwner;
    }

    public RefreshToken() {}

}
