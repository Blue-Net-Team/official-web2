package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.command.enrollform.EnrollFormCommands;
import com.bluenet.web.application.result.enrollform.EnrollFormResult;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.service.FileDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link EnrollFormAppServiceImpl} 单元测试。
 */
@DisplayName("EnrollFormAppServiceImpl 测试")
@ExtendWith(MockitoExtension.class)
class EnrollFormAppServiceImplTest {

    @Mock
    private FileRepository fileRepository;
    @Mock
    private FileDomainService fileDomainService;

    private EnrollFormAppServiceImpl appService;

    @BeforeEach
    void setUp() {
        appService = new EnrollFormAppServiceImpl(fileRepository, fileDomainService);
    }

    private File enrollFormFile(Long id, FileStatus status, String name) {
        return File.reconstruct(id, name, FileType.ENROLL_FORM, "url", status, LocalDateTime.now());
    }

    // ---------- getCurrentEnrollForm ----------

    @Test
    @DisplayName("getCurrentEnrollForm: 存在报名表时应返回结果")
    void getCurrentEnrollForm_existingForm_shouldReturnResult() {
        File file = enrollFormFile(1L, FileStatus.ACTIVE, "enroll_form-uuid.pdf");
        when(fileRepository.findLatestByType(FileType.ENROLL_FORM)).thenReturn(Optional.of(file));

        Optional<EnrollFormResult> result = appService.getCurrentEnrollForm();

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().fileId());
        assertEquals(file.getCreatedAt(), result.get().createdAt());
    }

    @Test
    @DisplayName("getCurrentEnrollForm: 不存在报名表时应返回空")
    void getCurrentEnrollForm_noForm_shouldReturnEmpty() {
        when(fileRepository.findLatestByType(FileType.ENROLL_FORM)).thenReturn(Optional.empty());

        Optional<EnrollFormResult> result = appService.getCurrentEnrollForm();

        assertTrue(result.isEmpty());
    }

    // ---------- setEnrollForm ----------

    @Test
    @DisplayName("setEnrollForm: 文件不存在时应抛出 DataNotFound")
    void setEnrollForm_fileNotFound_shouldThrowDataNotFound() {
        when(fileDomainService.getFileById(1L)).thenThrow(new DataNotFound("文件不存在"));

        assertThrows(
                DataNotFound.class,
                () -> appService.setEnrollForm(new EnrollFormCommands.SetEnrollFormCommand(1L)));
        verify(fileRepository, never()).deleteFileById(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("setEnrollForm: 文件类型不匹配时应抛出 BadRequest 且不删旧表")
    void setEnrollForm_wrongType_shouldThrowBadRequest() {
        File file = File.reconstruct(
                1L,
                "qr.png",
                FileType.QRCODE,
                "url",
                FileStatus.ACTIVE,
                LocalDateTime.now());
        when(fileDomainService.getFileById(1L)).thenReturn(file);

        assertThrows(
                BadRequest.class,
                () -> appService.setEnrollForm(new EnrollFormCommands.SetEnrollFormCommand(1L)));
        verify(fileRepository, never()).deleteFileById(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("setEnrollForm: 文件未激活时应抛出 BadRequest 且不删旧表")
    void setEnrollForm_notActive_shouldThrowBadRequest() {
        File file = enrollFormFile(1L, FileStatus.PENDING, "enroll_form-uuid.pdf");
        when(fileDomainService.getFileById(1L)).thenReturn(file);

        assertThrows(
                BadRequest.class,
                () -> appService.setEnrollForm(new EnrollFormCommands.SetEnrollFormCommand(1L)));
        verify(fileRepository, never()).deleteFileById(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("setEnrollForm: 扩展名不合法时应抛出 BadRequest 且不删旧表")
    void setEnrollForm_invalidExtension_shouldThrowBadRequest() {
        File file = enrollFormFile(1L, FileStatus.ACTIVE, "enroll_form-uuid.exe");
        when(fileDomainService.getFileById(1L)).thenReturn(file);

        assertThrows(
                BadRequest.class,
                () -> appService.setEnrollForm(new EnrollFormCommands.SetEnrollFormCommand(1L)));
        verify(fileRepository, never()).deleteFileById(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("setEnrollForm: 首次设置且当前无报名表时不应删除任何文件")
    void setEnrollForm_firstSet_shouldNotDelete() {
        File file = enrollFormFile(1L, FileStatus.ACTIVE, "enroll_form-uuid.pdf");
        when(fileDomainService.getFileById(1L)).thenReturn(file);
        when(fileRepository.findLatestByTypeExcludingId(FileType.ENROLL_FORM, 1L)).thenReturn(Optional.empty());

        appService.setEnrollForm(new EnrollFormCommands.SetEnrollFormCommand(1L));

        verify(fileRepository, never()).deleteFileById(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("setEnrollForm: 替换报名表时应删除旧文件")
    void setEnrollForm_replace_shouldDeleteOldFile() {
        File newFile = enrollFormFile(2L, FileStatus.ACTIVE, "enroll_form-new.docx");
        File oldFile = enrollFormFile(1L, FileStatus.ACTIVE, "enroll_form-old.pdf");
        when(fileDomainService.getFileById(2L)).thenReturn(newFile);
        when(fileRepository.findLatestByTypeExcludingId(FileType.ENROLL_FORM, 2L))
                .thenReturn(Optional.of(oldFile));

        appService.setEnrollForm(new EnrollFormCommands.SetEnrollFormCommand(2L));

        verify(fileRepository).deleteFileById(1L);
    }

    @Test
    @DisplayName("setEnrollForm: 重复设置同一文件时应幂等且不删除")
    void setEnrollForm_sameFile_shouldBeIdempotent() {
        File file = enrollFormFile(1L, FileStatus.ACTIVE, "enroll_form-uuid.pdf");
        when(fileDomainService.getFileById(1L)).thenReturn(file);
        when(fileRepository.findLatestByTypeExcludingId(FileType.ENROLL_FORM, 1L)).thenReturn(Optional.empty());

        appService.setEnrollForm(new EnrollFormCommands.SetEnrollFormCommand(1L));

        verify(fileRepository, never()).deleteFileById(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("setEnrollForm: doc 与 docx 扩展名应被接受")
    void setEnrollForm_wordExtensions_shouldBeAccepted() {
        File docFile = enrollFormFile(1L, FileStatus.ACTIVE, "enroll_form-a.DOC");
        File docxFile = enrollFormFile(2L, FileStatus.ACTIVE, "enroll_form-b.Docx");
        when(fileDomainService.getFileById(1L)).thenReturn(docFile);
        when(fileDomainService.getFileById(2L)).thenReturn(docxFile);
        when(fileRepository.findLatestByTypeExcludingId(eq(FileType.ENROLL_FORM), anyLong()))
                .thenReturn(Optional.empty());

        appService.setEnrollForm(new EnrollFormCommands.SetEnrollFormCommand(1L));
        appService.setEnrollForm(new EnrollFormCommands.SetEnrollFormCommand(2L));
    }

    // ---------- deleteEnrollForm ----------

    @Test
    @DisplayName("deleteEnrollForm: 存在报名表时应删除")
    void deleteEnrollForm_existingForm_shouldDelete() {
        File file = enrollFormFile(1L, FileStatus.ACTIVE, "enroll_form-uuid.pdf");
        when(fileRepository.findLatestByType(FileType.ENROLL_FORM)).thenReturn(Optional.of(file));

        appService.deleteEnrollForm();

        verify(fileRepository).deleteFileById(1L);
    }

    @Test
    @DisplayName("deleteEnrollForm: 不存在报名表时应抛出 DataNotFound")
    void deleteEnrollForm_noForm_shouldThrowDataNotFound() {
        when(fileRepository.findLatestByType(FileType.ENROLL_FORM)).thenReturn(Optional.empty());

        assertThrows(DataNotFound.class, () -> appService.deleteEnrollForm());
        verify(fileRepository, never()).deleteFileById(org.mockito.ArgumentMatchers.anyLong());
    }
}
