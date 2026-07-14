package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.Member;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.MemberRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MemberRepositoryImpl 集成测试。
 */
@DisplayName("MemberRepositoryImpl 集成测试")
class MemberRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final AtomicLong counter = new AtomicLong(1);

    private User createUser(String studentId, RoleType roleType, Direction direction) {
        User user = UserFixture.builder()
                .withStudentId(studentId)
                .withRoleType(roleType)
                .withDirection(direction)
                .withGender(Gender.MALE)
                .build();
        userRepository.save(user);
        return user;
    }

    private String nextStudentId() {
        return String.format("2025%05d", counter.getAndIncrement());
    }

    @Test
    @DisplayName("findById: 存在成员返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        User user = createUser(nextStudentId(), RoleType.MEMBER, Direction.COMPUTER_VISION);

        Optional<Member> found = memberRepository.findById(user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(user.getUsername());

        assertThat(memberRepository.findById(-1L)).isEmpty();
    }

    @Test
    @DisplayName("findById: 禁用账号不应返回")
    void findById_disabledUser_shouldReturnEmpty() {
        User user = createUser(nextStudentId(), RoleType.MEMBER, Direction.COMPUTER_VISION);
        user.setDisable(true);
        userRepository.save(user);

        Optional<Member> found = memberRepository.findById(user.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findAll: 应按方向和角色分页返回成员")
    void findAll_shouldFilterByDirection() {
        User member1 = createUser(nextStudentId(), RoleType.MEMBER, Direction.COMPUTER_VISION);
        createUser(nextStudentId(), RoleType.MEMBER, Direction.EMBEDDED);
        createUser(nextStudentId(), RoleType.CANDIDATE, Direction.COMPUTER_VISION);

        Page<Member> page = memberRepository.findAll(Direction.COMPUTER_VISION, PageRequest.of(0, 10));

        assertThat(page.getContent())
                .extracting(Member::getId)
                .contains(member1.getId());
        assertThat(page.getContent())
                .noneMatch(m -> m.getDirection() != Direction.COMPUTER_VISION);
    }

    @Test
    @DisplayName("findDirectionLeaders: 应返回方向负责人")
    void findDirectionLeaders_shouldReturnLeaders() {
        User directionAdmin = createUser(nextStudentId(), RoleType.DIRECTION_ADMIN, Direction.STRUCTURAL_DESIGN);
        createUser(nextStudentId(), RoleType.MEMBER, Direction.COMPUTER_VISION);

        List<Member> leaders = memberRepository.findDirectionLeaders();

        assertThat(leaders)
                .extracting(Member::getId)
                .contains(directionAdmin.getId());
    }
}
