package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.assessment_time.AssessmentTimeDTO;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.enumerate.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AssessmentTimeAppConverter 单元测试
 * <p>
 * 测试考核时间 Entity 与 DTO 之间的转换逻辑
 * </p>
 */
@DisplayName("AssessmentTimeAppConverter 单元测试")
class AssessmentTimeAppConverterTest {

    private final AssessmentTimeAppConverter converter = new AssessmentTimeAppConverter();

    private AssessmentTime createTestEntity() {
        return AssessmentTime.reconstruct(
                1L,
                Direction.COMPUTER_VISION,
                1,
                2025,
                LocalDateTime.of(2026, 6, 1, 9, 0),
                LocalDateTime.of(2026, 6, 1, 11, 0),
                true,
                120);
    }

    // ==================== convertToDTO 测试 ====================

    @Nested
    @DisplayName("convertToDTO 方法测试")
    class ConvertToDTOTests {

        @Test
        @DisplayName("正常转换：应完整映射所有字段")
        void convertToDTO_validEntity_shouldMapAllFields() {
            AssessmentTime entity = createTestEntity();

            AssessmentTimeDTO dto = converter.convertToDTO(entity);

            assertNotNull(dto);
            assertEquals(1L, dto.getId());
            assertEquals(Direction.COMPUTER_VISION, dto.getDirection());
            assertEquals(1, dto.getEpoch());
            assertEquals(2025, dto.getGrade());
            assertEquals(LocalDateTime.of(2026, 6, 1, 9, 0), dto.getStartTime());
            assertEquals(LocalDateTime.of(2026, 6, 1, 11, 0), dto.getEndTime());
            assertTrue(dto.getTimeLimit());
            assertEquals(120, dto.getTimeLimitMinutes());
        }

        @Test
        @DisplayName("不限时考核：timeLimitMinutes 应为 null")
        void convertToDTO_noTimeLimit_shouldHaveNullMinutes() {
            AssessmentTime entity = AssessmentTime.reconstruct(
                    2L,
                    Direction.EMBEDDED,
                    2,
                    2024,
                    LocalDateTime.of(2026, 7, 1, 9, 0),
                    LocalDateTime.of(2026, 7, 1, 12, 0),
                    false,
                    null);

            AssessmentTimeDTO dto = converter.convertToDTO(entity);

            assertNotNull(dto);
            assertFalse(dto.getTimeLimit());
            assertNull(dto.getTimeLimitMinutes());
        }

        @Test
        @DisplayName("进度数据字段应为 null（由 AppService 填充）")
        void convertToDTO_shouldHaveNullProgress() {
            AssessmentTime entity = createTestEntity();

            AssessmentTimeDTO dto = converter.convertToDTO(entity);

            assertNotNull(dto);
            assertNull(dto.getTotalQuestions());
            assertNull(dto.getCompletedQuestions());
        }
    }

    // ==================== convertToDTOList 测试 ====================

    @Nested
    @DisplayName("convertToDTOList 方法测试")
    class ConvertToDTOListTests {

        @Test
        @DisplayName("多个实体：应返回对应数量的DTO")
        void convertToDTOList_multipleEntities_shouldReturnDTOs() {
            List<AssessmentTime> entityList = new ArrayList<>();
            entityList.add(createTestEntity());
            entityList.add(
                    AssessmentTime.reconstruct(
                            2L,
                            Direction.STRUCTURAL_DESIGN,
                            1,
                            2023,
                            LocalDateTime.of(2026, 8, 1, 9, 0),
                            LocalDateTime.of(2026, 8, 1, 11, 0),
                            false,
                            null));

            List<AssessmentTimeDTO> dtoList = converter.convertToDTOList(entityList);

            assertNotNull(dtoList);
            assertEquals(2, dtoList.size());
            assertEquals(1L, dtoList.get(0).getId());
            assertEquals(2L, dtoList.get(1).getId());
            assertEquals(Direction.STRUCTURAL_DESIGN, dtoList.get(1).getDirection());
        }

        @Test
        @DisplayName("空列表：应返回空列表")
        void convertToDTOList_emptyList_shouldReturnEmptyList() {
            List<AssessmentTimeDTO> dtoList = converter.convertToDTOList(new ArrayList<>());

            assertNotNull(dtoList);
            assertTrue(dtoList.isEmpty());
        }
    }
}
