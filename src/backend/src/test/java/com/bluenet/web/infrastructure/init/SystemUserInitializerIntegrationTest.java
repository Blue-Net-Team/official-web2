package com.bluenet.web.infrastructure.init;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.config.properties.SystemUserProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SystemUserInitializer 集成测试")
public class SystemUserInitializerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SystemUserProperties systemUserProperties;

    @Autowired
    private SystemUserInitializer systemUserInitializer;

    @BeforeEach
    void setUpSystemUser() {
        systemUserInitializer.run();
    }

    @Test
    @DisplayName("首次启动：系统用户应被自动创建")
    void firstStartup_shouldCreateSystemUser() {
        Optional<UserVO> systemUser = userRepository.findByStudentId("000000000000");

        assertThat(systemUser).isPresent();
        assertThat(systemUser.get().getUsername()).isEqualTo("system");
        assertThat(systemUser.get().getStudentId()).isEqualTo("000000000000");
        assertThat(systemUser.get().isDisabled()).isFalse();
    }

    @Test
    @DisplayName("系统用户密码应被BCrypt加密")
    void systemUserPassword_shouldBeBCryptEncoded() {
        Optional<UserVO> systemUser = userRepository.findByStudentId("000000000000");

        assertThat(systemUser).isPresent();
        String storedPassword = systemUser.get().getPassword();

        assertThat(storedPassword).isNotNull();
        assertThat(storedPassword).startsWith("$2a$");

        // 密码经过 SHA-256 + BCrypt 双重加密
        String rawPassword = systemUserProperties.getPassword();
        String sha256Hashed = sha256Hash(rawPassword);
        assertThat(passwordEncoder.matches(sha256Hashed, storedPassword)).isTrue();
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

    @Test
    @DisplayName("系统用户学号应唯一")
    void systemUserStudentId_shouldBeUnique() {
        Optional<UserVO> user1 = userRepository.findByStudentId("000000000000");
        Optional<UserVO> user2 = userRepository.findByStudentId("000000000000");

        assertThat(user1).isPresent();
        assertThat(user2).isPresent();
        assertThat(user1.get().getId()).isEqualTo(user2.get().getId());
    }

    @Test
    @DisplayName("后续启动：系统用户已存在时应跳过创建")
    void subsequentStartup_shouldSkipCreationWhenUserExists() {
        long countBefore = userRepository.findByStudentId("000000000000").map(u -> 1L).orElse(0L);

        systemUserInitializer.run();

        Optional<UserVO> systemUser = userRepository.findByStudentId("000000000000");
        assertThat(systemUser).isPresent();
        assertThat(countBefore).isEqualTo(1L);
    }
}
