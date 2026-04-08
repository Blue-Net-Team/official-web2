package com.bluenet.web.api.dto.enrollment;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "报名列表查询参数")
public class EnrollmentListQueryDTO {
    @Schema(description = "页码，从0开始", example = "0")
    @Builder.Default
    private Integer page = 0;

    @Schema(description = "每页数量，默认20，最大100", example = "20")
    @Builder.Default
    private Integer size = 20;

    @Schema(description = "状态筛选：pending/approved/rejected", example = "pending")
    private EnrollStatus status;

    @Schema(description = "方向筛选", example = "computer_vision")
    private Direction direction;

    @Schema(description = "关键词搜索（姓名/学号）", example = "张三")
    private String keyword;
}
