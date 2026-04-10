package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.vo.VerifyCodeVO;

/**
 * 验证码领域服务
 * <p>
 * 负责验证码生成的领域逻辑
 * </p>
 */
public interface VerificationCodeDomainService {

    VerifyCodeVO generateCode(String email, String scene);
}
