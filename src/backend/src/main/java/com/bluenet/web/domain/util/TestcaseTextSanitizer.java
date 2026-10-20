package com.bluenet.web.domain.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * 判题用例文本净化工具类。
 * <p>
 * 处理浏览器录入时带入的不可见字符，避免判题时输入解析失败。规则：
 * </p>
 * <ul>
 * <li>{@code \r\n} 与 {@code \r} 归一为 {@code \n}</li>
 * <li>删除 {@code Cf}、{@code Zl}、{@code Zp} 类字符（零宽字符、BOM、软连字符、行/段分隔符）</li>
 * <li>非 ASCII 空格归一为 ASCII 空格，避免不同语言的分词行为不一致</li>
 * <li>保留 {@code \t}、{@code \n} 与 ASCII 空格</li>
 * </ul>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestcaseTextSanitizer {

    /**
     * 不可见格式字符：零宽空格/连接符、词连接符、BOM、软连字符，以及行分隔符与段分隔符。
     */
    private static final Pattern INVISIBLE_CHARACTERS = Pattern.compile("[\\p{Cf}\\p{Zl}\\p{Zp}]");

    /**
     * 非 ASCII 空格分隔符。
     * <p>
     * 必须归一为 ASCII 空格而非删除，否则会把相邻的两个 token 粘连成一个。
     * </p>
     */
    private static final Pattern NON_ASCII_SPACES = Pattern
            .compile("[\\u00a0\\u1680\\u2000-\\u200a\\u202f\\u205f\\u3000]");

    /**
     * 净化判题用例文本。
     *
     * @param input
     *            原始文本
     * @return 净化后的文本；输入为 {@code null} 时返回 {@code null}
     */
    public static String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String normalizedLineEnding = input.replace("\r\n", "\n").replace('\r', '\n');
        String withoutInvisibleCharacters = INVISIBLE_CHARACTERS.matcher(normalizedLineEnding).replaceAll("");
        return NON_ASCII_SPACES.matcher(withoutInvisibleCharacters).replaceAll(" ");
    }
}
