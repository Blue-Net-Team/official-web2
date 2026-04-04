package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.assessment_time.AssessmentProgressDTO;
import com.bluenet.web.api.dto.assessment_time.AssessmentTimeDTO;
import com.bluenet.web.api.dto.assessment_time.CreateAssessmentTimeRequestDTO;
import com.bluenet.web.api.dto.assessment_time.UpdateAssessmentTimeRequestDTO;
import com.bluenet.web.application.converter.AssessmentTimeConverter;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.service.AssessmentTimeDomainService;
import com.bluenet.web.infrastructure.security.RoleType;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * AssessmentTimeServiceImpl 单元测试
 * <p>
 * 测试考核时间应用服务的协调逻辑
 * </p>
 */
@DisplayName("AssessmentTimeServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentTimeServiceImplTest {

    @Mock
    private AssessmentTimeDomainService assessmentTimeDomainService;

    @Mock
    private AssessmentTimeConverter assessmentTimeConverter;

    @Mock
    private AssessmentTimeRepository assessmentTimeRepository;

    @Mock
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Mock
    private AssessmentAnswerRepository assessmentAnswerRepository;

    @InjectMocks
    private AssessmentTimeServiceImpl assessmentTimeService;

    private static final Long TEST_ID = 1L;
    private LocalDateTime futureStart = LocalDateTime.of(2099, 1, 1, 9, 0);
    private LocalDateTime futureEnd = LocalDateTime.of(2099, 1, 1, 11, 0);

    private AssessmentTimeVO createTestVO() {
        return AssessmentTimeVO.builder()
                .id(TEST_ID)
                .direction(Direction.COMPUTER_VISION)
                .epoch(1)
                .grade(1)
                .startTime(futureStart)
                .endTime(futureEnd)
                .timeLimit(true)
                .timeLimitMinutes(120)
                .build();
    }

    private AssessmentTimeDTO createTestDTO() {
        return AssessmentTimeDTO.builder()
                .id(TEST_ID)
                .direction(Direction.COMPUTER_VISION)
                .epoch(1)
                .grade(1)
                .startTime(futureStart)
                .endTime(futureEnd)
                .timeLimit(true)
                .timeLimitMinutes(120)
                .build();
    }

    // ==================== createAssessmentTime 测试 ====================

    @Nested
    @DisplayName("createAssessmentTime 方法测试")
    class CreateTests {

        @Test
        @DisplayName("正常创建：应返回DTO")
        void create_validRequest_shouldReturnDTO() {
            CreateAssessmentTimeRequestDTO request = CreateAssessmentTimeRequestDTO.builder()
                    .direction(Direction.COMPUTER_VISION)
                    .epoch(1)
                    .grade(1)
                    .startTime(futureStart)
                    .endTime(futureEnd)
                    .timeLimit(true)
                    .timeLimitMinutes(120)
                    .build();

            AssessmentTimeVO createdVO = createTestVO();
            AssessmentTimeDTO expectedDTO = createTestDTO();

            when(assessmentTimeDomainService.create(any(AssessmentTimeVO.class))).thenReturn(TEST_ID);
            when(assessmentTimeDomainService.getById(TEST_ID)).thenReturn(Optional.of(createdVO));
            when(assessmentTimeConverter.convertToDTO(createdVO)).thenReturn(expectedDTO);

            AssessmentTimeDTO result = assessmentTimeService.createAssessmentTime(request);

            assertNotNull(result);
            assertEquals(TEST_ID, result.getId());
            verify(assessmentTimeDomainService).create(any(AssessmentTimeVO.class));
            verify(assessmentTimeDomainService).getById(TEST_ID);
        }

        @Test
        @DisplayName("创建后查询为空：应抛出IllegalStateException")
        void create_createFailed_shouldThrow() {
            CreateAssessmentTimeRequestDTO request = CreateAssessmentTimeRequestDTO.builder()
                    .direction(Direction.COMPUTER_VISION)
                    .epoch(1)
                    .grade(1)
                    .startTime(futureStart)
                    .endTime(futureEnd)
                    .timeLimit(false)
                    .build();

            when(assessmentTimeDomainService.create(any(AssessmentTimeVO.class))).thenReturn(TEST_ID);
            when(assessmentTimeDomainService.getById(TEST_ID)).thenReturn(Optional.empty());

            assertThrows(
                    IllegalStateException.class,
                    () -> assessmentTimeService.createAssessmentTime(request));
        }

        @Test
        @DisplayName("领域服务校验失败：应抛出IllegalArgumentException")
        void create_domainValidationFails_shouldThrow() {
            CreateAssessmentTimeRequestDTO request = CreateAssessmentTimeRequestDTO.builder()
                    .direction(Direction.COMPUTER_VISION)
                    .epoch(1)
                    .grade(1)
                    .startTime(futureStart)
                    .endTime(futureEnd)
                    .timeLimit(true)
                    .build();

            when(assessmentTimeDomainService.create(any(AssessmentTimeVO.class)))
                    .thenThrow(new IllegalArgumentException("该方向轮次年级的考核时间已存在"));

            assertThrows(
                    IllegalArgumentException.class,
                    () -> assessmentTimeService.createAssessmentTime(request));
        }
    }

    // ==================== updateAssessmentTime 测试 ====================

    @Nested
    @DisplayName("updateAssessmentTime 方法测试")
    class UpdateTests {

        @Test
        @DisplayName("正常更新：应返回DTO")
        void update_validRequest_shouldReturnDTO() {
            UpdateAssessmentTimeRequestDTO request = UpdateAssessmentTimeRequestDTO.builder()
                    .timeLimitMinutes(90)
                    .build();

            AssessmentTimeVO updatedVO = createTestVO();
            AssessmentTimeDTO expectedDTO = createTestDTO();
            expectedDTO.setTimeLimitMinutes(90);

            when(assessmentTimeDomainService.getById(TEST_ID)).thenReturn(Optional.of(updatedVO));
            when(assessmentTimeConverter.convertToDTO(updatedVO)).thenReturn(expectedDTO);

            AssessmentTimeDTO result = assessmentTimeService.updateAssessmentTime(TEST_ID, request);

            assertNotNull(result);
            verify(assessmentTimeDomainService).update(any(AssessmentTimeVO.class));
        }

        @Test
        @DisplayName("更新后查询为空：应抛出IllegalStateException")
        void update_failed_shouldThrow() {
            UpdateAssessmentTimeRequestDTO request = UpdateAssessmentTimeRequestDTO.builder()
                    .timeLimitMinutes(90)
                    .build();

            when(assessmentTimeDomainService.getById(TEST_ID)).thenReturn(Optional.empty());

            assertThrows(
                    IllegalStateException.class,
                    () -> assessmentTimeService.updateAssessmentTime(TEST_ID, request));
        }
    }

    // ==================== deleteAssessmentTime 测试 ====================

    @Nested
    @DisplayName("deleteAssessmentTime 方法测试")
    class DeleteTests {

        @Test
        @DisplayName("正常删除：应成功")
        void delete_valid_shouldSucceed() {
            doNothing().when(assessmentTimeDomainService).delete(TEST_ID);

            assessmentTimeService.deleteAssessmentTime(TEST_ID);

            verify(assessmentTimeDomainService).delete(TEST_ID);
        }

        @Test
        @DisplayName("有关联题目：应抛出IllegalStateException")
        void delete_withQuestions_shouldThrow() {
            doThrow(new IllegalStateException("存在关联的考核题目，需先删除相关题目"))
                    .when(assessmentTimeDomainService)
                    .delete(TEST_ID);

            assertThrows(
                    IllegalStateException.class,
                    () -> assessmentTimeService.deleteAssessmentTime(TEST_ID));
        }
    }

    // ==================== listAssessmentTimes 测试 ====================

    @Nested
    @DisplayName("listAssessmentTimes 方法测试")
    class ListTests {

        @Test
        @DisplayName("方向管理员以上角色：应返回全部数据")
        void list_directionAdmin_shouldReturnAll() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class);
                    MockedStatic<RoleType> mockedRoleType = mockStatic(RoleType.class)) {

                com.bluenet.web.domain.model.vo.UserVO userVO = com.bluenet.web.domain.model.vo.UserVO.builder()
                        .id(1L)
                        .roleName("DIRECTION_ADMIN")
                        .build();
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(userVO);
                mockedRoleType.when(() -> RoleType.fromName("DIRECTION_ADMIN")).thenReturn(RoleType.DIRECTION_ADMIN);

                List<AssessmentTimeVO> voList = List.of(createTestVO());
                Page<AssessmentTimeVO> voPage = new PageImpl<>(voList);
                when(assessmentTimeRepository.findByFilters(isNull(), isNull(), any()))
                        .thenReturn(voPage);
                when(assessmentTimeConverter.convertToDTO(any())).thenReturn(createTestDTO());

                PageDTO<AssessmentTimeDTO> result = assessmentTimeService.listAssessmentTimes(0, 5);

                assertNotNull(result);
                assertEquals(1, result.getContent().size());
                verify(assessmentTimeRepository).findByFilters(isNull(), isNull(), any());
            }
        }

        @Test
        @DisplayName("MEMBER角色：应按方向过滤")
        void list_member_shouldFilterByDirection() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {

                com.bluenet.web.domain.model.vo.UserVO userVO = com.bluenet.web.domain.model.vo.UserVO.builder()
                        .id(2L)
                        .roleName("MEMBER")
                        .direction(Direction.COMPUTER_VISION)
                        .build();
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(userVO);

                List<AssessmentTimeVO> voList = List.of(createTestVO());
                Page<AssessmentTimeVO> voPage = new PageImpl<>(voList);
                when(assessmentTimeRepository.findByFilters(eq(Direction.COMPUTER_VISION), isNull(), any()))
                        .thenReturn(voPage);
                when(assessmentTimeConverter.convertToDTO(any())).thenReturn(createTestDTO());

                PageDTO<AssessmentTimeDTO> result = assessmentTimeService.listAssessmentTimes(0, 5);

                assertNotNull(result);
                verify(assessmentTimeRepository).findByFilters(eq(Direction.COMPUTER_VISION), isNull(), any());
            }
        }

        @Test
        @DisplayName("CANDIDATE角色：应按方向和年级过滤")
        void list_candidate_shouldFilterByDirectionAndGrade() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {

                com.bluenet.web.domain.model.vo.UserVO userVO = com.bluenet.web.domain.model.vo.UserVO.builder()
                        .id(3L)
                        .roleName("CANDIDATE")
                        .direction(Direction.COMPUTER_VISION)
                        .studentId("2024123456")
                        .build();
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(userVO);

                List<AssessmentTimeVO> voList = List.of(createTestVO());
                Page<AssessmentTimeVO> voPage = new PageImpl<>(voList);
                when(assessmentTimeRepository.findByFilters(eq(Direction.COMPUTER_VISION), any(Integer.class), any()))
                        .thenReturn(voPage);
                when(assessmentTimeConverter.convertToDTO(any())).thenReturn(createTestDTO());

                PageDTO<AssessmentTimeDTO> result = assessmentTimeService.listAssessmentTimes(0, 5);

                assertNotNull(result);
                verify(assessmentTimeRepository)
                        .findByFilters(eq(Direction.COMPUTER_VISION), any(Integer.class), any());
            }
        }

        @Test
        @DisplayName("未登录用户：应返回全部数据")
        void list_noUser_shouldReturnAll() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(null);

                Page<AssessmentTimeVO> voPage = new PageImpl<>(new ArrayList<>());
                when(assessmentTimeRepository.findByFilters(isNull(), isNull(), any()))
                        .thenReturn(voPage);

                PageDTO<AssessmentTimeDTO> result = assessmentTimeService.listAssessmentTimes(0, 5);

                assertNotNull(result);
                assertTrue(result.getContent().isEmpty());
                verify(assessmentTimeRepository).findByFilters(isNull(), isNull(), any());
            }
        }

        @Test
        @DisplayName("默认分页参数：page=0, size=5")
        void list_defaultParams_shouldUseDefaults() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(null);

                Page<AssessmentTimeVO> voPage = new PageImpl<>(new ArrayList<>());
                when(assessmentTimeRepository.findByFilters(isNull(), isNull(), any()))
                        .thenReturn(voPage);

                assessmentTimeService.listAssessmentTimes(null, null);

                verify(assessmentTimeRepository).findByFilters(isNull(), isNull(), eq(PageRequest.of(0, 5)));
            }
        }
    }

    // ==================== listAssessmentTimesForUser 测试 ====================

    @Nested
    @DisplayName("listAssessmentTimesForUser 方法测试")
    class ListForUserTests {

        @Test
        @DisplayName("已登录用户：应为每个DTO填充进度数据")
        void listForUser_authenticatedUser_shouldPopulateProgress() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                UserVO userVO = UserVO.builder()
                        .id(1L)
                        .roleName("CANDIDATE")
                        .direction(Direction.COMPUTER_VISION)
                        .studentId("2024123456")
                        .build();
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(userVO);

                List<AssessmentTimeVO> voList = List.of(createTestVO());
                Page<AssessmentTimeVO> voPage = new PageImpl<>(voList);
                when(assessmentTimeRepository.findByFilters(eq(Direction.COMPUTER_VISION), any(Integer.class), any()))
                        .thenReturn(voPage);

                AssessmentTimeDTO dto = createTestDTO();
                when(assessmentTimeConverter.convertToDTO(any())).thenReturn(dto);
                when(assessmentQuestionRepository.countByAssessmentTimeId(TEST_ID)).thenReturn(8);
                when(assessmentAnswerRepository.countByUserIdAndAssessmentTimeId(1L, TEST_ID)).thenReturn(5);

                PageDTO<AssessmentTimeDTO> result = assessmentTimeService.listAssessmentTimesForUser(0, 5);

                assertNotNull(result);
                assertEquals(1, result.getContent().size());
                assertEquals(8, result.getContent().get(0).getTotalQuestions());
                assertEquals(5, result.getContent().get(0).getCompletedQuestions());
                verify(assessmentQuestionRepository).countByAssessmentTimeId(TEST_ID);
                verify(assessmentAnswerRepository).countByUserIdAndAssessmentTimeId(1L, TEST_ID);
            }
        }

        @Test
        @DisplayName("未登录用户：不应填充进度数据")
        void listForUser_noUser_shouldNotPopulateProgress() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(null);

                Page<AssessmentTimeVO> voPage = new PageImpl<>(new ArrayList<>());
                when(assessmentTimeRepository.findByFilters(isNull(), isNull(), any()))
                        .thenReturn(voPage);

                PageDTO<AssessmentTimeDTO> result = assessmentTimeService.listAssessmentTimesForUser(0, 5);

                assertNotNull(result);
                assertTrue(result.getContent().isEmpty());
                verifyNoInteractions(assessmentQuestionRepository);
                verifyNoInteractions(assessmentAnswerRepository);
            }
        }

        @Test
        @DisplayName("多个考核时间：应为每个分别填充进度")
        void listForUser_multipleTimes_shouldPopulateEach() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                UserVO userVO = UserVO.builder()
                        .id(1L)
                        .roleName("MEMBER")
                        .direction(Direction.COMPUTER_VISION)
                        .build();
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(userVO);

                AssessmentTimeVO vo1 = AssessmentTimeVO.builder()
                        .id(1L)
                        .direction(Direction.COMPUTER_VISION)
                        .epoch(1)
                        .grade(1)
                        .startTime(futureStart)
                        .endTime(futureEnd)
                        .timeLimit(false)
                        .build();
                AssessmentTimeVO vo2 = AssessmentTimeVO.builder()
                        .id(2L)
                        .direction(Direction.COMPUTER_VISION)
                        .epoch(2)
                        .grade(1)
                        .startTime(futureStart)
                        .endTime(futureEnd)
                        .timeLimit(true)
                        .timeLimitMinutes(90)
                        .build();
                Page<AssessmentTimeVO> voPage = new PageImpl<>(List.of(vo1, vo2));
                when(assessmentTimeRepository.findByFilters(eq(Direction.COMPUTER_VISION), isNull(), any()))
                        .thenReturn(voPage);

                AssessmentTimeDTO dto1 = AssessmentTimeDTO.builder().id(1L).build();
                AssessmentTimeDTO dto2 = AssessmentTimeDTO.builder().id(2L).build();
                when(assessmentTimeConverter.convertToDTO(vo1)).thenReturn(dto1);
                when(assessmentTimeConverter.convertToDTO(vo2)).thenReturn(dto2);
                when(assessmentQuestionRepository.countByAssessmentTimeId(1L)).thenReturn(10);
                when(assessmentQuestionRepository.countByAssessmentTimeId(2L)).thenReturn(5);
                when(assessmentAnswerRepository.countByUserIdAndAssessmentTimeId(1L, 1L)).thenReturn(3);
                when(assessmentAnswerRepository.countByUserIdAndAssessmentTimeId(1L, 2L)).thenReturn(0);

                PageDTO<AssessmentTimeDTO> result = assessmentTimeService.listAssessmentTimesForUser(0, 5);

                assertEquals(2, result.getContent().size());
                assertEquals(10, result.getContent().get(0).getTotalQuestions());
                assertEquals(3, result.getContent().get(0).getCompletedQuestions());
                assertEquals(5, result.getContent().get(1).getTotalQuestions());
                assertEquals(0, result.getContent().get(1).getCompletedQuestions());
            }
        }
    }

    // ==================== getAssessmentProgress 测试 ====================

    @Nested
    @DisplayName("getAssessmentProgress 方法测试")
    class GetAssessmentProgressTests {

        @Test
        @DisplayName("考核时间存在：应返回进度数据")
        void getProgress_existing_shouldReturnProgress() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                UserVO userVO = UserVO.builder()
                        .id(1L)
                        .roleName("CANDIDATE")
                        .build();
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(userVO);

                when(assessmentTimeDomainService.getById(TEST_ID)).thenReturn(Optional.of(createTestVO()));
                when(assessmentQuestionRepository.countByAssessmentTimeId(TEST_ID)).thenReturn(8);
                when(assessmentAnswerRepository.countByUserIdAndAssessmentTimeId(1L, TEST_ID)).thenReturn(5);

                AssessmentProgressDTO result = assessmentTimeService.getAssessmentProgress(TEST_ID);

                assertNotNull(result);
                assertEquals(TEST_ID, result.getAssessmentTimeId());
                assertEquals(8, result.getTotalQuestions());
                assertEquals(5, result.getCompletedQuestions());
            }
        }

        @Test
        @DisplayName("考核时间不存在：应抛出IllegalArgumentException")
        void getProgress_notExisting_shouldThrow() {
            when(assessmentTimeDomainService.getById(999L)).thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> assessmentTimeService.getAssessmentProgress(999L));
            assertEquals("考核时间不存在", ex.getMessage());
        }

        @Test
        @DisplayName("无题目：totalQuestions 应为 0")
        void getProgress_noQuestions_shouldReturnZeroTotal() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                UserVO userVO = UserVO.builder().id(1L).roleName("CANDIDATE").build();
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(userVO);

                when(assessmentTimeDomainService.getById(TEST_ID)).thenReturn(Optional.of(createTestVO()));
                when(assessmentQuestionRepository.countByAssessmentTimeId(TEST_ID)).thenReturn(0);
                when(assessmentAnswerRepository.countByUserIdAndAssessmentTimeId(1L, TEST_ID)).thenReturn(0);

                AssessmentProgressDTO result = assessmentTimeService.getAssessmentProgress(TEST_ID);

                assertEquals(0, result.getTotalQuestions());
                assertEquals(0, result.getCompletedQuestions());
            }
        }

        @Test
        @DisplayName("未登录用户：completedQuestions 应为 0")
        void getProgress_noUser_shouldReturnZeroCompleted() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(null);

                when(assessmentTimeDomainService.getById(TEST_ID)).thenReturn(Optional.of(createTestVO()));
                when(assessmentQuestionRepository.countByAssessmentTimeId(TEST_ID)).thenReturn(10);

                AssessmentProgressDTO result = assessmentTimeService.getAssessmentProgress(TEST_ID);

                assertEquals(10, result.getTotalQuestions());
                assertEquals(0, result.getCompletedQuestions());
                verifyNoInteractions(assessmentAnswerRepository);
            }
        }
    }
}
