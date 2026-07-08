package com.bluenet.web.application.query.enroll;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;

/**
 * 查询报名列表查询参数。
 */
public record GetEnrollmentListQuery(
        /** 页码 */
        Integer page,
        /** 每页大小 */
        Integer size,
        /** 关键词 */
        String keyword,
        /** 状态 */
        EnrollStatus status,
        /** 方向 */
        Direction direction) {

    public GetEnrollmentListQuery {
        if (keyword != null) {
            keyword = keyword.trim();
        }
    }
}
