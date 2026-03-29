package com.bluenet.web.infrastructure.config.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;

import com.bluenet.web.domain.model.enumerate.ImageType;

/**
 * 枚举转换器测试
 * <p>
 * 用于测试自定义枚举转换器是否能够正确处理@EnumValue注解的枚举类型转换。
 * </p>
 */
@DisplayName("枚举转换器测试")
class EnumConverterTest {

    @Test
    @DisplayName("测试ImageType枚举转换：应该能够根据value字段转换")
    void testImageTypeConversion() {
        // 创建枚举转换器
        EnumConverterFactory factory = new EnumConverterFactory();
        Converter<String, ImageType> converter = factory.getConverter(ImageType.class);

        // 测试转换
        ImageType result = converter.convert("competition");

        // 验证
        assertNotNull(result);
        assertEquals(ImageType.COMPETITION, result);
        assertEquals("competition", result.getValue());
    }

    @Test
    @DisplayName("测试ImageType枚举转换：应该能够根据枚举名称转换")
    void testImageTypeConversionByName() {
        // 创建枚举转换器
        EnumConverterFactory factory = new EnumConverterFactory();
        Converter<String, ImageType> converter = factory.getConverter(ImageType.class);

        // 测试转换
        ImageType result = converter.convert("COMPETITION");

        // 验证
        assertNotNull(result);
        assertEquals(ImageType.COMPETITION, result);
    }

    @Test
    @DisplayName("测试ImageType枚举转换：应该能够不区分大小写转换")
    void testImageTypeConversionIgnoreCase() {
        // 创建枚举转换器
        EnumConverterFactory factory = new EnumConverterFactory();
        Converter<String, ImageType> converter = factory.getConverter(ImageType.class);

        // 测试转换
        ImageType result = converter.convert("Competition");

        // 验证
        assertNotNull(result);
        assertEquals(ImageType.COMPETITION, result);
    }

}
