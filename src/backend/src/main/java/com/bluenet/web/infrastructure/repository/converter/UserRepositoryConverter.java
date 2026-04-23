package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.infrastructure.repository.dataobject.UserDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户仓储转换器
 * <p>
 * 负责 User 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class UserRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public UserDO toDataObject(User entity) {
        if (entity == null) {
            return null;
        }
        return UserDO.builder()
                .id(entity.getId())
                .studentId(entity.getStudentId())
                .email(entity.getEmail())
                .roleId(entity.getRoleId())
                .password(entity.getPassword())
                .username(entity.getUsername())
                .nickname(entity.getNickname())
                .collegeId(entity.getCollegeId())
                .major(entity.getMajor())
                .assessmentGradeYear(entity.getAssessmentGradeYear())
                .direction(entity.getDirection())
                .gender(entity.getGender())
                .job(entity.getJob())
                .avatarId(entity.getAvatarId())
                .disable(entity.getDisable())
                .qrcodeId(entity.getQrcodeId())
                .githubId(entity.getGithubId())
                .githubUsername(entity.getGithubUsername())
                .internalReferralCode(entity.getInternalReferralCode())
                .bio(entity.getBio())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public User toEntity(UserDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return User.reconstruct(
                dataObject.getId(),
                dataObject.getStudentId(),
                dataObject.getEmail(),
                dataObject.getRoleId(),
                dataObject.getPassword(),
                dataObject.getUsername(),
                dataObject.getNickname(),
                dataObject.getCollegeId(),
                dataObject.getMajor(),
                dataObject.getAssessmentGradeYear(),
                dataObject.getDirection(),
                dataObject.getGender(),
                dataObject.getJob(),
                dataObject.getAvatarId(),
                dataObject.getDisable(),
                dataObject.getQrcodeId(),
                dataObject.getGithubId(),
                dataObject.getGithubUsername(),
                dataObject.getInternalReferralCode(),
                dataObject.getBio());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<User> toEntityList(List<UserDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
