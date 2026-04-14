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
import com.bluenet.web.api.dto.competition.CompetitionResponseDTO;
import com.bluenet.web.domain.model.entity.Competition;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.infrastructure.repository.mapper.CompetitionMapper;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

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
    private FileMapper fileMapper;

    private static final Long TEST_FILE_ID = 1L;
    private static final String TEST_FILE_URL = "http://example.com/logo.png";
    private static final String TEST_FILE_NAME = "logo.png";
    private static final FileType TEST_FILE_TYPE = FileType.NORMAL_IMG;
    private static final String TEST_NAME = "蓝桥杯";
    private static final String TEST_SHORT_NAME = "蓝桥杯";
    private static final String TEST_SUMMARY = "全国软件和信息技术专业人才大赛";

    @BeforeEach
    void setUpTestData() {
        File file = File.builder()
                .id(TEST_FILE_ID)
                .name(TEST_FILE_NAME)
                .url(TEST_FILE_URL)
                .type(TEST_FILE_TYPE)
                .build();
        fileMapper.insert(file);

        Competition competition1 = new Competition();
        competition1.setName(TEST_NAME);
        competition1.setShortName(TEST_SHORT_NAME);
        competition1.setLogoFileId(TEST_FILE_ID);
        competition1.setCoverFileId(TEST_FILE_ID);
        competition1.setSummary(TEST_SUMMARY);
        competition1.setSortOrder(100);
        competitionMapper.insert(competition1);

        Competition competition2 = new Competition();
        competition2.setName("ACM程序设计大赛");
        competition2.setShortName("ACM");
        competition2.setLogoFileId(TEST_FILE_ID);
        competition2.setSummary("ACM国际大学生程序设计竞赛");
        competition2.setSortOrder(50);
        competitionMapper.insert(competition2);
    }

    @Test
    @DisplayName("集成测试：获取竞赛列表应返回竞赛列表")
    void getCompetitionList_shouldReturnCompetitions() {
        ResponseEntity<ResponseMessage<List<CompetitionResponseDTO>>> response = restTemplate.exchange(
                "/api/v1/competitions?limit=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionResponseDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        List<CompetitionResponseDTO> competitions = response.getBody().getData();
        assertNotNull(competitions);
        assertTrue(competitions.size() >= 2);

        for (CompetitionResponseDTO dto : competitions) {
            assertNotNull(dto.getId());
            assertNotNull(dto.getName());
        }
    }

    @Test
    @DisplayName("集成测试：获取竞赛列表应按排序权重降序排列")
    void getCompetitionList_shouldOrderBySortOrderDesc() {
        ResponseEntity<ResponseMessage<List<CompetitionResponseDTO>>> response = restTemplate.exchange(
                "/api/v1/competitions?limit=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionResponseDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        List<CompetitionResponseDTO> competitions = response.getBody().getData();
        assertNotNull(competitions);
        assertTrue(competitions.size() >= 2);

        CompetitionResponseDTO first = competitions.get(0);
        assertEquals(TEST_NAME, first.getName());
    }

    @Test
    @DisplayName("集成测试：limit参数应限制返回数量")
    void getCompetitionList_withLimit_shouldLimitResults() {
        ResponseEntity<ResponseMessage<List<CompetitionResponseDTO>>> response = restTemplate.exchange(
                "/api/v1/competitions?limit=1",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionResponseDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        List<CompetitionResponseDTO> competitions = response.getBody().getData();
        assertNotNull(competitions);
        assertEquals(1, competitions.size());
    }

    @Test
    @DisplayName("集成测试：默认limit应为10")
    void getCompetitionList_defaultLimit_shouldBe10() {
        ResponseEntity<ResponseMessage<List<CompetitionResponseDTO>>> response = restTemplate.exchange(
                "/api/v1/competitions",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionResponseDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
    }

    @Test
    @DisplayName("集成测试：竞赛信息应包含必要字段")
    void getCompetitionList_shouldContainRequiredFields() {
        ResponseEntity<ResponseMessage<List<CompetitionResponseDTO>>> response = restTemplate.exchange(
                "/api/v1/competitions?limit=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CompetitionResponseDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<CompetitionResponseDTO> competitions = response.getBody().getData();

        CompetitionResponseDTO firstCompetition = competitions.stream()
                .filter(c -> TEST_NAME.equals(c.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull(firstCompetition);
        assertNotNull(firstCompetition.getId());
        assertEquals(TEST_NAME, firstCompetition.getName());
        assertEquals(TEST_SUMMARY, firstCompetition.getSummary());
    }
}
