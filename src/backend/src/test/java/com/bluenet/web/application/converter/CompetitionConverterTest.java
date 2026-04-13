package com.bluenet.web.application.converter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.bluenet.web.api.dto.competition.CompetitionBriefDTO;
import com.bluenet.web.api.dto.competition.CompetitionDetailDTO;
import com.bluenet.web.domain.model.vo.CompetitionBriefVO;
import com.bluenet.web.domain.model.vo.CompetitionVO;

/**
 * CompetitionConverter单元测试
 */
@DisplayName("CompetitionConverter 单元测试")
class CompetitionConverterTest {

    private final CompetitionConverter converter = new CompetitionConverter();

    private static final Long TEST_ID = 1L;
    private static final String TEST_NAME = "蓝桥杯";
    private static final String TEST_SHORT_NAME = "蓝桥杯";
    private static final String TEST_LOGO_URL = "http://example.com/logo.png";
    private static final Long TEST_LOGO_FILE_ID = 100L;
    private static final Long TEST_COVER_FILE_ID = 200L;
    private static final String TEST_SUMMARY = "全国软件和信息技术专业人才大赛";
    private static final String TEST_DETAIL = "蓝桥杯全国软件和信息技术专业人才大赛是由工业和信息化部人才交流中心举办的全国性IT学科赛事。";

    private CompetitionBriefVO createTestCompetitionBriefVO() {
        return CompetitionBriefVO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .shortName(TEST_SHORT_NAME)
                .logoUrl(TEST_LOGO_URL)
                .logoFileId(TEST_LOGO_FILE_ID)
                .coverFileId(TEST_COVER_FILE_ID)
                .summary(TEST_SUMMARY)
                .build();
    }

    private CompetitionVO createTestCompetitionVO() {
        return CompetitionVO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .shortName(TEST_SHORT_NAME)
                .logoUrl(TEST_LOGO_URL)
                .logoFileId(TEST_LOGO_FILE_ID)
                .coverFileId(TEST_COVER_FILE_ID)
                .summary(TEST_SUMMARY)
                .detail(TEST_DETAIL)
                .sortOrder(0)
                .build();
    }

    // ==================== convertToBriefDTO ====================

    /**
     * 转换为简要DTO：应正确转换所有字段
     */
    @Test
    @DisplayName("转换为简要DTO：应正确转换所有字段")
    void convertToBriefDTO_shouldConvertAllFields() {
        // 准备
        CompetitionBriefVO vo = createTestCompetitionBriefVO();

        // 执行
        CompetitionBriefDTO dto = converter.convertToBriefDTO(vo);

        // 验证
        assertNotNull(dto);
        assertEquals(TEST_ID, dto.getId());
        assertEquals(TEST_NAME, dto.getName());
        assertEquals(TEST_SHORT_NAME, dto.getShortName());
        assertEquals(TEST_LOGO_URL, dto.getLogoUrl());
        assertEquals(TEST_LOGO_FILE_ID, dto.getLogoFileId());
        assertEquals(TEST_COVER_FILE_ID, dto.getCoverFileId());
        assertEquals(TEST_SUMMARY, dto.getSummary());
    }

    /**
     * 转换为简要DTO：null字段应保持null
     */
    @Test
    @DisplayName("转换为简要DTO：null字段应保持null")
    void convertToBriefDTO_withNullFields_shouldKeepNull() {
        // 准备
        CompetitionBriefVO vo = CompetitionBriefVO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .shortName(null)
                .logoUrl(null)
                .logoFileId(null)
                .coverFileId(null)
                .summary(null)
                .build();

        // 执行
        CompetitionBriefDTO dto = converter.convertToBriefDTO(vo);

        // 验证
        assertNotNull(dto);
        assertEquals(TEST_ID, dto.getId());
        assertEquals(TEST_NAME, dto.getName());
        assertNull(dto.getShortName());
        assertNull(dto.getLogoUrl());
        assertNull(dto.getLogoFileId());
        assertNull(dto.getCoverFileId());
        assertNull(dto.getSummary());
    }

    // ==================== convertToBriefDTOList ====================

    /**
     * 转换为简要DTO列表：应正确转换列表
     */
    @Test
    @DisplayName("转换为简要DTO列表：应正确转换列表")
    void convertToBriefDTOList_shouldConvertList() {
        // 准备
        List<CompetitionBriefVO> voList = new ArrayList<>();
        voList.add(createTestCompetitionBriefVO());
        voList.add(
                CompetitionBriefVO.builder()
                        .id(2L)
                        .name("ACM程序设计大赛")
                        .shortName("ACM")
                        .logoUrl("http://example.com/acm.png")
                        .summary("ACM国际大学生程序设计竞赛")
                        .build());

        // 执行
        List<CompetitionBriefDTO> dtoList = converter.convertToBriefDTOList(voList);

        // 验证
        assertNotNull(dtoList);
        assertEquals(2, dtoList.size());
        assertEquals(TEST_ID, dtoList.get(0).getId());
        assertEquals("ACM程序设计大赛", dtoList.get(1).getName());
    }

    /**
     * 转换为简要DTO列表：空列表应返回空列表
     */
    @Test
    @DisplayName("转换为简要DTO列表：空列表应返回空列表")
    void convertToBriefDTOList_emptyList_shouldReturnEmptyList() {
        // 准备
        List<CompetitionBriefVO> voList = new ArrayList<>();

        // 执行
        List<CompetitionBriefDTO> dtoList = converter.convertToBriefDTOList(voList);

        // 验证
        assertNotNull(dtoList);
        assertTrue(dtoList.isEmpty());
    }

    /**
     * 转换为简要DTO列表：单元素列表应正确转换
     */
    @Test
    @DisplayName("转换为简要DTO列表：单元素列表应正确转换")
    void convertToBriefDTOList_singleElement_shouldConvertCorrectly() {
        // 准备
        List<CompetitionBriefVO> voList = new ArrayList<>();
        voList.add(createTestCompetitionBriefVO());

        // 执行
        List<CompetitionBriefDTO> dtoList = converter.convertToBriefDTOList(voList);

        // 验证
        assertNotNull(dtoList);
        assertEquals(1, dtoList.size());
        assertEquals(TEST_ID, dtoList.get(0).getId());
    }

    // ==================== convertToDetailDTO ====================

    /**
     * 转换为详情DTO：应正确转换所有字段
     */
    @Test
    @DisplayName("转换为详情DTO：应正确转换所有字段")
    void convertToDetailDTO_shouldConvertAllFields() {
        // 准备
        CompetitionVO vo = createTestCompetitionVO();

        // 执行
        CompetitionDetailDTO dto = converter.convertToDetailDTO(vo);

        // 验证
        assertNotNull(dto);
        assertEquals(TEST_ID, dto.getId());
        assertEquals(TEST_NAME, dto.getName());
        assertEquals(TEST_SHORT_NAME, dto.getShortName());
        assertEquals(TEST_LOGO_URL, dto.getLogoUrl());
        assertEquals(TEST_LOGO_FILE_ID, dto.getLogoFileId());
        assertEquals(TEST_COVER_FILE_ID, dto.getCoverFileId());
        assertEquals(TEST_SUMMARY, dto.getSummary());
        assertEquals(TEST_DETAIL, dto.getDetail());
    }

    /**
     * 转换为详情DTO：null字段应保持null
     */
    @Test
    @DisplayName("转换为详情DTO：null字段应保持null")
    void convertToDetailDTO_withNullFields_shouldKeepNull() {
        // 准备
        CompetitionVO vo = CompetitionVO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .shortName(null)
                .logoUrl(null)
                .logoFileId(null)
                .coverFileId(null)
                .summary(null)
                .detail(null)
                .build();

        // 执行
        CompetitionDetailDTO dto = converter.convertToDetailDTO(vo);

        // 验证
        assertNotNull(dto);
        assertEquals(TEST_ID, dto.getId());
        assertEquals(TEST_NAME, dto.getName());
        assertNull(dto.getShortName());
        assertNull(dto.getLogoUrl());
        assertNull(dto.getLogoFileId());
        assertNull(dto.getCoverFileId());
        assertNull(dto.getSummary());
        assertNull(dto.getDetail());
    }

    // ==================== 边界条件测试 ====================

    /**
     * 转换为简要DTO：超长名称应正确转换
     */
    @Test
    @DisplayName("转换为简要DTO：超长名称应正确转换")
    void convertToBriefDTO_withLongName_shouldConvertCorrectly() {
        // 准备
        String longName = "A".repeat(100);
        CompetitionBriefVO vo = CompetitionBriefVO.builder().id(TEST_ID).name(longName).build();

        // 执行
        CompetitionBriefDTO dto = converter.convertToBriefDTO(vo);

        // 验证
        assertNotNull(dto);
        assertEquals(longName, dto.getName());
    }

    /**
     * 转换为详情DTO：超长详情应正确转换
     */
    @Test
    @DisplayName("转换为详情DTO：超长详情应正确转换")
    void convertToDetailDTO_withLongDetail_shouldConvertCorrectly() {
        // 准备
        String longDetail = "A".repeat(10000);
        CompetitionVO vo = CompetitionVO.builder().id(TEST_ID).name(TEST_NAME).detail(longDetail).build();

        // 执行
        CompetitionDetailDTO dto = converter.convertToDetailDTO(vo);

        // 验证
        assertNotNull(dto);
        assertEquals(longDetail, dto.getDetail());
    }
}
