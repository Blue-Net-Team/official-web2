package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.repository.dataobject.FileDO;
import com.bluenet.web.infrastructure.repository.dataobject.RoleDO;
import com.bluenet.web.infrastructure.repository.dataobject.UserDO;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserRepositoryImpl 集成测试。
 * <p>
 * 验证实体驱动改造后的仓储行为：save/saveAll、Optional 查询、GitHub 绑定、级联删除、分页。
 * </p>
 */
@DisplayName("UserRepositoryImpl 集成测试")
class UserRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private FileMapper fileMapper;

    private User createUser(String studentId) {
        User user = User.create(
                studentId,
                studentId + "@example.com",
                null,
                "encodedPassword",
                "用户" + studentId,
                "昵称" + studentId,
                null,
                "专业",
                2024,
                Direction.COMPUTER_VISION,
                Gender.MALE,
                "开发",
                null,
                null,
                null,
                null,
                "REF" + studentId.substring(studentId.length() - 5),
                "bio");
        userRepository.save(user);
        return user;
    }

    @Test
    @DisplayName("save: 新用户应插入并回写ID")
    void save_newUser_shouldInsertAndReturnId() {
        User user = createUser("2024001001");

        assertNotNull(user.getId());
        UserDO dataObject = userMapper.selectById(user.getId());
        assertNotNull(dataObject);
        assertEquals("2024001001", dataObject.getStudentId());
        assertEquals("用户2024001001", dataObject.getUsername());
    }

    @Test
    @DisplayName("save: 已有用户应更新字段")
    void save_existingUser_shouldUpdateFields() {
        User user = createUser("2024001002");
        user.changeEmail("new@example.com");
        user.updateAvatar(123L);

        userRepository.save(user);

        UserDO updated = userMapper.selectById(user.getId());
        assertEquals("new@example.com", updated.getEmail());
        assertEquals(123L, updated.getAvatarId());
    }

    @Test
    @DisplayName("saveAll: 混合插入和更新应全部生效")
    void saveAll_mixedInsertAndUpdate_shouldPersistAll() {
        User existing = createUser("2024001003");
        existing.setNickname("旧昵称-已更新");
        User newUser = User.create(
                "2024001004",
                "2024001004@example.com",
                null,
                "pwd",
                "新用户",
                "新昵称",
                null,
                "专业",
                2024,
                Direction.STRUCTURAL_DESIGN,
                Gender.FEMALE,
                "测试",
                null,
                null,
                null,
                null,
                "REF00004",
                "bio");

        userRepository.saveAll(List.of(existing, newUser));

        assertNotNull(newUser.getId());
        UserDO existingDO = userMapper.selectById(existing.getId());
        assertEquals("旧昵称-已更新", existingDO.getNickname());
        UserDO newDO = userMapper.selectById(newUser.getId());
        assertEquals("2024001004", newDO.getStudentId());
        assertEquals(Direction.STRUCTURAL_DESIGN, newDO.getDirection());
    }

    @Test
    @DisplayName("saveAll: 空列表应直接返回")
    void saveAll_emptyList_shouldDoNothing() {
        assertDoesNotThrow(() -> userRepository.saveAll(List.of()));
        assertDoesNotThrow(() -> userRepository.saveAll(null));
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        User user = createUser("2024001005");

        Optional<User> found = userRepository.findById(user.getId());
        assertTrue(found.isPresent());
        assertEquals(user.getStudentId(), found.get().getStudentId());

        Optional<User> notFound = userRepository.findById(-1L);
        assertTrue(notFound.isEmpty());
    }

    @Test
    @DisplayName("findByEmail: 应正确查询")
    void findByEmail_shouldReturnUser() {
        User user = createUser("2024001006");

        Optional<User> found = userRepository.findByEmail(user.getEmail());
        assertTrue(found.isPresent());
        assertEquals(user.getId(), found.get().getId());

        assertTrue(userRepository.findByEmail("not-exist@example.com").isEmpty());
    }

    @Test
    @DisplayName("findByStudentId: 应正确查询")
    void findByStudentId_shouldReturnUser() {
        User user = createUser("2024001007");

        Optional<User> found = userRepository.findByStudentId(user.getStudentId());
        assertTrue(found.isPresent());
        assertEquals(user.getId(), found.get().getId());

        assertTrue(userRepository.findByStudentId("9999999999").isEmpty());
    }

    @Test
    @DisplayName("updateGithubBinding / clearGithubBinding: 应正确绑定和清除")
    void githubBinding_shouldBindAndClear() {
        User user = createUser("2024001008");

        userRepository.updateGithubBinding(user.getId(), "github-1", "github-user");

        UserDO bound = userMapper.selectById(user.getId());
        assertEquals("github-1", bound.getGithubId());
        assertEquals("github-user", bound.getGithubUsername());

        Optional<User> foundByGithub = userRepository.findByGithubId("github-1");
        assertTrue(foundByGithub.isPresent());
        assertEquals(user.getId(), foundByGithub.get().getId());

        userRepository.clearGithubBinding(user.getId());

        UserDO cleared = userMapper.selectById(user.getId());
        assertNull(cleared.getGithubId());
        assertNull(cleared.getGithubUsername());
    }

    @Test
    @DisplayName("existsByInternalReferralCode: 应正确判断")
    void existsByInternalReferralCode_shouldWork() {
        User user = createUser("2024001009");

        assertTrue(userRepository.existsByInternalReferralCode(user.getInternalReferralCode()));
        assertFalse(userRepository.existsByInternalReferralCode("NOTEXIST"));
    }

    @Test
    @DisplayName("findPage: 应按角色和方向筛选")
    void findPage_shouldFilterAndPaginate() {
        RoleDO role = RoleDO.builder().name("TEST_ROLE").build();
        roleMapper.insert(role);

        User user1 = createUser("2024001010");
        user1.setRoleId(role.getId());
        user1.setDirection(Direction.COMPUTER_VISION);
        userRepository.save(user1);

        User user2 = createUser("2024001011");
        user2.setRoleId(role.getId());
        user2.setDirection(Direction.STRUCTURAL_DESIGN);
        userRepository.save(user2);

        Page<User> page = userRepository.findPage(
                PageRequest.of(0, 10),
                role.getId(),
                Direction.COMPUTER_VISION,
                null,
                null);

        assertEquals(1, page.getTotalElements());
        assertEquals(user1.getId(), page.getContent().get(0).getId());
    }

    @Test
    @DisplayName("deleteByIdWithCascade: 应删除用户并忽略不存在的头像")
    void deleteByIdWithCascade_shouldRemoveUser() {
        User user = createUser("2024001012");
        Long userId = user.getId();

        userRepository.deleteByIdWithCascade(userId);

        assertNull(userMapper.selectById(userId));
    }

    @Test
    @DisplayName("deleteByIdWithCascade: 存在头像时应尝试删除")
    void deleteByIdWithCascade_withAvatar_shouldRemoveUserAndAvatarRecord() {
        FileDO avatar = FileDO.builder()
                .name("avatar-test.png")
                .type(com.bluenet.web.domain.model.enumerate.FileType.AVATAR)
                .url("http://example.com/avatar-test.png")
                .status(com.bluenet.web.domain.model.enumerate.FileStatus.ACTIVE)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        fileMapper.insert(avatar);

        User user = createUser("2024001013");
        user.updateAvatar(avatar.getId());
        userRepository.save(user);

        // 对象存储中无真实对象，Repository 会捕获异常并记录 warn，用户仍可被删除
        assertDoesNotThrow(() -> userRepository.deleteByIdWithCascade(user.getId()));
        assertNull(userMapper.selectById(user.getId()));
    }

    @Test
    @DisplayName("getStatistics: 应统计关联表数量")
    void getStatistics_shouldCountAssociations() {
        User user = createUser("2024001014");

        UserRepository.UserStatistics stats = userRepository.getStatistics(user.getId());

        assertEquals(0L, stats.experienceCount());
        assertEquals(0L, stats.achievementCount());
        assertEquals(0L, stats.answerCount());
        assertEquals(0L, stats.commentCount());
    }
}
