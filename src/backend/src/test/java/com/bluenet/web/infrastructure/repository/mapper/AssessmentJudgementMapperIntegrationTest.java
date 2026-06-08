package com.bluenet.web.infrastructure.repository.mapper;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.enumerate.ReviewerType;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentAnswerDO;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentJudgementDO;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentQuestionDO;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentTeamDO;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentTeamMemberDO;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentTimeDO;
import com.bluenet.web.infrastructure.repository.dataobject.RoleDO;
import com.bluenet.web.infrastructure.repository.dataobject.UserDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AssessmentCandidateScoreQueryDO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AssessmentJudgementMapper 集成测试。
 */
@DisplayName("AssessmentJudgementMapper 集成测试")
class AssessmentJudgementMapperIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AssessmentJudgementMapper assessmentJudgementMapper;

    @Autowired
    private AssessmentTimeMapper assessmentTimeMapper;

    @Autowired
    private AssessmentQuestionMapper assessmentQuestionMapper;

    @Autowired
    private AssessmentAnswerMapper assessmentAnswerMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private AssessmentTeamMapper assessmentTeamMapper;

    @Autowired
    private AssessmentTeamMemberMapper assessmentTeamMemberMapper;

    /**
     * 验证当考核时间的 grade 为 null（不限年级）时，人员视图仍能正确返回考生评分矩阵。
     */
    @Test
    @DisplayName("selectCandidateScoreRows: grade=null 时应返回考生评分数据")
    void selectCandidateScoreRows_nullGrade_shouldReturnCandidates() {
        // 1. 获取 CANDIDATE 角色
        RoleDO candidateRole = roleMapper.selectByName("CANDIDATE");
        assertThat(candidateRole).isNotNull();

        // 2. 创建考核时间：direction=COMPUTER_VISION, grade=null（不限年级）
        AssessmentTimeDO time = AssessmentTimeDO.builder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(1)
                .grade(null)
                .startTime(LocalDateTime.now().minusDays(7))
                .endTime(LocalDateTime.now().plusDays(7))
                .timeLimit(false)
                .timeLimitMinutes(null)
                .build();
        assessmentTimeMapper.insert(time);
        Long assessmentTimeId = time.getId();
        assertThat(assessmentTimeId).isNotNull();

        // 3. 创建考生用户
        UserDO candidate = UserDO.builder()
                .studentId("20260001")
                .email("candidate@test.com")
                .roleId(candidateRole.getId())
                .password("hashed_password")
                .username("testcandidate")
                .nickname("测试考生")
                .collegeId(1L)
                .direction(Direction.COMPUTER_VISION)
                .assessmentGradeYear(null)
                .disable(false)
                .build();
        userMapper.insert(candidate);
        Long candidateId = candidate.getId();
        assertThat(candidateId).isNotNull();

        // 4. 创建题目
        AssessmentQuestionDO question = AssessmentQuestionDO.builder()
                .assessmentTimeId(assessmentTimeId)
                .questionNo(1)
                .questionType(QuestionType.FILE_UPLOAD)
                .title("作品提交题")
                .score(BigDecimal.TEN)
                .build();
        assessmentQuestionMapper.insert(question);
        Long questionId = question.getId();
        assertThat(questionId).isNotNull();

        // 5. 创建作答
        AssessmentAnswerDO answer = AssessmentAnswerDO.builder()
                .userId(candidateId)
                .questionId(questionId)
                .content("测试作答内容")
                .submitTime(LocalDateTime.now())
                .build();
        assessmentAnswerMapper.insert(answer);
        Long answerId = answer.getId();
        assertThat(answerId).isNotNull();

        // 6. 创建评分记录
        AssessmentJudgementDO judgement = AssessmentJudgementDO.builder()
                .answerId(answerId)
                .questionId(questionId)
                .assessmentTimeId(assessmentTimeId)
                .userId(candidateId)
                .score(BigDecimal.valueOf(8))
                .maxScore(BigDecimal.TEN)
                .status(JudgementStatus.JUDGED)
                .resultCode(ObjectiveResultCode.AC)
                .source(JudgementSource.MANUAL)
                .reviewerId(1L)
                .reviewerType(ReviewerType.DIRECTION_ADMIN)
                .judgedAt(LocalDateTime.now())
                .build();
        assessmentJudgementMapper.insert(judgement);

        // 7. 调用 selectCandidateScoreRows 验证返回非空
        List<AssessmentCandidateScoreQueryDO> rows = assessmentJudgementMapper
                .selectCandidateScoreRows(assessmentTimeId, null);

        assertThat(rows).isNotEmpty();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getCandidateUserId()).isEqualTo(candidateId);
        assertThat(rows.get(0).getQuestionId()).isEqualTo(questionId);
        assertThat(rows.get(0).getJudgementScore()).isEqualByComparingTo(BigDecimal.valueOf(8));
    }

    /**
     * 验证当考核时间的 direction 和 grade 均为 null（全局考核）时，人员视图仍能正确返回考生评分矩阵。
     */
    @Test
    @DisplayName("selectCandidateScoreRows: direction=null 且 grade=null 时应返回考生评分数据")
    void selectCandidateScoreRows_nullDirectionAndGrade_shouldReturnCandidates() {
        // 1. 获取 CANDIDATE 角色
        RoleDO candidateRole = roleMapper.selectByName("CANDIDATE");
        assertThat(candidateRole).isNotNull();

        // 2. 创建全局考核时间：direction=null, grade=null
        AssessmentTimeDO time = AssessmentTimeDO.builder()
                .direction(null)
                .epoch(3)
                .grade(null)
                .startTime(LocalDateTime.now().minusDays(7))
                .endTime(LocalDateTime.now().plusDays(7))
                .timeLimit(false)
                .timeLimitMinutes(null)
                .build();
        assessmentTimeMapper.insert(time);
        Long assessmentTimeId = time.getId();
        assertThat(assessmentTimeId).isNotNull();

        // 3. 创建考生用户（任意方向）
        UserDO candidate = UserDO.builder()
                .studentId("20260002")
                .email("candidate2@test.com")
                .roleId(candidateRole.getId())
                .password("hashed_password")
                .username("testcandidate2")
                .nickname("测试考生2")
                .collegeId(1L)
                .direction(Direction.EMBEDDED)
                .assessmentGradeYear(2026)
                .disable(false)
                .build();
        userMapper.insert(candidate);
        Long candidateId = candidate.getId();
        assertThat(candidateId).isNotNull();

        // 4. 创建题目
        AssessmentQuestionDO question = AssessmentQuestionDO.builder()
                .assessmentTimeId(assessmentTimeId)
                .questionNo(1)
                .questionType(QuestionType.FILE_UPLOAD)
                .title("全局考核题")
                .score(BigDecimal.TEN)
                .build();
        assessmentQuestionMapper.insert(question);
        Long questionId = question.getId();
        assertThat(questionId).isNotNull();

        // 5. 创建作答
        AssessmentAnswerDO answer = AssessmentAnswerDO.builder()
                .userId(candidateId)
                .questionId(questionId)
                .content("全局考核作答")
                .submitTime(LocalDateTime.now())
                .build();
        assessmentAnswerMapper.insert(answer);
        Long answerId = answer.getId();
        assertThat(answerId).isNotNull();

        // 6. 创建评分记录
        AssessmentJudgementDO judgement = AssessmentJudgementDO.builder()
                .answerId(answerId)
                .questionId(questionId)
                .assessmentTimeId(assessmentTimeId)
                .userId(candidateId)
                .score(BigDecimal.valueOf(9))
                .maxScore(BigDecimal.TEN)
                .status(JudgementStatus.JUDGED)
                .resultCode(ObjectiveResultCode.AC)
                .source(JudgementSource.MANUAL)
                .reviewerId(1L)
                .reviewerType(ReviewerType.DIRECTION_ADMIN)
                .judgedAt(LocalDateTime.now())
                .build();
        assessmentJudgementMapper.insert(judgement);

        // 7. 调用 selectCandidateScoreRows 验证返回非空
        List<AssessmentCandidateScoreQueryDO> rows = assessmentJudgementMapper
                .selectCandidateScoreRows(assessmentTimeId, null);

        assertThat(rows).isNotEmpty();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getCandidateUserId()).isEqualTo(candidateId);
        assertThat(rows.get(0).getQuestionId()).isEqualTo(questionId);
        assertThat(rows.get(0).getJudgementScore()).isEqualByComparingTo(BigDecimal.valueOf(9));
    }

    /**
     * 验证全局考核 + 组队场景下，人员视图能正确返回所有组员（含跨方向）的评分矩阵。
     */
    @Test
    @DisplayName("selectCandidateScoreRows: 全局考核 + 组队场景应返回所有组员")
    void selectCandidateScoreRows_globalAssessmentWithTeam_shouldReturnAllMembers() {
        // 1. 获取 CANDIDATE 角色
        RoleDO candidateRole = roleMapper.selectByName("CANDIDATE");
        assertThat(candidateRole).isNotNull();

        // 2. 创建全局考核时间：direction=null, grade=null, allowTeam 通过应用层处理
        AssessmentTimeDO time = AssessmentTimeDO.builder()
                .direction(null)
                .epoch(3)
                .grade(null)
                .startTime(LocalDateTime.now().minusDays(7))
                .endTime(LocalDateTime.now().plusDays(7))
                .timeLimit(false)
                .timeLimitMinutes(null)
                .build();
        assessmentTimeMapper.insert(time);
        Long assessmentTimeId = time.getId();
        assertThat(assessmentTimeId).isNotNull();

        // 3. 创建跨方向考生用户
        UserDO leader = UserDO.builder()
                .studentId("20260010")
                .email("leader@test.com")
                .roleId(candidateRole.getId())
                .password("hashed_password")
                .username("teamleader")
                .nickname("队长")
                .collegeId(1L)
                .direction(Direction.COMPUTER_VISION)
                .assessmentGradeYear(2026)
                .disable(false)
                .build();
        userMapper.insert(leader);
        Long leaderId = leader.getId();
        assertThat(leaderId).isNotNull();

        UserDO member1 = UserDO.builder()
                .studentId("20260011")
                .email("member1@test.com")
                .roleId(candidateRole.getId())
                .password("hashed_password")
                .username("teammember1")
                .nickname("组员1")
                .collegeId(1L)
                .direction(Direction.EMBEDDED)
                .assessmentGradeYear(2026)
                .disable(false)
                .build();
        userMapper.insert(member1);
        Long member1Id = member1.getId();
        assertThat(member1Id).isNotNull();

        UserDO member2 = UserDO.builder()
                .studentId("20260012")
                .email("member2@test.com")
                .roleId(candidateRole.getId())
                .password("hashed_password")
                .username("teammember2")
                .nickname("组员2")
                .collegeId(1L)
                .direction(Direction.EMBEDDED)
                .assessmentGradeYear(2025)
                .disable(false)
                .build();
        userMapper.insert(member2);
        Long member2Id = member2.getId();
        assertThat(member2Id).isNotNull();

        // 4. 创建题目
        AssessmentQuestionDO question = AssessmentQuestionDO.builder()
                .assessmentTimeId(assessmentTimeId)
                .questionNo(1)
                .questionType(QuestionType.FILE_UPLOAD)
                .title("全局组队考核题")
                .score(BigDecimal.TEN)
                .build();
        assessmentQuestionMapper.insert(question);
        Long questionId = question.getId();
        assertThat(questionId).isNotNull();

        // 5. 创建队伍
        AssessmentTeamDO team = AssessmentTeamDO.builder()
                .assessmentTimeId(assessmentTimeId)
                .leaderId(leaderId)
                .name("跨方向队")
                .inviteCode("GLOBAL")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();
        assessmentTeamMapper.insert(team);
        Long teamId = team.getId();
        assertThat(teamId).isNotNull();

        // 6. 创建队员关系
        AssessmentTeamMemberDO leaderMember = AssessmentTeamMemberDO.builder()
                .teamId(teamId)
                .userId(leaderId)
                .joinedAt(LocalDateTime.now())
                .build();
        assessmentTeamMemberMapper.insert(leaderMember);

        AssessmentTeamMemberDO tm1 = AssessmentTeamMemberDO.builder()
                .teamId(teamId)
                .userId(member1Id)
                .joinedAt(LocalDateTime.now())
                .build();
        assessmentTeamMemberMapper.insert(tm1);

        AssessmentTeamMemberDO tm2 = AssessmentTeamMemberDO.builder()
                .teamId(teamId)
                .userId(member2Id)
                .joinedAt(LocalDateTime.now())
                .build();
        assessmentTeamMemberMapper.insert(tm2);

        // 7. 创建队长和组员的 answer 记录（写时复制）
        AssessmentAnswerDO leaderAnswer = AssessmentAnswerDO.builder()
                .userId(leaderId)
                .questionId(questionId)
                .content("队长作答")
                .submitTime(LocalDateTime.now())
                .teamId(teamId)
                .build();
        assessmentAnswerMapper.insert(leaderAnswer);
        Long leaderAnswerId = leaderAnswer.getId();
        assertThat(leaderAnswerId).isNotNull();

        AssessmentAnswerDO member1Answer = AssessmentAnswerDO.builder()
                .userId(member1Id)
                .questionId(questionId)
                .content("队长作答")
                .submitTime(LocalDateTime.now())
                .teamId(teamId)
                .build();
        assessmentAnswerMapper.insert(member1Answer);
        Long member1AnswerId = member1Answer.getId();
        assertThat(member1AnswerId).isNotNull();

        AssessmentAnswerDO member2Answer = AssessmentAnswerDO.builder()
                .userId(member2Id)
                .questionId(questionId)
                .content("队长作答")
                .submitTime(LocalDateTime.now())
                .teamId(teamId)
                .build();
        assessmentAnswerMapper.insert(member2Answer);
        Long member2AnswerId = member2Answer.getId();
        assertThat(member2AnswerId).isNotNull();

        // 8. 为所有组员创建评分记录
        AssessmentJudgementDO leaderJudgement = AssessmentJudgementDO.builder()
                .answerId(leaderAnswerId)
                .questionId(questionId)
                .assessmentTimeId(assessmentTimeId)
                .userId(leaderId)
                .score(BigDecimal.valueOf(9))
                .maxScore(BigDecimal.TEN)
                .status(JudgementStatus.JUDGED)
                .resultCode(ObjectiveResultCode.AC)
                .source(JudgementSource.MANUAL)
                .reviewerId(1L)
                .reviewerType(ReviewerType.DIRECTION_ADMIN)
                .judgedAt(LocalDateTime.now())
                .build();
        assessmentJudgementMapper.insert(leaderJudgement);

        AssessmentJudgementDO m1Judgement = AssessmentJudgementDO.builder()
                .answerId(member1AnswerId)
                .questionId(questionId)
                .assessmentTimeId(assessmentTimeId)
                .userId(member1Id)
                .score(BigDecimal.valueOf(9))
                .maxScore(BigDecimal.TEN)
                .status(JudgementStatus.JUDGED)
                .resultCode(ObjectiveResultCode.AC)
                .source(JudgementSource.ADMIN_FINALIZED)
                .reviewerId(1L)
                .reviewerType(ReviewerType.DIRECTION_ADMIN)
                .judgedAt(LocalDateTime.now())
                .build();
        assessmentJudgementMapper.insert(m1Judgement);

        AssessmentJudgementDO m2Judgement = AssessmentJudgementDO.builder()
                .answerId(member2AnswerId)
                .questionId(questionId)
                .assessmentTimeId(assessmentTimeId)
                .userId(member2Id)
                .score(BigDecimal.valueOf(9))
                .maxScore(BigDecimal.TEN)
                .status(JudgementStatus.JUDGED)
                .resultCode(ObjectiveResultCode.AC)
                .source(JudgementSource.ADMIN_FINALIZED)
                .reviewerId(1L)
                .reviewerType(ReviewerType.DIRECTION_ADMIN)
                .judgedAt(LocalDateTime.now())
                .build();
        assessmentJudgementMapper.insert(m2Judgement);

        // 9. 调用 selectCandidateScoreRows 验证返回所有组员
        List<AssessmentCandidateScoreQueryDO> rows = assessmentJudgementMapper
                .selectCandidateScoreRows(assessmentTimeId, null);

        assertThat(rows).isNotEmpty();
        assertThat(rows).hasSize(3);
        List<Long> actualUserIds = rows.stream()
                .map(AssessmentCandidateScoreQueryDO::getCandidateUserId)
                .toList();
        assertThat(actualUserIds).containsExactlyInAnyOrder(leaderId, member1Id, member2Id);

        // 10. 调用 selectQuestionSubmissions 验证返回所有组员提交
        List<com.bluenet.web.infrastructure.repository.dataobject.query.AssessmentQuestionSubmissionQueryDO> submissions = assessmentJudgementMapper
                .selectQuestionSubmissions(questionId, null, null);
        assertThat(submissions).hasSize(3);
        List<Long> submissionUserIds = submissions.stream()
                .map(
                        com.bluenet.web.infrastructure.repository.dataobject.query.AssessmentQuestionSubmissionQueryDO::getCandidateUserId)
                .toList();
        assertThat(submissionUserIds).containsExactlyInAnyOrder(leaderId, member1Id, member2Id);
    }
}
