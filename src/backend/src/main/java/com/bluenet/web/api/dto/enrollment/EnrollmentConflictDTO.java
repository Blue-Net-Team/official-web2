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
@Schema(description = "学号冲突响应（409返回）")
public class EnrollmentConflictDTO {
    @Schema(description = "已存在的报名ID", example = "123")
    private Long id;

    @Schema(description = "真实姓名", example = "张三")
    private String username;

    @Schema(description = "学号", example = "20210001001")
    private String studentId;

    @Schema(description = "报名状态", example = "pending")
    private EnrollStatus status;

    @Schema(description = "报名方向", example = "computer_vision")
    private Direction direction;
}
