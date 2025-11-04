package me.parhamziaei.practice.entity.jpa;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_settings")
public class UserSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "2fa_enabled")
    private boolean twoFAEnabled = false;

    @Column(name = "sms_enabled")
    private boolean smsNotification = false;

    @Column(name = "email_enabled")
    private boolean emailNotification = false;

}
