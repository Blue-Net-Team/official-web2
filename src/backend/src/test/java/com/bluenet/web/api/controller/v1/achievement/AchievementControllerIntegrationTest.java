package com.bluenet.web.api.controller.v1.achievement;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.testsupport.RepositoryTestObjects;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.achievement.AchievementDTO;
import com.bluenet.web.api.dto.achievement.AchievementStatsDTO;
import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.infrastructure.repository.mapper.AchievementMapper;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

@DisplayName("AchievementController 集成测试")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import({ TestcontainersConfiguration.class, com.bluenet.web.testconfig.TestSecurityConfig.class })
class AchievementControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private FileMapper fileMapper;

    private Long testFileId;

    @BeforeEach
    void setUpTestData() {
        File file = File.builder()
                .name("test-achievement.jpg")
                .url("http://example.com/test.jpg")
                .type(FileType.NORMAL_IMG)
                .build();
        RepositoryTestObjects.insert(fileMapper, file, FileDO.class);
        testFileId = file.getId();
    }

    private Achievement createAchievement(String title, AchievementType type, AwardLevel awardLevel, int year) {
        Achievement achievement = new Achievement();
        achievement.setTitle(title);
        achievement.setType(type);
        achievement.setRelateTo("测试关联项");
        achievement.setAchieveAt(LocalDate.of(year, 4, 15));
        achievement.setAwardLevel(awardLevel);
        achievement.setAwardName(awardLevel != null ? "测试奖项" : null);
        achievement.setFileId(testFileId);
        RepositoryTestObjects.insert(achievementMapper, achievement, AchievementDO.class);
        return achievement;
    }

    @Test
    @DisplayName("集成测试：获取成就列表应返回分页数据")
    void getAchievements_shouldReturnPagedData() {
        createAchievement("国家级竞赛一等奖", AchievementType.COMPETITION, AwardLevel.NATIONAL, 2024);
        createAchievement("省级竞赛二等奖", AchievementType.COMPETITION, AwardLevel.PROVINCIAL, 2024);
        createAchievement("论文发表", AchievementType.PAPER, null, 2024);

        ResponseEntity<ResponseMessage<PageDTO<AchievementDTO>>> response = restTemplate.exchange(
                "/api/v1/achievements?page=0&size=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<PageDTO<AchievementDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        PageDTO<AchievementDTO> page = response.getBody().getData();
        assertNotNull(page);
        assertEquals(3, page.getTotalElements());
        assertEquals(1, page.getTotalPages());
        assertEquals(3, page.getContent().size());
        assertFalse(page.isEmpty());
    }

    @Test
    @DisplayName("集成测试：按类型筛选成就")
    void getAchievements_filterByType_shouldReturnFilteredData() {
        createAchievement("竞赛成就1", AchievementType.COMPETITION, AwardLevel.NATIONAL, 2024);
        createAchievement("竞赛成就2", AchievementType.COMPETITION, AwardLevel.PROVINCIAL, 2024);
        createAchievement("论文成就", AchievementType.PAPER, null, 2024);

        ResponseEntity<ResponseMessage<PageDTO<AchievementDTO>>> response = restTemplate.exchange(
                "/api/v1/achievements?type=COMPETITION&page=0&size=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<PageDTO<AchievementDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        PageDTO<AchievementDTO> page = response.getBody().getData();
        assertEquals(2, page.getTotalElements());
        assertTrue(
                page.getContent()
                        .stream()
                        .allMatch(a -> a.getType() == AchievementType.COMPETITION));
    }

    @Test
    @DisplayName("集成测试：按奖项级别筛选成就")
    void getAchievements_filterByAwardLevel_shouldReturnFilteredData() {
        createAchievement("国家级竞赛", AchievementType.COMPETITION, AwardLevel.NATIONAL, 2024);
        createAchievement("省级竞赛", AchievementType.COMPETITION, AwardLevel.PROVINCIAL, 2024);
        createAchievement("校级竞赛", AchievementType.COMPETITION, AwardLevel.SCHOOL, 2024);

        ResponseEntity<ResponseMessage<PageDTO<AchievementDTO>>> response = restTemplate.exchange(
                "/api/v1/achievements?awardLevel=NATIONAL&page=0&size=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<PageDTO<AchievementDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        PageDTO<AchievementDTO> page = response.getBody().getData();
        assertEquals(1, page.getTotalElements());
        assertEquals(AwardLevel.NATIONAL, page.getContent().get(0).getAwardLevel());
    }

    @Test
    @DisplayName("集成测试：按年份筛选成就")
    void getAchievements_filterByYear_shouldReturnFilteredData() {
        createAchievement("2023年成就", AchievementType.COMPETITION, AwardLevel.NATIONAL, 2023);
        createAchievement("2024年成就1", AchievementType.COMPETITION, AwardLevel.NATIONAL, 2024);
        createAchievement("2024年成就2", AchievementType.PAPER, null, 2024);

        ResponseEntity<ResponseMessage<PageDTO<AchievementDTO>>> response = restTemplate.exchange(
                "/api/v1/achievements?year=2024&page=0&size=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<PageDTO<AchievementDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        PageDTO<AchievementDTO> page = response.getBody().getData();
        assertEquals(2, page.getTotalElements());
    }

    @Test
    @DisplayName("集成测试：组合筛选成就")
    void getAchievements_combinedFilter_shouldReturnFilteredData() {
        createAchievement("2024国家级竞赛", AchievementType.COMPETITION, AwardLevel.NATIONAL, 2024);
        createAchievement("2024省级竞赛", AchievementType.COMPETITION, AwardLevel.PROVINCIAL, 2024);
        createAchievement("2023国家级竞赛", AchievementType.COMPETITION, AwardLevel.NATIONAL, 2023);
        createAchievement("2024论文", AchievementType.PAPER, null, 2024);

        ResponseEntity<ResponseMessage<PageDTO<AchievementDTO>>> response = restTemplate.exchange(
                "/api/v1/achievements?type=COMPETITION&awardLevel=NATIONAL&year=2024&page=0&size=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<PageDTO<AchievementDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        PageDTO<AchievementDTO> page = response.getBody().getData();
        assertEquals(1, page.getTotalElements());
        AchievementDTO result = page.getContent().get(0);
        assertEquals(AchievementType.COMPETITION, result.getType());
        assertEquals(AwardLevel.NATIONAL, result.getAwardLevel());
        assertEquals(2024, result.getAchieveAt().getYear());
    }

    @Test
    @DisplayName("集成测试：空数据库返回空列表")
    void getAchievements_emptyDatabase_shouldReturnEmptyPage() {
        ResponseEntity<ResponseMessage<PageDTO<AchievementDTO>>> response = restTemplate.exchange(
                "/api/v1/achievements?page=0&size=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<PageDTO<AchievementDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        PageDTO<AchievementDTO> page = response.getBody().getData();
        assertTrue(page.isEmpty());
        assertEquals(0, page.getTotalElements());
        assertTrue(page.getContent().isEmpty());
    }

    @Test
    @DisplayName("集成测试：分页参数生效")
    void getAchievements_pagination_shouldWorkCorrectly() {
        for (int i = 1; i <= 15; i++) {
            createAchievement("成就" + i, AchievementType.COMPETITION, AwardLevel.NATIONAL, 2024);
        }

        ResponseEntity<ResponseMessage<PageDTO<AchievementDTO>>> response = restTemplate.exchange(
                "/api/v1/achievements?page=0&size=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<PageDTO<AchievementDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        PageDTO<AchievementDTO> page = response.getBody().getData();
        assertEquals(15, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
        assertEquals(0, page.getNumber());
        assertEquals(10, page.getSize());
        assertEquals(10, page.getNumberOfElements());
        assertTrue(page.isFirst());
        assertFalse(page.isLast());
    }

    @Test
    @DisplayName("集成测试：第二页数据正确")
    void getAchievements_secondPage_shouldReturnCorrectData() {
        for (int i = 1; i <= 15; i++) {
            createAchievement("成就" + i, AchievementType.COMPETITION, AwardLevel.NATIONAL, 2024);
        }

        ResponseEntity<ResponseMessage<PageDTO<AchievementDTO>>> response = restTemplate.exchange(
                "/api/v1/achievements?page=1&size=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<PageDTO<AchievementDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        PageDTO<AchievementDTO> page = response.getBody().getData();
        assertEquals(15, page.getTotalElements());
        assertEquals(1, page.getNumber());
        assertEquals(5, page.getNumberOfElements());
        assertFalse(page.isFirst());
        assertTrue(page.isLast());
    }

    @Test
    @DisplayName("集成测试：获取成就统计（仅统计竞赛成就）")
    void getAchievementStats_shouldReturnCorrectStats() {
        createAchievement("国家级1", AchievementType.COMPETITION, AwardLevel.NATIONAL, 2024);
        createAchievement("国家级2", AchievementType.COMPETITION, AwardLevel.NATIONAL, 2024);
        createAchievement("省级1", AchievementType.COMPETITION, AwardLevel.PROVINCIAL, 2024);
        createAchievement("省级2", AchievementType.COMPETITION, AwardLevel.PROVINCIAL, 2024);
        createAchievement("省级3", AchievementType.COMPETITION, AwardLevel.PROVINCIAL, 2024);
        createAchievement("校级1", AchievementType.COMPETITION, AwardLevel.SCHOOL, 2024);
        createAchievement("论文1", AchievementType.PAPER, null, 2024);

        ResponseEntity<ResponseMessage<AchievementStatsDTO>> response = restTemplate.exchange(
                "/api/v1/achievements/stats",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<AchievementStatsDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        AchievementStatsDTO stats = response.getBody().getData();
        assertNotNull(stats);
        assertEquals(6L, stats.getTotalAchievements());
        assertEquals(2L, stats.getNationalCount());
        assertEquals(3L, stats.getProvincialCount());
        assertEquals(1L, stats.getSchoolCount());
    }

    @Test
    @DisplayName("集成测试：空数据库统计为零")
    void getAchievementStats_emptyDatabase_shouldReturnZeroStats() {
        ResponseEntity<ResponseMessage<AchievementStatsDTO>> response = restTemplate.exchange(
                "/api/v1/achievements/stats",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<AchievementStatsDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AchievementStatsDTO stats = response.getBody().getData();
        assertEquals(0L, stats.getTotalAchievements());
        assertEquals(0L, stats.getNationalCount());
        assertEquals(0L, stats.getProvincialCount());
        assertEquals(0L, stats.getSchoolCount());
    }

    @Test
    @DisplayName("集成测试：查询接口无需认证")
    void getAchievements_withoutAuth_shouldSucceed() {
        createAchievement("测试成就", AchievementType.COMPETITION, AwardLevel.NATIONAL, 2024);

        ResponseEntity<ResponseMessage<PageDTO<AchievementDTO>>> response = restTemplate.exchange(
                "/api/v1/achievements?page=0&size=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<PageDTO<AchievementDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getCode());
    }

    @Test
    @DisplayName("集成测试：统计接口无需认证")
    void getAchievementStats_withoutAuth_shouldSucceed() {
        createAchievement("测试成就", AchievementType.COMPETITION, AwardLevel.NATIONAL, 2024);

        ResponseEntity<ResponseMessage<AchievementStatsDTO>> response = restTemplate.exchange(
                "/api/v1/achievements/stats",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<AchievementStatsDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getCode());
    }
}
