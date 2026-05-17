package com.bluenet.web.api.dto.adminuser;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "更新用户信息请求")
public class AdminUserUpdateRequestDTO {
    @Schema(description = "角色ID", example = "2")
    private Long roleId;

    @Schema(description = "方向", example = "computer_vision")
    private Direction direction;

    @Schema(description = "禁用状态", example = "true")
    private Boolean disable;

    @Schema(description = "岗位", example = "测试工程师")
    private String job;

    @Schema(description = "学号，12-13位数字，前4位为入学年份", example = "20210001001")
    private String studentId;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱地址", example = "user@example.com")
    private String email;

    @Schema(description = "姓名", example = "张三")
    private String username;

    @Schema(description = "昵称", example = "小张")
    private String nickname;

    @Schema(description = "学院ID", example = "1")
    private Long collegeId;

    @Schema(description = "专业", example = "计算机科学与技术")
    private String major;

    @Schema(description = "性别", example = "MALE")
    private Gender gender;

    @Schema(description = "考核年级年份", example = "2024")
    private Integer assessmentGradeYear;
}
