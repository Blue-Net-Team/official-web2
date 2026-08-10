package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.enumerate.Gender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Enroll 领域实体单元测试。
 */
@DisplayName("Enroll 领域实体测试")
class EnrollTest {

    @Test
    @DisplayName("create: 应创建待审核报名")
    void create_shouldCreatePendingEnroll() {
        Enroll enroll = Enroll.create(
                "  张三  ",
                "  2024001001  ",
                "encodedPassword",
                "REFCODE",
                1L,
                "计算机科学与技术",
                Gender.MALE,
                Direction.COMPUTER_VISION,
                100L,
                "zhangsan@example.com",
                "自我介绍");

        assertThat(enroll.getId()).isNull();
        assertThat(enroll.getUsername()).isEqualTo("张三");
        assertThat(enroll.getStudentId()).isEqualTo("  2024001001  ");
        assertThat(enroll.getPassword()).isEqualTo("encodedPassword");
        assertThat(enroll.getInternalReferralCode()).isEqualTo("REFCODE");
        assertThat(enroll.getCollegeId()).isEqualTo(1L);
        assertThat(enroll.getMajor()).isEqualTo("计算机科学与技术");
        assertThat(enroll.getGender()).isEqualTo(Gender.MALE);
        assertThat(enroll.getDirection()).isEqualTo(Direction.COMPUTER_VISION);
        assertThat(enroll.getAvatarId()).isEqualTo(100L);
        assertThat(enroll.getStatus()).isEqualTo(EnrollStatus.PENDING);
        assertThat(enroll.getEmail()).isEqualTo("zhangsan@example.com");
        assertThat(enroll.getIntroduction()).isEqualTo("自我介绍");
        assertThat(enroll.getCollegeName()).isNull();
        assertThat(enroll.getReferralUserId()).isNull();
        assertThat(enroll.getReferralUserName()).isNull();
    }

    @Test
    @DisplayName("create: 用户名为空应抛异常")
    void create_withBlankUsername_shouldThrow() {
        assertThatThrownBy(
                () -> Enroll.create(
                        "   ",
                        "2024001001",
                        "pwd",
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
    @DisplayName("create: 学号为空应抛异常")
    void create_withBlankStudentId_shouldThrow() {
        assertThatThrownBy(
                () -> Enroll.create(
                        "张三",
                        null,
                        "pwd",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("学号不能为空");
    }

    @Test
    @DisplayName("updateInfo: 待审核状态应允许更新")
    void updateInfo_withPendingStatus_shouldUpdateInfo() {
        Enroll enroll = Enroll.create("张三", "2024001001", "pwd", null, null, null, null, null, null, null, null);

        enroll.updateInfo(
                "李四",
                "2024001002",
                2L,
                "软件工程",
                Gender.FEMALE,
                Direction.EMBEDDED,
                200L,
                "lisi@example.com",
                "新介绍",
                "NEWCODE",
                "newPwd");

        assertThat(enroll.getUsername()).isEqualTo("李四");
        assertThat(enroll.getStudentId()).isEqualTo("2024001002");
        assertThat(enroll.getCollegeId()).isEqualTo(2L);
        assertThat(enroll.getMajor()).isEqualTo("软件工程");
        assertThat(enroll.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(enroll.getDirection()).isEqualTo(Direction.EMBEDDED);
        assertThat(enroll.getAvatarId()).isEqualTo(200L);
        assertThat(enroll.getEmail()).isEqualTo("lisi@example.com");
        assertThat(enroll.getIntroduction()).isEqualTo("新介绍");
        assertThat(enroll.getInternalReferralCode()).isEqualTo("NEWCODE");
        assertThat(enroll.getPassword()).isEqualTo("newPwd");
    }

    @Test
    @DisplayName("updateInfo: 非待审核状态应抛异常")
    void updateInfo_withNonPendingStatus_shouldThrow() {
        Enroll enroll = Enroll.create("张三", "2024001001", "pwd", null, null, null, null, null, null, null, null);
        enroll.approve();

        assertThatThrownBy(
                () -> enroll.updateInfo(
                        "李四",
                        "2024001002",
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
                                .hasMessageContaining("报名已审核");
    }

    @Test
    @DisplayName("approve: 待审核报名应变为已通过")
    void approve_withPendingStatus_shouldSetApproved() {
        Enroll enroll = Enroll.create("张三", "2024001001", "pwd", null, null, null, null, null, null, null, null);

        enroll.approve();

        assertThat(enroll.getStatus()).isEqualTo(EnrollStatus.APPROVED);
    }

    @Test
    @DisplayName("approve: 非待审核报名应抛异常")
    void approve_withNonPendingStatus_shouldThrow() {
        Enroll enroll = Enroll.create("张三", "2024001001", "pwd", null, null, null, null, null, null, null, null);
        enroll.reject();

        assertThatThrownBy(enroll::approve)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("只能审核待审核状态");
    }

    @Test
    @DisplayName("reject: 待审核报名应变为已拒绝")
    void reject_withPendingStatus_shouldSetRejected() {
        Enroll enroll = Enroll.create("张三", "2024001001", "pwd", null, null, null, null, null, null, null, null);

        enroll.reject();

        assertThat(enroll.getStatus()).isEqualTo(EnrollStatus.REJECTED);
    }

    @Test
    @DisplayName("reject: 非待审核报名应抛异常")
    void reject_withNonPendingStatus_shouldThrow() {
        Enroll enroll = Enroll.create("张三", "2024001001", "pwd", null, null, null, null, null, null, null, null);
        enroll.approve();

        assertThatThrownBy(enroll::reject)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("只能审核待审核状态");
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        Enroll enroll = Enroll.reconstruct(
                100L,
                "张三",
                "2024001001",
                "pwd",
                "REF",
                1L,
                "计算机",
                Gender.MALE,
                Direction.COMPUTER_VISION,
                10L,
                EnrollStatus.APPROVED,
                "email",
                "介绍",
                "学院名称",
                20L,
                "推荐人");

        assertThat(enroll.getId()).isEqualTo(100L);
        assertThat(enroll.getUsername()).isEqualTo("张三");
        assertThat(enroll.getStudentId()).isEqualTo("2024001001");
        assertThat(enroll.getPassword()).isEqualTo("pwd");
        assertThat(enroll.getInternalReferralCode()).isEqualTo("REF");
        assertThat(enroll.getCollegeId()).isEqualTo(1L);
        assertThat(enroll.getMajor()).isEqualTo("计算机");
        assertThat(enroll.getGender()).isEqualTo(Gender.MALE);
        assertThat(enroll.getDirection()).isEqualTo(Direction.COMPUTER_VISION);
        assertThat(enroll.getAvatarId()).isEqualTo(10L);
        assertThat(enroll.getStatus()).isEqualTo(EnrollStatus.APPROVED);
        assertThat(enroll.getEmail()).isEqualTo("email");
        assertThat(enroll.getIntroduction()).isEqualTo("介绍");
        assertThat(enroll.getCollegeName()).isEqualTo("学院名称");
        assertThat(enroll.getReferralUserId()).isEqualTo(20L);
        assertThat(enroll.getReferralUserName()).isEqualTo("推荐人");
    }
}
