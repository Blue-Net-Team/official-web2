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

    /**
     * 按名称查询标签。
     *
     * @param tagName
     *            标签名
     * @return 查询到的标签；不存在时为空
     */
    Optional<KnowledgeTag> findByName(String tagName);

    /**
     * 判断标签名是否已存在（排除指定ID，用于重名校验）。
     *
     * @param tagName
     *            标签名
     * @param excludeId
     *            需要排除的标签ID（新建时传 null）
     * @return 已存在返回 true
     */
    boolean existsByName(String tagName, Long excludeId);

    /**
     * 按主键删除标签。
     *
     * @param id
     *            标签ID
     */
    void deleteById(Long id);
}
