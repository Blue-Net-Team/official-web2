package com.bluenet.web.infrastructure.config.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;

import com.bluenet.web.domain.model.enumerate.FileType;

/**
 * 枚举转换器测试
 * <p>
 * 用于测试自定义枚举转换器是否能够正确处理@EnumValue注解的枚举类型转换。
 * </p>
 */
@DisplayName("枚举转换器测试")
class EnumConverterTest {

    @Test
    @DisplayName("测试FileType枚举转换：应该能够根据value字段转换")
    void testFileTypeConversion() {
        // 创建枚举转换器
        EnumConverterFactory factory = new EnumConverterFactory();
        Converter<String, FileType> converter = factory.getConverter(FileType.class);

        // 测试转换
        FileType result = converter.convert("avatar");

        // 验证
        assertNotNull(result);
        assertEquals(FileType.AVATAR, result);
        assertEquals("avatar", result.getValue());
    }

    @Test
    @DisplayName("测试FileType枚举转换：应该能够根据枚举名称转换")
    void testFileTypeConversionByName() {
        // 创建枚举转换器
        EnumConverterFactory factory = new EnumConverterFactory();
        Converter<String, FileType> converter = factory.getConverter(FileType.class);

        // 测试转换
        FileType result = converter.convert("AVATAR");

        // 验证
        assertNotNull(result);
        assertEquals(FileType.AVATAR, result);
    }

    @Test
    @DisplayName("测试FileType枚举转换：应该能够不区分大小写转换")
    void testFileTypeConversionIgnoreCase() {
        // 创建枚举转换器
        EnumConverterFactory factory = new EnumConverterFactory();
        Converter<String, FileType> converter = factory.getConverter(FileType.class);

        // 测试转换
        FileType result = converter.convert("Avatar");

        // 验证
        assertNotNull(result);
        assertEquals(FileType.AVATAR, result);
    }

}
