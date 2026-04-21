package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.EquipmentDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Mapper for tb_equipment only; RepositoryImpl assembles related file data.
 */
@Mapper
public interface EquipmentMapper extends BaseMapper<EquipmentDO> {
    /**
     * 查询设备 数据行。
     *
     * @return 满足条件的设备 结果集合。
     */
    List<EquipmentDO> selectAllOrderBySortOrderDesc();

    /**
     * 查询设备 数据行。
     *
     * @param id
     *            业务记录主键。
     * @return 匹配条件的设备 数据行；不存在时为 null。
     */
    EquipmentDO selectEquipmentById(@Param("id") Long id);
}
