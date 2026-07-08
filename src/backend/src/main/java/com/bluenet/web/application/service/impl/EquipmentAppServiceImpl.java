package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.EquipmentResult;
import com.bluenet.web.application.command.equipment.EquipmentCommands;
import com.bluenet.web.application.service.EquipmentAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Equipment;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.repository.EquipmentRepository;
import com.bluenet.web.domain.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 设备应用服务实现。
 * <p>
 * 实现设备聚合在应用层的业务逻辑编排。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class EquipmentAppServiceImpl implements EquipmentAppService {
    private final EquipmentRepository equipmentRepository;
    private final FileRepository fileRepository;

    /**
     * 查询设备列表。
     *
     * @return 设备结果列表
     */
    @Override
    public List<EquipmentResult> getAllEquipments() {
        return equipmentRepository.findAllOrderBySortOrderDesc()
                .stream()
                .map(this::toResult)
                .toList();
    }

    /**
     * 根据ID查询设备详情。
     *
     * @param id
     *            设备ID
     * @return 设备详情结果
     */
    @Override
    public EquipmentResult getEquipmentDetail(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("设备不存在"));
        return toResult(equipment);
    }

    /**
     * 创建设备。
     *
     * @param command
     *            创建设备命令
     * @return 创建后的设备结果
     */
    @Override
    @Transactional
    public EquipmentResult createEquipment(EquipmentCommands.CreateEquipmentCommand command) {
        Equipment equipment = Equipment.create(
                command.name(),
                command.brand(),
                command.description(),
                command.imageFileId(),
                command.sortOrder());
        equipmentRepository.save(equipment);
        return toResult(equipment);
    }

    /**
     * 更新设备。
     *
     * @param command
     *            更新设备命令
     * @return 更新后的设备结果
     */
    @Override
    @Transactional
    public EquipmentResult updateEquipment(EquipmentCommands.UpdateEquipmentCommand command) {
        Equipment equipment = equipmentRepository.findById(command.id())
                .orElseThrow(() -> new DataNotFound("设备不存在"));
        equipment.update(
                command.name(),
                command.brand(),
                command.description(),
                command.imageFileId(),
                command.sortOrder());
        equipmentRepository.save(equipment);
        return toResult(equipment);
    }

    /**
     * 删除设备。
     *
     * @param id
     *            设备ID
     */
    @Override
    @Transactional
    public void deleteEquipment(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("设备不存在"));
        equipmentRepository.deleteById(id);
    }

    /**
     * 更新设备图片。
     *
     * @param id
     *            设备ID
     * @param imageFileId
     *            图片文件ID
     */
    @Override
    @Transactional
    public void updateEquipmentImage(Long id, Long imageFileId) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("设备不存在"));
        equipment.updateImage(imageFileId);
        equipmentRepository.save(equipment);
    }

    private EquipmentResult toResult(Equipment equipment) {
        String imageUrl = Optional.ofNullable(equipment.getImageFileId())
                .flatMap(fileRepository::findById)
                .map(File::getUrl)
                .orElse(null);
        return new EquipmentResult(
                equipment.getId(),
                equipment.getName(),
                equipment.getBrand(),
                equipment.getDescription(),
                imageUrl,
                equipment.getImageFileId(),
                equipment.getSortOrder());
    }
}
