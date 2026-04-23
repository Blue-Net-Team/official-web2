package com.bluenet.web.application.command.permission;

/**
 * 权限聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class PermissionCommands {

    /** 禁止实例化。 */
    private PermissionCommands() {
    }

    /**
     * 查询权限列表命令。
     * <p>
     * 用于分页查询权限列表。
     * </p>
     */
    public record GetPermissionsCommand(
            /** 关键词 */
            String keyword,
            /** 格式 */
            String format,
            /** 页码 */
            Integer page,
            /** 每页大小 */
            Integer size) {
    }
}
