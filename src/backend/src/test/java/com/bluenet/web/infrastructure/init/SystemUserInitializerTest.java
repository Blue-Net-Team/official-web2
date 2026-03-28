package com.bluenet.web.infrastructure.init;

import com.bluenet.web.domain.model.entity.Role;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.config.properties.SystemUserProperties;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("SystemUserInitializer 单元测试")
@ExtendWith(MockitoExtension.class)
class SystemUserInitializerTest {

    @Mock
    private SystemUserProperties systemUserProperties;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private SystemUserInitializer systemUserInitializer;

    private static final String DEFAULT_STUDENT_ID = "000000000000";
    private static final String DEFAULT_USERNAME = "system";
    private static final String DEFAULT_PASSWORD = "admin123";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedPasswordHash";

    private static final String DEFAULT_PASSWORD_SHA256 = "240be518fabd2724ddb6f04eeb9d5b043d1d86a9d8a3c8d8a9d8a3c8d8a9d8a3";

    @Nested
    @DisplayName("首次启动场景")
    class FirstStartupScenario {

        @Test
        @DisplayName("系统用户不存在时应创建新用户，密码先SHA-256哈希再BCrypt加密")
        void run_whenSystemUserNotExists_shouldCreateUserWithSha256PreHash() throws Exception {
            String expectedSha256Hash = sha256Hash(DEFAULT_PASSWORD);

            when(systemUserProperties.getStudentId()).thenReturn(DEFAULT_STUDENT_ID);
            when(systemUserProperties.getUsername()).thenReturn(DEFAULT_USERNAME);
            when(systemUserProperties.getPassword()).thenReturn(DEFAULT_PASSWORD);
            when(userRepository.findByStudentId(DEFAULT_STUDENT_ID)).thenReturn(Optional.empty());
            when(passwordEncoder.encode(expectedSha256Hash)).thenReturn(ENCODED_PASSWORD);
            when(roleMapper.selectByName("SUPER_ADMIN")).thenReturn(Role.buildSuperAdmin());

            systemUserInitializer.run();

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getStudentId()).isEqualTo(DEFAULT_STUDENT_ID);
            assertThat(savedUser.getUsername()).isEqualTo(DEFAULT_USERNAME);
            assertThat(savedUser.getPassword()).isEqualTo(ENCODED_PASSWORD);
            assertThat(savedUser.getDisable()).isFalse();

            verify(passwordEncoder).encode(expectedSha256Hash);
        }

        @Test
        @DisplayName("创建用户时发生异常应捕获并记录日志")
        void run_whenExceptionOccurs_shouldCatchAndLog() throws Exception {
            String expectedSha256Hash = sha256Hash(DEFAULT_PASSWORD);

            when(systemUserProperties.getStudentId()).thenReturn(DEFAULT_STUDENT_ID);
            when(systemUserProperties.getUsername()).thenReturn(DEFAULT_USERNAME);
            when(systemUserProperties.getPassword()).thenReturn(DEFAULT_PASSWORD);
            when(userRepository.findByStudentId(DEFAULT_STUDENT_ID)).thenReturn(Optional.empty());
            when(passwordEncoder.encode(expectedSha256Hash)).thenReturn(ENCODED_PASSWORD);
            when(roleMapper.selectByName("SUPER_ADMIN")).thenReturn(Role.buildSuperAdmin());
            doThrow(new RuntimeException("Database error")).when(userRepository).save(any(User.class));

            systemUserInitializer.run();

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("使用自定义配置创建系统用户，密码正确进行SHA-256预哈希")
        void run_withCustomConfig_shouldCreateUserWithCustomValuesAndSha256PreHash() throws Exception {
            String customStudentId = "999999999999";
            String customUsername = "customAdmin";
            String customPassword = "customPassword";
            String customEncodedPassword = "$2a$10$customEncodedPassword";
            String expectedSha256Hash = sha256Hash(customPassword);

            when(systemUserProperties.getStudentId()).thenReturn(customStudentId);
            when(systemUserProperties.getUsername()).thenReturn(customUsername);
            when(systemUserProperties.getPassword()).thenReturn(customPassword);
            when(userRepository.findByStudentId(customStudentId)).thenReturn(Optional.empty());
            when(passwordEncoder.encode(expectedSha256Hash)).thenReturn(customEncodedPassword);
            when(roleMapper.selectByName("SUPER_ADMIN")).thenReturn(Role.buildSuperAdmin());

            systemUserInitializer.run();

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getStudentId()).isEqualTo(customStudentId);
            assertThat(savedUser.getUsername()).isEqualTo(customUsername);
            assertThat(savedUser.getPassword()).isEqualTo(customEncodedPassword);

            verify(passwordEncoder).encode(expectedSha256Hash);
        }
    }

    @Nested
    @DisplayName("后续启动场景")
    class SubsequentStartupScenario {

        @Test
        @DisplayName("系统用户已存在时应跳过创建")
        void run_whenSystemUserExists_shouldSkipCreation() throws Exception {
            when(systemUserProperties.getStudentId()).thenReturn(DEFAULT_STUDENT_ID);
            UserVO existingUser = UserVO.builder()
                    .id(1L)
                    .studentId(DEFAULT_STUDENT_ID)
                    .username(DEFAULT_USERNAME)
                    .build();
            when(userRepository.findByStudentId(DEFAULT_STUDENT_ID)).thenReturn(Optional.of(existingUser));

            systemUserInitializer.run();

            verify(userRepository, never()).save(any(User.class));
            verify(passwordEncoder, never()).encode(anyString());
        }
    }

    @Nested
    @DisplayName("SHA-256哈希验证")
    class Sha256HashValidation {

        @Test
        @DisplayName("SHA-256哈希应生成64位小写十六进制字符串")
        void sha256Hash_shouldGenerate64CharLowercaseHexString() throws Exception {
            String hash = sha256HashViaInitializer(DEFAULT_PASSWORD);

            assertThat(hash).hasSize(64);
            assertThat(hash).matches("[a-f0-9]+");
        }

        @Test
        @DisplayName("相同输入应生成相同哈希值")
        void sha256Hash_sameInput_shouldGenerateSameHash() throws Exception {
            String hash1 = sha256HashViaInitializer("test123");
            String hash2 = sha256HashViaInitializer("test123");

            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("不同输入应生成不同哈希值")
        void sha256Hash_differentInput_shouldGenerateDifferentHash() throws Exception {
            String hash1 = sha256HashViaInitializer("password1");
            String hash2 = sha256HashViaInitializer("password2");

            assertThat(hash1).isNotEqualTo(hash2);
        }
    }

    private String sha256HashViaInitializer(String input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private String sha256Hash(String input) {
        return sha256HashViaInitializer(input);
    }
}
