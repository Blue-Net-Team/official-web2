package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.VenueDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Mapper for tb_venue only; RepositoryImpl assembles related file data.
 */
@Mapper
public interface VenueMapper extends BaseMapper<VenueDO> {
    /**
     * 查询场地 数据行。
     *
     * @return 满足条件的场地 结果集合。
     */
    List<VenueDO> selectAllOrderBySortOrderDesc();

    /**
     * 查询场地 数据行。
     *
     * @param id
     *            业务记录主键。
     * @return 匹配条件的场地 数据行；不存在时为 null。
     */
    VenueDO selectVenueById(@Param("id") Long id);
}
