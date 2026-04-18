package com.bluenet.web.infrastructure.repository.impl;

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

    @Override
    public Optional<PermissionVO> findById(Long id) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(permission));
    }

    @Override
    public List<PermissionVO> findAll() {
        List<Permission> permissions = permissionMapper.selectList(null);
        return permissions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public org.springframework.data.domain.Page<PermissionVO> findAll(String keyword, String format,
            Pageable pageable) {
        // 构建 MyBatis-Plus 分页参数（Spring Data Pageable 页码从0开始，MyBatis-Plus 从1开始）
        int pageNum = pageable.getPageNumber() + 1;
        int pageSize = pageable.getPageSize();
        Page<Permission> page = new Page<>(pageNum, pageSize);

        // 构建查询条件
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();

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
        IPage<Permission> permissionPage = permissionMapper.selectPage(page, queryWrapper);

        // 转换为 VO
        List<PermissionVO> content = permissionPage.getRecords()
                .stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建 Spring Data Page 对象
        return new PageImpl<>(
                content,
                pageable,
                permissionPage.getTotal());
    }

    @Override
    public List<PermissionVO> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids);
        List<Permission> permissions = permissionMapper.selectList(queryWrapper);
        return permissions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 将 Permission 实体转换为 PermissionVO 值对象
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
