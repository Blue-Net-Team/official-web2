package com.bluenet.web.api.controller.v1.competition;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.competition.CompetitionResponseDTO;
import com.bluenet.web.domain.model.entity.Competition;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.infrastructure.repository.mapper.CompetitionMapper;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 竞赛列表页面集成测试
 * <p>
 * 测试竞赛列表接口的完整功能，包括： - 正常查询竞赛列表（包含 level、month、organizer 字段） -
 * 竞赛封面文件查询（tb_competition.cover_file_id） - 无封面时的处理 - 边界条件：空列表、limit 参数校验
 * </p>
 */
@DisplayName("竞赛列表页面集成测试")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class CompetitionListIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private FileMapper fileMapper;

    private static final String TEST_LEVEL = "国家级";
    private static final String TEST_MONTH = "4月";
    private static final String TEST_ORGANIZER = "工业和信息化部人才交流中心";

    private Long logoFileId;

    @BeforeEach
    void setUpTestData() {
        // 创建测试文件（不指定ID，使用自动生成的ID）
        File logoFile = File.builder()
                .name("logo.png")
                .url("http://example.com/logo.png")
                .type(FileType.NORMAL_IMG)
                .build();
        fileMapper.insert(logoFile);
        logoFileId = logoFile.getId();
    }

    // ==================== 正常查询竞赛列表 ====================

    /**
     * 集成测试：正常查询竞赛列表应返回包含 level、month、organizer 字段的数据
     */
    @Test
    @DisplayName("集成测试：正常查询竞赛列表应返回包含 level、month、organizer 字段的数据")
    void getCompetitionList_shouldReturnCompetitionsWithAllFields() {
        // 准备：创建测试竞赛
        Competition competition = new Competition();
        competition.setName("蓝桥杯");
        competition.setShortName("蓝桥杯");
        competition.setLogoFileId(logoFileId);
        competition.setSummary("全国软件和信息技术专业人才大赛");
        competition.setLevel(TEST_LEVEL);
        competition.setMonth(TEST_MONTH);
        competition.setOrganizer(TEST_ORGANIZER);
        competition.setSortOrder(100);
        competitionMapper.insert(competition);

        // 执行
        ResponseEntity<ResponseMessage<List<CompetitionResponseDTO>>> response = restTemplate.exchange(
                "/api/v1/competitions?limit=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionResponseDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        List<CompetitionResponseDTO> competitions = response.getBody().getData();
        assertNotNull(competitions);
        assertEquals(1, competitions.size());

        CompetitionResponseDTO dto = competitions.get(0);
        assertEquals("蓝桥杯", dto.getName());
        assertEquals(TEST_LEVEL, dto.getLevel());
        assertEquals(TEST_MONTH, dto.getMonth());
        assertEquals(TEST_ORGANIZER, dto.getOrganizer());
        assertEquals("全国软件和信息技术专业人才大赛", dto.getSummary());
    }

    // ==================== 封面文件关联查询 ====================

    /**
     * 集成测试：竞赛列表有coverFileId时应正确返回
     */
    @Test
    @DisplayName("集成测试：竞赛列表有coverFileId时应正确返回")
    void getCompetitionList_withCoverFile_shouldReturnCoverFileId() {
        // 创建封面文件
        File coverFile = File.builder()
                .name("cover.jpg")
                .url("http://example.com/cover.jpg")
                .type(FileType.NORMAL_IMG)
                .build();
        fileMapper.insert(coverFile);

        // 准备：创建竞赛并设置封面
        Competition competition = new Competition();
        competition.setName("蓝桥杯");
        competition.setShortName("蓝桥杯");
        competition.setLogoFileId(logoFileId);
        competition.setCoverFileId(coverFile.getId());
        competition.setSummary("全国软件和信息技术专业人才大赛");
        competition.setLevel(TEST_LEVEL);
        competition.setSortOrder(100);
        competitionMapper.insert(competition);

        // 执行
        ResponseEntity<ResponseMessage<List<CompetitionResponseDTO>>> response = restTemplate.exchange(
                "/api/v1/competitions?limit=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionResponseDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<CompetitionResponseDTO> competitions = response.getBody().getData();
        assertEquals(1, competitions.size());

        CompetitionResponseDTO dto = competitions.get(0);
        assertEquals(coverFile.getId(), dto.getCoverFileId());
    }

    /**
     * 集成测试：无封面时 coverFileId 应为 null
     */
    @Test
    @DisplayName("集成测试：无封面时 coverFileId 应为 null")
    void getCompetitionList_noCoverFile_shouldReturnNullCoverFileId() {
        // 准备：创建竞赛但不设置封面
        Competition competition = new Competition();
        competition.setName("无封面竞赛");
        competition.setShortName("无封面");
        competition.setLogoFileId(logoFileId);
        competition.setSummary("这个竞赛没有封面");
        competition.setSortOrder(100);
        competitionMapper.insert(competition);

        // 执行
        ResponseEntity<ResponseMessage<List<CompetitionResponseDTO>>> response = restTemplate.exchange(
                "/api/v1/competitions?limit=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionResponseDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<CompetitionResponseDTO> competitions = response.getBody().getData();
        assertEquals(1, competitions.size());

        CompetitionResponseDTO dto = competitions.get(0);
        assertNull(dto.getCoverFileId());
    }

    // ==================== 边界条件测试 ====================

    /**
     * 集成测试：空列表时应返回空数组
     */
    @Test
    @DisplayName("集成测试：空列表时应返回空数组")
    void getCompetitionList_emptyList_shouldReturnEmptyArray() {
        // 不创建任何竞赛数据

        // 执行
        ResponseEntity<ResponseMessage<List<CompetitionResponseDTO>>> response = restTemplate.exchange(
                "/api/v1/competitions?limit=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionResponseDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        List<CompetitionResponseDTO> competitions = response.getBody().getData();
        assertNotNull(competitions);
        assertTrue(competitions.isEmpty());
    }

    /**
     * 集成测试：limit 参数校验 - 小于 1 时应使用默认值
     */
    @Test
    @DisplayName("集成测试：limit 小于 1 时应返回数据")
    void getCompetitionList_limitLessThanOne_shouldReturnData() {
        // 准备：创建多个竞赛
        for (int i = 1; i <= 5; i++) {
            Competition competition = new Competition();
            competition.setName("竞赛" + i);
            competition.setShortName("竞赛" + i);
            competition.setLogoFileId(logoFileId);
            competition.setSummary("竞赛" + i + "简介");
            competition.setSortOrder(100 - i);
            competitionMapper.insert(competition);
        }

        // 执行：limit=0 应该被处理为 1
        ResponseEntity<ResponseMessage<List<CompetitionResponseDTO>>> response = restTemplate.exchange(
                "/api/v1/competitions?limit=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionResponseDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<CompetitionResponseDTO> competitions = response.getBody().getData();
        // limit=0 被处理为 1，应该返回 1 条数据
        assertEquals(1, competitions.size());
    }

    /**
     * 集成测试：limit 参数校验 - 大于 50 时应限制为 50
     */
    @Test
    @DisplayName("集成测试：limit 大于 50 时应限制为 50")
    void getCompetitionList_limitGreaterThan50_shouldLimitTo50() {
        // 准备：创建 60 个竞赛
        for (int i = 1; i <= 60; i++) {
            Competition competition = new Competition();
            competition.setName("竞赛" + i);
            competition.setShortName("竞赛" + i);
            competition.setLogoFileId(logoFileId);
            competition.setSummary("竞赛" + i + "简介");
            competition.setSortOrder(100 - i);
            competitionMapper.insert(competition);
        }

        // 执行：limit=100 应该被限制为 50
        ResponseEntity<ResponseMessage<List<CompetitionResponseDTO>>> response = restTemplate.exchange(
                "/api/v1/competitions?limit=100",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionResponseDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<CompetitionResponseDTO> competitions = response.getBody().getData();
        // limit=100 被限制为 50，应该返回 50 条数据
        assertEquals(50, competitions.size());
    }

    /**
     * 集成测试：默认 limit 应为 10
     */
    @Test
    @DisplayName("集成测试：默认 limit 应为 10")
    void getCompetitionList_defaultLimit_shouldBe10() {
        // 准备：创建 15 个竞赛
        for (int i = 1; i <= 15; i++) {
            Competition competition = new Competition();
            competition.setName("竞赛" + i);
            competition.setShortName("竞赛" + i);
            competition.setLogoFileId(logoFileId);
            competition.setSummary("竞赛" + i + "简介");
            competition.setSortOrder(100 - i);
            competitionMapper.insert(competition);
        }

        // 执行：不传 limit 参数
        ResponseEntity<ResponseMessage<List<CompetitionResponseDTO>>> response = restTemplate.exchange(
                "/api/v1/competitions",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionResponseDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<CompetitionResponseDTO> competitions = response.getBody().getData();
        // 默认 limit=10，应该返回 10 条数据
        assertEquals(10, competitions.size());
    }

    /**
     * 集成测试：多个竞赛各自应有正确的封面
     */
    @Test
    @DisplayName("集成测试：多个竞赛各自应有正确的封面")
    void getCompetitionList_multipleCompetitionsWithCovers_shouldReturnCorrectCovers() {
        // 创建封面文件
        File coverFile1 = File.builder()
                .name("cover1.jpg")
                .url("http://example.com/cover1.jpg")
                .type(FileType.NORMAL_IMG)
                .build();
        fileMapper.insert(coverFile1);

        File coverFile2 = File.builder()
                .name("cover2.jpg")
                .url("http://example.com/cover2.jpg")
                .type(FileType.NORMAL_IMG)
                .build();
        fileMapper.insert(coverFile2);

        // 准备：创建两个竞赛，各自有封面
        Competition competition1 = new Competition();
        competition1.setName("竞赛1");
        competition1.setShortName("C1");
        competition1.setLogoFileId(logoFileId);
        competition1.setCoverFileId(coverFile1.getId());
        competition1.setSummary("竞赛1简介");
        competition1.setSortOrder(100);
        competitionMapper.insert(competition1);

        Competition competition2 = new Competition();
        competition2.setName("竞赛2");
        competition2.setShortName("C2");
        competition2.setLogoFileId(logoFileId);
        competition2.setCoverFileId(coverFile2.getId());
        competition2.setSummary("竞赛2简介");
        competition2.setSortOrder(90);
        competitionMapper.insert(competition2);

        // 执行
        ResponseEntity<ResponseMessage<List<CompetitionResponseDTO>>> response = restTemplate.exchange(
                "/api/v1/competitions?limit=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionResponseDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<CompetitionResponseDTO> competitions = response.getBody().getData();
        assertEquals(2, competitions.size());

        // 按排序顺序，竞赛1应该在前面
        assertEquals("竞赛1", competitions.get(0).getName());
        assertEquals(coverFile1.getId(), competitions.get(0).getCoverFileId());

        assertEquals("竞赛2", competitions.get(1).getName());
        assertEquals(coverFile2.getId(), competitions.get(1).getCoverFileId());
    }
}
