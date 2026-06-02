package com.bluenet.web.api.dto.user;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Schema(description = "用户基本信息")
@Data
@AllArgsConstructor
@Builder
public class UserInfo {
    @Schema(description = "用户 ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "学院")
    private String college;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像文件ID")
    private Long avatarFileId;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "方向/类型")
    private Direction direction;

    @Schema(description = "性别")
    private Gender gender;

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "GitHub 用户名")
    private String githubUsername;

    @Schema(description = "微信二维码URL")
    private String wechatQrcode;
}
