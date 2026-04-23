package com.bluenet.web.application.service.assessment;

import java.util.Objects;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.policy.RoleHierarchy;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.infrastructure.security.util.UserCTX;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 考核评判访问范围 guard，集中处理角色等级和方向范围校验。
 */
@RequiredArgsConstructor
@Component
public class AssessmentJudgementAccessGuard {
    private final AssessmentTimeRepository assessmentTimeRepository;

    /**
     * 获取当前登录用户，未登录时抛出安全异常。
     *
     * @return 当前登录用户 VO。
     */
    public UserVO requireCurrentUser() {
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            throw new SecurityException("未登录");
        }
        return currentUser;
    }

    /**
     * 解析用户角色类型，角色无效时抛出权限异常。
     *
     * @param user
     *            用户 VO。
     * @return 角色类型枚举。
     */
    public RoleType requireRole(UserVO user) {
        RoleType roleType = RoleType.fromName(user.getRoleName());
        if (roleType == null) {
            throw new Forbidden("当前用户角色无效");
        }
        return roleType;
    }

    /**
     * 校验当前用户是否为团队成员及以上，并检查考核时间方向归属。
     *
     * @param assessmentTimeId
     *            考核时间ID。
     */
    public void requireMemberScope(Long assessmentTimeId) {
        UserVO currentUser = requireCurrentUser();
        RoleType roleType = requireRole(currentUser);
        if (!RoleHierarchy.isMemberOrAbove(roleType)) {
            throw new Forbidden("只有团队成员及以上权限可以查看考核评判");
        }
        assertAssessmentTimeScope(assessmentTimeId, currentUser, roleType);
    }

    /**
     * 校验当前用户是否为方向管理员及以上，并检查考核时间方向归属。
     *
     * @param assessmentTimeId
     *            考核时间ID。
     */
    public void requireDecisionScope(Long assessmentTimeId) {
        UserVO currentUser = requireCurrentUser();
        RoleType roleType = requireRole(currentUser);
        if (!RoleHierarchy.isDirectionAdminOrAbove(roleType)) {
            throw new Forbidden("只有方向管理员及以上权限可以查看录用决策");
        }
        assertAssessmentTimeScope(assessmentTimeId, currentUser, roleType);
    }

    private void assertAssessmentTimeScope(Long assessmentTimeId, UserVO currentUser, RoleType roleType) {
        AssessmentTime assessmentTime = assessmentTimeRepository.findById(assessmentTimeId)
                .orElseThrow(() -> new DataNotFound("考核时间不存在，ID: " + assessmentTimeId));
        if (roleType == RoleType.DIRECTION_ADMIN
                && currentUser.getDirection() != null
                && !Objects.equals(currentUser.getDirection(), assessmentTime.getDirection())) {
            throw new Forbidden("不能访问其他方向的考核评判数据");
        }
    }
}
