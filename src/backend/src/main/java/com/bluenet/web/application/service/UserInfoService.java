package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.user.ChangeEmailRequestDTO;
import com.bluenet.web.api.dto.user.SendEmailVerificationCodeRequestDTO;
import com.bluenet.web.api.dto.user.TabCountsDTO;
import com.bluenet.web.api.dto.user.UpdateProfileRequestDTO;
import com.bluenet.web.api.dto.user.UserInfo;

public interface UserInfoService {
    UserInfo getMyInfo();

    void updateProfile(UpdateProfileRequestDTO request);

    TabCountsDTO getTabCounts();

    void sendEmailVerificationCode(SendEmailVerificationCodeRequestDTO request);

    void changeEmail(ChangeEmailRequestDTO request);
}
