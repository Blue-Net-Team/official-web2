package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 成就聚合根
 * <p>
 * 承载成就相关的业务规则和行为
 * </p>
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Achievement {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 标题或名称，用于列表和详情展示。
     */
    private String title;
    /**
     * 业务分类或枚举类型。
     */
    private AchievementType type;
    /**
     * 成果关联的竞赛、项目或业务对象名称。
     */
    private String relateTo;
    /**
     * 成果取得或获奖发生的时间。
     */
    private LocalDate achieveAt;
    /**
     * 奖项级别，例如国家级、省级或校级。
     */
    private AwardLevel awardLevel;
    /**
     * 奖项名称或获奖名次。
     */
    private String awardName;
    /**
     * 关联文件记录标识。
     */
    private Long fileId;
    /**
     * 关联的系统内成员ID列表。
     */
    private List<Long> memberIds = new ArrayList<>();
    /**
     * 外部协作者姓名列表（非系统用户）。
     */
    private List<String> externalMembers = new ArrayList<>();

    private Achievement(Long id, String title, AchievementType type, String relateTo, LocalDate achieveAt,
            AwardLevel awardLevel, String awardName, Long fileId) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.relateTo = relateTo;
        this.achieveAt = achieveAt;
        this.awardLevel = awardLevel;
        this.awardName = awardName;
        this.fileId = fileId;
    }

    /**
     * 构造新聚合根 —— 带领域校验
     *
     * @param title
     *            成就标题
     * @param type
     *            成就类型
     * @param relateTo
     *            关联项（竞赛名称/期刊名称）
     * @param achieveAt
     *            获奖日期
     * @param awardLevel
     *            奖项级别
     * @param awardName
     *            奖项名称
     * @param fileId
     *            文件ID
     * @return 新的成就实体
     * @throws IllegalArgumentException
     *             如果竞赛成就未指定奖项级别
     */
    public static Achievement create(String title, AchievementType type, String relateTo, LocalDate achieveAt,
            AwardLevel awardLevel, String awardName, Long fileId) {
        if (type == AchievementType.COMPETITION && awardLevel == null) {
            throw new IllegalArgumentException("竞赛成就必须指定奖项级别");
        }
        return new Achievement(null, title, type, relateTo, achieveAt, awardLevel, awardName, fileId);
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     *
     * @param id
     *            成就ID
     * @param title
     *            成就标题
     * @param type
     *            成就类型
     * @param relateTo
     *            关联项
     * @param achieveAt
     *            获奖日期
     * @param awardLevel
     *            奖项级别
     * @param awardName
     *            奖项名称
     * @param fileId
     *            文件ID
     * @return 重建的成就实体
     */
    public static Achievement reconstruct(Long id, String title, AchievementType type, String relateTo,
            LocalDate achieveAt, AwardLevel awardLevel, String awardName, Long fileId) {
        return new Achievement(id, title, type, relateTo, achieveAt, awardLevel, awardName, fileId);
    }

    /**
     * 更新成就信息 —— 带领域校验
     *
     * @param title
     *            新标题
     * @param type
     *            新类型
     * @param relateTo
     *            新关联项
     * @param achieveAt
     *            新获奖日期
     * @param awardLevel
     *            新奖项级别
     * @param awardName
     *            新奖项名称
     * @param fileId
     *            新文件ID
     * @throws IllegalArgumentException
     *             如果竞赛成就未指定奖项级别
     */
    public void update(String title, AchievementType type, String relateTo, LocalDate achieveAt,
            AwardLevel awardLevel, String awardName, Long fileId) {
        if (type == AchievementType.COMPETITION && awardLevel == null) {
            throw new IllegalArgumentException("竞赛成就必须指定奖项级别");
        }
        this.title = title;
        this.type = type;
        this.relateTo = relateTo;
        this.achieveAt = achieveAt;
        this.awardLevel = awardLevel;
        this.awardName = awardName;
        this.fileId = fileId;
    }

    /**
     * 指派成就关联成员 —— 全量替换系统内成员与外部协作者
     *
     * @param userIds
     *            系统内成员ID列表，自动去重，null 视为空
     * @param externalMembers
     *            外部协作者姓名列表，自动 trim、去空、去重，null 视为空
     * @throws IllegalArgumentException
     *             如果外部协作者姓名超过100个字符
     */
    public void assignMembers(List<Long> userIds, List<String> externalMembers) {
        this.memberIds = userIds == null
                ? new ArrayList<>()
                : new ArrayList<>(userIds.stream().filter(id -> id != null).distinct().toList());
        this.externalMembers = normalizeExternalMembers(externalMembers);
    }

    private static List<String> normalizeExternalMembers(List<String> names) {
        List<String> normalized = new ArrayList<>();
        if (names == null) {
            return normalized;
        }
        for (String name : names) {
            if (name == null) {
                continue;
            }
            String trimmed = name.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.length() > 100) {
                throw new IllegalArgumentException("外部协作者姓名不能超过100字符");
            }
            if (!normalized.contains(trimmed)) {
                normalized.add(trimmed);
            }
        }
        return normalized;
    }
}
