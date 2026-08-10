package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.wpsform.WpsFormCommands;
import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.application.service.WpsFormAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.config.properties.WpsProperties;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testsupport.fixture.CollegeFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * WpsFormAppServiceImpl 集成测试。
 *
 * <p>
 * 验证 WPS 表单创建用户、邮件凭据分发以及绑定码解析逻辑。
 * </p>
 */
@DisplayName("WpsFormAppServiceImpl 集成测试")
class WpsFormAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WpsFormAppService wpsFormAppService;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WpsProperties wpsProperties;

    @MockitoBean
    private MessageDispatcher messageDispatcher;

    @AfterEach
    void cleanupSecurityContext() {
        UserCTX.clear();
        wpsProperties.setBindCode("");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("createUserFromWpsForm: 应创建用户并异步分发凭据邮件")
    void createUserFromWpsForm_shouldCreateUserAndDispatchCredentialEmail() {
        College college = CollegeFixture.saveCollege(collegeRepository, "计算机学院");
        String studentId = "2024" + UUID.randomUUID().toString().substring(0, 8);
        String email = studentId + "@example.com";
        WpsFormCommands.CreateUserFromWpsFormCommand command = new WpsFormCommands.CreateUserFromWpsFormCommand(
                studentId,
                "张三",
                email,
                "计算机视觉",
                "软件工程",
                "计算机学院",
                "男");

        wpsFormAppService.createUserFromWpsForm(command);

        Optional<User> savedUser = userRepository.findByStudentId(studentId);
        assertThat(savedUser).isPresent();
        User user = savedUser.get();
        assertThat(user.getUsername()).isEqualTo("张三");
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getCollegeId()).isEqualTo(college.getId());
        assertThat(user.getDirection()).isEqualTo(Direction.COMPUTER_VISION);

        ArgumentCaptor<MessageRequest> captor = ArgumentCaptor.forClass(MessageRequest.class);
        verify(messageDispatcher, times(1)).dispatchAsync(captor.capture());
        MessageRequest request = captor.getValue();
        assertThat(request.channel()).isEqualTo(com.bluenet.web.domain.model.enumerate.MessageChannel.EMAIL);
        assertThat(request.recipient()).isEqualTo(email);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("createUserFromWpsForm: 无效方向文本应抛出 BadRequest")
    void createUserFromWpsForm_invalidDirection_shouldThrowBadRequest() {
        CollegeFixture.saveCollege(collegeRepository, "计算机学院");
        WpsFormCommands.CreateUserFromWpsFormCommand command = new WpsFormCommands.CreateUserFromWpsFormCommand(
                "2024001001",
                "张三",
                "invalid-direction@example.com",
                "不存在的方向",
                "软件工程",
                "计算机学院",
                "男");

        assertThatThrownBy(() -> wpsFormAppService.createUserFromWpsForm(command))
                .isInstanceOf(BadRequest.class)
                .hasMessageContaining("方向字段值无效");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("createUserFromWpsForm: 无效学院文本应抛出 BadRequest")
    void createUserFromWpsForm_invalidCollege_shouldThrowBadRequest() {
        WpsFormCommands.CreateUserFromWpsFormCommand command = new WpsFormCommands.CreateUserFromWpsFormCommand(
                "2024001002",
                "张三",
                "invalid-college@example.com",
                "计算机视觉",
                "软件工程",
                "不存在的学院",
                "男");

        assertThatThrownBy(() -> wpsFormAppService.createUserFromWpsForm(command))
                .isInstanceOf(BadRequest.class)
                .hasMessageContaining("学院")
                .hasMessageContaining("不存在");
    }

    @Test
    @DisplayName("resolveBindCode: 配置不为空时应返回配置的绑定码")
    void resolveBindCode_withConfiguredBindCode_shouldReturnConfiguredValue() {
        wpsProperties.setBindCode("BIND123456");

        String result = wpsFormAppService.resolveBindCode("fallback-rid");

        assertThat(result).isEqualTo("BIND123456");
    }

    @Test
    @DisplayName("resolveBindCode: 配置为空时应回退到传入的 rid")
    void resolveBindCode_withBlankBindCode_shouldReturnRid() {
        wpsProperties.setBindCode("");

        String result = wpsFormAppService.resolveBindCode("answer-rid-123");

        assertThat(result).isEqualTo("answer-rid-123");
    }
}
