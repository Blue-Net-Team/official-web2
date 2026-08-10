package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.result.venue.VenueResult;
import com.bluenet.web.application.command.venue.VenueCommands;
import com.bluenet.web.application.service.VenueAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Venue;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 场地应用服务实现。
 * <p>
 * 实现场地聚合在应用层的业务逻辑编排。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class VenueAppServiceImpl implements VenueAppService {
    private final VenueRepository venueRepository;
    private final FileRepository fileRepository;

    /**
     * 查询场地列表。
     *
     * @return 场地结果列表
     */
    @Override
    public List<VenueResult> getAllVenues() {
        return venueRepository.findAllOrderBySortOrderDesc()
                .stream()
                .map(this::toResult)
                .toList();
    }

    /**
     * 根据ID查询场地详情。
     *
     * @param id
     *            场地ID
     * @return 场地详情结果
     */
    @Override
    public VenueResult getVenueDetail(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("场地不存在"));
        return toResult(venue);
    }

    /**
     * 创建场地。
     *
     * @param command
     *            创建场地命令
     * @return 创建后的场地结果
     */
    @Override
    @Transactional
    public VenueResult createVenue(VenueCommands.CreateVenueCommand command) {
        Venue venue = Venue.create(
                command.name(),
                command.subtitle(),
                command.description(),
                command.imageFileId(),
                command.sortOrder());
        venueRepository.save(venue);
        return toResult(venue);
    }

    /**
     * 更新场地。
     *
     * @param command
     *            更新场地命令
     * @return 更新后的场地结果
     */
    @Override
    @Transactional
    public VenueResult updateVenue(VenueCommands.UpdateVenueCommand command) {
        Venue venue = venueRepository.findById(command.id())
                .orElseThrow(() -> new DataNotFound("场地不存在"));
        venue.update(
                command.name(),
                command.subtitle(),
                command.description(),
                command.imageFileId(),
                command.sortOrder());
        venueRepository.save(venue);
        return toResult(venue);
    }

    /**
     * 删除场地。
     *
     * @param id
     *            场地ID
     */
    @Override
    @Transactional
    public void deleteVenue(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("场地不存在"));
        venueRepository.deleteById(id);
    }

    /**
     * 更新场地图片。
     *
     * @param id
     *            场地ID
     * @param imageFileId
     *            图片文件ID
     */
    @Override
    @Transactional
    public void updateVenueImage(Long id, Long imageFileId) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("场地不存在"));
        venue.updateImage(imageFileId);
        venueRepository.save(venue);
    }

    private VenueResult toResult(Venue venue) {
        String imageUrl = Optional.ofNullable(venue.getImageFileId())
                .flatMap(fileRepository::findById)
                .map(File::getUrl)
                .orElse(null);
        return new VenueResult(
                venue.getId(),
                venue.getName(),
                venue.getSubtitle(),
                venue.getDescription(),
                imageUrl,
                venue.getImageFileId(),
                venue.getSortOrder());
    }
}
