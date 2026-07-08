package com.bluenet.web.application.result.enroll;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.enumerate.Gender;

import java.util.Map;

/**
 * 报名聚合的应用层结果对象。
 * <p>
 * 封装了报名相关操作返回给 API 层的数据。
 * </p>
 */
public final class EnrollResult {

    private EnrollResult() {
        // 工具类，禁止实例化
    }

    /**
     * 报名创建/更新结果。
     */
    public record Enrollment(
            /** 唯一标识 */
            Long id,
            /** 用户名 */
            String username,
            /** 学号 */
            String studentId,
            /** 邮箱 */
            String email,
            /** 学院ID */
            Long collegeId,
            /** 学院名称 */
            String collegeName,
            /** 专业 */
            String major,
            /** 性别 */
            Gender gender,
            /** 方向 */
            Direction direction,
            /** 头像文件ID */
            Long avatarFileId,
            /** 状态 */
            EnrollStatus status,
            /** 自我介绍 */
            String introduction,
            /** 内推码 */
            String internalReferralCode,
            /** 推荐人ID */
            Long referralUserId,
            /** 推荐人用户名 */
            String referralUserName,
            /** 是否为新创建 */
            boolean created) {
    }

    /**
     * 报名列表摘要结果。
     */
    public record Brief(
            /** 唯一标识 */
            Long id,
            /** 用户名 */
            String username,
            /** 学号 */
            String studentId,
            /** 邮箱 */
            String email,
            /** 学院名称 */
            String collegeName,
            /** 专业 */
            String major,
            /** 性别 */
            Gender gender,
            /** 方向 */
            Direction direction,
            /** 状态 */
            EnrollStatus status,
            /** 头像文件ID */
            Long avatarFileId) {
    }

    /**
     * 报名详情结果。
     */
    public record Detail(
            /** 唯一标识 */
            Long id,
            /** 用户名 */
            String username,
            /** 学号 */
            String studentId,
            /** 邮箱 */
            String email,
            /** 学院ID */
            Long collegeId,
            /** 学院名称 */
            String collegeName,
            /** 专业 */
            String major,
            /** 性别 */
            Gender gender,
            /** 方向 */
            Direction direction,
            /** 头像文件ID */
            Long avatarFileId,
            /** 状态 */
            EnrollStatus status,
            /** 自我介绍 */
            String introduction,
            /** 内推码 */
            String internalReferralCode,
            /** 推荐人用户名 */
            String referralUserName) {
    }

    /**
     * 报名审核结果。
     */
    public record Approval(
            /** 唯一标识 */
            Long id,
            /** 状态 */
            EnrollStatus status,
            /** 创建用户ID */
            Long createdUserId) {
    }

    /**
     * 报名统计结果。
     */
    public record Statistics(
            /** 总数 */
            long total,
            /** 按状态统计 */
            Map<String, Long> byStatus,
            /** 按方向统计 */
            Map<Direction, Long> byDirection) {
    }
}
