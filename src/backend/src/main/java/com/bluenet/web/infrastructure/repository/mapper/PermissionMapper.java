package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.PermissionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<PermissionDO> {
    /**
     * 按条件查询权限 数据行。
     *
     * @param roleId
     *            角色主键。
     * @return 满足条件的权限 结果集合。
     */
    List<String> selectByRoleId(Long roleId);
}
