package com.bluenet.web.api.dto.member;

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
@Schema(description = "成员列表查询参数")
public class MemberListQueryDTO {
    @Schema(description = "页码，从0开始", example = "0", defaultValue = "0")
    private Integer page = 0;

    @Schema(description = "每页数量，默认20，最大100", example = "20", defaultValue = "20")
    private Integer size = 20;

    @Schema(description = "方向筛选", example = "computer_vision")
    private Direction direction;
}
