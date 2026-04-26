package com.bluenet.web.application.service;

import com.bluenet.web.application.AdminUserResult;
import com.bluenet.web.application.command.adminuser.AdminUserCommands;
import org.springframework.data.domain.Page;

/**
 * 管理员用户管理应用服务接口
 */
public interface AdminUserAppService {

    /**
     * 分页查询用户列表
     */
    Page<AdminUserResult.ListItem> getUserList(AdminUserCommands.GetUserListCommand command);

    /**
     * 获取用户详情
     */
    AdminUserResult.Detail getUserDetail(Long userId);

    /**
     * 更新用户信息
     */
    void updateUser(AdminUserCommands.UpdateUserCommand command);

    /**
     * 重置用户密码
     */
    void resetPassword(AdminUserCommands.ResetPasswordCommand command);

    /**
     * 删除用户
     */
    void deleteUser(Long userId);

    /**
     * 批量删除用户
     */
    void batchDelete(AdminUserCommands.BatchOperateCommand command);

    /**
     * 批量禁用/启用用户
     */
    void batchDisable(AdminUserCommands.BatchOperateCommand command, Boolean disable);

    /**
     * 批量更新用户角色
     */
    void batchUpdateRole(AdminUserCommands.BatchUpdateRoleCommand command);
}
