package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.converter.VerificationCodeRepositoryConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.domain.model.entity.VerifyCode;
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
    private final VerificationCodeRepositoryConverter converter;

    /**
     * 按邮箱和验证码查询验证码记录。
     *
     * @param email
     *            邮箱地址，用于定位用户或验证码。
     * @param code
     *            验证码或推荐码。
     * @return 查询到的验证码实体；不存在时为空。
     */
    @Override
    public Optional<VerifyCode> findByEmailAndCode(String email, String code) {
        VerifyCodeDO dataObject = verifyCodeMapper.selectLatestByTargetAndCode(email, code);
        return Optional.ofNullable(converter.toEntity(dataObject));
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
     * @return 查询到的验证码实体；不存在时为空。
     */
    @Override
    public Optional<VerifyCode> findByEmailAndCodeAndScene(String email, String code, String scene) {
        VerifyCodeDO dataObject = verifyCodeMapper.selectLatestByTargetAndCodeAndScene(email, code, scene);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    /**
     * 保存新的验证码记录。
     *
     * @param verifyCode
     *            验证码实体。
     */
    @Override
    public void save(VerifyCode verifyCode) {
        VerifyCodeDO dataObject = converter.toDataObject(verifyCode);
        verifyCodeMapper.insert(dataObject);
        verifyCode.setId(dataObject.getId());
        log.debug(
                "验证码已存储 - target={}, scene={}, expireAt={}",
                verifyCode.getTarget(),
                verifyCode.getScene(),
                verifyCode.getExpireAt());
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
}
