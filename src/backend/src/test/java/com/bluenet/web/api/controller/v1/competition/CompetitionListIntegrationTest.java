package com.bluenet.web.api.controller.v1.competition;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.competition.CompetitionResponseDTO;
import com.bluenet.web.domain.model.entity.Competition;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.IntroduceImage;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.ImageType;
import com.bluenet.web.infrastructure.repository.mapper.CompetitionMapper;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.infrastructure.repository.mapper.IntroduceImageMapper;
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
 * 竞赛列表关联介绍图片查询（tb_introduce_image 表，type='competition'） - 无介绍图片时的处理 - 多张介绍图片时按
 * sort_order 取第一张 - 边界条件：空列表、limit 参数校验
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
    private IntroduceImageMapper introduceImageMapper;

    @Autowired
    private FileMapper fileMapper;

    private static final String TEST_LEVEL = "国家级";
    private static final String TEST_MONTH = "4月";
    private static final String TEST_ORGANIZER = "工业和信息化部人才交流中心";

    private Long logoFileId;
    private Long imageFileId1;
    private Long imageFileId2;

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

        File imageFile1 = File.builder()
                .name("image1.jpg")
                .url("http://example.com/image1.jpg")
                .type(FileType.NORMAL_IMG)
                .build();
        fileMapper.insert(imageFile1);
        imageFileId1 = imageFile1.getId();

        File imageFile2 = File.builder()
                .name("image2.jpg")
                .url("http://example.com/image2.jpg")
                .type(FileType.NORMAL_IMG)
                .build();
        fileMapper.insert(imageFile2);
        imageFileId2 = imageFile2.getId();
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
        competition.setDetail("蓝桥杯详情");
        competition.setLevel(TEST_LEVEL);
        competition.setMonth(TEST_MONTH);
        competition.setOrganizer(TEST_ORGANIZER);
        competition.setSortOrder(100);
        competition.setEnabled(true);
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

    // ==================== 介绍图片关联查询 ====================

    /**
     * 集成测试：竞赛列表应关联查询介绍图片（tb_introduce_image 表，type='competition'）
     */
    @Test
    @DisplayName("集成测试：竞赛列表应关联查询介绍图片")
    void getCompetitionList_withIntroduceImage_shouldReturnImageFileId() {
        // 准备：创建竞赛和介绍图片
        Competition competition = new Competition();
        competition.setName("蓝桥杯");
        competition.setShortName("蓝桥杯");
        competition.setLogoFileId(logoFileId);
        competition.setSummary("全国软件和信息技术专业人才大赛");
        competition.setLevel(TEST_LEVEL);
        competition.setSortOrder(100);
        competition.setEnabled(true);
        competitionMapper.insert(competition);

        // 创建介绍图片
        IntroduceImage image = new IntroduceImage();
        image.setType(ImageType.COMPETITION);
        image.setCompetitionId(competition.getId());
        image.setFileId(imageFileId1);
        image.setDescription("竞赛照片");
        image.setSortOrder(10);
        introduceImageMapper.insert(image);

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
        assertEquals(2L, dto.getIntroduceImageFileId());
    }

    /**
     * 集成测试：无介绍图片时 introduceImageFileId 应为 null
     */
    @Test
    @DisplayName("集成测试：无介绍图片时 introduceImageFileId 应为 null")
    void getCompetitionList_noIntroduceImage_shouldReturnNullImageFileId() {
        // 准备：创建竞赛但不创建介绍图片
        Competition competition = new Competition();
        competition.setName("无图片竞赛");
        competition.setShortName("无图片");
        competition.setLogoFileId(logoFileId);
        competition.setSummary("这个竞赛没有介绍图片");
        competition.setSortOrder(100);
        competition.setEnabled(true);
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
        assertNull(dto.getIntroduceImageFileId());
    }

    /**
     * 集成测试：多张介绍图片时应按 sort_order 取第一张（sort_order 最大的）
     */
    @Test
    @DisplayName("集成测试：多张介绍图片时应按 sort_order 取第一张")
    void getCompetitionList_multipleImages_shouldReturnFirstBySortOrder() {
        // 准备：创建竞赛和多张介绍图片
        Competition competition = new Competition();
        competition.setName("多图片竞赛");
        competition.setShortName("多图片");
        competition.setLogoFileId(logoFileId);
        competition.setSummary("这个竞赛有多张介绍图片");
        competition.setSortOrder(100);
        competition.setEnabled(true);
        competitionMapper.insert(competition);

        Long competitionId = competition.getId();

        // 创建多张介绍图片，sort_order 不同
        IntroduceImage image1 = new IntroduceImage();
        image1.setType(ImageType.COMPETITION);
        image1.setCompetitionId(competitionId);
        image1.setFileId(imageFileId1); // sort_order=5
        image1.setDescription("图片1");
        image1.setSortOrder(5);
        introduceImageMapper.insert(image1);

        IntroduceImage image2 = new IntroduceImage();
        image2.setType(ImageType.COMPETITION);
        image2.setCompetitionId(competitionId);
        image2.setFileId(imageFileId2); // sort_order=20，应该取这张
        image2.setDescription("图片2");
        image2.setSortOrder(20);
        introduceImageMapper.insert(image2);

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
        // 应该返回 sort_order=20 的图片 fileId
        assertEquals(3L, dto.getIntroduceImageFileId());
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
            competition.setEnabled(true);
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
            competition.setEnabled(true);
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
            competition.setEnabled(true);
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
     * 集成测试：禁用的竞赛不应出现在列表中
     */
    @Test
    @DisplayName("集成测试：禁用的竞赛不应出现在列表中")
    void getCompetitionList_disabledCompetition_shouldNotAppear() {
        // 准备：创建启用的竞赛
        Competition enabledCompetition = new Competition();
        enabledCompetition.setName("启用的竞赛");
        enabledCompetition.setShortName("启用");
        enabledCompetition.setLogoFileId(logoFileId);
        enabledCompetition.setSummary("这个竞赛是启用的");
        enabledCompetition.setSortOrder(100);
        enabledCompetition.setEnabled(true);
        competitionMapper.insert(enabledCompetition);

        // 创建禁用的竞赛
        Competition disabledCompetition = new Competition();
        disabledCompetition.setName("禁用的竞赛");
        disabledCompetition.setShortName("禁用");
        disabledCompetition.setLogoFileId(logoFileId);
        disabledCompetition.setSummary("这个竞赛是禁用的");
        disabledCompetition.setSortOrder(90);
        disabledCompetition.setEnabled(false);
        competitionMapper.insert(disabledCompetition);

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
        assertEquals("启用的竞赛", competitions.get(0).getName());
    }

    /**
     * 集成测试：多个竞赛各自应有正确的介绍图片
     */
    @Test
    @DisplayName("集成测试：多个竞赛各自应有正确的介绍图片")
    void getCompetitionList_multipleCompetitionsWithImages_shouldReturnCorrectImages() {
        // 准备：创建两个竞赛，各自有介绍图片
        Competition competition1 = new Competition();
        competition1.setName("竞赛1");
        competition1.setShortName("C1");
        competition1.setLogoFileId(logoFileId);
        competition1.setSummary("竞赛1简介");
        competition1.setSortOrder(100);
        competition1.setEnabled(true);
        competitionMapper.insert(competition1);

        Competition competition2 = new Competition();
        competition2.setName("竞赛2");
        competition2.setShortName("C2");
        competition2.setLogoFileId(logoFileId);
        competition2.setSummary("竞赛2简介");
        competition2.setSortOrder(90);
        competition2.setEnabled(true);
        competitionMapper.insert(competition2);

        // 为竞赛1创建介绍图片
        IntroduceImage image1 = new IntroduceImage();
        image1.setType(ImageType.COMPETITION);
        image1.setCompetitionId(competition1.getId());
        image1.setFileId(imageFileId1);
        image1.setDescription("竞赛1图片");
        image1.setSortOrder(10);
        introduceImageMapper.insert(image1);

        // 为竞赛2创建介绍图片
        IntroduceImage image2 = new IntroduceImage();
        image2.setType(ImageType.COMPETITION);
        image2.setCompetitionId(competition2.getId());
        image2.setFileId(imageFileId2);
        image2.setDescription("竞赛2图片");
        image2.setSortOrder(10);
        introduceImageMapper.insert(image2);

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
        assertEquals(2L, competitions.get(0).getIntroduceImageFileId());

        assertEquals("竞赛2", competitions.get(1).getName());
        assertEquals(3L, competitions.get(1).getIntroduceImageFileId());
    }
}
