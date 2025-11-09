package me.parhamziaei.practice.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.dto.request.authenticate.ChangePasswordRequest;
import me.parhamziaei.practice.dto.request.authenticate.ForgotPasswordRequest;
import me.parhamziaei.practice.dto.request.authenticate.RegisterRequest;
import me.parhamziaei.practice.entity.jpa.Role;
import me.parhamziaei.practice.entity.jpa.User;
import me.parhamziaei.practice.entity.jpa.UserSetting;
import me.parhamziaei.practice.entity.jpa.Wallet;
import me.parhamziaei.practice.entity.redis.ForgotPasswordSession;
import me.parhamziaei.practice.enums.Roles;
import me.parhamziaei.practice.exception.custom.authenticate.EmailAlreadyTakenException;
import me.parhamziaei.practice.exception.custom.authenticate.PasswordPolicyException;
import me.parhamziaei.practice.repository.jpa.RoleRepo;
import me.parhamziaei.practice.repository.jpa.UserRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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

    public User loadUserByRequest(HttpServletRequest request) throws UsernameNotFoundException {
       final String accessToken = jwtService.extractJwtFromRequest(request);
       final String email = jwtService.extractUsername(accessToken);
       return userRepo.findByEmail(email);
    }

    public void initDefaultAdmin(RegisterRequest registerRequest) {
        Role userRole = roleRepo.findByName(Roles.USER.getName());
        Role adminRole = roleRepo.findByName(Roles.ADMIN.getName());
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

    public boolean changePasswordAndGetResult(ChangePasswordRequest cpRequest, String userEmail) {
        if (!cpRequest.getNewPasswordConfirm().equals(cpRequest.getNewPassword())) {
            throw new PasswordPolicyException();
        }
        User user = (User) loadUserByUsername(userEmail);
        if (passwordEncoder.matches(cpRequest.getOldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(cpRequest.getNewPassword()));
            userRepo.update(user);
            return true;
        } else  {
            return false;
        }
    }

    public void changeForgottenPassword(ForgotPasswordRequest fpRequest, ForgotPasswordSession fpSession) {
        if (!fpRequest.getNewPasswordConfirm().equals(fpRequest.getNewPassword())) {
            throw new PasswordPolicyException();
        }
        User user = (User) loadUserByUsername(fpSession.getUserEmail());
        user.setPassword(passwordEncoder.encode(fpRequest.getNewPassword()));
        userRepo.update(user);
    }

    public void register(RegisterRequest registerRequest) {
        if (!isEmailValid(registerRequest.getEmail())) {
            throw new EmailAlreadyTakenException();
        }
        if (!registerRequest.getRawPassword().equals(registerRequest.getRawPasswordConfirm())) {
            throw new PasswordPolicyException();
        }

        Role role = roleRepo.findByName(Roles.USER.getName());
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
        throw new UsernameNotFoundException("User not found");
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
