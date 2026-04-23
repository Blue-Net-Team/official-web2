package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.Member;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.RoleType;
import org.springframework.stereotype.Component;

/**
 * 成员仓储转换器
 * <p>
 * 负责将 User 领域对象及相关联数据转换为 Member 实体
 * </p>
 */
@Component
public class MemberRepositoryConverter {

    /**
     * 根据 User 及相关联数据构建 Member 实体
     */
    public Member toEntity(User user, String collegeName, String wechatQrCodeUrl,
            String roleName, Integer enrollmentYear) {
        if (user == null) {
            return null;
        }
        RoleType roleType = roleName != null ? RoleType.fromName(roleName) : null;
        return Member.reconstruct(
                user.getId(),
                user.getStudentId(),
                user.getUsername(),
                user.getNickname(),
                user.getDirection(),
                user.getJob(),
                user.getAvatarId(),
                collegeName,
                user.getMajor(),
                user.getGender(),
                roleType,
                roleName,
                user.getBio(),
                user.getGithubUsername(),
                wechatQrCodeUrl,
                enrollmentYear,
                user.getAssessmentGradeYear());
    }
}
