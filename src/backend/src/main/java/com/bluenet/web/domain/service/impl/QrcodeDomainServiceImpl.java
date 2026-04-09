package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Qrcode;
import com.bluenet.web.domain.model.enumerate.QrcodeType;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.QrcodeVO;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.QrcodeRepository;
import com.bluenet.web.domain.service.QrcodeDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 二维码领域服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "minio.enabled", havingValue = "true")
public class QrcodeDomainServiceImpl implements QrcodeDomainService {

    private final QrcodeRepository qrcodeRepository;
    private final FileRepository fileRepository;

    @Override
    public void saveQrcode(FileVO fileVO, QrcodeType type) {
        if (type == null) {
            throw new IllegalArgumentException("二维码类型不能为空");
        }
        QrcodeVO qrcodeVO = QrcodeVO.builder()
                .fileId(fileVO.getId())
                .type(type)
                .build();
        qrcodeRepository.save(qrcodeVO);
        log.info("二维码保存成功，fileId={}, type={}", fileVO.getId(), type);
    }

    @Override
    public List<Qrcode> getConsultationQrcodes() {
        return qrcodeRepository.findByType(QrcodeType.CONSULTATION);
    }

    @Override
    @Transactional
    public void deleteConsultationQrcode(Long id) {
        // 1. 获取二维码记录
        Qrcode qrcode = qrcodeRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("二维码不存在: " + id));

        // 2. 验证类型
        if (qrcode.getType() != QrcodeType.CONSULTATION) {
            throw new IllegalArgumentException("只能删除咨询群二维码");
        }

        // 3. 保存文件ID用于删除
        Long fileId = qrcode.getFileId();

        // 4. 删除二维码记录
        qrcodeRepository.deleteById(id);

        // 5. 删除关联文件（包括数据库记录和 MinIO 对象）
        if (fileId != null) {
            try {
                fileRepository.deleteFileById(fileId);
                log.info("删除咨询群二维码成功，id={}, fileId={}", id, fileId);
            } catch (Exception e) {
                log.warn("删除关联文件失败: fileId={}, error={}", fileId, e.getMessage());
                // 不抛出异常，因为二维码记录已成功删除
            }
        }
    }
}
