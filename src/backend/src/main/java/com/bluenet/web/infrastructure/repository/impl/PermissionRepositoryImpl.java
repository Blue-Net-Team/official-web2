package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.Permission;
import com.bluenet.web.domain.model.vo.PermissionVO;
import com.bluenet.web.domain.repository.PermissionRepository;
import com.bluenet.web.infrastructure.repository.mapper.PermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 权限仓储实现
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {

    private final PermissionMapper permissionMapper;

    /**
     * 按主键查询权限 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的权限 结果；不存在时为空。
     */
    @Override
    public Optional<PermissionVO> findById(Long id) {
        Permission permission = RepositoryObjectConverter.toDomain(permissionMapper.selectById(id), Permission.class);
        if (permission == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(permission));
    }

    /**
     * 查询全部权限 记录。
     *
     * @return 满足条件的权限 结果集合。
     */
    @Override
    public List<PermissionVO> findAll() {
        List<Permission> permissions = RepositoryObjectConverter
                .toDomainList(permissionMapper.selectList(null), Permission.class);
        return permissions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 查询全部权限 记录。
     *
     * @param keyword
     *            搜索关键字。
     * @param format
     *            权限返回格式或展示格式。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的权限 结果。
     */
    @Override
    public org.springframework.data.domain.Page<PermissionVO> findAll(String keyword, String format,
            Pageable pageable) {
        // 构建 MyBatis-Plus 分页参数（Spring Data Pageable 页码从0开始，MyBatis-Plus 从1开始）
        int pageNum = pageable.getPageNumber() + 1;
        int pageSize = pageable.getPageSize();
        Page<PermissionDO> page = new Page<>(pageNum, pageSize);

        // 查询条件由 XML 承载，避免在 Java 代码中拼写 SQL 逻辑。
        IPage<PermissionDO> permissionPage = permissionMapper.selectPageByConditions(page, keyword, format);

        // 转换为 VO
        List<PermissionVO> content = permissionPage.getRecords()
                .stream()
                .map(permission -> RepositoryObjectConverter.toDomain(permission, Permission.class))
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建 Spring Data Page 对象
        return new PageImpl<>(
                content,
                pageable,
                permissionPage.getTotal());
    }

    /**
     * 按主键集合批量查询权限 记录。
     *
     * @param ids
     *            业务记录主键集合。
     * @return 满足条件的权限 结果集合。
     */
    @Override
    public List<PermissionVO> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<Permission> permissions = RepositoryObjectConverter
                .toDomainList(permissionMapper.selectByIds(ids), Permission.class);
        return permissions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 在权限 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param permission
     *            权限领域对象。
     * @return 转换后的目标模型对象。
     */
    private PermissionVO convertToVO(Permission permission) {
        return PermissionVO.builder()
                .id(permission.getId())
                .name(permission.getName())
                .value(permission.getValue())
                .url(permission.getUrl())
                .method(permission.getMethod())
                .accessLevel(permission.getAccessLevel())
                .build();
    }
}
