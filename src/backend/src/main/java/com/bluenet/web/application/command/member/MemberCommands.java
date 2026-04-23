package com.bluenet.web.application.command.member;

import com.bluenet.web.domain.model.enumerate.Direction;

/**
 * 成员聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class MemberCommands {

    /** 禁止实例化。 */
    private MemberCommands() {
    }

    /**
     * 获取成员列表命令。
     * <p>
     * 用于分页获取成员列表。
     * </p>
     */
    public record GetMemberListCommand(
            /** 方向 */
            Direction direction,
            /** 页码 */
            Integer page,
            /** 每页大小 */
            Integer size) {
    }
}
