package com.bluenet.web.api.converter.userinfo;

import com.bluenet.web.api.dto.user.TabCountsDTO;
import com.bluenet.web.api.dto.user.UserInfo;
import com.bluenet.web.application.UserInfoResult;
import org.springframework.stereotype.Component;

/**
 * 用户信息响应转换器
 * <p>
 * 负责将应用层结果转换为 API 响应 DTO
 * </p>
 */
@Component
public class UserInfoResponseConverter {

    /**
     * 将应用层结果转换为 API 响应 DTO
     */
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
                .qrcodeFileId(result.wechatQrcode())
                .build();
    }

    /**
     * 将Tab计数结果转换为 TabCounts DTO
     */
    public TabCountsDTO toTabCountsDTO(UserInfoResult.TabCounts tabCounts) {
        return TabCountsDTO.builder()
                .projects(tabCounts.projects())
                .competitions(tabCounts.competitions())
                .internships(tabCounts.internships())
                .build();
    }
}
