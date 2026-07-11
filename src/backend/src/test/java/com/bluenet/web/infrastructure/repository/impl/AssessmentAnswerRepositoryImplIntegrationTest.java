package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTeamRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentAnswerDO;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentAnswerMapper;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import com.bluenet.web.testsupport.fixture.CollegeFixture;
import com.bluenet.web.testsupport.fixture.FileFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AssessmentAnswerRepositoryImpl 集成测试。
 */
@DisplayName("AssessmentAnswerRepositoryImpl 集成测试")
class AssessmentAnswerRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AssessmentAnswerRepository assessmentAnswerRepository;

    @Autowired
    private AssessmentAnswerMapper assessmentAnswerMapper;

    @Autowired
    private AssessmentTimeRepository assessmentTimeRepository;

    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Autowired
    private AssessmentTeamRepository assessmentTeamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private FileRepository fileRepository;

    private int timeCounter = 1;

    private College createCollege() {
        return CollegeFixture.saveDefaultCollege(collegeRepository);
    }

    private User createUser(String studentId) {
        College college = createCollege();
        return UserFixture.candidate(studentId)
                .withCollege(college)
                .save(userRepository, passwordEncoder);
    }

    private AssessmentTime createTime() {
        int epoch = timeCounter++;
        return AssessmentFixture.timeBuilder()
                .epoch(epoch)
                .grade(2020 + epoch)
                .save(assessmentTimeRepository);
    }

    private AssessmentTime createTeamEnabledTime() {
        int epoch = timeCounter++;
        return AssessmentFixture.timeBuilder()
                .epoch(epoch)
                .grade(2020 + epoch)
                .allowTeam()
                .save(assessmentTimeRepository);
    }

    private AssessmentQuestion createQuestion(AssessmentTime time) {
        return AssessmentFixture.questionBuilder()
                .assessmentTimeId(time.getId())
                .questionNo(1)
                .save(assessmentQuestionRepository);
    }

    private AssessmentQuestion createQuestion(AssessmentTime time, int questionNo) {
        return AssessmentFixture.questionBuilder()
                .assessmentTimeId(time.getId())
                .questionNo(questionNo)
                .save(assessmentQuestionRepository);
    }

    private AssessmentTeam createTeam(AssessmentTime time, User leader) {
        return AssessmentFixture.teamBuilder()
                .assessmentTime(time)
                .leader(leader)
                .save(assessmentTeamRepository);
    }

    @Test
    @DisplayName("save: 应插入新作答并回写 ID")
    void save_shouldInsertNewAnswerAndSetId() {
        User user = createUser("2024001001");
        AssessmentTime time = createTime();
        AssessmentQuestion question = createQuestion(time);
        AssessmentAnswer answer = AssessmentFixture.answerBuilder()
                .user(user)
                .question(question)
                .build();

        assessmentAnswerRepository.save(answer);

        assertNotNull(answer.getId());
        AssessmentAnswerDO dataObject = assessmentAnswerMapper.selectById(answer.getId());
        assertNotNull(dataObject);
        assertEquals(user.getId(), dataObject.getUserId());
        assertEquals(question.getId(), dataObject.getQuestionId());
        assertEquals("测试答案", dataObject.getContent());
        assertEquals(ProgrammingLanguage.CPP, dataObject.getLanguage());
    }

    @Test
    @DisplayName("save: 应更新已有作答内容")
    void save_shouldUpdateExistingAnswerContent() {
        User user = createUser("2024001002");
        AssessmentTime time = createTime();
        AssessmentQuestion question = createQuestion(time);
        AssessmentAnswer answer = AssessmentFixture.answerBuilder()
                .user(user)
                .question(question)
                .build();
        assessmentAnswerRepository.save(answer);

        answer.update("更新后的答案", ProgrammingLanguage.JAVA, null);
        assessmentAnswerRepository.save(answer);

        AssessmentAnswerDO updated = assessmentAnswerMapper.selectById(answer.getId());
        assertEquals("更新后的答案", updated.getContent());
        assertEquals(ProgrammingLanguage.JAVA, updated.getLanguage());
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnPresentOrEmpty() {
        User user = createUser("2024001003");
        AssessmentTime time = createTime();
        AssessmentQuestion question = createQuestion(time);
        AssessmentAnswer answer = AssessmentFixture.answerBuilder()
                .user(user)
                .question(question)
                .save(assessmentAnswerRepository);

        Optional<AssessmentAnswer> found = assessmentAnswerRepository.findById(answer.getId());
        assertTrue(found.isPresent());
        assertEquals(answer.getUserId(), found.get().getUserId());

        assertTrue(assessmentAnswerRepository.findById(-1L).isEmpty());
    }

    @Test
    @DisplayName("findByFileId: 应按文件 ID 查询关联作答")
    void findByFileId_shouldReturnAnswerByFileId() {
        User user = createUser("2024001004");
        AssessmentTime time = createTime();
        AssessmentQuestion question = createQuestion(time);
        File file = FileFixture.save(fileRepository, "attachment.txt", FileType.ASSESSMENT_ATTACHMENT);
        AssessmentAnswer answer = AssessmentFixture.answerBuilder()
                .user(user)
                .question(question)
                .file(file)
                .build();
        assessmentAnswerRepository.save(answer);

        Optional<AssessmentAnswer> found = assessmentAnswerRepository.findByFileId(file.getId());
        assertTrue(found.isPresent());
        assertEquals(answer.getId(), found.get().getId());

        assertTrue(assessmentAnswerRepository.findByFileId(-1L).isEmpty());
    }

    @Test
    @DisplayName("countByUserIdAndAssessmentTimeId: 应统计用户在考核场次下的作答数")
    void countByUserIdAndAssessmentTimeId_shouldCountAnswers() {
        User user = createUser("2024001005");
        AssessmentTime time = createTime();
        AssessmentQuestion question1 = createQuestion(time, 1);
        AssessmentQuestion question2 = createQuestion(time, 2);
        AssessmentFixture.answerBuilder()
                .user(user)
                .question(question1)
                .save(assessmentAnswerRepository);
        AssessmentFixture.answerBuilder()
                .user(user)
                .question(question2)
                .content("第二题答案")
                .save(assessmentAnswerRepository);

        int count = assessmentAnswerRepository.countByUserIdAndAssessmentTimeId(user.getId(), time.getId());

        assertEquals(2, count);
    }

    @Test
    @DisplayName("countByUserIdAndAssessmentTimeIds: 应批量统计多个考核场次作答数")
    void countByUserIdAndAssessmentTimeIds_shouldCountMultipleTimes() {
        User user = createUser("2024001006");
        AssessmentTime time1 = createTime();
        AssessmentQuestion time1Question1 = createQuestion(time1, 1);
        AssessmentQuestion time1Question2 = createQuestion(time1, 2);
        AssessmentFixture.answerBuilder()
                .user(user)
                .question(time1Question1)
                .save(assessmentAnswerRepository);
        AssessmentFixture.answerBuilder()
                .user(user)
                .question(time1Question2)
                .content("场次一第二题")
                .save(assessmentAnswerRepository);

        AssessmentTime time2 = createTime();
        AssessmentQuestion time2Question1 = createQuestion(time2, 1);
        AssessmentFixture.answerBuilder()
                .user(user)
                .question(time2Question1)
                .content("场次二第一题")
                .save(assessmentAnswerRepository);

        Map<Long, Integer> counts = assessmentAnswerRepository.countByUserIdAndAssessmentTimeIds(
                user.getId(),
                List.of(time1.getId(), time2.getId(), -1L));

        assertEquals(2, counts.get(time1.getId()));
        assertEquals(1, counts.get(time2.getId()));
        assertFalse(counts.containsKey(-1L));
    }

    @Test
    @DisplayName("existsByUserIdAndQuestionId: 应判断用户是否已作答指定题目")
    void existsByUserIdAndQuestionId_shouldReturnTrueOrFalse() {
        User user = createUser("2024001007");
        AssessmentTime time = createTime();
        AssessmentQuestion question = createQuestion(time);
        AssessmentFixture.answerBuilder()
                .user(user)
                .question(question)
                .save(assessmentAnswerRepository);

        assertTrue(assessmentAnswerRepository.existsByUserIdAndQuestionId(user.getId(), question.getId()));
        assertFalse(assessmentAnswerRepository.existsByUserIdAndQuestionId(user.getId(), -1L));
        assertFalse(assessmentAnswerRepository.existsByUserIdAndQuestionId(-1L, question.getId()));
    }

    @Test
    @DisplayName("findByUserIdAndQuestionId: 应按用户和题目查询作答")
    void findByUserIdAndQuestionId_shouldReturnAnswer() {
        User user = createUser("2024001008");
        AssessmentTime time = createTime();
        AssessmentQuestion question = createQuestion(time);
        AssessmentAnswer answer = AssessmentFixture.answerBuilder()
                .user(user)
                .question(question)
                .save(assessmentAnswerRepository);

        Optional<AssessmentAnswer> found = assessmentAnswerRepository
                .findByUserIdAndQuestionId(user.getId(), question.getId());
        assertTrue(found.isPresent());
        assertEquals(answer.getId(), found.get().getId());

        assertTrue(assessmentAnswerRepository.findByUserIdAndQuestionId(user.getId(), -1L).isEmpty());
    }

    @Test
    @DisplayName("findByTeamIdAndQuestionId: 应按队伍和题目查询作答")
    void findByTeamIdAndQuestionId_shouldReturnTeamAnswers() {
        User user = createUser("2024001009");
        AssessmentTime time = createTeamEnabledTime();
        AssessmentTeam team = createTeam(time, user);
        AssessmentQuestion question = createQuestion(time);
        AssessmentAnswer answer = AssessmentFixture.answerBuilder()
                .user(user)
                .question(question)
                .team(team)
                .save(assessmentAnswerRepository);

        List<AssessmentAnswer> found = assessmentAnswerRepository
                .findByTeamIdAndQuestionId(team.getId(), question.getId());
        assertEquals(1, found.size());
        assertEquals(answer.getId(), found.get(0).getId());

        List<AssessmentAnswer> empty = assessmentAnswerRepository.findByTeamIdAndQuestionId(-1L, question.getId());
        assertTrue(empty.isEmpty());
    }

    @Test
    @DisplayName("deleteByTeamId: 应删除指定队伍下的所有作答")
    void deleteByTeamId_shouldRemoveTeamAnswers() {
        User user = createUser("2024001010");
        AssessmentTime time = createTeamEnabledTime();
        AssessmentTeam team = createTeam(time, user);
        AssessmentQuestion question1 = createQuestion(time, 1);
        AssessmentQuestion question2 = createQuestion(time, 2);
        AssessmentAnswer answer1 = AssessmentFixture.answerBuilder()
                .user(user)
                .question(question1)
                .team(team)
                .save(assessmentAnswerRepository);
        AssessmentAnswer answer2 = AssessmentFixture.answerBuilder()
                .user(user)
                .question(question2)
                .team(team)
                .content("队伍第二题答案")
                .save(assessmentAnswerRepository);

        assessmentAnswerRepository.deleteByTeamId(team.getId());

        assertTrue(assessmentAnswerRepository.findById(answer1.getId()).isEmpty());
        assertTrue(assessmentAnswerRepository.findById(answer2.getId()).isEmpty());
        assertTrue(assessmentAnswerRepository.findByTeamIdAndQuestionId(team.getId(), question1.getId()).isEmpty());
    }

    @Test
    @DisplayName("countByTeamId: 应统计队伍作答数")
    void countByTeamId_shouldCountTeamAnswers() {
        User user = createUser("2024001011");
        AssessmentTime time = createTeamEnabledTime();
        AssessmentTeam team = createTeam(time, user);
        AssessmentQuestion question1 = createQuestion(time, 1);
        AssessmentQuestion question2 = createQuestion(time, 2);
        AssessmentFixture.answerBuilder()
                .user(user)
                .question(question1)
                .team(team)
                .save(assessmentAnswerRepository);
        AssessmentFixture.answerBuilder()
                .user(user)
                .question(question2)
                .team(team)
                .content("队伍第二题答案")
                .save(assessmentAnswerRepository);

        assertEquals(2, assessmentAnswerRepository.countByTeamId(team.getId()));
        assertEquals(0, assessmentAnswerRepository.countByTeamId(-1L));
    }

    @Test
    @DisplayName("findAnswerIdsByTeamId: 应返回队伍下所有作答 ID")
    void findAnswerIdsByTeamId_shouldReturnAnswerIds() {
        User user = createUser("2024001012");
        AssessmentTime time = createTeamEnabledTime();
        AssessmentTeam team = createTeam(time, user);
        AssessmentQuestion question1 = createQuestion(time, 1);
        AssessmentQuestion question2 = createQuestion(time, 2);
        AssessmentAnswer answer1 = AssessmentFixture.answerBuilder()
                .user(user)
                .question(question1)
                .team(team)
                .save(assessmentAnswerRepository);
        AssessmentAnswer answer2 = AssessmentFixture.answerBuilder()
                .user(user)
                .question(question2)
                .team(team)
                .content("队伍第二题答案")
                .save(assessmentAnswerRepository);

        List<Long> ids = assessmentAnswerRepository.findAnswerIdsByTeamId(team.getId());
        assertEquals(2, ids.size());
        assertTrue(ids.contains(answer1.getId()));
        assertTrue(ids.contains(answer2.getId()));
        assertTrue(assessmentAnswerRepository.findAnswerIdsByTeamId(-1L).isEmpty());
    }

    @Test
    @DisplayName("batchInsert: 应批量插入作答数据行")
    void batchInsert_shouldInsertRows() {
        User user = createUser("2024001013");
        AssessmentTime time = createTime();
        AssessmentQuestion question1 = createQuestion(time, 1);
        AssessmentQuestion question2 = createQuestion(time, 2);
        AssessmentAnswer answer1 = AssessmentFixture.answerBuilder()
                .user(user)
                .question(question1)
                .build();
        AssessmentAnswer answer2 = AssessmentFixture.answerBuilder()
                .user(user)
                .question(question2)
                .content("批量第二题")
                .build();

        assessmentAnswerRepository.batchInsert(List.of(answer1, answer2));

        int count = assessmentAnswerRepository.countByUserIdAndAssessmentTimeId(user.getId(), time.getId());
        assertEquals(2, count);
    }

    @Test
    @DisplayName("findExistingAnswerUserIds: 应返回已作答的用户 ID 列表")
    void findExistingAnswerUserIds_shouldReturnUserIdsWithAnswers() {
        User user1 = createUser("2024001014");
        User user2 = createUser("2024001015");
        AssessmentTime time = createTime();
        AssessmentQuestion question = createQuestion(time);
        AssessmentFixture.answerBuilder()
                .user(user1)
                .question(question)
                .save(assessmentAnswerRepository);

        List<Long> existing = assessmentAnswerRepository.findExistingAnswerUserIds(
                List.of(user1.getId(), user2.getId(), -1L),
                question.getId());

        assertEquals(List.of(user1.getId()), existing);
    }

    @Test
    @DisplayName("countPersonalAnswersByUserIdAndAssessmentTimeId: 应只统计个人 FILE_UPLOAD 作答")
    void countPersonalAnswersByUserIdAndAssessmentTimeId_shouldCountPersonalOnly() {
        User user = createUser("2024001016");
        AssessmentTime time = createTime();
        AssessmentQuestion question = createQuestion(time);
        AssessmentFixture.answerBuilder()
                .user(user)
                .question(question)
                .save(assessmentAnswerRepository);

        int personalCount = assessmentAnswerRepository
                .countPersonalAnswersByUserIdAndAssessmentTimeId(user.getId(), time.getId());

        assertEquals(1, personalCount);
    }

    @Test
    @DisplayName("countTeamAnswersByUserIdAndAssessmentTimeId: 应只统计队伍 FILE_UPLOAD 作答")
    void countTeamAnswersByUserIdAndAssessmentTimeId_shouldCountTeamOnly() {
        User user = createUser("2024001017");
        AssessmentTime time = createTeamEnabledTime();
        AssessmentTeam team = createTeam(time, user);
        AssessmentQuestion question = createQuestion(time);
        AssessmentFixture.answerBuilder()
                .user(user)
                .question(question)
                .team(team)
                .save(assessmentAnswerRepository);

        int teamCount = assessmentAnswerRepository
                .countTeamAnswersByUserIdAndAssessmentTimeId(user.getId(), time.getId());

        assertEquals(1, teamCount);
    }
}
