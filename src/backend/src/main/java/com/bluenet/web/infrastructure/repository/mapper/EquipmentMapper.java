package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.domain.model.entity.Equipment;
import com.bluenet.web.domain.model.vo.EquipmentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 设备Mapper接口
 */
@Mapper
public interface EquipmentMapper extends BaseMapper<Equipment> {
    /**
     * 查询所有设备（按排序号降序，包含图片URL）
     */
    List<EquipmentVO> selectAllOrderBySortOrderDesc();

    /**
     * 根据ID查询设备（包含图片URL）
     */
    Optional<EquipmentVO> selectByIdWithImageUrl(@Param("id") Long id);
}
