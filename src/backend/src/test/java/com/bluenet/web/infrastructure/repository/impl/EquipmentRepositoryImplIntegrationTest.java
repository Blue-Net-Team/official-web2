package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.Equipment;
import com.bluenet.web.domain.repository.EquipmentRepository;
import com.bluenet.web.infrastructure.repository.dataobject.EquipmentDO;
import com.bluenet.web.infrastructure.repository.mapper.EquipmentMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EquipmentRepositoryImpl 集成测试。
 */
@DisplayName("EquipmentRepositoryImpl 集成测试")
class EquipmentRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private EquipmentMapper equipmentMapper;

    private Equipment createEquipment(String name, Integer sortOrder) {
        Equipment equipment = Equipment.create(name, "品牌" + name, name + "描述", null, sortOrder);
        equipmentRepository.save(equipment);
        return equipment;
    }

    @Test
    @DisplayName("save: 新设备应插入并回写ID")
    void save_newEquipment_shouldInsertAndReturnId() {
        Equipment equipment = createEquipment("3D打印机", 10);

        assertThat(equipment.getId()).isNotNull();
        EquipmentDO dataObject = equipmentMapper.selectById(equipment.getId());
        assertThat(dataObject).isNotNull();
        assertThat(dataObject.getName()).isEqualTo("3D打印机");
        assertThat(dataObject.getBrand()).isEqualTo("品牌3D打印机");
    }

    @Test
    @DisplayName("save: 已有设备应更新字段")
    void save_existingEquipment_shouldUpdateFields() {
        Equipment equipment = createEquipment("激光切割机", 5);
        equipment.update("更新后设备", "新品牌", "新描述", null, 20);

        equipmentRepository.save(equipment);

        EquipmentDO updated = equipmentMapper.selectById(equipment.getId());
        assertThat(updated.getName()).isEqualTo("更新后设备");
        assertThat(updated.getBrand()).isEqualTo("新品牌");
        assertThat(updated.getSortOrder()).isEqualTo(20);
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        Equipment equipment = createEquipment("查询设备", 1);

        Optional<Equipment> found = equipmentRepository.findById(equipment.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("查询设备");

        assertThat(equipmentRepository.findById(-1L)).isEmpty();
    }

    @Test
    @DisplayName("findAllOrderBySortOrderDesc: 应按排序号倒序返回")
    void findAllOrderBySortOrderDesc_shouldSortDesc() {
        Equipment equipment1 = createEquipment("排序10", 10);
        Equipment equipment2 = createEquipment("排序30", 30);
        Equipment equipment3 = createEquipment("排序20", 20);

        List<Equipment> equipments = equipmentRepository.findAllOrderBySortOrderDesc();

        assertThat(equipments)
                .extracting(Equipment::getId)
                .containsExactly(equipment2.getId(), equipment3.getId(), equipment1.getId());
    }

    @Test
    @DisplayName("existsById: 应正确判断设备是否存在")
    void existsById_shouldWork() {
        Equipment equipment = createEquipment("存在设备", 1);

        assertThat(equipmentRepository.existsById(equipment.getId())).isTrue();
        assertThat(equipmentRepository.existsById(-1L)).isFalse();
    }

    @Test
    @DisplayName("deleteById: 应删除设备")
    void deleteById_shouldRemoveEquipment() {
        Equipment equipment = createEquipment("待删除设备", 1);
        Long equipmentId = equipment.getId();

        equipmentRepository.deleteById(equipmentId);

        assertThat(equipmentMapper.selectById(equipmentId)).isNull();
    }
}
