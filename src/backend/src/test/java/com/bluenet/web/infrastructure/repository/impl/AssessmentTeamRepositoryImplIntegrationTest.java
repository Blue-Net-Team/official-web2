package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.model.entity.AssessmentTeamMember;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.repository.AssessmentTeamRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentTeamDO;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentTeamMemberDO;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentTeamMapper;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentTeamMemberMapper;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import com.bluenet.web.testsupport.fixture.CollegeFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AssessmentTeamRepositoryImpl 集成测试。
 * <p>
 * 验证考核队伍仓储行为：save 插入/更新、自动插入队长成员、多种条件查询、成员管理、级联删除。
 * </p>
 */
@DisplayName("AssessmentTeamRepositoryImpl 集成测试")
class AssessmentTeamRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AssessmentTeamRepository assessmentTeamRepository;

    @Autowired
    private AssessmentTeamMapper assessmentTeamMapper;

    @Autowired
    private AssessmentTeamMemberMapper assessmentTeamMemberMapper;

    @Autowired
    private AssessmentTimeRepository assessmentTimeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CollegeRepository collegeRepository;

    private AssessmentFixtureState prepareFixture(String leaderStudentId, Direction direction, Integer epoch,
            Integer grade) {
        College college = CollegeFixture.saveDefaultCollege(collegeRepository);
        User leader = UserFixture.candidate(leaderStudentId)
                .withCollege(college)
                .withDirection(direction)
                .save(userRepository, passwordEncoder);
        AssessmentTime assessmentTime = AssessmentFixture.timeBuilder()
                .direction(direction)
                .epoch(epoch)
                .grade(grade)
                .save(assessmentTimeRepository);
        return new AssessmentFixtureState(leader, assessmentTime, college, direction);
    }

    private User createMember(String studentId, College college, Direction direction) {
        return UserFixture.candidate(studentId)
                .withCollege(college)
                .withDirection(direction)
                .save(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("save: 新队伍应插入并回写ID，同时自动插入队长为成员")
    void save_newTeam_shouldInsertAndAddLeaderAsMember() {
        AssessmentFixtureState state = prepareFixture("2024001001", Direction.COMPUTER_VISION, 1, 2024);

        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTime(state.assessmentTime)
                .leader(state.leader)
                .name("测试队伍A")
                .inviteCode("INVITE-A")
                .build();
        assessmentTeamRepository.save(team);

        assertNotNull(team.getId());
        AssessmentTeamDO teamDO = assessmentTeamMapper.selectById(team.getId());
        assertNotNull(teamDO);
        assertEquals(state.assessmentTime.getId(), teamDO.getAssessmentTimeId());
        assertEquals(state.leader.getId(), teamDO.getLeaderId());
        assertEquals("测试队伍A", teamDO.getName());
        assertEquals("INVITE-A", teamDO.getInviteCode());

        List<AssessmentTeamMemberDO> members = assessmentTeamMemberMapper.selectByTeamId(team.getId());
        assertEquals(1, members.size());
        assertEquals(state.leader.getId(), members.get(0).getUserId());
    }

    @Test
    @DisplayName("save: 已有队伍应更新名称和状态")
    void save_existingTeam_shouldUpdateNameAndStatus() {
        AssessmentFixtureState state = prepareFixture("2024001002", Direction.STRUCTURAL_DESIGN, 1, 2024);
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTime(state.assessmentTime)
                .leader(state.leader)
                .name("原始名称")
                .build();
        assessmentTeamRepository.save(team);

        team.setName("更新后名称");
        team.disband();
        assessmentTeamRepository.save(team);

        AssessmentTeamDO updated = assessmentTeamMapper.selectById(team.getId());
        assertEquals("更新后名称", updated.getName());
        assertEquals(AssessmentTeam.TeamStatus.DISBANDED.name(), updated.getStatus());
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        AssessmentFixtureState state = prepareFixture("2024001003", Direction.EMBEDDED, 1, 2024);
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTime(state.assessmentTime)
                .leader(state.leader)
                .build();
        assessmentTeamRepository.save(team);

        Optional<AssessmentTeam> found = assessmentTeamRepository.findById(team.getId());
        assertTrue(found.isPresent());
        assertEquals(team.getName(), found.get().getName());
        assertEquals(state.leader.getId(), found.get().getLeaderId());

        assertTrue(assessmentTeamRepository.findById(-1L).isEmpty());
    }

    @Test
    @DisplayName("findByInviteCode: 存在返回实体，不存在返回空")
    void findByInviteCode_shouldReturnOptional() {
        AssessmentFixtureState state = prepareFixture("2024001004", Direction.COMPUTER_VISION, 2, 2024);
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTime(state.assessmentTime)
                .leader(state.leader)
                .inviteCode("INVITE-004")
                .build();
        assessmentTeamRepository.save(team);

        Optional<AssessmentTeam> found = assessmentTeamRepository.findByInviteCode("INVITE-004");
        assertTrue(found.isPresent());
        assertEquals(team.getId(), found.get().getId());

        assertTrue(assessmentTeamRepository.findByInviteCode("NOT-EXIST-CODE").isEmpty());
    }

    @Test
    @DisplayName("findByAssessmentTimeIdAndUserId: 应返回用户所在队伍")
    void findByAssessmentTimeIdAndUserId_shouldReturnTeam() {
        AssessmentFixtureState state = prepareFixture("2024001005", Direction.STRUCTURAL_DESIGN, 2, 2024);
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTime(state.assessmentTime)
                .leader(state.leader)
                .build();
        assessmentTeamRepository.save(team);

        Optional<AssessmentTeam> found = assessmentTeamRepository
                .findByAssessmentTimeIdAndUserId(state.assessmentTime.getId(), state.leader.getId());
        assertTrue(found.isPresent());
        assertEquals(team.getId(), found.get().getId());
    }

    @Test
    @DisplayName("existsByAssessmentTimeIdAndUserId: 应正确判断用户是否在队伍")
    void existsByAssessmentTimeIdAndUserId_shouldReturnBoolean() {
        AssessmentFixtureState state = prepareFixture("2024001006", Direction.EMBEDDED, 2, 2024);
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTime(state.assessmentTime)
                .leader(state.leader)
                .build();
        assessmentTeamRepository.save(team);

        assertTrue(
                assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(
                        state.assessmentTime.getId(),
                        state.leader.getId()));
        assertFalse(
                assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(
                        state.assessmentTime.getId(),
                        -1L));
    }

    @Test
    @DisplayName("deleteById: 应删除队伍及其成员")
    void deleteById_shouldRemoveTeamAndMembers() {
        AssessmentFixtureState state = prepareFixture("2024001007", Direction.COMPUTER_VISION, 3, 2024);
        User member = createMember("2024001008", state.college, state.direction);
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTime(state.assessmentTime)
                .leader(state.leader)
                .build();
        assessmentTeamRepository.save(team);
        assessmentTeamRepository.addMember(team.getId(), member.getId());

        assessmentTeamRepository.deleteById(team.getId());

        assertNull(assessmentTeamMapper.selectById(team.getId()));
        List<AssessmentTeamMemberDO> members = assessmentTeamMemberMapper.selectByTeamId(team.getId());
        assertTrue(members.isEmpty());
    }

    @Test
    @DisplayName("addMember / removeMember: 应正确管理成员")
    void addMemberAndRemoveMember_shouldManageMembers() {
        AssessmentFixtureState state = prepareFixture("2024001009", Direction.STRUCTURAL_DESIGN, 3, 2024);
        User member = createMember("2024001010", state.college, state.direction);
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTime(state.assessmentTime)
                .leader(state.leader)
                .build();
        assessmentTeamRepository.save(team);

        assessmentTeamRepository.addMember(team.getId(), member.getId());

        List<AssessmentTeamMemberDO> afterAdd = assessmentTeamMemberMapper.selectByTeamId(team.getId());
        assertEquals(2, afterAdd.size());
        assertTrue(afterAdd.stream().anyMatch(m -> m.getUserId().equals(member.getId())));

        assessmentTeamRepository.removeMember(team.getId(), member.getId());

        List<AssessmentTeamMemberDO> afterRemove = assessmentTeamMemberMapper.selectByTeamId(team.getId());
        assertEquals(1, afterRemove.size());
        assertFalse(afterRemove.stream().anyMatch(m -> m.getUserId().equals(member.getId())));
    }

    @Test
    @DisplayName("findMembersByTeamId: 应返回队伍成员列表")
    void findMembersByTeamId_shouldReturnMembers() {
        AssessmentFixtureState state = prepareFixture("2024001011", Direction.EMBEDDED, 3, 2024);
        User member = createMember("2024001012", state.college, state.direction);
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTime(state.assessmentTime)
                .leader(state.leader)
                .build();
        assessmentTeamRepository.save(team);
        assessmentTeamRepository.addMember(team.getId(), member.getId());

        List<AssessmentTeamMember> members = assessmentTeamRepository.findMembersByTeamId(team.getId());

        assertEquals(2, members.size());
        assertTrue(members.stream().anyMatch(m -> m.getUserId().equals(state.leader.getId())));
        assertTrue(members.stream().anyMatch(m -> m.getUserId().equals(member.getId())));
    }

    @Test
    @DisplayName("isMember: 应正确判断用户是否为队伍成员")
    void isMember_shouldReturnBoolean() {
        AssessmentFixtureState state = prepareFixture("2024001013", Direction.COMPUTER_VISION, 4, 2024);
        User nonMember = createMember("2024001014", state.college, state.direction);
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTime(state.assessmentTime)
                .leader(state.leader)
                .build();
        assessmentTeamRepository.save(team);

        assertTrue(assessmentTeamRepository.isMember(team.getId(), state.leader.getId()));
        assertFalse(assessmentTeamRepository.isMember(team.getId(), nonMember.getId()));
        assertFalse(assessmentTeamRepository.isMember(-1L, state.leader.getId()));
    }

    private record AssessmentFixtureState(User leader, AssessmentTime assessmentTime, College college,
            Direction direction) {
    }
}
