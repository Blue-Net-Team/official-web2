package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.VerifyCode;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.infrastructure.repository.dataobject.VerifyCodeDO;
import com.bluenet.web.infrastructure.repository.mapper.VerifyCodeMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VerificationCodeRepositoryImpl 集成测试。
 * <p>
 * 验证验证码仓储行为：save、按邮箱/验证码/场景查询、latest 语义、标记已使用。
 * </p>
 */
@DisplayName("VerificationCodeRepositoryImpl 集成测试")
class VerificationCodeRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private VerifyCodeMapper verifyCodeMapper;

    private static final String TEST_EMAIL = "verify-test@example.com";
    private static final String TEST_CODE = "123456";
    private static final String SCENE_LOGIN = "LOGIN";
    private static final String SCENE_REGISTER = "REGISTER";

    private VerifyCode createVerifyCode(String target, String code, String scene, int expireMinutes) {
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(expireMinutes);
        VerifyCode verifyCode = VerifyCode.create(target, code, expireAt, scene);
        verificationCodeRepository.save(verifyCode);
        return verifyCode;
    }

    @Test
    @DisplayName("save: 新 VerifyCode 应插入并回写 ID")
    void save_newVerifyCode_shouldInsertAndAssignId() {
        VerifyCode verifyCode = VerifyCode.create(
                TEST_EMAIL,
                TEST_CODE,
                LocalDateTime.now().plusMinutes(5),
                SCENE_LOGIN);

        verificationCodeRepository.save(verifyCode);

        assertNotNull(verifyCode.getId());
        VerifyCodeDO dataObject = verifyCodeMapper.selectById(verifyCode.getId());
        assertNotNull(dataObject);
        assertEquals(TEST_EMAIL, dataObject.getTarget());
        assertEquals(TEST_CODE, dataObject.getCode());
        assertEquals(SCENE_LOGIN, dataObject.getScene());
        assertNull(dataObject.getUsedAt());
    }

    @Test
    @DisplayName("findByEmailAndCode: 同邮箱同验证码多行时返回最新记录")
    void findByEmailAndCode_multipleRows_shouldReturnLatest() {
        VerifyCode older = createVerifyCode(TEST_EMAIL, TEST_CODE, SCENE_LOGIN, 5);
        VerifyCode newer = createVerifyCode(TEST_EMAIL, TEST_CODE, SCENE_LOGIN, 10);

        Optional<VerifyCode> found = verificationCodeRepository.findByEmailAndCode(TEST_EMAIL, TEST_CODE);

        assertTrue(found.isPresent());
        assertEquals(newer.getId(), found.get().getId());
        assertEquals(TEST_EMAIL, found.get().getTarget());
        assertEquals(TEST_CODE, found.get().getCode());
        assertEquals(SCENE_LOGIN, found.get().getScene());
    }

    @Test
    @DisplayName("findByEmailAndCode: 不存在时返回空 Optional")
    void findByEmailAndCode_notExist_shouldReturnEmpty() {
        Optional<VerifyCode> found = verificationCodeRepository.findByEmailAndCode("not-exist@example.com", "000000");
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("findByEmailAndCodeAndScene: 应精确匹配场景并返回最新记录")
    void findByEmailAndCodeAndScene_multipleScenes_shouldReturnMatchingLatest() {
        VerifyCode loginCode1 = createVerifyCode(TEST_EMAIL, TEST_CODE, SCENE_LOGIN, 5);
        createVerifyCode(TEST_EMAIL, TEST_CODE, SCENE_REGISTER, 10);
        VerifyCode loginCode2 = createVerifyCode(TEST_EMAIL, TEST_CODE, SCENE_LOGIN, 15);

        Optional<VerifyCode> found = verificationCodeRepository
                .findByEmailAndCodeAndScene(TEST_EMAIL, TEST_CODE, SCENE_LOGIN);

        assertTrue(found.isPresent());
        assertEquals(loginCode2.getId(), found.get().getId());

        Optional<VerifyCode> notFound = verificationCodeRepository
                .findByEmailAndCodeAndScene(TEST_EMAIL, TEST_CODE, "RESET_PASSWORD");
        assertTrue(notFound.isEmpty());
    }

    @Test
    @DisplayName("markAsUsed(email, code): 应将匹配记录标记为已使用")
    void markAsUsed_byEmailAndCode_shouldSetUsedAt() {
        VerifyCode verifyCode = createVerifyCode(TEST_EMAIL, TEST_CODE, SCENE_LOGIN, 5);

        verificationCodeRepository.markAsUsed(TEST_EMAIL, TEST_CODE);

        VerifyCodeDO updated = verifyCodeMapper.selectById(verifyCode.getId());
        assertNotNull(updated);
        assertNotNull(updated.getUsedAt());
    }

    @Test
    @DisplayName("markAsUsed(email, code, scene): 应仅将匹配场景的记录标记为已使用")
    void markAsUsed_byEmailAndCodeAndScene_shouldSetUsedAtForMatchingSceneOnly() {
        VerifyCode loginCode = createVerifyCode(TEST_EMAIL, TEST_CODE, SCENE_LOGIN, 5);
        VerifyCode registerCode = createVerifyCode(TEST_EMAIL, TEST_CODE, SCENE_REGISTER, 5);

        verificationCodeRepository.markAsUsed(TEST_EMAIL, TEST_CODE, SCENE_LOGIN);

        VerifyCodeDO loginUpdated = verifyCodeMapper.selectById(loginCode.getId());
        VerifyCodeDO registerUpdated = verifyCodeMapper.selectById(registerCode.getId());

        assertNotNull(loginUpdated);
        assertNotNull(loginUpdated.getUsedAt());
        assertNotNull(registerUpdated);
        assertNull(registerUpdated.getUsedAt());
    }

    @Test
    @DisplayName("VerifyCode 工厂方法：create 与 reconstruct 应保持字段一致")
    void verifyCodeFactoryMethods_shouldProduceExpectedEntity() {
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(5);
        LocalDateTime usedAt = LocalDateTime.now();

        VerifyCode created = VerifyCode.create(TEST_EMAIL, TEST_CODE, expireAt, SCENE_LOGIN);
        assertNull(created.getId());
        assertNull(created.getUsedAt());
        assertEquals(TEST_EMAIL, created.getTarget());
        assertEquals(TEST_CODE, created.getCode());
        assertEquals(expireAt, created.getExpireAt());
        assertEquals(SCENE_LOGIN, created.getScene());

        VerifyCode reconstructed = VerifyCode.reconstruct(100L, TEST_EMAIL, TEST_CODE, expireAt, usedAt, SCENE_LOGIN);
        assertEquals(100L, reconstructed.getId());
        assertEquals(TEST_EMAIL, reconstructed.getTarget());
        assertEquals(TEST_CODE, reconstructed.getCode());
        assertEquals(expireAt, reconstructed.getExpireAt());
        assertEquals(usedAt, reconstructed.getUsedAt());
        assertEquals(SCENE_LOGIN, reconstructed.getScene());
        assertTrue(reconstructed.isUsed());
    }
}
