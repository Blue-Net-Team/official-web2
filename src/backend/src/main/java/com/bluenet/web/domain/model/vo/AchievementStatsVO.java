package com.bluenet.web.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AchievementStatsVO {
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
