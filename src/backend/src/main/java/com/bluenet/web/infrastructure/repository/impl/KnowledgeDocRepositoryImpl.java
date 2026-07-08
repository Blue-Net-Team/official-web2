package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.KnowledgeDoc;
import com.bluenet.web.domain.model.enumerate.DocParseStatus;
import com.bluenet.web.domain.repository.KnowledgeDocRepository;
import com.bluenet.web.infrastructure.repository.converter.KnowledgeDocRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.KnowledgeDocDO;
import com.bluenet.web.infrastructure.repository.mapper.KnowledgeDocMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 知识库文档仓储实现。
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class KnowledgeDocRepositoryImpl implements KnowledgeDocRepository {

    private final KnowledgeDocMapper knowledgeDocMapper;
    private final KnowledgeDocRepositoryConverter converter;

    @Override
    public org.springframework.data.domain.Page<KnowledgeDoc> findAll(Pageable pageable) {
        Page<KnowledgeDocDO> mpPage = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        QueryWrapper<KnowledgeDocDO> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("created_at");
        Page<KnowledgeDocDO> result = knowledgeDocMapper.selectPage(mpPage, wrapper);

        List<KnowledgeDoc> items = converter.toEntityList(result.getRecords());
        return new PageImpl<>(items, pageable, result.getTotal());
    }

    @Override
    public Optional<KnowledgeDoc> findById(Long id) {
        KnowledgeDocDO dataObject = knowledgeDocMapper.selectById(id);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    @Override
    public void save(KnowledgeDoc doc) {
        KnowledgeDocDO dataObject = converter.toDataObject(doc);
        if (dataObject.getId() == null) {
            knowledgeDocMapper.insert(dataObject);
            doc.setId(dataObject.getId());
        } else {
            knowledgeDocMapper.updateById(dataObject);
        }
        log.debug("知识库文档保存成功: id={}", doc.getId());
    }
    @Override
    public void deleteById(Long id) {
        knowledgeDocMapper.deleteById(id);
        log.debug("知识库文档删除成功: id={}", id);
    }

    @Override
    public void updateStatus(Long id, DocParseStatus status, Integer chunkCount, String errorMessage) {
        knowledgeDocMapper.updateStatus(
                id,
                status,
                chunkCount != null ? chunkCount : 0,
                errorMessage != null ? errorMessage : "");
        log.debug("知识库文档状态更新成功: id={}, status={}", id, status.getValue());
    }
}
