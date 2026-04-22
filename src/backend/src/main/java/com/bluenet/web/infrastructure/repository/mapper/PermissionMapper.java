package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bluenet.web.infrastructure.repository.dataobject.PermissionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    /**
     * 按关键字和权限格式分页查询权限数据行。
     *
     * @param page
     *            MyBatis-Plus 分页请求对象。
     * @param keyword
     *            权限标识或名称关键字。
     * @param format
     *            权限标识格式过滤条件。
     * @return 分页后的权限数据行。
     */
    IPage<PermissionDO> selectPageByConditions(IPage<PermissionDO> page,
            @Param("keyword") String keyword,
            @Param("format") String format);

    /**
     * 按主键集合批量查询权限数据行。
     *
     * @param ids
     *            权限主键集合。
     * @return 匹配主键集合的权限数据行。
     */
    List<PermissionDO> selectByIds(@Param("ids") List<Long> ids);
}
