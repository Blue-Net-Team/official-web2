package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

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

    /**
     * 按邮箱和验证码查询验证码记录。
     *
     * @param email
     *            邮箱地址，用于定位用户或验证码。
     * @param code
     *            验证码或推荐码。
     * @return 查询到的验证码 结果；不存在时为空。
     */
    @Override
    public Optional<VerifyCodeVO> findByEmailAndCode(String email, String code) {
        VerifyCode verifyCode = RepositoryObjectConverter.toDomain(
                verifyCodeMapper.selectLatestByTargetAndCode(email, code),
                VerifyCode.class);

        if (verifyCode == null) {
            return Optional.empty();
        }

        return Optional.of(convert(verifyCode));
    }

    /**
     * 按邮箱、验证码和场景查询验证码记录。
     *
     * @param email
     *            邮箱地址，用于定位用户或验证码。
     * @param code
     *            验证码或推荐码。
     * @param scene
     *            验证码使用场景。
     * @return 查询到的验证码 结果；不存在时为空。
     */
    @Override
    public Optional<VerifyCodeVO> findByEmailAndCodeAndScene(String email, String code, String scene) {
        VerifyCode verifyCode = RepositoryObjectConverter.toDomain(
                verifyCodeMapper.selectLatestByTargetAndCodeAndScene(email, code, scene),
                VerifyCode.class);

        if (verifyCode == null) {
            return Optional.empty();
        }

        return Optional.of(convert(verifyCode));
    }

    /**
     * 保存新的验证码 记录。
     *
     * @param verifyCodeVO
     *            验证码视图对象。
     */
    @Override
    public void save(VerifyCodeVO verifyCodeVO) {
        VerifyCode verifyCode = new VerifyCode();
        verifyCode.setTarget(verifyCodeVO.getTarget());
        verifyCode.setCode(verifyCodeVO.getCode());
        verifyCode.setExpireAt(verifyCodeVO.getExpireAt());
        verifyCode.setScene(verifyCodeVO.getScene());
        RepositoryObjectConverter.insert(verifyCodeMapper, verifyCode, VerifyCodeDO.class);
        log.debug(
                "验证码已存储 - target={}, scene={}, expireAt={}",
                verifyCodeVO.getTarget(),
                verifyCodeVO.getScene(),
                verifyCodeVO.getExpireAt());
    }

    /**
     * 将匹配的验证码记录标记为已使用。
     *
     * @param email
     *            邮箱地址，用于定位用户或验证码。
     * @param code
     *            验证码或推荐码。
     */
    @Override
    public void markAsUsed(String email, String code) {
        verifyCodeMapper.markAsUsed(email, code, LocalDateTime.now());
        log.debug("验证码已标记为已使用 - target={}", email);
    }

    /**
     * 将匹配的验证码记录标记为已使用。
     *
     * @param email
     *            邮箱地址，用于定位用户或验证码。
     * @param code
     *            验证码或推荐码。
     * @param scene
     *            验证码使用场景。
     */
    @Override
    public void markAsUsed(String email, String code, String scene) {
        verifyCodeMapper.markAsUsedWithScene(email, code, scene, LocalDateTime.now());
        log.debug("验证码已标记为已使用 - target={}, scene={}", email, scene);
    }

    /**
     * 转换验证码 相关对象，隔离持久层和领域层模型。
     *
     * @param verifyCode
     *            验证码领域对象。
     * @return 转换后的目标模型对象。
     */
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
