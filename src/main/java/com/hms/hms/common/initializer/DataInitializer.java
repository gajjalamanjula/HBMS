package com.hms.hms.common.initializer;

import com.hms.hms.common.enums.Role;
import com.hms.hms.modules.user.entity.User;
import com.hms.hms.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.findByEmail("admin@mail.com").isEmpty()) {

            User admin = User.builder()
                    .email("admin@mail.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.SUPER_ADMIN)
                    .phone("9999999999")
                    .name("Admin")
                    .active(true)
                    .verified(true)
                    .build();

            userRepository.save(admin);

            System.out.println("SUPER_ADMIN created");
        }

        if (userRepository.findByEmail("admin2@mail.com").isEmpty()) {

            User admin2 = User.builder()
                    .email("admin2@mail.com")
                    .password(passwordEncoder.encode("12345678"))
                    .role(Role.SUPER_ADMIN)
                    .phone("8888888888")
                    .name("Admin Two")
                    .active(true)
                    .verified(true)
                    .kycVerified(true)
                    .mustChangePassword(false)
                    .build();

            userRepository.save(admin2);

            System.out.println("SUPER_ADMIN 2 created");
        }
    }
}