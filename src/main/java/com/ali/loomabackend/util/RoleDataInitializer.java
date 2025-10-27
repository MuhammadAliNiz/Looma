package com.ali.loomabackend.util;

import com.ali.loomabackend.model.entity.user.Role;
import com.ali.loomabackend.model.enums.UserRolesEnum;
import com.ali.loomabackend.repository.user.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Order(1)
public class RoleDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleDataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public void run(String... args) throws Exception {

        Arrays.stream(UserRolesEnum.values()).forEach(userRoleEnum -> {
            String roleName = userRoleEnum.name();

            if (roleRepository.findByRoleName(roleName).isEmpty()) {

                Role newRole = Role.builder().roleName(roleName).build();
                roleRepository.save(newRole);

            }
        });
    }
}
