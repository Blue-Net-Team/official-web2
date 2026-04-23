package com.bluenet.web.application;

import com.bluenet.web.domain.model.enumerate.ExperienceType;

/**
 * 用户经历聚合的应用层结果对象。
 * <p>
 * 封装了用户经历相关操作返回给 API 层的数据。
 * </p>
 */
public record UserExperienceResult(
        /** 唯一标识 */
        Long id,
        /** 类型 */
        ExperienceType type,
        /** 标题 */
        String title,
        /** 开始时间 */
        String startTime,
        /** 结束时间 */
        String endTime,
        /** 内容 */
        String content) {
}
