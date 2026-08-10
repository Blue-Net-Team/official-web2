package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.VerifyCode;

import java.util.Optional;

public interface VerificationCodeRepository {
    /**
     * 按邮箱和验证码查询验证码记录。
     *
     * @param email
     *            邮箱地址，用于定位用户或验证码。
     * @param code
     *            验证码或推荐码。
     * @return 查询到的验证码实体；不存在时为空。
     */
    Optional<VerifyCode> findByEmailAndCode(String email, String code);

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
    Optional<VerifyCode> findByEmailAndCodeAndScene(String email, String code, String scene);

    /**
     * 保存新的验证码记录。
     *
     * @param verifyCode
     *            验证码实体。
     */
    void save(VerifyCode verifyCode);

    /**
     * 将匹配的验证码记录标记为已使用。
     *
     * @param email
     *            邮箱地址，用于定位用户或验证码。
     * @param code
     *            验证码或推荐码。
     */
    void markAsUsed(String email, String code);

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
    void markAsUsed(String email, String code, String scene);
}
