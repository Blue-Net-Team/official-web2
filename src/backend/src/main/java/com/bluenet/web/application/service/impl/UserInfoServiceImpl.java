package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.user.TabCountsDTO;
import com.bluenet.web.api.dto.user.UpdateProfileRequestDTO;
import com.bluenet.web.api.dto.user.UserInfo;
import com.bluenet.web.application.converter.UserConverter;
import com.bluenet.web.application.service.UserInfoService;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.vo.TabCountsVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.service.UserDomainService;
import com.bluenet.web.infrastructure.security.RoleType;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserInfoServiceImpl implements UserInfoService {
    private final UserConverter userConverter;
    private final UserDomainService userDomainService;

    @Override
    public UserInfo getMyInfo() {
        UserVO userVO = UserCTX.getCurrentUser();

        if (userVO == null) {
            log.warn("用户未认证，无法获取用户信息");
            throw new Unauthorized("未认证");
        }

        return userConverter.convertToUserInfo(userVO);
    }

    @Override
    public void updateProfile(UpdateProfileRequestDTO request) {
        Long userId = getCurrentUserId();
        UserVO currentUser = UserCTX.getCurrentUser();

        validateProfileUpdatePermission(currentUser, request);

        userDomainService.updateProfile(
                userId,
                request.getUsername(),
                request.getNickname(),
                request.getCollege(),
                request.getMajor(),
                request.getDirection(),
                request.getGender(),
                request.getBio());
    }

    @Override
    public TabCountsDTO getTabCounts() {
        Long userId = getCurrentUserId();

        TabCountsVO tabCountsVO = userDomainService.getTabCounts(userId);

        return TabCountsDTO.builder()
                .projects(tabCountsVO.getProjects())
                .competitions(tabCountsVO.getCompetitions())
                .internships(tabCountsVO.getInternships())
                .build();
    }

    private Long getCurrentUserId() {
        UserVO userVO = UserCTX.getCurrentUser();
        if (userVO == null) {
            log.warn("用户未认证");
            throw new Unauthorized("未认证");
        }
        return userVO.getId();
    }

    private void validateProfileUpdatePermission(UserVO user, UpdateProfileRequestDTO request) {
        RoleType role = RoleType.fromName(user.getRoleName());

        if (role == RoleType.CANDIDATE) {
            if (request.getUsername() != null || request.getGender() != null || request.getCollege() != null
                    || request.getMajor() != null || request.getDirection() != null) {
                throw new Forbidden("只有成员及以上角色才能修改用户名、性别、学院、专业和报名方向");
            }
        }
    }
}
