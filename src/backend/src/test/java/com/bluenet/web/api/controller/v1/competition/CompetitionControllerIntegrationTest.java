package com.bluenet.web.api.controller.v1.competition;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

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

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.competition.CompetitionBriefDTO;
import com.bluenet.web.api.dto.competition.CompetitionDetailDTO;
import com.bluenet.web.domain.model.entity.Competition;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.IntroduceImage;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.ImageType;
import com.bluenet.web.infrastructure.repository.mapper.CompetitionMapper;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.infrastructure.repository.mapper.IntroduceImageMapper;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

/**
 * CompetitionController集成测试
 */
@DisplayName("CompetitionController 集成测试")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class CompetitionControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private IntroduceImageMapper introduceImageMapper;

    @Autowired
    private FileMapper fileMapper;

    private static final Long TEST_FILE_ID = 1L;
    private static final String TEST_FILE_URL = "http://example.com/logo.png";
    private static final String TEST_FILE_NAME = "logo.png";
    private static final FileType TEST_FILE_TYPE = FileType.NORMAL_IMG;
    private static final String TEST_NAME = "蓝桥杯";
    private static final String TEST_SHORT_NAME = "蓝桥杯";
    private static final String TEST_SUMMARY = "全国软件和信息技术专业人才大赛";
    private static final String TEST_DETAIL = "蓝桥杯全国软件和信息技术专业人才大赛是由工业和信息化部人才交流中心举办的全国性IT学科赛事。";

    @BeforeEach
    void setUpTestData() {
        // 创建测试文件
        File file = File.builder()
                .id(TEST_FILE_ID)
                .name(TEST_FILE_NAME)
                .url(TEST_FILE_URL)
                .type(TEST_FILE_TYPE)
                .build();
        fileMapper.insert(file);

        // 创建测试竞赛1
        Competition competition1 = new Competition();
        competition1.setName(TEST_NAME);
        competition1.setShortName(TEST_SHORT_NAME);
        competition1.setLogoFileId(TEST_FILE_ID);
        competition1.setSummary(TEST_SUMMARY);
        competition1.setDetail(TEST_DETAIL);
        competition1.setSortOrder(100);
        competition1.setEnabled(true);
        competitionMapper.insert(competition1);

        // 创建测试竞赛2
        Competition competition2 = new Competition();
        competition2.setName("ACM程序设计大赛");
        competition2.setShortName("ACM");
        competition2.setLogoFileId(TEST_FILE_ID);
        competition2.setSummary("ACM国际大学生程序设计竞赛");
        competition2.setDetail("ACM国际大学生程序设计竞赛是全球最具影响力的大学生程序设计竞赛");
        competition2.setSortOrder(50);
        competition2.setEnabled(true);
        competitionMapper.insert(competition2);

        // 创建禁用的竞赛
        Competition disabledCompetition = new Competition();
        disabledCompetition.setName("已禁用的竞赛");
        disabledCompetition.setShortName("DISABLED");
        disabledCompetition.setSummary("这个竞赛已被禁用");
        disabledCompetition.setSortOrder(0);
        disabledCompetition.setEnabled(false);
        competitionMapper.insert(disabledCompetition);

        // 为竞赛1创建测试图片
        IntroduceImage image1 = new IntroduceImage();
        image1.setType(ImageType.COMPETITION);
        image1.setCompetitionId(competition1.getId());
        image1.setFileId(TEST_FILE_ID);
        image1.setDescription("竞赛照片1");
        image1.setSortOrder(1);
        introduceImageMapper.insert(image1);

        IntroduceImage image2 = new IntroduceImage();
        image2.setType(ImageType.COMPETITION);
        image2.setCompetitionId(competition1.getId());
        image2.setFileId(TEST_FILE_ID);
        image2.setDescription("竞赛照片2");
        image2.setSortOrder(2);
        introduceImageMapper.insert(image2);
    }

    // ==================== GET /api/v1/competitions ====================

    /**
     * 集成测试：获取竞赛列表应返回启用的竞赛
     */
    @Test
    @DisplayName("集成测试：获取竞赛列表应返回启用的竞赛")
    void getCompetitionList_shouldReturnEnabledCompetitions() {
        // 执行
        ResponseEntity<ResponseMessage<List<CompetitionBriefDTO>>> response = restTemplate.exchange(
                "/api/v1/competitions?limit=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionBriefDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        List<CompetitionBriefDTO> competitions = response.getBody().getData();
        assertNotNull(competitions);
        assertTrue(competitions.size() >= 2);

        // 验证只返回启用的竞赛
        for (CompetitionBriefDTO dto : competitions) {
            assertNotNull(dto.getId());
            assertNotNull(dto.getName());
        }
    }

    /**
     * 集成测试：获取竞赛列表应按排序权重降序排列
     */
    @Test
    @DisplayName("集成测试：获取竞赛列表应按排序权重降序排列")
    void getCompetitionList_shouldOrderBySortOrderDesc() {
        // 执行
        ResponseEntity<ResponseMessage<List<CompetitionBriefDTO>>> response = restTemplate.exchange(
                "/api/v1/competitions?limit=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionBriefDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        List<CompetitionBriefDTO> competitions = response.getBody().getData();
        assertNotNull(competitions);
        assertTrue(competitions.size() >= 2);

        // 验证排序：蓝桥杯(sortOrder=100)应该在ACM(sortOrder=50)前面
        CompetitionBriefDTO first = competitions.get(0);
        assertEquals(TEST_NAME, first.getName());
    }

    /**
     * 集成测试：limit参数应限制返回数量
     */
    @Test
    @DisplayName("集成测试：limit参数应限制返回数量")
    void getCompetitionList_withLimit_shouldLimitResults() {
        // 执行
        ResponseEntity<ResponseMessage<List<CompetitionBriefDTO>>> response = restTemplate.exchange(
                "/api/v1/competitions?limit=1",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionBriefDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        List<CompetitionBriefDTO> competitions = response.getBody().getData();
        assertNotNull(competitions);
        assertEquals(1, competitions.size());
    }

    /**
     * 集成测试：默认limit应为10
     */
    @Test
    @DisplayName("集成测试：默认limit应为10")
    void getCompetitionList_defaultLimit_shouldBe10() {
        // 执行
        ResponseEntity<ResponseMessage<List<CompetitionBriefDTO>>> response = restTemplate.exchange(
                "/api/v1/competitions",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionBriefDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
    }

    /**
     * 集成测试：竞赛简要信息应包含必要字段
     */
    @Test
    @DisplayName("集成测试：竞赛简要信息应包含必要字段")
    void getCompetitionList_shouldContainRequiredFields() {
        // 执行
        ResponseEntity<ResponseMessage<List<CompetitionBriefDTO>>> response = restTemplate.exchange(
                "/api/v1/competitions?limit=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionBriefDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<CompetitionBriefDTO> competitions = response.getBody().getData();

        CompetitionBriefDTO firstCompetition = competitions.stream()
                .filter(c -> TEST_NAME.equals(c.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull(firstCompetition);
        assertNotNull(firstCompetition.getId());
        assertEquals(TEST_NAME, firstCompetition.getName());
        assertEquals(TEST_SHORT_NAME, firstCompetition.getShortName());
        assertEquals(TEST_FILE_URL, firstCompetition.getLogoUrl());
        assertEquals(TEST_FILE_ID, firstCompetition.getLogoFileId());
        assertEquals(TEST_SUMMARY, firstCompetition.getSummary());
    }

    // ==================== GET /api/v1/competitions/{id} ====================

    /**
     * 集成测试：获取竞赛详情应返回完整信息
     */
    @Test
    @DisplayName("集成测试：获取竞赛详情应返回完整信息")
    void getCompetitionDetail_shouldReturnCompleteInfo() {
        // 准备：获取第一个启用的竞赛ID
        ResponseEntity<ResponseMessage<List<CompetitionBriefDTO>>> listResponse = restTemplate.exchange(
                "/api/v1/competitions?limit=1",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionBriefDTO>>>() {
                });
        Long competitionId = listResponse.getBody().getData().get(0).getId();

        // 执行
        ResponseEntity<ResponseMessage<CompetitionDetailDTO>> response = restTemplate.exchange(
                "/api/v1/competitions/" + competitionId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<CompetitionDetailDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        CompetitionDetailDTO detail = response.getBody().getData();
        assertNotNull(detail);
        assertEquals(competitionId, detail.getId());
        assertNotNull(detail.getName());
        assertNotNull(detail.getSummary());
        assertNotNull(detail.getDetail());
    }

    /**
     * 集成测试：获取竞赛详情应包含关联图片
     */
    @Test
    @DisplayName("集成测试：获取竞赛详情应包含关联图片")
    void getCompetitionDetail_shouldIncludeImages() {
        // 准备：获取蓝桥杯竞赛ID
        ResponseEntity<ResponseMessage<List<CompetitionBriefDTO>>> listResponse = restTemplate.exchange(
                "/api/v1/competitions?limit=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionBriefDTO>>>() {
                });
        Long competitionId = listResponse.getBody()
                .getData()
                .stream()
                .filter(c -> TEST_NAME.equals(c.getName()))
                .findFirst()
                .get()
                .getId();

        // 执行
        ResponseEntity<ResponseMessage<CompetitionDetailDTO>> response = restTemplate.exchange(
                "/api/v1/competitions/" + competitionId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<CompetitionDetailDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        CompetitionDetailDTO detail = response.getBody().getData();
        assertNotNull(detail.getImages());
        assertEquals(2, detail.getImages().size());

        // 验证图片信息
        assertNotNull(detail.getImages().get(0).getId());
        assertEquals(TEST_FILE_URL, detail.getImages().get(0).getUrl());
        assertNotNull(detail.getImages().get(0).getDescription());
    }

    /**
     * 集成测试：获取不存在的竞赛应返回404
     */
    @Test
    @DisplayName("集成测试：获取不存在的竞赛应返回404")
    void getCompetitionDetail_notFound_shouldReturn404() {
        // 执行
        ResponseEntity<ResponseMessage<CompetitionDetailDTO>> response = restTemplate.exchange(
                "/api/v1/competitions/99999",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<CompetitionDetailDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getCode());
        assertEquals("竞赛不存在", response.getBody().getMsg());
    }

    /**
     * 集成测试：无图片的竞赛应返回空图片列表
     */
    @Test
    @DisplayName("集成测试：无图片的竞赛应返回空图片列表")
    void getCompetitionDetail_noImages_shouldReturnEmptyList() {
        // 准备：获取ACM竞赛ID（没有图片）
        ResponseEntity<ResponseMessage<List<CompetitionBriefDTO>>> listResponse = restTemplate.exchange(
                "/api/v1/competitions?limit=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionBriefDTO>>>() {
                });
        Long competitionId = listResponse.getBody()
                .getData()
                .stream()
                .filter(c -> "ACM程序设计大赛".equals(c.getName()))
                .findFirst()
                .get()
                .getId();

        // 执行
        ResponseEntity<ResponseMessage<CompetitionDetailDTO>> response = restTemplate.exchange(
                "/api/v1/competitions/" + competitionId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<CompetitionDetailDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        CompetitionDetailDTO detail = response.getBody().getData();
        assertNotNull(detail.getImages());
        assertTrue(detail.getImages().isEmpty());
    }

    /**
     * 集成测试：竞赛详情应包含所有必要字段
     */
    @Test
    @DisplayName("集成测试：竞赛详情应包含所有必要字段")
    void getCompetitionDetail_shouldContainAllFields() {
        // 准备：获取蓝桥杯竞赛ID
        ResponseEntity<ResponseMessage<List<CompetitionBriefDTO>>> listResponse = restTemplate.exchange(
                "/api/v1/competitions?limit=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionBriefDTO>>>() {
                });
        Long competitionId = listResponse.getBody()
                .getData()
                .stream()
                .filter(c -> TEST_NAME.equals(c.getName()))
                .findFirst()
                .get()
                .getId();

        // 执行
        ResponseEntity<ResponseMessage<CompetitionDetailDTO>> response = restTemplate.exchange(
                "/api/v1/competitions/" + competitionId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<CompetitionDetailDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        CompetitionDetailDTO detail = response.getBody().getData();

        assertEquals(competitionId, detail.getId());
        assertEquals(TEST_NAME, detail.getName());
        assertEquals(TEST_SHORT_NAME, detail.getShortName());
        assertEquals(TEST_FILE_URL, detail.getLogoUrl());
        assertEquals(TEST_FILE_ID, detail.getLogoFileId());
        assertEquals(TEST_SUMMARY, detail.getSummary());
        assertEquals(TEST_DETAIL, detail.getDetail());
    }

    /**
     * 集成测试：禁用的竞赛不应在列表中显示
     */
    @Test
    @DisplayName("集成测试：禁用的竞赛不应在列表中显示")
    void getCompetitionList_shouldNotIncludeDisabledCompetitions() {
        // 执行
        ResponseEntity<ResponseMessage<List<CompetitionBriefDTO>>> response = restTemplate.exchange(
                "/api/v1/competitions?limit=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionBriefDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<CompetitionBriefDTO> competitions = response.getBody().getData();

        // 验证禁用的竞赛不在列表中
        boolean hasDisabledCompetition = competitions.stream().anyMatch(c -> "已禁用的竞赛".equals(c.getName()));
        assertFalse(hasDisabledCompetition);
    }
}
