package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.equipment.CreateEquipmentRequestDTO;
import com.bluenet.web.api.dto.equipment.EquipmentDTO;
import com.bluenet.web.api.dto.equipment.UpdateEquipmentRequestDTO;
import com.bluenet.web.application.converter.EquipmentConverter;
import com.bluenet.web.application.service.EquipmentService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.vo.EquipmentVO;
import com.bluenet.web.domain.service.EquipmentDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 设备应用服务实现
 */
@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {
    private final EquipmentDomainService equipmentDomainService;
    private final EquipmentConverter equipmentConverter;

    @Override
    public List<EquipmentDTO> getEquipmentList() {
        List<EquipmentVO> voList = equipmentDomainService.getAllEquipments();
        return equipmentConverter.convertToDTOList(voList);
    }

    @Override
    public EquipmentDTO getEquipmentDetail(Long id) {
        Optional<EquipmentVO> equipmentOpt = equipmentDomainService.getEquipmentById(id);
        if (equipmentOpt.isEmpty()) {
            throw new DataNotFound("设备不存在");
        }
        return equipmentConverter.convertToDTO(equipmentOpt.get());
    }

    @Override
    @Transactional
    public EquipmentDTO createEquipment(CreateEquipmentRequestDTO request) {
        Long id = equipmentDomainService.createEquipment(
                request.getName(),
                request.getBrand(),
                request.getDescription(),
                request.getImageFileId(),
                request.getSortOrder());

        Optional<EquipmentVO> created = equipmentDomainService.getEquipmentById(id);
        if (created.isEmpty()) {
            throw new IllegalStateException("创建设备失败");
        }

        return equipmentConverter.convertToDTO(created.get());
    }

    @Override
    @Transactional
    public EquipmentDTO updateEquipment(Long id, UpdateEquipmentRequestDTO request) {
        if (!equipmentDomainService.existsById(id)) {
            throw new DataNotFound("设备不存在");
        }

        equipmentDomainService.updateEquipment(
                id,
                request.getName(),
                request.getBrand(),
                request.getDescription(),
                request.getImageFileId(),
                request.getSortOrder());

        Optional<EquipmentVO> updated = equipmentDomainService.getEquipmentById(id);
        if (updated.isEmpty()) {
            throw new IllegalStateException("更新设备失败");
        }

        return equipmentConverter.convertToDTO(updated.get());
    }

    @Override
    @Transactional
    public void deleteEquipment(Long id) {
        if (!equipmentDomainService.existsById(id)) {
            throw new DataNotFound("设备不存在");
        }
        equipmentDomainService.deleteEquipment(id);
    }

    @Override
    @Transactional
    public void updateEquipmentImage(Long id, Long imageFileId) {
        if (!equipmentDomainService.existsById(id)) {
            throw new DataNotFound("设备不存在");
        }
        equipmentDomainService.updateImage(id, imageFileId);
    }
}
