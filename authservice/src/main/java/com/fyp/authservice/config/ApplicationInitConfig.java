package com.fyp.authservice.config;


import com.fyp.authservice.constant.PredefinedRole;
import com.fyp.authservice.entity.Role;
import com.fyp.authservice.entity.User;
import com.fyp.authservice.repository.RoleRepository;
import com.fyp.authservice.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
     PasswordEncoder passwordEncoder ;
    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository){

        return args -> {
        if (userRepository.findByEmailOrUsername("admin@gmail.com").isEmpty()){
            Role adminRole = roleRepository.save(Role.builder()
                            .name(PredefinedRole.ADMIN_ROLE)
                                    .description("Admin Role").build());
            var roles = new HashSet<Role>();
            roles.add(adminRole);
            User user = User.builder()
                    .username("admin")
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("admin"))
                   .roles(roles)
                    .build();
            userRepository.save(user);
            log.warn("admin has been created with default password");
        }
        };
    }

}
