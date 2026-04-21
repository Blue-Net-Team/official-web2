package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.domain.model.entity.Venue;
import com.bluenet.web.domain.model.vo.VenueVO;
import com.bluenet.web.domain.repository.VenueRepository;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.infrastructure.repository.mapper.VenueMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 场地仓库实现
 */
@Repository
@RequiredArgsConstructor
public class VenueRepositoryImpl implements VenueRepository {
    private final VenueMapper venueMapper;
    private final FileMapper fileMapper;

    /**
     * 按展示排序倒序查询全部场地 视图。
     *
     * @return 满足条件的场地 结果集合。
     */
    @Override
    public List<VenueVO> findAllOrderBySortOrderDesc() {
        List<VenueDO> venues = venueMapper.selectAllOrderBySortOrderDesc();
        Map<Long, FileDO> files = loadFiles(venues);
        return venues.stream().map(venue -> toVO(venue, files.get(venue.getImageFileId()))).toList();
    }

    /**
     * 按主键查询场地 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的场地 结果；不存在时为空。
     */
    @Override
    public Optional<VenueVO> findById(Long id) {
        VenueDO venue = venueMapper.selectVenueById(id);
        if (venue == null) {
            return Optional.empty();
        }
        FileDO file = venue.getImageFileId() == null ? null : fileMapper.selectById(venue.getImageFileId());
        return Optional.of(toVO(venue, file));
    }

    /**
     * 保存新的场地 记录。
     *
     * @param venue
     *            场地领域对象。
     * @return 新记录的主键。
     */
    @Override
    public Long save(Venue venue) {
        VenueDO dataObject = RepositoryObjectConverter.copy(venue, VenueDO.class);
        RepositoryObjectConverter.insert(venueMapper, dataObject, VenueDO.class);
        RepositoryObjectConverter.copyInto(dataObject, venue);
        return dataObject.getId();
    }

    /**
     * 更新已有场地 记录。
     *
     * @param venue
     *            场地领域对象。
     */
    @Override
    public void update(Venue venue) {
        RepositoryObjectConverter
                .updateById(venueMapper, RepositoryObjectConverter.copy(venue, VenueDO.class), VenueDO.class);
    }

    /**
     * 删除指定场地 记录。
     *
     * @param id
     *            业务记录主键。
     */
    @Override
    public void deleteById(Long id) {
        venueMapper.deleteById(id);
    }

    /**
     * 判断是否存在满足条件的场地 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    @Override
    public boolean existsById(Long id) {
        return venueMapper.selectById(id) != null;
    }

    /**
     * 更新场地 展示图片文件关联。
     *
     * @param id
     *            业务记录主键。
     * @param imageFileId
     *            展示图片文件主键。
     */
    @Override
    public void updateImage(Long id, Long imageFileId) {
        VenueDO venue = new VenueDO();
        venue.setId(id);
        venue.setImageFileId(imageFileId);
        RepositoryObjectConverter.updateById(venueMapper, venue, VenueDO.class);
    }

    /**
     * 批量加载场地 关联的文件数据，用于仓储层组装展示视图。
     *
     * @param venues
     *            场地数据行集合。
     * @return 查询或处理得到的场地 结果。
     */
    private Map<Long, FileDO> loadFiles(List<VenueDO> venues) {
        List<Long> fileIds = venues.stream()
                .map(VenueDO::getImageFileId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        // 空 Map 需要允许 get(null) 返回 null，兼容 imageFileId 为空的场地记录。
        return fileIds.isEmpty()
                ? Collections.emptyMap()
                : fileMapper.selectBatchIds(fileIds)
                        .stream()
                        .collect(Collectors.toMap(FileDO::getId, Function.identity()));
    }

    /**
     * 将场地 及其关联数据组装为领域视图对象。
     *
     * @param venue
     *            场地领域对象。
     * @param file
     *            文件领域对象或文件视图对象。
     * @return 转换后的目标模型对象。
     */
    private VenueVO toVO(VenueDO venue, FileDO file) {
        return VenueVO.builder()
                .id(venue.getId())
                .name(venue.getName())
                .subtitle(venue.getSubtitle())
                .description(venue.getDescription())
                .imageFileId(venue.getImageFileId())
                .imageUrl(file == null ? null : file.getUrl())
                .sortOrder(venue.getSortOrder())
                .build();
    }
}
