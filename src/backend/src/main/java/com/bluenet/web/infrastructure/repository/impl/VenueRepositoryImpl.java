package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.Venue;
import com.bluenet.web.domain.repository.VenueRepository;
import com.bluenet.web.infrastructure.repository.converter.VenueRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.VenueDO;
import com.bluenet.web.infrastructure.repository.mapper.VenueMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 场地仓库实现类
 * <p>
 * 实现场地数据的持久化操作，使用显式转换器替代 BeanUtils
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class VenueRepositoryImpl implements VenueRepository {
    private final VenueMapper venueMapper;
    private final VenueRepositoryConverter converter;

    @Override
    public List<Venue> findAllOrderBySortOrderDesc() {
        List<VenueDO> dataObjects = venueMapper.selectAllOrderBySortOrderDesc();
        return converter.toEntityList(dataObjects);
    }

    @Override
    public Optional<Venue> findById(Long id) {
        VenueDO dataObject = venueMapper.selectById(id);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    @Override
    public void save(Venue venue) {
        VenueDO dataObject = converter.toDataObject(venue);
        if (dataObject.getId() == null) {
            venueMapper.insert(dataObject);
            venue.setId(dataObject.getId());
        } else {
            venueMapper.updateById(dataObject);
        }
    }
    @Override
    public void deleteById(Long id) {
        venueMapper.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return venueMapper.selectById(id) != null;
    }

    @Override
    public void updateImage(Long id, Long imageFileId) {
        VenueDO dataObject = new VenueDO();
        dataObject.setId(id);
        dataObject.setImageFileId(imageFileId);
        venueMapper.updateById(dataObject);
    }
}
