package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.query.member.GetMemberListQuery;
import com.bluenet.web.application.result.member.MemberResult;
import com.bluenet.web.application.result.user.UserExperienceResult;
import com.bluenet.web.application.service.MemberAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.entity.UserExperience;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.repository.MemberRepository;
import com.bluenet.web.domain.repository.UserExperienceRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MemberAppServiceImpl 集成测试。
 *
 * <p>
 * 验证成员应用服务的列表查询、详情查询、方向负责人查询及成员经历查询逻辑。
 * </p>
 */
@DisplayName("MemberAppServiceImpl 集成测试")
class MemberAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MemberAppService memberAppService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserExperienceRepository userExperienceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void cleanupSecurityContext() {
        UserCTX.clear();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getMemberList: 应返回成员分页列表")
    void getMemberList_shouldReturnMemberPage() {
        createMember("2024003001", Direction.COMPUTER_VISION);
        createMember("2024003002", Direction.STRUCTURAL_DESIGN);

        Page<MemberResult> result = memberAppService.getMemberList(
                new GetMemberListQuery(null, 0, 20));

        assertThat(result).isNotNull();
        assertThat(result.getContent())
                .extracting(MemberResult::studentId)
                .contains("2024003001", "2024003002");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getMemberList: 应按方向过滤成员")
    void getMemberList_withDirectionFilter_shouldReturnFilteredPage() {
        createMember("2024003003", Direction.COMPUTER_VISION);
        createMember("2024003004", Direction.STRUCTURAL_DESIGN);

        Page<MemberResult> result = memberAppService.getMemberList(
                new GetMemberListQuery(Direction.COMPUTER_VISION, 0, 20));

        assertThat(result.getContent())
                .extracting(MemberResult::studentId)
                .contains("2024003003")
                .doesNotContain("2024003004");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getMemberList: pageSize 应被限制在最大值 100")
    void getMemberList_withOversizedPage_shouldClampPageSize() {
        Page<MemberResult> result = memberAppService.getMemberList(
                new GetMemberListQuery(null, 0, 200));

        assertThat(result.getSize()).isEqualTo(100);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getMemberById: 应返回成员详情")
    void getMemberById_shouldReturnMemberDetail() {
        User user = createMember("2024003005", Direction.COMPUTER_VISION);

        MemberResult result = memberAppService.getMemberById(user.getId());

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(user.getId());
        assertThat(result.studentId()).isEqualTo("2024003005");
        assertThat(result.direction()).isEqualTo(Direction.COMPUTER_VISION);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getMemberById: 成员不存在应抛 DataNotFound")
    void getMemberById_notFound_shouldThrowDataNotFound() {
        assertThatThrownBy(() -> memberAppService.getMemberById(99999L))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("成员不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getDirectionLeaders: 应返回方向负责人列表")
    void getDirectionLeaders_shouldReturnDirectionLeaders() {
        User leader = UserFixture.directionAdmin("2024003006", Direction.COMPUTER_VISION)
                .save(userRepository, passwordEncoder);

        List<MemberResult> result = memberAppService.getDirectionLeaders();

        assertThat(result).isNotEmpty();
        assertThat(result)
                .extracting(MemberResult::id)
                .contains(leader.getId());
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getMemberExperiences: 应返回团队成员的经历列表")
    void getMemberExperiences_teamMember_shouldReturnExperiences() {
        User user = createMember("2024003007", Direction.COMPUTER_VISION);
        UserExperience experience = UserExperience.create(
                user.getId(),
                ExperienceType.PROJECT,
                "项目经历",
                "项目内容",
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 6, 1, 0, 0));
        userExperienceRepository.save(experience);

        List<UserExperienceResult> result = memberAppService.getMemberExperiences(user.getId(), null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("项目经历");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getMemberExperiences: 应按经历类型过滤")
    void getMemberExperiences_withTypeFilter_shouldReturnFilteredExperiences() {
        User user = createMember("2024003008", Direction.COMPUTER_VISION);
        UserExperience project = UserExperience.create(
                user.getId(),
                ExperienceType.PROJECT,
                "项目经历",
                "项目内容",
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 6, 1, 0, 0));
        UserExperience internship = UserExperience.create(
                user.getId(),
                ExperienceType.INTERNSHIP,
                "实习经历",
                "实习内容",
                LocalDateTime.of(2024, 2, 1, 0, 0),
                LocalDateTime.of(2024, 7, 1, 0, 0));
        userExperienceRepository.save(project);
        userExperienceRepository.save(internship);

        List<UserExperienceResult> result = memberAppService.getMemberExperiences(
                user.getId(),
                ExperienceType.PROJECT.name());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("项目经历");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getMemberExperiences: 非团队成员应返回空列表")
    void getMemberExperiences_nonTeamMember_shouldReturnEmptyList() {
        User candidate = UserFixture.candidate("2024003009")
                .save(userRepository, passwordEncoder);

        List<UserExperienceResult> result = memberAppService.getMemberExperiences(candidate.getId(), null);

        assertThat(result).isEmpty();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getMemberExperiences: 无效经历类型应抛 BadRequest")
    void getMemberExperiences_invalidType_shouldThrowBadRequest() {
        User user = createMember("2024003010", Direction.COMPUTER_VISION);

        assertThatThrownBy(() -> memberAppService.getMemberExperiences(user.getId(), "INVALID"))
                .isInstanceOf(BadRequest.class)
                .hasMessageContaining("无效的经历类型");
    }

    private User createMember(String studentId, Direction direction) {
        return UserFixture.member(studentId)
                .withDirection(direction)
                .save(userRepository, passwordEncoder);
    }
}
