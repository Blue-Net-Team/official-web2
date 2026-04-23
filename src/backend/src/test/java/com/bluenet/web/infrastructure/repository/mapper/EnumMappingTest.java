package com.bluenet.web.infrastructure.repository.mapper;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.testsupport.RepositoryTestObjects;

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
        College college = College.create("测试学院");
        RepositoryTestObjects.insert(collegeMapper, college, CollegeDO.class);

        // 创建用户并设置枚举值
        User user = User.reconstruct(
                null,
                "202401010001",
                "test@example.com",
                null,
                null,
                "测试用户",
                null,
                college.getId(),
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

        // 保存到数据库
        RepositoryTestObjects.insert(userMapper, user, UserDO.class);

        // 从数据库读取
        User retrieved = RepositoryTestObjects.toDomain(userMapper.selectById(user.getId()), User.class);

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
