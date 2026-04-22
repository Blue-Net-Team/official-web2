package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.vo.EnrollVO;
import com.bluenet.web.domain.model.vo.EnrollmentApprovalVO;
import com.bluenet.web.domain.model.vo.RoleVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.EnrollRepository;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.RoleRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.ReferralCodeGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollDomainServiceApprovalCredentialTest {

    @Mock
    private EnrollRepository enrollRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FileRepository fileRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ReferralCodeGenerator referralCodeGenerator;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EnrollDomainServiceImpl enrollDomainService;

    private EnrollVO buildPendingEnroll() {
        return EnrollVO.builder()
                .id(1L)
                .username("测试用户")
                .studentId("20240001001")
                .email("candidate@example.com")
                .collegeId(1L)
                .major("软件工程")
                .direction(Direction.COMPUTER_VISION)
                .status(EnrollStatus.PENDING)
                .build();
    }

    @Test
    void approveEnrollment_newUser_shouldPersistEmailAndEncodedPassword_andReturnPlainPasswordForApplicationMessage()
            throws Exception {
        EnrollVO enroll = buildPendingEnroll();
        RoleVO role = RoleVO.builder().id(2L).name("CANDIDATE").build();

        when(enrollRepository.findById(1L)).thenReturn(Optional.of(enroll));
        when(userRepository.findByStudentId(enroll.getStudentId())).thenReturn(Optional.empty());
        when(roleRepository.findByName("CANDIDATE")).thenReturn(Optional.of(role));
        when(referralCodeGenerator.generate()).thenReturn("ABCD1234");
        when(passwordEncoder.encode(anyString())).thenReturn("bcrypt-value");

        EnrollmentApprovalVO approval = enrollDomainService.approveEnrollment(1L);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<String> encoderInputCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository).save(userCaptor.capture());
        verify(passwordEncoder).encode(encoderInputCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("candidate@example.com", savedUser.getEmail());
        assertEquals("bcrypt-value", savedUser.getPassword());

        String plainPassword = approval.getInitialPassword();
        assertTrue(approval.isNewUserCreated(), "新建账号时应返回通知所需初始凭据");
        assertEquals("candidate@example.com", approval.getEmail());
        assertEquals(8, plainPassword.length());

        String expectedSha256 = sha256Hex(plainPassword);
        assertEquals(expectedSha256, encoderInputCaptor.getValue());
    }

    @Test
    void approveEnrollment_existingUser_shouldNotRotatePasswordOrResendCredentialEmail() {
        EnrollVO enroll = buildPendingEnroll();
        UserVO existingUser = UserVO.builder().id(88L).studentId(enroll.getStudentId()).build();
        when(enrollRepository.findById(1L)).thenReturn(Optional.of(enroll));
        when(userRepository.findByStudentId(enroll.getStudentId())).thenReturn(Optional.of(existingUser));

        enrollDomainService.approveEnrollment(1L);

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void approveEnrollment_newUser_shouldUpdateEnrollmentStatusToApproved() {
        EnrollVO enroll = buildPendingEnroll();
        RoleVO role = RoleVO.builder().id(2L).name("CANDIDATE").build();
        when(enrollRepository.findById(1L)).thenReturn(Optional.of(enroll));
        when(userRepository.findByStudentId(enroll.getStudentId())).thenReturn(Optional.empty());
        when(roleRepository.findByName("CANDIDATE")).thenReturn(Optional.of(role));
        when(referralCodeGenerator.generate()).thenReturn("ABCD1234");
        when(passwordEncoder.encode(anyString())).thenReturn("bcrypt-value");

        enrollDomainService.approveEnrollment(1L);

        ArgumentCaptor<EnrollVO> enrollCaptor = ArgumentCaptor.forClass(EnrollVO.class);
        verify(enrollRepository).update(enrollCaptor.capture());
        assertNotNull(enrollCaptor.getValue());
        assertEquals(EnrollStatus.APPROVED, enrollCaptor.getValue().getStatus());
    }

    private String sha256Hex(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            String part = Integer.toHexString(0xff & b);
            if (part.length() == 1) {
                hex.append('0');
            }
            hex.append(part);
        }
        return hex.toString();
    }
}
