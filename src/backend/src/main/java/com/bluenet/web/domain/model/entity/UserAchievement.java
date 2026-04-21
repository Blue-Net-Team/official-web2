package com.bluenet.web.domain.model.entity;

import lombok.Data;

@Data
public class UserAchievement {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 关联用户标识。
     */
    private Long userId;
    /**
     * 用户成果关联的成果记录标识。
     */
    private Long achievementId;
}
