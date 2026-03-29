package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.Equipment;
import com.bluenet.web.domain.model.vo.EquipmentVO;
import com.bluenet.web.domain.repository.EquipmentRepository;
import com.bluenet.web.infrastructure.repository.mapper.EquipmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 设备仓库实现
 */
@Repository
@RequiredArgsConstructor
public class EquipmentRepositoryImpl implements EquipmentRepository {
    private final EquipmentMapper equipmentMapper;

    @Override
    public List<EquipmentVO> findAllOrderBySortOrderDesc() {
        return equipmentMapper.selectAllOrderBySortOrderDesc();
    }

    @Override
    public Optional<EquipmentVO> findById(Long id) {
        return equipmentMapper.selectByIdWithImageUrl(id);
    }

    @Override
    public Long save(Equipment equipment) {
        equipmentMapper.insert(equipment);
        return equipment.getId();
    }

    @Override
    public void update(Equipment equipment) {
        equipmentMapper.updateById(equipment);
    }

    @Override
    public void deleteById(Long id) {
        equipmentMapper.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return equipmentMapper.selectById(id) != null;
    }

    @Override
    public void updateImage(Long id, Long imageFileId) {
        Equipment equipment = new Equipment();
        equipment.setId(id);
        equipment.setImageFileId(imageFileId);
        equipmentMapper.updateById(equipment);
    }
}
