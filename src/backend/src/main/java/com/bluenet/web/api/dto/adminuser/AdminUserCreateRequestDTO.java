package com.bluenet.web.api.dto.adminuser;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "创建用户请求")
public class AdminUserCreateRequestDTO {
    @NotBlank(message = "学号不能为空")
    @Pattern(regexp = "^\\d{12,13}$", message = "学号必须为12-13位数字")
    @Schema(description = "学号，12-13位数字，前4位为入学年份", example = "20210001001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String studentId;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱地址", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "姓名不能为空")
    @Schema(description = "姓名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码（前端已进行 SHA256 哈希）", example = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "昵称", example = "小张")
    private String nickname;

    @NotNull(message = "角色不能为空")
    @Schema(description = "角色ID", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long roleId;

    @Schema(description = "学院ID", example = "1")
    private Long collegeId;

    @Schema(description = "专业", example = "计算机科学与技术")
    private String major;

    @Schema(description = "方向", example = "computer_vision")
    private Direction direction;

    @Schema(description = "性别", example = "MALE")
    private Gender gender;

    @Schema(description = "岗位", example = "后端开发")
    private String job;

    @Schema(description = "考核年级年份", example = "2024")
    private Integer assessmentGradeYear;
}
