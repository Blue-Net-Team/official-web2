package com.bluenet.web.domain.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;

/**
 * 密码生成器，用于生成随机初始密码。
 * <p>
 * 统一密码生成策略，确保各模块生成的密码强度和字符组成一致。
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PasswordGenerator {

    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*";
    private static final String ALL_CHARS = LOWERCASE + UPPERCASE + DIGITS + SPECIAL;
    private static final String ALL_CHARS_NO_SPECIAL = LOWERCASE + UPPERCASE + DIGITS;

    /**
     * 生成随机密码。
     *
     * @param length
     *            密码长度
     * @param includeSpecial
     *            是否包含特殊字符
     * @return 随机密码字符串
     */
    public static String generate(int length, boolean includeSpecial) {
        if (length < 4) {
            throw new IllegalArgumentException("Password length must be at least 4");
        }

        String pool = includeSpecial ? ALL_CHARS : ALL_CHARS_NO_SPECIAL;
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        // 确保至少包含各类字符
        sb.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        sb.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        sb.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        if (includeSpecial) {
            sb.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));
        }

        // 剩余字符从完整字符池随机选取
        for (int i = sb.length(); i < length; i++) {
            sb.append(pool.charAt(random.nextInt(pool.length())));
        }

        // 打乱字符顺序
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
}
