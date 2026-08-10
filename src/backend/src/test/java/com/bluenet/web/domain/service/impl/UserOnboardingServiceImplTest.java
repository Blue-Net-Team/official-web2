package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.model.entity.Role;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.vo.UserOnboardingCreateUserRequest;
import com.bluenet.web.domain.model.vo.UserOnboardingResult;
import com.bluenet.web.domain.repository.RoleRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.ReferralCodeGenerator;
import com.bluenet.web.domain.util.HashUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link UserOnboardingServiceImpl} 单元测试。
 */
@DisplayName("UserOnboardingServiceImpl 测试")
@ExtendWith(MockitoExtension.class)
class UserOnboardingServiceImplTest {

    private static final String STUDENT_ID = "202400101";
    private static final String EMAIL = "test@example.com";
    private static final Long ROLE_ID = 2L;
    private static final String USERNAME = "Test User";
    private static final Long COLLEGE_ID = 1L;
    private static final String MAJOR = "Computer Science";
    private static final Integer ASSESSMENT_GRADE_YEAR = 2024;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ReferralCodeGenerator referralCodeGenerator;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserOnboardingServiceImpl domainService;

    @BeforeEach
    void setUp() {
        domainService = new UserOnboardingServiceImpl(userRepository, roleRepository, referralCodeGenerator,
                passwordEncoder);
    }

    private UserOnboardingCreateUserRequest buildRequest() {
        return UserOnboardingCreateUserRequest.builder()
                .studentId(STUDENT_ID)
                .email(EMAIL)
                .roleId(ROLE_ID)
                .username(USERNAME)
                .collegeId(COLLEGE_ID)
                .major(MAJOR)
                .assessmentGradeYear(ASSESSMENT_GRADE_YEAR)
                .direction(Direction.COMPUTER_VISION)
                .gender(Gender.MALE)
                .avatarId(null)
                .build();
    }

    @Test
    @DisplayName("createUserWithGeneratedPassword: 应生成密码并创建用户")
    void createUserWithGeneratedPassword_shouldCreateUser() {
        UserOnboardingCreateUserRequest request = buildRequest();
        when(userRepository.findByStudentId(STUDENT_ID)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(referralCodeGenerator.generate()).thenReturn("REFERRAL");
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return null;
        }).when(userRepository).save(any(User.class));

        UserOnboardingResult result = domainService.createUserWithGeneratedPassword(request);

        assertEquals(1L, result.userId());
        assertNotNull(result.initialPassword());
        assertTrue(result.initialPassword().length() >= 10);
        assertEquals("REFERRAL", result.referralCode());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser: 学号已存在时应抛出 DataConflict")
    void createUser_existingStudentId_shouldThrowDataConflict() {
        UserOnboardingCreateUserRequest request = buildRequest();
        User existingUser = User.create(
                STUDENT_ID,
                "other@example.com",
                ROLE_ID,
                "encoded",
                "Other",
                null,
                null,
                null,
                null,
                null,
                Gender.UNKNOWN,
                null,
                null,
                null,
                null,
                null,
                "CODE",
                null);
        when(userRepository.findByStudentId(STUDENT_ID)).thenReturn(Optional.of(existingUser));

        assertThrows(DataConflict.class, () -> domainService.createUser(request, "initialPassword"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("createUser: 邮箱已存在时应抛出 DataConflict")
    void createUser_existingEmail_shouldThrowDataConflict() {
        UserOnboardingCreateUserRequest request = buildRequest();
        when(userRepository.findByStudentId(STUDENT_ID)).thenReturn(Optional.empty());
        User existingUser = User.create(
                "other",
                EMAIL,
                ROLE_ID,
                "encoded",
                "Other",
                null,
                null,
                null,
                null,
                null,
                Gender.UNKNOWN,
                null,
                null,
                null,
                null,
                null,
                "CODE",
                null);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));

        assertThrows(DataConflict.class, () -> domainService.createUser(request, "initialPassword"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("createUser: 应保存密码哈希并返回用户信息")
    void createUser_success_shouldSaveAndReturnResult() {
        UserOnboardingCreateUserRequest request = buildRequest();
        String initialPassword = "Initial123";
        String expectedHashedPassword = HashUtils.sha256(initialPassword);
        String encodedPassword = "encoded-password";
        when(userRepository.findByStudentId(STUDENT_ID)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(expectedHashedPassword)).thenReturn(encodedPassword);
        when(referralCodeGenerator.generate()).thenReturn("REFERRAL");
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return null;
        }).when(userRepository).save(any(User.class));

        UserOnboardingResult result = domainService.createUser(request, initialPassword);

        assertEquals(1L, result.userId());
        assertEquals(initialPassword, result.initialPassword());
        assertEquals("REFERRAL", result.referralCode());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(STUDENT_ID, savedUser.getStudentId());
        assertEquals(EMAIL, savedUser.getEmail());
        assertEquals(ROLE_ID, savedUser.getRoleId());
        assertEquals(encodedPassword, savedUser.getPassword());
        assertEquals(Direction.COMPUTER_VISION, savedUser.getDirection());
        assertEquals(Gender.MALE, savedUser.getGender());
        assertEquals("REFERRAL", savedUser.getInternalReferralCode());
        assertEquals(Boolean.FALSE, savedUser.getDisable());
        verify(passwordEncoder).encode(expectedHashedPassword);
    }

    @Test
    @DisplayName("createUser: 性别为空时应使用 UNKNOWN 默认值")
    void createUser_nullGender_shouldDefaultToUnknown() {
        UserOnboardingCreateUserRequest request = UserOnboardingCreateUserRequest.builder()
                .studentId(STUDENT_ID)
                .email(EMAIL)
                .roleId(ROLE_ID)
                .username(USERNAME)
                .collegeId(COLLEGE_ID)
                .major(MAJOR)
                .assessmentGradeYear(ASSESSMENT_GRADE_YEAR)
                .direction(Direction.COMPUTER_VISION)
                .gender(null)
                .avatarId(null)
                .build();
        String initialPassword = "Initial123";
        when(userRepository.findByStudentId(STUDENT_ID)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(referralCodeGenerator.generate()).thenReturn("REFERRAL");
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return null;
        }).when(userRepository).save(any(User.class));

        domainService.createUser(request, initialPassword);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(Gender.UNKNOWN, savedUser.getGender());
    }

    @Test
    @DisplayName("getMemberRole: 角色存在时应返回 MEMBER 角色")
    void getMemberRole_existing_shouldReturnRole() {
        Role memberRole = Role.reconstruct(2L, RoleType.MEMBER.getName());
        when(roleRepository.findByName(RoleType.MEMBER.getName())).thenReturn(Optional.of(memberRole));

        Role result = domainService.getMemberRole();

        assertEquals(memberRole, result);
    }

    @Test
    @DisplayName("getMemberRole: 角色不存在时应抛出 IllegalStateException")
    void getMemberRole_notFound_shouldThrowIllegalStateException() {
        when(roleRepository.findByName(RoleType.MEMBER.getName())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> domainService.getMemberRole());
    }

    @Test
    @DisplayName("getCandidateRole: 角色存在时应返回 CANDIDATE 角色")
    void getCandidateRole_existing_shouldReturnRole() {
        Role candidateRole = Role.reconstruct(1L, RoleType.CANDIDATE.getName());
        when(roleRepository.findByName(RoleType.CANDIDATE.getName())).thenReturn(Optional.of(candidateRole));

        Role result = domainService.getCandidateRole();

        assertEquals(candidateRole, result);
    }

    @Test
    @DisplayName("getCandidateRole: 角色不存在时应抛出 IllegalStateException")
    void getCandidateRole_notFound_shouldThrowIllegalStateException() {
        when(roleRepository.findByName(RoleType.CANDIDATE.getName())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> domainService.getCandidateRole());
    }
}
