package com.bluenet.web.application.service.impl;

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

import com.bluenet.web.api.dto.enrollment.*;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.vo.EnrollBriefVO;
import com.bluenet.web.domain.model.vo.EnrollStatisticsVO;
import com.bluenet.web.domain.model.vo.EnrollVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.EnrollDomainService;

@DisplayName("EnrollServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class EnrollServiceImplTest {

    @Mock
    private EnrollDomainService enrollDomainService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EnrollServiceImpl enrollService;

    private static final Long TEST_ID = 1L;
    private static final String TEST_USERNAME = "张三";
    private static final String TEST_STUDENT_ID = "20210001001";
    private static final Long TEST_COLLEGE_ID = 1L;
    private static final String TEST_COLLEGE_NAME = "计算机学院";
    private static final String TEST_MAJOR = "计算机科学与技术";
    private static final Gender TEST_GENDER = Gender.MALE;
    private static final Direction TEST_DIRECTION = Direction.COMPUTER_VISION;
    private static final Long TEST_AVATAR_ID = 100L;
    private static final String TEST_REFERRAL_CODE = "ABC12345";

    private CreateEnrollmentRequestDTO createTestCreateRequest() {
        return CreateEnrollmentRequestDTO.builder()
                .username(TEST_USERNAME)
                .studentId(TEST_STUDENT_ID)
                .collegeId(TEST_COLLEGE_ID)
                .major(TEST_MAJOR)
                .gender(TEST_GENDER)
                .direction(TEST_DIRECTION)
                .avatarId(TEST_AVATAR_ID)
                .internalReferralCode(TEST_REFERRAL_CODE)
                .build();
    }

    private EnrollVO createTestEnrollVO() {
        return EnrollVO.builder()
                .id(TEST_ID)
                .username(TEST_USERNAME)
                .studentId(TEST_STUDENT_ID)
                .collegeId(TEST_COLLEGE_ID)
                .collegeName(TEST_COLLEGE_NAME)
                .major(TEST_MAJOR)
                .gender(TEST_GENDER)
                .direction(TEST_DIRECTION)
                .avatarFileId(TEST_AVATAR_ID)
                .status(EnrollStatus.PENDING)
                .internalReferralCode(TEST_REFERRAL_CODE)
                .build();
    }

    private EnrollBriefVO createTestEnrollBriefVO() {
        return EnrollBriefVO.builder()
                .id(TEST_ID)
                .username(TEST_USERNAME)
                .studentId(TEST_STUDENT_ID)
                .collegeName(TEST_COLLEGE_NAME)
                .major(TEST_MAJOR)
                .gender(TEST_GENDER)
                .direction(TEST_DIRECTION)
                .status(EnrollStatus.PENDING)
                .avatarFileId(TEST_AVATAR_ID)
                .build();
    }

    @Nested
    @DisplayName("createEnrollment 方法测试")
    class CreateEnrollmentTests {

        @Test
        @DisplayName("正常创建：应返回EnrollmentResultDTO")
        void createEnrollment_validRequest_shouldReturnResultDTO() {
            CreateEnrollmentRequestDTO request = createTestCreateRequest();

            when(enrollDomainService.createEnrollment(any(EnrollVO.class))).thenReturn(TEST_ID);

            EnrollmentResultDTO result = enrollService.createEnrollment(request);

            assertNotNull(result);
            assertEquals(TEST_ID, result.getId());
            assertEquals(TEST_USERNAME, result.getUsername());
            assertEquals(TEST_STUDENT_ID, result.getStudentId());
            assertEquals(TEST_DIRECTION, result.getDirection());
            assertEquals(EnrollStatus.PENDING, result.getStatus());
            assertTrue(result.isCreated());
            verify(enrollDomainService).validateAvatar(TEST_AVATAR_ID);
            verify(enrollDomainService).createEnrollment(any(EnrollVO.class));
        }

        @Test
        @DisplayName("无头像：应跳过头像验证")
        void createEnrollment_noAvatar_shouldSkipValidation() {
            CreateEnrollmentRequestDTO request = CreateEnrollmentRequestDTO.builder()
                    .username(TEST_USERNAME)
                    .studentId(TEST_STUDENT_ID)
                    .collegeId(TEST_COLLEGE_ID)
                    .major(TEST_MAJOR)
                    .gender(TEST_GENDER)
                    .direction(TEST_DIRECTION)
                    .avatarId(null)
                    .build();

            when(enrollDomainService.createEnrollment(any(EnrollVO.class))).thenReturn(TEST_ID);

            EnrollmentResultDTO result = enrollService.createEnrollment(request);

            assertNotNull(result);
            verify(enrollDomainService, never()).validateAvatar(any());
        }

        @Test
        @DisplayName("头像验证失败：应抛出异常")
        void createEnrollment_invalidAvatar_shouldThrowException() {
            CreateEnrollmentRequestDTO request = createTestCreateRequest();

            doThrow(new IllegalArgumentException("头像文件不存在"))
                    .when(enrollDomainService)
                    .validateAvatar(TEST_AVATAR_ID);

            assertThrows(IllegalArgumentException.class, () -> enrollService.createEnrollment(request));
            verify(enrollDomainService, never()).createEnrollment(any());
        }
    }

    @Nested
    @DisplayName("updateEnrollment 方法测试")
    class UpdateEnrollmentTests {

        @Test
        @DisplayName("正常更新：应返回EnrollmentBriefDTO")
        void updateEnrollment_validRequest_shouldReturnBriefDTO() {
            CreateEnrollmentRequestDTO request = createTestCreateRequest();
            EnrollVO existing = createTestEnrollVO();

            when(enrollDomainService.getEnrollmentByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.of(existing));

            EnrollmentBriefDTO result = enrollService.updateEnrollment(TEST_STUDENT_ID, request);

            assertNotNull(result);
            assertEquals(TEST_ID, result.getId());
            assertEquals(TEST_USERNAME, result.getUsername());
            verify(enrollDomainService).updateEnrollment(any(EnrollVO.class));
        }

        @Test
        @DisplayName("报名不存在：应抛出DataNotFound异常")
        void updateEnrollment_nonExistingEnroll_shouldThrowDataNotFound() {
            CreateEnrollmentRequestDTO request = createTestCreateRequest();

            when(enrollDomainService.getEnrollmentByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.empty());

            assertThrows(DataNotFound.class, () -> enrollService.updateEnrollment(TEST_STUDENT_ID, request));
        }

        @Test
        @DisplayName("更新头像：应验证新头像")
        void updateEnrollment_withNewAvatar_shouldValidateAvatar() {
            CreateEnrollmentRequestDTO request = createTestCreateRequest();
            EnrollVO existing = createTestEnrollVO();

            when(enrollDomainService.getEnrollmentByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.of(existing));

            enrollService.updateEnrollment(TEST_STUDENT_ID, request);

            verify(enrollDomainService).validateAvatar(TEST_AVATAR_ID);
        }
    }

    @Nested
    @DisplayName("getEnrollmentList 方法测试")
    class GetEnrollmentListTests {

        @Test
        @DisplayName("正常查询：应返回分页结果")
        void getEnrollmentList_validQuery_shouldReturnPagedResult() {
            EnrollmentListQueryDTO query = EnrollmentListQueryDTO.builder()
                    .page(0)
                    .size(10)
                    .build();

            List<EnrollBriefVO> voList = new ArrayList<>();
            voList.add(createTestEnrollBriefVO());
            Page<EnrollBriefVO> voPage = new PageImpl<>(voList, PageRequest.of(0, 10), 1);

            when(enrollDomainService.getEnrollmentList(null, null, null, PageRequest.of(0, 10)))
                    .thenReturn(voPage);

            Page<EnrollmentBriefDTO> result = enrollService.getEnrollmentList(query);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(1, result.getContent().size());
        }

        @Test
        @DisplayName("带筛选条件：应传递正确的参数")
        void getEnrollmentList_withFilters_shouldPassCorrectParams() {
            EnrollmentListQueryDTO query = EnrollmentListQueryDTO.builder()
                    .page(0)
                    .size(10)
                    .keyword("张三")
                    .status(EnrollStatus.PENDING)
                    .direction(TEST_DIRECTION)
                    .build();

            List<EnrollBriefVO> voList = new ArrayList<>();
            Page<EnrollBriefVO> voPage = new PageImpl<>(voList, PageRequest.of(0, 10), 0);

            when(
                    enrollDomainService
                            .getEnrollmentList("张三", EnrollStatus.PENDING, TEST_DIRECTION, PageRequest.of(0, 10)))
                                    .thenReturn(voPage);

            Page<EnrollmentBriefDTO> result = enrollService.getEnrollmentList(query);

            assertNotNull(result);
            verify(enrollDomainService)
                    .getEnrollmentList("张三", EnrollStatus.PENDING, TEST_DIRECTION, PageRequest.of(0, 10));
        }

        @Test
        @DisplayName("默认分页参数：应使用默认值")
        void getEnrollmentList_defaultParams_shouldUseDefaults() {
            EnrollmentListQueryDTO query = EnrollmentListQueryDTO.builder().build();

            List<EnrollBriefVO> voList = new ArrayList<>();
            Page<EnrollBriefVO> voPage = new PageImpl<>(voList, PageRequest.of(0, 20), 0);

            when(enrollDomainService.getEnrollmentList(null, null, null, PageRequest.of(0, 20)))
                    .thenReturn(voPage);

            Page<EnrollmentBriefDTO> result = enrollService.getEnrollmentList(query);

            assertNotNull(result);
            verify(enrollDomainService).getEnrollmentList(null, null, null, PageRequest.of(0, 20));
        }

        @Test
        @DisplayName("size超过100：应限制为100")
        void getEnrollmentList_largeSize_shouldLimitTo100() {
            EnrollmentListQueryDTO query = EnrollmentListQueryDTO.builder()
                    .page(0)
                    .size(200)
                    .build();

            List<EnrollBriefVO> voList = new ArrayList<>();
            Page<EnrollBriefVO> voPage = new PageImpl<>(voList, PageRequest.of(0, 100), 0);

            when(enrollDomainService.getEnrollmentList(null, null, null, PageRequest.of(0, 100)))
                    .thenReturn(voPage);

            Page<EnrollmentBriefDTO> result = enrollService.getEnrollmentList(query);

            assertNotNull(result);
            verify(enrollDomainService).getEnrollmentList(null, null, null, PageRequest.of(0, 100));
        }
    }

    @Nested
    @DisplayName("getEnrollmentDetail 方法测试")
    class GetEnrollmentDetailTests {

        @Test
        @DisplayName("正常查询：应返回EnrollmentDetailDTO")
        void getEnrollmentDetail_existingId_shouldReturnDetailDTO() {
            EnrollVO vo = createTestEnrollVO();

            when(enrollDomainService.getEnrollmentById(TEST_ID)).thenReturn(Optional.of(vo));

            EnrollmentDetailDTO result = enrollService.getEnrollmentDetail(TEST_ID);

            assertNotNull(result);
            assertEquals(TEST_ID, result.getId());
            assertEquals(TEST_USERNAME, result.getUsername());
            assertEquals(TEST_STUDENT_ID, result.getStudentId());
            assertEquals(TEST_COLLEGE_ID, result.getCollegeId());
            assertEquals(TEST_COLLEGE_NAME, result.getCollegeName());
        }

        @Test
        @DisplayName("报名不存在：应抛出DataNotFound异常")
        void getEnrollmentDetail_nonExistingId_shouldThrowDataNotFound() {
            when(enrollDomainService.getEnrollmentById(TEST_ID)).thenReturn(Optional.empty());

            assertThrows(DataNotFound.class, () -> enrollService.getEnrollmentDetail(TEST_ID));
        }
    }

    @Nested
    @DisplayName("approveEnrollment 方法测试")
    class ApproveEnrollmentTests {

        @Test
        @DisplayName("正常审核通过：应返回审核结果")
        void approveEnrollment_normalCase_shouldReturnResult() {
            EnrollVO vo = createTestEnrollVO();
            UserVO createdUser = UserVO.builder().id(999L).studentId(TEST_STUDENT_ID).build();

            when(enrollDomainService.getEnrollmentById(TEST_ID)).thenReturn(Optional.of(vo));
            when(userRepository.findByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.of(createdUser));

            EnrollmentApprovalResultDTO result = enrollService.approveEnrollment(TEST_ID);

            assertNotNull(result);
            assertEquals(TEST_ID, result.getId());
            assertEquals(EnrollStatus.APPROVED, result.getStatus());
            assertEquals(999L, result.getCreatedUserId());
            verify(enrollDomainService).approveEnrollment(TEST_ID, null);
        }

        @Test
        @DisplayName("approve with assessment grade year: should pass override to domain")
        void approveEnrollment_withAssessmentGradeYear_shouldPassOverride() {
            EnrollVO vo = createTestEnrollVO();
            ApproveEnrollmentRequestDTO request = ApproveEnrollmentRequestDTO.builder()
                    .assessmentGradeYear(2024)
                    .build();

            when(enrollDomainService.getEnrollmentById(TEST_ID)).thenReturn(Optional.of(vo));
            when(userRepository.findByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.empty());

            EnrollmentApprovalResultDTO result = enrollService.approveEnrollment(TEST_ID, request);

            assertNotNull(result);
            verify(enrollDomainService).approveEnrollment(TEST_ID, 2024);
        }

        @Test
        @DisplayName("报名不存在：应抛出DataNotFound异常")
        void approveEnrollment_nonExistingId_shouldThrowDataNotFound() {
            when(enrollDomainService.getEnrollmentById(TEST_ID)).thenReturn(Optional.empty());

            assertThrows(DataNotFound.class, () -> enrollService.approveEnrollment(TEST_ID));
        }

        @Test
        @DisplayName("用户未创建：createdUserId应为null")
        void approveEnrollment_userNotCreated_shouldReturnNullUserId() {
            EnrollVO vo = createTestEnrollVO();

            when(enrollDomainService.getEnrollmentById(TEST_ID)).thenReturn(Optional.of(vo));
            when(userRepository.findByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.empty());

            EnrollmentApprovalResultDTO result = enrollService.approveEnrollment(TEST_ID);

            assertNotNull(result);
            assertNull(result.getCreatedUserId());
        }
    }

    @Nested
    @DisplayName("rejectEnrollment 方法测试")
    class RejectEnrollmentTests {

        @Test
        @DisplayName("正常拒绝：应返回审核结果")
        void rejectEnrollment_normalCase_shouldReturnResult() {
            RejectEnrollmentRequestDTO request = RejectEnrollmentRequestDTO.builder()
                    .reason("不符合条件")
                    .build();

            EnrollmentApprovalResultDTO result = enrollService.rejectEnrollment(TEST_ID, request);

            assertNotNull(result);
            assertEquals(TEST_ID, result.getId());
            assertEquals(EnrollStatus.REJECTED, result.getStatus());
            verify(enrollDomainService).rejectEnrollment(TEST_ID, "不符合条件");
        }

        @Test
        @DisplayName("无拒绝原因：应正常处理")
        void rejectEnrollment_nullReason_shouldProcessNormally() {
            EnrollmentApprovalResultDTO result = enrollService.rejectEnrollment(TEST_ID, null);

            assertNotNull(result);
            assertEquals(EnrollStatus.REJECTED, result.getStatus());
            verify(enrollDomainService).rejectEnrollment(TEST_ID, null);
        }

        @Test
        @DisplayName("空请求对象：应正常处理")
        void rejectEnrollment_nullRequest_shouldProcessNormally() {
            EnrollmentApprovalResultDTO result = enrollService.rejectEnrollment(TEST_ID, null);

            assertNotNull(result);
            assertEquals(EnrollStatus.REJECTED, result.getStatus());
        }
    }

    @Nested
    @DisplayName("getStatistics 方法测试")
    class GetStatisticsTests {

        @Test
        @DisplayName("正常查询：应返回统计数据")
        void getStatistics_normalCase_shouldReturnStatistics() {
            EnrollStatisticsVO vo = EnrollStatisticsVO.builder()
                    .total(10L)
                    .byStatus(Map.of("pending", 5L, "approved", 3L, "rejected", 2L))
                    .byDirection(Map.of(Direction.COMPUTER_VISION, 4L, Direction.EMBEDDED, 3L))
                    .build();

            when(enrollDomainService.getStatistics()).thenReturn(vo);

            EnrollmentStatisticsDTO result = enrollService.getStatistics();

            assertNotNull(result);
            assertEquals(10L, result.getTotal());
            assertNotNull(result.getByStatus());
            assertNotNull(result.getByDirection());
        }

        @Test
        @DisplayName("无数据：应返回零统计")
        void getStatistics_noData_shouldReturnZeroStatistics() {
            EnrollStatisticsVO vo = EnrollStatisticsVO.builder()
                    .total(0L)
                    .byStatus(new HashMap<>())
                    .byDirection(new HashMap<>())
                    .build();

            when(enrollDomainService.getStatistics()).thenReturn(vo);

            EnrollmentStatisticsDTO result = enrollService.getStatistics();

            assertNotNull(result);
            assertEquals(0L, result.getTotal());
        }
    }
}
