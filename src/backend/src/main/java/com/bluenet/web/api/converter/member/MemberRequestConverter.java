package com.bluenet.web.api.converter.member;

import com.bluenet.web.api.dto.member.MemberListQueryDTO;
import com.bluenet.web.application.command.member.MemberCommands;
import org.springframework.stereotype.Component;

/**
 * 成员请求转换器
 * <p>
 * 负责将 API 层的 RequestDTO 转换为应用层的 Command
 * </p>
 */
@Component
public class MemberRequestConverter {

    /**
     * 将成员列表查询 DTO 转换为命令
     */
    public MemberCommands.GetMemberListCommand toCommand(MemberListQueryDTO dto) {
        return new MemberCommands.GetMemberListCommand(dto.getDirection(), dto.getPage(), dto.getSize());
    }
}
