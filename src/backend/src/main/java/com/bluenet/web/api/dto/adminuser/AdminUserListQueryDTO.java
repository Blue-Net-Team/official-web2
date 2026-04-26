package com.bluenet.web.api.dto.adminuser;

import com.bluenet.web.domain.model.enumerate.Direction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "用户列表查询参数")
public class AdminUserListQueryDTO {
    @Schema(description = "页码，从0开始", example = "0", defaultValue = "0")
    @Builder.Default
    private Integer page = 0;

    @Schema(description = "每页数量，默认20，最大100", example = "20", defaultValue = "20")
    @Builder.Default
    private Integer size = 20;

    @Schema(description = "角色ID筛选", example = "1")
    private Long roleId;

    @Schema(description = "方向筛选", example = "computer_vision")
    private Direction direction;

    @Schema(description = "学院ID筛选", example = "1")
    private Long collegeId;

    @Schema(description = "关键词搜索（学号/姓名）", example = "张三")
    private String keyword;
}
