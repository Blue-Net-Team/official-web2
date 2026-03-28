package com.bluenet.web.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Tab计数值对象
 * <p>
 * 用于统计用户各类型经历的数量。
 * </p>
 */
@Getter
@AllArgsConstructor
@Builder
public class TabCountsVO {
    /**
     * 项目经历数
     */
    private Integer projects;

    /**
     * 竞赛经历数
     */
    private Integer competitions;

    /**
     * 实习经历数
     */
    private Integer internships;
}
