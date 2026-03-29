package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.venue.CreateVenueRequestDTO;
import com.bluenet.web.api.dto.venue.UpdateVenueRequestDTO;
import com.bluenet.web.api.dto.venue.VenueDTO;
import com.bluenet.web.application.converter.VenueConverter;
import com.bluenet.web.application.service.VenueService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.vo.VenueVO;
import com.bluenet.web.domain.service.VenueDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 场地应用服务实现
 */
@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements VenueService {
    private final VenueDomainService venueDomainService;
    private final VenueConverter venueConverter;

    @Override
    public List<VenueDTO> getVenueList() {
        List<VenueVO> voList = venueDomainService.getAllVenues();
        return venueConverter.convertToDTOList(voList);
    }

    @Override
    public VenueDTO getVenueDetail(Long id) {
        Optional<VenueVO> venueOpt = venueDomainService.getVenueById(id);
        if (venueOpt.isEmpty()) {
            throw new DataNotFound("场地不存在");
        }
        return venueConverter.convertToDTO(venueOpt.get());
    }

    @Override
    @Transactional
    public VenueDTO createVenue(CreateVenueRequestDTO request) {
        Long id = venueDomainService.createVenue(
                request.getName(),
                request.getSubtitle(),
                request.getDescription(),
                request.getImageFileId(),
                request.getSortOrder());

        Optional<VenueVO> created = venueDomainService.getVenueById(id);
        if (created.isEmpty()) {
            throw new IllegalStateException("创建场地失败");
        }

        return venueConverter.convertToDTO(created.get());
    }

    @Override
    @Transactional
    public VenueDTO updateVenue(Long id, UpdateVenueRequestDTO request) {
        if (!venueDomainService.existsById(id)) {
            throw new DataNotFound("场地不存在");
        }

        venueDomainService.updateVenue(
                id,
                request.getName(),
                request.getSubtitle(),
                request.getDescription(),
                request.getImageFileId(),
                request.getSortOrder());

        Optional<VenueVO> updated = venueDomainService.getVenueById(id);
        if (updated.isEmpty()) {
            throw new IllegalStateException("更新场地失败");
        }

        return venueConverter.convertToDTO(updated.get());
    }

    @Override
    @Transactional
    public void deleteVenue(Long id) {
        if (!venueDomainService.existsById(id)) {
            throw new DataNotFound("场地不存在");
        }
        venueDomainService.deleteVenue(id);
    }

    @Override
    @Transactional
    public void updateVenueImage(Long id, Long imageFileId) {
        if (!venueDomainService.existsById(id)) {
            throw new DataNotFound("场地不存在");
        }
        venueDomainService.updateImage(id, imageFileId);
    }
}
