package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 考核时间仓库接口
 * <p>
 * 负责考核时间数据的持久化操作
 * </p>
 */
public interface AssessmentTimeRepository {
    /**
     * 根据ID查询考核时间
     *
     * @param id
     *            考核时间ID
     * @return 考核时间信息，如果不存在则返回Optional.empty()
     */
    Optional<AssessmentTimeVO> findById(Long id);

    /**
     * 保存考核时间
     *
     * @param vo
     *            考核时间VO
     * @return 保存后的考核时间ID
     */
    Long save(AssessmentTimeVO vo);

    /**
     * 更新考核时间
     *
     * @param vo
     *            考核时间VO
     */
    void update(AssessmentTimeVO vo);

    /**
     * 根据ID删除考核时间
     *
     * @param id
     *            考核时间ID
     */
    void deleteById(Long id);

    /**
     * 检查考核时间是否存在
     *
     * @param id
     *            考核时间ID
     * @return 如果存在返回true，否则返回false
     */
    boolean existsById(Long id);

    /**
     * 检查方向+届次+年级组合是否已存在
     *
     * @param direction
     *            方向
     * @param epoch
     *            届次
     * @param grade
     *            年级
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByDirectionAndEpochAndGrade(Direction direction, Integer epoch, Integer grade);

    /**
     * 检查方向+届次+年级组合是否已存在（排除指定ID）
     *
     * @param direction
     *            方向
     * @param epoch
     *            届次
     * @param grade
     *            年级
     * @param excludeId
     *            排除的考核时间ID
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByDirectionAndEpochAndGradeAndIdNot(Direction direction, Integer epoch, Integer grade,
            Long excludeId);

    /**
     * 检查是否有关联的考核题目
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @return 如果有关联题目返回true，否则返回false
     */
    boolean hasAssociatedQuestions(Long assessmentTimeId);

    /**
     * 分页查询考核时间（支持方向和年级过滤）
     *
     * @param direction
     *            方向（null表示不过滤）
     * @param grade
     *            年级（null表示不过滤）
     * @param pageable
     *            分页参数
     * @return 考核时间分页结果
     */
    Page<AssessmentTimeVO> findByFilters(Direction direction, Integer grade, Pageable pageable);

    /**
     * 按用户参与视角查询考核时间（分配给用户的 + 用户参与过的）
     *
     * @param userId
     *            当前用户ID
     * @param direction
     *            当前用户方向（null时仅按answer过滤）
     * @param enrollmentYear
     *            当前用户入学年份（null时仅按answer过滤）
     * @param pageable
     *            分页参数
     * @return 考核时间分页结果
     */
    Page<AssessmentTimeVO> findByUserParticipation(Long userId, Direction direction, Integer enrollmentYear,
            Pageable pageable);
}
