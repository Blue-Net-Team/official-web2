package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.UserDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
    /**
     * 按条件查询用户 数据行。
     *
     * @param email
     *            邮箱地址，用于定位用户或验证码。
     * @return 匹配条件的用户 数据行；不存在时为 null。
     */
    UserDO selectByEmail(String email);

    /**
     * 按条件查询用户 数据行。
     *
     * @param studentId
     *            学生学号，用于定位用户或报名申请。
     * @return 匹配条件的用户 数据行；不存在时为 null。
     */
    UserDO selectByStudentId(String studentId);

    /**
     * 按内部推荐码查询用户数据行。
     *
     * @param code
     *            验证码或推荐码。
     * @return 匹配条件的用户 数据行；不存在时为 null。
     */
    UserDO selectByInternalReferralCode(String code);

    /**
     * 更新用户头像文件关联。
     *
     * @param id
     *            业务记录主键。
     * @param avatarId
     *            头像文件主键。
     * @return 数据库受影响行数。
     */
    int updateAvatarId(Long id, Long avatarId);

    /**
     * 更新用户微信二维码文件关联。
     *
     * @param id
     *            业务记录主键。
     * @param qrcodeId
     *            二维码文件主键。
     * @return 数据库受影响行数。
     */
    int updateQrcodeId(Long id, Long qrcodeId);

    /**
     * 按角色名称、技术方向和排除用户名分页查询用户数据行。
     *
     * @param page
     *            分页请求或 MyBatis-Plus 分页对象。
     * @param roleNames
     *            角色名称集合。
     * @param direction
     *            技术方向过滤条件。
     * @param requireDirectionNotNull
     *            是否只查询已设置方向的用户。
     * @param excludeUsername
     *            需要排除的用户名。
     * @return 分页后的用户 结果。
     */
    IPage<UserDO> selectByRoleNamesAndDirection(
            Page<UserDO> page,
            @Param("roleNames") List<String> roleNames,
            @Param("direction") Direction direction,
            @Param("requireDirectionNotNull") Boolean requireDirectionNotNull,
            @Param("excludeUsername") String excludeUsername);

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
    int updateProfile(
            @Param("userId") Long userId,
            @Param("username") String username,
            @Param("nickname") String nickname,
            @Param("college") String college,
            @Param("major") String major,
            @Param("direction") Direction direction,
            @Param("gender") Gender gender,
            @Param("bio") String bio);

    /**
     * 按条件查询用户 数据行。
     *
     * @param githubId
     *            GitHub 用户唯一标识。
     * @return 匹配条件的用户 数据行；不存在时为 null。
     */
    UserDO selectByGithubId(String githubId);

    /**
     * 保存用户与 GitHub 账号的绑定信息。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param githubId
     *            GitHub 用户唯一标识。
     * @param githubUsername
     *            GitHub 登录名。
     * @return 数据库受影响行数。
     */
    int updateGithubBinding(@Param("userId") Long userId,
            @Param("githubId") String githubId,
            @Param("githubUsername") String githubUsername);

    /**
     * 清除用户与 GitHub 账号的绑定信息。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @return 匹配条件的用户 数据行；不存在时为 null。
     */
    int clearGithubBinding(@Param("userId") Long userId);

    /**
     * 更新用户邮箱地址。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param email
     *            邮箱地址，用于定位用户或验证码。
     * @return 数据库受影响行数。
     */
    int updateEmail(@Param("userId") Long userId, @Param("email") String email);

    /**
     * 更新用户加密后的登录密码。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param password
     *            加密后的密码。
     * @return 数据库受影响行数。
     */
    int updatePassword(@Param("userId") Long userId, @Param("password") String encodedPassword);

    /**
     * 统计指定学院下的用户数量。
     *
     * @param collegeId
     *            学院主键。
     * @return 满足条件的记录数量。
     */
    long countByCollegeId(@Param("collegeId") Long collegeId);

    /**
     * 查询淘汰超过指定时间且未被禁用的用户ID列表。
     *
     * @param cutoffTime
     *            截止时间，decided_at 早于该时间的淘汰决策会被选中。
     * @return 需要被禁用的用户ID列表。
     */
    List<Long> selectUserIdsToDisableByElimination(@Param("cutoffTime") LocalDateTime cutoffTime);
}
