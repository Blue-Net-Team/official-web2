package com.bluenet.web.application.service;

import com.bluenet.web.application.command.wpsform.WpsFormCommands;
import jakarta.validation.Valid;

/**
 * WPS 智能表单应用服务接口。
 */
public interface WpsFormAppService {

    /**
     * 解析 WPS 绑定验证码。
     *
     * @param rid
     *            WPS 答卷 ID，未配置固定 bindCode 时作为 fallback
     * @return 应返回给 WPS 的 bind_code
     */
    String resolveBindCode(String rid);

    /**
     * 通过 WPS 表单提交数据创建用户。
     * <p>
     * 解析方向文本，生成随机密码，创建 MEMBER 角色用户，并将密码通过邮件发送到用户邮箱。
     * </p>
     *
     * @param command
     *            创建用户命令
     */
    void createUserFromWpsForm(@Valid WpsFormCommands.CreateUserFromWpsFormCommand command);
}
