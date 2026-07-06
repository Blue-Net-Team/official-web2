package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.TabCountsVO;

public interface UserDomainService {
    void updateUserAvatar(Long userId, FileVO file);

    void updateProfile(Long userId, String username, String nickname, String college,
            String major, Direction direction, Gender gender, String bio, Long qrcodeFileId);

    TabCountsVO getTabCounts(Long userId);

    void changeEmail(Long userId, String currentEmail, String originalEmailVerifyCode,
            String newEmail, String newEmailVerifyCode);

    void changePassword(Long userId, String rawNewPassword);
}
