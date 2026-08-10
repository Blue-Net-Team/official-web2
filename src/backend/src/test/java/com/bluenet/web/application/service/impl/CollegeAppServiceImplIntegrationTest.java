package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.college.CollegeCommands;
import com.bluenet.web.application.result.college.CollegeResult;
import com.bluenet.web.application.service.CollegeAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.Enroll;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.domain.repository.EnrollRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testsupport.fixture.CollegeFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CollegeAppServiceImpl 集成测试。
 *
 * <p>
 * 验证学院应用服务的查询、创建、更新、删除逻辑，以及删除前关联数据的约束校验。
 * </p>
 */
@DisplayName("CollegeAppServiceImpl 集成测试")
class CollegeAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CollegeAppService collegeAppService;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollRepository enrollRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void cleanupSecurityContext() {
        UserCTX.clear();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getAllColleges: 应返回所有 persisted colleges")
    void getAllColleges_shouldReturnAllPersistedColleges() {
        CollegeFixture.saveCollege(collegeRepository, "软件学院");
        CollegeFixture.saveCollege(collegeRepository, "电子学院");

        List<CollegeResult> result = collegeAppService.getAllColleges();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(CollegeResult::name)
                .containsExactly("软件学院", "电子学院");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createCollege: 应创建学院并持久化")
    void createCollege_shouldCreateAndPersist() {
        CollegeCommands.CreateCollegeCommand command = new CollegeCommands.CreateCollegeCommand("新媒体学院");

        CollegeResult result = collegeAppService.createCollege(command);

        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("新媒体学院");
        assertThat(collegeRepository.findById(result.id()))
                .isPresent()
                .hasValueSatisfying(college -> assertThat(college.getName()).isEqualTo("新媒体学院"));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createCollege: 重复名称应抛 IllegalArgumentException")
    void createCollege_duplicateName_shouldThrowIllegalArgument() {
        CollegeFixture.saveCollege(collegeRepository, "新媒体学院");
        CollegeCommands.CreateCollegeCommand command = new CollegeCommands.CreateCollegeCommand("新媒体学院");

        assertThatThrownBy(() -> collegeAppService.createCollege(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("学院名称已存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateCollege: 应重命名并持久化")
    void updateCollege_shouldRenameAndPersist() {
        College college = CollegeFixture.saveCollege(collegeRepository, "旧学院名");
        CollegeCommands.UpdateCollegeCommand command = new CollegeCommands.UpdateCollegeCommand(college.getId(),
                "新学院名");

        CollegeResult result = collegeAppService.updateCollege(command);

        assertThat(result.name()).isEqualTo("新学院名");
        assertThat(collegeRepository.findById(college.getId()))
                .isPresent()
                .hasValueSatisfying(updated -> assertThat(updated.getName()).isEqualTo("新学院名"));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateCollege: 不存在的 id 应抛 DataNotFound")
    void updateCollege_notFound_shouldThrowDataNotFound() {
        CollegeCommands.UpdateCollegeCommand command = new CollegeCommands.UpdateCollegeCommand(99999L, "任意学院");

        assertThatThrownBy(() -> collegeAppService.updateCollege(command))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("学院不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateCollege: 与其他学院重复名称应抛 IllegalArgumentException")
    void updateCollege_duplicateName_shouldThrowIllegalArgument() {
        College collegeA = CollegeFixture.saveCollege(collegeRepository, "学院A");
        CollegeFixture.saveCollege(collegeRepository, "学院B");
        CollegeCommands.UpdateCollegeCommand command = new CollegeCommands.UpdateCollegeCommand(collegeA.getId(),
                "学院B");

        assertThatThrownBy(() -> collegeAppService.updateCollege(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("学院名称已存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("deleteCollege: 应删除学院")
    void deleteCollege_shouldDelete() {
        College college = CollegeFixture.saveCollege(collegeRepository, "待删除学院");

        collegeAppService.deleteCollege(college.getId());

        assertThat(collegeRepository.findById(college.getId())).isEmpty();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("deleteCollege: 不存在的 id 应抛 DataNotFound")
    void deleteCollege_notFound_shouldThrowDataNotFound() {
        assertThatThrownBy(() -> collegeAppService.deleteCollege(99999L))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("学院不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("deleteCollege: 存在关联用户时应抛 IllegalArgumentException")
    void deleteCollege_withAssociatedUser_shouldThrowIllegalArgument() {
        College college = CollegeFixture.saveCollege(collegeRepository, "关联用户学院");
        UserFixture.member("2024001002")
                .withCollegeId(college.getId())
                .save(userRepository, passwordEncoder);

        assertThatThrownBy(() -> collegeAppService.deleteCollege(college.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("该学院下存在关联用户，无法删除");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("deleteCollege: 存在关联报名记录时应抛 IllegalArgumentException")
    void deleteCollege_withAssociatedEnroll_shouldThrowIllegalArgument() {
        College college = CollegeFixture.saveCollege(collegeRepository, "关联报名学院");
        Enroll enroll = Enroll.create(
                "报名用户",
                "2024002001",
                "password",
                "REF00001",
                college.getId(),
                "软件工程",
                Gender.MALE,
                Direction.COMPUTER_VISION,
                null,
                "2024002001@example.com",
                "自我介绍");
        enrollRepository.save(enroll);

        assertThatThrownBy(() -> collegeAppService.deleteCollege(college.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("该学院下存在关联报名记录，无法删除");
    }
}
