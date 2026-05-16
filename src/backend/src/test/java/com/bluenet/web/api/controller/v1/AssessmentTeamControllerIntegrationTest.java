package com.bluenet.web.api.controller.v1;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.assessment_team.*;
import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.Role;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentTimeDO;
import com.bluenet.web.infrastructure.repository.dataobject.UserDO;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentTimeMapper;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;
import com.bluenet.web.testsupport.RepositoryTestObjects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AssessmentTeamController 集成测试。
 * <p>
 * 测试考核队伍相关接口的完整链路。
 * </p>
 */
@DisplayName("AssessmentTeamController 集成测试")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class AssessmentTeamControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private AssessmentTimeMapper assessmentTimeMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private com.bluenet.web.infrastructure.security.scanner.PermissionScanner permissionScanner;

    private static final String LEADER_STUDENT_ID = "teamleader001";
    private static final String MEMBER_STUDENT_ID = "teammember001";
    private static final String TEST_PASSWORD = "testPassword123";

    private Long leaderUserId;
    private Long memberUserId;
    private Long assessmentTimeId;

    @BeforeEach
    void setUpTestData() {
        // 查找 MEMBER 角色
        Role memberRole = RepositoryTestObjects.toDomain(roleMapper.selectByName("MEMBER"), Role.class);
        assertNotNull(memberRole, "MEMBER 角色应存在");

        // 创建队长用户
        User leaderUser = User.reconstruct(
                null,
                LEADER_STUDENT_ID,
                "leader@test.com",
                memberRole.getId(),
                passwordEncoder.encode(TEST_PASSWORD),
                "队长用户",
                null,
                null,
                null,
                null,
                Direction.COMPUTER_VISION,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null);
        RepositoryTestObjects.insert(userMapper, leaderUser, UserDO.class);
        leaderUserId = leaderUser.getId();

        // 创建队员用户
        User memberUser = User.reconstruct(
                null,
                MEMBER_STUDENT_ID,
                "member@test.com",
                memberRole.getId(),
                passwordEncoder.encode(TEST_PASSWORD),
                "队员用户",
                null,
                null,
                null,
                null,
                Direction.COMPUTER_VISION,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null);
        RepositoryTestObjects.insert(userMapper, memberUser, UserDO.class);
        memberUserId = memberUser.getId();

        // 创建允许组队的考核时间
        AssessmentTime assessmentTime = AssessmentTime.create(
                Direction.COMPUTER_VISION,
                1,
                2026,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(7),
                false,
                null,
                true);
        RepositoryTestObjects.insert(assessmentTimeMapper, assessmentTime, AssessmentTimeDO.class);
        assessmentTimeId = assessmentTime.getId();
    }

    // ========== 辅助方法 ==========

    private HttpHeaders loginAndGetCookies(String studentId, String password) {
        StudentIdLoginRequestDTO loginRequest = new StudentIdLoginRequestDTO();
        loginRequest.setStudentId(studentId);
        loginRequest.setPassword(password);

        ResponseEntity<ResponseMessage<UserAuthResponseDTO>> loginResponse = restTemplate.exchange(
                "/api/v1/auth/login/student-id",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<ResponseMessage<UserAuthResponseDTO>>() {
                });

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        List<String> setCookies = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookies, "登录响应应包含 Set-Cookie");

        StringBuilder cookieBuilder = new StringBuilder();
        for (String setCookie : setCookies) {
            int semicolonIndex = setCookie.indexOf(';');
            String nameValue = semicolonIndex > 0 ? setCookie.substring(0, semicolonIndex) : setCookie;
            if (cookieBuilder.length() > 0) {
                cookieBuilder.append("; ");
            }
            cookieBuilder.append(nameValue);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, cookieBuilder.toString());
        String csrfToken = loginResponse.getBody().getData().getCsrfToken();
        headers.set("X-CSRF-Token-Stored", csrfToken);
        return headers;
    }

    private String getStoredCsrfToken(HttpHeaders headers) {
        return headers.getFirst("X-CSRF-Token-Stored");
    }

    private HttpHeaders createHeadersWithCsrf(HttpHeaders cookies) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, cookies.getFirst(HttpHeaders.COOKIE));
        headers.set("X-CSRF-Token", getStoredCsrfToken(cookies));
        return headers;
    }

    // ========== 测试用例 ==========

    @Test
    @DisplayName("创建队伍：已登录用户应成功创建")
    void createTeam_authenticated_shouldSucceed() {
        HttpHeaders cookies = loginAndGetCookies(LEADER_STUDENT_ID, TEST_PASSWORD);
        HttpHeaders headers = createHeadersWithCsrf(cookies);

        CreateTeamRequestDTO request = new CreateTeamRequestDTO();
        request.setAssessmentTimeId(assessmentTimeId);
        request.setName("蓝网先锋队");

        ResponseEntity<ResponseMessage<AssessmentTeamDTO>> response = restTemplate.exchange(
                "/api/v1/assessment-teams",
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                new ParameterizedTypeReference<ResponseMessage<AssessmentTeamDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseMessage<AssessmentTeamDTO> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getCode());

        AssessmentTeamDTO team = body.getData();
        assertNotNull(team);
        assertEquals("蓝网先锋队", team.getName());
        assertEquals(assessmentTimeId, team.getAssessmentTimeId());
        assertNotNull(team.getInviteCode());
        assertEquals(1, team.getMembers().size());
    }

    @Test
    @DisplayName("创建队伍：未登录应返回401")
    void createTeam_unauthenticated_shouldReturn401() {
        CreateTeamRequestDTO request = new CreateTeamRequestDTO();
        request.setAssessmentTimeId(assessmentTimeId);
        request.setName("未登录队伍");

        ResponseEntity<ResponseMessage> response = restTemplate.exchange(
                "/api/v1/assessment-teams",
                HttpMethod.POST,
                new HttpEntity<>(request),
                ResponseMessage.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("创建队伍：重复创建应返回400")
    void createTeam_duplicate_shouldReturn400() {
        HttpHeaders cookies = loginAndGetCookies(LEADER_STUDENT_ID, TEST_PASSWORD);
        HttpHeaders headers = createHeadersWithCsrf(cookies);

        // 第一次创建
        CreateTeamRequestDTO request = new CreateTeamRequestDTO();
        request.setAssessmentTimeId(assessmentTimeId);
        request.setName("第一次创建");

        ResponseEntity<ResponseMessage<AssessmentTeamDTO>> firstResponse = restTemplate.exchange(
                "/api/v1/assessment-teams",
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                new ParameterizedTypeReference<ResponseMessage<AssessmentTeamDTO>>() {
                });
        assertEquals(HttpStatus.OK, firstResponse.getStatusCode());

        // 第二次创建同一考核
        request.setName("第二次创建");
        ResponseEntity<ResponseMessage> secondResponse = restTemplate.exchange(
                "/api/v1/assessment-teams",
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                ResponseMessage.class);

        assertEquals(HttpStatus.BAD_REQUEST, secondResponse.getStatusCode());
    }

    @Test
    @DisplayName("查询我的队伍：已加入应返回队伍信息")
    void getMyTeam_hasTeam_shouldReturnTeam() {
        HttpHeaders cookies = loginAndGetCookies(LEADER_STUDENT_ID, TEST_PASSWORD);
        HttpHeaders headers = createHeadersWithCsrf(cookies);

        // 先创建队伍
        CreateTeamRequestDTO createRequest = new CreateTeamRequestDTO();
        createRequest.setAssessmentTimeId(assessmentTimeId);
        createRequest.setName("查询测试队");

        ResponseEntity<ResponseMessage<AssessmentTeamDTO>> createResponse = restTemplate.exchange(
                "/api/v1/assessment-teams",
                HttpMethod.POST,
                new HttpEntity<>(createRequest, headers),
                new ParameterizedTypeReference<ResponseMessage<AssessmentTeamDTO>>() {
                });
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());

        // 查询我的队伍
        ResponseEntity<ResponseMessage<AssessmentTeamDTO>> response = restTemplate.exchange(
                "/api/v1/assessment-teams/my-team?assessmentTimeId=" + assessmentTimeId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<ResponseMessage<AssessmentTeamDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseMessage<AssessmentTeamDTO> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getCode());
        assertEquals("查询测试队", body.getData().getName());
    }

    @Test
    @DisplayName("查询我的队伍：未加入应返回404")
    void getMyTeam_noTeam_shouldReturn404() {
        HttpHeaders cookies = loginAndGetCookies(MEMBER_STUDENT_ID, TEST_PASSWORD);
        HttpHeaders headers = createHeadersWithCsrf(cookies);

        ResponseEntity<ResponseMessage> response = restTemplate.exchange(
                "/api/v1/assessment-teams/my-team?assessmentTimeId=" + assessmentTimeId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ResponseMessage.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("预览队伍：应返回队伍预览信息")
    void previewTeam_validInviteCode_shouldReturnPreview() {
        HttpHeaders leaderCookies = loginAndGetCookies(LEADER_STUDENT_ID, TEST_PASSWORD);
        HttpHeaders leaderHeaders = createHeadersWithCsrf(leaderCookies);

        // 创建队伍
        CreateTeamRequestDTO createRequest = new CreateTeamRequestDTO();
        createRequest.setAssessmentTimeId(assessmentTimeId);
        createRequest.setName("预览测试队");

        ResponseEntity<ResponseMessage<AssessmentTeamDTO>> createResponse = restTemplate.exchange(
                "/api/v1/assessment-teams",
                HttpMethod.POST,
                new HttpEntity<>(createRequest, leaderHeaders),
                new ParameterizedTypeReference<ResponseMessage<AssessmentTeamDTO>>() {
                });
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        String inviteCode = createResponse.getBody().getData().getInviteCode();

        // 预览队伍
        PreviewTeamRequestDTO previewRequest = new PreviewTeamRequestDTO();
        previewRequest.setInviteCode(inviteCode);

        ResponseEntity<ResponseMessage<TeamPreviewResponseDTO>> response = restTemplate.exchange(
                "/api/v1/assessment-teams/preview",
                HttpMethod.POST,
                new HttpEntity<>(previewRequest, leaderHeaders),
                new ParameterizedTypeReference<ResponseMessage<TeamPreviewResponseDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseMessage<TeamPreviewResponseDTO> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getCode());
        assertEquals("预览测试队", body.getData().getName());
        assertEquals(1, body.getData().getMemberCount());
    }

    @Test
    @DisplayName("加入队伍：队员应成功加入")
    void joinTeam_valid_shouldSucceed() {
        HttpHeaders leaderCookies = loginAndGetCookies(LEADER_STUDENT_ID, TEST_PASSWORD);
        HttpHeaders leaderHeaders = createHeadersWithCsrf(leaderCookies);

        // 队长创建队伍
        CreateTeamRequestDTO createRequest = new CreateTeamRequestDTO();
        createRequest.setAssessmentTimeId(assessmentTimeId);
        createRequest.setName("加入测试队");

        ResponseEntity<ResponseMessage<AssessmentTeamDTO>> createResponse = restTemplate.exchange(
                "/api/v1/assessment-teams",
                HttpMethod.POST,
                new HttpEntity<>(createRequest, leaderHeaders),
                new ParameterizedTypeReference<ResponseMessage<AssessmentTeamDTO>>() {
                });
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        String inviteCode = createResponse.getBody().getData().getInviteCode();

        // 队员加入队伍
        HttpHeaders memberCookies = loginAndGetCookies(MEMBER_STUDENT_ID, TEST_PASSWORD);
        HttpHeaders memberHeaders = createHeadersWithCsrf(memberCookies);

        JoinTeamRequestDTO joinRequest = new JoinTeamRequestDTO();
        joinRequest.setInviteCode(inviteCode);

        ResponseEntity<ResponseMessage<AssessmentTeamDTO>> response = restTemplate.exchange(
                "/api/v1/assessment-teams/join",
                HttpMethod.POST,
                new HttpEntity<>(joinRequest, memberHeaders),
                new ParameterizedTypeReference<ResponseMessage<AssessmentTeamDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseMessage<AssessmentTeamDTO> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getCode());
        assertEquals(2, body.getData().getMembers().size());
    }

    @Test
    @DisplayName("离开队伍：队员应成功离开")
    void leaveTeam_member_shouldSucceed() {
        HttpHeaders leaderCookies = loginAndGetCookies(LEADER_STUDENT_ID, TEST_PASSWORD);
        HttpHeaders leaderHeaders = createHeadersWithCsrf(leaderCookies);

        // 队长创建队伍
        CreateTeamRequestDTO createRequest = new CreateTeamRequestDTO();
        createRequest.setAssessmentTimeId(assessmentTimeId);
        createRequest.setName("离开测试队");

        ResponseEntity<ResponseMessage<AssessmentTeamDTO>> createResponse = restTemplate.exchange(
                "/api/v1/assessment-teams",
                HttpMethod.POST,
                new HttpEntity<>(createRequest, leaderHeaders),
                new ParameterizedTypeReference<ResponseMessage<AssessmentTeamDTO>>() {
                });
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        Long teamId = createResponse.getBody().getData().getId();
        String inviteCode = createResponse.getBody().getData().getInviteCode();

        // 队员加入
        HttpHeaders memberCookies = loginAndGetCookies(MEMBER_STUDENT_ID, TEST_PASSWORD);
        HttpHeaders memberHeaders = createHeadersWithCsrf(memberCookies);

        JoinTeamRequestDTO joinRequest = new JoinTeamRequestDTO();
        joinRequest.setInviteCode(inviteCode);

        ResponseEntity<ResponseMessage<AssessmentTeamDTO>> joinResponse = restTemplate.exchange(
                "/api/v1/assessment-teams/join",
                HttpMethod.POST,
                new HttpEntity<>(joinRequest, memberHeaders),
                new ParameterizedTypeReference<ResponseMessage<AssessmentTeamDTO>>() {
                });
        assertEquals(HttpStatus.OK, joinResponse.getStatusCode());

        // 队员离开
        LeaveTeamRequestDTO leaveRequest = new LeaveTeamRequestDTO();
        leaveRequest.setTeamId(teamId);

        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/assessment-teams/leave",
                HttpMethod.POST,
                new HttpEntity<>(leaveRequest, memberHeaders),
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseMessage<Void> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getCode());
    }

    @Test
    @DisplayName("转让队长：队长应成功转让")
    void transferLeader_leader_shouldSucceed() {
        HttpHeaders leaderCookies = loginAndGetCookies(LEADER_STUDENT_ID, TEST_PASSWORD);
        HttpHeaders leaderHeaders = createHeadersWithCsrf(leaderCookies);

        // 队长创建队伍
        CreateTeamRequestDTO createRequest = new CreateTeamRequestDTO();
        createRequest.setAssessmentTimeId(assessmentTimeId);
        createRequest.setName("转让测试队");

        ResponseEntity<ResponseMessage<AssessmentTeamDTO>> createResponse = restTemplate.exchange(
                "/api/v1/assessment-teams",
                HttpMethod.POST,
                new HttpEntity<>(createRequest, leaderHeaders),
                new ParameterizedTypeReference<ResponseMessage<AssessmentTeamDTO>>() {
                });
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        Long teamId = createResponse.getBody().getData().getId();
        String inviteCode = createResponse.getBody().getData().getInviteCode();

        // 队员加入
        HttpHeaders memberCookies = loginAndGetCookies(MEMBER_STUDENT_ID, TEST_PASSWORD);
        HttpHeaders memberHeaders = createHeadersWithCsrf(memberCookies);

        JoinTeamRequestDTO joinRequest = new JoinTeamRequestDTO();
        joinRequest.setInviteCode(inviteCode);

        ResponseEntity<ResponseMessage<AssessmentTeamDTO>> joinResponse = restTemplate.exchange(
                "/api/v1/assessment-teams/join",
                HttpMethod.POST,
                new HttpEntity<>(joinRequest, memberHeaders),
                new ParameterizedTypeReference<ResponseMessage<AssessmentTeamDTO>>() {
                });
        assertEquals(HttpStatus.OK, joinResponse.getStatusCode());

        // 队长转让
        TransferLeaderRequestDTO transferRequest = new TransferLeaderRequestDTO();
        transferRequest.setTeamId(teamId);
        transferRequest.setNewLeaderId(memberUserId);

        ResponseEntity<ResponseMessage<AssessmentTeamDTO>> response = restTemplate.exchange(
                "/api/v1/assessment-teams/transfer",
                HttpMethod.POST,
                new HttpEntity<>(transferRequest, leaderHeaders),
                new ParameterizedTypeReference<ResponseMessage<AssessmentTeamDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseMessage<AssessmentTeamDTO> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getCode());
        assertEquals(memberUserId, body.getData().getLeaderId());
    }

    @Test
    @DisplayName("解散队伍：队长应成功解散")
    void disbandTeam_leader_shouldSucceed() {
        HttpHeaders leaderCookies = loginAndGetCookies(LEADER_STUDENT_ID, TEST_PASSWORD);
        HttpHeaders leaderHeaders = createHeadersWithCsrf(leaderCookies);

        // 队长创建队伍
        CreateTeamRequestDTO createRequest = new CreateTeamRequestDTO();
        createRequest.setAssessmentTimeId(assessmentTimeId);
        createRequest.setName("解散测试队");

        ResponseEntity<ResponseMessage<AssessmentTeamDTO>> createResponse = restTemplate.exchange(
                "/api/v1/assessment-teams",
                HttpMethod.POST,
                new HttpEntity<>(createRequest, leaderHeaders),
                new ParameterizedTypeReference<ResponseMessage<AssessmentTeamDTO>>() {
                });
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        Long teamId = createResponse.getBody().getData().getId();

        // 队长解散
        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/assessment-teams/" + teamId,
                HttpMethod.DELETE,
                new HttpEntity<>(leaderHeaders),
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseMessage<Void> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getCode());

        // 解散后查询应返回404
        ResponseEntity<ResponseMessage> getResponse = restTemplate.exchange(
                "/api/v1/assessment-teams/my-team?assessmentTimeId=" + assessmentTimeId,
                HttpMethod.GET,
                new HttpEntity<>(leaderHeaders),
                ResponseMessage.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }
}
