package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.knowledge.KnowledgeCommands;
import com.bluenet.web.application.result.knowledge.KnowledgeDocResult;
import com.bluenet.web.application.service.KnowledgeBaseAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.KnowledgeDoc;
import com.bluenet.web.domain.model.enumerate.DocParseStatus;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.KnowledgeChunkRepository;
import com.bluenet.web.domain.repository.KnowledgeDocRepository;
import com.bluenet.web.domain.repository.KnowledgeTagRepository;
import com.bluenet.web.infrastructure.messaging.KnowledgeParsePublisher;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;

/**
 * KnowledgeBaseAppServiceImpl 集成测试。
 *
 * <p>
 * 验证知识库文档的上传、重新解析、取消解析、删除以及标签描述更新逻辑， 同时覆盖文件类型校验、状态校验等业务规则分支。
 * </p>
 */
@DisplayName("KnowledgeBaseAppServiceImpl 集成测试")
@WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN", permissions = {
        "knowledge:doc:upload",
        "knowledge:doc:reparse",
        "knowledge:doc:cancel",
        "knowledge:doc:delete",
        "knowledge:tag:update" })
class KnowledgeBaseAppServiceImplIntegrationTest extends BaseIntegrationTest {

    private static final byte[] MD_BYTES = "# Hello Knowledge Base".getBytes(StandardCharsets.UTF_8);

    @Autowired
    private KnowledgeBaseAppService knowledgeBaseAppService;

    @Autowired
    private KnowledgeDocRepository knowledgeDocRepository;

    @Autowired
    private KnowledgeTagRepository knowledgeTagRepository;

    @Autowired
    private KnowledgeChunkRepository knowledgeChunkRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private KnowledgeParsePublisher knowledgeParsePublisher;

    @AfterEach
    void cleanupSecurityContext() {
        UserCTX.clear();
    }

    @Test
    @DisplayName("uploadDocument: 上传 .md 文件应保存为 PENDING 状态并发布解析任务")
    void uploadDocument_withMarkdownFile_shouldSavePendingAndPublish() {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "intro.md",
                MediaType.TEXT_MARKDOWN_VALUE,
                MD_BYTES);
        KnowledgeCommands.UploadDocumentCommand command = new KnowledgeCommands.UploadDocumentCommand(multipartFile,
                "文档标题");

        KnowledgeDocResult.Uploaded result = knowledgeBaseAppService.uploadDocument(command);

        assertThat(result).isNotNull();
        assertThat(result.docId()).isPositive();
        assertThat(result.status()).isEqualTo(DocParseStatus.PENDING);
        assertThat(knowledgeDocRepository.findById(result.docId()))
                .isPresent()
                .hasValueSatisfying(doc -> {
                    assertThat(doc.getTitle()).isEqualTo("文档标题");
                    assertThat(doc.getStatus()).isEqualTo(DocParseStatus.PENDING);
                });
        verify(knowledgeParsePublisher).publish(eq(result.docId()), anyLong(), anyString(), eq(false));
    }

    @Test
    @DisplayName("uploadDocument: 上传非 .md 文件应抛 BadRequest")
    void uploadDocument_withNonMarkdownFile_shouldThrowBadRequest() {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "intro.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "plain text".getBytes(StandardCharsets.UTF_8));
        KnowledgeCommands.UploadDocumentCommand command = new KnowledgeCommands.UploadDocumentCommand(multipartFile,
                null);

        assertThatThrownBy(() -> knowledgeBaseAppService.uploadDocument(command))
                .isInstanceOf(BadRequest.class)
                .hasMessageContaining("仅支持上传 .md 文件");
    }

    @Test
    @DisplayName("reparse: 重新解析已有文档应发布解析任务并置为 PENDING")
    void reparse_withExistingDocument_shouldPublishAndSetPending() {
        Long docId = uploadMarkdownAndReturnDocId("reparse.md");
        clearInvocations(knowledgeParsePublisher);
        KnowledgeCommands.ReparseDocumentCommand command = new KnowledgeCommands.ReparseDocumentCommand(docId);

        knowledgeBaseAppService.reparse(command);

        assertThat(knowledgeDocRepository.findById(docId))
                .isPresent()
                .hasValueSatisfying(doc -> assertThat(doc.getStatus()).isEqualTo(DocParseStatus.PENDING));
        verify(knowledgeParsePublisher).publish(eq(docId), anyLong(), anyString(), eq(true));
    }

    @Test
    @DisplayName("reparse: 文档不存在时应抛 DataNotFound")
    void reparse_withNonExistingDocument_shouldThrowDataNotFound() {
        KnowledgeCommands.ReparseDocumentCommand command = new KnowledgeCommands.ReparseDocumentCommand(999_999L);

        assertThatThrownBy(() -> knowledgeBaseAppService.reparse(command))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("文档不存在");
    }

    @Test
    @DisplayName("cancelParse: PENDING 状态文档应更新为 CANCELING")
    void cancelParse_withPendingDocument_shouldSetCanceling() {
        Long docId = uploadMarkdownAndReturnDocId("cancel-pending.md");
        KnowledgeCommands.CancelParseCommand command = new KnowledgeCommands.CancelParseCommand(docId);

        knowledgeBaseAppService.cancelParse(command);

        assertThat(knowledgeDocRepository.findById(docId))
                .isPresent()
                .hasValueSatisfying(doc -> assertThat(doc.getStatus()).isEqualTo(DocParseStatus.CANCELING));
    }

    @Test
    @DisplayName("cancelParse: 已完成文档应抛 BadRequest")
    void cancelParse_withCompletedDocument_shouldThrowBadRequest() {
        Long docId = uploadMarkdownAndReturnDocId("cancel-completed.md");
        KnowledgeDoc doc = knowledgeDocRepository.findById(docId).orElseThrow();
        doc.updateStatus(DocParseStatus.COMPLETED, null, null);
        knowledgeDocRepository.save(doc);
        KnowledgeCommands.CancelParseCommand command = new KnowledgeCommands.CancelParseCommand(docId);

        assertThatThrownBy(() -> knowledgeBaseAppService.cancelParse(command))
                .isInstanceOf(BadRequest.class)
                .hasMessageContaining("当前状态不允许取消解析");
    }

    @Test
    @DisplayName("deleteDocument: 应删除文档、分段及文件元数据")
    void deleteDocument_withExistingDocument_shouldDeleteDocChunksAndFile() {
        Long docId = uploadMarkdownAndReturnDocId("delete.md");
        Long fileId = knowledgeDocRepository.findById(docId).orElseThrow().getFileId();
        KnowledgeCommands.DeleteDocumentCommand command = new KnowledgeCommands.DeleteDocumentCommand(docId);

        knowledgeBaseAppService.deleteDocument(command);

        assertThat(knowledgeDocRepository.findById(docId)).isEmpty();
        assertThat(knowledgeChunkRepository.findByDocId(docId, PageRequest.of(0, 10)).getTotalElements()).isZero();
        assertThat(fileRepository.findById(fileId)).isEmpty();
    }

    @Test
    @DisplayName("updateTagDescription: 应更新标签描述")
    void updateTagDescription_withExistingTag_shouldUpdateDescription() {
        Long tagId = createTagWithVector("Java", "原始描述");

        knowledgeBaseAppService.updateTagDescription(tagId, "更新后的描述");

        assertThat(knowledgeTagRepository.findById(tagId))
                .isPresent()
                .hasValueSatisfying(updated -> assertThat(updated.getTagDescription()).isEqualTo("更新后的描述"));
    }

    private Long createTagWithVector(String tagName, String description) {
        String vectorLiteral = "[" + "0,".repeat(1023) + "0]";
        jdbcTemplate.update(
                "INSERT INTO tb_rag_tags (tag_name, tag_vector, tag_description, chunks_count) VALUES (?, ?::vector, ?, ?)",
                tagName,
                vectorLiteral,
                description,
                0);
        return jdbcTemplate.queryForObject("SELECT id FROM tb_rag_tags WHERE tag_name = ?", Long.class, tagName);
    }

    @Test
    @DisplayName("updateTagDescription: 标签不存在时应抛 DataNotFound")
    void updateTagDescription_withNonExistingTag_shouldThrowDataNotFound() {
        assertThatThrownBy(() -> knowledgeBaseAppService.updateTagDescription(999_999L, "任意描述"))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("标签不存在");
    }

    private Long uploadMarkdownAndReturnDocId(String filename) {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                filename,
                MediaType.TEXT_MARKDOWN_VALUE,
                MD_BYTES);
        KnowledgeCommands.UploadDocumentCommand command = new KnowledgeCommands.UploadDocumentCommand(multipartFile,
                null);
        KnowledgeDocResult.Uploaded result = knowledgeBaseAppService.uploadDocument(command);
        return result.docId();
    }
}
