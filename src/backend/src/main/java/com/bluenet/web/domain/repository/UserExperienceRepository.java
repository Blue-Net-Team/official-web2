package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.model.vo.ExperienceVO;

import java.util.List;
import java.util.Optional;

/**
 * 用户经历仓储接口
 */
public interface UserExperienceRepository {
    /**
     * 按主键查询用户经历 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的用户经历 结果；不存在时为空。
     */
    Optional<ExperienceVO> findById(Long id);

    /**
     * 查询指定用户关联的用户经历 记录。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @return 满足条件的用户经历 结果集合。
     */
    List<ExperienceVO> findByUserId(Long userId);

    /**
     * 查询用户指定类型的经历列表。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param type
     *            业务类型或枚举类型。
     * @return 满足条件的用户经历 结果集合。
     */
    List<ExperienceVO> findByUserIdAndType(Long userId, ExperienceType type);

    /**
     * 保存新的用户经历 记录。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param type
     *            业务类型或枚举类型。
     * @param title
     *            经历或展示项标题。
     * @param startTime
     *            经历开始时间。
     * @param endTime
     *            经历结束时间。
     * @param content
     *            作答内容、经历内容或题目内容。
     * @return 查询或处理得到的用户经历 结果。
     */
    ExperienceVO save(Long userId, ExperienceType type, String title,
            String startTime, String endTime, String content);

    /**
     * 更新已有用户经历 记录。
     *
     * @param id
     *            业务记录主键。
     * @param title
     *            经历或展示项标题。
     * @param startTime
     *            经历开始时间。
     * @param endTime
     *            经历结束时间。
     * @param content
     *            作答内容、经历内容或题目内容。
     * @return 数据库受影响行数。
     */
    ExperienceVO update(Long id, String title, String startTime, String endTime, String content);

    /**
     * 删除指定用户经历 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 数据库受影响行数。
     */
    boolean deleteById(Long id);

    /**
     * 统计用户指定类型的经历数量。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param type
     *            业务类型或枚举类型。
     * @return 满足条件的记录数量。
     */
    int countByUserIdAndType(Long userId, ExperienceType type);

    /**
     * 校验用户是否拥有指定经历记录。
     *
     * @param experienceId
     *            用户经历记录主键。
     * @param userId
     *            用户主键，用于限定用户范围。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean checkOwner(Long experienceId, Long userId);
}
