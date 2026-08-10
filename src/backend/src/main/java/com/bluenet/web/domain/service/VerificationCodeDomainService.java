package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.entity.VerifyCode;

/**
 * 验证码领域服务
 * <p>
 * 负责验证码生成的领域逻辑
 * </p>
 */
public interface VerificationCodeDomainService {

    /**
     * 生成验证码。
     *
     * @param email
     *            发送目标邮箱
     * @param scene
     *            业务场景
     * @return 验证码实体
     */
    VerifyCode generateCode(String email, String scene);
}
