package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.Venue;
import com.bluenet.web.domain.model.vo.VenueVO;
import com.bluenet.web.domain.repository.VenueRepository;
import com.bluenet.web.infrastructure.repository.mapper.VenueMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 场地仓库实现
 */
@Repository
@RequiredArgsConstructor
public class VenueRepositoryImpl implements VenueRepository {
    private final VenueMapper venueMapper;

    @Override
    public List<VenueVO> findAllOrderBySortOrderDesc() {
        return venueMapper.selectAllOrderBySortOrderDesc();
    }

    @Override
    public Optional<VenueVO> findById(Long id) {
        return venueMapper.selectByIdWithImageUrl(id);
    }

    @Override
    public Long save(Venue venue) {
        venueMapper.insert(venue);
        return venue.getId();
    }

    @Override
    public void update(Venue venue) {
        venueMapper.updateById(venue);
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
        Venue venue = new Venue();
        venue.setId(id);
        venue.setImageFileId(imageFileId);
        venueMapper.updateById(venue);
    }
}
