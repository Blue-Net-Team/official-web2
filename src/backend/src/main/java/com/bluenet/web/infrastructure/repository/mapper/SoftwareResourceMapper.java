package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceDirection;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceStatus;
import com.bluenet.web.domain.repository.SoftwareResourceRepository;
import com.bluenet.web.infrastructure.repository.dataobject.SoftwareResourceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Mapper for tb_software_resource.
 */
@Mapper
public interface SoftwareResourceMapper extends BaseMapper<SoftwareResourceDO> {

    /**
     * 分页查询指定方向下已启用的软件资源。
     * <p>
     * 当 {@code directions} 不为空时，返回方向在列表中的资源；为 null 或空时查询所有方向。 当 {@code keyword}
     * 不为空时，按名称、分类、描述做不区分大小写模糊匹配。
     * </p>
     *
     * @param page
     *            分页请求。
     * @param directions
     *            方向列表；为 null 或空时查询所有方向。
     * @param activeStatus
     *            启用状态值。
     * @param keyword
     *            搜索关键词；为 null 或空时忽略。
     * @return 分页后的软件资源。
     */
    IPage<SoftwareResourceDO> selectActiveByDirection(Page<SoftwareResourceDO> page,
            @Param("directions") List<SoftwareResourceDirection> directions,
            @Param("activeStatus") SoftwareResourceStatus activeStatus,
            @Param("keyword") String keyword);

    /**
     * 分页查询所有软件资源（管理后台）。
     *
     * @param page
     *            分页请求。
     * @return 分页后的软件资源。
     */
    IPage<SoftwareResourceDO> selectAllForAdmin(Page<SoftwareResourceDO> page);

    /**
     * 批量更新软件资源的排序号。
     * <p>
     * 使用单条 {@code CASE WHEN} SQL 一次性更新多条记录的排序号，避免逐条循环执行 SQL。
     * </p>
     *
     * @param items
     *            排序项列表（id 与目标排序号）。
     */
    void batchUpdateSortOrder(@Param("items") List<SoftwareResourceRepository.SortItem> items);
}
