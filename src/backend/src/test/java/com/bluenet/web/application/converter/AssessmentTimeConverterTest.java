package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.assessment_time.AssessmentTimeDTO;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AssessmentTimeConverter 单元测试
 * <p>
 * 测试考核时间 VO 与 DTO 之间的转换逻辑
 * </p>
 */
@DisplayName("AssessmentTimeConverter 单元测试")
class AssessmentTimeConverterTest {

    private final AssessmentTimeConverter converter = new AssessmentTimeConverter();

    private AssessmentTimeVO createTestVO() {
        return AssessmentTimeVO.builder()
                .id(1L)
                .direction(Direction.COMPUTER_VISION)
                .epoch(1)
                .grade(2)
                .startTime(LocalDateTime.of(2026, 6, 1, 9, 0))
                .endTime(LocalDateTime.of(2026, 6, 1, 11, 0))
                .timeLimit(true)
                .timeLimitMinutes(120)
                .build();
    }

    // ==================== convertToDTO 测试 ====================

    @Nested
    @DisplayName("convertToDTO 方法测试")
    class ConvertToDTOTests {

        @Test
        @DisplayName("正常转换：应完整映射所有字段")
        void convertToDTO_validVO_shouldMapAllFields() {
            AssessmentTimeVO vo = createTestVO();

            AssessmentTimeDTO dto = converter.convertToDTO(vo);

            assertNotNull(dto);
            assertEquals(1L, dto.getId());
            assertEquals(Direction.COMPUTER_VISION, dto.getDirection());
            assertEquals(1, dto.getEpoch());
            assertEquals(2, dto.getGrade());
            assertEquals(LocalDateTime.of(2026, 6, 1, 9, 0), dto.getStartTime());
            assertEquals(LocalDateTime.of(2026, 6, 1, 11, 0), dto.getEndTime());
            assertTrue(dto.getTimeLimit());
            assertEquals(120, dto.getTimeLimitMinutes());
        }

        @Test
        @DisplayName("不限时考核：timeLimitMinutes 应为 null")
        void convertToDTO_noTimeLimit_shouldHaveNullMinutes() {
            AssessmentTimeVO vo = AssessmentTimeVO.builder()
                    .id(2L)
                    .direction(Direction.EMBEDDED)
                    .epoch(2)
                    .grade(1)
                    .startTime(LocalDateTime.of(2026, 7, 1, 9, 0))
                    .endTime(LocalDateTime.of(2026, 7, 1, 12, 0))
                    .timeLimit(false)
                    .timeLimitMinutes(null)
                    .build();

            AssessmentTimeDTO dto = converter.convertToDTO(vo);

            assertNotNull(dto);
            assertFalse(dto.getTimeLimit());
            assertNull(dto.getTimeLimitMinutes());
        }
    }

    // ==================== convertToDTOList 测试 ====================

    @Nested
    @DisplayName("convertToDTOList 方法测试")
    class ConvertToDTOListTests {

        @Test
        @DisplayName("多个VO：应返回对应数量的DTO")
        void convertToDTOList_multipleVOs_shouldReturnDTOs() {
            List<AssessmentTimeVO> voList = new ArrayList<>();
            voList.add(createTestVO());
            voList.add(
                    AssessmentTimeVO.builder()
                            .id(2L)
                            .direction(Direction.STRUCTURAL_DESIGN)
                            .epoch(1)
                            .grade(3)
                            .startTime(LocalDateTime.of(2026, 8, 1, 9, 0))
                            .endTime(LocalDateTime.of(2026, 8, 1, 11, 0))
                            .timeLimit(false)
                            .build());

            List<AssessmentTimeDTO> dtoList = converter.convertToDTOList(voList);

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
