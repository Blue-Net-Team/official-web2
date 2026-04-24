package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.enumerate.Direction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 考核时间仓库接口
 * <p>
 * 负责考核时间数据的持久化操作，只操作 Entity，不暴露 VO 或 DTO
 * </p>
 */
public interface AssessmentTimeRepository {
    /**
     * 按主键查询考核场次记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的考核场次实体；不存在时为 Optional.empty()。
     */
    Optional<AssessmentTime> findById(Long id);

    /**
     * 保存新的考核场次记录。
     *
     * @param assessmentTime
     *            考核时间实体。
     */
    void save(AssessmentTime assessmentTime);

    /**
     * 更新已有考核场次记录。
     *
     * @param assessmentTime
     *            考核时间实体（id 必须非空）。
     */
    void update(AssessmentTime assessmentTime);

    /**
     * 删除指定考核场次记录。
     *
     * @param id
     *            业务记录主键。
     */
    void deleteById(Long id);

    /**
     * 判断是否存在满足条件的考核场次记录。
     *
     * @param id
     *            业务记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsById(Long id);

    /**
     * 判断是否存在满足条件的考核场次记录。
     *
     * @param direction
     *            技术方向过滤条件。
     * @param epoch
     *            考核批次或轮次。
     * @param grade
     *            考核年级。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsByDirectionAndEpochAndGrade(Direction direction, Integer epoch, Integer grade);

    /**
     * 判断除当前记录外是否存在相同业务唯一键的考核场次记录。
     *
     * @param direction
     *            技术方向过滤条件。
     * @param epoch
     *            考核批次或轮次。
     * @param grade
     *            考核年级。
     * @param excludeId
     *            需要排除的当前记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsByDirectionAndEpochAndGradeAndIdNot(Direction direction, Integer epoch, Integer grade,
            Long excludeId);

    /**
     * 判断考核场次下是否仍有关联题目。
     *
     * @param assessmentTimeId
     *            考核场次主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean hasAssociatedQuestions(Long assessmentTimeId);

    /**
     * 按组合筛选条件分页查询考核场次实体。
     *
     * @param direction
     *            技术方向过滤条件。
     * @param grade
     *            考核年级。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的考核场次结果。
     */
    Page<AssessmentTime> findByFilters(Direction direction, Integer grade, Pageable pageable);

    /**
     * 查询用户已经参与过的考核场次。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param direction
     *            技术方向过滤条件。
     * @param enrollmentYear
     *            入学年份过滤条件。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的考核场次结果。
     */
    Page<AssessmentTime> findByUserParticipation(Long userId, Direction direction, Integer enrollmentYear,
            Pageable pageable);

    /**
     * 查询指定方向和年级的最大考核轮次。
     *
     * @param direction
     *            技术方向过滤条件。
     * @param grade
     *            考核年级。
     * @return 最大轮次；不存在时返回 Optional.empty()。
     */
    Optional<Integer> findMaxEpochByDirectionAndGrade(Direction direction, Integer grade);
}
