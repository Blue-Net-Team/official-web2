package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bluenet.web.domain.model.entity.VerifyCode;
import com.bluenet.web.domain.model.vo.VerifyCodeVO;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.infrastructure.repository.mapper.VerifyCodeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class VerificationCodeRepositoryImpl implements VerificationCodeRepository {

    private final VerifyCodeMapper verifyCodeMapper;

    @Override
    public Optional<VerifyCodeVO> findByEmailAndCode(String email, String code) {
        LambdaQueryWrapper<VerifyCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VerifyCode::getTarget, email)
                .eq(VerifyCode::getCode, code)
                .orderByDesc(VerifyCode::getId)
                .last("LIMIT 1");

        VerifyCode verifyCode = verifyCodeMapper.selectOne(wrapper);

        if (verifyCode == null) {
            return Optional.empty();
        }

        return Optional.of(convert(verifyCode));
    }

    private VerifyCodeVO convert(VerifyCode verifyCode) {
        return VerifyCodeVO.builder()
                .target(verifyCode.getTarget())
                .code(verifyCode.getCode())
                .expireAt(verifyCode.getExpireAt())
                .used(verifyCode.getUsedAt() != null)
                .build();
    }
}
