package com.bluenet.web.application.command.enroll;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.enumerate.Gender;

/**
 * 报名聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class EnrollCommands {

    /** 禁止实例化。 */
    private EnrollCommands() {
    }

    /**
     * 创建报名命令。
     * <p>
     * 用于创建新的报名信息。
     * </p>
     */
    public record CreateEnrollmentCommand(
            /** 用户名 */
            String username,
            /** 学号 */
            String studentId,
            /** 邮箱 */
            String email,
            /** 学院ID */
            Long collegeId,
            /** 专业 */
            String major,
            /** 性别 */
            Gender gender,
            /** 方向 */
            Direction direction,
            /** 头像ID */
            Long avatarId,
            /** 自我介绍 */
            String introduction,
            /** 内推码 */
            String internalReferralCode,
            /** 是否强制更新 */
            Boolean forceUpdate) {
        public CreateEnrollmentCommand {
            if (username != null) {
                username = username.trim();
            }
            if (studentId != null) {
                studentId = studentId.trim();
            }
            if (email != null) {
                email = email.trim();
            }
            if (introduction != null) {
                introduction = introduction.trim();
            }
            if (internalReferralCode != null) {
                internalReferralCode = internalReferralCode.trim();
            }
        }
    }

    /**
     * 更新报名命令。
     * <p>
     * 用于更新已有的报名信息。
     * </p>
     */
    public record UpdateEnrollmentCommand(
            /** 学号 */
            String studentId,
            /** 用户名 */
            String username,
            /** 邮箱 */
            String email,
            /** 学院ID */
            Long collegeId,
            /** 专业 */
            String major,
            /** 性别 */
            Gender gender,
            /** 方向 */
            Direction direction,
            /** 头像ID */
            Long avatarId,
            /** 自我介绍 */
            String introduction,
            /** 内推码 */
            String internalReferralCode) {
        public UpdateEnrollmentCommand {
            if (username != null) {
                username = username.trim();
            }
            if (studentId != null) {
                studentId = studentId.trim();
            }
            if (email != null) {
                email = email.trim();
            }
            if (introduction != null) {
                introduction = introduction.trim();
            }
            if (internalReferralCode != null) {
                internalReferralCode = internalReferralCode.trim();
            }
        }
    }

    /**
     * 查询报名列表命令。
     * <p>
     * 用于分页查询报名列表。
     * </p>
     */
    public record GetEnrollmentListCommand(
            /** 页码 */
            Integer page,
            /** 每页大小 */
            Integer size,
            /** 关键词 */
            String keyword,
            /** 状态 */
            EnrollStatus status,
            /** 方向 */
            Direction direction) {
        public GetEnrollmentListCommand {
            if (keyword != null) {
                keyword = keyword.trim();
            }
        }
    }

    /**
     * 审核报名命令。
     * <p>
     * 用于审核通过报名。
     * </p>
     */
    public record ApproveEnrollmentCommand(
            /** 考核年级年份 */
            Integer assessmentGradeYear) {
    }

    /**
     * 拒绝报名命令。
     * <p>
     * 用于拒绝报名申请。
     * </p>
     */
    public record RejectEnrollmentCommand(
            /** 原因 */
            String reason) {
        public RejectEnrollmentCommand {
            if (reason != null) {
                reason = reason.trim();
            }
        }
    }
}
