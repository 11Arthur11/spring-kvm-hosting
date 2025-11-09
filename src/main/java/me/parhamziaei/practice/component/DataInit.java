package me.parhamziaei.practice.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.parhamziaei.practice.configuration.properties.RuntimeInitProperties;
import me.parhamziaei.practice.dto.request.authenticate.RegisterRequest;
import me.parhamziaei.practice.entity.jpa.Role;
import me.parhamziaei.practice.enums.Roles;
import me.parhamziaei.practice.repository.jpa.RoleRepo;
import me.parhamziaei.practice.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInit implements CommandLineRunner {

    private final RoleRepo roleRepo;
    private final UserService userService;
    private final RuntimeInitProperties initProperties;

    @Override
    public void run(String... args) {
        initRoles();
        initAdmin();
    }

    public void initRoles() {
        List<String> dbRoles = roleRepo.findAll().stream().map(Role::getName).toList();
        List<String> internalRoles = Arrays.stream(Roles.values()).map(Roles::getName).toList();
        internalRoles.forEach(role -> {
            if (!dbRoles.contains(role)) {
                roleRepo.save(new Role(role));
                log.info("Role ({}) created by system", role);
            }
        });
    }

    public void initAdmin() {
        try {
            UserDetails user = userService.loadUserByUsername(initProperties.adminEmail());
        } catch (UsernameNotFoundException e) {
            RegisterRequest request = RegisterRequest
                    .builder()
                    .email(initProperties.adminEmail())
                    .rawPassword(initProperties.adminPassword())
                    .rawPasswordConfirm(initProperties.adminPassword())
                    .fullName(initProperties.adminFullName())
                    .build();

            userService.initDefaultAdmin(request);
        }
    }

}
