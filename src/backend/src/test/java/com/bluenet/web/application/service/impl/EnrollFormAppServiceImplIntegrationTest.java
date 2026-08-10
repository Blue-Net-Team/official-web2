package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.enrollform.EnrollFormCommands;
import com.bluenet.web.application.result.enrollform.EnrollFormResult;
import com.bluenet.web.application.service.EnrollFormAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.testsupport.fixture.FileFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * EnrollFormAppServiceImpl 集成测试。
 * <p>
 * 验证报名表的查询、设置/替换与删除完整流程，以及各校验分支。
 * </p>
 */
@DisplayName("EnrollFormAppServiceImpl 集成测试")
class EnrollFormAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private EnrollFormAppService enrollFormAppService;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileMapper fileMapper;

    /**
     * 创建并保存一条 ACTIVE 状态的报名表文件元数据。
     */
    private File createActiveEnrollFormFile(String name) {
        File file = FileFixture.save(fileRepository, name, FileType.ENROLL_FORM);
        return fileRepository.save(FileFixture.withStatus(file, FileStatus.ACTIVE));
    }

    @Test
    @DisplayName("getCurrentEnrollForm: 无报名表时应返回空")
    void getCurrentEnrollForm_noForm_shouldReturnEmpty() {
        Optional<EnrollFormResult> result = enrollFormAppService.getCurrentEnrollForm();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("setEnrollForm: 首次设置后公开查询应返回该文件")
    void setEnrollForm_firstSet_shouldBeQueryable() {
        File file = createActiveEnrollFormFile("enroll_form-first.pdf");

        enrollFormAppService.setEnrollForm(new EnrollFormCommands.SetEnrollFormCommand(file.getId()));

        Optional<EnrollFormResult> result = enrollFormAppService.getCurrentEnrollForm();
        assertThat(result).isPresent();
        assertThat(result.get().fileId()).isEqualTo(file.getId());
        assertThat(result.get().createdAt()).isNotNull();
    }

    @Test
    @DisplayName("setEnrollForm: 替换后旧文件记录应被删除，新文件成为当前报名表")
    void setEnrollForm_replace_shouldDeleteOldAndSetNew() {
        File oldFile = createActiveEnrollFormFile("enroll_form-old.pdf");
        File newFile = createActiveEnrollFormFile("enroll_form-new.docx");

        enrollFormAppService.setEnrollForm(new EnrollFormCommands.SetEnrollFormCommand(newFile.getId()));

        Optional<EnrollFormResult> result = enrollFormAppService.getCurrentEnrollForm();
        assertThat(result).isPresent();
        assertThat(result.get().fileId()).isEqualTo(newFile.getId());
        assertThat(fileMapper.selectById(oldFile.getId())).isNull();
        assertThat(fileMapper.selectById(newFile.getId())).isNotNull();
    }

    @Test
    @DisplayName("setEnrollForm: 重复设置同一文件应幂等且文件保留")
    void setEnrollForm_sameFile_shouldBeIdempotent() {
        File file = createActiveEnrollFormFile("enroll_form-same.pdf");

        enrollFormAppService.setEnrollForm(new EnrollFormCommands.SetEnrollFormCommand(file.getId()));
        enrollFormAppService.setEnrollForm(new EnrollFormCommands.SetEnrollFormCommand(file.getId()));

        assertThat(fileMapper.selectById(file.getId())).isNotNull();
        assertThat(enrollFormAppService.getCurrentEnrollForm()).isPresent();
    }

    @Test
    @DisplayName("setEnrollForm: 文件不存在应抛 DataNotFound")
    void setEnrollForm_fileNotFound_shouldThrowDataNotFound() {
        assertThatThrownBy(
                () -> enrollFormAppService.setEnrollForm(new EnrollFormCommands.SetEnrollFormCommand(99999L)))
                        .isInstanceOf(DataNotFound.class);
    }

    @Test
    @DisplayName("setEnrollForm: 文件类型不匹配应抛 BadRequest 且旧表保留")
    void setEnrollForm_wrongType_shouldThrowBadRequest() {
        File oldFile = createActiveEnrollFormFile("enroll_form-old.pdf");
        File wrongType = FileFixture.save(fileRepository, "qr-code.png", FileType.QRCODE);
        fileRepository.save(FileFixture.withStatus(wrongType, FileStatus.ACTIVE));

        assertThatThrownBy(
                () -> enrollFormAppService.setEnrollForm(
                        new EnrollFormCommands.SetEnrollFormCommand(wrongType.getId())))
                                .isInstanceOf(BadRequest.class)
                                .hasMessageContaining("文件类型不匹配");
        assertThat(fileMapper.selectById(oldFile.getId())).isNotNull();
    }

    @Test
    @DisplayName("setEnrollForm: 文件未激活应抛 BadRequest 且旧表保留")
    void setEnrollForm_notActive_shouldThrowBadRequest() {
        File oldFile = createActiveEnrollFormFile("enroll_form-old.pdf");
        File pending = FileFixture.save(fileRepository, "enroll_form-pending.pdf", FileType.ENROLL_FORM);

        assertThatThrownBy(
                () -> enrollFormAppService.setEnrollForm(
                        new EnrollFormCommands.SetEnrollFormCommand(pending.getId())))
                                .isInstanceOf(BadRequest.class)
                                .hasMessageContaining("上传确认");
        assertThat(fileMapper.selectById(oldFile.getId())).isNotNull();
    }

    @Test
    @DisplayName("setEnrollForm: 扩展名不合法应抛 BadRequest 且旧表保留")
    void setEnrollForm_invalidExtension_shouldThrowBadRequest() {
        File oldFile = createActiveEnrollFormFile("enroll_form-old.pdf");
        File exe = createActiveEnrollFormFile("enroll_form-evil.exe");

        assertThatThrownBy(
                () -> enrollFormAppService.setEnrollForm(new EnrollFormCommands.SetEnrollFormCommand(exe.getId())))
                        .isInstanceOf(BadRequest.class)
                        .hasMessageContaining("pdf/doc/docx");
        assertThat(fileMapper.selectById(oldFile.getId())).isNotNull();
        assertThat(fileMapper.selectById(exe.getId())).isNotNull();
    }

    @Test
    @DisplayName("deleteEnrollForm: 删除后公开查询应为空")
    void deleteEnrollForm_existingForm_shouldRemove() {
        File file = createActiveEnrollFormFile("enroll_form-del.pdf");

        enrollFormAppService.deleteEnrollForm();

        assertThat(fileMapper.selectById(file.getId())).isNull();
        assertThat(enrollFormAppService.getCurrentEnrollForm()).isEmpty();
    }

    @Test
    @DisplayName("deleteEnrollForm: 无报名表时应抛 DataNotFound")
    void deleteEnrollForm_noForm_shouldThrowDataNotFound() {
        assertThatThrownBy(() -> enrollFormAppService.deleteEnrollForm())
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("报名表");
    }
}
