package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.KnowledgeTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 知识库标签仓储接口。
 */
public interface KnowledgeTagRepository {

    /**
     * 分页查询所有标签。
     *
     * @param pageable
     *            分页参数
     * @return 标签分页结果
     */
    Page<KnowledgeTag> findAll(Pageable pageable);

    /**
     * 按主键查询标签。
     *
     * @param id
     *            标签ID
     * @return 查询到的标签；不存在时为空
     */
    Optional<KnowledgeTag> findById(Long id);

    /**
     * 保存或更新标签。
     *
     * @param tag
     *            标签实体。若 id 为空则插入，否则按 id 更新。
     */
    void save(KnowledgeTag tag);
}
