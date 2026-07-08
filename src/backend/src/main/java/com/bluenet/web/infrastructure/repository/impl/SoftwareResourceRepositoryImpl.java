package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.SoftwareResource;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceDirection;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceStatus;
import com.bluenet.web.domain.repository.SoftwareResourceRepository;
import com.bluenet.web.infrastructure.repository.converter.SoftwareResourceRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.SoftwareResourceDO;
import com.bluenet.web.infrastructure.repository.mapper.SoftwareResourceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 软件资源仓库实现类。
 * <p>
 * 实现软件资源数据的持久化操作，使用显式转换器替代 BeanUtils。
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class SoftwareResourceRepositoryImpl implements SoftwareResourceRepository {

    private final SoftwareResourceMapper softwareResourceMapper;
    private final SoftwareResourceRepositoryConverter converter;

    @Override
    public Optional<SoftwareResource> findById(Long id) {
        SoftwareResourceDO dataObject = softwareResourceMapper.selectById(id);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    @Override
    public org.springframework.data.domain.Page<SoftwareResource> findActiveByDirection(
            SoftwareResourceDirection direction,
            String keyword,
            Pageable pageable) {
        Page<SoftwareResourceDO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        List<SoftwareResourceDirection> directions = direction == null
                ? null
                : List.of(direction, SoftwareResourceDirection.GENERAL);
        IPage<SoftwareResourceDO> result = softwareResourceMapper.selectActiveByDirection(
                page,
                directions,
                SoftwareResourceStatus.ACTIVE,
                keyword);
        List<SoftwareResource> content = converter.toEntityList(result.getRecords());
        return new PageImpl<>(content, pageable, result.getTotal());
    }

    @Override
    public void save(SoftwareResource softwareResource) {
        SoftwareResourceDO dataObject = converter.toDataObject(softwareResource);
        if (dataObject.getId() == null) {
            softwareResourceMapper.insert(dataObject);
            softwareResource.setId(dataObject.getId());
        } else {
            softwareResourceMapper.updateById(dataObject);
        }
    }
    @Override
    public void deleteById(Long id) {
        softwareResourceMapper.deleteById(id);
    }

    @Override
    public org.springframework.data.domain.Page<SoftwareResource> findAllForAdmin(Pageable pageable) {
        Page<SoftwareResourceDO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<SoftwareResourceDO> result = softwareResourceMapper.selectAllForAdmin(page);
        List<SoftwareResource> content = converter.toEntityList(result.getRecords());
        return new PageImpl<>(content, pageable, result.getTotal());
    }

    @Override
    public boolean existsById(Long id) {
        return softwareResourceMapper.selectById(id) != null;
    }

    @Override
    public Integer findMaxSortOrder() {
        return softwareResourceMapper.selectMaxSortOrder();
    }

    @Override
    public void batchUpdateSortOrder(List<SortItem> sortItems) {
        if (sortItems.isEmpty()) {
            return;
        }
        softwareResourceMapper.batchUpdateSortOrder(sortItems);
    }
}
