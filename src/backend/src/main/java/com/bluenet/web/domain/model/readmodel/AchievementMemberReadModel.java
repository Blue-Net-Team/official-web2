package com.bluenet.web.domain.model.readmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 成就关联成员简要信息读模型。
 * <p>
 * 用于成就卡片展示系统内成员（头像+姓名，可跳转成员主页）。
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AchievementMemberReadModel {
    /**
     * 成员用户ID。
     */
    private Long userId;
    /**
     * 成员姓名。
     */
    private String username;
    /**
     * 成员头像文件标识。
     */
    private Long avatarFileId;
}
