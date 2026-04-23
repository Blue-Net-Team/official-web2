package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.user.TabCountsDTO;
import com.bluenet.web.api.dto.user.UserInfo;
import com.bluenet.web.application.UserInfoResult;
import org.springframework.stereotype.Component;

/**
 * 用户信息应用层转换器
 * <p>
 * 负责应用层 Result 与 API 层 DTO 之间的转换
 * </p>
 */
@Component
public class UserInfoAppConverter {

    public UserInfo toDTO(UserInfoResult result) {
        return UserInfo.builder()
                .id(result.id())
                .username(result.username())
                .nickname(result.nickname())
                .college(result.college())
                .major(result.major())
                .grade(result.grade())
                .email(result.email())
                .avatarFileId(result.avatarFileId())
                .roleName(result.roleName())
                .direction(result.direction())
                .gender(result.gender())
                .bio(result.bio())
                .githubUsername(result.githubUsername())
                .build();
    }

    public TabCountsDTO toTabCountsDTO(UserInfoResult.TabCounts tabCounts) {
        return TabCountsDTO.builder()
                .projects(tabCounts.projects())
                .competitions(tabCounts.competitions())
                .internships(tabCounts.internships())
                .build();
    }
}
