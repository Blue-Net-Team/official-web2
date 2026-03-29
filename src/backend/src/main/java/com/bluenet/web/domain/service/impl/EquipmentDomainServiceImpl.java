package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.model.entity.Equipment;
import com.bluenet.web.domain.model.vo.EquipmentVO;
import com.bluenet.web.domain.repository.EquipmentRepository;
import com.bluenet.web.domain.service.EquipmentDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 设备领域服务实现
 */
@Service
@RequiredArgsConstructor
public class EquipmentDomainServiceImpl implements EquipmentDomainService {
    private final EquipmentRepository equipmentRepository;

    @Override
    public List<EquipmentVO> getAllEquipments() {
        return equipmentRepository.findAllOrderBySortOrderDesc();
    }

    @Override
    public Optional<EquipmentVO> getEquipmentById(Long id) {
        return equipmentRepository.findById(id);
    }

    @Override
    public Long createEquipment(String name, String brand, String description, Long imageFileId, Integer sortOrder) {
        Equipment equipment = new Equipment();
        equipment.setName(name);
        equipment.setBrand(brand);
        equipment.setDescription(description);
        equipment.setImageFileId(imageFileId);
        equipment.setSortOrder(sortOrder != null ? sortOrder : 0);
        return equipmentRepository.save(equipment);
    }

    @Override
    public void updateEquipment(Long id, String name, String brand, String description, Long imageFileId,
            Integer sortOrder) {
        Equipment equipment = new Equipment();
        equipment.setId(id);
        equipment.setName(name);
        equipment.setBrand(brand);
        equipment.setDescription(description);
        equipment.setImageFileId(imageFileId);
        equipment.setSortOrder(sortOrder);
        equipmentRepository.update(equipment);
    }

    @Override
    public void deleteEquipment(Long id) {
        equipmentRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return equipmentRepository.existsById(id);
    }

    @Override
    public void updateImage(Long id, Long imageFileId) {
        equipmentRepository.updateImage(id, imageFileId);
    }
}
