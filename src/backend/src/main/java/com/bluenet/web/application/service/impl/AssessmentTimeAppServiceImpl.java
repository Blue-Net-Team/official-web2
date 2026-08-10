package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.result.assessment.AssessmentProgressResult;
import com.bluenet.web.application.result.assessment.AssessmentTimeResult;
import com.bluenet.web.application.command.assessment_time.AssessmentTimeCommands;
import com.bluenet.web.application.service.AssessmentTimeAppService;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.policy.RoleHierarchy;
import com.bluenet.web.domain.model.entity.AssessmentDecision;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.infrastructure.security.principal.RoleTypeResolver;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentDecisionRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.AssessmentDecisionDomainService;
import com.bluenet.web.domain.util.GradeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final AssessmentDecisionRepository assessmentDecisionRepository;
    private final AssessmentDecisionDomainService assessmentDecisionDomainService;
    private final UserRepository userRepository;
    private final RoleTypeResolver roleTypeResolver;

    @Override
    @Transactional
    public AssessmentTimeResult createAssessmentTime(Long userId,
            AssessmentTimeCommands.CreateAssessmentTimeCommand command) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new Unauthorized("用户不存在"));
        validateDirectionPermission(currentUser, command.direction());

        if (command.direction() == null) {
            if (assessmentTimeRepository.countByEpochGrade(command.epoch(), command.grade()) > 0) {
                throw new IllegalArgumentException("该轮次的全局考核时间已存在");
            }
        } else {
            // 同方向同轮次 grade 形式互斥校验
            if (assessmentTimeRepository.hasConflictingGradeByDirectionAndEpoch(
                    command.direction(),
                    command.epoch(),
                    command.grade())) {
                String existingType = command.grade() == null ? "限年级" : "不限年级";
                String newType = command.grade() == null ? "不限年级" : "限年级";
                throw new DataConflict(
                        "该方向轮次已存在" + existingType + "的考核时间，不能创建" + newType + "的考核时间");
            }
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

    @Override
    @Transactional
    public AssessmentTimeResult updateAssessmentTime(Long userId,
            AssessmentTimeCommands.UpdateAssessmentTimeCommand command) {
        AssessmentTime existing = assessmentTimeRepository.findById(command.id())
                .orElseThrow(() -> new DataNotFound("考核时间不存在"));

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new Unauthorized("用户不存在"));
        validateDirectionPermission(currentUser, existing.getDirection());

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
            // 同方向同轮次 grade 形式互斥校验
            if (assessmentTimeRepository.hasConflictingGradeByDirectionAndEpochAndIdNot(
                    newDirection,
                    newEpoch,
                    newGrade,
                    command.id())) {
                String existingType = newGrade == null ? "限年级" : "不限年级";
                String newType = newGrade == null ? "不限年级" : "限年级";
                throw new DataConflict(
                        "该方向轮次已存在" + existingType + "的考核时间，不能更新为" + newType + "的考核时间");
            }
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
        assessmentTimeRepository.save(existing);
        return toResult(existing);
    }

    @Override
    @Transactional
    public void deleteAssessmentTime(Long userId, Long id) {
        AssessmentTime existing = assessmentTimeRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("考核时间不存在"));

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new Unauthorized("用户不存在"));
        validateDirectionPermission(currentUser, existing.getDirection());

        if (assessmentTimeRepository.hasAssociatedQuestions(id)) {
            throw new DataConflict("存在关联的考核题目，需先删除相关题目");
        }

        assessmentTimeRepository.deleteById(id);
    }

    /**
     * 校验 DIRECTION_ADMIN 方向权限：只能操作自己方向的考核时间；全局考核（targetDirection == null）仅
     * SUPER_ADMIN 可操作
     */
    private void validateDirectionPermission(User currentUser, Direction targetDirection) {
        RoleType roleType = roleTypeResolver.resolve(currentUser.getRoleId());
        if (roleType == RoleType.DIRECTION_ADMIN) {
            if (targetDirection == null) {
                throw new Forbidden("方向管理员不能创建跨方向考核");
            }
            if (!targetDirection.equals(currentUser.getDirection())) {
                throw new Forbidden("只能操作本方向的考核时间");
            }
        }
    }

    @Override
    public Page<AssessmentTimeResult> listAssessmentTimes(Long userId, Integer page, Integer size) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 5;

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new Unauthorized("用户不存在"));
        Direction direction = null;
        Integer grade = null;

        RoleType roleType = roleTypeResolver.resolve(currentUser.getRoleId());
        if (roleType != null && !RoleHierarchy.isDirectionAdminOrAbove(roleType)) {
            direction = currentUser.getDirection();

            if (roleType == RoleType.CANDIDATE) {
                grade = GradeCalculator.resolveAssessmentYear(
                        currentUser.getStudentId(),
                        currentUser.getAssessmentGradeYear());
            }
        }

        Page<AssessmentTime> entityPage = assessmentTimeRepository.findByFilters(
                direction,
                grade,
                PageRequest.of(pageNum, pageSize));
        return entityPage.map(entity -> toResult(entity, null, null));
    }

    @Override
    public Page<AssessmentTimeResult> listAssessmentTimesForUser(Long userId, Integer page, Integer size) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 5;

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new Unauthorized("用户不存在"));

        Integer enrollmentYear = GradeCalculator.resolveAssessmentYear(
                currentUser.getStudentId(),
                currentUser.getAssessmentGradeYear());
        Direction direction = currentUser.getDirection();

        Page<AssessmentTime> entityPage = assessmentTimeRepository.findByUserParticipation(
                currentUser.getId(),
                direction,
                enrollmentYear,
                PageRequest.of(pageNum, pageSize));

        RoleType roleType = roleTypeResolver.resolve(currentUser.getRoleId());
        boolean isCandidate = roleType == RoleType.CANDIDATE;

        List<Long> assessmentTimeIds = entityPage.getContent()
                .stream()
                .map(AssessmentTime::getId)
                .toList();

        Map<Long, Integer> totalQuestionCounts = assessmentQuestionRepository
                .countByAssessmentTimeIds(assessmentTimeIds);
        Map<Long, Integer> completedQuestionCounts = assessmentAnswerRepository
                .countByUserIdAndAssessmentTimeIds(currentUser.getId(), assessmentTimeIds);

        List<AssessmentDecision> eliminatedDecisions = isCandidate
                ? assessmentDecisionRepository.findEliminatedDecisionsByUserId(currentUser.getId())
                : List.of();
        List<Long> decisionTimeIds = eliminatedDecisions.stream()
                .map(AssessmentDecision::getAssessmentTimeId)
                .distinct()
                .toList();
        Map<Long, AssessmentTime> decisionTimeMap = assessmentTimeRepository.findAllById(decisionTimeIds)
                .stream()
                .collect(Collectors.toMap(AssessmentTime::getId, time -> time));

        return entityPage.map(entity -> {
            int totalQuestions = totalQuestionCounts.getOrDefault(entity.getId(), 0);
            int completedQuestions = completedQuestionCounts.getOrDefault(entity.getId(), 0);
            boolean eliminated = false;
            if (isCandidate) {
                eliminated = assessmentDecisionDomainService
                        .isEliminatedFromPriorEpoch(entity, eliminatedDecisions, decisionTimeMap);
            }
            return toResult(entity, totalQuestions, completedQuestions, eliminated);
        });
    }

    @Override
    public AssessmentProgressResult getAssessmentProgress(Long userId, Long assessmentTimeId) {
        assessmentTimeRepository.findById(assessmentTimeId)
                .orElseThrow(() -> new IllegalArgumentException("考核时间不存在"));

        int totalQuestions = assessmentQuestionRepository.countByAssessmentTimeId(assessmentTimeId);
        int completedQuestions = assessmentAnswerRepository
                .countByUserIdAndAssessmentTimeId(userId, assessmentTimeId);

        return new AssessmentProgressResult(assessmentTimeId, totalQuestions, completedQuestions);
    }

    private AssessmentTimeResult toResult(AssessmentTime entity) {
        return toResult(entity, null, null, false);
    }

    private AssessmentTimeResult toResult(AssessmentTime entity, Integer totalQuestions, Integer completedQuestions) {
        return toResult(entity, totalQuestions, completedQuestions, false);
    }

    private AssessmentTimeResult toResult(AssessmentTime entity, Integer totalQuestions, Integer completedQuestions,
            boolean eliminated) {
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
                completedQuestions,
                eliminated);
    }
}
