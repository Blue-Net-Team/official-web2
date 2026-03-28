package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.model.vo.ExperienceVO;

import java.util.List;
import java.util.Optional;

/**
 * 用户经历领域服务
 * <p>
 * 处理用户经历（项目/竞赛/实习）的领域逻辑。
 * </p>
 */
public interface UserExperienceDomainService {
    /**
     * 获取用户所有经历
     *
     * @param userId
     *            用户ID
     * @return 经历列表
     */
    List<ExperienceVO> getExperiences(Long userId);

    /**
     * 获取用户指定类型的经历
     *
     * @param userId
     *            用户ID
     * @param type
     *            经历类型
     * @return 经历列表
     */
    List<ExperienceVO> getExperiencesByType(Long userId, ExperienceType type);

    /**
     * 获取指定经历的详情
     *
     * @param experienceId
     *            经历ID
     * @param userId
     *            用户ID（用于权限校验）
     * @return 经历详情
     */
    Optional<ExperienceVO> getExperienceById(Long experienceId, Long userId);

    /**
     * 创建经历
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
     *            结束时间（可为null）
     * @param content
     *            JSON格式内容
     * @return 创建的经历
     */
    ExperienceVO createExperience(Long userId, ExperienceType type, String title,
            String startTime, String endTime, String content);

    /**
     * 更新经历
     *
     * @param experienceId
     *            经历ID
     * @param userId
     *            用户ID（用于权限校验）
     * @param title
     *            标题
     * @param startTime
     *            开始时间
     * @param endTime
     *            结束时间（可为null）
     * @param content
     *            JSON格式内容
     * @return 更新后的经历
     */
    ExperienceVO updateExperience(Long experienceId, Long userId, String title,
            String startTime, String endTime, String content);

    /**
     * 删除经历
     *
     * @param experienceId
     *            经历ID
     * @param userId
     *            用户ID（用于权限校验）
     * @return 是否删除成功
     */
    boolean deleteExperience(Long experienceId, Long userId);

    /**
     * 获取用户各类型经历的数量
     *
     * @param userId
     *            用户ID
     * @return Tab计数
     */
    TabCounts getTabCounts(Long userId);

    /**
     * Tab计数
     */
    record TabCounts(int projects, int competitions, int internships) {
    }
}
