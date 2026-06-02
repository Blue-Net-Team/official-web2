package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.KnowledgeTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
     * 更新标签描述。
     *
     * @param id
     *            标签ID
     * @param description
     *            新描述
     * @return 影响行数
     */
    int updateDescription(Long id, String description);
}
