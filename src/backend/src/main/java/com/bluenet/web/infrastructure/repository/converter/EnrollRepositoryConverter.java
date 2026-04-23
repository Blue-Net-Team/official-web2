package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.Enroll;
import com.bluenet.web.infrastructure.repository.dataobject.EnrollDO;
import org.springframework.stereotype.Component;

/**
 * 报名仓储转换器
 * <p>
 * 负责 Enroll 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class EnrollRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public EnrollDO toDataObject(Enroll entity) {
        if (entity == null) {
            return null;
        }
        return EnrollDO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .studentId(entity.getStudentId())
                .password(entity.getPassword())
                .internalReferralCode(entity.getInternalReferralCode())
                .collegeId(entity.getCollegeId())
                .major(entity.getMajor())
                .gender(entity.getGender())
                .direction(entity.getDirection())
                .avatarId(entity.getAvatarId())
                .status(entity.getStatus())
                .email(entity.getEmail())
                .introduction(entity.getIntroduction())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public Enroll toEntity(EnrollDO dataObject, String collegeName, Long referralUserId, String referralUserName) {
        if (dataObject == null) {
            return null;
        }
        return Enroll.reconstruct(
                dataObject.getId(),
                dataObject.getUsername(),
                dataObject.getStudentId(),
                dataObject.getPassword(),
                dataObject.getInternalReferralCode(),
                dataObject.getCollegeId(),
                dataObject.getMajor(),
                dataObject.getGender(),
                dataObject.getDirection(),
                dataObject.getAvatarId(),
                dataObject.getStatus(),
                dataObject.getEmail(),
                dataObject.getIntroduction(),
                collegeName,
                referralUserId,
                referralUserName);
    }
}
