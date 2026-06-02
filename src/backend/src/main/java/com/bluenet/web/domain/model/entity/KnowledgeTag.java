package com.bluenet.web.domain.model.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库标签领域实体。
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeTag {

    private Long id;
    private String tagName;
    private String tagDescription;
    private Integer chunksCount;

    /**
     * 创建新标签。
     *
     * @param tagName
     *            标签名
     * @param tagDescription
     *            标签描述
     * @return 新标签实体
     */
    public static KnowledgeTag create(String tagName, String tagDescription) {
        if (tagName == null || tagName.isBlank()) {
            throw new IllegalArgumentException("标签名不能为空");
        }
        return new KnowledgeTag(null, tagName, tagDescription != null ? tagDescription : "", 0);
    }

    /**
     * 从数据库重建。
     */
    public static KnowledgeTag reconstruct(Long id, String tagName, String tagDescription, Integer chunksCount) {
        return new KnowledgeTag(id, tagName, tagDescription, chunksCount);
    }

    /**
     * 更新描述。
     *
     * @param description
     *            新描述
     */
    public void updateDescription(String description) {
        this.tagDescription = description != null ? description : "";
    }
}
