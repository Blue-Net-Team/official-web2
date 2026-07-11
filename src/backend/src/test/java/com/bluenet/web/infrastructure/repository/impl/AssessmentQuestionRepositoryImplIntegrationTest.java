package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentQuestionDO;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentQuestionMapper;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import com.bluenet.web.testsupport.fixture.FileFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AssessmentQuestionRepositoryImpl 集成测试。
 */
@DisplayName("AssessmentQuestionRepositoryImpl 集成测试")
class AssessmentQuestionRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Autowired
    private AssessmentQuestionMapper assessmentQuestionMapper;

    @Autowired
    private AssessmentTimeRepository assessmentTimeRepository;

    @Autowired
    private FileRepository fileRepository;

    private int timeCounter = 1;

    private AssessmentTime createTime() {
        int epoch = timeCounter++;
        return AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(epoch)
                .grade(2020 + epoch)
                .save(assessmentTimeRepository);
    }

    @Test
    @DisplayName("save: 新题目应插入并回写ID")
    void save_newQuestion_shouldInsertAndReturnId() {
        AssessmentTime time = createTime();

        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .questionNo(1)
                .title("测试题目")
                .score(BigDecimal.valueOf(100))
                .save(assessmentQuestionRepository);

        assertNotNull(question.getId());
        AssessmentQuestionDO dataObject = assessmentQuestionMapper.selectById(question.getId());
        assertNotNull(dataObject);
        assertEquals(time.getId(), dataObject.getAssessmentTimeId());
        assertEquals("测试题目", dataObject.getTitle());
    }

    @Test
    @DisplayName("save: 已有题目应更新字段")
    void save_existingQuestion_shouldUpdateFields() {
        AssessmentTime time = createTime();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .questionNo(1)
                .title("旧标题")
                .save(assessmentQuestionRepository);

        question.setTitle("新标题");
        question.setScore(BigDecimal.valueOf(80));
        assessmentQuestionRepository.save(question);

        AssessmentQuestionDO updated = assessmentQuestionMapper.selectById(question.getId());
        assertEquals("新标题", updated.getTitle());
        assertEquals(0, BigDecimal.valueOf(80).compareTo(updated.getScore()));
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        AssessmentTime time = createTime();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .questionNo(1)
                .save(assessmentQuestionRepository);

        Optional<AssessmentQuestion> found = assessmentQuestionRepository.findById(question.getId());
        assertTrue(found.isPresent());
        assertEquals(question.getTitle(), found.get().getTitle());

        assertTrue(assessmentQuestionRepository.findById(-1L).isEmpty());
    }

    @Test
    @DisplayName("findByAttachmentId: 应按附件文件ID查询题目")
    void findByAttachmentId_shouldReturnQuestionByAttachment() {
        AssessmentTime time = createTime();
        File attachment = FileFixture.save(fileRepository, "attachment.txt", FileType.ASSESSMENT_ATTACHMENT);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .questionNo(1)
                .attachmentId(attachment.getId())
                .save(assessmentQuestionRepository);

        Optional<AssessmentQuestion> found = assessmentQuestionRepository.findByAttachmentId(attachment.getId());

        assertTrue(found.isPresent());
        assertEquals(question.getId(), found.get().getId());
        assertTrue(assessmentQuestionRepository.findByAttachmentId(-1L).isEmpty());
    }

    @Test
    @DisplayName("countByAssessmentTimeId: 应统计考核场次下的题目数量")
    void countByAssessmentTimeId_shouldCountQuestions() {
        AssessmentTime time = createTime();
        AssessmentFixture.questionBuilder().assessmentTime(time).questionNo(1).save(assessmentQuestionRepository);
        AssessmentFixture.questionBuilder().assessmentTime(time).questionNo(2).save(assessmentQuestionRepository);

        int count = assessmentQuestionRepository.countByAssessmentTimeId(time.getId());

        assertEquals(2, count);
    }

    @Test
    @DisplayName("countByAssessmentTimeIds: 应批量统计多个考核场次的题目数量")
    void countByAssessmentTimeIds_shouldReturnCountMap() {
        AssessmentTime time1 = createTime();
        AssessmentTime time2 = createTime();
        AssessmentTime time3 = createTime();
        AssessmentFixture.questionBuilder().assessmentTime(time1).questionNo(1).save(assessmentQuestionRepository);
        AssessmentFixture.questionBuilder().assessmentTime(time1).questionNo(2).save(assessmentQuestionRepository);
        AssessmentFixture.questionBuilder().assessmentTime(time2).questionNo(1).save(assessmentQuestionRepository);

        Map<Long, Integer> counts = assessmentQuestionRepository.countByAssessmentTimeIds(
                List.of(time1.getId(), time2.getId(), time3.getId()));

        assertEquals(2, counts.get(time1.getId()));
        assertEquals(1, counts.get(time2.getId()));
        assertNull(counts.get(time3.getId()));
        assertTrue(assessmentQuestionRepository.countByAssessmentTimeIds(List.of()).isEmpty());
    }

    @Test
    @DisplayName("findAllByTimeId: 应分页查询考核场次下的题目")
    void findAllByTimeId_shouldReturnPagedQuestions() {
        AssessmentTime time = createTime();
        AssessmentFixture.questionBuilder().assessmentTime(time).questionNo(1).save(assessmentQuestionRepository);
        AssessmentFixture.questionBuilder().assessmentTime(time).questionNo(2).save(assessmentQuestionRepository);
        AssessmentFixture.questionBuilder().assessmentTime(time).questionNo(3).save(assessmentQuestionRepository);

        Page<AssessmentQuestion> page = assessmentQuestionRepository
                .findAllByTimeId(time.getId(), PageRequest.of(0, 2));

        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getContent().size());
    }

    @Test
    @DisplayName("deleteById: 应删除指定题目")
    void deleteById_shouldRemoveQuestion() {
        AssessmentTime time = createTime();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .questionNo(1)
                .save(assessmentQuestionRepository);

        assessmentQuestionRepository.deleteById(question.getId());

        assertNull(assessmentQuestionMapper.selectById(question.getId()));
    }

    @Test
    @DisplayName("existsById: 应正确判断题目是否存在")
    void existsById_shouldReturnBoolean() {
        AssessmentTime time = createTime();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .questionNo(1)
                .save(assessmentQuestionRepository);

        assertTrue(assessmentQuestionRepository.existsById(question.getId()));
        assertFalse(assessmentQuestionRepository.existsById(-1L));
    }

    @Test
    @DisplayName("findByTimeIdAndQuestionNo: 应按考核场次和题号查询")
    void findByTimeIdAndQuestionNo_shouldReturnQuestion() {
        AssessmentTime time = createTime();
        AssessmentFixture.questionBuilder().assessmentTime(time).questionNo(1).save(assessmentQuestionRepository);
        AssessmentQuestion question2 = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .questionNo(2)
                .save(assessmentQuestionRepository);

        Optional<AssessmentQuestion> found = assessmentQuestionRepository.findByTimeIdAndQuestionNo(time.getId(), 2);

        assertTrue(found.isPresent());
        assertEquals(question2.getId(), found.get().getId());
        assertTrue(assessmentQuestionRepository.findByTimeIdAndQuestionNo(time.getId(), 99).isEmpty());
    }

    @Test
    @DisplayName("save: 应保存不同类型的题目内容")
    void save_shouldPersistDifferentQuestionTypes() {
        AssessmentTime time = createTime();

        AssessmentQuestion singleChoice = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .questionNo(1)
                .singleChoice("A", "A", "B", "C")
                .save(assessmentQuestionRepository);
        AssessmentQuestion multipleChoice = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .questionNo(2)
                .multipleChoice(List.of("A", "B"), "A", "B", "C")
                .save(assessmentQuestionRepository);
        AssessmentQuestion algorithm = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .questionNo(3)
                .algorithm()
                .save(assessmentQuestionRepository);

        assertEquals(
                QuestionType.SINGLE_CHOICE,
                assessmentQuestionRepository.findById(singleChoice.getId()).orElseThrow().getQuestionType());
        assertEquals(
                QuestionType.MULTIPLE_CHOICE,
                assessmentQuestionRepository.findById(multipleChoice.getId()).orElseThrow().getQuestionType());
        assertEquals(
                QuestionType.ALGORITHM,
                assessmentQuestionRepository.findById(algorithm.getId()).orElseThrow().getQuestionType());
    }
}
