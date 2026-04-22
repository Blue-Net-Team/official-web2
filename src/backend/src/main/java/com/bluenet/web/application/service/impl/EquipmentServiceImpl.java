package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.equipment.CreateEquipmentRequestDTO;
import com.bluenet.web.api.dto.equipment.EquipmentDTO;
import com.bluenet.web.api.dto.equipment.UpdateEquipmentRequestDTO;
import com.bluenet.web.application.converter.EquipmentConverter;
import com.bluenet.web.application.service.EquipmentService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.GlobalException;
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
        return equipmentConverter.convertToDTO(requireEquipment(id));
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

        return equipmentConverter.convertToDTO(loadAfterWrite(id, "创建设备失败"));
    }

    @Override
    @Transactional
    public EquipmentDTO updateEquipment(Long id, UpdateEquipmentRequestDTO request) {
        requireEquipmentExists(id);

        equipmentDomainService.updateEquipment(
                id,
                request.getName(),
                request.getBrand(),
                request.getDescription(),
                request.getImageFileId(),
                request.getSortOrder());

        return equipmentConverter.convertToDTO(loadAfterWrite(id, "更新设备失败"));
    }

    @Override
    @Transactional
    public void deleteEquipment(Long id) {
        requireEquipmentExists(id);
        equipmentDomainService.deleteEquipment(id);
    }

    @Override
    @Transactional
    public void updateEquipmentImage(Long id, Long imageFileId) {
        requireEquipmentExists(id);
        equipmentDomainService.updateImage(id, imageFileId);
    }

    /**
     * 读取设备详情，不存在时统一抛出业务 404。
     */
    private EquipmentVO requireEquipment(Long id) {
        return equipmentDomainService.getEquipmentById(id)
                .orElseThrow(() -> new DataNotFound("设备不存在"));
    }

    /**
     * 写操作后重新读取，集中处理“写入成功但回读失败”的异常分支。
     */
    private EquipmentVO loadAfterWrite(Long id, String errorMessage) {
        Optional<EquipmentVO> equipment = equipmentDomainService.getEquipmentById(id);
        if (equipment.isEmpty()) {
            throw new GlobalException(errorMessage);
        }
        return equipment.get();
    }

    /**
     * 更新、删除和图片替换前统一校验设备存在性。
     */
    private void requireEquipmentExists(Long id) {
        if (!equipmentDomainService.existsById(id)) {
            throw new DataNotFound("设备不存在");
        }
    }
}
