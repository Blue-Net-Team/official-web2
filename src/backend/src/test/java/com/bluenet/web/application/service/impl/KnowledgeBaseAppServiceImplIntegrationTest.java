package com.bluenet.web.application.service.impl;

import com.bluenet.web.DBIntegrationTest;
import com.bluenet.web.application.command.knowledge.KnowledgeCommands;
import com.bluenet.web.application.result.knowledge.KnowledgeDocResult;
import com.bluenet.web.application.service.KnowledgeBaseAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.KnowledgeDoc;
import com.bluenet.web.domain.model.enumerate.ChunkVectorStatus;
import com.bluenet.web.domain.model.enumerate.DocParseStatus;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.KnowledgeChunkRepository;
import com.bluenet.web.domain.repository.KnowledgeChunkTagRepository;
import com.bluenet.web.domain.repository.KnowledgeDocRepository;
import com.bluenet.web.domain.repository.KnowledgeTagRepository;
import com.bluenet.web.infrastructure.messaging.KnowledgeParsePublisher;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import io.github.ivencn.infra.security.principal.UserCTX;
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
import java.util.List;

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
        "knowledge:tag:update",
        "knowledge:tag:create",
        "knowledge:tag:delete",
        "knowledge:chunk:update",
        "knowledge:chunk:delete",
        "knowledge:doc:replace-file" })
class KnowledgeBaseAppServiceImplIntegrationTest extends DBIntegrationTest {

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
    private KnowledgeChunkTagRepository knowledgeChunkTagRepository;

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
                .hasValueSatisfying(updated -> {
                    assertThat(updated.getTagDescription()).isEqualTo("更新后的描述");
                    assertThat(updated.getVectorStatus()).isEqualTo(ChunkVectorStatus.SYNCED);
                });
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

    @Test
    @DisplayName("updateChunk: 应更新内容标签并置为向量化中")
    void updateChunk_withValidInput_shouldUpdateAndPublishReembed() {
        Long docId = uploadMarkdownAndReturnDocId("chunk-edit.md");
        markDocCompleted(docId);
        Long chunkId = insertChunk(docId, "旧内容");
        Long tagId1 = createTagWithVector("标签A", "");
        Long tagId2 = createTagWithVector("标签B", "");
        clearInvocations(knowledgeParsePublisher);

        knowledgeBaseAppService
                .updateChunk(new KnowledgeCommands.UpdateChunkCommand(chunkId, "新内容", List.of(tagId1, tagId2)));

        assertThat(knowledgeChunkRepository.findById(chunkId))
                .isPresent()
                .hasValueSatisfying(chunk -> {
                    assertThat(chunk.getContent()).isEqualTo("新内容");
                    assertThat(chunk.getTagIds()).containsExactlyInAnyOrder(tagId1, tagId2);
                    assertThat(chunk.getVectorStatus()).isEqualTo(ChunkVectorStatus.EMBEDDING);
                });
        verify(knowledgeParsePublisher).publishReembed(chunkId);
    }

    @Test
    @DisplayName("updateChunk: 文档解析中应抛 DataConflict")
    void updateChunk_withParsingDocument_shouldThrowConflict() {
        Long docId = uploadMarkdownAndReturnDocId("chunk-edit-conflict.md");
        Long chunkId = insertChunk(docId, "内容");
        KnowledgeDoc doc = knowledgeDocRepository.findById(docId).orElseThrow();
        doc.updateStatus(DocParseStatus.PARSING, null, null);
        knowledgeDocRepository.save(doc);

        assertThatThrownBy(
                () -> knowledgeBaseAppService.updateChunk(
                        new KnowledgeCommands.UpdateChunkCommand(chunkId, "新内容", List.of())))
                                .isInstanceOf(DataConflict.class);
    }

    @Test
    @DisplayName("updateChunk: 标签不存在应抛 BadRequest")
    void updateChunk_withNonExistingTag_shouldThrowBadRequest() {
        Long docId = uploadMarkdownAndReturnDocId("chunk-edit-bad-tag.md");
        markDocCompleted(docId);
        Long chunkId = insertChunk(docId, "内容");

        assertThatThrownBy(
                () -> knowledgeBaseAppService.updateChunk(
                        new KnowledgeCommands.UpdateChunkCommand(chunkId, "新内容", List.of(999_999L))))
                                .isInstanceOf(BadRequest.class)
                                .hasMessageContaining("标签不存在");
    }

    @Test
    @DisplayName("updateChunk: 分片不存在应抛 DataNotFound")
    void updateChunk_withNonExistingChunk_shouldThrowDataNotFound() {
        assertThatThrownBy(
                () -> knowledgeBaseAppService.updateChunk(
                        new KnowledgeCommands.UpdateChunkCommand(999_999L, "新内容", List.of())))
                                .isInstanceOf(DataNotFound.class)
                                .hasMessageContaining("分片不存在");
    }

    @Test
    @DisplayName("deleteChunk: 应删除分片、解除关联并将文档分段数减一")
    void deleteChunk_withValidChunk_shouldDeleteAndDecrementCount() {
        Long docId = uploadMarkdownAndReturnDocId("chunk-delete.md");
        Long keepChunkId = insertChunk(docId, "保留内容");
        Long deleteChunkId = insertChunk(docId, "待删内容");
        KnowledgeDoc completedDoc = knowledgeDocRepository.findById(docId).orElseThrow();
        completedDoc.updateStatus(DocParseStatus.COMPLETED, 2, null);
        knowledgeDocRepository.save(completedDoc);
        Long tagId = createTagWithVector("删除分段标签", "");
        jdbcTemplate.update(
                "INSERT INTO tb_rag_chunk_tags (chunk_id, tag_id) VALUES (?, ?)",
                deleteChunkId,
                tagId);
        clearInvocations(knowledgeParsePublisher);

        knowledgeBaseAppService.deleteChunk(new KnowledgeCommands.DeleteChunkCommand(deleteChunkId));

        assertThat(knowledgeChunkRepository.findById(deleteChunkId)).isEmpty();
        assertThat(knowledgeChunkTagRepository.countByTagId(tagId)).isZero();
        assertThat(knowledgeChunkRepository.findById(keepChunkId)).isPresent();
        assertThat(knowledgeDocRepository.findById(docId))
                .isPresent()
                .hasValueSatisfying(doc -> assertThat(doc.getChunkCount()).isEqualTo(1));
        verify(knowledgeParsePublisher, org.mockito.Mockito.never()).publishReembed(anyLong());
    }

    @Test
    @DisplayName("deleteChunk: 文档解析中应抛 DataConflict")
    void deleteChunk_withParsingDocument_shouldThrowConflict() {
        Long docId = uploadMarkdownAndReturnDocId("chunk-delete-conflict.md");
        Long chunkId = insertChunk(docId, "内容");
        KnowledgeDoc doc = knowledgeDocRepository.findById(docId).orElseThrow();
        doc.updateStatus(DocParseStatus.PARSING, null, null);
        knowledgeDocRepository.save(doc);

        assertThatThrownBy(
                () -> knowledgeBaseAppService.deleteChunk(new KnowledgeCommands.DeleteChunkCommand(chunkId)))
                        .isInstanceOf(DataConflict.class);
        assertThat(knowledgeChunkRepository.findById(chunkId)).isPresent();
    }

    @Test
    @DisplayName("deleteChunk: 分片不存在应抛 DataNotFound")
    void deleteChunk_withNonExistingChunk_shouldThrowDataNotFound() {
        assertThatThrownBy(
                () -> knowledgeBaseAppService.deleteChunk(new KnowledgeCommands.DeleteChunkCommand(999_999L)))
                        .isInstanceOf(DataNotFound.class)
                        .hasMessageContaining("分片不存在");
    }

    @Test
    @DisplayName("replaceDocFile: 应保存新文件并触发重新解析")
    void replaceDocFile_withMarkdownFile_shouldReplaceAndReparse() {
        Long docId = uploadMarkdownAndReturnDocId("replace-file.md");
        markDocCompleted(docId);
        Long oldFileId = knowledgeDocRepository.findById(docId).orElseThrow().getFileId();
        clearInvocations(knowledgeParsePublisher);
        MockMultipartFile newFile = new MockMultipartFile(
                "file",
                "updated.md",
                MediaType.TEXT_MARKDOWN_VALUE,
                MD_BYTES);

        knowledgeBaseAppService.replaceDocFile(new KnowledgeCommands.ReplaceDocFileCommand(docId, newFile));

        assertThat(knowledgeDocRepository.findById(docId))
                .isPresent()
                .hasValueSatisfying(doc -> {
                    assertThat(doc.getStatus()).isEqualTo(DocParseStatus.PENDING);
                    assertThat(doc.getFileId()).isNotEqualTo(oldFileId);
                });
        assertThat(fileRepository.findById(oldFileId)).isPresent();
        verify(knowledgeParsePublisher).publish(eq(docId), anyLong(), anyString(), eq(true));
    }

    @Test
    @DisplayName("replaceDocFile: 上传非 .md 文件应抛 BadRequest")
    void replaceDocFile_withNonMarkdownFile_shouldThrowBadRequest() {
        Long docId = uploadMarkdownAndReturnDocId("replace-file-bad.md");
        MockMultipartFile newFile = new MockMultipartFile(
                "file",
                "updated.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "plain".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(
                () -> knowledgeBaseAppService.replaceDocFile(
                        new KnowledgeCommands.ReplaceDocFileCommand(docId, newFile)))
                                .isInstanceOf(BadRequest.class)
                                .hasMessageContaining("仅支持上传 .md 文件");
    }

    @Test
    @DisplayName("replaceDocFile: 文档解析中应抛 DataConflict")
    void replaceDocFile_withParsingDocument_shouldThrowConflict() {
        Long docId = uploadMarkdownAndReturnDocId("replace-file-conflict.md");
        KnowledgeDoc doc = knowledgeDocRepository.findById(docId).orElseThrow();
        doc.updateStatus(DocParseStatus.PARSING, null, null);
        knowledgeDocRepository.save(doc);
        MockMultipartFile newFile = new MockMultipartFile(
                "file",
                "updated.md",
                MediaType.TEXT_MARKDOWN_VALUE,
                MD_BYTES);

        assertThatThrownBy(
                () -> knowledgeBaseAppService.replaceDocFile(
                        new KnowledgeCommands.ReplaceDocFileCommand(docId, newFile)))
                                .isInstanceOf(DataConflict.class);
    }

    @Test
    @DisplayName("createTag: 应创建标签并发布向量任务")
    void createTag_withUniqueName_shouldCreateAndPublish() {
        Long tagId = knowledgeBaseAppService.createTag(new KnowledgeCommands.CreateTagCommand("新标签", "描述"));

        assertThat(knowledgeTagRepository.findById(tagId))
                .isPresent()
                .hasValueSatisfying(tag -> {
                    assertThat(tag.getTagName()).isEqualTo("新标签");
                    assertThat(tag.getTagDescription()).isEqualTo("描述");
                    assertThat(tag.getVectorStatus()).isEqualTo(ChunkVectorStatus.EMBEDDING);
                });
        verify(knowledgeParsePublisher).publishTagUpsert(tagId, false);
    }

    @Test
    @DisplayName("createTag: 标签名重复应抛 BadRequest")
    void createTag_withDuplicateName_shouldThrowBadRequest() {
        createTagWithVector("重复标签", "");

        assertThatThrownBy(
                () -> knowledgeBaseAppService.createTag(
                        new KnowledgeCommands.CreateTagCommand("重复标签", null)))
                                .isInstanceOf(BadRequest.class)
                                .hasMessageContaining("标签名已存在");
    }

    @Test
    @DisplayName("updateTag: 重命名应更新名称并发布向量任务")
    void updateTag_withNewName_shouldRenameAndPublish() {
        Long tagId = createTagWithVector("旧名", "描述");
        clearInvocations(knowledgeParsePublisher);

        knowledgeBaseAppService.updateTag(new KnowledgeCommands.UpdateTagCommand(tagId, "新名", null));

        assertThat(knowledgeTagRepository.findById(tagId))
                .isPresent()
                .hasValueSatisfying(tag -> {
                    assertThat(tag.getTagName()).isEqualTo("新名");
                    assertThat(tag.getVectorStatus()).isEqualTo(ChunkVectorStatus.EMBEDDING);
                });
        verify(knowledgeParsePublisher).publishTagUpsert(tagId, false);
    }

    @Test
    @DisplayName("updateTag: 重命名为已存在的名字应抛 BadRequest")
    void updateTag_withDuplicateName_shouldThrowBadRequest() {
        Long tagId = createTagWithVector("名字A", "");
        createTagWithVector("名字B", "");

        assertThatThrownBy(
                () -> knowledgeBaseAppService.updateTag(
                        new KnowledgeCommands.UpdateTagCommand(tagId, "名字B", null)))
                                .isInstanceOf(BadRequest.class)
                                .hasMessageContaining("标签名已存在");
    }

    @Test
    @DisplayName("deleteTag: 应删除标签并解除全部分片关联")
    void deleteTag_withAssociations_shouldDissociateAndDelete() {
        Long docId = uploadMarkdownAndReturnDocId("delete-tag.md");
        Long chunkId = insertChunk(docId, "内容");
        Long tagId = createTagWithVector("待删标签", "");
        jdbcTemplate.update(
                "INSERT INTO tb_rag_chunk_tags (chunk_id, tag_id) VALUES (?, ?)",
                chunkId,
                tagId);

        int dissociated = knowledgeBaseAppService.deleteTag(new KnowledgeCommands.DeleteTagCommand(tagId));

        assertThat(dissociated).isEqualTo(1);
        assertThat(knowledgeTagRepository.findById(tagId)).isEmpty();
        assertThat(knowledgeChunkTagRepository.countByTagId(tagId)).isZero();
        assertThat(knowledgeChunkRepository.findById(chunkId)).isPresent();
        verify(knowledgeParsePublisher).publishTagUpsert(tagId, true);
    }

    @Test
    @DisplayName("deleteTag: 标签不存在应抛 DataNotFound")
    void deleteTag_withNonExistingTag_shouldThrowDataNotFound() {
        assertThatThrownBy(() -> knowledgeBaseAppService.deleteTag(new KnowledgeCommands.DeleteTagCommand(999_999L)))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("标签不存在");
    }

    private Long insertChunk(Long docId, String content) {
        String vectorLiteral = "[" + "0,".repeat(1023) + "0]";
        return jdbcTemplate.queryForObject(
                "INSERT INTO tb_rag_chunks (doc_id, chunk_vector, content, vector_status, source) "
                        + "VALUES (?, ?::vector, ?, 'synced', 'source') RETURNING id",
                Long.class,
                docId,
                vectorLiteral,
                content);
    }

    private void markDocCompleted(Long docId) {
        KnowledgeDoc doc = knowledgeDocRepository.findById(docId).orElseThrow();
        doc.updateStatus(DocParseStatus.COMPLETED, null, null);
        knowledgeDocRepository.save(doc);
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
