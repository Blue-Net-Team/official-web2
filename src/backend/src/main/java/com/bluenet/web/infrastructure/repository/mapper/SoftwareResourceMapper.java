package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.infrastructure.repository.dataobject.SoftwareResourceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Mapper for tb_software_resource.
 */
@Mapper
public interface SoftwareResourceMapper extends BaseMapper<SoftwareResourceDO> {

    /**
     * 分页查询指定方向下已启用的软件资源。
     *
     * @param page
     *            分页请求。
     * @param direction
     *            方向；为 null 时查询所有方向。
     * @return 分页后的软件资源。
     */
    IPage<SoftwareResourceDO> selectActiveByDirection(Page<SoftwareResourceDO> page,
            @Param("direction") Direction direction);

    /**
     * 分页查询所有软件资源（管理后台）。
     *
     * @param page
     *            分页请求。
     * @return 分页后的软件资源。
     */
    IPage<SoftwareResourceDO> selectAllForAdmin(Page<SoftwareResourceDO> page);
}
