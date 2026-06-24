package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.Direction;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 考核时间聚合根
 * <p>
 * 承载考核时间相关的业务规则和行为
 * </p>
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AssessmentTime {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 用户或考核所属技术方向。
     */
    private Direction direction;
    /**
     * 考核批次或轮次编号。
     */
    private Integer epoch;
    /**
     * 学生年级或成绩等级。
     */
    private Integer grade;
    /**
     * 经历、考核或有效期的开始时间。
     */
    private LocalDateTime startTime;
    /**
     * 经历、考核或有效期的结束时间。
     */
    private LocalDateTime endTime;
    /**
     * 算法题时间限制，通常以毫秒为单位。
     */
    private Boolean timeLimit;
    /**
     * 考核作答时长限制，单位分钟。
     */
    private Integer timeLimitMinutes;
    /**
     * 考核结果发布时间，设置后考生可见评论和最终评分。
     */
    private LocalDateTime resultsPublishedAt;
    /**
     * 是否允许组队答题。
     */
    private Boolean allowTeam;

    private AssessmentTime(Long id, Direction direction, Integer epoch, Integer grade,
            LocalDateTime startTime, LocalDateTime endTime,
            Boolean timeLimit, Integer timeLimitMinutes, LocalDateTime resultsPublishedAt,
            Boolean allowTeam) {
        this.id = id;
        this.direction = direction;
        this.epoch = epoch;
        this.grade = grade;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeLimit = timeLimit;
        this.timeLimitMinutes = timeLimitMinutes;
        this.resultsPublishedAt = resultsPublishedAt;
        this.allowTeam = allowTeam;
    }

    /**
     * 构造新聚合根 —— 带领域校验
     */
    public static AssessmentTime create(Direction direction, Integer epoch, Integer grade,
            LocalDateTime startTime, LocalDateTime endTime,
            Boolean timeLimit, Integer timeLimitMinutes, Boolean allowTeam) {
        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("开始时间必须早于结束时间");
        }
        if (Boolean.TRUE.equals(timeLimit) && (timeLimitMinutes == null || timeLimitMinutes <= 0)) {
            throw new IllegalArgumentException("限时考核必须设置有效的限时分钟数");
        }
        return new AssessmentTime(null, direction, epoch, grade, startTime, endTime, timeLimit, timeLimitMinutes, null,
                allowTeam);
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     */
    public static AssessmentTime reconstruct(Long id, Direction direction, Integer epoch, Integer grade,
            LocalDateTime startTime, LocalDateTime endTime,
            Boolean timeLimit, Integer timeLimitMinutes, LocalDateTime resultsPublishedAt,
            Boolean allowTeam) {
        return new AssessmentTime(id, direction, epoch, grade, startTime, endTime, timeLimit, timeLimitMinutes,
                resultsPublishedAt, allowTeam);
    }

    /**
     * 更新考核时间属性
     */
    public void update(Direction direction, Integer epoch, Integer grade,
            LocalDateTime startTime, LocalDateTime endTime,
            Boolean timeLimit, Integer timeLimitMinutes, Boolean allowTeam) {
        if (direction != null) {
            this.direction = direction;
        }
        if (epoch != null) {
            this.epoch = epoch;
        }
        if (grade != null) {
            this.grade = grade;
        }
        if (startTime != null) {
            this.startTime = startTime;
        }
        if (endTime != null) {
            this.endTime = endTime;
        }
        if (timeLimit != null) {
            this.timeLimit = timeLimit;
        }
        if (timeLimitMinutes != null) {
            this.timeLimitMinutes = timeLimitMinutes;
        }
        if (allowTeam != null) {
            this.allowTeam = allowTeam;
        }
    }

    /**
     * 发布考核结果
     */
    public void publishResults() {
        this.resultsPublishedAt = LocalDateTime.now();
    }

    /**
     * 考核结果是否已发布
     */
    public boolean isResultsPublished() {
        return this.resultsPublishedAt != null;
    }

    /**
     * 校验开始时间早于结束时间
     */
    public void validateStartBeforeEnd() {
        if (this.startTime != null && this.endTime != null && !this.startTime.isBefore(this.endTime)) {
            throw new IllegalArgumentException("开始时间必须早于结束时间");
        }
    }

    /**
     * 校验限时考核必须设置限时分钟数
     */
    public void validateTimeLimit() {
        if (Boolean.TRUE.equals(this.timeLimit) && (this.timeLimitMinutes == null || this.timeLimitMinutes <= 0)) {
            throw new IllegalArgumentException("限时考核必须设置有效的限时分钟数");
        }
    }

    /**
     * 是否已经开始
     */
    public boolean hasStarted() {
        return this.startTime != null && !this.startTime.isAfter(LocalDateTime.now());
    }

    /**
     * 是否为全局最终考核（direction=null 且 epoch=0）。
     * <p>
     * 全局最终考核是跨方向的综合团队考核，只有通过后才发送「录取」邮件， 淘汰则发送「淘汰」邮件；方向考核无论第几轮均使用「通过 / 未通过」文案。
     * </p>
     *
     * @return 当前考核是否为全局最终考核
     */
    public boolean isGlobalFinalAssessment() {
        return getScope().isGlobalFinal();
    }

    /**
     * 获取当前考核的范围值对象。
     *
     * @return 当前考核的范围
     */
    public AssessmentScope getScope() {
        return new AssessmentScope(this.direction, this.epoch);
    }

    /**
     * 判断当前考核与目标考核的年级是否匹配。
     * <p>
     * 规则：任一考核的 grade 为 null（不限年级）则匹配；否则必须精确相等。
     * </p>
     *
     * @param other
     *            目标考核
     * @return 年级是否匹配
     */
    public boolean matchesGrade(AssessmentTime other) {
        if (other == null) {
            return false;
        }
        if (this.grade == null || other.grade == null) {
            return true;
        }
        return this.grade.equals(other.grade);
    }
}
