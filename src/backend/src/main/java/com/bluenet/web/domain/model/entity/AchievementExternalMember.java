package com.bluenet.web.domain.model.entity;

import lombok.Data;

/**
 * 成就外部协作者
 * <p>
 * 非系统用户的合作成员，仅做展示，不建立唯一身份。
 * </p>
 */
@Data
public class AchievementExternalMember {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 关联成就标识。
     */
    private Long achievementId;
    /**
     * 外部协作者姓名。
     */
    private String name;
    /**
     * 展示顺序。
     */
    private Integer displayOrder;

    public static AchievementExternalMember create(Long achievementId, String name, Integer displayOrder) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("外部协作者姓名不能为空");
        }
        AchievementExternalMember member = new AchievementExternalMember();
        member.setAchievementId(achievementId);
        member.setName(name.trim());
        member.setDisplayOrder(displayOrder == null ? 0 : displayOrder);
        return member;
    }
}
