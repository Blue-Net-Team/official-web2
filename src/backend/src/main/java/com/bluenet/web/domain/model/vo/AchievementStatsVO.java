package com.bluenet.web.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AchievementStatsVO {
    private Long totalAchievements;
    private Long nationalCount;
    private Long provincialCount;
    private Long schoolCount;
}
