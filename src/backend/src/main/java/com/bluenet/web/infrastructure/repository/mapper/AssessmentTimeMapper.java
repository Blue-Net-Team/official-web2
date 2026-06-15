package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentTimeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AssessmentTimeMapper extends BaseMapper<AssessmentTimeDO> {
    /**
     * 统计满足条件的考核场次 记录数量。
     *
     * @param direction
     *            技术方向过滤条件。
     * @param epoch
     *            考核批次或轮次。
     * @param grade
     *            考核年级。
     * @return 满足条件的记录数量。
     */
    long countByDirectionEpochGrade(@Param("direction") Direction direction, @Param("epoch") Integer epoch,
            @Param("grade") Integer grade);

    /**
     * 统计满足条件的考核场次 记录数量。
     *
     * @param direction
     *            技术方向过滤条件。
     * @param epoch
     *            考核批次或轮次。
     * @param grade
     *            考核年级。
     * @param excludeId
     *            需要排除的当前记录主键。
     * @return 满足条件的记录数量。
     */
    long countByDirectionEpochGradeAndIdNot(@Param("direction") Direction direction, @Param("epoch") Integer epoch,
            @Param("grade") Integer grade, @Param("excludeId") Long excludeId);

    /**
     * 处理考核场次 仓储职责中的业务数据访问逻辑。
     *
     * @param id
     *            业务记录主键。
     * @return 匹配条件的考核场次 数据行；不存在时为 null。
     */
    int clearTimeLimitMinutesById(@Param("id") Long id);

    /**
     * 查询考核场次 数据行。
     *
     * @param page
     *            分页请求或 MyBatis-Plus 分页对象。
     * @param direction
     *            技术方向过滤条件。
     * @param grade
     *            考核年级。
     * @return 分页后的考核场次 结果。
     */
    IPage<AssessmentTimeDO> selectPageByFilters(IPage<AssessmentTimeDO> page, @Param("direction") Direction direction,
            @Param("grade") Integer grade);

    /**
     * 查询考核场次 数据行。
     *
     * @param page
     *            分页请求或 MyBatis-Plus 分页对象。
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param direction
     *            技术方向过滤条件。
     * @param enrollmentYear
     *            入学年份过滤条件。
     * @return 分页后的考核场次 结果。
     */
    IPage<AssessmentTimeDO> selectPageByUserParticipation(IPage<AssessmentTimeDO> page, @Param("userId") Long userId,
            @Param("direction") Direction direction, @Param("enrollmentYear") Integer enrollmentYear);

    /**
     * 统计全局考核（direction IS NULL）下满足轮次和年级的记录数量。
     *
     * @param epoch
     *            考核批次或轮次。
     * @param grade
     *            考核年级（可为 null）。
     * @return 满足条件的记录数量。
     */
    long countByEpochGrade(@Param("epoch") Integer epoch, @Param("grade") Integer grade);

    /**
     * 统计同方向同轮次下是否存在与指定 grade 形式冲突的记录。 当 grade 为 null 时，查询是否存在 grade IS NOT NULL
     * 的记录； 当 grade 不为 null 时，查询是否存在 grade IS NULL 的记录。
     *
     * @param direction
     *            技术方向过滤条件。
     * @param epoch
     *            考核批次或轮次。
     * @param grade
     *            考核年级。
     * @return 满足条件的记录数量。
     */
    long countConflictingGradeByDirectionAndEpoch(@Param("direction") Direction direction,
            @Param("epoch") Integer epoch, @Param("grade") Integer grade);

    /**
     * 统计同方向同轮次下（排除自身）是否存在与指定 grade 形式冲突的记录。
     *
     * @param direction
     *            技术方向过滤条件。
     * @param epoch
     *            考核批次或轮次。
     * @param grade
     *            考核年级。
     * @param excludeId
     *            需要排除的当前记录主键。
     * @return 满足条件的记录数量。
     */
    long countConflictingGradeByDirectionAndEpochAndIdNot(@Param("direction") Direction direction,
            @Param("epoch") Integer epoch, @Param("grade") Integer grade, @Param("excludeId") Long excludeId);
}
