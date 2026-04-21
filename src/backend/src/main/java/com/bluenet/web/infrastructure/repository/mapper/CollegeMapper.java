package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.CollegeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CollegeMapper extends BaseMapper<CollegeDO> {
    /**
     * 统计满足条件的学院 记录数量。
     *
     * @param name
     *            业务对象名称。
     * @return 满足条件的记录数量。
     */
    long countByName(@Param("name") String name);

    /**
     * 统计满足条件的学院 记录数量。
     *
     * @param name
     *            业务对象名称。
     * @param excludeId
     *            需要排除的当前记录主键。
     * @return 满足条件的记录数量。
     */
    long countByNameAndIdNot(@Param("name") String name, @Param("excludeId") Long excludeId);
}
