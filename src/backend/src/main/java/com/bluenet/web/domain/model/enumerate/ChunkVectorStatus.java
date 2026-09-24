package com.bluenet.web.domain.model.enumerate;

import io.github.ivencn.infra.web.enumx.ValueEnum;

import lombok.Getter;

/**
 * 知识库分段向量同步状态枚举。
 */
@Getter
public enum ChunkVectorStatus implements ValueEnum {
    SYNCED("synced", "已同步"),
    EMBEDDING("embedding", "向量化中");

    private final String value;
    private final String description;

    ChunkVectorStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
