package com.bluenet.web.application.result.competition;

/**
 * 竞赛聚合的应用层结果对象。
 * <p>
 * 封装了竞赛相关操作返回给 API 层的数据。
 * </p>
 */
public record CompetitionResult(
        /** 唯一标识 */
        Long id,
        /** 名称 */
        String name,
        /** 简称 */
        String shortName,
        /** 级别 */
        String level,
        /** 月份 */
        String month,
        /** 主办方 */
        String organizer,
        /** 简介 */
        String summary,
        /** Logo文件ID */
        Long logoFileId,
        /** 封面文件ID */
        Long coverFileId,
        /** 排序序号 */
        Integer sortOrder) {
}
