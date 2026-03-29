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

import com.bluenet.web.domain.model.entity.Venue;
import com.bluenet.web.domain.model.vo.VenueVO;
import com.bluenet.web.domain.repository.VenueRepository;

/**
 * VenueDomainServiceImpl 单元测试
 */
@DisplayName("VenueDomainServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class VenueDomainServiceImplTest {

    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private VenueDomainServiceImpl venueDomainService;

    private static final Long TEST_ID = 1L;
    private static final Long TEST_FILE_ID = 100L;
    private static final String TEST_NAME = "测试场地";
    private static final String TEST_SUBTITLE = "测试副标题";
    private static final String TEST_DESCRIPTION = "测试描述";
    private static final Integer TEST_SORT_ORDER = 10;

    private VenueVO createTestVenueVO() {
        return VenueVO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .subtitle(TEST_SUBTITLE)
                .description(TEST_DESCRIPTION)
                .imageFileId(TEST_FILE_ID)
                .imageUrl("http://example.com/image.jpg")
                .sortOrder(TEST_SORT_ORDER)
                .build();
    }

    // ==================== getAllVenues 测试 ====================

    @Test
    @DisplayName("获取所有场地：应返回按排序倒序的场地列表")
    void getAllVenues_shouldReturnSortedList() {
        // 准备
        List<VenueVO> expectedVenues = new ArrayList<>();
        expectedVenues.add(createTestVenueVO());
        when(venueRepository.findAllOrderBySortOrderDesc()).thenReturn(expectedVenues);

        // 执行
        List<VenueVO> result = venueDomainService.getAllVenues();

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_NAME, result.get(0).getName());
        verify(venueRepository).findAllOrderBySortOrderDesc();
    }

    @Test
    @DisplayName("获取所有场地：无场地时应返回空列表")
    void getAllVenues_noVenues_shouldReturnEmptyList() {
        // 准备
        when(venueRepository.findAllOrderBySortOrderDesc()).thenReturn(new ArrayList<>());

        // 执行
        List<VenueVO> result = venueDomainService.getAllVenues();

        // 验证
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getVenueById 测试 ====================

    @Test
    @DisplayName("根据ID获取场地：场地存在时应返回场地")
    void getVenueById_existingVenue_shouldReturnVenue() {
        // 准备
        VenueVO expectedVenue = createTestVenueVO();
        when(venueRepository.findById(TEST_ID)).thenReturn(Optional.of(expectedVenue));

        // 执行
        Optional<VenueVO> result = venueDomainService.getVenueById(TEST_ID);

        // 验证
        assertTrue(result.isPresent());
        assertEquals(TEST_NAME, result.get().getName());
        verify(venueRepository).findById(TEST_ID);
    }

    @Test
    @DisplayName("根据ID获取场地：场地不存在时应返回空")
    void getVenueById_nonExistingVenue_shouldReturnEmpty() {
        // 准备
        when(venueRepository.findById(TEST_ID)).thenReturn(Optional.empty());

        // 执行
        Optional<VenueVO> result = venueDomainService.getVenueById(TEST_ID);

        // 验证
        assertFalse(result.isPresent());
    }

    // ==================== createVenue 测试 ====================

    @Test
    @DisplayName("创建场地：应成功创建并返回ID")
    void createVenue_shouldCreateAndReturnId() {
        // 准备
        Long expectedId = 1L;
        when(venueRepository.save(any(Venue.class))).thenReturn(expectedId);

        // 执行
        Long result = venueDomainService.createVenue(
                TEST_NAME,
                TEST_SUBTITLE,
                TEST_DESCRIPTION,
                TEST_FILE_ID,
                TEST_SORT_ORDER);

        // 验证
        assertEquals(expectedId, result);
        verify(venueRepository).save(any(Venue.class));
    }

    @Test
    @DisplayName("创建场地：无排序值时应使用默认值0")
    void createVenue_withoutSortOrder_shouldUseDefaultZero() {
        // 准备
        Long expectedId = 2L;
        when(venueRepository.save(any(Venue.class))).thenReturn(expectedId);

        // 执行
        Long result = venueDomainService.createVenue(
                TEST_NAME,
                TEST_SUBTITLE,
                TEST_DESCRIPTION,
                TEST_FILE_ID,
                null);

        // 验证
        assertEquals(expectedId, result);
        verify(venueRepository).save(any(Venue.class));
    }

    // ==================== updateVenue 测试 ====================

    @Test
    @DisplayName("更新场地：应成功更新场地信息")
    void updateVenue_shouldUpdateSuccessfully() {
        // 准备
        doNothing().when(venueRepository).update(any(Venue.class));

        // 执行
        venueDomainService.updateVenue(
                TEST_ID,
                TEST_NAME,
                TEST_SUBTITLE,
                TEST_DESCRIPTION,
                TEST_FILE_ID,
                TEST_SORT_ORDER);

        // 验证
        verify(venueRepository).update(any(Venue.class));
    }

    // ==================== deleteVenue 测试 ====================

    @Test
    @DisplayName("删除场地：应成功删除场地")
    void deleteVenue_shouldDeleteSuccessfully() {
        // 准备
        doNothing().when(venueRepository).deleteById(TEST_ID);

        // 执行
        venueDomainService.deleteVenue(TEST_ID);

        // 验证
        verify(venueRepository).deleteById(TEST_ID);
    }

    // ==================== existsById 测试 ====================

    @Test
    @DisplayName("检查场地存在：场地存在时应返回true")
    void existsById_existingVenue_shouldReturnTrue() {
        // 准备
        when(venueRepository.existsById(TEST_ID)).thenReturn(true);

        // 执行
        boolean result = venueDomainService.existsById(TEST_ID);

        // 验证
        assertTrue(result);
    }

    @Test
    @DisplayName("检查场地存在：场地不存在时应返回false")
    void existsById_nonExistingVenue_shouldReturnFalse() {
        // 准备
        when(venueRepository.existsById(TEST_ID)).thenReturn(false);

        // 执行
        boolean result = venueDomainService.existsById(TEST_ID);

        // 验证
        assertFalse(result);
    }

    // ==================== updateImage 测试 ====================

    @Test
    @DisplayName("更新场地图片：应成功更新图片")
    void updateImage_shouldUpdateSuccessfully() {
        // 准备
        Long newImageFileId = 200L;
        doNothing().when(venueRepository).updateImage(TEST_ID, newImageFileId);

        // 执行
        venueDomainService.updateImage(TEST_ID, newImageFileId);

        // 验证
        verify(venueRepository).updateImage(TEST_ID, newImageFileId);
    }
}
