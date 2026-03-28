package com.bluenet.web.api.controller.v1.member;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.member.DirectionLeaderDTO;
import com.bluenet.web.api.dto.member.MemberBriefDTO;
import com.bluenet.web.api.dto.member.MemberDetailDTO;
import com.bluenet.web.api.dto.experience.ExperienceDTO;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.Role;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.infrastructure.repository.mapper.CollegeMapper;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

@DisplayName("MemberController 集成测试")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class MemberControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private CollegeMapper collegeMapper;

    private Long memberRoleId;
    private Long directionAdminRoleId;
    private Long superAdminRoleId;
    private Long collegeId;

    @BeforeEach
    void setUpTestData() {
        createRoles();
        createCollege();
        createTestMembers();
    }

    private void createRoles() {
        Role memberRole = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>().eq(Role::getName, "MEMBER"));
        if (memberRole == null) {
            memberRole = new Role();
            memberRole.setName("MEMBER");
            roleMapper.insert(memberRole);
        }
        memberRoleId = memberRole.getId();

        Role directionAdminRole = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>().eq(Role::getName, "DIRECTION_ADMIN"));
        if (directionAdminRole == null) {
            directionAdminRole = new Role();
            directionAdminRole.setName("DIRECTION_ADMIN");
            roleMapper.insert(directionAdminRole);
        }
        directionAdminRoleId = directionAdminRole.getId();

        Role superAdminRole = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>().eq(Role::getName, "SUPER_ADMIN"));
        if (superAdminRole == null) {
            superAdminRole = new Role();
            superAdminRole.setName("SUPER_ADMIN");
            roleMapper.insert(superAdminRole);
        }
        superAdminRoleId = superAdminRole.getId();
    }

    private void createCollege() {
        College college = new College();
        college.setName("计算机学院");
        collegeMapper.insert(college);
        collegeId = college.getId();
    }

    private void createTestMembers() {
        List<User> members = new ArrayList<>();

        members.add(createUser("2021010001", "张三", "小张", Direction.COMPUTER_VISION, memberRoleId, 2021));
        members.add(createUser("2022010002", "李四", "小李", Direction.COMPUTER_VISION, memberRoleId, 2022));
        members.add(createUser("2023010003", "王五", "小王", Direction.EMBEDDED, memberRoleId, 2023));
        members.add(createUser("2020010004", "赵六", "小赵", Direction.STRUCTURAL_DESIGN, directionAdminRoleId, 2020));
        members.add(createUser("2019010005", "钱七", "小钱", Direction.COMPUTER_VISION, directionAdminRoleId, 2019));

        User disabledUser = createUser("2022010006", "禁用用户", "禁用", Direction.COMPUTER_VISION, memberRoleId, 2022);
        disabledUser.setDisable(true);
        members.add(disabledUser);

        for (User member : members) {
            userMapper.insert(member);
        }
    }

    private User createUser(String studentId, String username, String nickname, Direction direction, Long roleId,
            Integer enrollmentYear) {
        return User.builder()
                .studentId(studentId)
                .username(username)
                .nickname(nickname)
                .direction(direction)
                .roleId(roleId)
                .collegeId(collegeId)
                .major("计算机科学与技术")
                .gender(Gender.MALE)
                .job("开发")
                .disable(false)
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/members - 获取成员列表")
    class GetMemberListTests {

        @Test
        @DisplayName("正常情况：应返回成员列表，按入学年份降序排列")
        void getMemberList_shouldReturnSortedList() {
            ResponseEntity<ResponseMessage<PageDTO<MemberBriefDTO>>> response = restTemplate.exchange(
                    "/api/v1/members",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseMessage<PageDTO<MemberBriefDTO>>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getCode());

            PageDTO<MemberBriefDTO> page = response.getBody().getData();
            assertNotNull(page);
            assertTrue(page.getContent().size() >= 5);

            List<MemberBriefDTO> members = page.getContent();
            for (int i = 0; i < members.size() - 1; i++) {
                Integer currentYear = members.get(i).getEnrollmentYear();
                Integer nextYear = members.get(i + 1).getEnrollmentYear();
                if (currentYear != null && nextYear != null) {
                    assertTrue(
                            currentYear >= nextYear,
                            "成员应按入学年份降序排列: " + currentYear + " >= " + nextYear);
                }
            }
        }

        @Test
        @DisplayName("性别字段：成员简要信息应包含性别字段")
        void getMemberList_shouldIncludeGenderField() {
            ResponseEntity<ResponseMessage<PageDTO<MemberBriefDTO>>> response = restTemplate.exchange(
                    "/api/v1/members",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseMessage<PageDTO<MemberBriefDTO>>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            PageDTO<MemberBriefDTO> page = response.getBody().getData();

            for (MemberBriefDTO member : page.getContent()) {
                // 性别字段应存在（可以为 MALE、FEMALE、UNKNOWN 或 null）
                // 由于测试数据都设置了 Gender.MALE，验证非 null 成员的性别
                if (member.getUsername() != null) {
                    assertNotNull(member.getGender(), "成员 " + member.getUsername() + " 的性别字段不应为 null");
                    assertEquals(Gender.MALE, member.getGender(), "测试数据应返回性别为 MALE");
                }
            }
        }

        @Test
        @DisplayName("方向筛选：应返回指定方向的成员")
        void getMemberList_withDirection_shouldReturnFilteredMembers() {
            ResponseEntity<ResponseMessage<PageDTO<MemberBriefDTO>>> response = restTemplate.exchange(
                    "/api/v1/members?direction=computer_vision",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseMessage<PageDTO<MemberBriefDTO>>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            PageDTO<MemberBriefDTO> page = response.getBody().getData();
            assertNotNull(page);

            for (MemberBriefDTO member : page.getContent()) {
                assertEquals(Direction.COMPUTER_VISION, member.getDirection());
            }
        }

        @Test
        @DisplayName("分页参数：应正确分页")
        void getMemberList_withPaging_shouldReturnCorrectPage() {
            ResponseEntity<ResponseMessage<PageDTO<MemberBriefDTO>>> response = restTemplate.exchange(
                    "/api/v1/members?page=0&size=2",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseMessage<PageDTO<MemberBriefDTO>>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            PageDTO<MemberBriefDTO> page = response.getBody().getData();
            assertNotNull(page);
            assertEquals(2, page.getContent().size());
            assertTrue(page.getTotalElements() >= 5);
        }

        @Test
        @DisplayName("公开接口：无需认证即可访问")
        void getMemberList_publicEndpoint_shouldNotRequireAuth() {
            ResponseEntity<ResponseMessage<PageDTO<MemberBriefDTO>>> response = restTemplate.exchange(
                    "/api/v1/members",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseMessage<PageDTO<MemberBriefDTO>>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getCode());
        }

        @Test
        @DisplayName("禁用用户：不应出现在列表中")
        void getMemberList_shouldExcludeDisabledUsers() {
            ResponseEntity<ResponseMessage<PageDTO<MemberBriefDTO>>> response = restTemplate.exchange(
                    "/api/v1/members",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseMessage<PageDTO<MemberBriefDTO>>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            PageDTO<MemberBriefDTO> page = response.getBody().getData();

            for (MemberBriefDTO member : page.getContent()) {
                assertNotEquals("禁用用户", member.getUsername());
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/members/{id} - 获取成员详情")
    class GetMemberByIdTests {

        @Test
        @DisplayName("正常情况：应返回成员详情")
        void getMemberById_existingMember_shouldReturnDetail() {
            User testUser = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, "张三"));
            assertNotNull(testUser);

            ResponseEntity<ResponseMessage<MemberDetailDTO>> response = restTemplate.exchange(
                    "/api/v1/members/" + testUser.getId(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseMessage<MemberDetailDTO>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getCode());

            MemberDetailDTO detail = response.getBody().getData();
            assertNotNull(detail);
            assertEquals("张三", detail.getUsername());
            assertEquals("小张", detail.getNickname());
            assertEquals(Direction.COMPUTER_VISION, detail.getDirection());
        }

        @Test
        @DisplayName("成员不存在：应返回404")
        void getMemberById_nonExistingMember_shouldReturn404() {
            ResponseEntity<ResponseMessage<MemberDetailDTO>> response = restTemplate.exchange(
                    "/api/v1/members/999999",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseMessage<MemberDetailDTO>>() {
                    });

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(404, response.getBody().getCode());
        }

        @Test
        @DisplayName("公开接口：无需认证即可访问")
        void getMemberById_publicEndpoint_shouldNotRequireAuth() {
            User testUser = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, "张三"));

            ResponseEntity<ResponseMessage<MemberDetailDTO>> response = restTemplate.exchange(
                    "/api/v1/members/" + testUser.getId(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseMessage<MemberDetailDTO>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(200, response.getBody().getCode());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/members/direction-leaders - 获取方向负责人")
    class GetDirectionLeadersTests {

        @Test
        @DisplayName("正常情况：应返回方向负责人列表")
        void getDirectionLeaders_shouldReturnLeaders() {
            ResponseEntity<ResponseMessage<List<DirectionLeaderDTO>>> response = restTemplate.exchange(
                    "/api/v1/members/direction-leaders",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseMessage<List<DirectionLeaderDTO>>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getCode());

            List<DirectionLeaderDTO> leaders = response.getBody().getData();
            assertNotNull(leaders);
            assertEquals(3, leaders.size());

            for (DirectionLeaderDTO leader : leaders) {
                assertNotNull(leader.getDirection());
                assertNotNull(leader.getDirectionName());
            }
        }

        @Test
        @DisplayName("公开接口：无需认证即可访问")
        void getDirectionLeaders_publicEndpoint_shouldNotRequireAuth() {
            ResponseEntity<ResponseMessage<List<DirectionLeaderDTO>>> response = restTemplate.exchange(
                    "/api/v1/members/direction-leaders",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseMessage<List<DirectionLeaderDTO>>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(200, response.getBody().getCode());
        }

        @Test
        @DisplayName("响应格式：应包含所有方向")
        void getDirectionLeaders_shouldIncludeAllDirections() {
            ResponseEntity<ResponseMessage<List<DirectionLeaderDTO>>> response = restTemplate.exchange(
                    "/api/v1/members/direction-leaders",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseMessage<List<DirectionLeaderDTO>>>() {
                    });

            List<DirectionLeaderDTO> leaders = response.getBody().getData();
            List<Direction> directions = leaders.stream()
                    .map(DirectionLeaderDTO::getDirection)
                    .toList();

            assertTrue(directions.contains(Direction.COMPUTER_VISION));
            assertTrue(directions.contains(Direction.STRUCTURAL_DESIGN));
            assertTrue(directions.contains(Direction.EMBEDDED));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/members/{memberId}/experiences - 获取成员经历")
    class GetMemberExperiencesTests {

        @Test
        @DisplayName("公开接口：无需认证即可访问")
        void getMemberExperiences_publicEndpoint_shouldNotRequireAuth() {
            User testUser = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, "张三"));
            assertNotNull(testUser);

            ResponseEntity<ResponseMessage<List<ExperienceDTO>>> response = restTemplate.exchange(
                    "/api/v1/members/" + testUser.getId() + "/experiences",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseMessage<List<ExperienceDTO>>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(200, response.getBody().getCode());
        }

        @Test
        @DisplayName("成员不存在：应返回404")
        void getMemberExperiences_nonExistingMember_shouldReturn404() {
            ResponseEntity<ResponseMessage<List<ExperienceDTO>>> response = restTemplate.exchange(
                    "/api/v1/members/999999/experiences",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseMessage<List<ExperienceDTO>>>() {
                    });

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("成员无经历：应返回空列表")
        void getMemberExperiences_memberNoExperiences_shouldReturnEmptyList() {
            User testUser = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, "张三"));
            assertNotNull(testUser);

            ResponseEntity<ResponseMessage<List<ExperienceDTO>>> response = restTemplate.exchange(
                    "/api/v1/members/" + testUser.getId() + "/experiences",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseMessage<List<ExperienceDTO>>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<ExperienceDTO> experiences = response.getBody().getData();
            assertNotNull(experiences);
            assertTrue(experiences.isEmpty());
        }

        @Test
        @DisplayName("按类型筛选：应只返回指定类型的经历")
        void getMemberExperiences_withTypeFilter_shouldReturnFilteredExperiences() {
            User testUser = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, "张三"));
            assertNotNull(testUser);

            ResponseEntity<ResponseMessage<List<ExperienceDTO>>> response = restTemplate.exchange(
                    "/api/v1/members/" + testUser.getId() + "/experiences?type=project",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseMessage<List<ExperienceDTO>>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<ExperienceDTO> experiences = response.getBody().getData();
            assertNotNull(experiences);
            // 如果有经历，验证类型都是project
            experiences.forEach(exp -> assertEquals("project", exp.getType()));
        }

        @Test
        @DisplayName("非团队成员：应返回空列表")
        void getMemberExperiences_nonTeamMember_shouldReturnEmptyList() {
            // 创建CANDIDATE角色用户（非团队成员）
            Role candidateRole = roleMapper.selectOne(
                    new LambdaQueryWrapper<Role>().eq(Role::getName, "CANDIDATE"));
            if (candidateRole == null) {
                candidateRole = new Role();
                candidateRole.setName("CANDIDATE");
                roleMapper.insert(candidateRole);
            }

            User candidateUser = User.builder()
                    .studentId("2024001999")
                    .username("考生用户")
                    .nickname("考生")
                    .direction(Direction.COMPUTER_VISION)
                    .roleId(candidateRole.getId())
                    .collegeId(collegeId)
                    .major("计算机科学与技术")
                    .gender(Gender.MALE)
                    .disable(false)
                    .build();
            userMapper.insert(candidateUser);

            ResponseEntity<ResponseMessage<List<ExperienceDTO>>> response = restTemplate.exchange(
                    "/api/v1/members/" + candidateUser.getId() + "/experiences",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseMessage<List<ExperienceDTO>>>() {
                    });

            // 非团队成员应返回空列表
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<ExperienceDTO> experiences = response.getBody().getData();
            assertNotNull(experiences);
            assertTrue(experiences.isEmpty(), "非团队成员应返回空经历列表");
        }

        @Test
        @DisplayName("无效类型参数：应返回400或忽略")
        void getMemberExperiences_invalidType_shouldHandleGracefully() {
            User testUser = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, "张三"));
            assertNotNull(testUser);

            ResponseEntity<ResponseMessage<List<ExperienceDTO>>> response = restTemplate.exchange(
                    "/api/v1/members/" + testUser.getId() + "/experiences?type=invalid_type",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseMessage<List<ExperienceDTO>>>() {
                    });

            // 应该返回400错误或忽略参数返回所有经历
            assertTrue(
                    response.getStatusCode() == HttpStatus.BAD_REQUEST
                            || response.getStatusCode() == HttpStatus.OK,
                    "无效类型参数应返回400或被忽略");
        }
    }
}
