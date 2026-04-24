package com.bluenet.web.application.message.template;

import lombok.Getter;

/**
 * 邮箱验证码场景枚举。
 * <p>
 * 定义各验证码场景的显示文案，供 {@link EmailVerificationCodeTemplate} 使用。
 * </p>
 */
@Getter
public enum VerificationCodeScene {

    LOGIN("登录", "您的验证码为：", "验证码5分钟内有效。"),
    RESET_PASSWORD("密码重置", "您正在重置密码，验证码为：", "验证码5分钟内有效，如非本人操作请忽略此邮件"),
    CHANGE_EMAIL_ORIGINAL("修改邮箱 - 验证原邮箱", "您的验证码为：", "验证码5分钟内有效，请勿泄露给他人。"),
    CHANGE_EMAIL_NEW("修改邮箱 - 验证新邮箱", "您的验证码为：", "验证码5分钟内有效，请勿泄露给他人。");

    private final String title;
    private final String description;
    private final String footer;

    VerificationCodeScene(String title, String description, String footer) {
        this.title = title;
        this.description = description;
        this.footer = footer;
    }
}
