package com.bluenet.web.infrastructure.repository.mapper;

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
        College college = new College();
        college.setName("计算机学院");
        collegeMapper.insert(college);

        Role role = roleMapper.selectByName("MEMBER");

        // 创建用户
        User user = new User();
        user.setStudentId("202401010001");
        user.setEmail("test@example.com");
        user.setUsername("测试用户");
        user.setCollegeId(college.getId());
        user.setRoleId(role.getId());
        user.setDirection(Direction.COMPUTER_VISION);
        user.setGender(Gender.MALE);
        user.setDisable(false);

        // 插入
        int insertCount = userMapper.insert(user);
        assertEquals(1, insertCount);
        assertNotNull(user.getId());

        // 查询
        User retrieved = userMapper.selectById(user.getId());
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
        AssessmentTime evalTime = new AssessmentTime();
        evalTime.setDirection(Direction.COMPUTER_VISION);
        evalTime.setEpoch(1);
        evalTime.setStartTime(LocalDateTime.now());
        evalTime.setEndTime(LocalDateTime.now().plusDays(7));
        evalTime.setTimeLimit(true);
        evalTime.setTimeLimitMinutes(120);
        assessmentTimeMapper.insert(evalTime);

        // 创建题目 - 单选题
        AssessmentQuestion question = new AssessmentQuestion();
        question.setAssessmentTimeId(evalTime.getId());
        question.setQuestionNo(1);
        question.setQuestionType(QuestionType.SINGLE_CHOICE);
        question.setTitle("测试单选题");
        question.setScore(new BigDecimal("10.00"));

        SingleChoiceContent content = new SingleChoiceContent();
        content.setContent("这是题干");
        content.setOptions(Arrays.asList("A. 选项1", "B. 选项2", "C. 选项3"));
        content.setCorrectAnswer("B");
        question.setContent(content);

        // 插入
        int insertCount = assessmentQuestionMapper.insert(question);
        assertEquals(1, insertCount);

        // 查询
        AssessmentQuestion retrieved = assessmentQuestionMapper.selectById(question.getId());
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
        AssessmentTime evalTime = new AssessmentTime();
        evalTime.setDirection(Direction.EMBEDDED);
        evalTime.setEpoch(1);
        evalTime.setStartTime(LocalDateTime.now());
        evalTime.setEndTime(LocalDateTime.now().plusDays(7));
        assessmentTimeMapper.insert(evalTime);

        AssessmentQuestion question = new AssessmentQuestion();
        question.setAssessmentTimeId(evalTime.getId());
        question.setQuestionNo(2);
        question.setQuestionType(QuestionType.ALGORITHM);
        question.setTitle("两数之和");
        question.setScore(new BigDecimal("20.00"));

        AlgorithmContent content = new AlgorithmContent();
        content.setContent("给定一个整数数组，返回两数之和为目标值的索引");

        AlgorithmContent.TestCase testCase = new AlgorithmContent.TestCase();
        testCase.setInput("[2,7,11,15], 9");
        testCase.setExpectedOutput("[0,1]");
        content.setTestCases(Arrays.asList(testCase));

        content.setTimeLimit(1000);
        content.setMemoryLimit(256);
        question.setContent(content);

        // 插入并验证
        assessmentQuestionMapper.insert(question);

        AssessmentQuestion retrieved = assessmentQuestionMapper.selectById(question.getId());
        assertTrue(retrieved.getContent() instanceof AlgorithmContent);

        AlgorithmContent algorithmContent = (AlgorithmContent) retrieved.getContent();
        assertEquals(1000, algorithmContent.getTimeLimit());
        assertEquals(256, algorithmContent.getMemoryLimit());
        assertEquals(1, algorithmContent.getTestCases().size());
    }

    @Test
    void testEnrollCrud() {
        // 创建学院
        College college = new College();
        college.setName("测试学院");
        collegeMapper.insert(college);

        // 创建报名记录
        Enroll enroll = new Enroll();
        enroll.setStudentId("202401020001");
        enroll.setUsername("报名测试");
        enroll.setPassword("password123");
        enroll.setCollegeId(college.getId());
        enroll.setMajor("软件工程");
        enroll.setGrade(2024);
        enroll.setDirection(Direction.STRUCTURAL_DESIGN);
        enroll.setStatus(EnrollStatus.PENDING);

        // 插入
        int insertCount = enrollMapper.insert(enroll);
        assertEquals(1, insertCount);

        // 查询
        Enroll retrieved = enrollMapper.selectById(enroll.getId());
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
        UserExperience experience = new UserExperience();
        experience.setType(ExperienceType.COMPETITION);
        experience.setTitle("蓝桥杯");

        assertEquals(ExperienceType.COMPETITION, experience.getType());

        // AchievementType 在 Achievement 中
        Achievement achievement = new Achievement();
        achievement.setTitle("一等奖");
        achievement.setType(AchievementType.COMPETITION);

        assertEquals(AchievementType.COMPETITION, achievement.getType());

        // ImageType 在 IntroduceImage 中
        IntroduceImage image = new IntroduceImage();
        image.setType(ImageType.COMPETITION);
        image.setDescription("竞赛照片");

        assertEquals(ImageType.COMPETITION, image.getType());

        // ProgrammingLanguage 在 AssessmentAnswer 中
        AssessmentAnswer answer = new AssessmentAnswer();
        answer.setLanguage(ProgrammingLanguage.JAVA);
        answer.setContent("public class Main {}");

        assertEquals(ProgrammingLanguage.JAVA, answer.getLanguage());
    }
}
