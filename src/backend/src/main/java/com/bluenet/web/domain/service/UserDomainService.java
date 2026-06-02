package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.TabCountsVO;
import com.bluenet.web.domain.model.vo.UserVO;

import java.util.Optional;

public interface UserDomainService {
    void updateUserAvatar(Long userId, FileVO file);

    Optional<UserVO> getUser(Long userId);

    void updateProfile(Long userId, String username, String nickname, String college,
            String major, Direction direction, Gender gender, String bio, Long qrcodeFileId);

    TabCountsVO getTabCounts(Long userId);

    void changeEmail(Long userId, String currentEmail, String originalEmailVerifyCode,
            String newEmail, String newEmailVerifyCode);

    void changePassword(Long userId, String rawNewPassword);
}
