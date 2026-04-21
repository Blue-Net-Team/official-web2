package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import org.springframework.util.StringUtils;

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

        // 构建查询条件
        QueryWrapper<PermissionDO> queryWrapper = new QueryWrapper<>();

        // 关键词搜索：权限标识符或名称
        if (StringUtils.hasText(keyword)) {
            String keywordTrim = keyword.trim();
            queryWrapper.and(
                    wrapper -> wrapper
                            .like("value", keywordTrim)
                            .or()
                            .like("name", keywordTrim));
        }

        // 格式筛选：根据分隔符数量筛选
        if (StringUtils.hasText(format)) {
            String formatTrim = format.trim();
            if ("resource:action".equals(formatTrim)) {
                // 格式如 assessment:create（一个冒号）
                queryWrapper.apply("LENGTH(value) - LENGTH(REPLACE(value, ':', '')) = 1");
            } else if ("resource:subresource:action".equals(formatTrim)) {
                // 格式如 assessment:question:create（两个冒号）
                queryWrapper.apply("LENGTH(value) - LENGTH(REPLACE(value, ':', '')) = 2");
            } else if ("resource-action:action".equals(formatTrim)) {
                // 格式如 competition-sort:update（一个连字符和一个冒号）
                queryWrapper.like("value", "-");
            }
        }

        // 执行分页查询
        IPage<PermissionDO> permissionPage = permissionMapper.selectPage(page, queryWrapper);

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
        QueryWrapper<PermissionDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids);
        List<Permission> permissions = RepositoryObjectConverter
                .toDomainList(permissionMapper.selectList(queryWrapper), Permission.class);
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
