package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.user.TabCountsDTO;
import com.bluenet.web.api.dto.user.UpdateProfileRequestDTO;
import com.bluenet.web.api.dto.user.UserInfo;

public interface UserInfoService {
    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    UserInfo getMyInfo();

    /**
     * 更新当前用户基本信息
     *
     * @param request
     *            更新请求
     */
    void updateProfile(UpdateProfileRequestDTO request);

    /**
     * 获取Tab计数
     *
     * @return Tab计数
     */
    TabCountsDTO getTabCounts();
}
