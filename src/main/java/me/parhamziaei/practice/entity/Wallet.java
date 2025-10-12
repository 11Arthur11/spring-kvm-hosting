package me.parhamziaei.practice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "user_wallets")
@Getter
@Setter
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(precision = 19, scale = 2, name = "balance")
    private BigDecimal balance = BigDecimal.ZERO;

}
