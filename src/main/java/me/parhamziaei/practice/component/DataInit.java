package me.parhamziaei.practice.component;

import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.dto.request.RegisterRequest;
import me.parhamziaei.practice.entity.Role;
import me.parhamziaei.practice.repository.RoleRepo;
import me.parhamziaei.practice.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInit implements CommandLineRunner {

    private final RoleRepo roleRepo;
    private final UserService userService;

    @Value("${initialize.admin.email}")
    String adminEmail;
    @Value("${initialize.admin.password}")
    String adminPassword;
    @Value("${initialize.admin.fullname}")
    String adminFullName;

    @Override
    public void run(String... args) throws Exception {
        initRoles();
        initAdmin();
    }

    public void initRoles() {
        if (roleRepo.findAll().isEmpty()) {
            Role adminRole = new Role("ROLE_ADMIN");
            Role userRole = new Role("ROLE_USER");
            roleRepo.save(adminRole);
            roleRepo.save(userRole);
        }
    }

    public void initAdmin() {
        try {
            UserDetails user = userService.loadUserByUsername(adminEmail);
        } catch (UsernameNotFoundException e) {
            RegisterRequest request = RegisterRequest
                    .builder()
                    .email(adminEmail)
                    .rawPassword(adminPassword)
                    .rawPasswordConfirm(adminPassword)
                    .fullName(adminFullName)
                    .build();

            userService.register(request);
            userService.addRole(
                    request.getEmail(),
                    roleRepo.findByName("ROLE_ADMIN")
            );
        }
    }

}
