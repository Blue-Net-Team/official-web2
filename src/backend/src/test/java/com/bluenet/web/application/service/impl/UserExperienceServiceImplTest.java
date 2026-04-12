package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.experience.CreateExperienceRequestDTO;
import com.bluenet.web.api.dto.experience.ExperienceDTO;
import com.bluenet.web.api.dto.experience.UpdateExperienceRequestDTO;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.model.vo.ExperienceVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.service.UserExperienceDomainService;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserExperienceService 单元测试
 */
@DisplayName("UserExperienceServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class UserExperienceServiceImplTest {

    @Mock
    private UserExperienceDomainService userExperienceDomainService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private UserExperienceServiceImpl userExperienceService;

    private static final Long USER_ID = 1L;
    private static final Long EXPERIENCE_ID = 100L;

    private MockedStatic<UserCTX> userCTXMockedStatic;

    @BeforeEach
    void setUp() {
        userCTXMockedStatic = mockStatic(UserCTX.class);
    }

    @AfterEach
    void tearDown() {
        userCTXMockedStatic.close();
    }

    private void mockCurrentUser(Long userId) {
        UserVO userVO = UserVO.builder()
                .id(userId)
                .username("testUser")
                .build();
        userCTXMockedStatic.when(UserCTX::getCurrentUser).thenReturn(userVO);
    }

    private ExperienceVO createExperienceVO(Long id, ExperienceType type, String title) {
        return ExperienceVO.builder()
                .id(id)
                .type(type)
                .title(title)
                .startTime("2024.01")
                .endTime("2024.06")
                .content("{\"role\":\"开发者\",\"description\":\"测试内容\"}")
                .build();
    }

    @Nested
    @DisplayName("getExperiences 方法测试")
    class GetExperiencesTests {

        @Test
        @DisplayName("未认证用户：应抛出Unauthorized异常")
        void getExperiences_whenNotAuthenticated_shouldThrowUnauthorized() {
            userCTXMockedStatic.when(UserCTX::getCurrentUser).thenReturn(null);

            assertThrows(Unauthorized.class, () -> userExperienceService.getExperiences(null));
        }

        @Test
        @DisplayName("正常情况：应返回经历列表")
        void getExperiences_whenAuthenticated_shouldReturnList() {
            mockCurrentUser(USER_ID);

            List<ExperienceVO> experiences = Arrays.asList(
                    createExperienceVO(EXPERIENCE_ID, ExperienceType.PROJECT, "项目1"),
                    createExperienceVO(101L, ExperienceType.COMPETITION, "竞赛1"));

            when(userExperienceDomainService.getExperiences(USER_ID)).thenReturn(experiences);

            List<ExperienceDTO> result = userExperienceService.getExperiences(null);

            assertNotNull(result);
            assertEquals(2, result.size());
            verify(userExperienceDomainService).getExperiences(USER_ID);
        }

        @Test
        @DisplayName("按类型筛选：应调用正确的领域服务方法")
        void getExperiences_withType_shouldFilterByType() {
            mockCurrentUser(USER_ID);

            List<ExperienceVO> experiences = Arrays.asList(
                    createExperienceVO(EXPERIENCE_ID, ExperienceType.PROJECT, "项目1"));

            when(userExperienceDomainService.getExperiencesByType(USER_ID, ExperienceType.PROJECT))
                    .thenReturn(experiences);

            List<ExperienceDTO> result = userExperienceService.getExperiences("PROJECT");

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(userExperienceDomainService).getExperiencesByType(USER_ID, ExperienceType.PROJECT);
            verify(userExperienceDomainService, never()).getExperiences(anyLong());
        }

        @Test
        @DisplayName("无效类型：应抛出IllegalArgumentException")
        void getExperiences_withInvalidType_shouldThrowException() {
            mockCurrentUser(USER_ID);

            assertThrows(IllegalArgumentException.class, () -> userExperienceService.getExperiences("invalid_type"));
        }

        @Test
        @DisplayName("空列表：应返回空列表")
        void getExperiences_whenEmpty_shouldReturnEmptyList() {
            mockCurrentUser(USER_ID);

            when(userExperienceDomainService.getExperiences(USER_ID)).thenReturn(Collections.emptyList());

            List<ExperienceDTO> result = userExperienceService.getExperiences(null);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("createExperience 方法测试")
    class CreateExperienceTests {

        @Test
        @DisplayName("未认证用户：应抛出Unauthorized异常")
        void createExperience_whenNotAuthenticated_shouldThrowUnauthorized() {
            userCTXMockedStatic.when(UserCTX::getCurrentUser).thenReturn(null);

            CreateExperienceRequestDTO request = new CreateExperienceRequestDTO();
            request.setType("PROJECT");
            request.setName("测试项目");

            assertThrows(Unauthorized.class, () -> userExperienceService.createExperience(request));
        }

        @Test
        @DisplayName("创建项目经历：应成功创建")
        void createExperience_whenProjectType_shouldCreateSuccessfully() {
            mockCurrentUser(USER_ID);

            CreateExperienceRequestDTO request = new CreateExperienceRequestDTO();
            request.setType("PROJECT");
            request.setName("测试项目");
            request.setRole("开发者");
            request.setStartDate("2024.01");
            request.setEndDate("2024.06");
            request.setDescription("项目描述");

            ExperienceVO createdExperience = createExperienceVO(EXPERIENCE_ID, ExperienceType.PROJECT, "测试项目");

            when(
                    userExperienceDomainService.createExperience(
                            eq(USER_ID),
                            eq(ExperienceType.PROJECT),
                            eq("测试项目"),
                            eq("2024.01"),
                            eq("2024.06"),
                            anyString())).thenReturn(createdExperience);

            ExperienceDTO result = userExperienceService.createExperience(request);

            assertNotNull(result);
            assertEquals(String.valueOf(EXPERIENCE_ID), result.getId());
            assertEquals("PROJECT", result.getType());
            verify(userExperienceDomainService).createExperience(
                    eq(USER_ID),
                    eq(ExperienceType.PROJECT),
                    eq("测试项目"),
                    eq("2024.01"),
                    eq("2024.06"),
                    anyString());
        }

        @Test
        @DisplayName("创建竞赛经历：应成功创建")
        void createExperience_whenCompetitionType_shouldCreateSuccessfully() {
            mockCurrentUser(USER_ID);

            CreateExperienceRequestDTO request = new CreateExperienceRequestDTO();
            request.setType("COMPETITION");
            request.setName("全国大学生创新创业大赛");
            request.setDate("2024年8月");
            request.setLevel("国家级");
            request.setAward("一等奖");

            ExperienceVO createdExperience = createExperienceVO(
                    EXPERIENCE_ID,
                    ExperienceType.COMPETITION,
                    "全国大学生创新创业大赛");

            when(
                    userExperienceDomainService.createExperience(
                            eq(USER_ID),
                            eq(ExperienceType.COMPETITION),
                            eq("全国大学生创新创业大赛"),
                            eq("2024年8月"),
                            isNull(),
                            anyString())).thenReturn(createdExperience);

            ExperienceDTO result = userExperienceService.createExperience(request);

            assertNotNull(result);
            assertEquals("COMPETITION", result.getType());
        }

        @Test
        @DisplayName("创建实习经历：应使用公司名称作为标题")
        void createExperience_whenInternshipType_shouldUseCompanyNameAsTitle() {
            mockCurrentUser(USER_ID);

            CreateExperienceRequestDTO request = new CreateExperienceRequestDTO();
            request.setType("INTERNSHIP");
            request.setCompany("字节跳动");
            request.setPosition("后端开发实习生");
            request.setStartDate("2024.03");
            request.setEndDate("2024.09");

            ExperienceVO createdExperience = createExperienceVO(EXPERIENCE_ID, ExperienceType.INTERNSHIP, "字节跳动");

            when(
                    userExperienceDomainService.createExperience(
                            eq(USER_ID),
                            eq(ExperienceType.INTERNSHIP),
                            eq("字节跳动"),
                            eq("2024.03"),
                            eq("2024.09"),
                            anyString())).thenReturn(createdExperience);

            ExperienceDTO result = userExperienceService.createExperience(request);

            assertNotNull(result);
            assertEquals("INTERNSHIP", result.getType());
        }

        @Test
        @DisplayName("无效类型：应抛出IllegalArgumentException")
        void createExperience_withInvalidType_shouldThrowException() {
            mockCurrentUser(USER_ID);

            CreateExperienceRequestDTO request = new CreateExperienceRequestDTO();
            request.setType("invalid_type");
            request.setName("测试");

            assertThrows(IllegalArgumentException.class, () -> userExperienceService.createExperience(request));
        }
    }

    @Nested
    @DisplayName("updateExperience 方法测试")
    class UpdateExperienceTests {

        @Test
        @DisplayName("未认证用户：应抛出Unauthorized异常")
        void updateExperience_whenNotAuthenticated_shouldThrowUnauthorized() {
            userCTXMockedStatic.when(UserCTX::getCurrentUser).thenReturn(null);

            UpdateExperienceRequestDTO request = new UpdateExperienceRequestDTO();
            request.setName("更新后的项目");

            assertThrows(Unauthorized.class, () -> userExperienceService.updateExperience(EXPERIENCE_ID, request));
        }

        @Test
        @DisplayName("经历不存在：应抛出IllegalArgumentException")
        void updateExperience_whenNotExists_shouldThrowException() {
            mockCurrentUser(USER_ID);

            UpdateExperienceRequestDTO request = new UpdateExperienceRequestDTO();
            request.setName("更新后的项目");

            when(userExperienceDomainService.getExperienceById(EXPERIENCE_ID, USER_ID))
                    .thenReturn(Optional.empty());

            assertThrows(
                    IllegalArgumentException.class,
                    () -> userExperienceService.updateExperience(EXPERIENCE_ID, request));
        }

        @Test
        @DisplayName("正常更新：应成功更新经历")
        void updateExperience_whenExists_shouldUpdateSuccessfully() {
            mockCurrentUser(USER_ID);

            ExperienceVO existingExperience = createExperienceVO(EXPERIENCE_ID, ExperienceType.PROJECT, "原项目");
            ExperienceVO updatedExperience = createExperienceVO(EXPERIENCE_ID, ExperienceType.PROJECT, "更新后的项目");

            UpdateExperienceRequestDTO request = new UpdateExperienceRequestDTO();
            request.setName("更新后的项目");
            request.setRole("负责人");
            request.setStartDate("2024.01");
            request.setEndDate("2024.12");

            when(userExperienceDomainService.getExperienceById(EXPERIENCE_ID, USER_ID))
                    .thenReturn(Optional.of(existingExperience));
            when(
                    userExperienceDomainService.updateExperience(
                            eq(EXPERIENCE_ID),
                            eq(USER_ID),
                            eq("更新后的项目"),
                            eq("2024.01"),
                            eq("2024.12"),
                            anyString())).thenReturn(updatedExperience);

            ExperienceDTO result = userExperienceService.updateExperience(EXPERIENCE_ID, request);

            assertNotNull(result);
            verify(userExperienceDomainService).updateExperience(
                    eq(EXPERIENCE_ID),
                    eq(USER_ID),
                    eq("更新后的项目"),
                    eq("2024.01"),
                    eq("2024.12"),
                    anyString());
        }
    }

    @Nested
    @DisplayName("deleteExperience 方法测试")
    class DeleteExperienceTests {

        @Test
        @DisplayName("未认证用户：应抛出Unauthorized异常")
        void deleteExperience_whenNotAuthenticated_shouldThrowUnauthorized() {
            userCTXMockedStatic.when(UserCTX::getCurrentUser).thenReturn(null);

            assertThrows(Unauthorized.class, () -> userExperienceService.deleteExperience(EXPERIENCE_ID));
        }

        @Test
        @DisplayName("正常删除：应调用领域服务删除")
        void deleteExperience_whenAuthenticated_shouldCallDomainService() {
            mockCurrentUser(USER_ID);

            when(userExperienceDomainService.deleteExperience(EXPERIENCE_ID, USER_ID)).thenReturn(true);

            userExperienceService.deleteExperience(EXPERIENCE_ID);

            verify(userExperienceDomainService).deleteExperience(EXPERIENCE_ID, USER_ID);
        }
    }
}
