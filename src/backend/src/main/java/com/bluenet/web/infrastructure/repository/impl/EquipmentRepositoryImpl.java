package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.Equipment;
import com.bluenet.web.domain.repository.EquipmentRepository;
import com.bluenet.web.infrastructure.repository.converter.EquipmentRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.EquipmentDO;
import com.bluenet.web.infrastructure.repository.mapper.EquipmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 设备仓库实现类
 * <p>
 * 实现设备数据的持久化操作，使用显式转换器替代 BeanUtils
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class EquipmentRepositoryImpl implements EquipmentRepository {
    private final EquipmentMapper equipmentMapper;
    private final EquipmentRepositoryConverter converter;

    @Override
    public List<Equipment> findAllOrderBySortOrderDesc() {
        List<EquipmentDO> dataObjects = equipmentMapper.selectAllOrderBySortOrderDesc();
        return converter.toEntityList(dataObjects);
    }

    @Override
    public Optional<Equipment> findById(Long id) {
        EquipmentDO dataObject = equipmentMapper.selectById(id);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    @Override
    public void save(Equipment equipment) {
        EquipmentDO dataObject = converter.toDataObject(equipment);
        if (dataObject.getId() == null) {
            equipmentMapper.insert(dataObject);
            equipment.setId(dataObject.getId());
        } else {
            equipmentMapper.updateById(dataObject);
        }
    }
    @Override
    public void deleteById(Long id) {
        equipmentMapper.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return equipmentMapper.selectById(id) != null;
    }

}
