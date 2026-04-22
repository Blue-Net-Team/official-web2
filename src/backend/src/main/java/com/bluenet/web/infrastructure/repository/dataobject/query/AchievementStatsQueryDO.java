package com.bluenet.web.infrastructure.repository.dataobject.query;

import lombok.Data;

/**
 * 成果统计查询数据对象，仅用于承接 XML 聚合查询结果。
 */
@Data
public class AchievementStatsQueryDO {
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
