package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.vo.VerifyCodeVO;

/**
 * 验证码领域服务
 * <p>
 * 负责验证码生成的领域逻辑
 * </p>
 */
public interface VerificationCodeDomainService {

    /**
     * 生成验证码
     *
     * @param email
     *            目标邮箱
     * @param ipaddress
     *            请求IP地址
     * @return 验证码值对象
     */
    VerifyCodeVO generateCode(String email, String ipaddress);
}
