package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.KnowledgeDoc;
import com.bluenet.web.domain.model.enumerate.DocParseStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.KnowledgeDocRepository;
import com.bluenet.web.infrastructure.repository.dataobject.KnowledgeDocDO;
import com.bluenet.web.infrastructure.repository.mapper.KnowledgeDocMapper;
import com.bluenet.web.testsupport.fixture.FileFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * KnowledgeDocRepositoryImpl 集成测试。
 */
@DisplayName("KnowledgeDocRepositoryImpl 集成测试")
class KnowledgeDocRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private KnowledgeDocRepository knowledgeDocRepository;

    @Autowired
    private KnowledgeDocMapper knowledgeDocMapper;

    @Autowired
    private FileRepository fileRepository;

    private final AtomicLong counter = new AtomicLong(1);

    private File createFile() {
        String name = "knowledge-doc-" + counter.getAndIncrement() + ".pdf";
        return FileFixture.save(fileRepository, name, FileType.KNOWLEDGE);
    }

    private KnowledgeDoc createDoc(String title) {
        File file = createFile();
        KnowledgeDoc doc = KnowledgeDoc.create(file.getId(), title);
        knowledgeDocRepository.save(doc);
        return doc;
    }

    @Test
    @DisplayName("save: 新文档应插入并回写ID")
    void save_newDoc_shouldInsertAndReturnId() {
        KnowledgeDoc doc = createDoc("测试文档");

        assertThat(doc.getId()).isNotNull();
        KnowledgeDocDO dataObject = knowledgeDocMapper.selectById(doc.getId());
        assertThat(dataObject).isNotNull();
        assertThat(dataObject.getTitle()).isEqualTo("测试文档");
        assertThat(dataObject.getStatus()).isEqualTo(DocParseStatus.PENDING);
    }

    @Test
    @DisplayName("save: 已有文档应更新字段")
    void save_existingDoc_shouldUpdateFields() {
        KnowledgeDoc doc = createDoc("旧标题");
        doc.updateStatus(DocParseStatus.COMPLETED, 5, "");

        knowledgeDocRepository.save(doc);

        KnowledgeDocDO updated = knowledgeDocMapper.selectById(doc.getId());
        assertThat(updated.getStatus()).isEqualTo(DocParseStatus.COMPLETED);
        assertThat(updated.getChunkCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        KnowledgeDoc doc = createDoc("按ID查询");

        Optional<KnowledgeDoc> found = knowledgeDocRepository.findById(doc.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("按ID查询");

        assertThat(knowledgeDocRepository.findById(-1L)).isEmpty();
    }

    @Test
    @DisplayName("findAll: 应按创建时间倒序分页返回")
    void findAll_shouldPaginate() {
        KnowledgeDoc doc1 = createDoc("文档1");
        KnowledgeDoc doc2 = createDoc("文档2");

        Page<KnowledgeDoc> page = knowledgeDocRepository.findAll(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(page.getContent())
                .extracting(KnowledgeDoc::getId)
                .contains(doc1.getId(), doc2.getId());
    }

    @Test
    @DisplayName("deleteById: 应删除文档")
    void deleteById_shouldRemoveDoc() {
        KnowledgeDoc doc = createDoc("待删除文档");
        Long docId = doc.getId();

        knowledgeDocRepository.deleteById(docId);

        assertThat(knowledgeDocMapper.selectById(docId)).isNull();
    }
}
