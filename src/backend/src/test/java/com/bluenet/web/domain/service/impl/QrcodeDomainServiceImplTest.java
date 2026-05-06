package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Qrcode;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.QrcodeType;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.QrcodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 二维码领域服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class QrcodeDomainServiceImplTest {

    @Mock
    private QrcodeRepository qrcodeRepository;

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private QrcodeDomainServiceImpl qrcodeDomainService;

    private FileVO fileVO;

    @BeforeEach
    void setUp() {
        fileVO = FileVO.builder()
                .id(1L)
                .name("test-qrcode.png")
                .type(FileType.QRCODE)
                .build();
    }

    // ==================== saveQrcode 测试 ====================

    @Test
    @DisplayName("保存二维码 - CONSULTATION类型应成功")
    void saveQrcode_consultationType_shouldSucceed() {
        // When
        qrcodeDomainService.saveQrcode(fileVO, QrcodeType.CONSULTATION);

        // Then
        verify(qrcodeRepository).save(any(Qrcode.class));
    }

    @Test
    @DisplayName("保存二维码 - USER类型应成功")
    void saveQrcode_userType_shouldSucceed() {
        // When
        qrcodeDomainService.saveQrcode(fileVO, QrcodeType.USER);

        // Then
        verify(qrcodeRepository).save(any(Qrcode.class));
    }

    @Test
    @DisplayName("保存二维码 - ASSESSMENT类型应成功")
    void saveQrcode_assessmentType_shouldSucceed() {
        // When
        qrcodeDomainService.saveQrcode(fileVO, QrcodeType.ASSESSMENT);

        // Then
        verify(qrcodeRepository).save(any(Qrcode.class));
    }

    @Test
    @DisplayName("保存二维码 - 类型为null应抛出异常")
    void saveQrcode_nullType_shouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            qrcodeDomainService.saveQrcode(fileVO, null);
        });

        verify(qrcodeRepository, never()).save(any());
    }

    // ==================== getConsultationQrcodes 测试 ====================

    @Test
    @DisplayName("获取咨询群列表 - 应返回CONSULTATION类型二维码")
    void getConsultationQrcodes_shouldReturnConsultationQrcodes() {
        // Given
        Qrcode qrcode1 = Qrcode.forConsultation(1L);
        qrcode1.setId(1L);

        Qrcode qrcode2 = Qrcode.forConsultation(2L);
        qrcode2.setId(2L);

        List<Qrcode> expectedQrcodes = Arrays.asList(qrcode1, qrcode2);
        when(qrcodeRepository.findByType(QrcodeType.CONSULTATION)).thenReturn(expectedQrcodes);

        // When
        List<Qrcode> result = qrcodeDomainService.getConsultationQrcodes();

        // Then
        assertEquals(2, result.size());
        assertEquals(QrcodeType.CONSULTATION, result.get(0).getType());
        assertEquals(QrcodeType.CONSULTATION, result.get(1).getType());
        verify(qrcodeRepository).findByType(QrcodeType.CONSULTATION);
    }

    @Test
    @DisplayName("获取咨询群列表 - 无数据应返回空列表")
    void getConsultationQrcodes_noData_shouldReturnEmptyList() {
        // Given
        when(qrcodeRepository.findByType(QrcodeType.CONSULTATION)).thenReturn(List.of());

        // When
        List<Qrcode> result = qrcodeDomainService.getConsultationQrcodes();

        // Then
        assertTrue(result.isEmpty());
        verify(qrcodeRepository).findByType(QrcodeType.CONSULTATION);
    }

    // ==================== deleteConsultationQrcode 测试 ====================

    @Test
    @DisplayName("删除咨询群二维码 - 正常删除应成功")
    void deleteConsultationQrcode_normalCase_shouldSucceed() {
        // Given
        Qrcode qrcode = Qrcode.forConsultation(1L);
        qrcode.setId(1L);

        when(qrcodeRepository.findById(1L)).thenReturn(Optional.of(qrcode));
        doNothing().when(qrcodeRepository).deleteById(1L);
        doNothing().when(fileRepository).deleteFileById(1L);

        // When
        qrcodeDomainService.deleteConsultationQrcode(1L);

        // Then
        verify(qrcodeRepository).deleteById(1L);
        verify(fileRepository).deleteFileById(1L);
    }

    @Test
    @DisplayName("删除咨询群二维码 - 二维码不存在应抛出DataNotFound")
    void deleteConsultationQrcode_notFound_shouldThrowDataNotFound() {
        // Given
        when(qrcodeRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DataNotFound.class, () -> {
            qrcodeDomainService.deleteConsultationQrcode(999L);
        });

        verify(qrcodeRepository, never()).deleteById(any());
        verify(fileRepository, never()).deleteFileById(any());
    }

    @Test
    @DisplayName("删除咨询群二维码 - 非CONSULTATION类型应抛出异常")
    void deleteConsultationQrcode_wrongType_shouldThrowException() {
        // Given
        Qrcode userQrcode = Qrcode.reconstruct(1L, 1L, QrcodeType.USER, null, null, null);

        when(qrcodeRepository.findById(1L)).thenReturn(Optional.of(userQrcode));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            qrcodeDomainService.deleteConsultationQrcode(1L);
        });

        verify(qrcodeRepository, never()).deleteById(any());
        verify(fileRepository, never()).deleteFileById(any());
    }

    @Test
    @DisplayName("删除咨询群二维码 - 文件删除失败时仍应成功删除二维码记录")
    void deleteConsultationQrcode_fileDeleteFailed_shouldStillDeleteQrcode() {
        // Given
        Qrcode qrcode = Qrcode.forConsultation(1L);
        qrcode.setId(1L);

        when(qrcodeRepository.findById(1L)).thenReturn(Optional.of(qrcode));
        doNothing().when(qrcodeRepository).deleteById(1L);
        doThrow(new RuntimeException("File not found")).when(fileRepository).deleteFileById(1L);

        // When - 不应抛出异常
        qrcodeDomainService.deleteConsultationQrcode(1L);

        // Then
        verify(qrcodeRepository).deleteById(1L);
        verify(fileRepository).deleteFileById(1L);
    }

    @Test
    @DisplayName("删除咨询群二维码 - 删除ASSESSMENT类型应抛出异常")
    void deleteConsultationQrcode_assessmentType_shouldThrowException() {
        // Given
        Qrcode assessmentQrcode = Qrcode.reconstruct(1L, 1L, QrcodeType.ASSESSMENT, null, null, null);

        when(qrcodeRepository.findById(1L)).thenReturn(Optional.of(assessmentQrcode));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            qrcodeDomainService.deleteConsultationQrcode(1L);
        });

        verify(qrcodeRepository, never()).deleteById(any());
    }

    // ==================== updateConsultationQrcode 测试 ====================

    @Test
    @DisplayName("更新咨询群二维码 - 正常更新应成功")
    void updateConsultationQrcode_normalCase_shouldSucceed() {
        // Given
        Qrcode oldQrcode = Qrcode.forConsultation(1L);
        oldQrcode.setId(1L);

        FileVO newFileVO = FileVO.builder()
                .id(2L)
                .name("new-qrcode.png")
                .type(FileType.QRCODE)
                .build();

        when(qrcodeRepository.findById(1L)).thenReturn(Optional.of(oldQrcode));
        doNothing().when(fileRepository).deleteFileById(1L);

        // When
        qrcodeDomainService.updateConsultationQrcode(1L, newFileVO);

        // Then
        verify(qrcodeRepository).save(any(Qrcode.class));
        verify(fileRepository).deleteFileById(1L);
    }

    @Test
    @DisplayName("更新咨询群二维码 - 二维码不存在应抛出DataNotFound")
    void updateConsultationQrcode_notFound_shouldThrowDataNotFound() {
        // Given
        FileVO fileVO = FileVO.builder().id(1L).type(FileType.QRCODE).build();
        when(qrcodeRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DataNotFound.class, () -> {
            qrcodeDomainService.updateConsultationQrcode(999L, fileVO);
        });
    }

    // ==================== getAssessmentQrcodes 测试 ====================

    @Test
    @DisplayName("获取考核群二维码 - 无筛选条件应返回所有")
    void getAssessmentQrcodes_noFilter_shouldReturnAll() {
        // Given
        Qrcode qrcode1 = Qrcode.forAssessment(1L, 1, "COMPUTER_VISION", false);
        qrcode1.setId(1L);

        Qrcode qrcode2 = Qrcode.forAssessment(2L, 1, null, true);
        qrcode2.setId(2L);

        when(qrcodeRepository.findAssessmentQrcodes(null, null)).thenReturn(Arrays.asList(qrcode1, qrcode2));

        // When
        List<Qrcode> result = qrcodeDomainService.getAssessmentQrcodes(null, null);

        // Then
        assertEquals(2, result.size());
        verify(qrcodeRepository).findAssessmentQrcodes(null, null);
    }

    @Test
    @DisplayName("获取考核群二维码 - 按方向筛选应成功")
    void getAssessmentQrcodes_byDirection_shouldSucceed() {
        // Given
        String direction = "COMPUTER_VISION";
        Qrcode qrcode = Qrcode.forAssessment(1L, 1, direction, false);
        qrcode.setId(1L);

        when(qrcodeRepository.findAssessmentQrcodes(direction, null)).thenReturn(List.of(qrcode));

        // When
        List<Qrcode> result = qrcodeDomainService.getAssessmentQrcodes(direction, null);

        // Then
        assertEquals(1, result.size());
        assertEquals(direction, result.get(0).getDirection());
    }

    // ==================== saveAssessmentQrcode 测试 ====================

    @Test
    @DisplayName("保存考核群二维码 - 正常保存应成功")
    void saveAssessmentQrcode_normalCase_shouldSucceed() {
        // Given
        Qrcode qrcode = Qrcode.forAssessment(1L, 1, "COMPUTER_VISION", false);

        // When
        qrcodeDomainService.saveAssessmentQrcode(qrcode);

        // Then
        verify(qrcodeRepository).save(qrcode);
    }

    // ==================== updateAssessmentQrcode 测试 ====================

    @Test
    @DisplayName("更新考核群二维码 - 正常更新应成功")
    void updateAssessmentQrcode_normalCase_shouldSucceed() {
        // Given
        Qrcode oldQrcode = Qrcode.forAssessment(1L, 1, "COMPUTER_VISION", false);
        oldQrcode.setId(1L);

        FileVO newFileVO = FileVO.builder().id(2L).type(FileType.QRCODE).build();
        String newDirection = "STRUCTURAL_DESIGN";
        Integer newEpoch = 2;
        Boolean newIsShared = false;

        when(qrcodeRepository.findById(1L)).thenReturn(Optional.of(oldQrcode));
        doNothing().when(fileRepository).deleteFileById(1L);

        // When
        qrcodeDomainService.updateAssessmentQrcode(1L, newFileVO, newDirection, newEpoch, newIsShared);

        // Then
        verify(qrcodeRepository).save(any(Qrcode.class));
        verify(fileRepository).deleteFileById(1L);
    }

    @Test
    @DisplayName("更新考核群二维码 - 设置为共用时应清空方向")
    void updateAssessmentQrcode_setToShared_shouldClearDirection() {
        // Given
        Qrcode oldQrcode = Qrcode.forAssessment(1L, 1, "COMPUTER_VISION", false);
        oldQrcode.setId(1L);

        when(qrcodeRepository.findById(1L)).thenReturn(Optional.of(oldQrcode));

        // When
        qrcodeDomainService.updateAssessmentQrcode(1L, null, null, null, true);

        // Then
        verify(qrcodeRepository).save(argThat(qrcode -> qrcode.getIsShared() && qrcode.getDirection() == null));
    }

    // ==================== deleteAssessmentQrcode 测试 ====================

    @Test
    @DisplayName("删除考核群二维码 - 正常删除应成功")
    void deleteAssessmentQrcode_normalCase_shouldSucceed() {
        // Given
        Qrcode qrcode = Qrcode.forAssessment(1L, 1, "COMPUTER_VISION", false);
        qrcode.setId(1L);

        when(qrcodeRepository.findById(1L)).thenReturn(Optional.of(qrcode));
        doNothing().when(qrcodeRepository).deleteById(1L);
        doNothing().when(fileRepository).deleteFileById(1L);

        // When
        qrcodeDomainService.deleteAssessmentQrcode(1L);

        // Then
        verify(qrcodeRepository).deleteById(1L);
        verify(fileRepository).deleteFileById(1L);
    }
}
