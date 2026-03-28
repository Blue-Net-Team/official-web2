package com.bluenet.web.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bluenet.web.api.dto.college.CollegeDTO;
import com.bluenet.web.api.dto.college.CreateCollegeRequestDTO;
import com.bluenet.web.api.dto.college.UpdateCollegeRequestDTO;
import com.bluenet.web.application.converter.CollegeConverter;
import com.bluenet.web.domain.model.vo.CollegeVO;
import com.bluenet.web.domain.service.CollegeDomainService;

/**
 * CollegeServiceImpl 单元测试
 * <p>
 * 测试学院应用服务的协调逻辑
 * </p>
 */
@DisplayName("CollegeServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class CollegeServiceImplTest {

    @Mock
    private CollegeDomainService collegeDomainService;

    @Mock
    private CollegeConverter collegeConverter;

    @InjectMocks
    private CollegeServiceImpl collegeService;

    private static final Long TEST_ID = 1L;
    private static final String TEST_NAME = "计算机科学与技术学院";
    private static final String TEST_NAME_2 = "软件学院";

    private CollegeVO createTestCollegeVO() {
        return CollegeVO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();
    }

    private CollegeVO createTestCollegeVO(Long id, String name) {
        return CollegeVO.builder()
                .id(id)
                .name(name)
                .build();
    }

    private CollegeDTO createTestCollegeDTO() {
        return CollegeDTO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();
    }

    private CollegeDTO createTestCollegeDTO(Long id, String name) {
        return CollegeDTO.builder()
                .id(id)
                .name(name)
                .build();
    }

    private CreateCollegeRequestDTO createTestCreateRequest() {
        return CreateCollegeRequestDTO.builder()
                .name(TEST_NAME)
                .build();
    }

    private UpdateCollegeRequestDTO createTestUpdateRequest() {
        return UpdateCollegeRequestDTO.builder()
                .name(TEST_NAME_2)
                .build();
    }

    // ==================== getAllColleges 方法测试 ====================

    @Nested
    @DisplayName("getAllColleges 方法测试")
    class GetAllCollegesTests {

        @Test
        @DisplayName("正常情况：应返回转换后的DTO列表")
        void getAllColleges_withColleges_shouldReturnDTOList() {
            // 准备
            List<CollegeVO> voList = new ArrayList<>();
            voList.add(createTestCollegeVO(1L, "计算机学院"));
            voList.add(createTestCollegeVO(2L, "软件学院"));

            List<CollegeDTO> expectedDTOs = new ArrayList<>();
            expectedDTOs.add(createTestCollegeDTO(1L, "计算机学院"));
            expectedDTOs.add(createTestCollegeDTO(2L, "软件学院"));

            when(collegeDomainService.getAllColleges()).thenReturn(voList);
            when(collegeConverter.convertToDTOList(voList)).thenReturn(expectedDTOs);

            // 执行
            List<CollegeDTO> result = collegeService.getAllColleges();

            // 验证
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(1L, result.get(0).getId());
            assertEquals("计算机学院", result.get(0).getName());
            verify(collegeDomainService).getAllColleges();
            verify(collegeConverter).convertToDTOList(voList);
        }

        @Test
        @DisplayName("无学院数据：应返回空列表")
        void getAllColleges_noColleges_shouldReturnEmptyList() {
            // 准备
            List<CollegeVO> voList = new ArrayList<>();
            List<CollegeDTO> expectedDTOs = new ArrayList<>();

            when(collegeDomainService.getAllColleges()).thenReturn(voList);
            when(collegeConverter.convertToDTOList(voList)).thenReturn(expectedDTOs);

            // 执行
            List<CollegeDTO> result = collegeService.getAllColleges();

            // 验证
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(collegeDomainService).getAllColleges();
            verify(collegeConverter).convertToDTOList(voList);
        }
    }

    // ==================== createCollege 方法测试 ====================

    @Nested
    @DisplayName("createCollege 方法测试")
    class CreateCollegeTests {

        @Test
        @DisplayName("正常创建：应返回创建后的DTO")
        void createCollege_validRequest_shouldReturnDTO() {
            // 准备
            CreateCollegeRequestDTO request = createTestCreateRequest();
            CollegeVO createdVO = createTestCollegeVO();
            CollegeDTO expectedDTO = createTestCollegeDTO();

            when(collegeDomainService.createCollege(TEST_NAME)).thenReturn(TEST_ID);
            when(collegeDomainService.getCollegeById(TEST_ID)).thenReturn(Optional.of(createdVO));
            when(collegeConverter.convertToDTO(createdVO)).thenReturn(expectedDTO);

            // 执行
            CollegeDTO result = collegeService.createCollege(request);

            // 验证
            assertNotNull(result);
            assertEquals(TEST_ID, result.getId());
            assertEquals(TEST_NAME, result.getName());
            verify(collegeDomainService).createCollege(TEST_NAME);
            verify(collegeDomainService).getCollegeById(TEST_ID);
            verify(collegeConverter).convertToDTO(createdVO);
        }

        @Test
        @DisplayName("创建后查询为空：应抛出IllegalStateException")
        void createCollege_createFailed_shouldThrowException() {
            // 准备
            CreateCollegeRequestDTO request = createTestCreateRequest();

            when(collegeDomainService.createCollege(TEST_NAME)).thenReturn(TEST_ID);
            when(collegeDomainService.getCollegeById(TEST_ID)).thenReturn(Optional.empty());

            // 执行 & 验证
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> collegeService.createCollege(request));
            assertEquals("创建学院失败", exception.getMessage());

            verify(collegeDomainService).createCollege(TEST_NAME);
            verify(collegeDomainService).getCollegeById(TEST_ID);
        }

        @Test
        @DisplayName("名称已存在：应抛出IllegalArgumentException")
        void createCollege_duplicateName_shouldThrowException() {
            // 准备
            CreateCollegeRequestDTO request = createTestCreateRequest();

            when(collegeDomainService.createCollege(TEST_NAME))
                    .thenThrow(new IllegalArgumentException("学院名称已存在"));

            // 执行 & 验证
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> collegeService.createCollege(request));
            assertEquals("学院名称已存在", exception.getMessage());

            verify(collegeDomainService).createCollege(TEST_NAME);
            verify(collegeDomainService, never()).getCollegeById(any());
        }
    }

    // ==================== updateCollege 方法测试 ====================

    @Nested
    @DisplayName("updateCollege 方法测试")
    class UpdateCollegeTests {

        @Test
        @DisplayName("正常更新：应返回更新后的DTO")
        void updateCollege_validRequest_shouldReturnDTO() {
            // 准备
            UpdateCollegeRequestDTO request = createTestUpdateRequest();
            CollegeVO updatedVO = createTestCollegeVO(TEST_ID, TEST_NAME_2);
            CollegeDTO expectedDTO = createTestCollegeDTO(TEST_ID, TEST_NAME_2);

            when(collegeDomainService.getCollegeById(TEST_ID)).thenReturn(Optional.of(updatedVO));
            when(collegeConverter.convertToDTO(updatedVO)).thenReturn(expectedDTO);

            // 执行
            CollegeDTO result = collegeService.updateCollege(TEST_ID, request);

            // 验证
            assertNotNull(result);
            assertEquals(TEST_ID, result.getId());
            assertEquals(TEST_NAME_2, result.getName());
            verify(collegeDomainService).updateCollege(TEST_ID, TEST_NAME_2);
            verify(collegeDomainService).getCollegeById(TEST_ID);
            verify(collegeConverter).convertToDTO(updatedVO);
        }

        @Test
        @DisplayName("更新后查询为空：应抛出IllegalStateException")
        void updateCollege_updateFailed_shouldThrowException() {
            // 准备
            UpdateCollegeRequestDTO request = createTestUpdateRequest();

            when(collegeDomainService.getCollegeById(TEST_ID)).thenReturn(Optional.empty());

            // 执行 & 验证
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> collegeService.updateCollege(TEST_ID, request));
            assertEquals("更新学院失败", exception.getMessage());

            verify(collegeDomainService).updateCollege(TEST_ID, TEST_NAME_2);
            verify(collegeDomainService).getCollegeById(TEST_ID);
        }

        @Test
        @DisplayName("学院不存在：应抛出IllegalArgumentException")
        void updateCollege_nonExistingCollege_shouldThrowException() {
            // 准备
            UpdateCollegeRequestDTO request = createTestUpdateRequest();

            doThrow(new IllegalArgumentException("学院不存在"))
                    .when(collegeDomainService)
                    .updateCollege(TEST_ID, TEST_NAME_2);

            // 执行 & 验证
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> collegeService.updateCollege(TEST_ID, request));
            assertEquals("学院不存在", exception.getMessage());

            verify(collegeDomainService).updateCollege(TEST_ID, TEST_NAME_2);
            verify(collegeDomainService, never()).getCollegeById(any());
        }

        @Test
        @DisplayName("名称已被其他学院使用：应抛出IllegalArgumentException")
        void updateCollege_duplicateName_shouldThrowException() {
            // 准备
            UpdateCollegeRequestDTO request = createTestUpdateRequest();

            doThrow(new IllegalArgumentException("学院名称已存在"))
                    .when(collegeDomainService)
                    .updateCollege(TEST_ID, TEST_NAME_2);

            // 执行 & 验证
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> collegeService.updateCollege(TEST_ID, request));
            assertEquals("学院名称已存在", exception.getMessage());

            verify(collegeDomainService).updateCollege(TEST_ID, TEST_NAME_2);
        }
    }

    // ==================== deleteCollege 方法测试 ====================

    @Nested
    @DisplayName("deleteCollege 方法测试")
    class DeleteCollegeTests {

        @Test
        @DisplayName("正常删除：应成功删除学院")
        void deleteCollege_validId_shouldDeleteSuccessfully() {
            // 准备
            doNothing().when(collegeDomainService).deleteCollege(TEST_ID);

            // 执行
            collegeService.deleteCollege(TEST_ID);

            // 验证
            verify(collegeDomainService).deleteCollege(TEST_ID);
        }

        @Test
        @DisplayName("学院不存在：应抛出IllegalArgumentException")
        void deleteCollege_nonExistingCollege_shouldThrowException() {
            // 准备
            doThrow(new IllegalArgumentException("学院不存在"))
                    .when(collegeDomainService)
                    .deleteCollege(TEST_ID);

            // 执行 & 验证
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> collegeService.deleteCollege(TEST_ID));
            assertEquals("学院不存在", exception.getMessage());

            verify(collegeDomainService).deleteCollege(TEST_ID);
        }

        @Test
        @DisplayName("有关联用户：应抛出IllegalArgumentException")
        void deleteCollege_withAssociatedUsers_shouldThrowException() {
            // 准备
            doThrow(new IllegalArgumentException("该学院下存在关联用户，无法删除"))
                    .when(collegeDomainService)
                    .deleteCollege(TEST_ID);

            // 执行 & 验证
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> collegeService.deleteCollege(TEST_ID));
            assertEquals("该学院下存在关联用户，无法删除", exception.getMessage());

            verify(collegeDomainService).deleteCollege(TEST_ID);
        }

        @Test
        @DisplayName("有关联报名记录：应抛出IllegalArgumentException")
        void deleteCollege_withAssociatedEnrolls_shouldThrowException() {
            // 准备
            doThrow(new IllegalArgumentException("该学院下存在关联报名记录，无法删除"))
                    .when(collegeDomainService)
                    .deleteCollege(TEST_ID);

            // 执行 & 验证
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> collegeService.deleteCollege(TEST_ID));
            assertEquals("该学院下存在关联报名记录，无法删除", exception.getMessage());

            verify(collegeDomainService).deleteCollege(TEST_ID);
        }
    }
}
