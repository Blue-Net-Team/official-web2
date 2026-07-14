package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.KnowledgeDoc;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.KnowledgeChunkRepository;
import com.bluenet.web.domain.repository.KnowledgeDocRepository;
import com.bluenet.web.infrastructure.repository.dataobject.KnowledgeChunkDO;
import com.bluenet.web.infrastructure.repository.mapper.KnowledgeChunkMapper;
import com.bluenet.web.testsupport.fixture.FileFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * KnowledgeChunkRepositoryImpl 集成测试。
 */
@DisplayName("KnowledgeChunkRepositoryImpl 集成测试")
class KnowledgeChunkRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private KnowledgeChunkRepository knowledgeChunkRepository;

    @Autowired
    private KnowledgeChunkMapper knowledgeChunkMapper;

    @Autowired
    private KnowledgeDocRepository knowledgeDocRepository;

    @Autowired
    private FileRepository fileRepository;

    private final AtomicLong counter = new AtomicLong(1);

    private KnowledgeDoc createDoc() {
        String name = "knowledge-chunk-doc-" + counter.getAndIncrement() + ".pdf";
        File file = FileFixture.save(fileRepository, name, FileType.KNOWLEDGE);
        KnowledgeDoc doc = KnowledgeDoc.create(file.getId(), "分档测试文档" + counter.get());
        knowledgeDocRepository.save(doc);
        return doc;
    }

    private KnowledgeChunkDO insertChunk(Long docId, String content) {
        KnowledgeChunkDO chunk = new KnowledgeChunkDO();
        chunk.setDocId(docId);
        chunk.setChunkVector(new float[VECTOR_DIMENSION]);
        chunk.setContent(content);
        chunk.setTags(List.of("标签"));
        chunk.setSource("source");
        knowledgeChunkMapper.insert(chunk);
        return chunk;
    }

    private static final int VECTOR_DIMENSION = 1024;

    @Test
    @DisplayName("findByDocId: 应按文档ID分页查询分段")
    void findByDocId_shouldPaginate() {
        KnowledgeDoc doc = createDoc();
        KnowledgeChunkDO chunk1 = insertChunk(doc.getId(), "内容1");
        KnowledgeChunkDO chunk2 = insertChunk(doc.getId(), "内容2");
        KnowledgeDoc otherDoc = createDoc();
        insertChunk(otherDoc.getId(), "其他内容");

        Page<com.bluenet.web.domain.model.entity.KnowledgeChunk> page = knowledgeChunkRepository
                .findByDocId(doc.getId(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(com.bluenet.web.domain.model.entity.KnowledgeChunk::getId)
                .containsExactlyInAnyOrder(chunk1.getId(), chunk2.getId());
    }

    @Test
    @DisplayName("deleteByDocId: 应删除指定文档下的所有分段")
    void deleteByDocId_shouldRemoveChunks() {
        KnowledgeDoc doc = createDoc();
        KnowledgeChunkDO chunk = insertChunk(doc.getId(), "待删除");
        KnowledgeDoc otherDoc = createDoc();
        KnowledgeChunkDO otherChunk = insertChunk(otherDoc.getId(), "保留");

        knowledgeChunkRepository.deleteByDocId(doc.getId());

        assertThat(knowledgeChunkMapper.selectById(chunk.getId())).isNull();
        assertThat(knowledgeChunkMapper.selectById(otherChunk.getId())).isNotNull();
    }
}
