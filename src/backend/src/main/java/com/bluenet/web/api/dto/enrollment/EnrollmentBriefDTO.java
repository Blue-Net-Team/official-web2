package com.bluenet.web.api.dto.enrollment;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.enumerate.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "报名简要信息")
public class EnrollmentBriefDTO {
    @Schema(description = "报名ID", example = "123")
    private Long id;

    @Schema(description = "真实姓名", example = "张三")
    private String username;

    @Schema(description = "学号", example = "20210001001")
    private String studentId;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "学院名称", example = "计算机学院")
    private String collegeName;

    @Schema(description = "专业", example = "计算机科学与技术")
    private String major;

    @Schema(description = "性别", example = "male")
    private Gender gender;

    @Schema(description = "报名方向", example = "computer_vision")
    private Direction direction;

    @Schema(description = "报名状态", example = "pending")
    private EnrollStatus status;

    @Schema(description = "头像文件ID", example = "456")
    private Long avatarFileId;
}
