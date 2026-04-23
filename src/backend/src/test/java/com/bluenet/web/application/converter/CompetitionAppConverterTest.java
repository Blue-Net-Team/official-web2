package com.bluenet.web.application.converter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.bluenet.web.api.dto.competition.CompetitionResponseDTO;
import com.bluenet.web.domain.model.vo.CompetitionVO;

@DisplayName("CompetitionAppConverter 单元测试")
class CompetitionAppConverterTest {

    private final CompetitionAppConverter converter = new CompetitionAppConverter();

    private static final Long TEST_ID = 1L;
    private static final String TEST_NAME = "蓝桥杯";
    private static final String TEST_SHORT_NAME = "蓝桥杯";
    private static final String TEST_LOGO_URL = "http://example.com/logo.png";
    private static final Long TEST_LOGO_FILE_ID = 100L;
    private static final Long TEST_COVER_FILE_ID = 200L;
    private static final String TEST_SUMMARY = "全国软件和信息技术专业人才大赛";

    private CompetitionVO createTestCompetitionBriefVO() {
        return CompetitionVO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .shortName(TEST_SHORT_NAME)
                .logoUrl(TEST_LOGO_URL)
                .logoFileId(TEST_LOGO_FILE_ID)
                .coverFileId(TEST_COVER_FILE_ID)
                .summary(TEST_SUMMARY)
                .build();
    }

    @Test
    @DisplayName("转换为响应DTO：应正确转换所有字段")
    void convertToResponseDTO_shouldConvertAllFields() {
        CompetitionVO vo = createTestCompetitionBriefVO();

        CompetitionResponseDTO dto = converter.convertToResponseDTO(vo);

        assertNotNull(dto);
        assertEquals(TEST_ID, dto.getId());
        assertEquals(TEST_NAME, dto.getName());
        assertEquals(TEST_SUMMARY, dto.getSummary());
        assertEquals(TEST_LOGO_FILE_ID, dto.getLogoFileId());
        assertEquals(TEST_COVER_FILE_ID, dto.getCoverFileId());
    }

    @Test
    @DisplayName("转换为响应DTO：null字段应保持null")
    void convertToResponseDTO_withNullFields_shouldKeepNull() {
        CompetitionVO vo = CompetitionVO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();

        CompetitionResponseDTO dto = converter.convertToResponseDTO(vo);

        assertNotNull(dto);
        assertEquals(TEST_ID, dto.getId());
        assertEquals(TEST_NAME, dto.getName());
        assertNull(dto.getSummary());
        assertNull(dto.getLogoFileId());
        assertNull(dto.getCoverFileId());
    }

    @Test
    @DisplayName("转换为响应DTO列表：应正确转换列表")
    void convertToResponseDTOList_shouldConvertList() {
        List<CompetitionVO> voList = new ArrayList<>();
        voList.add(createTestCompetitionBriefVO());
        voList.add(
                CompetitionVO.builder()
                        .id(2L)
                        .name("ACM程序设计大赛")
                        .summary("ACM国际大学生程序设计竞赛")
                        .build());

        List<CompetitionResponseDTO> dtoList = converter.convertToResponseDTOList(voList);

        assertNotNull(dtoList);
        assertEquals(2, dtoList.size());
        assertEquals(TEST_ID, dtoList.get(0).getId());
        assertEquals("ACM程序设计大赛", dtoList.get(1).getName());
    }

    @Test
    @DisplayName("转换为响应DTO列表：空列表应返回空列表")
    void convertToResponseDTOList_emptyList_shouldReturnEmptyList() {
        List<CompetitionVO> voList = new ArrayList<>();

        List<CompetitionResponseDTO> dtoList = converter.convertToResponseDTOList(voList);

        assertNotNull(dtoList);
        assertTrue(dtoList.isEmpty());
    }

    @Test
    @DisplayName("转换为响应DTO：超长名称应正确转换")
    void convertToResponseDTO_withLongName_shouldConvertCorrectly() {
        String longName = "A".repeat(100);
        CompetitionVO vo = CompetitionVO.builder().id(TEST_ID).name(longName).build();

        CompetitionResponseDTO dto = converter.convertToResponseDTO(vo);

        assertNotNull(dto);
        assertEquals(longName, dto.getName());
    }
}
