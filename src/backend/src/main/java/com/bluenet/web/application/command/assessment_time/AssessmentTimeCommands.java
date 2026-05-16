package com.bluenet.web.application.command.assessment_time;

import com.bluenet.web.domain.model.enumerate.Direction;

import java.time.LocalDateTime;

/**
 * 考核时间聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class AssessmentTimeCommands {

    /** 禁止实例化。 */
    private AssessmentTimeCommands() {
    }

    /**
     * 创建考核时间命令。
     * <p>
     * 用于创建新的考核时间安排。
     * </p>
     */
    public record CreateAssessmentTimeCommand(
            /** 方向 */
            Direction direction,
            /** 届数 */
            Integer epoch,
            /** 年级 */
            Integer grade,
            /** 开始时间 */
            LocalDateTime startTime,
            /** 结束时间 */
            LocalDateTime endTime,
            /** 是否限时 */
            Boolean timeLimit,
            /** 限时分钟数 */
            Integer timeLimitMinutes,
            /** 是否允许组队 */
            Boolean allowTeam) {
    }

    /**
     * 更新考核时间命令。
     * <p>
     * 用于更新已有的考核时间安排。
     * </p>
     */
    public record UpdateAssessmentTimeCommand(
            /** ID */
            Long id,
            /** 方向 */
            Direction direction,
            /** 届数 */
            Integer epoch,
            /** 年级 */
            Integer grade,
            /** 开始时间 */
            LocalDateTime startTime,
            /** 结束时间 */
            LocalDateTime endTime,
            /** 是否限时 */
            Boolean timeLimit,
            /** 限时分钟数 */
            Integer timeLimitMinutes,
            /** 是否允许组队 */
            Boolean allowTeam) {
    }
}
