package com.bluenet.web.api.dto.adminuser;

import com.bluenet.web.domain.model.enumerate.Direction;
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
@Schema(description = "用户列表项")
public class AdminUserListItemResponseDTO {
    @Schema(description = "用户ID")
    private Long id;
    @Schema(description = "学号")
    private String studentId;
    @Schema(description = "姓名")
    private String username;
    @Schema(description = "昵称")
    private String nickname;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "角色ID")
    private Long roleId;
    @Schema(description = "角色名称")
    private String roleName;
    @Schema(description = "方向")
    private Direction direction;
    @Schema(description = "学院ID")
    private Long collegeId;
    @Schema(description = "学院")
    private String college;
    @Schema(description = "专业")
    private String major;
    @Schema(description = "性别")
    private Gender gender;
    @Schema(description = "岗位")
    private String job;
    @Schema(description = "是否禁用")
    private Boolean disable;
    @Schema(description = "头像文件ID")
    private Long avatarFileId;
    @Schema(description = "考核年级年份")
    private Integer assessmentGradeYear;
}
