package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.message.MessageTemplateInfo;
import com.bluenet.web.application.message.MessageTemplateRegistry;
import com.bluenet.web.application.service.MessageTemplateAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MessageTemplateAppServiceImpl 集成测试。
 *
 * <p>
 * 验证消息模板应用服务的查询、更新、切换和预览逻辑。
 * </p>
 */
@DisplayName("MessageTemplateAppServiceImpl 集成测试")
class MessageTemplateAppServiceImplIntegrationTest extends BaseIntegrationTest {

    private static final String EMAIL_VERIFICATION_CODE = "EMAIL_VERIFICATION_CODE";

    @Autowired
    private MessageTemplateAppService messageTemplateAppService;

    @Autowired
    private MessageTemplateRegistry messageTemplateRegistry;

    @BeforeEach
    void resetRegistryOverrides() {
        clearRegistryOverrides();
    }

    @AfterEach
    void cleanup() {
        UserCTX.clear();
        clearRegistryOverrides();
    }

    /**
     * 清除 {@link MessageTemplateRegistry} 中的运行时覆盖，避免测试间状态污染。
     */
    private void clearRegistryOverrides() {
        try {
            Field contentField = MessageTemplateRegistry.class.getDeclaredField("contentOverrides");
            contentField.setAccessible(true);
            ((Map<?, ?>) contentField.get(messageTemplateRegistry)).clear();

            Field subjectField = MessageTemplateRegistry.class.getDeclaredField("subjectOverrides");
            subjectField.setAccessible(true);
            ((Map<?, ?>) subjectField.get(messageTemplateRegistry)).clear();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("重置模板注册表覆盖失败", e);
        }
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("listTemplates: 应返回所有硬编码注册的模板")
    void listTemplates_shouldReturnAllRegisteredTemplates() {
        List<MessageTemplateInfo> result = messageTemplateAppService.listTemplates();

        assertThat(result).hasSize(4);
        assertThat(result)
                .extracting(MessageTemplateInfo::code)
                .containsExactlyInAnyOrder(
                        EMAIL_VERIFICATION_CODE,
                        "ENROLL_APPROVAL_CREDENTIAL",
                        "ENROLL_REJECTION",
                        "ASSESSMENT_DECISION_NOTIFICATION");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getTemplate: 应返回指定编码的模板")
    void getTemplate_shouldReturnKnownTemplate() {
        MessageTemplateInfo result = messageTemplateAppService.getTemplate(EMAIL_VERIFICATION_CODE);

        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo(EMAIL_VERIFICATION_CODE);
        assertThat(result.name()).isEqualTo("邮箱验证码");
        assertThat(result.subject()).isEqualTo("蓝网验证码");
        assertThat(result.variables()).containsExactly("title", "description", "code", "footer");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getTemplate: 未知编码应抛 DataNotFound")
    void getTemplate_unknownCode_shouldThrowDataNotFound() {
        assertThatThrownBy(() -> messageTemplateAppService.getTemplate("NON_EXISTENT_CODE"))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("模板不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateTemplate: 应更新内容并持久化")
    void updateTemplate_shouldUpdateContent() {
        String newContent = """
                <section>
                    <h1 style="color:#fa8c16;">蓝网{{title}}</h1>
                    <p>{{description}}</p>
                    <p style="font-size:24px;">{{code}}</p>
                    <p style="color:#999;">{{footer}}</p>
                </section>
                """;

        messageTemplateAppService.updateTemplate(EMAIL_VERIFICATION_CODE, null, newContent);

        MessageTemplateInfo result = messageTemplateAppService.getTemplate(EMAIL_VERIFICATION_CODE);
        assertThat(result.content()).isEqualTo(newContent);
        assertThat(result.defaultContent()).isNotEqualTo(newContent);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateTemplate: 应更新主题并持久化")
    void updateTemplate_shouldUpdateSubject() {
        String newSubject = "新的验证码主题";

        messageTemplateAppService.updateTemplate(EMAIL_VERIFICATION_CODE, newSubject, null);

        MessageTemplateInfo result = messageTemplateAppService.getTemplate(EMAIL_VERIFICATION_CODE);
        assertThat(result.subject()).isEqualTo(newSubject);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateTemplate: null 或空字符串不应修改对应字段")
    void updateTemplate_withNullOrBlank_shouldLeaveUnchanged() {
        MessageTemplateInfo before = messageTemplateAppService.getTemplate(EMAIL_VERIFICATION_CODE);

        messageTemplateAppService.updateTemplate(EMAIL_VERIFICATION_CODE, null, null);
        messageTemplateAppService.updateTemplate(EMAIL_VERIFICATION_CODE, "", "   ");

        MessageTemplateInfo after = messageTemplateAppService.getTemplate(EMAIL_VERIFICATION_CODE);
        assertThat(after).isEqualTo(before);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("toggleTemplate: 调用不应抛出异常且不改变模板")
    void toggleTemplate_shouldBeNoOp() {
        MessageTemplateInfo before = messageTemplateAppService.getTemplate(EMAIL_VERIFICATION_CODE);

        assertThatNoException()
                .isThrownBy(() -> messageTemplateAppService.toggleTemplate(EMAIL_VERIFICATION_CODE, false));

        MessageTemplateInfo after = messageTemplateAppService.getTemplate(EMAIL_VERIFICATION_CODE);
        assertThat(after).isEqualTo(before);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("previewTemplate: 应使用变量替换渲染模板内容")
    void previewTemplate_shouldSubstituteVariables() {
        Map<String, String> variables = Map.of(
                "title",
                "登录验证",
                "description",
                "您的验证码如下",
                "code",
                "123456",
                "footer",
                "请勿泄露");

        String result = messageTemplateAppService.previewTemplate(EMAIL_VERIFICATION_CODE, variables);

        assertThat(result)
                .contains("蓝网登录验证")
                .contains("您的验证码如下")
                .contains("123456")
                .contains("请勿泄露")
                .doesNotContain("{{title}}")
                .doesNotContain("{{description}}")
                .doesNotContain("{{code}}")
                .doesNotContain("{{footer}}");
    }
}
