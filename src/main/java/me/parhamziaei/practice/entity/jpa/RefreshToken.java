package me.parhamziaei.practice.entity.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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

    @Column(columnDefinition = "TIMESTAMP(0)", name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "active")
    private boolean active = true;

    public RefreshToken(String token, String tokenOwner, LocalDateTime expiryDate) {
        this.token = token;
        this.tokenOwner = tokenOwner;
        this.expiryDate = expiryDate;
    }

    public RefreshToken() {}

}
