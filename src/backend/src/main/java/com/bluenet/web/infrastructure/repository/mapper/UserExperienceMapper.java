package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.infrastructure.repository.dataobject.UserExperienceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserExperienceMapper extends BaseMapper<UserExperienceDO> {
    /**
     * 按条件查询用户经历 数据行。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @return 满足条件的用户经历 结果集合。
     */
    List<UserExperienceDO> selectByUserId(@Param("userId") Long userId);

    /**
     * 按条件查询用户经历 数据行。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param type
     *            业务类型或枚举类型。
     * @return 满足条件的用户经历 结果集合。
     */
    List<UserExperienceDO> selectByUserIdAndType(@Param("userId") Long userId, @Param("type") ExperienceType type);

    /**
     * 统计用户指定类型的经历数量。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param type
     *            业务类型或枚举类型。
     * @return 满足条件的记录数量。
     */
    long countByUserIdAndType(@Param("userId") Long userId, @Param("type") ExperienceType type);
}
