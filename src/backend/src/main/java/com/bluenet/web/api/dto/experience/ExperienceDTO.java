package com.bluenet.web.api.dto.experience;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 经历DTO - 统一的返回格式 根据type不同，包含不同的字段
 */
@Schema(description = "经历信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceDTO {
    @Schema(description = "经历ID")
    private String id;

    @Schema(description = "经历类型: project/competition/internhip")
    private String type;

    // 通用字段
    @Schema(description = "标题（项目名/竞赛名/公司名）")
    private String name;

    @Schema(description = "开始时间")
    private String startDate;

    @Schema(description = "结束时间")
    private String endDate;

    // 项目特有字段
    @Schema(description = "角色")
    private String role;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "技术栈（项目类型）")
    private List<String> techStack;

    @Schema(description = "演示链接（项目类型）")
    private String demoUrl;

    // 竞赛特有字段
    @Schema(description = "参赛时间（竞赛类型）")
    private String date;

    @Schema(description = "竞赛级别（竞赛类型）")
    private String level;

    @Schema(description = "获奖等级（竞赛类型）")
    private String award;

    @Schema(description = "团队人数（竞赛类型）")
    private Integer teamSize;

    @Schema(description = "证书链接（竞赛类型）")
    private String certificateUrl;

    // 实习特有字段
    @Schema(description = "公司名称（实习类型，同name）")
    private String company;

    @Schema(description = "实习岗位（实习类型）")
    private String position;

    @Schema(description = "状态（实习类型）: ACTIVE/ENDED")
    private String status;

    @Schema(description = "成就列表（实习类型）")
    private List<String> achievements;

    /**
     * 根据类型设置名称
     */
    public void setNameByType(String type, String title) {
        this.type = type;
        if ("INTERNSHIP".equals(type)) {
            this.company = title;
        }
        this.name = title;
    }
}
