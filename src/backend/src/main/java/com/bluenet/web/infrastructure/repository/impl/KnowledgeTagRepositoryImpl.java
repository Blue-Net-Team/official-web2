package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.KnowledgeTag;
import com.bluenet.web.domain.repository.KnowledgeTagRepository;
import com.bluenet.web.infrastructure.repository.converter.KnowledgeTagRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.KnowledgeTagDO;
import com.bluenet.web.infrastructure.repository.mapper.KnowledgeTagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 知识库标签仓储实现。
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class KnowledgeTagRepositoryImpl implements KnowledgeTagRepository {

    private final KnowledgeTagMapper knowledgeTagMapper;
    private final KnowledgeTagRepositoryConverter converter;

    @Override
    public org.springframework.data.domain.Page<KnowledgeTag> findAll(Pageable pageable) {
        Page<KnowledgeTagDO> mpPage = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        QueryWrapper<KnowledgeTagDO> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("chunks_count");
        Page<KnowledgeTagDO> result = knowledgeTagMapper.selectPage(mpPage, wrapper);

        List<KnowledgeTag> items = converter.toEntityList(result.getRecords());
        return new PageImpl<>(items, pageable, result.getTotal());
    }

    @Override
    public Optional<KnowledgeTag> findById(Long id) {
        KnowledgeTagDO dataObject = knowledgeTagMapper.selectById(id);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    @Override
    public void save(KnowledgeTag tag) {
        KnowledgeTagDO dataObject = converter.toDataObject(tag);
        if (dataObject.getId() == null) {
            if (dataObject.getTagVector() == null) {
                dataObject.setTagVector(new float[VECTOR_DIMENSION]);
            }
            knowledgeTagMapper.insert(dataObject);
            tag.setId(dataObject.getId());
        } else {
            knowledgeTagMapper.updateById(dataObject);
        }
        log.debug("知识库标签保存成功: id={}", tag.getId());
    }

    private static final int VECTOR_DIMENSION = 1024;
}
