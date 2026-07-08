package com.bluenet.web.application.query.member;

import com.bluenet.web.domain.model.enumerate.Direction;

/**
 * 获取成员列表查询参数。
 */
public record GetMemberListQuery(
        /** 方向 */
        Direction direction,
        /** 页码 */
        Integer page,
        /** 每页大小 */
        Integer size) {
}
