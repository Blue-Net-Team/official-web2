package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.RoleDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMapper extends BaseMapper<RoleDO> {
    /**
     * 按条件查询角色 数据行。
     *
     * @param name
     *            业务对象名称。
     * @return 匹配条件的角色 数据行；不存在时为 null。
     */
    RoleDO selectByName(String name);
}
