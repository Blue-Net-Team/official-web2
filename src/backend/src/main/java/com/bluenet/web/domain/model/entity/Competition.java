package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.AwardLevel;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Competition {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 业务对象名称。
     */
    private String name;
    /**
     * 业务对象简称，用于紧凑展示。
     */
    private String shortName;
    /**
     * Logo 图片对应的文件记录标识。
     */
    private Long logoFileId;
    /**
     * 封面图对应的文件记录标识。
     */
    private Long coverFileId;
    /**
     * 项目、竞赛或经历的摘要说明。
     */
    private String summary;
    /**
     * 成果、权限或展示项的层级。
     */
    private AwardLevel level = AwardLevel.PROVINCIAL;
    /**
     * 统计数据对应的月份。
     */
    private String month;
    /**
     * 竞赛或活动主办方。
     */
    private String organizer;
    /**
     * 列表展示排序值，数值越大通常越靠前。
     */
    private Integer sortOrder;

    private Competition(Long id, String name, String shortName, Long logoFileId, Long coverFileId, String summary,
            AwardLevel level, String month, String organizer, Integer sortOrder) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.logoFileId = logoFileId;
        this.coverFileId = coverFileId;
        this.summary = summary;
        this.level = level;
        this.month = month;
        this.organizer = organizer;
        this.sortOrder = sortOrder;
    }

    /**
     * 构造新聚合根 —— 带领域校验
     *
     * @param name
     *            竞赛名称
     * @param shortName
     *            竞赛简称
     * @param logoFileId
     *            Logo文件ID
     * @param coverFileId
     *            封面文件ID
     * @param summary
     *            竞赛简介
     * @param level
     *            竞赛级别
     * @param month
     *            举办月份
     * @param organizer
     *            主办方
     * @param sortOrder
     *            排序值
     * @return 新的竞赛实体
     * @throws IllegalArgumentException
     *             如果名称为空
     */
    public static Competition create(String name, String shortName, Long logoFileId, Long coverFileId, String summary,
            AwardLevel level, String month, String organizer, Integer sortOrder) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("竞赛名称不能为空");
        }
        return new Competition(null, name.trim(), shortName, logoFileId, coverFileId, summary,
                level != null ? level : AwardLevel.PROVINCIAL, month, organizer, sortOrder);
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     *
     * @param id
     *            竞赛ID
     * @param name
     *            竞赛名称
     * @param shortName
     *            竞赛简称
     * @param logoFileId
     *            Logo文件ID
     * @param coverFileId
     *            封面文件ID
     * @param summary
     *            竞赛简介
     * @param level
     *            竞赛级别
     * @param month
     *            举办月份
     * @param organizer
     *            主办方
     * @param sortOrder
     *            排序值
     * @return 重建的竞赛实体
     */
    public static Competition reconstruct(Long id, String name, String shortName, Long logoFileId, Long coverFileId,
            String summary, AwardLevel level, String month, String organizer, Integer sortOrder) {
        return new Competition(id, name, shortName, logoFileId, coverFileId, summary, level, month, organizer,
                sortOrder);
    }

    /**
     * 更新竞赛信息
     *
     * @param name
     *            新名称
     * @param shortName
     *            新简称
     * @param logoFileId
     *            新Logo文件ID
     * @param coverFileId
     *            新封面文件ID
     * @param summary
     *            新简介
     * @param level
     *            新级别
     * @param month
     *            新月份
     * @param organizer
     *            新主办方
     * @throws IllegalArgumentException
     *             如果名称为空
     */
    public void update(String name, String shortName, Long logoFileId, Long coverFileId, String summary,
            AwardLevel level, String month, String organizer) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("竞赛名称不能为空");
        }
        this.name = name.trim();
        this.shortName = shortName;
        this.logoFileId = logoFileId;
        this.coverFileId = coverFileId;
        this.summary = summary;
        this.level = level;
        this.month = month;
        this.organizer = organizer;
    }

    /**
     * 更新排序值
     *
     * @param sortOrder
     *            新排序值
     */
    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
