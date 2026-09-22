package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.DBIntegrationTest;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.KnowledgeDoc;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.KnowledgeChunkTagRepository;
import com.bluenet.web.domain.repository.KnowledgeDocRepository;
import com.bluenet.web.infrastructure.repository.dataobject.KnowledgeChunkDO;
import com.bluenet.web.infrastructure.repository.mapper.KnowledgeChunkMapper;
import com.bluenet.web.testsupport.fixture.FileFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * KnowledgeChunkTagRepositoryImpl 集成测试。
 */
@DisplayName("KnowledgeChunkTagRepositoryImpl 集成测试")
class KnowledgeChunkTagRepositoryImplIntegrationTest extends DBIntegrationTest {

    @Autowired
    private KnowledgeChunkTagRepository knowledgeChunkTagRepository;

    @Autowired
    private KnowledgeChunkMapper knowledgeChunkMapper;

    @Autowired
    private KnowledgeDocRepository knowledgeDocRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final AtomicLong counter = new AtomicLong(1);

    private KnowledgeDoc createDoc() {
        String name = "chunk-tag-doc-" + counter.getAndIncrement() + ".md";
        File file = FileFixture.save(fileRepository, name, FileType.KNOWLEDGE);
        KnowledgeDoc doc = KnowledgeDoc.create(file.getId(), "中间表测试文档" + counter.get());
        knowledgeDocRepository.save(doc);
        return doc;
    }

    private Long createTag(String tagName) {
        String vectorLiteral = "[" + "0,".repeat(1023) + "0]";
        jdbcTemplate.update(
                "INSERT INTO tb_rag_tags (tag_name, tag_vector, tag_description, chunks_count) VALUES (?, ?::vector, '', 0)",
                tagName,
                vectorLiteral);
        return jdbcTemplate.queryForObject("SELECT id FROM tb_rag_tags WHERE tag_name = ?", Long.class, tagName);
    }

    private Long insertChunk(Long docId) {
        KnowledgeChunkDO chunk = new KnowledgeChunkDO();
        chunk.setDocId(docId);
        chunk.setChunkVector(new float[1024]);
        chunk.setContent("内容");
        chunk.setVectorStatus("synced");
        chunk.setSource("source");
        knowledgeChunkMapper.insert(chunk);
        return chunk.getId();
    }

    @Test
    @DisplayName("replaceByChunkId: 应重建分片的全部标签关联")
    void replaceByChunkId_shouldRebuildAssociations() {
        KnowledgeDoc doc = createDoc();
        Long chunkId = insertChunk(doc.getId());
        Long tagId1 = createTag("标签1");
        Long tagId2 = createTag("标签2");
        Long tagId3 = createTag("标签3");
        knowledgeChunkTagRepository.replaceByChunkId(chunkId, List.of(tagId1, tagId2));

        knowledgeChunkTagRepository.replaceByChunkId(chunkId, List.of(tagId2, tagId3));

        assertThat(
                jdbcTemplate.queryForList(
                        "SELECT tag_id FROM tb_rag_chunk_tags WHERE chunk_id = ?",
                        Long.class,
                        chunkId))
                                .containsExactlyInAnyOrder(tagId2, tagId3);
    }

    @Test
    @DisplayName("replaceByChunkId: 空标签列表应清空关联")
    void replaceByChunkId_withEmptyTags_shouldClearAssociations() {
        KnowledgeDoc doc = createDoc();
        Long chunkId = insertChunk(doc.getId());
        Long tagId = createTag("待清空标签");
        knowledgeChunkTagRepository.replaceByChunkId(chunkId, List.of(tagId));

        knowledgeChunkTagRepository.replaceByChunkId(chunkId, List.of());

        assertThat(knowledgeChunkTagRepository.countByTagId(tagId)).isZero();
    }

    @Test
    @DisplayName("deleteByTagId: 应删除标签的全部关联")
    void deleteByTagId_shouldRemoveAssociations() {
        KnowledgeDoc doc = createDoc();
        Long chunkId1 = insertChunk(doc.getId());
        Long chunkId2 = insertChunk(doc.getId());
        Long tagId = createTag("删除标签");
        knowledgeChunkTagRepository.replaceByChunkId(chunkId1, List.of(tagId));
        knowledgeChunkTagRepository.replaceByChunkId(chunkId2, List.of(tagId));

        knowledgeChunkTagRepository.deleteByTagId(tagId);

        assertThat(knowledgeChunkTagRepository.countByTagId(tagId)).isZero();
    }

    @Test
    @DisplayName("deleteByDocId: 应删除文档下所有分片的关联")
    void deleteByDocId_shouldRemoveDocAssociations() {
        KnowledgeDoc doc = createDoc();
        Long chunkId = insertChunk(doc.getId());
        Long tagId = createTag("文档删除标签");
        knowledgeChunkTagRepository.replaceByChunkId(chunkId, List.of(tagId));
        KnowledgeDoc otherDoc = createDoc();
        Long otherChunkId = insertChunk(otherDoc.getId());
        knowledgeChunkTagRepository.replaceByChunkId(otherChunkId, List.of(tagId));

        knowledgeChunkTagRepository.deleteByDocId(doc.getId());

        assertThat(knowledgeChunkTagRepository.countByTagId(tagId)).isEqualTo(1);
    }

    @Test
    @DisplayName("countByTagId: 应统计标签关联的分段数")
    void countByTagId_shouldCountAssociations() {
        KnowledgeDoc doc = createDoc();
        Long chunkId1 = insertChunk(doc.getId());
        Long chunkId2 = insertChunk(doc.getId());
        Long tagId = createTag("计数标签");
        knowledgeChunkTagRepository.replaceByChunkId(chunkId1, List.of(tagId));
        knowledgeChunkTagRepository.replaceByChunkId(chunkId2, List.of(tagId));

        assertThat(knowledgeChunkTagRepository.countByTagId(tagId)).isEqualTo(2);
    }
}
