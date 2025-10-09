package me.parhamziaei.practice.service;

import jakarta.servlet.http.Cookie;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.dto.request.LoginRequest;
import me.parhamziaei.practice.dto.request.RegisterRequest;
import me.parhamziaei.practice.entity.Role;
import me.parhamziaei.practice.entity.User;
import me.parhamziaei.practice.repository.RoleRepo;
import me.parhamziaei.practice.repository.UserRepo;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;


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

    public User register(RegisterRequest registerRequest) throws IllegalArgumentException {

        if (!isEmailValid(registerRequest.getEmail())) {
            throw new IllegalArgumentException("email already taken");
        }
        if (!registerRequest.getRawPassword().equals(registerRequest.getRawPasswordConfirm())) {
            throw new IllegalArgumentException("passwords does not match");
        }
        Role role = roleRepo.findByName("ROLE_USER");
        User user = new User();
        user.setFullName(registerRequest.getFullName().trim().toLowerCase());
        user.setEmail(registerRequest.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(registerRequest.getRawPassword()));
        user.setEnabled(true);
        user.setLocked(false);
        user.setCredentialsExpired(false);
        user.setProvider("Local");
        user.setRoles(Set.of(role));
        userRepo.save(user);
        return user;
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
