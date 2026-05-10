package com.bluenet.web.api.converter.adminuser;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.adminuser.AdminUserCreateResponseDTO;
import com.bluenet.web.api.dto.adminuser.AdminUserDetailResponseDTO;
import com.bluenet.web.api.dto.adminuser.AdminUserListItemResponseDTO;
import com.bluenet.web.application.AdminUserResult;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * 管理员用户响应转换器
 * <p>
 * 负责将管理员用户 Result 转换为接口 DTO
 * </p>
 */
@Component
public class AdminUserResponseConverter {

    public PageDTO<AdminUserListItemResponseDTO> toPageDTO(Page<AdminUserResult.ListItem> page) {
        return PageDTO.from(page.map(this::toListItemDTO));
    }

    public AdminUserListItemResponseDTO toListItemDTO(AdminUserResult.ListItem item) {
        return AdminUserListItemResponseDTO.builder()
                .id(item.id())
                .studentId(item.studentId())
                .username(item.username())
                .nickname(item.nickname())
                .email(item.email())
                .roleId(item.roleId())
                .roleName(item.roleName())
                .direction(item.direction())
                .college(item.college())
                .major(item.major())
                .gender(item.gender())
                .job(item.job())
                .disable(item.disable())
                .avatarFileId(item.avatarFileId())
                .build();
    }

    public AdminUserCreateResponseDTO toCreateResponseDTO(AdminUserResult.Created created) {
        return AdminUserCreateResponseDTO.builder()
                .id(created.id())
                .studentId(created.studentId())
                .username(created.username())
                .roleId(created.roleId())
                .build();
    }

    public AdminUserDetailResponseDTO toDetailDTO(AdminUserResult.Detail detail) {
        return AdminUserDetailResponseDTO.builder()
                .id(detail.id())
                .studentId(detail.studentId())
                .username(detail.username())
                .nickname(detail.nickname())
                .email(detail.email())
                .roleId(detail.roleId())
                .roleName(detail.roleName())
                .direction(detail.direction())
                .college(detail.college())
                .major(detail.major())
                .gender(detail.gender())
                .job(detail.job())
                .disable(detail.disable())
                .avatarFileId(detail.avatarFileId())
                .githubUsername(detail.githubUsername())
                .bio(detail.bio())
                .assessmentGradeYear(detail.assessmentGradeYear())
                .experienceCount(detail.experienceCount())
                .achievementCount(detail.achievementCount())
                .answerCount(detail.answerCount())
                .commentCount(detail.commentCount())
                .build();
    }
}
