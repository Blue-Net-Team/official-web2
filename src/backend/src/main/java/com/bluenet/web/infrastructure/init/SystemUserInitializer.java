package com.bluenet.web.infrastructure.init;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.vo.RoleVO;
import com.bluenet.web.domain.repository.RoleRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.config.properties.SystemUserProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemUserInitializer implements CommandLineRunner {

    private final SystemUserProperties systemUserProperties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        String studentId = systemUserProperties.getStudentId();

        if (userRepository.findByStudentId(studentId).isPresent()) {
            log.info("System user already exists with studentId: {}", studentId);
            return;
        }

        try {
            RoleVO role = roleRepository.findByName(RoleType.SUPER_ADMIN.getName())
                    .orElse(null);
            if (role == null) {
                log.error("Failed to create system user: SUPER_ADMIN role not found");
                return;
            }
            String rawPassword = systemUserProperties.getPassword();
            String hashedPassword = sha256Hash(rawPassword);
            String encodedPassword = passwordEncoder.encode(hashedPassword);

            User systemUser = User.create(
                    studentId,
                    null,
                    role.getId(),
                    encodedPassword,
                    systemUserProperties.getUsername(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);

            userRepository.save(systemUser);
            log.info("System user created successfully with username: {}", systemUserProperties.getUsername());
        } catch (Exception e) {
            log.error("Failed to create system user: {}", e.getMessage(), e);
        }
    }

    private String sha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
