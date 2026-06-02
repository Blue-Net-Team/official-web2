package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.domain.model.vo.TabCountsVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * UserDomainService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserDomainServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FileDomainService fileDomainService;

    @InjectMocks
    private UserDomainServiceImpl userDomainService;

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_USERNAME = "测试用户";
    private static final String TEST_NICKNAME = "测试昵称";
    private static final String TEST_COLLEGE = "计算机学院";
    private static final String TEST_MAJOR = "软件工程";
    private static final String TEST_BIO = "这是个人简介";

    @Test
    void getUser_whenUserExists_returnsUserVO() {
        UserVO userVO = UserVO.builder()
                .id(TEST_USER_ID)
                .username(TEST_USERNAME)
                .nickname(TEST_NICKNAME)
                .bio(TEST_BIO)
                .build();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userVO));

        Optional<UserVO> result = userDomainService.getUser(TEST_USER_ID);

        assertTrue(result.isPresent());
        assertEquals(TEST_USER_ID, result.get().getId());
        assertEquals(TEST_NICKNAME, result.get().getNickname());
        assertEquals(TEST_BIO, result.get().getBio());
        verify(userRepository).findById(TEST_USER_ID);
    }

    @Test
    void getUser_whenUserNotExists_returnsEmpty() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        Optional<UserVO> result = userDomainService.getUser(TEST_USER_ID);

        assertFalse(result.isPresent());
        verify(userRepository).findById(TEST_USER_ID);
    }

    @Test
    void updateProfile_whenUserExists_updatesSuccessfully() {
        UserVO userVO = UserVO.builder()
                .id(TEST_USER_ID)
                .username(TEST_USERNAME)
                .build();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userVO));
        when(
                userRepository.updateProfile(
                        eq(TEST_USER_ID),
                        eq(TEST_USERNAME),
                        eq(TEST_NICKNAME),
                        eq(TEST_COLLEGE),
                        eq(TEST_MAJOR),
                        eq(Direction.COMPUTER_VISION),
                        eq(Gender.MALE),
                        eq(TEST_BIO))).thenReturn(1);

        userDomainService.updateProfile(
                TEST_USER_ID,
                TEST_USERNAME,
                TEST_NICKNAME,
                TEST_COLLEGE,
                TEST_MAJOR,
                Direction.COMPUTER_VISION,
                Gender.MALE,
                TEST_BIO,
                null);

        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository).updateProfile(
                eq(TEST_USER_ID),
                eq(TEST_USERNAME),
                eq(TEST_NICKNAME),
                eq(TEST_COLLEGE),
                eq(TEST_MAJOR),
                eq(Direction.COMPUTER_VISION),
                eq(Gender.MALE),
                eq(TEST_BIO));
        verify(userRepository).updateQrcodeId(TEST_USER_ID, null);
    }

    @Test
    void updateProfile_whenUserNotExists_throwsDataNotFound() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(
                DataNotFound.class,
                () -> userDomainService.updateProfile(
                        TEST_USER_ID,
                        TEST_USERNAME,
                        TEST_NICKNAME,
                        TEST_COLLEGE,
                        TEST_MAJOR,
                        Direction.COMPUTER_VISION,
                        Gender.MALE,
                        TEST_BIO,
                        null));

        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository, never()).updateProfile(anyLong(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void updateProfile_withNullValues_updatesSuccessfully() {
        UserVO userVO = UserVO.builder()
                .id(TEST_USER_ID)
                .username(TEST_USERNAME)
                .nickname("原昵称")
                .bio("原简介")
                .build();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userVO));
        when(
                userRepository.updateProfile(
                        eq(TEST_USER_ID),
                        isNull(),
                        isNull(),
                        isNull(),
                        isNull(),
                        isNull(),
                        isNull(),
                        isNull())).thenReturn(1);
        when(userRepository.updateQrcodeId(TEST_USER_ID, null)).thenReturn(1);

        userDomainService.updateProfile(TEST_USER_ID, null, null, null, null, null, null, null, null);

        verify(userRepository).updateProfile(
                eq(TEST_USER_ID),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull());
        verify(userRepository).updateQrcodeId(TEST_USER_ID, null);
    }

    @Test
    void updateProfile_withValidQrcodeFileId_updatesQrcode() {
        UserVO userVO = UserVO.builder()
                .id(TEST_USER_ID)
                .username(TEST_USERNAME)
                .build();
        FileVO qrcodeFile = FileVO.builder().id(100L).type(FileType.QRCODE).build();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userVO));
        when(fileDomainService.getFileById(100L)).thenReturn(qrcodeFile);
        when(userRepository.updateProfile(anyLong(), any(), any(), any(), any(), any(), any(), any())).thenReturn(1);
        when(userRepository.updateQrcodeId(TEST_USER_ID, 100L)).thenReturn(1);

        userDomainService.updateProfile(
                TEST_USER_ID,
                TEST_USERNAME,
                TEST_NICKNAME,
                TEST_COLLEGE,
                TEST_MAJOR,
                Direction.COMPUTER_VISION,
                Gender.MALE,
                TEST_BIO,
                100L);

        verify(fileDomainService).getFileById(100L);
        verify(userRepository).updateQrcodeId(TEST_USER_ID, 100L);
    }

    @Test
    void updateProfile_withQrcodeFileNotFound_throwsDataNotFound() {
        UserVO userVO = UserVO.builder()
                .id(TEST_USER_ID)
                .username(TEST_USERNAME)
                .build();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userVO));
        when(fileDomainService.getFileById(999L)).thenReturn(null);

        DataNotFound ex = assertThrows(
                DataNotFound.class,
                () -> userDomainService.updateProfile(
                        TEST_USER_ID,
                        TEST_USERNAME,
                        TEST_NICKNAME,
                        TEST_COLLEGE,
                        TEST_MAJOR,
                        Direction.COMPUTER_VISION,
                        Gender.MALE,
                        TEST_BIO,
                        999L));

        assertEquals("文件不存在", ex.getMessage());
        verify(userRepository, never()).updateQrcodeId(anyLong(), any());
    }

    @Test
    void updateProfile_withQrcodeFileTypeMismatch_throwsBadRequest() {
        UserVO userVO = UserVO.builder()
                .id(TEST_USER_ID)
                .username(TEST_USERNAME)
                .build();
        FileVO wrongFile = FileVO.builder().id(100L).type(FileType.AVATAR).build();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userVO));
        when(fileDomainService.getFileById(100L)).thenReturn(wrongFile);

        BadRequest ex = assertThrows(
                BadRequest.class,
                () -> userDomainService.updateProfile(
                        TEST_USER_ID,
                        TEST_USERNAME,
                        TEST_NICKNAME,
                        TEST_COLLEGE,
                        TEST_MAJOR,
                        Direction.COMPUTER_VISION,
                        Gender.MALE,
                        TEST_BIO,
                        100L));

        assertEquals("文件类型不匹配，期望 QRCODE", ex.getMessage());
        verify(userRepository, never()).updateQrcodeId(anyLong(), any());
    }

    @Test
    void getTabCounts_returnsCorrectCounts() {
        TabCountsVO expectedCounts = new TabCountsVO(5, 3, 2);
        when(userRepository.getTabCounts(TEST_USER_ID)).thenReturn(expectedCounts);

        TabCountsVO result = userDomainService.getTabCounts(TEST_USER_ID);

        assertNotNull(result);
        assertEquals(5, result.getProjects());
        assertEquals(3, result.getCompetitions());
        assertEquals(2, result.getInternships());
        verify(userRepository).getTabCounts(TEST_USER_ID);
    }

    @Test
    void getTabCounts_whenNoExperiences_returnsZeroCounts() {
        TabCountsVO expectedCounts = new TabCountsVO(0, 0, 0);
        when(userRepository.getTabCounts(TEST_USER_ID)).thenReturn(expectedCounts);

        TabCountsVO result = userDomainService.getTabCounts(TEST_USER_ID);

        assertNotNull(result);
        assertEquals(0, result.getProjects());
        assertEquals(0, result.getCompetitions());
        assertEquals(0, result.getInternships());
    }

    @Test
    void updateUserAvatar_whenUserExists_updatesSuccessfully() {
        UserVO userVO = UserVO.builder()
                .id(TEST_USER_ID)
                .username(TEST_USERNAME)
                .build();
        FileVO fileVO = FileVO.builder()
                .id(100L)
                .name("avatar.jpg")
                .build();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userVO));
        when(userRepository.updateAvatar(any(UserVO.class), any(FileVO.class))).thenReturn(1);

        userDomainService.updateUserAvatar(TEST_USER_ID, fileVO);

        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository).updateAvatar(any(UserVO.class), any(FileVO.class));
    }

    @Test
    void changePassword_whenUserExists_encodesAndUpdates() {
        UserVO userVO = UserVO.builder().id(TEST_USER_ID).build();
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userVO));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encoded_new_pwd");

        userDomainService.changePassword(TEST_USER_ID, "newPassword123");

        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).updatePassword(TEST_USER_ID, "encoded_new_pwd");
    }

    @Test
    void changePassword_whenUserNotExists_throwsDataNotFound() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(
                DataNotFound.class,
                () -> userDomainService.changePassword(TEST_USER_ID, "newPassword123"));

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).updatePassword(anyLong(), anyString());
    }
}
