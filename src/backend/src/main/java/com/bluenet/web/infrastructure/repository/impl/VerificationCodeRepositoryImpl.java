package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bluenet.web.domain.model.entity.VerifyCode;
import com.bluenet.web.domain.model.vo.VerifyCodeVO;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.infrastructure.repository.mapper.VerifyCodeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @Override
    public Optional<VerifyCodeVO> findByEmailAndCodeAndScene(String email, String code, String scene) {
        LambdaQueryWrapper<VerifyCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VerifyCode::getTarget, email)
                .eq(VerifyCode::getCode, code)
                .eq(VerifyCode::getScene, scene)
                .orderByDesc(VerifyCode::getId)
                .last("LIMIT 1");

        VerifyCode verifyCode = verifyCodeMapper.selectOne(wrapper);

        if (verifyCode == null) {
            return Optional.empty();
        }

        return Optional.of(convert(verifyCode));
    }

    @Override
    public void save(VerifyCodeVO verifyCodeVO) {
        VerifyCode verifyCode = new VerifyCode();
        verifyCode.setTarget(verifyCodeVO.getTarget());
        verifyCode.setCode(verifyCodeVO.getCode());
        verifyCode.setExpireAt(verifyCodeVO.getExpireAt());
        verifyCode.setScene(verifyCodeVO.getScene());
        verifyCodeMapper.insert(verifyCode);
        log.debug(
                "验证码已存储 - target={}, scene={}, expireAt={}",
                verifyCodeVO.getTarget(),
                verifyCodeVO.getScene(),
                verifyCodeVO.getExpireAt());
    }

    @Override
    public void markAsUsed(String email, String code) {
        LambdaUpdateWrapper<VerifyCode> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(VerifyCode::getTarget, email)
                .eq(VerifyCode::getCode, code)
                .set(VerifyCode::getUsedAt, LocalDateTime.now());
        verifyCodeMapper.update(null, wrapper);
        log.debug("验证码已标记为已使用 - target={}", email);
    }

    @Override
    public void markAsUsed(String email, String code, String scene) {
        LambdaUpdateWrapper<VerifyCode> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(VerifyCode::getTarget, email)
                .eq(VerifyCode::getCode, code)
                .eq(VerifyCode::getScene, scene)
                .set(VerifyCode::getUsedAt, LocalDateTime.now());
        verifyCodeMapper.update(null, wrapper);
        log.debug("验证码已标记为已使用 - target={}, scene={}", email, scene);
    }

    @Override
    public Optional<VerifyCodeVO> findLatestByEmailWithinSeconds(String email, int seconds) {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(seconds);
        LambdaQueryWrapper<VerifyCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VerifyCode::getTarget, email)
                .ge(VerifyCode::getExpireAt, threshold)
                .orderByDesc(VerifyCode::getId)
                .last("LIMIT 1");

        VerifyCode verifyCode = verifyCodeMapper.selectOne(wrapper);
        if (verifyCode == null) {
            return Optional.empty();
        }
        return Optional.of(convert(verifyCode));
    }

    @Override
    public Optional<VerifyCodeVO> findLatestByEmailAndSceneWithinSeconds(String email, String scene, int seconds) {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(seconds);
        LambdaQueryWrapper<VerifyCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VerifyCode::getTarget, email)
                .eq(VerifyCode::getScene, scene)
                .ge(VerifyCode::getExpireAt, threshold)
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
                .scene(verifyCode.getScene())
                .build();
    }
}
