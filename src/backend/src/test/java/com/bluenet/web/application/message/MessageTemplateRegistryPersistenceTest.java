package com.bluenet.web.application.message;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.infrastructure.repository.dataobject.MessageTemplateDO;
import com.bluenet.web.infrastructure.repository.mapper.MessageTemplateMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MessageTemplateRegistry 持久化集成测试。
 *
 * <p>
 * 验证模板运行时覆盖（content、subject）在数据库中的持久化与启动加载行为。 "重启" 通过构造新的
 * {@link MessageTemplateRegistry} 实例并传入真实 Mapper 来模拟。
 * </p>
 */
class MessageTemplateRegistryPersistenceTest extends BaseIntegrationTest {

    @Autowired
    private MessageTemplateMapper mapper;

    @Test
    @DisplayName("启动时应从数据库加载 content 覆盖值")
    void shouldLoadContentOverrideFromDatabaseOnStartup() {
        String overriddenContent = "<div>覆盖后的验证码内容</div>";
        insertRecord("EMAIL_VERIFICATION_CODE", "邮箱验证码", "蓝网验证码", overriddenContent, "测试");

        MessageTemplateRegistry registry = new MessageTemplateRegistry(mapper);

        assertEquals(overriddenContent, registry.getTemplateContent("EMAIL_VERIFICATION_CODE"));
    }

    @Test
    @DisplayName("启动时应从数据库加载 subject 覆盖值")
    void shouldLoadSubjectOverrideFromDatabaseOnStartup() {
        String overriddenSubject = "覆盖后的主题";
        insertRecord("EMAIL_VERIFICATION_CODE", "邮箱验证码", overriddenSubject, null, "测试");

        MessageTemplateRegistry registry = new MessageTemplateRegistry(mapper);

        assertEquals(overriddenSubject, registry.getTemplateSubject("EMAIL_VERIFICATION_CODE"));
    }

    @Test
    @DisplayName("编辑 content 应同步持久化到数据库，新实例启动后可恢复")
    void shouldPersistContentEditToDatabaseAcrossRestarts() {
        MessageTemplateRegistry registry = new MessageTemplateRegistry(mapper);
        String newContent = "<div>新验证码内容</div>";

        registry.updateContent("EMAIL_VERIFICATION_CODE", newContent);

        // 模拟重启：创建新实例
        MessageTemplateRegistry restarted = new MessageTemplateRegistry(mapper);
        assertEquals(newContent, restarted.getTemplateContent("EMAIL_VERIFICATION_CODE"));

        // 直接查库验证
        MessageTemplateDO record = findByCode("EMAIL_VERIFICATION_CODE");
        assertNotNull(record);
        assertEquals(newContent, record.getContent());
    }

    @Test
    @DisplayName("编辑 subject 应同步持久化到数据库，新实例启动后可恢复")
    void shouldPersistSubjectEditToDatabaseAcrossRestarts() {
        MessageTemplateRegistry registry = new MessageTemplateRegistry(mapper);
        String newSubject = "新验证码主题";

        registry.updateSubject("EMAIL_VERIFICATION_CODE", newSubject);

        MessageTemplateRegistry restarted = new MessageTemplateRegistry(mapper);
        assertEquals(newSubject, restarted.getTemplateSubject("EMAIL_VERIFICATION_CODE"));

        MessageTemplateDO record = findByCode("EMAIL_VERIFICATION_CODE");
        assertNotNull(record);
        assertEquals(newSubject, record.getSubject());
    }

    @Test
    @DisplayName("删除数据库记录后应回退到代码默认值")
    void shouldFallbackToDefaultWhenDatabaseRecordRemoved() {
        MessageTemplateRegistry registry = new MessageTemplateRegistry(mapper);
        String defaultContent = registry.getTemplateContent("EMAIL_VERIFICATION_CODE");
        String defaultSubject = registry.getTemplateSubject("EMAIL_VERIFICATION_CODE");

        registry.updateContent("EMAIL_VERIFICATION_CODE", "<div>临时覆盖</div>");
        registry.updateSubject("EMAIL_VERIFICATION_CODE", "临时主题");

        // 删除数据库记录
        mapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MessageTemplateDO>()
                        .eq("code", "EMAIL_VERIFICATION_CODE"));

        MessageTemplateRegistry restarted = new MessageTemplateRegistry(mapper);
        assertEquals(defaultContent, restarted.getTemplateContent("EMAIL_VERIFICATION_CODE"));
        assertEquals(defaultSubject, restarted.getTemplateSubject("EMAIL_VERIFICATION_CODE"));
    }

    @Test
    @DisplayName("模板元数据在编辑后应保持不变")
    void shouldKeepMetadataImmutableAfterEdit() {
        MessageTemplateRegistry registry = new MessageTemplateRegistry(mapper);
        String originalName = registry.findByCode("EMAIL_VERIFICATION_CODE")
                .orElseThrow()
                .name();

        registry.updateContent("EMAIL_VERIFICATION_CODE", "<div>新内容</div>");
        registry.updateSubject("EMAIL_VERIFICATION_CODE", "新主题");

        MessageTemplateInfo info = registry.findByCode("EMAIL_VERIFICATION_CODE").orElseThrow();
        assertEquals(originalName, info.name());
        assertEquals("新主题", info.subject());
        assertEquals("<div>新内容</div>", info.content());
    }

    // --- helpers ---

    private void insertRecord(String code, String name, String subject, String content,
            String description) {
        MessageTemplateDO record = MessageTemplateDO.builder()
                .code(code)
                .name(name)
                .subject(subject)
                .content(content)
                .description(description)
                .enabled(true)
                .build();
        mapper.insert(record);
    }

    private MessageTemplateDO findByCode(String code) {
        return mapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MessageTemplateDO>()
                        .eq("code", code));
    }
}
