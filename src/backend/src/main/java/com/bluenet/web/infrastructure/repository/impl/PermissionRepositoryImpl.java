package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.Permission;
import com.bluenet.web.domain.repository.PermissionRepository;
import com.bluenet.web.infrastructure.repository.converter.PermissionRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.PermissionDO;
import com.bluenet.web.infrastructure.repository.mapper.PermissionMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 权限仓储实现
 * <p>
 * 实现权限数据的持久化操作，使用显式转换器替代 BeanUtils
 * </p>
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {

    private final PermissionMapper permissionMapper;
    private final PermissionRepositoryConverter converter;

    @Override
    public Optional<Permission> findById(Long id) {
        PermissionDO dataObject = permissionMapper.selectById(id);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    @Override
    public List<Permission> findAll() {
        List<PermissionDO> dataObjects = permissionMapper.selectList(null);
        return converter.toEntityList(dataObjects);
    }

    @Override
    public org.springframework.data.domain.Page<Permission> findAll(String keyword, String format, Pageable pageable) {
        // 构建 MyBatis-Plus 分页参数（Spring Data Pageable 页码从0开始，MyBatis-Plus 从1开始）
        int pageNum = pageable.getPageNumber() + 1;
        int pageSize = pageable.getPageSize();
        Page<PermissionDO> page = new Page<>(pageNum, pageSize);

        // 查询条件由 XML 承载，避免在 Java 代码中拼写 SQL 逻辑。
        IPage<PermissionDO> permissionPage = permissionMapper.selectPageByConditions(page, keyword, format);

        // 转换为 Entity
        List<Permission> content = converter.toEntityList(permissionPage.getRecords());

        // 构建 Spring Data Page 对象
        return new PageImpl<>(content, pageable, permissionPage.getTotal());
    }

    @Override
    public List<Permission> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<PermissionDO> dataObjects = permissionMapper.selectByIds(ids);
        return converter.toEntityList(dataObjects);
    }

    @Override
    public void save(Permission permission) {
        PermissionDO dataObject = converter.toDataObject(permission);
        if (dataObject.getId() == null) {
            permissionMapper.insert(dataObject);
            permission.setId(dataObject.getId());
        } else {
            permissionMapper.updateById(dataObject);
        }
    }
    @Override
    public void deleteById(Long id) {
        permissionMapper.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return permissionMapper.selectById(id) != null;
    }
}
