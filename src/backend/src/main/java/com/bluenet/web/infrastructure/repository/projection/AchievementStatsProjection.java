package com.bluenet.web.infrastructure.repository.projection;

import lombok.Data;

/**
 * Aggregate query projection for achievement statistics; it is not a table DO.
 */
@Data
public class AchievementStatsProjection {
    /**
     * 成果总数量。
     */
    private Long totalAchievements;
    /**
     * 国家级成果数量。
     */
    private Long nationalCount;
    /**
     * 省级成果数量。
     */
    private Long provincialCount;
    /**
     * 校级成果数量。
     */
    private Long schoolCount;
}
