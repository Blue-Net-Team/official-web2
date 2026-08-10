package com.bluenet.web.application.command.wpsform;

import jakarta.validation.constraints.NotBlank;

/**
 * WPS 智能表单聚合的命令对象集合。
 * <p>
 * 定义 WPS 表单回调在应用层处理时使用的命令参数。
 * </p>
 */
public final class WpsFormCommands {

    private WpsFormCommands() {
    }

    /**
     * 根据 WPS 表单提交内容创建用户的命令。
     */
    public record CreateUserFromWpsFormCommand(
            /** 学号 */
            @NotBlank(message = "学号不能为空") String studentId,
            /** 姓名 */
            @NotBlank(message = "姓名不能为空") String username,
            /** 邮箱 */
            @NotBlank(message = "邮箱不能为空") String email,
            /** 方向（中文，如"视觉"、"计算机视觉"） */
            @NotBlank(message = "方向不能为空") String directionText,
            /** 专业 */
            String major,
            /** 学院（中文文本） */
            String collegeText,
            /** 性别（中文文本） */
            String genderText) {

        public CreateUserFromWpsFormCommand {
            if (studentId != null) {
                studentId = studentId.trim();
            }
            if (username != null) {
                username = username.trim();
            }
            if (email != null) {
                email = email.trim();
            }
            if (directionText != null) {
                directionText = directionText.trim();
            }
            if (major != null) {
                major = major.trim();
            }
            if (collegeText != null) {
                collegeText = collegeText.trim();
            }
            if (genderText != null) {
                genderText = genderText.trim();
            }
        }
    }
}
