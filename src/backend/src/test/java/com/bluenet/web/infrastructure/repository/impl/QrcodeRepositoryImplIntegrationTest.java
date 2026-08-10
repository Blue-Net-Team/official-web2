package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.Qrcode;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.QrcodeType;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.QrcodeRepository;
import com.bluenet.web.infrastructure.repository.dataobject.QrcodeDO;
import com.bluenet.web.infrastructure.repository.mapper.QrcodeMapper;
import com.bluenet.web.testsupport.fixture.FileFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * QrcodeRepositoryImpl 集成测试。
 */
@DisplayName("QrcodeRepositoryImpl 集成测试")
class QrcodeRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private QrcodeRepository qrcodeRepository;

    @Autowired
    private QrcodeMapper qrcodeMapper;

    @Autowired
    private FileRepository fileRepository;

    private final AtomicLong counter = new AtomicLong(1);

    private File createFile() {
        String name = "qrcode-" + counter.getAndIncrement() + ".png";
        return FileFixture.save(fileRepository, name, FileType.QRCODE);
    }

    private Qrcode createQrcode(QrcodeType type) {
        File file = createFile();
        Qrcode qrcode = Qrcode.create(file.getId(), type);
        qrcodeRepository.save(qrcode);
        return qrcode;
    }

    private Qrcode createAssessmentQrcode(Integer epoch, String direction, Boolean isShared) {
        File file = createFile();
        Qrcode qrcode = Qrcode.forAssessment(file.getId(), epoch, direction, isShared);
        qrcodeRepository.save(qrcode);
        return qrcode;
    }

    @Test
    @DisplayName("save: 新二维码应插入并回写ID")
    void save_newQrcode_shouldInsertAndReturnId() {
        Qrcode qrcode = createQrcode(QrcodeType.USER);

        assertThat(qrcode.getId()).isNotNull();
        QrcodeDO dataObject = qrcodeMapper.selectById(qrcode.getId());
        assertThat(dataObject).isNotNull();
        assertThat(dataObject.getType()).isEqualTo(QrcodeType.USER);
    }

    @Test
    @DisplayName("save: 已有二维码应更新字段")
    void save_existingQrcode_shouldUpdateFields() {
        Qrcode qrcode = createQrcode(QrcodeType.CONSULTATION);
        File newFile = createFile();
        qrcode = Qrcode.reconstruct(
                qrcode.getId(),
                newFile.getId(),
                QrcodeType.ASSESSMENT,
                1,
                Direction.COMPUTER_VISION.getValue(),
                false);

        qrcodeRepository.save(qrcode);

        QrcodeDO updated = qrcodeMapper.selectById(qrcode.getId());
        assertThat(updated.getType()).isEqualTo(QrcodeType.ASSESSMENT);
        assertThat(updated.getFileId()).isEqualTo(newFile.getId());
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        Qrcode qrcode = createQrcode(QrcodeType.USER);

        Optional<Qrcode> found = qrcodeRepository.findById(qrcode.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo(QrcodeType.USER);

        assertThat(qrcodeRepository.findById(-1L)).isEmpty();
    }

    @Test
    @DisplayName("findByFileId: 应按文件ID查询")
    void findByFileId_shouldReturnQrcode() {
        File file = createFile();
        Qrcode qrcode = Qrcode.create(file.getId(), QrcodeType.CONSULTATION);
        qrcodeRepository.save(qrcode);

        Optional<Qrcode> found = qrcodeRepository.findByFileId(file.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(qrcode.getId());

        assertThat(qrcodeRepository.findByFileId(-1L)).isEmpty();
    }

    @Test
    @DisplayName("findByType: 应按类型查询二维码")
    void findByType_shouldReturnQrcodes() {
        Qrcode userQrcode = createQrcode(QrcodeType.USER);
        createQrcode(QrcodeType.CONSULTATION);
        createQrcode(QrcodeType.CONSULTATION);

        List<Qrcode> userQrcodes = qrcodeRepository.findByType(QrcodeType.USER);
        List<Qrcode> consultationQrcodes = qrcodeRepository.findByType(QrcodeType.CONSULTATION);

        assertThat(userQrcodes).hasSize(1);
        assertThat(userQrcodes.get(0).getId()).isEqualTo(userQrcode.getId());
        assertThat(consultationQrcodes).hasSize(2);
    }

    @Test
    @DisplayName("findAssessmentQrcodes: 应支持按方向和轮次筛选考核群二维码")
    void findAssessmentQrcodes_shouldFilter() {
        Qrcode cvQrcode = createAssessmentQrcode(1, Direction.COMPUTER_VISION.getValue(), false);
        createAssessmentQrcode(1, Direction.EMBEDDED.getValue(), false);
        createAssessmentQrcode(2, Direction.COMPUTER_VISION.getValue(), false);

        List<Qrcode> epoch1Cv = qrcodeRepository.findAssessmentQrcodes(Direction.COMPUTER_VISION.getValue(), 1);
        List<Qrcode> allEpoch1 = qrcodeRepository.findAssessmentQrcodes(null, 1);

        assertThat(epoch1Cv).hasSize(1);
        assertThat(epoch1Cv.get(0).getId()).isEqualTo(cvQrcode.getId());
        assertThat(allEpoch1).hasSize(2);
    }

    @Test
    @DisplayName("findAssessmentByEpoch: 应按考核轮次查询")
    void findAssessmentByEpoch_shouldReturnByEpoch() {
        Qrcode epoch1 = createAssessmentQrcode(1, Direction.COMPUTER_VISION.getValue(), false);
        createAssessmentQrcode(2, Direction.EMBEDDED.getValue(), false);

        List<Qrcode> results = qrcodeRepository.findAssessmentByEpoch(1);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(epoch1.getId());
    }

    @Test
    @DisplayName("deleteById: 应删除二维码")
    void deleteById_shouldRemoveQrcode() {
        Qrcode qrcode = createQrcode(QrcodeType.USER);
        Long qrcodeId = qrcode.getId();

        qrcodeRepository.deleteById(qrcodeId);

        assertThat(qrcodeMapper.selectById(qrcodeId)).isNull();
    }
}
