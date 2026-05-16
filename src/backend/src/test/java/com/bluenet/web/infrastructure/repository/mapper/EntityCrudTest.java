package com.bluenet.web.infrastructure.repository.mapper;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.testsupport.RepositoryTestObjects;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.*;
import com.bluenet.web.domain.model.enumerate.*;
import com.bluenet.web.domain.model.vo.evaluation.AlgorithmContent;
import com.bluenet.web.domain.model.vo.evaluation.QuestionContent;
import com.bluenet.web.domain.model.vo.evaluation.SingleChoiceContent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 实体 CRUD 集成测试 使用 PostgreSQL 测试数据库
 */
class EntityCrudTest extends BaseIntegrationTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CollegeMapper collegeMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private AssessmentQuestionMapper assessmentQuestionMapper;

    @Autowired
    private AssessmentTimeMapper assessmentTimeMapper;

    @Autowired
    private EnrollMapper enrollMapper;

    @Test
    void testUserCrudWithEnum() {
        // 创建学院和角色
        College college = College.create("计算机学院");
        RepositoryTestObjects.insert(collegeMapper, college, CollegeDO.class);

        Role role = RepositoryTestObjects.toDomain(roleMapper.selectByName("MEMBER"), Role.class);

        // 创建用户
        User user = User.reconstruct(
                null,
                "202401010001",
                "test@example.com",
                role.getId(),
                null,
                "测试用户",
                null,
                college.getId(),
                null,
                null,
                Direction.COMPUTER_VISION,
                Gender.MALE,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null);

        // 插入
        int insertCount = RepositoryTestObjects.insert(userMapper, user, UserDO.class);
        assertEquals(1, insertCount);
        assertNotNull(user.getId());

        // 查询
        User retrieved = RepositoryTestObjects.toDomain(userMapper.selectById(user.getId()), User.class);
        assertNotNull(retrieved);
        assertEquals("202401010001", retrieved.getStudentId());
        assertEquals("test@example.com", retrieved.getEmail());

        // 验证枚举映射
        assertEquals(Direction.COMPUTER_VISION, retrieved.getDirection());
        assertEquals(Gender.MALE, retrieved.getGender());
    }

    @Test
    void testEvaluationQuestionWithJsonContent() {
        // 创建考核时间
        AssessmentTime evalTime = AssessmentTime.create(
                Direction.COMPUTER_VISION,
                1,
                null,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                true,
                120,
                false);
        RepositoryTestObjects.insert(assessmentTimeMapper, evalTime, AssessmentTimeDO.class);

        // 创建题目 - 单选题
        SingleChoiceContent content = new SingleChoiceContent();
        content.setContent("这是题干");
        content.setOptions(Arrays.asList("A. 选项1", "B. 选项2", "C. 选项3"));
        content.setCorrectAnswer("B");

        AssessmentQuestion question = AssessmentQuestion.create(
                evalTime.getId(),
                1,
                QuestionType.SINGLE_CHOICE,
                "测试单选题",
                content,
                null,
                new BigDecimal("10.00"));

        // 插入
        int insertCount = RepositoryTestObjects.insert(assessmentQuestionMapper, question, AssessmentQuestionDO.class);
        assertEquals(1, insertCount);

        // 查询
        AssessmentQuestion retrieved = RepositoryTestObjects
                .toDomain(assessmentQuestionMapper.selectById(question.getId()), AssessmentQuestion.class);
        assertNotNull(retrieved);
        assertEquals(QuestionType.SINGLE_CHOICE, retrieved.getQuestionType());

        // 验证 JSON 内容
        QuestionContent retrievedContent = retrieved.getContent();
        assertNotNull(retrievedContent);
        assertTrue(retrievedContent instanceof SingleChoiceContent);

        SingleChoiceContent singleChoice = (SingleChoiceContent) retrievedContent;
        assertEquals("这是题干", singleChoice.getContent());
        assertEquals("B", singleChoice.getCorrectAnswer());
        assertEquals(3, singleChoice.getOptions().size());
    }

    @Test
    void testEvaluationQuestionWithAlgorithmContent() {
        // 先创建考核时间
        AssessmentTime evalTime = AssessmentTime.create(
                Direction.EMBEDDED,
                1,
                null,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                false,
                null,
                false);
        RepositoryTestObjects.insert(assessmentTimeMapper, evalTime, AssessmentTimeDO.class);

        AlgorithmContent content = new AlgorithmContent();
        content.setContent("给定一个整数数组，返回两数之和为目标值的索引");

        AlgorithmContent.TestCase testCase = new AlgorithmContent.TestCase();
        testCase.setInput("[2,7,11,15], 9");
        testCase.setExpectedOutput("[0,1]");
        content.setTestCases(Arrays.asList(testCase));

        content.setTimeLimit(1000);
        content.setMemoryLimit(256);

        AssessmentQuestion question = AssessmentQuestion.create(
                evalTime.getId(),
                2,
                QuestionType.ALGORITHM,
                "两数之和",
                content,
                null,
                new BigDecimal("20.00"));

        // 插入并验证
        RepositoryTestObjects.insert(assessmentQuestionMapper, question, AssessmentQuestionDO.class);

        AssessmentQuestion retrieved = RepositoryTestObjects
                .toDomain(assessmentQuestionMapper.selectById(question.getId()), AssessmentQuestion.class);
        assertTrue(retrieved.getContent() instanceof AlgorithmContent);

        AlgorithmContent algorithmContent = (AlgorithmContent) retrieved.getContent();
        assertEquals(1000, algorithmContent.getTimeLimit());
        assertEquals(256, algorithmContent.getMemoryLimit());
        assertEquals(1, algorithmContent.getTestCases().size());
    }

    @Test
    void testEnrollCrud() {
        // 创建学院
        College college = College.create("测试学院");
        RepositoryTestObjects.insert(collegeMapper, college, CollegeDO.class);

        // 创建报名记录
        Enroll enroll = Enroll.reconstruct(
                null,
                "报名测试",
                "202401020001",
                "password123",
                null,
                college.getId(),
                "软件工程",
                null,
                Direction.STRUCTURAL_DESIGN,
                null,
                EnrollStatus.PENDING,
                null,
                null,
                null,
                null,
                null);

        // 插入
        int insertCount = RepositoryTestObjects.insert(enrollMapper, enroll, EnrollDO.class);
        assertEquals(1, insertCount);

        // 查询
        Enroll retrieved = RepositoryTestObjects.toDomain(enrollMapper.selectById(enroll.getId()), Enroll.class);
        assertNotNull(retrieved);
        assertEquals("202401020001", retrieved.getStudentId());
        assertEquals(Direction.STRUCTURAL_DESIGN, retrieved.getDirection());
        assertEquals(EnrollStatus.PENDING, retrieved.getStatus());
    }

    @Test
    void testAllEnumsInEntityContext() {
        // 验证所有枚举都能在实体中正确使用

        // FileType 在 File 实体中
        File file = File.builder().name("test.jpg").type(FileType.AVATAR).url("/uploads/test.jpg").build();

        assertEquals(FileType.AVATAR, file.getType());

        // ExperienceType 在 UserExperience 中
        UserExperience experience = UserExperience.reconstruct(
                null,
                null,
                ExperienceType.COMPETITION,
                "蓝桥杯",
                null,
                null,
                null);

        assertEquals(ExperienceType.COMPETITION, experience.getType());

        // AchievementType 在 Achievement 中
        Achievement achievement = Achievement
                .create("一等奖", AchievementType.COMPETITION, null, null, AwardLevel.NATIONAL, null, null);
        achievement.setType(AchievementType.COMPETITION);

        assertEquals(AchievementType.COMPETITION, achievement.getType());

        // ProgrammingLanguage 在 AssessmentAnswer 中
        AssessmentAnswer answer = AssessmentAnswer
                .create(1L, 1L, "public class Main {}", ProgrammingLanguage.JAVA, null);

        assertEquals(ProgrammingLanguage.JAVA, answer.getLanguage());
    }
}
