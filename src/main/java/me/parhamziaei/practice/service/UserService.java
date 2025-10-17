package me.parhamziaei.practice.service;

import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.dto.request.ChangePasswordRequest;
import me.parhamziaei.practice.dto.request.RegisterRequest;
import me.parhamziaei.practice.entity.Role;
import me.parhamziaei.practice.entity.User;
import me.parhamziaei.practice.entity.UserSetting;
import me.parhamziaei.practice.entity.Wallet;
import me.parhamziaei.practice.exception.custom.authenticate.EmailAlreadyTakenException;
import me.parhamziaei.practice.exception.custom.authenticate.PasswordPolicyException;
import me.parhamziaei.practice.repository.RoleRepo;
import me.parhamziaei.practice.repository.UserRepo;
import me.parhamziaei.practice.repository.redis.EmailVerifyRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final EmailVerifyRepo emailVerifyRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user = userRepo.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("USER_NOT_FOUND");
        }
        return user;
    }

    public boolean isEmailValid(String email) {
        return !userRepo.existsByEmail(email);
    }

    public void initDefaultAdmin(RegisterRequest registerRequest) {
        Role userRole = roleRepo.findByName("ROLE_USER");
        Role adminRole = roleRepo.findByName("ROLE_ADMIN");
        Wallet wallet = new Wallet();
        UserSetting userSetting = new UserSetting();
        User user = User.builder()
                .fullName(registerRequest.getFullName().trim().toLowerCase())
                .email(registerRequest.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(registerRequest.getRawPassword()))
                .roles(Set.of(userRole, adminRole))
                .enabled(true)
                .locked(false)
                .expired(false)
                .build();
        user.setWallet(wallet);
        user.setSetting(userSetting);
        userRepo.save(user);
    }

    public boolean changePasswordAndGetResult(ChangePasswordRequest cpRequest) {
        if (!cpRequest.getNewPasswordConfirm().equals(cpRequest.getNewPassword())) {
            throw new PasswordPolicyException("PASSWORD_CANNOT_BE_CHANGED");
        }
        User user = (User) loadUserByUsername(cpRequest.getUserEmail());
        if (passwordEncoder.matches(cpRequest.getOldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(cpRequest.getNewPassword()));
            userRepo.update(user);
            return true;
        } else  {
            return false;
        }
    }

    public void register(RegisterRequest registerRequest) {
        if (!isEmailValid(registerRequest.getEmail())) {
            throw new EmailAlreadyTakenException();
        }
        if (!registerRequest.getRawPassword().equals(registerRequest.getRawPasswordConfirm())) {
            throw new PasswordPolicyException("PASSWORDS_DONT_MATCH");
        }

        Role role = roleRepo.findByName("ROLE_USER");
        Wallet wallet = new Wallet();
        UserSetting userSetting = new UserSetting();
        User user = User.builder()
                .fullName(registerRequest.getFullName().trim().toLowerCase())
                .email(registerRequest.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(registerRequest.getRawPassword()))
                .roles(Set.of(role))
                .enabled(false)
                .locked(false)
                .expired(false)
                .build();
        user.setWallet(wallet);
        user.setSetting(userSetting);
        userRepo.save(user);
    }

    public void enableUser(String email) {
        User user = userRepo.findByEmail(email);
        user.setEnabled(true);
        userRepo.save(user);
    }

    public boolean isUserTwoFAEnabled(String email) {
        User user = userRepo.findByEmail(email);
        if (user != null) {
            return user.getSetting().isTwoFAEnabled();
        }
        throw new UsernameNotFoundException("USER_NOT_FOUND");
    }

    public Set<Role> getRoles(String email) {
        User user = (User) loadUserByUsername(email);
        return user.getRoles();
    }

    public Set<Role> addRole(String email, Role role) {
        User user = (User) loadUserByUsername(email);
        user.getRoles().add(role);
        userRepo.update(user);
        return user.getRoles();
    }

    public Set<Role> removeRole(String email, Role role) {
        User user = (User) loadUserByUsername(email);
        user.getRoles().remove(role);
        userRepo.update(user);
        return user.getRoles();
    }

    public void updateLastLogin(String email) {
        User user = (User) loadUserByUsername(email);
        user.setLastLogin(LocalDateTime.now().withNano(0));
        userRepo.update(user);
    }

}
