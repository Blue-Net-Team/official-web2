package com.bluenet.web.domain.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MessageTemplate 领域实体单元测试。
 * <p>
 * MessageTemplate 为贫血 POJO，仅验证字段访问行为。
 * </p>
 */
@DisplayName("MessageTemplate 领域实体测试")
class MessageTemplateTest {

    @Test
    @DisplayName("字段应支持 getter/setter 访问")
    void fields_shouldBeAccessibleViaGetterAndSetter() {
        MessageTemplate template = new MessageTemplate();
        template.setId(1L);
        template.setCode("VERIFY_CODE");
        template.setName("验证码模板");
        template.setSubject("验证码");
        template.setContent("您的验证码是 1234");
        template.setDescription("登录验证码");
        template.setEnabled(true);

        assertThat(template.getId()).isEqualTo(1L);
        assertThat(template.getCode()).isEqualTo("VERIFY_CODE");
        assertThat(template.getName()).isEqualTo("验证码模板");
        assertThat(template.getSubject()).isEqualTo("验证码");
        assertThat(template.getContent()).isEqualTo("您的验证码是 1234");
        assertThat(template.getDescription()).isEqualTo("登录验证码");
        assertThat(template.getEnabled()).isTrue();
    }
}
