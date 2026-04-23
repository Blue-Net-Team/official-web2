package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.UserExperienceResult;
import com.bluenet.web.application.command.userexperience.UserExperienceCommands;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.entity.UserExperience;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.UserExperienceRepository;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserExperienceAppServiceImpl 单元测试
 */
@DisplayName("UserExperienceAppServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class UserExperienceAppServiceImplTest {

    @Mock
    private UserExperienceRepository userExperienceRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private UserExperienceAppServiceImpl userExperienceAppService;

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

    private UserExperience createExperience(Long id, ExperienceType type, String title) {
        return UserExperience.reconstruct(
                id,
                USER_ID,
                type,
                title,
                "{\"role\":\"开发者\",\"description\":\"测试内容\"}",
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 6, 1, 0, 0));
    }

    @Nested
    @DisplayName("getExperiences 方法测试")
    class GetExperiencesTests {

        @Test
        @DisplayName("未认证用户：应抛出Unauthorized异常")
        void getExperiences_whenNotAuthenticated_shouldThrowUnauthorized() {
            userCTXMockedStatic.when(UserCTX::getCurrentUser).thenReturn(null);

            assertThrows(Unauthorized.class, () -> userExperienceAppService.getExperiences(null));
        }

        @Test
        @DisplayName("正常情况：应返回经历列表")
        void getExperiences_whenAuthenticated_shouldReturnList() {
            mockCurrentUser(USER_ID);

            List<UserExperience> experiences = Arrays.asList(
                    createExperience(EXPERIENCE_ID, ExperienceType.PROJECT, "项目1"),
                    createExperience(101L, ExperienceType.COMPETITION, "竞赛1"));

            when(userExperienceRepository.findByUserId(USER_ID)).thenReturn(experiences);

            List<UserExperienceResult> result = userExperienceAppService.getExperiences(null);

            assertNotNull(result);
            assertEquals(2, result.size());
            verify(userExperienceRepository).findByUserId(USER_ID);
        }

        @Test
        @DisplayName("按类型筛选：应调用正确的仓储方法")
        void getExperiences_withType_shouldFilterByType() {
            mockCurrentUser(USER_ID);

            List<UserExperience> experiences = Arrays.asList(
                    createExperience(EXPERIENCE_ID, ExperienceType.PROJECT, "项目1"));

            when(userExperienceRepository.findByUserIdAndType(USER_ID, ExperienceType.PROJECT))
                    .thenReturn(experiences);

            List<UserExperienceResult> result = userExperienceAppService.getExperiences("PROJECT");

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(userExperienceRepository).findByUserIdAndType(USER_ID, ExperienceType.PROJECT);
            verify(userExperienceRepository, never()).findByUserId(anyLong());
        }

        @Test
        @DisplayName("无效类型：应抛出IllegalArgumentException")
        void getExperiences_withInvalidType_shouldThrowException() {
            mockCurrentUser(USER_ID);

            assertThrows(IllegalArgumentException.class, () -> userExperienceAppService.getExperiences("invalid_type"));
        }

        @Test
        @DisplayName("空列表：应返回空列表")
        void getExperiences_whenEmpty_shouldReturnEmptyList() {
            mockCurrentUser(USER_ID);

            when(userExperienceRepository.findByUserId(USER_ID)).thenReturn(Collections.emptyList());

            List<UserExperienceResult> result = userExperienceAppService.getExperiences(null);

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

            UserExperienceCommands.CreateExperienceCommand command = new UserExperienceCommands.CreateExperienceCommand(
                    "PROJECT", "测试项目", null, null, null, null, null, null, null, null, null, null, null, null, null,
                    null, null);

            assertThrows(Unauthorized.class, () -> userExperienceAppService.createExperience(command));
        }

        @Test
        @DisplayName("创建项目经历：应成功创建")
        void createExperience_whenProjectType_shouldCreateSuccessfully() {
            mockCurrentUser(USER_ID);

            UserExperienceCommands.CreateExperienceCommand command = new UserExperienceCommands.CreateExperienceCommand(
                    "PROJECT", "测试项目", "开发者", "2024.01", "2024.06", "项目描述",
                    null, null, null, null, null, null, null, null, null, null, null);

            doAnswer(invocation -> {
                UserExperience exp = invocation.getArgument(0);
                exp.setId(EXPERIENCE_ID);
                return null;
            }).when(userExperienceRepository).save(any(UserExperience.class));

            UserExperienceResult result = userExperienceAppService.createExperience(command);

            assertNotNull(result);
            assertEquals(EXPERIENCE_ID, result.id());
            assertEquals(ExperienceType.PROJECT, result.type());
            assertEquals("测试项目", result.title());
            verify(userExperienceRepository).save(any(UserExperience.class));
        }

        @Test
        @DisplayName("创建竞赛经历：应成功创建")
        void createExperience_whenCompetitionType_shouldCreateSuccessfully() {
            mockCurrentUser(USER_ID);

            UserExperienceCommands.CreateExperienceCommand command = new UserExperienceCommands.CreateExperienceCommand(
                    "COMPETITION", "全国大学生创新创业大赛", null, null, null, null,
                    null, null, "2024年8月", "国家级", "一等奖", null, null, null, null, null, null);

            doAnswer(invocation -> {
                UserExperience exp = invocation.getArgument(0);
                exp.setId(EXPERIENCE_ID);
                return null;
            }).when(userExperienceRepository).save(any(UserExperience.class));

            UserExperienceResult result = userExperienceAppService.createExperience(command);

            assertNotNull(result);
            assertEquals(ExperienceType.COMPETITION, result.type());
        }

        @Test
        @DisplayName("创建实习经历：应使用公司名称作为标题")
        void createExperience_whenInternshipType_shouldUseCompanyNameAsTitle() {
            mockCurrentUser(USER_ID);

            UserExperienceCommands.CreateExperienceCommand command = new UserExperienceCommands.CreateExperienceCommand(
                    "INTERNSHIP", null, null, "2024.03", "2024.09", null,
                    null, null, null, null, null, null, null, "字节跳动", "后端开发实习生", null, null);

            doAnswer(invocation -> {
                UserExperience exp = invocation.getArgument(0);
                exp.setId(EXPERIENCE_ID);
                return null;
            }).when(userExperienceRepository).save(any(UserExperience.class));

            UserExperienceResult result = userExperienceAppService.createExperience(command);

            assertNotNull(result);
            assertEquals(ExperienceType.INTERNSHIP, result.type());
            assertEquals("字节跳动", result.title());
        }

        @Test
        @DisplayName("无效类型：应抛出IllegalArgumentException")
        void createExperience_withInvalidType_shouldThrowException() {
            mockCurrentUser(USER_ID);

            UserExperienceCommands.CreateExperienceCommand command = new UserExperienceCommands.CreateExperienceCommand(
                    "invalid_type", "测试", null, null, null, null, null, null, null, null, null, null, null, null, null,
                    null, null);

            assertThrows(IllegalArgumentException.class, () -> userExperienceAppService.createExperience(command));
        }
    }

    @Nested
    @DisplayName("updateExperience 方法测试")
    class UpdateExperienceTests {

        @Test
        @DisplayName("未认证用户：应抛出Unauthorized异常")
        void updateExperience_whenNotAuthenticated_shouldThrowUnauthorized() {
            userCTXMockedStatic.when(UserCTX::getCurrentUser).thenReturn(null);

            UserExperienceCommands.UpdateExperienceCommand command = new UserExperienceCommands.UpdateExperienceCommand(
                    EXPERIENCE_ID, "更新后的项目", null, null, null, null, null, null, null, null, null, null, null, null,
                    null, null);

            assertThrows(Unauthorized.class, () -> userExperienceAppService.updateExperience(command));
        }

        @Test
        @DisplayName("经历不存在：应抛出DataNotFound")
        void updateExperience_whenNotExists_shouldThrowException() {
            mockCurrentUser(USER_ID);

            UserExperienceCommands.UpdateExperienceCommand command = new UserExperienceCommands.UpdateExperienceCommand(
                    EXPERIENCE_ID, "更新后的项目", null, null, null, null, null, null, null, null, null, null, null, null,
                    null, null);

            when(userExperienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.empty());

            assertThrows(DataNotFound.class, () -> userExperienceAppService.updateExperience(command));
        }

        @Test
        @DisplayName("非所有者：应抛出Forbidden")
        void updateExperience_whenNotOwner_shouldThrowForbidden() {
            mockCurrentUser(USER_ID);

            UserExperience existing = createExperience(EXPERIENCE_ID, ExperienceType.PROJECT, "原项目");
            UserExperienceCommands.UpdateExperienceCommand command = new UserExperienceCommands.UpdateExperienceCommand(
                    EXPERIENCE_ID, "更新后的项目", null, null, null, null, null, null, null, null, null, null, null, null,
                    null, null);

            when(userExperienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.of(existing));
            when(userExperienceRepository.checkOwner(EXPERIENCE_ID, USER_ID)).thenReturn(false);

            assertThrows(Forbidden.class, () -> userExperienceAppService.updateExperience(command));
        }

        @Test
        @DisplayName("正常更新：应成功更新经历")
        void updateExperience_whenExists_shouldUpdateSuccessfully() {
            mockCurrentUser(USER_ID);

            UserExperience existing = createExperience(EXPERIENCE_ID, ExperienceType.PROJECT, "原项目");
            UserExperienceCommands.UpdateExperienceCommand command = new UserExperienceCommands.UpdateExperienceCommand(
                    EXPERIENCE_ID, "更新后的项目", "负责人", "2024.01", "2024.12", null, null, null, null, null, null, null,
                    null, null, null, null);

            when(userExperienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.of(existing));
            when(userExperienceRepository.checkOwner(EXPERIENCE_ID, USER_ID)).thenReturn(true);

            UserExperienceResult result = userExperienceAppService.updateExperience(command);

            assertNotNull(result);
            assertEquals("更新后的项目", result.title());
            verify(userExperienceRepository).update(any(UserExperience.class));
        }
    }

    @Nested
    @DisplayName("deleteExperience 方法测试")
    class DeleteExperienceTests {

        @Test
        @DisplayName("未认证用户：应抛出Unauthorized异常")
        void deleteExperience_whenNotAuthenticated_shouldThrowUnauthorized() {
            userCTXMockedStatic.when(UserCTX::getCurrentUser).thenReturn(null);

            assertThrows(Unauthorized.class, () -> userExperienceAppService.deleteExperience(EXPERIENCE_ID));
        }

        @Test
        @DisplayName("正常删除：应调用仓储删除")
        void deleteExperience_whenAuthenticated_shouldCallRepository() {
            mockCurrentUser(USER_ID);

            UserExperience existing = createExperience(EXPERIENCE_ID, ExperienceType.PROJECT, "原项目");
            when(userExperienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.of(existing));
            when(userExperienceRepository.checkOwner(EXPERIENCE_ID, USER_ID)).thenReturn(true);

            userExperienceAppService.deleteExperience(EXPERIENCE_ID);

            verify(userExperienceRepository).deleteById(EXPERIENCE_ID);
        }

        @Test
        @DisplayName("非所有者：应抛出Forbidden")
        void deleteExperience_whenNotOwner_shouldThrowForbidden() {
            mockCurrentUser(USER_ID);

            UserExperience existing = createExperience(EXPERIENCE_ID, ExperienceType.PROJECT, "原项目");
            when(userExperienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.of(existing));
            when(userExperienceRepository.checkOwner(EXPERIENCE_ID, USER_ID)).thenReturn(false);

            assertThrows(Forbidden.class, () -> userExperienceAppService.deleteExperience(EXPERIENCE_ID));
        }
    }
}
