package com.bluenet.web.api.converter.permission;

import com.bluenet.web.api.dto.permission.PermissionQueryDTO;
import com.bluenet.web.application.command.permission.PermissionCommands;
import org.springframework.stereotype.Component;

/**
 * 权限请求转换器
 * <p>
 * 负责将 API 层的 RequestDTO 转换为应用层的 Command
 * </p>
 */
@Component
public class PermissionRequestConverter {

    /**
     * 将查询请求 DTO 转换为命令
     */
    public PermissionCommands.GetPermissionsCommand toCommand(PermissionQueryDTO dto) {
        return new PermissionCommands.GetPermissionsCommand(
                dto.getKeyword(),
                dto.getFormat(),
                dto.getPage(),
                dto.getSize());
    }
}
