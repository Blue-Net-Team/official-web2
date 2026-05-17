package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.QrcodeVO;
import com.bluenet.web.domain.model.vo.TabCountsVO;
import com.bluenet.web.domain.model.vo.UserVO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    /**
     * 保存新的用户 记录。
     *
     * @param user
     *            用户领域对象。
     */
    void save(User user);
    /**
     * 按主键查询用户 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的用户 结果；不存在时为空。
     */
    Optional<UserVO> findById(Long id);
    /**
     * 按邮箱查询用户视图。
     *
     * @param email
     *            邮箱地址，用于定位用户或验证码。
     * @return 查询到的用户 结果；不存在时为空。
     */
    Optional<UserVO> findByEmail(String email);
    /**
     * 按学号查询用户或报名申请。
     *
     * @param studentId
     *            学生学号，用于定位用户或报名申请。
     * @return 查询到的用户 结果；不存在时为空。
     */
    Optional<UserVO> findByStudentId(String studentId);
    /**
     * 更新用户头像文件关联。
     *
     * @param user
     *            用户领域对象。
     * @param file
     *            文件领域对象或文件视图对象。
     * @return 数据库受影响行数。
     */
    int updateAvatar(UserVO user, FileVO file);
    /**
     * 更新用户头像文件关联。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param id
     *            业务记录主键。
     * @return 数据库受影响行数。
     */
    int updateAvatar(Long userId, Long id);
    /**
     * 更新用户微信二维码文件关联。
     *
     * @param user
     *            用户领域对象。
     * @param qrcode
     *            二维码领域对象或视图对象。
     * @return 数据库受影响行数。
     */
    int updateQrcode(UserVO user, QrcodeVO qrcode);

    /**
     * 更新用户个人资料字段。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param username
     *            用户姓名或登录名。
     * @param nickname
     *            用户昵称。
     * @param college
     *            学院名称。
     * @param major
     *            专业名称。
     * @param direction
     *            技术方向过滤条件。
     * @param gender
     *            性别。
     * @param bio
     *            个人简介。
     * @return 数据库受影响行数。
     */
    int updateProfile(Long userId, String username, String nickname, String college,
            String major, Direction direction, Gender gender, String bio);

    /**
     * 统计用户主页各标签页展示数量。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @return 查询或处理得到的用户 结果。
     */
    TabCountsVO getTabCounts(Long userId);

    /**
     * 按 GitHub 用户标识查询已绑定用户。
     *
     * @param githubId
     *            GitHub 用户唯一标识。
     * @return 查询到的用户 结果；不存在时为空。
     */
    Optional<UserVO> findByGithubId(String githubId);

    /**
     * 保存用户与 GitHub 账号的绑定信息。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param githubId
     *            GitHub 用户唯一标识。
     * @param githubUsername
     *            GitHub 登录名。
     */
    void updateGithubBinding(Long userId, String githubId, String githubUsername);

    /**
     * 清除用户与 GitHub 账号的绑定信息。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     */
    void clearGithubBinding(Long userId);

    /**
     * 更新用户邮箱地址。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param newEmail
     *            新的邮箱地址。
     */
    void updateEmail(Long userId, String newEmail);

    /**
     * 更新用户加密后的登录密码。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param encodedPassword
     *            加密后的密码。
     */
    void updatePassword(Long userId, String encodedPassword);

    /**
     * 判断内部推荐码是否已被用户占用。
     *
     * @param code
     *            验证码或推荐码。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsByInternalReferralCode(String code);

    // ========== Admin User Management ==========

    /**
     * 分页查询用户列表（支持筛选和搜索）
     *
     * @param pageable
     *            分页参数
     * @param roleId
     *            角色ID筛选
     * @param direction
     *            方向筛选
     * @param collegeId
     *            学院ID筛选
     * @param keyword
     *            学号/姓名关键词搜索
     * @return 分页用户实体列表
     */
    Page<User> findPage(Pageable pageable, Long roleId, Direction direction, Long collegeId, String keyword);

    /**
     * 按主键查询用户实体（管理员用）
     *
     * @param id
     *            用户ID
     * @return 用户实体
     */
    Optional<User> findEntityById(Long id);

    /**
     * 更新用户管理员可修改字段
     *
     * @param userId
     *            用户主键
     * @param roleId
     *            角色ID
     * @param direction
     *            方向
     * @param disable
     *            禁用状态
     * @param job
     *            岗位
     * @param studentId
     *            学号
     * @param email
     *            邮箱
     * @param username
     *            姓名
     * @param nickname
     *            昵称
     * @param collegeId
     *            学院ID
     * @param major
     *            专业
     * @param gender
     *            性别
     * @param assessmentGradeYear
     *            考核年级年份
     * @return 受影响行数
     */
    int updateAdminFields(Long userId, Long roleId, Direction direction, Boolean disable, String job,
            String studentId, String email, String username, String nickname,
            Long collegeId, String major, Gender gender, Integer assessmentGradeYear);

    /**
     * 级联删除用户及关联数据
     *
     * @param userId
     *            用户ID
     */
    void deleteByIdWithCascade(Long userId);

    /**
     * 批量删除用户及关联数据
     *
     * @param userIds
     *            用户ID列表
     */
    void batchDeleteByIds(List<Long> userIds);

    /**
     * 批量更新禁用状态
     *
     * @param userIds
     *            用户ID列表
     * @param disable
     *            禁用状态
     */
    void batchUpdateDisable(List<Long> userIds, Boolean disable);

    /**
     * 批量更新角色
     *
     * @param userIds
     *            用户ID列表
     * @param roleId
     *            角色ID
     */
    void batchUpdateRole(List<Long> userIds, Long roleId);

    /**
     * 统计用户关联数据数量
     *
     * @param userId
     *            用户ID
     * @return 关联数据统计
     */
    UserStatistics getStatistics(Long userId);

    /**
     * 用户关联数据统计
     */
    record UserStatistics(long experienceCount, long achievementCount, long answerCount, long commentCount) {
    }
}
