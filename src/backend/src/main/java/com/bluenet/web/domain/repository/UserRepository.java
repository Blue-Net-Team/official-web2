package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.QrcodeVO;
import com.bluenet.web.domain.model.vo.TabCountsVO;
import com.bluenet.web.domain.model.vo.UserVO;

import java.util.Optional;

public interface UserRepository {
    void save(User user);
    Optional<UserVO> findById(Long id);
    Optional<UserVO> findByEmail(String email);
    Optional<UserVO> findByStudentId(String studentId);
    int updateAvatar(UserVO user, FileVO file);
    int updateAvatar(Long userId, Long id);
    int updateQrcode(UserVO user, QrcodeVO qrcode);

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
     * @return 影响的行数
     */
    int updateProfile(Long userId, String username, String nickname, String college,
            String major, Direction direction, Gender gender, String bio);

    /**
     * 获取用户Tab计数
     *
     * @param userId
     *            用户ID
     * @return Tab计数
     */
    TabCountsVO getTabCounts(Long userId);

    Optional<UserVO> findByGithubId(String githubId);

    void updateGithubBinding(Long userId, String githubId, String githubUsername);

    void clearGithubBinding(Long userId);

    void updateEmail(Long userId, String newEmail);

    void updatePassword(Long userId, String encodedPassword);

    /**
     * 检查内推码是否已存在（用于唯一性校验）
     *
     * @param code
     *            内推码
     * @return true 如果该内推码已被使用
     */
    boolean existsByInternalReferralCode(String code);
}
