package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.RoleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Member 领域实体单元测试。
 */
@DisplayName("Member 领域实体测试")
class MemberTest {

    @Test
    @DisplayName("create: 应创建新成员")
    void create_shouldCreateMember() {
        Member member = Member.create(
                "2024001001",
                "  张三  ",
                "昵称",
                Direction.COMPUTER_VISION,
                "开发",
                1L,
                "计算机学院",
                "软件工程",
                Gender.MALE,
                RoleType.MEMBER,
                "成员",
                "简介",
                "github",
                10L,
                2024,
                2025);

        assertThat(member.getId()).isNull();
        assertThat(member.getStudentId()).isEqualTo("2024001001");
        assertThat(member.getUsername()).isEqualTo("张三");
        assertThat(member.getNickname()).isEqualTo("昵称");
        assertThat(member.getDirection()).isEqualTo(Direction.COMPUTER_VISION);
        assertThat(member.getJob()).isEqualTo("开发");
        assertThat(member.getAvatarFileId()).isEqualTo(1L);
        assertThat(member.getCollege()).isEqualTo("计算机学院");
        assertThat(member.getMajor()).isEqualTo("软件工程");
        assertThat(member.getGender()).isEqualTo(Gender.MALE);
        assertThat(member.getRole()).isEqualTo(RoleType.MEMBER);
        assertThat(member.getRoleName()).isEqualTo("成员");
        assertThat(member.getBio()).isEqualTo("简介");
        assertThat(member.getGithubUsername()).isEqualTo("github");
        assertThat(member.getWechatQrcode()).isEqualTo(10L);
        assertThat(member.getEnrollmentYear()).isEqualTo(2024);
        assertThat(member.getAssessmentGradeYear()).isEqualTo(2025);
    }

    @Test
    @DisplayName("create: 用户名为空应抛异常")
    void create_withBlankUsername_shouldThrow() {
        assertThatThrownBy(
                () -> Member.create(
                        "2024001001",
                        "   ",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("用户名不能为空");
    }

    @Test
    @DisplayName("isTeamMember: 角色至少为 MEMBER 时应返回 true")
    void isTeamMember_withMemberOrAbove_shouldReturnTrue() {
        Member member = Member.create(
                "2024001001",
                "张三",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                RoleType.MEMBER,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThat(member.isTeamMember()).isTrue();
    }

    @Test
    @DisplayName("isTeamMember: 角色低于 MEMBER 时应返回 false")
    void isTeamMember_withCandidate_shouldReturnFalse() {
        Member member = Member.create(
                "2024001001",
                "张三",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                RoleType.CANDIDATE,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThat(member.isTeamMember()).isFalse();
    }

    @Test
    @DisplayName("isTeamMember: 角色为空时应返回 false")
    void isTeamMember_withNullRole_shouldReturnFalse() {
        Member member = Member.create(
                "2024001001",
                "张三",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThat(member.isTeamMember()).isFalse();
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        Member member = Member.reconstruct(
                100L,
                "2024001001",
                "张三",
                "昵称",
                Direction.EMBEDDED,
                "开发",
                1L,
                "计算机学院",
                "软件工程",
                Gender.FEMALE,
                RoleType.DIRECTION_ADMIN,
                "方向管理员",
                "简介",
                "github",
                10L,
                2024,
                2025);

        assertThat(member.getId()).isEqualTo(100L);
        assertThat(member.getStudentId()).isEqualTo("2024001001");
        assertThat(member.getUsername()).isEqualTo("张三");
        assertThat(member.getNickname()).isEqualTo("昵称");
        assertThat(member.getDirection()).isEqualTo(Direction.EMBEDDED);
        assertThat(member.getJob()).isEqualTo("开发");
        assertThat(member.getAvatarFileId()).isEqualTo(1L);
        assertThat(member.getCollege()).isEqualTo("计算机学院");
        assertThat(member.getMajor()).isEqualTo("软件工程");
        assertThat(member.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(member.getRole()).isEqualTo(RoleType.DIRECTION_ADMIN);
        assertThat(member.getRoleName()).isEqualTo("方向管理员");
        assertThat(member.getBio()).isEqualTo("简介");
        assertThat(member.getGithubUsername()).isEqualTo("github");
        assertThat(member.getWechatQrcode()).isEqualTo(10L);
        assertThat(member.getEnrollmentYear()).isEqualTo(2024);
        assertThat(member.getAssessmentGradeYear()).isEqualTo(2025);
    }
}
