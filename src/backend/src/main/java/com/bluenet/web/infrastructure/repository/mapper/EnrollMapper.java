package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.EnrollDO;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EnrollMapper extends BaseMapper<EnrollDO> {
    /**
     * 按条件查询报名申请 数据行。
     *
     * @param studentId
     *            学生学号，用于定位用户或报名申请。
     * @return 匹配条件的报名申请 数据行；不存在时为 null。
     */
    EnrollDO selectByStudentId(@Param("studentId") String studentId);

    /**
     * 统计满足条件的报名申请 记录数量。
     *
     * @param studentId
     *            学生学号，用于定位用户或报名申请。
     * @return 满足条件的记录数量。
     */
    long countByStudentId(@Param("studentId") String studentId);

    /**
     * 统计指定学院下的用户数量。
     *
     * @param collegeId
     *            学院主键。
     * @return 满足条件的记录数量。
     */
    long countByCollegeId(@Param("collegeId") Long collegeId);

    /**
     * 查询报名申请 数据行。
     *
     * @param page
     *            分页请求或 MyBatis-Plus 分页对象。
     * @param keyword
     *            搜索关键字。
     * @param status
     *            业务状态过滤条件。
     * @param direction
     *            技术方向过滤条件。
     * @return 分页后的报名申请 结果。
     */
    IPage<EnrollDO> selectPageByConditions(IPage<EnrollDO> page,
            @Param("keyword") String keyword,
            @Param("status") EnrollStatus status,
            @Param("direction") Direction direction);

    /**
     * 处理报名申请 仓储职责中的业务数据访问逻辑。
     *
     * @return 满足条件的记录数量。
     */
    long countAll();

    /**
     * 统计满足条件的报名申请 记录数量。
     *
     * @param status
     *            业务状态过滤条件。
     * @return 满足条件的记录数量。
     */
    long countByStatus(@Param("status") EnrollStatus status);

    /**
     * 统计满足条件的报名申请 记录数量。
     *
     * @param direction
     *            技术方向过滤条件。
     * @return 满足条件的记录数量。
     */
    long countByDirection(@Param("direction") Direction direction);
}
