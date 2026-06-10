package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.ExperienceType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户经历聚合根
 * <p>
 * 承载用户经历（项目/竞赛/实习）的业务规则和行为
 * </p>
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserExperience {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 关联用户标识。
     */
    private Long userId;
    /**
     * 业务分类或枚举类型。
     */
    private ExperienceType type;
    /**
     * 标题或名称，用于列表和详情展示。
     */
    private String title;
    /**
     * 正文内容、题目内容或结构化配置内容。
     */
    private String content;
    /**
     * 经历、考核或有效期的开始时间。
     */
    private LocalDateTime startTime;
    /**
     * 经历、考核或有效期的结束时间。
     */
    private LocalDateTime endTime;

    private UserExperience(Long id, Long userId, ExperienceType type, String title,
            String content, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * 构造新聚合根 —— 带领域校验
     */
    public static UserExperience create(Long userId, ExperienceType type, String title,
            String content, LocalDateTime startTime, LocalDateTime endTime) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (type == null) {
            throw new IllegalArgumentException("经历类型不能为空");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("名称不能为空");
        }
        return new UserExperience(null, userId, type, title.trim(), content, startTime, endTime);
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     */
    public static UserExperience reconstruct(Long id, Long userId, ExperienceType type, String title,
            String content, LocalDateTime startTime, LocalDateTime endTime) {
        return new UserExperience(id, userId, type, title, content, startTime, endTime);
    }

    /**
     * 更新经历详情 —— 带领域校验
     */
    public void updateDetails(String title, String content, LocalDateTime startTime, LocalDateTime endTime) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("名称不能为空");
        }
        this.title = title.trim();
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
