package com.bluenet.web.application.service;

import com.bluenet.web.application.command.wpsform.WpsFormCommands;

/**
 * WPS 智能表单应用服务接口。
 */
public interface WpsFormAppService {

    /**
     * 通过 WPS 表单提交数据创建用户。
     * <p>
     * 生成随机密码，创建 MEMBER 角色用户，并将密码通过邮件发送到用户邮箱。
     * </p>
     *
     * @param command 表单解析后的创建用户命令
     */
    void createUserFromWpsForm(WpsFormCommands.CreateUserFromWpsCommand command);
}
