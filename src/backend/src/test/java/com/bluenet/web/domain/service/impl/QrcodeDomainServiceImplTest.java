package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.Qrcode;
import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.QrcodeType;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.QrcodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link QrcodeDomainServiceImpl} 单元测试。
 */
@DisplayName("QrcodeDomainServiceImpl 测试")
@ExtendWith(MockitoExtension.class)
class QrcodeDomainServiceImplTest {

    @Mock
    private QrcodeRepository qrcodeRepository;

    @Mock
    private FileRepository fileRepository;

    private QrcodeDomainServiceImpl domainService;

    @BeforeEach
    void setUp() {
        domainService = new QrcodeDomainServiceImpl(qrcodeRepository, fileRepository);
    }

    @Test
    @DisplayName("saveQrcode: 类型为空时应抛出 IllegalArgumentException")
    void saveQrcode_nullType_shouldThrowIllegalArgumentException() {
        File file = File.reconstruct(1L, "qr.png", FileType.QRCODE, "url", FileStatus.ACTIVE, LocalDateTime.now());

        assertThrows(IllegalArgumentException.class, () -> domainService.saveQrcode(file, null));
        verify(qrcodeRepository, never()).save(any(Qrcode.class));
    }

    @Test
    @DisplayName("saveQrcode: 应保存咨询群二维码")
    void saveQrcode_consultation_shouldSave() {
        File file = File.reconstruct(1L, "qr.png", FileType.QRCODE, "url", FileStatus.ACTIVE, LocalDateTime.now());
        doNothing().when(qrcodeRepository).save(any(Qrcode.class));

        domainService.saveQrcode(file, QrcodeType.CONSULTATION);

        ArgumentCaptor<Qrcode> captor = ArgumentCaptor.forClass(Qrcode.class);
        verify(qrcodeRepository).save(captor.capture());
        Qrcode saved = captor.getValue();
        assertEquals(file.getId(), saved.getFileId());
        assertEquals(QrcodeType.CONSULTATION, saved.getType());
    }

    @Test
    @DisplayName("getConsultationQrcodes: 应按类型查询咨询群二维码")
    void getConsultationQrcodes_shouldReturnConsultationList() {
        Qrcode qr = Qrcode.reconstruct(1L, 10L, QrcodeType.CONSULTATION, null, null, null);
        when(qrcodeRepository.findByType(QrcodeType.CONSULTATION)).thenReturn(List.of(qr));

        List<Qrcode> result = domainService.getConsultationQrcodes();

        assertEquals(1, result.size());
        assertEquals(qr, result.get(0));
    }

    @Test
    @DisplayName("updateConsultationQrcode: 二维码不存在时应抛出 DataNotFound")
    void updateConsultationQrcode_notFound_shouldThrowDataNotFound() {
        Long id = 1L;
        File file = File.reconstruct(2L, "qr.png", FileType.QRCODE, "url", FileStatus.ACTIVE, LocalDateTime.now());
        when(qrcodeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(DataNotFound.class, () -> domainService.updateConsultationQrcode(id, file));
    }

    @Test
    @DisplayName("updateConsultationQrcode: 非咨询群二维码时应抛出 IllegalArgumentException")
    void updateConsultationQrcode_wrongType_shouldThrowIllegalArgumentException() {
        Long id = 1L;
        File file = File.reconstruct(2L, "qr.png", FileType.QRCODE, "url", FileStatus.ACTIVE, LocalDateTime.now());
        Qrcode qrcode = Qrcode.reconstruct(id, 10L, QrcodeType.ASSESSMENT, 1, "computer_vision", false);
        when(qrcodeRepository.findById(id)).thenReturn(Optional.of(qrcode));

        assertThrows(IllegalArgumentException.class, () -> domainService.updateConsultationQrcode(id, file));
    }

    @Test
    @DisplayName("updateConsultationQrcode: 应更新文件 ID 并删除旧关联文件")
    void updateConsultationQrcode_success_shouldUpdateAndDeleteOldFile() {
        Long id = 1L;
        Long oldFileId = 10L;
        Long newFileId = 20L;
        Qrcode qrcode = Qrcode.reconstruct(id, oldFileId, QrcodeType.CONSULTATION, null, null, null);
        File newFile = File.reconstruct(
                newFileId,
                "qr.png",
                FileType.QRCODE,
                "url",
                FileStatus.ACTIVE,
                LocalDateTime.now());
        when(qrcodeRepository.findById(id)).thenReturn(Optional.of(qrcode));
        doNothing().when(fileRepository).deleteFileById(oldFileId);

        domainService.updateConsultationQrcode(id, newFile);

        assertEquals(newFileId, qrcode.getFileId());
        verify(qrcodeRepository).save(qrcode);
        verify(fileRepository).deleteFileById(oldFileId);
    }

    @Test
    @DisplayName("updateConsultationQrcode: 新旧文件 ID 相同时不删除文件")
    void updateConsultationQrcode_sameFileId_shouldNotDeleteFile() {
        Long id = 1L;
        Long fileId = 10L;
        Qrcode qrcode = Qrcode.reconstruct(id, fileId, QrcodeType.CONSULTATION, null, null, null);
        File newFile = File.reconstruct(
                fileId,
                "qr.png",
                FileType.QRCODE,
                "url",
                FileStatus.ACTIVE,
                LocalDateTime.now());
        when(qrcodeRepository.findById(id)).thenReturn(Optional.of(qrcode));

        domainService.updateConsultationQrcode(id, newFile);

        verify(qrcodeRepository).save(qrcode);
        verify(fileRepository, never()).deleteFileById(any());
    }

    @Test
    @DisplayName("deleteConsultationQrcode: 二维码不存在时应抛出 DataNotFound")
    void deleteConsultationQrcode_notFound_shouldThrowDataNotFound() {
        Long id = 1L;
        when(qrcodeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(DataNotFound.class, () -> domainService.deleteConsultationQrcode(id));
    }

    @Test
    @DisplayName("deleteConsultationQrcode: 非咨询群二维码时应抛出 IllegalArgumentException")
    void deleteConsultationQrcode_wrongType_shouldThrowIllegalArgumentException() {
        Long id = 1L;
        Qrcode qrcode = Qrcode.reconstruct(id, 10L, QrcodeType.ASSESSMENT, 1, "computer_vision", false);
        when(qrcodeRepository.findById(id)).thenReturn(Optional.of(qrcode));

        assertThrows(IllegalArgumentException.class, () -> domainService.deleteConsultationQrcode(id));
    }

    @Test
    @DisplayName("deleteConsultationQrcode: 应删除二维码记录及关联文件")
    void deleteConsultationQrcode_success_shouldDeleteRecordAndFile() {
        Long id = 1L;
        Long fileId = 10L;
        Qrcode qrcode = Qrcode.reconstruct(id, fileId, QrcodeType.CONSULTATION, null, null, null);
        when(qrcodeRepository.findById(id)).thenReturn(Optional.of(qrcode));
        doNothing().when(fileRepository).deleteFileById(fileId);

        domainService.deleteConsultationQrcode(id);

        verify(qrcodeRepository).deleteById(id);
        verify(fileRepository).deleteFileById(fileId);
    }

    @Test
    @DisplayName("getAssessmentQrcodes: 应按方向和轮次查询考核群二维码")
    void getAssessmentQrcodes_shouldReturnAssessmentList() {
        String direction = "computer_vision";
        Integer epoch = 1;
        Qrcode qr = Qrcode.reconstruct(1L, 10L, QrcodeType.ASSESSMENT, epoch, direction, false);
        when(qrcodeRepository.findAssessmentQrcodes(direction, epoch)).thenReturn(List.of(qr));

        List<Qrcode> result = domainService.getAssessmentQrcodes(direction, epoch);

        assertEquals(1, result.size());
        assertEquals(qr, result.get(0));
    }

    @Test
    @DisplayName("saveAssessmentQrcode: 考核轮次非正整数时应抛出 IllegalArgumentException")
    void saveAssessmentQrcode_nonPositiveEpoch_shouldThrowIllegalArgumentException() {
        Qrcode qrcode = Qrcode.reconstruct(null, 1L, QrcodeType.ASSESSMENT, 0, "computer_vision", false);

        assertThrows(IllegalArgumentException.class, () -> domainService.saveAssessmentQrcode(qrcode));
    }

    @Test
    @DisplayName("saveAssessmentQrcode: 共用二维码与已有记录冲突时应抛出 IllegalArgumentException")
    void saveAssessmentQrcode_sharedConflict_shouldThrowIllegalArgumentException() {
        Qrcode existing = Qrcode.reconstruct(10L, 2L, QrcodeType.ASSESSMENT, 1, "structural_design", false);
        Qrcode newQrcode = Qrcode.reconstruct(null, 1L, QrcodeType.ASSESSMENT, 1, "computer_vision", true);
        when(qrcodeRepository.findAssessmentByEpoch(1)).thenReturn(List.of(existing));

        assertThrows(IllegalArgumentException.class, () -> domainService.saveAssessmentQrcode(newQrcode));
    }

    @Test
    @DisplayName("saveAssessmentQrcode: 同轮次同方向冲突时应抛出 IllegalArgumentException")
    void saveAssessmentQrcode_directionConflict_shouldThrowIllegalArgumentException() {
        Qrcode existing = Qrcode.reconstruct(10L, 2L, QrcodeType.ASSESSMENT, 1, "computer_vision", false);
        Qrcode newQrcode = Qrcode.forAssessment(1L, 1, "computer_vision", false);
        when(qrcodeRepository.findAssessmentByEpoch(1)).thenReturn(List.of(existing));

        assertThrows(IllegalArgumentException.class, () -> domainService.saveAssessmentQrcode(newQrcode));
    }

    @Test
    @DisplayName("saveAssessmentQrcode: 无冲突时应保存考核群二维码")
    void saveAssessmentQrcode_noConflict_shouldSave() {
        Qrcode qrcode = Qrcode.forAssessment(1L, 1, "computer_vision", false);
        when(qrcodeRepository.findAssessmentByEpoch(1)).thenReturn(Collections.emptyList());

        domainService.saveAssessmentQrcode(qrcode);

        verify(qrcodeRepository).save(qrcode);
    }

    @Test
    @DisplayName("updateAssessmentQrcode: 二维码不存在时应抛出 DataNotFound")
    void updateAssessmentQrcode_notFound_shouldThrowDataNotFound() {
        Long id = 1L;
        File file = File.reconstruct(2L, "qr.png", FileType.QRCODE, "url", FileStatus.ACTIVE, LocalDateTime.now());
        when(qrcodeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                DataNotFound.class,
                () -> domainService.updateAssessmentQrcode(id, file, "computer_vision", 1, false));
    }

    @Test
    @DisplayName("updateAssessmentQrcode: 非考核群二维码时应抛出 IllegalArgumentException")
    void updateAssessmentQrcode_wrongType_shouldThrowIllegalArgumentException() {
        Long id = 1L;
        File file = File.reconstruct(2L, "qr.png", FileType.QRCODE, "url", FileStatus.ACTIVE, LocalDateTime.now());
        Qrcode qrcode = Qrcode.reconstruct(id, 10L, QrcodeType.CONSULTATION, null, null, null);
        when(qrcodeRepository.findById(id)).thenReturn(Optional.of(qrcode));

        assertThrows(
                IllegalArgumentException.class,
                () -> domainService.updateAssessmentQrcode(id, file, "computer_vision", 1, false));
    }

    @Test
    @DisplayName("updateAssessmentQrcode: 更新后考核轮次非正整数时应抛出 IllegalArgumentException")
    void updateAssessmentQrcode_nonPositiveEpoch_shouldThrowIllegalArgumentException() {
        Long id = 1L;
        Qrcode qrcode = Qrcode.reconstruct(id, 10L, QrcodeType.ASSESSMENT, 1, "computer_vision", false);
        when(qrcodeRepository.findById(id)).thenReturn(Optional.of(qrcode));

        assertThrows(
                IllegalArgumentException.class,
                () -> domainService.updateAssessmentQrcode(id, null, null, 0, null));
    }

    @Test
    @DisplayName("updateAssessmentQrcode: 与排除自身外的记录冲突时应抛出 IllegalArgumentException")
    void updateAssessmentQrcode_conflict_shouldThrowIllegalArgumentException() {
        Long id = 1L;
        Qrcode qrcode = Qrcode.reconstruct(id, 10L, QrcodeType.ASSESSMENT, 1, "computer_vision", false);
        Qrcode existing = Qrcode.reconstruct(2L, 20L, QrcodeType.ASSESSMENT, 1, "computer_vision", false);
        when(qrcodeRepository.findById(id)).thenReturn(Optional.of(qrcode));
        when(qrcodeRepository.findAssessmentByEpoch(1)).thenReturn(List.of(existing));

        assertThrows(
                IllegalArgumentException.class,
                () -> domainService.updateAssessmentQrcode(id, null, "computer_vision", 1, false));
    }

    @Test
    @DisplayName("updateAssessmentQrcode: 排除自身后不应判定为冲突")
    void updateAssessmentQrcode_excludeSelf_shouldNotThrow() {
        Long id = 1L;
        Qrcode qrcode = Qrcode.reconstruct(id, 10L, QrcodeType.ASSESSMENT, 1, "computer_vision", false);
        when(qrcodeRepository.findById(id)).thenReturn(Optional.of(qrcode));
        when(qrcodeRepository.findAssessmentByEpoch(1)).thenReturn(List.of(qrcode));

        domainService.updateAssessmentQrcode(id, null, "computer_vision", 1, false);

        verify(qrcodeRepository).save(qrcode);
    }

    @Test
    @DisplayName("updateAssessmentQrcode: 应更新文件、方向、轮次并删除旧文件")
    void updateAssessmentQrcode_success_shouldUpdateFieldsAndDeleteOldFile() {
        Long id = 1L;
        Long oldFileId = 10L;
        Long newFileId = 20L;
        Qrcode qrcode = Qrcode.reconstruct(id, oldFileId, QrcodeType.ASSESSMENT, 1, "computer_vision", false);
        File newFile = File.reconstruct(
                newFileId,
                "qr.png",
                FileType.QRCODE,
                "url",
                FileStatus.ACTIVE,
                LocalDateTime.now());
        when(qrcodeRepository.findById(id)).thenReturn(Optional.of(qrcode));
        when(qrcodeRepository.findAssessmentByEpoch(2)).thenReturn(Collections.emptyList());
        doNothing().when(fileRepository).deleteFileById(oldFileId);

        domainService.updateAssessmentQrcode(id, newFile, "embedded", 2, false);

        assertEquals(newFileId, qrcode.getFileId());
        assertEquals("embedded", qrcode.getDirection());
        assertEquals(2, qrcode.getEpoch());
        assertEquals(false, qrcode.getIsShared());
        verify(qrcodeRepository).save(qrcode);
        verify(fileRepository).deleteFileById(oldFileId);
    }

    @Test
    @DisplayName("updateAssessmentQrcode: 设置为共用后方向应被清空")
    void updateAssessmentQrcode_setShared_shouldClearDirection() {
        Long id = 1L;
        Qrcode qrcode = Qrcode.reconstruct(id, 10L, QrcodeType.ASSESSMENT, 1, "computer_vision", false);
        when(qrcodeRepository.findById(id)).thenReturn(Optional.of(qrcode));
        when(qrcodeRepository.findAssessmentByEpoch(1)).thenReturn(Collections.emptyList());

        domainService.updateAssessmentQrcode(id, null, null, null, true);

        assertTrue(qrcode.getIsShared());
        assertNull(qrcode.getDirection());
    }

    @Test
    @DisplayName("updateAssessmentQrcode: 共用二维码方向非空时应抛出 IllegalArgumentException")
    void updateAssessmentQrcode_sharedWithDirection_shouldThrowIllegalArgumentException() {
        Long id = 1L;
        Qrcode qrcode = Qrcode.reconstruct(id, 10L, QrcodeType.ASSESSMENT, 1, "computer_vision", true);
        when(qrcodeRepository.findById(id)).thenReturn(Optional.of(qrcode));
        when(qrcodeRepository.findAssessmentByEpoch(1)).thenReturn(Collections.emptyList());

        assertThrows(
                IllegalArgumentException.class,
                () -> domainService.updateAssessmentQrcode(id, null, "computer_vision", null, null));
    }

    @Test
    @DisplayName("updateAssessmentQrcode: 非共用二维码方向为空时应抛出 IllegalArgumentException")
    void updateAssessmentQrcode_nonSharedWithoutDirection_shouldThrowIllegalArgumentException() {
        Long id = 1L;
        Qrcode qrcode = Qrcode.reconstruct(id, 10L, QrcodeType.ASSESSMENT, 1, null, false);
        when(qrcodeRepository.findById(id)).thenReturn(Optional.of(qrcode));
        when(qrcodeRepository.findAssessmentByEpoch(1)).thenReturn(Collections.emptyList());

        assertThrows(
                IllegalArgumentException.class,
                () -> domainService.updateAssessmentQrcode(id, null, null, null, false));
    }

    @Test
    @DisplayName("deleteAssessmentQrcode: 二维码不存在时应抛出 DataNotFound")
    void deleteAssessmentQrcode_notFound_shouldThrowDataNotFound() {
        Long id = 1L;
        when(qrcodeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(DataNotFound.class, () -> domainService.deleteAssessmentQrcode(id));
    }

    @Test
    @DisplayName("deleteAssessmentQrcode: 非考核群二维码时应抛出 IllegalArgumentException")
    void deleteAssessmentQrcode_wrongType_shouldThrowIllegalArgumentException() {
        Long id = 1L;
        Qrcode qrcode = Qrcode.reconstruct(id, 10L, QrcodeType.CONSULTATION, null, null, null);
        when(qrcodeRepository.findById(id)).thenReturn(Optional.of(qrcode));

        assertThrows(IllegalArgumentException.class, () -> domainService.deleteAssessmentQrcode(id));
    }

    @Test
    @DisplayName("deleteAssessmentQrcode: 应删除二维码记录及关联文件")
    void deleteAssessmentQrcode_success_shouldDeleteRecordAndFile() {
        Long id = 1L;
        Long fileId = 10L;
        Qrcode qrcode = Qrcode.reconstruct(id, fileId, QrcodeType.ASSESSMENT, 1, "computer_vision", false);
        when(qrcodeRepository.findById(id)).thenReturn(Optional.of(qrcode));
        doNothing().when(fileRepository).deleteFileById(fileId);

        domainService.deleteAssessmentQrcode(id);

        verify(qrcodeRepository).deleteById(id);
        verify(fileRepository).deleteFileById(fileId);
    }
}
