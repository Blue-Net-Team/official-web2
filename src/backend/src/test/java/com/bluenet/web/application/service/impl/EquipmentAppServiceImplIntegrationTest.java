package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.equipment.EquipmentCommands;
import com.bluenet.web.application.result.equipment.EquipmentResult;
import com.bluenet.web.application.service.EquipmentAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Equipment;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.repository.EquipmentRepository;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testsupport.fixture.FileFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * EquipmentAppServiceImpl 集成测试。
 *
 * <p>
 * 验证设备应用服务的查询、创建、更新、删除以及图片更新逻辑。
 * </p>
 */
@DisplayName("EquipmentAppServiceImpl 集成测试")
class EquipmentAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private EquipmentAppService equipmentAppService;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private FileRepository fileRepository;

    @AfterEach
    void cleanupSecurityContext() {
        UserCTX.clear();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createEquipment: 应创建设备并持久化")
    void createEquipment_shouldCreateAndPersist() {
        EquipmentCommands.CreateEquipmentCommand command = new EquipmentCommands.CreateEquipmentCommand(
                "3D 打印机", "创想三维", "用于打印三维模型", null, 10);

        EquipmentResult result = equipmentAppService.createEquipment(command);

        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("3D 打印机");
        assertThat(result.brand()).isEqualTo("创想三维");
        assertThat(result.description()).isEqualTo("用于打印三维模型");
        assertThat(result.imageUrl()).isNull();
        assertThat(result.imageFileId()).isNull();
        assertThat(result.sortOrder()).isEqualTo(10);
        assertThat(equipmentRepository.findById(result.id()))
                .isPresent()
                .hasValueSatisfying(equipment -> {
                    assertThat(equipment.getName()).isEqualTo("3D 打印机");
                    assertThat(equipment.getBrand()).isEqualTo("创想三维");
                    assertThat(equipment.getDescription()).isEqualTo("用于打印三维模型");
                    assertThat(equipment.getSortOrder()).isEqualTo(10);
                });
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getAllEquipments: 应按 sortOrder 降序返回所有设备")
    void getAllEquipments_shouldReturnAllEquipmentsOrderedBySortOrderDesc() {
        Equipment equipmentA = Equipment.create("激光切割机", "品牌A", "描述A", null, 5);
        Equipment equipmentB = Equipment.create("3D 打印机", "品牌B", "描述B", null, 20);
        Equipment equipmentC = Equipment.create("数控机床", "品牌C", "描述C", null, 10);
        equipmentRepository.save(equipmentA);
        equipmentRepository.save(equipmentB);
        equipmentRepository.save(equipmentC);

        List<EquipmentResult> result = equipmentAppService.getAllEquipments();

        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(EquipmentResult::name)
                .containsExactly("3D 打印机", "数控机床", "激光切割机");
        assertThat(result)
                .extracting(EquipmentResult::sortOrder)
                .containsExactly(20, 10, 5);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getEquipmentDetail: 当 imageFileId 存在时应解析图片 URL")
    void getEquipmentDetail_withImage_shouldResolveImageUrl() {
        File savedFile = FileFixture.save(fileRepository, "equipment.png", FileType.NORMAL_IMG);
        Equipment equipment = Equipment.create("激光切割机", "品牌A", "描述A", savedFile.getId(), 1);
        equipmentRepository.save(equipment);

        EquipmentResult result = equipmentAppService.getEquipmentDetail(equipment.getId());

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(equipment.getId());
        assertThat(result.name()).isEqualTo("激光切割机");
        assertThat(result.imageFileId()).isEqualTo(savedFile.getId());
        assertThat(result.imageUrl()).isEqualTo(savedFile.getUrl());
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getEquipmentDetail: 当无图片时 imageUrl 应为 null")
    void getEquipmentDetail_withoutImage_shouldReturnNullImageUrl() {
        Equipment equipment = Equipment.create("3D 打印机", "品牌B", "描述B", null, 2);
        equipmentRepository.save(equipment);

        EquipmentResult result = equipmentAppService.getEquipmentDetail(equipment.getId());

        assertThat(result).isNotNull();
        assertThat(result.imageFileId()).isNull();
        assertThat(result.imageUrl()).isNull();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getEquipmentDetail: 不存在的 id 应抛 DataNotFound")
    void getEquipmentDetail_notFound_shouldThrowDataNotFound() {
        assertThatThrownBy(() -> equipmentAppService.getEquipmentDetail(99999L))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("设备不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateEquipment: 应更新设备并持久化")
    void updateEquipment_shouldUpdateAndPersist() {
        Equipment equipment = Equipment.create("旧设备", "旧品牌", "旧描述", null, 1);
        equipmentRepository.save(equipment);
        EquipmentCommands.UpdateEquipmentCommand command = new EquipmentCommands.UpdateEquipmentCommand(
                equipment.getId(), "新设备", "新品牌", "新描述", null, 5);

        EquipmentResult result = equipmentAppService.updateEquipment(command);

        assertThat(result.name()).isEqualTo("新设备");
        assertThat(result.brand()).isEqualTo("新品牌");
        assertThat(result.description()).isEqualTo("新描述");
        assertThat(result.sortOrder()).isEqualTo(5);
        assertThat(equipmentRepository.findById(equipment.getId()))
                .isPresent()
                .hasValueSatisfying(updated -> {
                    assertThat(updated.getName()).isEqualTo("新设备");
                    assertThat(updated.getBrand()).isEqualTo("新品牌");
                    assertThat(updated.getDescription()).isEqualTo("新描述");
                    assertThat(updated.getSortOrder()).isEqualTo(5);
                });
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateEquipment: 不存在的 id 应抛 DataNotFound")
    void updateEquipment_notFound_shouldThrowDataNotFound() {
        EquipmentCommands.UpdateEquipmentCommand command = new EquipmentCommands.UpdateEquipmentCommand(
                99999L, "任意设备", "品牌", "描述", null, 1);

        assertThatThrownBy(() -> equipmentAppService.updateEquipment(command))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("设备不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("deleteEquipment: 应删除设备")
    void deleteEquipment_shouldDelete() {
        Equipment equipment = Equipment.create("待删除设备", "品牌", "描述", null, 1);
        equipmentRepository.save(equipment);

        equipmentAppService.deleteEquipment(equipment.getId());

        assertThat(equipmentRepository.findById(equipment.getId())).isEmpty();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("deleteEquipment: 不存在的 id 应抛 DataNotFound")
    void deleteEquipment_notFound_shouldThrowDataNotFound() {
        assertThatThrownBy(() -> equipmentAppService.deleteEquipment(99999L))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("设备不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateEquipmentImage: 应更新设备图片文件 id")
    void updateEquipmentImage_shouldUpdateImageFileId() {
        Equipment equipment = Equipment.create("设备", "品牌", "描述", null, 1);
        equipmentRepository.save(equipment);
        File imageFile = FileFixture.save(fileRepository, "new-image.png", FileType.NORMAL_IMG);

        equipmentAppService.updateEquipmentImage(equipment.getId(), imageFile.getId());

        assertThat(equipmentRepository.findById(equipment.getId()))
                .isPresent()
                .hasValueSatisfying(updated -> assertThat(updated.getImageFileId()).isEqualTo(imageFile.getId()));
    }
}
