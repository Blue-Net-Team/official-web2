package com.bluenet.web.domain.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bluenet.web.domain.model.entity.Equipment;
import com.bluenet.web.domain.model.vo.EquipmentVO;
import com.bluenet.web.domain.repository.EquipmentRepository;

/**
 * EquipmentDomainServiceImpl 单元测试
 */
@DisplayName("EquipmentDomainServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class EquipmentDomainServiceImplTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private EquipmentDomainServiceImpl equipmentDomainService;

    private static final Long TEST_ID = 1L;
    private static final Long TEST_FILE_ID = 100L;
    private static final String TEST_NAME = "测试设备";
    private static final String TEST_BRAND = "测试品牌";
    private static final String TEST_DESCRIPTION = "测试描述";
    private static final Integer TEST_SORT_ORDER = 10;

    private EquipmentVO createTestEquipmentVO() {
        return EquipmentVO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .brand(TEST_BRAND)
                .description(TEST_DESCRIPTION)
                .imageFileId(TEST_FILE_ID)
                .imageUrl("http://example.com/image.jpg")
                .sortOrder(TEST_SORT_ORDER)
                .build();
    }

    // ==================== getAllEquipments 测试 ====================

    @Test
    @DisplayName("获取所有设备：应返回按排序倒序的设备列表")
    void getAllEquipments_shouldReturnSortedList() {
        // 准备
        List<EquipmentVO> expectedEquipments = new ArrayList<>();
        expectedEquipments.add(createTestEquipmentVO());
        when(equipmentRepository.findAllOrderBySortOrderDesc()).thenReturn(expectedEquipments);

        // 执行
        List<EquipmentVO> result = equipmentDomainService.getAllEquipments();

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_NAME, result.get(0).getName());
        verify(equipmentRepository).findAllOrderBySortOrderDesc();
    }

    @Test
    @DisplayName("获取所有设备：无设备时应返回空列表")
    void getAllEquipments_noEquipments_shouldReturnEmptyList() {
        // 准备
        when(equipmentRepository.findAllOrderBySortOrderDesc()).thenReturn(new ArrayList<>());

        // 执行
        List<EquipmentVO> result = equipmentDomainService.getAllEquipments();

        // 验证
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getEquipmentById 测试 ====================

    @Test
    @DisplayName("根据ID获取设备：设备存在时应返回设备")
    void getEquipmentById_existingEquipment_shouldReturnEquipment() {
        // 准备
        EquipmentVO expectedEquipment = createTestEquipmentVO();
        when(equipmentRepository.findById(TEST_ID)).thenReturn(Optional.of(expectedEquipment));

        // 执行
        Optional<EquipmentVO> result = equipmentDomainService.getEquipmentById(TEST_ID);

        // 验证
        assertTrue(result.isPresent());
        assertEquals(TEST_NAME, result.get().getName());
        verify(equipmentRepository).findById(TEST_ID);
    }

    @Test
    @DisplayName("根据ID获取设备：设备不存在时应返回空")
    void getEquipmentById_nonExistingEquipment_shouldReturnEmpty() {
        // 准备
        when(equipmentRepository.findById(TEST_ID)).thenReturn(Optional.empty());

        // 执行
        Optional<EquipmentVO> result = equipmentDomainService.getEquipmentById(TEST_ID);

        // 验证
        assertFalse(result.isPresent());
    }

    // ==================== createEquipment 测试 ====================

    @Test
    @DisplayName("创建设备：应成功创建并返回ID")
    void createEquipment_shouldCreateAndReturnId() {
        // 准备
        Long expectedId = 1L;
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(expectedId);

        // 执行
        Long result = equipmentDomainService.createEquipment(
                TEST_NAME,
                TEST_BRAND,
                TEST_DESCRIPTION,
                TEST_FILE_ID,
                TEST_SORT_ORDER);

        // 验证
        assertEquals(expectedId, result);
        verify(equipmentRepository).save(any(Equipment.class));
    }

    @Test
    @DisplayName("创建设备：无排序值时应使用默认值0")
    void createEquipment_withoutSortOrder_shouldUseDefaultZero() {
        // 准备
        Long expectedId = 2L;
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(expectedId);

        // 执行
        Long result = equipmentDomainService.createEquipment(
                TEST_NAME,
                TEST_BRAND,
                TEST_DESCRIPTION,
                TEST_FILE_ID,
                null);

        // 验证
        assertEquals(expectedId, result);
        verify(equipmentRepository).save(any(Equipment.class));
    }

    // ==================== updateEquipment 测试 ====================

    @Test
    @DisplayName("更新设备：应成功更新设备信息")
    void updateEquipment_shouldUpdateSuccessfully() {
        // 准备
        doNothing().when(equipmentRepository).update(any(Equipment.class));

        // 执行
        equipmentDomainService.updateEquipment(
                TEST_ID,
                TEST_NAME,
                TEST_BRAND,
                TEST_DESCRIPTION,
                TEST_FILE_ID,
                TEST_SORT_ORDER);

        // 验证
        verify(equipmentRepository).update(any(Equipment.class));
    }

    // ==================== deleteEquipment 测试 ====================

    @Test
    @DisplayName("删除设备：应成功删除设备")
    void deleteEquipment_shouldDeleteSuccessfully() {
        // 准备
        doNothing().when(equipmentRepository).deleteById(TEST_ID);

        // 执行
        equipmentDomainService.deleteEquipment(TEST_ID);

        // 验证
        verify(equipmentRepository).deleteById(TEST_ID);
    }

    // ==================== existsById 测试 ====================

    @Test
    @DisplayName("检查设备存在：设备存在时应返回true")
    void existsById_existingEquipment_shouldReturnTrue() {
        // 准备
        when(equipmentRepository.existsById(TEST_ID)).thenReturn(true);

        // 执行
        boolean result = equipmentDomainService.existsById(TEST_ID);

        // 验证
        assertTrue(result);
    }

    @Test
    @DisplayName("检查设备存在：设备不存在时应返回false")
    void existsById_nonExistingEquipment_shouldReturnFalse() {
        // 准备
        when(equipmentRepository.existsById(TEST_ID)).thenReturn(false);

        // 执行
        boolean result = equipmentDomainService.existsById(TEST_ID);

        // 验证
        assertFalse(result);
    }

    // ==================== updateImage 测试 ====================

    @Test
    @DisplayName("更新设备图片：应成功更新图片")
    void updateImage_shouldUpdateSuccessfully() {
        // 准备
        Long newImageFileId = 200L;
        doNothing().when(equipmentRepository).updateImage(TEST_ID, newImageFileId);

        // 执行
        equipmentDomainService.updateImage(TEST_ID, newImageFileId);

        // 验证
        verify(equipmentRepository).updateImage(TEST_ID, newImageFileId);
    }
}
