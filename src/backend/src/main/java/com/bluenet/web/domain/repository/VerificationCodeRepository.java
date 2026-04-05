package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.vo.VerifyCodeVO;

import java.util.Optional;

public interface VerificationCodeRepository {
    Optional<VerifyCodeVO> findByEmailAndCode(String email, String code);

    void save(VerifyCodeVO verifyCodeVO);

    void markAsUsed(String email, String code);

    Optional<VerifyCodeVO> findLatestByEmailWithinSeconds(String email, int seconds);
}
