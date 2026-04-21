package com.bluenet.web.domain.model.vo.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 竞赛经历内容
 * <p>
 * 存储在 tb_user_experience.content 字段中的JSON结构。
 * </p>
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompetitionContent {
    /**
     * 角色
     */
    private String role;

    /**
     * 参赛时间
     */
    private String date;

    /**
     * 竞赛级别（市级/省级/国家级）
     */
    private String level;

    /**
     * 获奖等级（一等奖/二等奖/三等奖）
     */
    private String award;

    /**
     * 团队人数
     */
    private Integer teamSize;

    /**
     * 描述
     */
    private String description;

    /**
     * 证书链接
     */
    /**
     * 证书或证明材料的访问地址。
     */
    @JsonProperty("certificateUrl")
    private String certificateUrl;
}
