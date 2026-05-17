package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.AssessmentProgressResult;
import com.bluenet.web.application.AssessmentTimeResult;
import com.bluenet.web.application.command.assessment_time.AssessmentTimeCommands;
import com.bluenet.web.application.service.AssessmentTimeAppService;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.policy.RoleHierarchy;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.util.GradeCalculator;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考核时间应用服务实现。
 * <p>
 * 实现考核时间聚合在应用层的业务逻辑编排。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AssessmentTimeAppServiceImpl implements AssessmentTimeAppService {
    private final AssessmentTimeRepository assessmentTimeRepository;
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final AssessmentAnswerRepository assessmentAnswerRepository;

    /**
     * 创建考核时间。
     *
     * @param command
     *            创建考核时间命令
     * @return 创建后的考核时间结果
     */
    @Override
    @Transactional
    public AssessmentTimeResult createAssessmentTime(AssessmentTimeCommands.CreateAssessmentTimeCommand command) {
        validateDirectionPermission(command.direction());

        if (command.direction() == null) {
            if (assessmentTimeRepository.countByEpochGrade(command.epoch(), command.grade()) > 0) {
                throw new IllegalArgumentException("该轮次的全局考核时间已存在");
            }
        } else {
            if (assessmentTimeRepository.existsByDirectionAndEpochAndGrade(
                    command.direction(),
                    command.epoch(),
                    command.grade())) {
                throw new IllegalArgumentException("该方向轮次年级的考核时间已存在");
            }
        }

        AssessmentTime entity = AssessmentTime.create(
                command.direction(),
                command.epoch(),
                command.grade(),
                command.startTime(),
                command.endTime(),
                command.timeLimit(),
                command.timeLimitMinutes(),
                command.allowTeam());

        assessmentTimeRepository.save(entity);
        return toResult(entity);
    }

    /**
     * 更新考核时间。
     *
     * @param command
     *            更新考核时间命令
     * @return 更新后的考核时间结果
     */
    @Override
    @Transactional
    public AssessmentTimeResult updateAssessmentTime(AssessmentTimeCommands.UpdateAssessmentTimeCommand command) {
        AssessmentTime existing = assessmentTimeRepository.findById(command.id())
                .orElseThrow(() -> new DataNotFound("考核时间不存在"));
        validateDirectionPermission(existing.getDirection());

        LocalDateTime now = LocalDateTime.now();

        // 校验已开始的考核不允许修改开始时间
        if (existing.getStartTime() != null && !existing.getStartTime().isAfter(now)) {
            if (command.startTime() != null && !command.startTime().equals(existing.getStartTime())) {
                throw new IllegalArgumentException("已开始的考核不允许修改开始时间");
            }
        }

        // 合并现有值和更新值用于校验
        LocalDateTime newStartTime = command.startTime() != null ? command.startTime() : existing.getStartTime();
        LocalDateTime newEndTime = command.endTime() != null ? command.endTime() : existing.getEndTime();
        if (newStartTime != null && newEndTime != null && !newStartTime.isBefore(newEndTime)) {
            throw new IllegalArgumentException("开始时间必须早于结束时间");
        }

        Boolean newTimeLimit = command.timeLimit() != null ? command.timeLimit() : existing.getTimeLimit();
        Integer newTimeLimitMinutes = command.timeLimitMinutes() != null
                ? command.timeLimitMinutes()
                : existing.getTimeLimitMinutes();
        if (Boolean.TRUE.equals(newTimeLimit) && (newTimeLimitMinutes == null || newTimeLimitMinutes <= 0)) {
            throw new IllegalArgumentException("限时考核必须设置有效的限时分钟数");
        }

        Direction newDirection = command.direction() != null ? command.direction() : existing.getDirection();
        Integer newEpoch = command.epoch() != null ? command.epoch() : existing.getEpoch();
        Integer newGrade = command.grade() != null ? command.grade() : existing.getGrade();

        if (newDirection == null) {
            if (assessmentTimeRepository.countByEpochGrade(newEpoch, newGrade) > 0) {
                throw new IllegalArgumentException("该轮次的全局考核时间已存在");
            }
        } else {
            if (assessmentTimeRepository.existsByDirectionAndEpochAndGradeAndIdNot(
                    newDirection,
                    newEpoch,
                    newGrade,
                    command.id())) {
                throw new IllegalArgumentException("该方向轮次年级的考核时间已存在");
            }
        }

        Boolean newAllowTeam = command.allowTeam() != null ? command.allowTeam() : existing.getAllowTeam();

        existing.update(
                newDirection,
                newEpoch,
                newGrade,
                newStartTime,
                newEndTime,
                newTimeLimit,
                newTimeLimitMinutes,
                newAllowTeam);
        assessmentTimeRepository.update(existing);
        return toResult(existing);
    }

    /**
     * 删除考核时间。
     *
     * @param id
     *            考核时间ID
     */
    @Override
    @Transactional
    public void deleteAssessmentTime(Long id) {
        AssessmentTime existing = assessmentTimeRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("考核时间不存在"));
        validateDirectionPermission(existing.getDirection());

        if (assessmentTimeRepository.hasAssociatedQuestions(id)) {
            throw new DataConflict("存在关联的考核题目，需先删除相关题目");
        }

        assessmentTimeRepository.deleteById(id);
    }

    /**
     * 校验 DIRECTION_ADMIN 方向权限：只能操作自己方向的考核时间；全局考核（targetDirection == null）仅
     * SUPER_ADMIN 可操作
     */
    private void validateDirectionPermission(Direction targetDirection) {
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser == null)
            return;

        RoleType roleType = RoleType.fromName(currentUser.getRoleName());
        if (roleType == RoleType.DIRECTION_ADMIN) {
            if (targetDirection == null) {
                throw new Forbidden("方向管理员不能创建跨方向考核");
            }
            if (!targetDirection.equals(currentUser.getDirection())) {
                throw new Forbidden("只能操作本方向的考核时间");
            }
        }
    }

    /**
     * 分页查询考核时间列表。
     *
     * @param page
     *            页码
     * @param size
     *            每页大小
     * @return 考核时间分页结果
     */
    @Override
    public Page<AssessmentTimeResult> listAssessmentTimes(Integer page, Integer size) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 5;

        UserVO currentUser = UserCTX.getCurrentUser();
        Direction direction = null;
        Integer grade = null;

        if (currentUser != null) {
            RoleType roleType = RoleType.fromName(currentUser.getRoleName());
            if (roleType != null && !RoleHierarchy.isDirectionAdminOrAbove(roleType)) {
                direction = currentUser.getDirection();

                if (roleType == RoleType.CANDIDATE) {
                    grade = GradeCalculator.resolveAssessmentYear(
                            currentUser.getStudentId(),
                            currentUser.getAssessmentGradeYear());
                }
            }
        }

        Page<AssessmentTime> entityPage = assessmentTimeRepository.findByFilters(
                direction,
                grade,
                PageRequest.of(pageNum, pageSize));
        return entityPage.map(entity -> toResult(entity, null, null));
    }

    /**
     * 分页查询用户考核时间列表。
     *
     * @param page
     *            页码
     * @param size
     *            每页大小
     * @return 用户考核时间分页结果
     */
    @Override
    public Page<AssessmentTimeResult> listAssessmentTimesForUser(Integer page, Integer size) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 5;

        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            Page<AssessmentTime> emptyPage = new PageImpl<>(
                    List.of(), PageRequest.of(pageNum, pageSize), 0);
            return emptyPage.map(entity -> toResult(entity, null, null));
        }

        Integer enrollmentYear = GradeCalculator.resolveAssessmentYear(
                currentUser.getStudentId(),
                currentUser.getAssessmentGradeYear());
        Direction direction = currentUser.getDirection();

        Page<AssessmentTime> entityPage = assessmentTimeRepository.findByUserParticipation(
                currentUser.getId(),
                direction,
                enrollmentYear,
                PageRequest.of(pageNum, pageSize));

        return entityPage.map(entity -> {
            int totalQuestions = assessmentQuestionRepository.countByAssessmentTimeId(entity.getId());
            int completedQuestions = assessmentAnswerRepository
                    .countByUserIdAndAssessmentTimeId(currentUser.getId(), entity.getId());
            return toResult(entity, totalQuestions, completedQuestions);
        });
    }

    /**
     * 获取考核进度。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @return 考核进度DTO
     */
    @Override
    public AssessmentProgressResult getAssessmentProgress(Long assessmentTimeId) {
        assessmentTimeRepository.findById(assessmentTimeId)
                .orElseThrow(() -> new IllegalArgumentException("考核时间不存在"));

        UserVO currentUser = UserCTX.getCurrentUser();
        int totalQuestions = assessmentQuestionRepository.countByAssessmentTimeId(assessmentTimeId);
        int completedQuestions = 0;
        if (currentUser != null) {
            completedQuestions = assessmentAnswerRepository
                    .countByUserIdAndAssessmentTimeId(currentUser.getId(), assessmentTimeId);
        }

        return new AssessmentProgressResult(assessmentTimeId, totalQuestions, completedQuestions);
    }

    private AssessmentTimeResult toResult(AssessmentTime entity) {
        return toResult(entity, null, null);
    }

    private AssessmentTimeResult toResult(AssessmentTime entity, Integer totalQuestions, Integer completedQuestions) {
        return new AssessmentTimeResult(
                entity.getId(),
                entity.getDirection(),
                entity.getEpoch(),
                entity.getGrade(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getTimeLimit(),
                entity.getTimeLimitMinutes(),
                entity.getAllowTeam(),
                totalQuestions,
                completedQuestions);
    }
}
