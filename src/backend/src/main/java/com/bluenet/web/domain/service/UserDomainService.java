package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.TabCountsVO;
import com.bluenet.web.domain.model.vo.UserVO;

import java.util.Optional;

public interface UserDomainService {
    void updateUserAvatar(Long userId, FileVO file);

    /**
     * 获取用户信息
     *
     * @param userId
     *            用户ID
     * @return 用户信息
     */
    Optional<UserVO> getUser(Long userId);

    /**
     * 更新用户基本信息
     *
     * @param userId
     *            用户ID
     * @param username
     *            用户名（可为null）
     * @param nickname
     *            昵称（可为null）
     * @param college
     *            学院（可为null）
     * @param major
     *            专业（可为null）
     * @param direction
     *            方向（可为null）
     * @param gender
     *            性别（可为null）
     * @param bio
     *            个人简介（可为null）
     */
    void updateProfile(Long userId, String username, String nickname, String college,
            String major, Direction direction, Gender gender, String bio);

    /**
     * 获取用户Tab计数
     *
     * @param userId
     *            用户ID
     * @return Tab计数
     */
    TabCountsVO getTabCounts(Long userId);
}
