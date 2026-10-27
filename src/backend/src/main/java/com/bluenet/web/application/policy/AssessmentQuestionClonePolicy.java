package com.bluenet.web.application.policy;

import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.infrastructure.security.principal.RoleTypeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 考题克隆的方向访问策略。
 * <p>
 * 与复制编排分离，后续扩展跨方向克隆时无需改变题目和判题资产复制逻辑。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class AssessmentQuestionClonePolicy {
    private final RoleTypeResolver roleTypeResolver;

    /**
     * 校验当前用户是否可以读取源考核并写入目标考核。
     *
     * @param currentUser
     *            当前用户
     * @param sourceTime
     *            源考核时间
     * @param targetTime
     *            目标考核时间
     */
    public void validate(User currentUser, AssessmentTime sourceTime, AssessmentTime targetTime) {
        RoleType roleType = roleTypeResolver.resolve(currentUser.getRoleId());
        if (roleType == RoleType.SUPER_ADMIN) {
            if (!Objects.equals(sourceTime.getDirection(), targetTime.getDirection())) {
                throw new Forbidden("当前仅支持同方向考核之间克隆考题");
            }
            return;
        }
        if (roleType == RoleType.DIRECTION_ADMIN) {
            if (currentUser.getDirection() == null
                    || !currentUser.getDirection().equals(sourceTime.getDirection())
                    || !currentUser.getDirection().equals(targetTime.getDirection())) {
                throw new Forbidden("只能在本方向的考核时间之间克隆考题");
            }
            return;
        }
        throw new Forbidden("当前角色无权克隆考题");
    }
}
