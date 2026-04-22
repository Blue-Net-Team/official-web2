package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.venue.CreateVenueRequestDTO;
import com.bluenet.web.api.dto.venue.UpdateVenueRequestDTO;
import com.bluenet.web.api.dto.venue.VenueDTO;
import com.bluenet.web.application.converter.VenueConverter;
import com.bluenet.web.application.service.VenueService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.GlobalException;
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
        return venueConverter.convertToDTO(requireVenue(id));
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

        return venueConverter.convertToDTO(loadAfterWrite(id, "创建场地失败"));
    }

    @Override
    @Transactional
    public VenueDTO updateVenue(Long id, UpdateVenueRequestDTO request) {
        requireVenueExists(id);

        venueDomainService.updateVenue(
                id,
                request.getName(),
                request.getSubtitle(),
                request.getDescription(),
                request.getImageFileId(),
                request.getSortOrder());

        return venueConverter.convertToDTO(loadAfterWrite(id, "更新场地失败"));
    }

    @Override
    @Transactional
    public void deleteVenue(Long id) {
        requireVenueExists(id);
        venueDomainService.deleteVenue(id);
    }

    @Override
    @Transactional
    public void updateVenueImage(Long id, Long imageFileId) {
        requireVenueExists(id);
        venueDomainService.updateImage(id, imageFileId);
    }

    /**
     * 读取场地详情，不存在时统一抛出业务 404。
     */
    private VenueVO requireVenue(Long id) {
        return venueDomainService.getVenueById(id)
                .orElseThrow(() -> new DataNotFound("场地不存在"));
    }

    /**
     * 写操作后重新读取，集中处理“写入成功但回读失败”的异常分支。
     */
    private VenueVO loadAfterWrite(Long id, String errorMessage) {
        Optional<VenueVO> venue = venueDomainService.getVenueById(id);
        if (venue.isEmpty()) {
            throw new GlobalException(errorMessage);
        }
        return venue.get();
    }

    /**
     * 更新、删除和图片替换前统一校验场地存在性。
     */
    private void requireVenueExists(Long id) {
        if (!venueDomainService.existsById(id)) {
            throw new DataNotFound("场地不存在");
        }
    }
}
