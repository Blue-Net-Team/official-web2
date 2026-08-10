package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.qrcode.QrcodeCommands;
import com.bluenet.web.application.result.qrcode.QrcodeResult;
import com.bluenet.web.application.service.QrcodeAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.Qrcode;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.QrcodeType;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.QrcodeRepository;
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
 * QrcodeAppServiceImpl 集成测试。
 *
 * <p>
 * 验证二维码应用服务中咨询群二维码与考核群二维码的创建、查询、更新、删除逻辑， 同时覆盖文件存在性校验、文件类型校验等分支。
 * </p>
 */
@DisplayName("QrcodeAppServiceImpl 集成测试")
class QrcodeAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private QrcodeAppService qrcodeAppService;

    @Autowired
    private QrcodeRepository qrcodeRepository;

    @Autowired
    private FileRepository fileRepository;

    @AfterEach
    void cleanupSecurityContext() {
        UserCTX.clear();
    }

    private File createQrcodeFile(String name) {
        return FileFixture.save(fileRepository, name, FileType.QRCODE);
    }

    private File createNormalImageFile(String name) {
        return FileFixture.save(fileRepository, name, FileType.NORMAL_IMG);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createConsultationQrcode: 使用 QRCODE 文件创建成功")
    void createConsultationQrcode_withQrcodeFile_shouldCreate() {
        File file = createQrcodeFile("consultation-qr");
        QrcodeCommands.CreateConsultationQrcodeCommand command = new QrcodeCommands.CreateConsultationQrcodeCommand(
                file.getId());

        qrcodeAppService.createConsultationQrcode(command);

        List<Qrcode> qrcodes = qrcodeRepository.findByType(QrcodeType.CONSULTATION);
        assertThat(qrcodes).hasSize(1);
        assertThat(qrcodes.get(0).getFileId()).isEqualTo(file.getId());
        assertThat(qrcodes.get(0).getType()).isEqualTo(QrcodeType.CONSULTATION);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createConsultationQrcode: 文件不存在应抛 DataNotFound")
    void createConsultationQrcode_withNonExistentFile_shouldThrowDataNotFound() {
        QrcodeCommands.CreateConsultationQrcodeCommand command = new QrcodeCommands.CreateConsultationQrcodeCommand(
                99999L);

        assertThatThrownBy(() -> qrcodeAppService.createConsultationQrcode(command))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("文件不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createConsultationQrcode: 文件类型不是 QRCODE 应抛 BadRequest")
    void createConsultationQrcode_withWrongFileType_shouldThrowBadRequest() {
        File file = createNormalImageFile("normal-img");
        QrcodeCommands.CreateConsultationQrcodeCommand command = new QrcodeCommands.CreateConsultationQrcodeCommand(
                file.getId());

        assertThatThrownBy(() -> qrcodeAppService.createConsultationQrcode(command))
                .isInstanceOf(BadRequest.class)
                .hasMessageContaining("文件类型不匹配");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getConsultationQrcodes: 应返回已创建的咨询群二维码列表")
    void getConsultationQrcodes_shouldReturnCreatedQrcodes() {
        File file1 = createQrcodeFile("consultation-qr-1");
        File file2 = createQrcodeFile("consultation-qr-2");
        qrcodeAppService.createConsultationQrcode(new QrcodeCommands.CreateConsultationQrcodeCommand(file1.getId()));
        qrcodeAppService.createConsultationQrcode(new QrcodeCommands.CreateConsultationQrcodeCommand(file2.getId()));

        List<QrcodeResult> result = qrcodeAppService.getConsultationQrcodes();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(QrcodeResult::fileId)
                .containsExactly(file1.getId(), file2.getId());
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateConsultationQrcode: 应成功更新关联文件")
    void updateConsultationQrcode_shouldUpdateFile() {
        File oldFile = createQrcodeFile("consultation-old");
        qrcodeAppService.createConsultationQrcode(new QrcodeCommands.CreateConsultationQrcodeCommand(oldFile.getId()));
        Qrcode created = qrcodeRepository.findByFileId(oldFile.getId()).orElseThrow();
        File newFile = createQrcodeFile("consultation-new");

        qrcodeAppService.updateConsultationQrcode(
                new QrcodeCommands.UpdateConsultationQrcodeCommand(created.getId(), newFile.getId()));

        Qrcode updated = qrcodeRepository.findById(created.getId()).orElseThrow();
        assertThat(updated.getFileId()).isEqualTo(newFile.getId());
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("deleteConsultationQrcode: 应成功删除二维码记录")
    void deleteConsultationQrcode_shouldDelete() {
        File file = createQrcodeFile("consultation-delete");
        qrcodeAppService.createConsultationQrcode(new QrcodeCommands.CreateConsultationQrcodeCommand(file.getId()));
        Qrcode created = qrcodeRepository.findByFileId(file.getId()).orElseThrow();

        qrcodeAppService.deleteConsultationQrcode(new QrcodeCommands.DeleteConsultationQrcodeCommand(created.getId()));

        assertThat(qrcodeRepository.findById(created.getId())).isEmpty();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createAssessmentQrcode: 使用 QRCODE 文件创建成功")
    void createAssessmentQrcode_withQrcodeFile_shouldCreate() {
        File file = createQrcodeFile("assessment-qr");
        QrcodeCommands.CreateAssessmentQrcodeCommand command = new QrcodeCommands.CreateAssessmentQrcodeCommand(
                file.getId(),
                Direction.COMPUTER_VISION.name(),
                1,
                false);

        qrcodeAppService.createAssessmentQrcode(command);

        List<Qrcode> qrcodes = qrcodeRepository.findAssessmentQrcodes(Direction.COMPUTER_VISION.name(), 1);
        assertThat(qrcodes).hasSize(1);
        assertThat(qrcodes.get(0).getFileId()).isEqualTo(file.getId());
        assertThat(qrcodes.get(0).getDirection()).isEqualTo(Direction.COMPUTER_VISION.name());
        assertThat(qrcodes.get(0).getEpoch()).isEqualTo(1);
        assertThat(qrcodes.get(0).getIsShared()).isFalse();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createAssessmentQrcode: 文件类型不是 QRCODE 应抛 BadRequest")
    void createAssessmentQrcode_withWrongFileType_shouldThrowBadRequest() {
        File file = createNormalImageFile("normal-img-assessment");
        QrcodeCommands.CreateAssessmentQrcodeCommand command = new QrcodeCommands.CreateAssessmentQrcodeCommand(
                file.getId(),
                Direction.STRUCTURAL_DESIGN.name(),
                1,
                false);

        assertThatThrownBy(() -> qrcodeAppService.createAssessmentQrcode(command))
                .isInstanceOf(BadRequest.class)
                .hasMessageContaining("文件类型不匹配");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getAssessmentQrcodes: 应按方向和轮次过滤考核群二维码")
    void getAssessmentQrcodes_shouldFilterByDirectionAndEpoch() {
        File cvEpoch1File = createQrcodeFile("cv-epoch1");
        File cvEpoch2File = createQrcodeFile("cv-epoch2");
        File sdEpoch1File = createQrcodeFile("sd-epoch1");
        File embEpoch1File = createQrcodeFile("emb-epoch1");
        qrcodeAppService.createAssessmentQrcode(
                new QrcodeCommands.CreateAssessmentQrcodeCommand(
                        cvEpoch1File.getId(), Direction.COMPUTER_VISION.name(), 1, false));
        qrcodeAppService.createAssessmentQrcode(
                new QrcodeCommands.CreateAssessmentQrcodeCommand(
                        cvEpoch2File.getId(), Direction.COMPUTER_VISION.name(), 2, false));
        qrcodeAppService.createAssessmentQrcode(
                new QrcodeCommands.CreateAssessmentQrcodeCommand(
                        sdEpoch1File.getId(), Direction.STRUCTURAL_DESIGN.name(), 1, false));
        qrcodeAppService.createAssessmentQrcode(
                new QrcodeCommands.CreateAssessmentQrcodeCommand(
                        embEpoch1File.getId(), Direction.EMBEDDED.name(), 1, false));

        List<QrcodeResult> cvEpoch1 = qrcodeAppService.getAssessmentQrcodes(Direction.COMPUTER_VISION.name(), 1);
        assertThat(cvEpoch1).hasSize(1);
        assertThat(cvEpoch1.get(0).fileId()).isEqualTo(cvEpoch1File.getId());

        List<QrcodeResult> cvAll = qrcodeAppService.getAssessmentQrcodes(Direction.COMPUTER_VISION.name(), null);
        assertThat(cvAll).hasSize(2);
        assertThat(cvAll)
                .extracting(QrcodeResult::fileId)
                .containsOnly(cvEpoch1File.getId(), cvEpoch2File.getId());

        List<QrcodeResult> epoch1All = qrcodeAppService.getAssessmentQrcodes(null, 1);
        assertThat(epoch1All).hasSize(3);
        assertThat(epoch1All)
                .extracting(QrcodeResult::fileId)
                .containsOnly(cvEpoch1File.getId(), sdEpoch1File.getId(), embEpoch1File.getId());
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateAssessmentQrcode: 应成功更新文件、方向、轮次和共用标志")
    void updateAssessmentQrcode_withFile_shouldUpdate() {
        File oldFile = createQrcodeFile("assessment-old");
        qrcodeAppService.createAssessmentQrcode(
                new QrcodeCommands.CreateAssessmentQrcodeCommand(
                        oldFile.getId(), Direction.COMPUTER_VISION.name(), 1, false));
        Qrcode created = qrcodeRepository.findAssessmentQrcodes(Direction.COMPUTER_VISION.name(), 1).get(0);
        File newFile = createQrcodeFile("assessment-new");

        qrcodeAppService.updateAssessmentQrcode(
                new QrcodeCommands.UpdateAssessmentQrcodeCommand(
                        created.getId(),
                        newFile.getId(),
                        Direction.STRUCTURAL_DESIGN.name(),
                        2,
                        false));

        Qrcode updated = qrcodeRepository.findById(created.getId()).orElseThrow();
        assertThat(updated.getFileId()).isEqualTo(newFile.getId());
        assertThat(updated.getDirection()).isEqualTo(Direction.STRUCTURAL_DESIGN.name());
        assertThat(updated.getEpoch()).isEqualTo(2);
        assertThat(updated.getIsShared()).isFalse();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateAssessmentQrcode: 不指定 fileId 时应成功更新其他字段")
    void updateAssessmentQrcode_withoutFile_shouldUpdate() {
        File file = createQrcodeFile("assessment-no-file");
        qrcodeAppService.createAssessmentQrcode(
                new QrcodeCommands.CreateAssessmentQrcodeCommand(
                        file.getId(), Direction.COMPUTER_VISION.name(), 1, false));
        Qrcode created = qrcodeRepository.findAssessmentQrcodes(Direction.COMPUTER_VISION.name(), 1).get(0);

        qrcodeAppService.updateAssessmentQrcode(
                new QrcodeCommands.UpdateAssessmentQrcodeCommand(
                        created.getId(),
                        null,
                        Direction.EMBEDDED.name(),
                        3,
                        false));

        Qrcode updated = qrcodeRepository.findById(created.getId()).orElseThrow();
        assertThat(updated.getFileId()).isEqualTo(file.getId());
        assertThat(updated.getDirection()).isEqualTo(Direction.EMBEDDED.name());
        assertThat(updated.getEpoch()).isEqualTo(3);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("deleteAssessmentQrcode: 应成功删除考核群二维码记录")
    void deleteAssessmentQrcode_shouldDelete() {
        File file = createQrcodeFile("assessment-delete");
        qrcodeAppService.createAssessmentQrcode(
                new QrcodeCommands.CreateAssessmentQrcodeCommand(
                        file.getId(), Direction.COMPUTER_VISION.name(), 1, false));
        Qrcode created = qrcodeRepository.findAssessmentQrcodes(Direction.COMPUTER_VISION.name(), 1).get(0);

        qrcodeAppService.deleteAssessmentQrcode(new QrcodeCommands.DeleteAssessmentQrcodeCommand(created.getId()));

        assertThat(qrcodeRepository.findById(created.getId())).isEmpty();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateAssessmentQrcode: 记录不存在应抛 DataNotFound")
    void updateAssessmentQrcode_withNonExistentId_shouldThrowDataNotFound() {
        QrcodeCommands.UpdateAssessmentQrcodeCommand command = new QrcodeCommands.UpdateAssessmentQrcodeCommand(
                99999L,
                null,
                null,
                null,
                null);

        assertThatThrownBy(() -> qrcodeAppService.updateAssessmentQrcode(command))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("二维码不存在");
    }
}
