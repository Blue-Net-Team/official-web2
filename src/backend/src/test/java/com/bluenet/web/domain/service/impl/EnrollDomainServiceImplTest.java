package com.bluenet.web.domain.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.Role;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.EnrollBriefVO;
import com.bluenet.web.domain.model.vo.EnrollStatisticsVO;
import com.bluenet.web.domain.model.vo.EnrollVO;
import com.bluenet.web.domain.repository.EnrollRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.ReferralCodeGenerator;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;

@DisplayName("EnrollDomainServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class EnrollDomainServiceImplTest {

    @Mock
    private EnrollRepository enrollRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private FileMapper fileMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private ReferralCodeGenerator referralCodeGenerator;

    @InjectMocks
    private EnrollDomainServiceImpl enrollDomainService;

    private static final Long TEST_ID = 1L;
    private static final String TEST_USERNAME = "张三";
    private static final String TEST_STUDENT_ID = "20210001001";
    private static final Long TEST_COLLEGE_ID = 1L;
    private static final String TEST_COLLEGE_NAME = "计算机学院";
    private static final String TEST_MAJOR = "计算机科学与技术";
    private static final Integer TEST_GRADE = 2;
    private static final Direction TEST_DIRECTION = Direction.COMPUTER_VISION;
    private static final Long TEST_AVATAR_ID = 100L;
    private static final Long TEST_ROLE_ID = 1L;

    private EnrollVO createTestEnrollVO() {
        return EnrollVO.builder()
                .id(TEST_ID)
                .username(TEST_USERNAME)
                .studentId(TEST_STUDENT_ID)
                .collegeId(TEST_COLLEGE_ID)
                .collegeName(TEST_COLLEGE_NAME)
                .major(TEST_MAJOR)
                .grade(TEST_GRADE)
                .direction(TEST_DIRECTION)
                .avatarFileId(TEST_AVATAR_ID)
                .status(EnrollStatus.PENDING)
                .build();
    }

    private EnrollBriefVO createTestEnrollBriefVO() {
        return EnrollBriefVO.builder()
                .id(TEST_ID)
                .username(TEST_USERNAME)
                .studentId(TEST_STUDENT_ID)
                .collegeName(TEST_COLLEGE_NAME)
                .major(TEST_MAJOR)
                .grade(TEST_GRADE)
                .direction(TEST_DIRECTION)
                .status(EnrollStatus.PENDING)
                .avatarFileId(TEST_AVATAR_ID)
                .build();
    }

    private Role createTestRole() {
        return Role.builder()
                .id(TEST_ROLE_ID)
                .name("CANDIDATE")
                .build();
    }

    private File createTestAvatarFile() {
        return File.builder()
                .id(TEST_AVATAR_ID)
                .name("avatar.jpg")
                .type(FileType.AVATAR)
                .url("http://example.com/avatar.jpg")
                .build();
    }

    @Nested
    @DisplayName("getEnrollmentById 方法测试")
    class GetEnrollmentByIdTests {

        @Test
        @DisplayName("正常情况：应返回EnrollVO")
        void getEnrollmentById_existingId_shouldReturnEnrollVO() {
            EnrollVO expected = createTestEnrollVO();
            when(enrollRepository.findById(TEST_ID)).thenReturn(Optional.of(expected));

            Optional<EnrollVO> result = enrollDomainService.getEnrollmentById(TEST_ID);

            assertTrue(result.isPresent());
            assertEquals(TEST_ID, result.get().getId());
            assertEquals(TEST_USERNAME, result.get().getUsername());
            verify(enrollRepository).findById(TEST_ID);
        }

        @Test
        @DisplayName("报名不存在：应返回空Optional")
        void getEnrollmentById_nonExistingId_shouldReturnEmpty() {
            when(enrollRepository.findById(TEST_ID)).thenReturn(Optional.empty());

            Optional<EnrollVO> result = enrollDomainService.getEnrollmentById(TEST_ID);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getEnrollmentByStudentId 方法测试")
    class GetEnrollmentByStudentIdTests {

        @Test
        @DisplayName("正常情况：应返回EnrollVO")
        void getEnrollmentByStudentId_existingStudentId_shouldReturnEnrollVO() {
            EnrollVO expected = createTestEnrollVO();
            when(enrollRepository.findByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.of(expected));

            Optional<EnrollVO> result = enrollDomainService.getEnrollmentByStudentId(TEST_STUDENT_ID);

            assertTrue(result.isPresent());
            assertEquals(TEST_STUDENT_ID, result.get().getStudentId());
            verify(enrollRepository).findByStudentId(TEST_STUDENT_ID);
        }

        @Test
        @DisplayName("学号不存在：应返回空Optional")
        void getEnrollmentByStudentId_nonExistingStudentId_shouldReturnEmpty() {
            when(enrollRepository.findByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.empty());

            Optional<EnrollVO> result = enrollDomainService.getEnrollmentByStudentId(TEST_STUDENT_ID);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("existsByStudentId 方法测试")
    class ExistsByStudentIdTests {

        @Test
        @DisplayName("学号存在：应返回true")
        void existsByStudentId_existingStudentId_shouldReturnTrue() {
            when(enrollRepository.existsByStudentId(TEST_STUDENT_ID)).thenReturn(true);

            boolean result = enrollDomainService.existsByStudentId(TEST_STUDENT_ID);

            assertTrue(result);
        }

        @Test
        @DisplayName("学号不存在：应返回false")
        void existsByStudentId_nonExistingStudentId_shouldReturnFalse() {
            when(enrollRepository.existsByStudentId(TEST_STUDENT_ID)).thenReturn(false);

            boolean result = enrollDomainService.existsByStudentId(TEST_STUDENT_ID);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("createEnrollment 方法测试")
    class CreateEnrollmentTests {

        @Test
        @DisplayName("正常创建：应设置状态为PENDING并返回ID")
        void createEnrollment_validEnrollVO_shouldSetPendingAndReturnId() {
            EnrollVO vo = EnrollVO.builder()
                    .username(TEST_USERNAME)
                    .studentId(TEST_STUDENT_ID)
                    .collegeId(TEST_COLLEGE_ID)
                    .major(TEST_MAJOR)
                    .grade(TEST_GRADE)
                    .direction(TEST_DIRECTION)
                    .build();

            when(enrollRepository.save(any(EnrollVO.class))).thenReturn(TEST_ID);

            Long result = enrollDomainService.createEnrollment(vo);

            assertEquals(TEST_ID, result);
            verify(enrollRepository).save(argThat(saved -> saved.getStatus() == EnrollStatus.PENDING));
        }
    }

    @Nested
    @DisplayName("updateEnrollment 方法测试")
    class UpdateEnrollmentTests {

        @Test
        @DisplayName("正常更新：应调用仓库更新")
        void updateEnrollment_existingEnroll_shouldCallRepositoryUpdate() {
            EnrollVO existing = createTestEnrollVO();
            EnrollVO update = EnrollVO.builder()
                    .id(TEST_ID)
                    .username("李四")
                    .studentId(TEST_STUDENT_ID)
                    .build();

            when(enrollRepository.findById(TEST_ID)).thenReturn(Optional.of(existing));
            doAnswer(inv -> null).when(enrollRepository).update(any(EnrollVO.class));

            enrollDomainService.updateEnrollment(update);

            verify(enrollRepository).update(any(EnrollVO.class));
        }

        @Test
        @DisplayName("报名不存在：应抛出DataNotFound异常")
        void updateEnrollment_nonExistingEnroll_shouldThrowDataNotFound() {
            EnrollVO update = EnrollVO.builder().id(TEST_ID).build();
            when(enrollRepository.findById(TEST_ID)).thenReturn(Optional.empty());

            assertThrows(DataNotFound.class, () -> enrollDomainService.updateEnrollment(update));
        }

        @Test
        @DisplayName("被拒绝的报名更新：状态应保持不变")
        void updateEnrollment_rejectedEnroll_shouldKeepStatus() {
            EnrollVO existing = createTestEnrollVO().toBuilder()
                    .status(EnrollStatus.REJECTED)
                    .build();

            EnrollVO update = EnrollVO.builder()
                    .id(TEST_ID)
                    .username("李四")
                    .studentId(TEST_STUDENT_ID)
                    .build();

            when(enrollRepository.findById(TEST_ID)).thenReturn(Optional.of(existing));

            enrollDomainService.updateEnrollment(update);

            verify(enrollRepository).update(argThat(saved -> saved.getStatus() == EnrollStatus.REJECTED));
        }

        @Test
        @DisplayName("已通过的报名更新：状态应保持不变")
        void updateEnrollment_approvedEnroll_shouldKeepStatus() {
            EnrollVO existing = createTestEnrollVO().toBuilder()
                    .status(EnrollStatus.APPROVED)
                    .build();

            EnrollVO update = EnrollVO.builder()
                    .id(TEST_ID)
                    .username("李四")
                    .studentId(TEST_STUDENT_ID)
                    .build();

            when(enrollRepository.findById(TEST_ID)).thenReturn(Optional.of(existing));

            enrollDomainService.updateEnrollment(update);

            verify(enrollRepository).update(argThat(saved -> saved.getStatus() == EnrollStatus.APPROVED));
        }
    }

    @Nested
    @DisplayName("getEnrollmentList 方法测试")
    class GetEnrollmentListTests {

        @Test
        @DisplayName("正常查询：应返回分页结果")
        void getEnrollmentList_normalCase_shouldReturnPagedResult() {
            Pageable pageable = PageRequest.of(0, 10);
            List<EnrollBriefVO> voList = new ArrayList<>();
            voList.add(createTestEnrollBriefVO());
            Page<EnrollBriefVO> expectedPage = new PageImpl<>(voList, pageable, 1);

            when(enrollRepository.search(null, null, null, pageable)).thenReturn(expectedPage);

            Page<EnrollBriefVO> result = enrollDomainService.getEnrollmentList(null, null, null, pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(1, result.getContent().size());
        }

        @Test
        @DisplayName("带筛选条件：应传递正确的参数")
        void getEnrollmentList_withFilters_shouldPassCorrectParams() {
            Pageable pageable = PageRequest.of(0, 10);
            List<EnrollBriefVO> voList = new ArrayList<>();
            Page<EnrollBriefVO> expectedPage = new PageImpl<>(voList, pageable, 0);

            when(enrollRepository.search("张三", EnrollStatus.PENDING, TEST_DIRECTION, pageable))
                    .thenReturn(expectedPage);

            Page<EnrollBriefVO> result = enrollDomainService.getEnrollmentList(
                    "张三",
                    EnrollStatus.PENDING,
                    TEST_DIRECTION,
                    pageable);

            assertNotNull(result);
            verify(enrollRepository).search("张三", EnrollStatus.PENDING, TEST_DIRECTION, pageable);
        }
    }

    @Nested
    @DisplayName("getStatistics 方法测试")
    class GetStatisticsTests {

        @Test
        @DisplayName("正常查询：应返回统计数据")
        void getStatistics_normalCase_shouldReturnStatistics() {
            EnrollStatisticsVO expected = EnrollStatisticsVO.builder()
                    .total(10L)
                    .byStatus(Map.of("pending", 5L, "approved", 3L, "rejected", 2L))
                    .byDirection(Map.of(Direction.COMPUTER_VISION, 4L, Direction.EMBEDDED, 3L))
                    .build();

            when(enrollRepository.getStatistics()).thenReturn(expected);

            EnrollStatisticsVO result = enrollDomainService.getStatistics();

            assertNotNull(result);
            assertEquals(10L, result.getTotal());
        }
    }

    @Nested
    @DisplayName("approveEnrollment 方法测试")
    class ApproveEnrollmentTests {

        @Test
        @DisplayName("正常审核通过：应更新状态为APPROVED并创建CANDIDATE角色用户")
        void approveEnrollment_normalCase_shouldUpdateStatusToApproved() {
            EnrollVO enroll = createTestEnrollVO();
            Role candidateRole = createTestRole();

            when(enrollRepository.findById(TEST_ID)).thenReturn(Optional.of(enroll));
            when(roleMapper.selectByName("CANDIDATE")).thenReturn(candidateRole);
            when(userMapper.selectByStudentId(TEST_STUDENT_ID)).thenReturn(null);
            when(userMapper.insert(any(User.class))).thenReturn(1);
            when(referralCodeGenerator.generate()).thenReturn("ABCD1234");

            enrollDomainService.approveEnrollment(TEST_ID);

            verify(enrollRepository).update(argThat(updated -> updated.getStatus() == EnrollStatus.APPROVED));
            verify(referralCodeGenerator).generate();
        }

        @Test
        @DisplayName("报名不存在：应抛出DataNotFound异常")
        void approveEnrollment_nonExistingEnroll_shouldThrowDataNotFound() {
            when(enrollRepository.findById(TEST_ID)).thenReturn(Optional.empty());

            assertThrows(DataNotFound.class, () -> enrollDomainService.approveEnrollment(TEST_ID));
        }

        @Test
        @DisplayName("非PENDING状态：应抛出IllegalStateException")
        void approveEnrollment_notPendingStatus_shouldThrowIllegalStateException() {
            EnrollVO enroll = createTestEnrollVO().toBuilder()
                    .status(EnrollStatus.APPROVED)
                    .build();

            when(enrollRepository.findById(TEST_ID)).thenReturn(Optional.of(enroll));

            assertThrows(IllegalStateException.class, () -> enrollDomainService.approveEnrollment(TEST_ID));
        }

        @Test
        @DisplayName("用户已存在：应跳过用户创建")
        void approveEnrollment_userAlreadyExists_shouldSkipUserCreation() {
            EnrollVO enroll = createTestEnrollVO();
            User existingUser = User.builder().id(999L).studentId(TEST_STUDENT_ID).build();

            when(enrollRepository.findById(TEST_ID)).thenReturn(Optional.of(enroll));
            when(userMapper.selectByStudentId(TEST_STUDENT_ID)).thenReturn(existingUser);

            enrollDomainService.approveEnrollment(TEST_ID);

            verify(userMapper, never()).insert(any(User.class));
            verify(enrollRepository).update(argThat(updated -> updated.getStatus() == EnrollStatus.APPROVED));
        }

        @Test
        @DisplayName("CANDIDATE角色不存在：应抛出IllegalStateException")
        void approveEnrollment_candidateRoleNotExists_shouldThrowIllegalStateException() {
            EnrollVO enroll = createTestEnrollVO();

            when(enrollRepository.findById(TEST_ID)).thenReturn(Optional.of(enroll));
            when(userMapper.selectByStudentId(TEST_STUDENT_ID)).thenReturn(null);
            when(roleMapper.selectByName("CANDIDATE")).thenReturn(null);

            assertThrows(IllegalStateException.class, () -> enrollDomainService.approveEnrollment(TEST_ID));
        }
    }

    @Nested
    @DisplayName("rejectEnrollment 方法测试")
    class RejectEnrollmentTests {

        @Test
        @DisplayName("正常拒绝：应更新状态为REJECTED")
        void rejectEnrollment_normalCase_shouldUpdateStatusToRejected() {
            EnrollVO enroll = createTestEnrollVO();

            when(enrollRepository.findById(TEST_ID)).thenReturn(Optional.of(enroll));

            enrollDomainService.rejectEnrollment(TEST_ID, "不符合条件");

            verify(enrollRepository).update(argThat(updated -> updated.getStatus() == EnrollStatus.REJECTED));
        }

        @Test
        @DisplayName("报名不存在：应抛出DataNotFound异常")
        void rejectEnrollment_nonExistingEnroll_shouldThrowDataNotFound() {
            when(enrollRepository.findById(TEST_ID)).thenReturn(Optional.empty());

            assertThrows(DataNotFound.class, () -> enrollDomainService.rejectEnrollment(TEST_ID, "原因"));
        }

        @Test
        @DisplayName("非PENDING状态：应抛出IllegalStateException")
        void rejectEnrollment_notPendingStatus_shouldThrowIllegalStateException() {
            EnrollVO enroll = createTestEnrollVO().toBuilder()
                    .status(EnrollStatus.REJECTED)
                    .build();

            when(enrollRepository.findById(TEST_ID)).thenReturn(Optional.of(enroll));

            assertThrows(IllegalStateException.class, () -> enrollDomainService.rejectEnrollment(TEST_ID, "原因"));
        }

        @Test
        @DisplayName("拒绝原因为空：应正常处理")
        void rejectEnrollment_nullReason_shouldProcessNormally() {
            EnrollVO enroll = createTestEnrollVO();

            when(enrollRepository.findById(TEST_ID)).thenReturn(Optional.of(enroll));

            enrollDomainService.rejectEnrollment(TEST_ID, null);

            verify(enrollRepository).update(argThat(updated -> updated.getStatus() == EnrollStatus.REJECTED));
        }
    }

    @Nested
    @DisplayName("validateAvatar 方法测试")
    class ValidateAvatarTests {

        @Test
        @DisplayName("avatarId为null：应正常通过")
        void validateAvatar_nullAvatarId_shouldPass() {
            assertDoesNotThrow(() -> enrollDomainService.validateAvatar(null));
        }

        @Test
        @DisplayName("正常头像文件：应正常通过")
        void validateAvatar_validAvatarFile_shouldPass() {
            File avatarFile = createTestAvatarFile();
            when(fileMapper.selectById(TEST_AVATAR_ID)).thenReturn(avatarFile);

            assertDoesNotThrow(() -> enrollDomainService.validateAvatar(TEST_AVATAR_ID));
        }

        @Test
        @DisplayName("文件不存在：应抛出BadRequest异常")
        void validateAvatar_nonExistingFile_shouldThrowException() {
            when(fileMapper.selectById(TEST_AVATAR_ID)).thenReturn(null);

            BadRequest exception = assertThrows(
                    BadRequest.class,
                    () -> enrollDomainService.validateAvatar(TEST_AVATAR_ID));
            assertEquals("头像文件不存在", exception.getMessage());
        }

        @Test
        @DisplayName("文件类型不是头像：应抛出GlobalException")
        void validateAvatar_wrongFileType_shouldThrowException() {
            File wrongTypeFile = File.builder()
                    .id(TEST_AVATAR_ID)
                    .name("document.pdf")
                    .type(FileType.WORK)
                    .url("http://example.com/document.pdf")
                    .build();
            when(fileMapper.selectById(TEST_AVATAR_ID)).thenReturn(wrongTypeFile);

            GlobalException exception = assertThrows(
                    GlobalException.class,
                    () -> enrollDomainService.validateAvatar(TEST_AVATAR_ID));
            assertEquals("文件类型不是头像", exception.getMessage());
        }
    }
}
