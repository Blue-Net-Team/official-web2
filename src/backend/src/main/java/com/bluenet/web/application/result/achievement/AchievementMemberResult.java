package com.bluenet.web.application.result.achievement;

/**
 * 成就关联成员简要信息。
 * <p>
 * 用于成就卡片展示系统内成员（头像+姓名，可跳转成员主页）。
 * </p>
 */
public record AchievementMemberResult(
        /** 成员用户ID */
        Long userId,
        /** 成员姓名 */
        String username,
        /** 头像文件ID */
        Long avatarFileId) {
}
