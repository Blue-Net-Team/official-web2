package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.entity.UserExperience;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.repository.UserExperienceRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.repository.dataobject.UserExperienceDO;
import com.bluenet.web.infrastructure.repository.mapper.UserExperienceMapper;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserExperienceRepositoryImpl 集成测试。
 */
@DisplayName("UserExperienceRepositoryImpl 集成测试")
class UserExperienceRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserExperienceRepository userExperienceRepository;

    @Autowired
    private UserExperienceMapper userExperienceMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final AtomicLong userCounter = new AtomicLong(1);

    private Long createUser() {
        long seq = userCounter.getAndIncrement();
        String studentId = String.format("2025%05d", seq);
        User user = UserFixture.member(studentId)
                .withGender(Gender.UNKNOWN)
                .save(userRepository, passwordEncoder);
        return user.getId();
    }

    private UserExperience createExperience(Long userId, ExperienceType type, String title) {
        UserExperience experience = UserExperience.create(
                userId,
                type,
                title,
                title + "内容",
                LocalDateTime.now().minusMonths(6),
                LocalDateTime.now());
        userExperienceRepository.save(experience);
        return experience;
    }

    @Test
    @DisplayName("save: 新用户经历应插入并回写ID")
    void save_newExperience_shouldInsertAndReturnId() {
        Long userId = createUser();
        UserExperience experience = createExperience(userId, ExperienceType.INTERNSHIP, "实习经历");

        assertThat(experience.getId()).isNotNull();
        UserExperienceDO dataObject = userExperienceMapper.selectById(experience.getId());
        assertThat(dataObject).isNotNull();
        assertThat(dataObject.getTitle()).isEqualTo("实习经历");
        assertThat(dataObject.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("save: 已有用户经历应更新字段")
    void save_existingExperience_shouldUpdateFields() {
        Long userId = createUser();
        UserExperience experience = createExperience(userId, ExperienceType.PROJECT, "旧项目");
        experience.updateDetails("新项目", "新项目内容", LocalDateTime.now().minusMonths(3), LocalDateTime.now());

        userExperienceRepository.save(experience);

        UserExperienceDO updated = userExperienceMapper.selectById(experience.getId());
        assertThat(updated.getTitle()).isEqualTo("新项目");
        assertThat(updated.getContent()).isEqualTo("新项目内容");
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        Long userId = createUser();
        UserExperience experience = createExperience(userId, ExperienceType.INTERNSHIP, "实习经历");

        Optional<UserExperience> found = userExperienceRepository.findById(experience.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("实习经历");

        assertThat(userExperienceRepository.findById(-1L)).isEmpty();
    }

    @Test
    @DisplayName("findByUserId: 应按用户ID查询经历列表")
    void findByUserId_shouldReturnExperiences() {
        Long userId1 = createUser();
        Long userId2 = createUser();
        UserExperience experience1 = createExperience(userId1, ExperienceType.INTERNSHIP, "用户1实习");
        createExperience(userId2, ExperienceType.PROJECT, "用户2项目");

        List<UserExperience> experiences = userExperienceRepository.findByUserId(userId1);

        assertThat(experiences)
                .extracting(UserExperience::getId)
                .containsExactly(experience1.getId());
    }

    @Test
    @DisplayName("findByUserIdAndType: 应按用户ID和类型查询经历")
    void findByUserIdAndType_shouldReturnTypedExperiences() {
        Long userId = createUser();
        UserExperience internship = createExperience(userId, ExperienceType.INTERNSHIP, "实习1");
        createExperience(userId, ExperienceType.PROJECT, "项目1");

        List<UserExperience> experiences = userExperienceRepository
                .findByUserIdAndType(userId, ExperienceType.INTERNSHIP);

        assertThat(experiences)
                .extracting(UserExperience::getId)
                .containsExactly(internship.getId());
    }

    @Test
    @DisplayName("countByUserIdAndType: 应统计指定类型经历数量")
    void countByUserIdAndType_shouldCount() {
        Long userId = createUser();
        createExperience(userId, ExperienceType.INTERNSHIP, "实习1");
        createExperience(userId, ExperienceType.INTERNSHIP, "实习2");
        createExperience(userId, ExperienceType.PROJECT, "项目1");

        int internshipCount = userExperienceRepository.countByUserIdAndType(userId, ExperienceType.INTERNSHIP);
        int projectCount = userExperienceRepository.countByUserIdAndType(userId, ExperienceType.PROJECT);

        assertThat(internshipCount).isEqualTo(2);
        assertThat(projectCount).isEqualTo(1);
    }

    @Test
    @DisplayName("checkOwner: 应正确判断经历归属")
    void checkOwner_shouldWork() {
        Long userId = createUser();
        Long otherUserId = createUser();
        UserExperience experience = createExperience(userId, ExperienceType.INTERNSHIP, "实习");

        assertThat(userExperienceRepository.checkOwner(experience.getId(), userId)).isTrue();
        assertThat(userExperienceRepository.checkOwner(experience.getId(), otherUserId)).isFalse();
        assertThat(userExperienceRepository.checkOwner(-1L, userId)).isFalse();
    }

    @Test
    @DisplayName("deleteById: 应删除用户经历")
    void deleteById_shouldRemoveExperience() {
        Long userId = createUser();
        UserExperience experience = createExperience(userId, ExperienceType.INTERNSHIP, "待删除");
        Long experienceId = experience.getId();

        userExperienceRepository.deleteById(experienceId);

        assertThat(userExperienceMapper.selectById(experienceId)).isNull();
    }
}
