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
     * 根据ID查询经历
     *
     * @param id
     *            经历ID
     * @return 经历值对象
     */
    Optional<ExperienceVO> findById(Long id);

    /**
     * 查询用户所有经历
     *
     * @param userId
     *            用户ID
     * @return 经历列表
     */
    List<ExperienceVO> findByUserId(Long userId);

    /**
     * 查询用户指定类型的经历
     *
     * @param userId
     *            用户ID
     * @param type
     *            经历类型
     * @return 经历列表
     */
    List<ExperienceVO> findByUserIdAndType(Long userId, ExperienceType type);

    /**
     * 保存经历
     *
     * @param userId
     *            用户ID
     * @param type
     *            经历类型
     * @param title
     *            标题
     * @param startTime
     *            开始时间
     * @param endTime
     *            结束时间
     * @param content
     *            JSON内容
     * @return 保存后的经历
     */
    ExperienceVO save(Long userId, ExperienceType type, String title,
            String startTime, String endTime, String content);

    /**
     * 更新经历
     *
     * @param id
     *            经历ID
     * @param title
     *            标题
     * @param startTime
     *            开始时间
     * @param endTime
     *            结束时间
     * @param content
     *            JSON内容
     * @return 更新后的经历
     */
    ExperienceVO update(Long id, String title, String startTime, String endTime, String content);

    /**
     * 删除经历
     *
     * @param id
     *            经历ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);

    /**
     * 统计用户指定类型经历数量
     *
     * @param userId
     *            用户ID
     * @param type
     *            经历类型
     * @return 数量
     */
    int countByUserIdAndType(Long userId, ExperienceType type);

    /**
     * 检查经历是否属于指定用户
     *
     * @param experienceId
     *            经历ID
     * @param userId
     *            用户ID
     * @return 是否属于该用户
     */
    boolean checkOwner(Long experienceId, Long userId);
}
