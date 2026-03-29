package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.domain.model.entity.Venue;
import com.bluenet.web.domain.model.vo.VenueVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 场地Mapper接口
 */
@Mapper
public interface VenueMapper extends BaseMapper<Venue> {
    /**
     * 查询所有场地（按排序号降序，包含图片URL）
     */
    List<VenueVO> selectAllOrderBySortOrderDesc();

    /**
     * 根据ID查询场地（包含图片URL）
     */
    Optional<VenueVO> selectByIdWithImageUrl(@Param("id") Long id);
}
