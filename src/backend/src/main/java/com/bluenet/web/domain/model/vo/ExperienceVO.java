package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.ExperienceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 用户经历值对象
 * <p>
 * 封装用户经历数据，包含项目、竞赛、实习三种类型。
 * </p>
 */
@Getter
@AllArgsConstructor
@Builder
public class ExperienceVO {
    /**
     * 经历ID
     */
    private Long id;

    /**
     * 经历类型
     */
    private ExperienceType type;

    /**
     * 标题（项目名/竞赛名/公司名）
     */
    private String title;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间（可选）
     */
    private String endTime;

    /**
     * JSON格式的详细内容 根据type不同，解析为ProjectContent、CompetitionContent或InternshipContent
     */
    private String content;
}
