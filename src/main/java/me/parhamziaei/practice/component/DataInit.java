package me.parhamziaei.practice.component;

import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.dto.request.RegisterRequest;
import me.parhamziaei.practice.entity.Role;
import me.parhamziaei.practice.repository.RoleRepo;
import me.parhamziaei.practice.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInit implements CommandLineRunner {

    private final RoleRepo roleRepo;
    private final UserService userService;

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
            UserDetails user = userService.loadUserByUsername("javadtoktamp@gmail.com");
        } catch (UsernameNotFoundException e) {
            RegisterRequest registerRequest = RegisterRequest
                    .builder()
                    .email("javadtoktamp@gmail.com")
                    .rawPassword("12345678")
                    .rawPasswordConfirm("12345678")
                    .fullName("john doe")
                    .build();

            userService.register(registerRequest);
            userService.addRole("javadtoktamp@gmail.com", roleRepo.findByName("ROLE_ADMIN"));
        }
    }

}
