package com.bluenet.web.api.dto.member;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.infrastructure.security.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "成员详细信息")
public class MemberDetailDTO {
    @Schema(description = "成员ID", example = "123")
    private Long id;

    @Schema(description = "入学年份", example = "2021")
    private Integer enrollmentYear;

    @Schema(description = "学号", example = "202511520254")
    private String studentId;

    @Schema(description = "真实姓名", example = "张三")
    private String username;

    @Schema(description = "昵称", example = "小张")
    private String nickname;

    @Schema(description = "方向", example = "computer_vision")
    private Direction direction;

    @Schema(description = "职责", example = "后端开发")
    private String job;

    @Schema(description = "头像文件ID", example = "456")
    private Long avatarFileId;

    @Schema(description = "学院", example = "计算机学院")
    private String college;

    @Schema(description = "专业", example = "计算机科学与技术")
    private String major;

    @Schema(description = "性别", example = "male")
    private Gender gender;

    @Schema(description = "角色", example = "MEMBER")
    private RoleType role;

    @Schema(description = "个人简介", example = "我是一名热爱计算机视觉的学生，喜欢参加各种竞赛。")
    private String bio;

    @Schema(description = "GitHub用户名", example = "zhangsan")
    private String githubUsername;

    @Schema(description = "微信二维码URL", example = "/api/v1/files/789")
    private String wechatQrcode;
}
