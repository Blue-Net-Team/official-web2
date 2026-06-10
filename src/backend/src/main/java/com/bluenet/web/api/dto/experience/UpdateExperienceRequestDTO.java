package com.bluenet.web.api.dto.experience;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 更新经历请求DTO
 */
@Schema(description = "更新经历请求")
@Data
public class UpdateExperienceRequestDTO {
    @Schema(description = "标题（项目名/竞赛名/公司名）")
    private String name;

    @Schema(description = "开始时间，格式：yyyy.MM")
    private String startDate;

    @Schema(description = "结束时间，格式：yyyy.MM")
    private String endDate;

    @Schema(description = "描述")
    private String description;

    // 项目字段
    @Schema(description = "角色")
    private String role;

    @Schema(description = "技术栈（项目类型）")
    private List<String> techStack;

    @Schema(description = "演示链接（项目类型）")
    private String demoUrl;

    // 竞赛字段
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

    // 实习字段
    @Schema(description = "公司名称（实习类型），与 name 同义")
    private String company;

    @Schema(description = "实习岗位（实习类型）")
    private String position;

    @Schema(description = "状态（实习类型）: active/ended")
    private String status;

    @Schema(description = "成就列表（实习类型）")
    private List<String> achievements;
}
