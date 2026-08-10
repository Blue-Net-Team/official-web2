package com.bluenet.web.application.result.common;

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
public class TabCounts {
    /**
     * 项目经历数
     */
    private Integer projects;

    /**
     * 个人成就数
     */
    private Integer achievements;

    /**
     * 实习经历数
     */
    private Integer internships;
}
