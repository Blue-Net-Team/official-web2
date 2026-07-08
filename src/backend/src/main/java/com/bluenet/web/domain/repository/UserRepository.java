package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.TabCountsVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    /**
     * 保存或更新用户记录。
     *
     * @param user
     *            用户领域对象。若 id 为空则插入，否则按 id 更新。
     */
    void save(User user);

    /**
     * 批量保存或更新用户记录。
     *
     * @param users
     *            用户领域对象列表。每个对象若 id 为空则插入，否则按 id 更新。
     */
    void saveAll(List<User> users);
    /**
     * 按主键查询用户 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的用户 结果；不存在时为空。
     */
    Optional<User> findById(Long id);
    /**
     * 按邮箱查询用户视图。
     *
     * @param email
     *            邮箱地址，用于定位用户或验证码。
     * @return 查询到的用户 结果；不存在时为空。
     */
    Optional<User> findByEmail(String email);
    /**
     * 按学号查询用户或报名申请。
     *
     * @param studentId
     *            学生学号，用于定位用户或报名申请。
     * @return 查询到的用户 结果；不存在时为空。
     */
    Optional<User> findByStudentId(String studentId);
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
    Optional<User> findByGithubId(String githubId);

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
     * 级联删除用户及关联数据
     *
     * @param userId
     *            用户ID
     */
    void deleteByIdWithCascade(Long userId);

    /**
     * 统计用户关联数据数量
     *
     * @param userId
     *            用户ID
     * @return 关联数据统计
     */
    UserStatistics getStatistics(Long userId);

    /**
     * 查询淘汰超过指定时间且未被禁用的用户ID列表。
     *
     * @param cutoffTime
     *            截止时间，decided_at 早于该时间的淘汰决策会被选中。
     * @return 需要被禁用的用户ID列表。
     */
    List<Long> findUserIdsToDisableByElimination(LocalDateTime cutoffTime);

    /**
     * 用户关联数据统计
     */
    record UserStatistics(long experienceCount, long achievementCount, long answerCount, long commentCount) {
    }
}
