package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.domain.model.entity.Equipment;
import com.bluenet.web.domain.model.vo.EquipmentVO;
import com.bluenet.web.domain.repository.EquipmentRepository;
import com.bluenet.web.infrastructure.repository.mapper.EquipmentMapper;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
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
 * 设备仓库实现
 */
@Repository
@RequiredArgsConstructor
public class EquipmentRepositoryImpl implements EquipmentRepository {
    private final EquipmentMapper equipmentMapper;
    private final FileMapper fileMapper;

    /**
     * 按展示排序倒序查询全部设备 视图。
     *
     * @return 满足条件的设备 结果集合。
     */
    @Override
    public List<EquipmentVO> findAllOrderBySortOrderDesc() {
        List<EquipmentDO> equipments = equipmentMapper.selectAllOrderBySortOrderDesc();
        Map<Long, FileDO> files = loadFiles(equipments);
        return equipments.stream().map(equipment -> toVO(equipment, files.get(equipment.getImageFileId()))).toList();
    }

    /**
     * 按主键查询设备 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的设备 结果；不存在时为空。
     */
    @Override
    public Optional<EquipmentVO> findById(Long id) {
        EquipmentDO equipment = equipmentMapper.selectEquipmentById(id);
        if (equipment == null) {
            return Optional.empty();
        }
        FileDO file = equipment.getImageFileId() == null ? null : fileMapper.selectById(equipment.getImageFileId());
        return Optional.of(toVO(equipment, file));
    }

    /**
     * 保存新的设备 记录。
     *
     * @param equipment
     *            设备领域对象。
     * @return 新记录的主键。
     */
    @Override
    public Long save(Equipment equipment) {
        EquipmentDO dataObject = RepositoryObjectConverter.copy(equipment, EquipmentDO.class);
        RepositoryObjectConverter.insert(equipmentMapper, dataObject, EquipmentDO.class);
        RepositoryObjectConverter.copyInto(dataObject, equipment);
        return dataObject.getId();
    }

    /**
     * 更新已有设备 记录。
     *
     * @param equipment
     *            设备领域对象。
     */
    @Override
    public void update(Equipment equipment) {
        RepositoryObjectConverter.updateById(
                equipmentMapper,
                RepositoryObjectConverter.copy(equipment, EquipmentDO.class),
                EquipmentDO.class);
    }

    /**
     * 删除指定设备 记录。
     *
     * @param id
     *            业务记录主键。
     */
    @Override
    public void deleteById(Long id) {
        equipmentMapper.deleteById(id);
    }

    /**
     * 判断是否存在满足条件的设备 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    @Override
    public boolean existsById(Long id) {
        return equipmentMapper.selectById(id) != null;
    }

    /**
     * 更新设备 展示图片文件关联。
     *
     * @param id
     *            业务记录主键。
     * @param imageFileId
     *            展示图片文件主键。
     */
    @Override
    public void updateImage(Long id, Long imageFileId) {
        EquipmentDO equipment = new EquipmentDO();
        equipment.setId(id);
        equipment.setImageFileId(imageFileId);
        RepositoryObjectConverter.updateById(equipmentMapper, equipment, EquipmentDO.class);
    }

    /**
     * 批量加载设备 关联的文件数据，用于仓储层组装展示视图。
     *
     * @param equipments
     *            设备数据行集合。
     * @return 查询或处理得到的设备 结果。
     */
    private Map<Long, FileDO> loadFiles(List<EquipmentDO> equipments) {
        List<Long> fileIds = equipments.stream()
                .map(EquipmentDO::getImageFileId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        // 空 Map 需要允许 get(null) 返回 null，兼容 imageFileId 为空的设备记录。
        return fileIds.isEmpty()
                ? Collections.emptyMap()
                : fileMapper.selectBatchIds(fileIds)
                        .stream()
                        .collect(Collectors.toMap(FileDO::getId, Function.identity()));
    }

    /**
     * 将设备 及其关联数据组装为领域视图对象。
     *
     * @param equipment
     *            设备领域对象。
     * @param file
     *            文件领域对象或文件视图对象。
     * @return 转换后的目标模型对象。
     */
    private EquipmentVO toVO(EquipmentDO equipment, FileDO file) {
        return EquipmentVO.builder()
                .id(equipment.getId())
                .name(equipment.getName())
                .brand(equipment.getBrand())
                .description(equipment.getDescription())
                .imageFileId(equipment.getImageFileId())
                .imageUrl(file == null ? null : file.getUrl())
                .sortOrder(equipment.getSortOrder())
                .build();
    }
}
