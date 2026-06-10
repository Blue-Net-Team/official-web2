package com.bluenet.web.infrastructure.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.Direction;

/**
 * DirectionSlugConverter单元测试
 */
@DisplayName("DirectionSlugConverter 单元测试")
class DirectionSlugConverterTest {

    // ==================== fromSlug ====================

    @Test
    @DisplayName("fromSlug: cv应映射到COMPUTER_VISION")
    void fromSlug_cv_shouldMapToComputerVision() {
        // 执行
        Direction result = DirectionSlugConverter.fromSlug("cv");

        // 验证
        assertEquals(Direction.COMPUTER_VISION, result);
    }

    @Test
    @DisplayName("fromSlug: embed应映射到EMBEDDED")
    void fromSlug_embed_shouldMapToEmbedded() {
        // 执行
        Direction result = DirectionSlugConverter.fromSlug("embed");

        // 验证
        assertEquals(Direction.EMBEDDED, result);
    }

    @Test
    @DisplayName("fromSlug: struct应映射到STRUCTURAL_DESIGN")
    void fromSlug_struct_shouldMapToStructuralDesign() {
        // 执行
        Direction result = DirectionSlugConverter.fromSlug("struct");

        // 验证
        assertEquals(Direction.STRUCTURAL_DESIGN, result);
    }

    @Test
    @DisplayName("fromSlug: 大写slug应正确映射")
    void fromSlug_upperCase_shouldMapCorrectly() {
        // 执行
        Direction result = DirectionSlugConverter.fromSlug("CV");

        // 验证
        assertEquals(Direction.COMPUTER_VISION, result);
    }

    @Test
    @DisplayName("fromSlug: 混合大小写slug应正确映射")
    void fromSlug_mixedCase_shouldMapCorrectly() {
        // 执行
        Direction result = DirectionSlugConverter.fromSlug("EmBeD");

        // 验证
        assertEquals(Direction.EMBEDDED, result);
    }

    @Test
    @DisplayName("fromSlug: 无效slug应抛出异常")
    void fromSlug_invalidSlug_shouldThrowException() {
        // 执行 & 验证
        DataNotFound exception = assertThrows(
                DataNotFound.class,
                () -> DirectionSlugConverter.fromSlug("invalid"));

        assertTrue(exception.getMessage().contains("无效的方向标识"));
    }

    @Test
    @DisplayName("fromSlug: null应抛出异常")
    void fromSlug_null_shouldThrowException() {
        // 执行 & 验证
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> DirectionSlugConverter.fromSlug(null));

        assertTrue(exception.getMessage().contains("方向标识不能为空"));
    }

    @Test
    @DisplayName("fromSlug: 空字符串应抛出异常")
    void fromSlug_emptyString_shouldThrowException() {
        // 执行 & 验证
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> DirectionSlugConverter.fromSlug(""));

        assertTrue(exception.getMessage().contains("方向标识不能为空"));
    }

    @Test
    @DisplayName("fromSlug: 空白字符串应抛出异常")
    void fromSlug_blankString_shouldThrowException() {
        // 执行 & 验证
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> DirectionSlugConverter.fromSlug("   "));

        assertTrue(exception.getMessage().contains("方向标识不能为空"));
    }

    // ==================== toSlug ====================

    @Test
    @DisplayName("toSlug: COMPUTER_VISION应映射到cv")
    void toSlug_computerVision_shouldMapToCv() {
        // 执行
        String result = DirectionSlugConverter.toSlug(Direction.COMPUTER_VISION);

        // 验证
        assertEquals("cv", result);
    }

    @Test
    @DisplayName("toSlug: EMBEDDED应映射到embed")
    void toSlug_embedded_shouldMapToEmbed() {
        // 执行
        String result = DirectionSlugConverter.toSlug(Direction.EMBEDDED);

        // 验证
        assertEquals("embed", result);
    }

    @Test
    @DisplayName("toSlug: STRUCTURAL_DESIGN应映射到struct")
    void toSlug_structuralDesign_shouldMapToStruct() {
        // 执行
        String result = DirectionSlugConverter.toSlug(Direction.STRUCTURAL_DESIGN);

        // 验证
        assertEquals("struct", result);
    }

    @Test
    @DisplayName("toSlug: null应抛出异常")
    void toSlug_null_shouldThrowException() {
        // 执行 & 验证
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> DirectionSlugConverter.toSlug(null));

        assertTrue(exception.getMessage().contains("方向不能为空"));
    }

    // ==================== 双向转换测试 ====================

    @Test
    @DisplayName("双向转换: cv应能正确往返转换")
    void roundTrip_cv_shouldConvertCorrectly() {
        // 执行
        Direction direction = DirectionSlugConverter.fromSlug("cv");
        String slug = DirectionSlugConverter.toSlug(direction);

        // 验证
        assertEquals("cv", slug);
        assertEquals(Direction.COMPUTER_VISION, direction);
    }

    @Test
    @DisplayName("双向转换: embed应能正确往返转换")
    void roundTrip_embed_shouldConvertCorrectly() {
        // 执行
        Direction direction = DirectionSlugConverter.fromSlug("embed");
        String slug = DirectionSlugConverter.toSlug(direction);

        // 验证
        assertEquals("embed", slug);
        assertEquals(Direction.EMBEDDED, direction);
    }

    @Test
    @DisplayName("双向转换: struct应能正确往返转换")
    void roundTrip_struct_shouldConvertCorrectly() {
        // 执行
        Direction direction = DirectionSlugConverter.fromSlug("struct");
        String slug = DirectionSlugConverter.toSlug(direction);

        // 验证
        assertEquals("struct", slug);
        assertEquals(Direction.STRUCTURAL_DESIGN, direction);
    }
}
