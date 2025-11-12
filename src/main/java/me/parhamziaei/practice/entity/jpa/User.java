package me.parhamziaei.practice.entity.jpa;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import me.parhamziaei.practice.enums.Roles;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100, name = "email")
    private String email;

    @Column(nullable = false, length = 100, name = "full_name")
    private String fullName;

    @Column(nullable = false, length = 80, name = "password")
    private String password;

    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL)
    private Wallet wallet;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserSetting setting;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @Column(columnDefinition = "TIMESTAMP(0)", name = "last_login")
    private LocalDateTime lastLogin;

    @Column(columnDefinition = "TIMESTAMP(0)", name = "created_at")
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TIMESTAMP(0)", name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "email_verified")
    private boolean emailVerified;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "expired")
    private boolean expired;

    @Column(name = "locked")
    private boolean locked;

    @Column(name = "credentials_expired")
    private boolean credentialsExpired = false;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now().withNano(0);
        this.updatedAt = LocalDateTime.now().withNano(0);
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now().withNano(0);
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
        wallet.setOwner(this);
    }

    public void setSetting(UserSetting setting) {
        this.setting = setting;
        setting.setUser(this);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(role -> (GrantedAuthority) role::getName).toList();
    }

    public boolean isStaff() {
        Set<String> userRoles = roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        List<String> staffRoles = Roles.staffRoles().stream()
                .map(Roles::value)
                .toList();

        for (String userRole : userRoles) {
            if (staffRoles.contains(userRole)) {
                return true;
            }
        }
        return false;
    }

    public Role getHigherAuthority() {
        return roles.stream().max(Comparator.comparing(Role::getHierarchy)).get();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !this.expired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !this.credentialsExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
