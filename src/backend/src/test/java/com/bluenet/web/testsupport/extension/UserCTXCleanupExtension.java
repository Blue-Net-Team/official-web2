package com.bluenet.web.testsupport.extension;

import com.bluenet.web.infrastructure.security.util.UserCTX;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 扩展：在每个测试方法结束后清理 {@link UserCTX}。
 * <p>
 * 防止 ThreadLocal 中的安全主体泄漏到后续测试，导致权限判断异常。
 * </p>
 */
public class UserCTXCleanupExtension implements AfterEachCallback {

    @Override
    public void afterEach(ExtensionContext context) {
        UserCTX.clear();
    }
}
