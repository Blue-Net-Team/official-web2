package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.model.entity.Venue;
import com.bluenet.web.domain.model.vo.VenueVO;
import com.bluenet.web.domain.repository.VenueRepository;
import com.bluenet.web.domain.service.VenueDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 场地领域服务实现
 */
@Service
@RequiredArgsConstructor
public class VenueDomainServiceImpl implements VenueDomainService {
    private final VenueRepository venueRepository;

    @Override
    public List<VenueVO> getAllVenues() {
        return venueRepository.findAllOrderBySortOrderDesc();
    }

    @Override
    public Optional<VenueVO> getVenueById(Long id) {
        return venueRepository.findById(id);
    }

    @Override
    public Long createVenue(String name, String subtitle, String description, Long imageFileId, Integer sortOrder) {
        Venue venue = new Venue();
        venue.setName(name);
        venue.setSubtitle(subtitle);
        venue.setDescription(description);
        venue.setImageFileId(imageFileId);
        venue.setSortOrder(sortOrder != null ? sortOrder : 0);
        return venueRepository.save(venue);
    }

    @Override
    public void updateVenue(Long id, String name, String subtitle, String description, Long imageFileId,
            Integer sortOrder) {
        Venue venue = new Venue();
        venue.setId(id);
        venue.setName(name);
        venue.setSubtitle(subtitle);
        venue.setDescription(description);
        venue.setImageFileId(imageFileId);
        venue.setSortOrder(sortOrder);
        venueRepository.update(venue);
    }

    @Override
    public void deleteVenue(Long id) {
        venueRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return venueRepository.existsById(id);
    }

    @Override
    public void updateImage(Long id, Long imageFileId) {
        venueRepository.updateImage(id, imageFileId);
    }
}
