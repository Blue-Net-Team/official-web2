package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentTimeDO;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentTimeMapper;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import com.bluenet.web.testsupport.fixture.CollegeFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AssessmentTimeRepositoryImpl 集成测试。
 * <p>
 * 验证考核场次仓储行为：save 插入/更新、Optional 查询、批量查询、删除、唯一键冲突、关联题目判断、分页与全局统计。
 * </p>
 */
@DisplayName("AssessmentTimeRepositoryImpl 集成测试")
class AssessmentTimeRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AssessmentTimeRepository assessmentTimeRepository;

    @Autowired
    private AssessmentTimeMapper assessmentTimeMapper;

    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AssessmentTime createTime(Direction direction, Integer epoch, Integer grade) {
        return AssessmentFixture.timeBuilder()
                .direction(direction)
                .epoch(epoch)
                .grade(grade)
                .save(assessmentTimeRepository);
    }

    @Test
    @DisplayName("save: 新考核场次应插入并回写ID")
    void save_newAssessmentTime_shouldInsertAndReturnId() {
        AssessmentTime time = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(1)
                .grade(2024)
                .build();

        assessmentTimeRepository.save(time);

        assertNotNull(time.getId());
        AssessmentTimeDO dataObject = assessmentTimeMapper.selectById(time.getId());
        assertNotNull(dataObject);
        assertEquals(Direction.COMPUTER_VISION, dataObject.getDirection());
        assertEquals(Integer.valueOf(1), dataObject.getEpoch());
        assertEquals(Integer.valueOf(2024), dataObject.getGrade());
    }

    @Test
    @DisplayName("save: 已有考核场次应更新字段")
    void save_existingAssessmentTime_shouldUpdateFields() {
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2, 2024);
        time.setAllowTeam(true);

        assessmentTimeRepository.save(time);

        AssessmentTimeDO updated = assessmentTimeMapper.selectById(time.getId());
        assertTrue(updated.getAllowTeam());
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 3, 2024);

        Optional<AssessmentTime> found = assessmentTimeRepository.findById(time.getId());
        assertTrue(found.isPresent());
        assertEquals(time.getId(), found.get().getId());
        assertEquals(Direction.COMPUTER_VISION, found.get().getDirection());

        Optional<AssessmentTime> notFound = assessmentTimeRepository.findById(-1L);
        assertTrue(notFound.isEmpty());
    }

    @Test
    @DisplayName("findAllById: 应返回存在的记录并忽略不存在的主键")
    void findAllById_shouldReturnExistingRecords() {
        AssessmentTime time1 = createTime(Direction.COMPUTER_VISION, 4, 2024);
        AssessmentTime time2 = createTime(Direction.STRUCTURAL_DESIGN, 4, 2024);

        List<AssessmentTime> found = assessmentTimeRepository.findAllById(List.of(time1.getId(), time2.getId(), -1L));

        assertEquals(2, found.size());
        List<Long> foundIds = found.stream().map(AssessmentTime::getId).toList();
        assertTrue(foundIds.contains(time1.getId()));
        assertTrue(foundIds.contains(time2.getId()));
    }

    @Test
    @DisplayName("deleteById: 应删除指定考核场次")
    void deleteById_shouldRemoveRecord() {
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 5, 2024);

        assessmentTimeRepository.deleteById(time.getId());

        assertNull(assessmentTimeMapper.selectById(time.getId()));
    }

    @Test
    @DisplayName("existsById: 应正确判断存在性")
    void existsById_shouldWork() {
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 6, 2024);

        assertTrue(assessmentTimeRepository.existsById(time.getId()));
        assertFalse(assessmentTimeRepository.existsById(-1L));
    }

    @Test
    @DisplayName("existsByDirectionAndEpochAndGrade: 应正确判断唯一键存在性")
    void existsByDirectionAndEpochAndGrade_shouldWork() {
        createTime(Direction.COMPUTER_VISION, 7, 2024);

        assertTrue(assessmentTimeRepository.existsByDirectionAndEpochAndGrade(Direction.COMPUTER_VISION, 7, 2024));
        assertFalse(assessmentTimeRepository.existsByDirectionAndEpochAndGrade(Direction.COMPUTER_VISION, 7, 2025));
    }

    @Test
    @DisplayName("existsByDirectionAndEpochAndGradeAndIdNot: 排除自身后应正确判断冲突")
    void existsByDirectionAndEpochAndGradeAndIdNot_shouldWork() {
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 8, 2024);

        assertFalse(
                assessmentTimeRepository.existsByDirectionAndEpochAndGradeAndIdNot(
                        Direction.COMPUTER_VISION,
                        8,
                        2024,
                        time.getId()));
        assertTrue(
                assessmentTimeRepository.existsByDirectionAndEpochAndGradeAndIdNot(
                        Direction.COMPUTER_VISION,
                        8,
                        2024,
                        -1L));
    }

    @Test
    @DisplayName("hasAssociatedQuestions: 应正确判断是否存在关联题目")
    void hasAssociatedQuestions_shouldWork() {
        AssessmentTime timeWithQuestion = createTime(Direction.COMPUTER_VISION, 9, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTimeId(timeWithQuestion.getId())
                .questionNo(1)
                .save(assessmentQuestionRepository);
        assertNotNull(question.getId());

        AssessmentTime timeWithoutQuestion = createTime(Direction.STRUCTURAL_DESIGN, 9, 2024);

        assertTrue(assessmentTimeRepository.hasAssociatedQuestions(timeWithQuestion.getId()));
        assertFalse(assessmentTimeRepository.hasAssociatedQuestions(timeWithoutQuestion.getId()));
    }

    @Test
    @DisplayName("findByFilters: 应按方向和年级分页查询")
    void findByFilters_shouldFilterAndPaginate() {
        Direction direction = Direction.COMPUTER_VISION;
        Integer grade = 2024;
        createTime(direction, 10, grade);
        createTime(direction, 11, grade);
        createTime(direction, 12, grade);
        createTime(Direction.STRUCTURAL_DESIGN, 10, grade);

        Page<AssessmentTime> page = assessmentTimeRepository.findByFilters(direction, grade, PageRequest.of(0, 2));

        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getContent().size());
    }

    @Test
    @DisplayName("findByUserParticipation: 应按用户方向与入学年份分页查询")
    void findByUserParticipation_shouldFilterAndPaginate() {
        College college = CollegeFixture.saveDefaultCollege(collegeRepository);
        User user = UserFixture.candidate("2024001010")
                .withCollege(college)
                .withDirection(Direction.COMPUTER_VISION)
                .withAssessmentGradeYear(2024)
                .save(userRepository, passwordEncoder);

        AssessmentTime matchingTime = createTime(Direction.COMPUTER_VISION, 13, 2024);
        AssessmentTime matchingNullGradeTime = createTime(Direction.COMPUTER_VISION, 14, null);
        createTime(Direction.STRUCTURAL_DESIGN, 15, 2024);

        Page<AssessmentTime> page = assessmentTimeRepository.findByUserParticipation(
                user.getId(),
                Direction.COMPUTER_VISION,
                2024,
                PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
        List<Long> foundIds = page.getContent().stream().map(AssessmentTime::getId).toList();
        assertTrue(foundIds.contains(matchingTime.getId()));
        assertTrue(foundIds.contains(matchingNullGradeTime.getId()));
    }

    @Test
    @DisplayName("countByEpochGrade: 应统计全局考核场次数量")
    void countByEpochGrade_shouldCountGlobalAssessments() {
        createTime(null, 16, 2021);
        createTime(null, 16, null);

        assertEquals(1L, assessmentTimeRepository.countByEpochGrade(16, 2021));
        assertEquals(1L, assessmentTimeRepository.countByEpochGrade(16, null));
        assertEquals(0L, assessmentTimeRepository.countByEpochGrade(16, 2020));
    }

    @Test
    @DisplayName("hasConflictingGradeByDirectionAndEpoch: 应正确判断年级形式冲突")
    void hasConflictingGradeByDirectionAndEpoch_shouldDetectConflict() {
        Direction direction = Direction.COMPUTER_VISION;
        Integer epoch = 17;
        createTime(direction, epoch, null);

        assertTrue(assessmentTimeRepository.hasConflictingGradeByDirectionAndEpoch(direction, epoch, 2024));
        assertFalse(assessmentTimeRepository.hasConflictingGradeByDirectionAndEpoch(direction, epoch, null));
    }

    @Test
    @DisplayName("hasConflictingGradeByDirectionAndEpochAndIdNot: 排除自身后应正确判断年级形式冲突")
    void hasConflictingGradeByDirectionAndEpochAndIdNot_shouldDetectConflict() {
        Direction direction = Direction.COMPUTER_VISION;
        Integer epoch = 18;
        AssessmentTime time = createTime(direction, epoch, null);

        assertFalse(
                assessmentTimeRepository.hasConflictingGradeByDirectionAndEpochAndIdNot(
                        direction,
                        epoch,
                        2024,
                        time.getId()));
        assertTrue(
                assessmentTimeRepository.hasConflictingGradeByDirectionAndEpochAndIdNot(
                        direction,
                        epoch,
                        2024,
                        -1L));
    }

    @Test
    @DisplayName("save: 关闭限时后应清空限时分钟数")
    void save_disableTimeLimit_shouldClearTimeLimitMinutes() {
        AssessmentTime time = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(19)
                .grade(2024)
                .timeLimit(30)
                .save(assessmentTimeRepository);

        time.setTimeLimit(false);
        assessmentTimeRepository.save(time);

        AssessmentTimeDO updated = assessmentTimeMapper.selectById(time.getId());
        assertFalse(updated.getTimeLimit());
        assertNull(updated.getTimeLimitMinutes());
    }
}
