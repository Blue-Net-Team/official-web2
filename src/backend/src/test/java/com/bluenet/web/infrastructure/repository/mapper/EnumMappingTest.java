package com.bluenet.web.infrastructure.repository.mapper;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 枚举映射集成测试 验证枚举与数据库的正确映射
 */
class EnumMappingTest extends BaseIntegrationTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CollegeMapper collegeMapper;

    @Test
    void testDirectionEnumMapping() {
        // 创建学院
        College college = new College();
        college.setName("测试学院");
        collegeMapper.insert(college);

        // 创建用户并设置枚举值
        User user = new User();
        user.setStudentId("202401010001");
        user.setUsername("测试用户");
        user.setEmail("test@example.com");
        user.setCollegeId(college.getId());
        user.setDirection(Direction.COMPUTER_VISION);
        user.setDisable(false);

        // 保存到数据库
        userMapper.insert(user);

        // 从数据库读取
        User retrieved = userMapper.selectById(user.getId());

        // 验证枚举正确映射
        assertEquals(Direction.COMPUTER_VISION, retrieved.getDirection());
        assertEquals("computer_vision", retrieved.getDirection().getValue());
    }

    @Test
    void testAllDirectionEnumValues() {
        assertEquals("computer_vision", Direction.COMPUTER_VISION.getValue());
        assertEquals("structural_design", Direction.STRUCTURAL_DESIGN.getValue());
        assertEquals("embedded", Direction.EMBEDDED.getValue());
    }

    @Test
    void testFileTypeEnumValues() {
        assertEquals("avatar", FileType.AVATAR.getValue());
        assertEquals("normal-img", FileType.NORMAL_IMG.getValue());
        assertEquals("assessment-attachment", FileType.ASSESSMENT_ATTACHMENT.getValue());
        assertEquals("work", FileType.WORK.getValue());
        assertEquals("qrcode", FileType.QRCODE.getValue());
    }

    @Test
    void testExperienceTypeEnumValues() {
        assertEquals("competition", ExperienceType.COMPETITION.getValue());
        assertEquals("project", ExperienceType.PROJECT.getValue());
        assertEquals("internship", ExperienceType.INTERNSHIP.getValue());
    }

    @Test
    void testAchievementTypeEnumValues() {
        assertEquals("paper", AchievementType.PAPER.getValue());
        assertEquals("patent", AchievementType.PATENT.getValue());
        assertEquals("competition", AchievementType.COMPETITION.getValue());
    }

    @Test
    void testEnrollStatusEnumValues() {
        assertEquals("pending", EnrollStatus.PENDING.getValue());
        assertEquals("approved", EnrollStatus.APPROVED.getValue());
        assertEquals("rejected", EnrollStatus.REJECTED.getValue());
    }

    @Test
    void testImageTypeEnumValues() {
        assertEquals("competition", ImageType.COMPETITION.getValue());
    }

    @Test
    void testQuestionTypeEnumValues() {
        assertEquals("single_choice", QuestionType.SINGLE_CHOICE.getValue());
        assertEquals("multiple_choice", QuestionType.MULTIPLE_CHOICE.getValue());
        assertEquals("file_upload", QuestionType.FILE_UPLOAD.getValue());
        assertEquals("algorithm", QuestionType.ALGORITHM.getValue());
    }

    @Test
    void testProgrammingLanguageEnumValues() {
        assertEquals("python", ProgrammingLanguage.PYTHON.getValue());
        assertEquals("c", ProgrammingLanguage.C.getValue());
        assertEquals("cpp", ProgrammingLanguage.CPP.getValue());
        assertEquals("java", ProgrammingLanguage.JAVA.getValue());
        assertEquals("javascript", ProgrammingLanguage.JAVASCRIPT.getValue());
    }
}
