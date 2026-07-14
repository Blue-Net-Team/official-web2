package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.KnowledgeTag;
import com.bluenet.web.domain.repository.KnowledgeTagRepository;
import com.bluenet.web.infrastructure.repository.dataobject.KnowledgeTagDO;
import com.bluenet.web.infrastructure.repository.mapper.KnowledgeTagMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * KnowledgeTagRepositoryImpl 集成测试。
 * <p>
 * 注意：当前 {@code tb_rag_tags.tag_vector} 列为 NOT NULL，但 {@link KnowledgeTagDO}
 * 未映射该字段，save 可能触发数据库非空约束异常。该问题属于生产代码缺陷，本测试保留以暴露此问题。
 * </p>
 */
@DisplayName("KnowledgeTagRepositoryImpl 集成测试")
class KnowledgeTagRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private KnowledgeTagRepository knowledgeTagRepository;

    @Autowired
    private KnowledgeTagMapper knowledgeTagMapper;

    private final AtomicLong counter = new AtomicLong(1);

    private String nextTagName() {
        return "标签" + counter.getAndIncrement();
    }

    private KnowledgeTag createTag(String tagName) {
        KnowledgeTag tag = KnowledgeTag.create(tagName, tagName + "描述");
        knowledgeTagRepository.save(tag);
        return tag;
    }

    @Test
    @DisplayName("save: 新标签应插入并回写ID")
    void save_newTag_shouldInsertAndReturnId() {
        KnowledgeTag tag = createTag(nextTagName());

        assertThat(tag.getId()).isNotNull();
        KnowledgeTagDO dataObject = knowledgeTagMapper.selectById(tag.getId());
        assertThat(dataObject).isNotNull();
        assertThat(dataObject.getTagName()).isEqualTo(tag.getTagName());
    }

    @Test
    @DisplayName("save: 已有标签应更新描述")
    void save_existingTag_shouldUpdateDescription() {
        KnowledgeTag tag = createTag(nextTagName());
        tag.updateDescription("更新后的描述");

        knowledgeTagRepository.save(tag);

        KnowledgeTagDO updated = knowledgeTagMapper.selectById(tag.getId());
        assertThat(updated.getTagDescription()).isEqualTo("更新后的描述");
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        KnowledgeTag tag = createTag(nextTagName());

        Optional<KnowledgeTag> found = knowledgeTagRepository.findById(tag.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTagName()).isEqualTo(tag.getTagName());

        assertThat(knowledgeTagRepository.findById(-1L)).isEmpty();
    }

    @Test
    @DisplayName("findAll: 应按关联分段数倒序分页返回")
    void findAll_shouldPaginate() {
        KnowledgeTag tag1 = createTag(nextTagName());
        KnowledgeTag tag2 = createTag(nextTagName());

        Page<KnowledgeTag> page = knowledgeTagRepository.findAll(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(page.getContent())
                .extracting(KnowledgeTag::getId)
                .contains(tag1.getId(), tag2.getId());
    }
}
