package com.bluenet.web.application.query.adminuser;

import com.bluenet.web.domain.model.enumerate.Direction;

/**
 * 分页查询用户列表查询参数。
 */
public record GetUserListQuery(
        Integer page,
        Integer size,
        Long roleId,
        Direction direction,
        Long collegeId,
        String keyword) {
}
