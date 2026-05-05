package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Qrcode;
import com.bluenet.web.domain.model.enumerate.QrcodeType;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.QrcodeRepository;
import com.bluenet.web.domain.service.QrcodeDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 二维码领域服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QrcodeDomainServiceImpl implements QrcodeDomainService {

    private final QrcodeRepository qrcodeRepository;
    private final FileRepository fileRepository;

    @Override
    public void saveQrcode(FileVO fileVO, QrcodeType type) {
        if (type == null) {
            throw new IllegalArgumentException("二维码类型不能为空");
        }
        Qrcode qrcode = Qrcode.builder()
                .fileId(fileVO.getId())
                .type(type)
                .build();
        qrcodeRepository.save(qrcode);
        log.info("二维码保存成功，fileId={}, type={}", fileVO.getId(), type);
    }

    @Override
    public List<Qrcode> getConsultationQrcodes() {
        return qrcodeRepository.findByType(QrcodeType.CONSULTATION);
    }

    @Override
    @Transactional
    public void updateConsultationQrcode(Long id, FileVO fileVO) {
        // 1. 获取二维码记录
        Qrcode qrcode = qrcodeRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("二维码不存在: " + id));

        // 2. 验证类型
        if (qrcode.getType() != QrcodeType.CONSULTATION) {
            throw new IllegalArgumentException("只能更新咨询群二维码");
        }

        // 3. 保存旧的文件ID用于删除
        Long oldFileId = qrcode.getFileId();

        // 4. 更新二维码记录
        qrcode.setFileId(fileVO.getId());
        qrcodeRepository.save(qrcode);

        // 5. 删除旧的关联文件
        if (oldFileId != null && !oldFileId.equals(fileVO.getId())) {
            try {
                fileRepository.deleteFileById(oldFileId);
                log.info("更新咨询群二维码成功，id={}, oldFileId={}, newFileId={}", id,
                        oldFileId, fileVO.getId());
            } catch (Exception e) {
                log.warn("删除旧关联文件失败: oldFileId={}, error={}", oldFileId,
                        e.getMessage());
                // 不抛出异常，因为二维码记录已成功更新
            }
        }
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

    @Override
    public List<Qrcode> getAssessmentQrcodes(String direction, Integer epoch) {
        return qrcodeRepository.findAssessmentQrcodes(direction, epoch);
    }

    @Override
    @Transactional
    public void saveAssessmentQrcode(Qrcode qrcode) {
        qrcodeRepository.save(qrcode);
        log.info("考核群二维码保存成功，id={}, fileId={}", qrcode.getId(),
                qrcode.getFileId());
    }

    @Override
    @Transactional
    public void updateAssessmentQrcode(Long id, FileVO fileVO, String direction,
            Integer epoch, Boolean isShared) {
        // 1. 获取二维码记录
        Qrcode qrcode = qrcodeRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("二维码不存在: " + id));

        // 2. 验证类型
        if (qrcode.getType() != QrcodeType.ASSESSMENT) {
            throw new IllegalArgumentException("只能更新考核群二维码");
        }

        // 3. 保存旧的文件ID用于删除
        Long oldFileId = qrcode.getFileId();

        // 4. 更新二维码记录
        if (fileVO != null) {
            qrcode.setFileId(fileVO.getId());
        }
        if (direction != null) {
            qrcode.setDirection(direction);
        }
        if (epoch != null) {
            qrcode.setEpoch(epoch);
        }
        if (isShared != null) {
            qrcode.setIsShared(isShared);
            // 如果设为共用，清空方向
            if (isShared) {
                qrcode.setDirection(null);
            }
        }

        // 再次验证领域规则
        if (qrcode.getIsShared() != null && qrcode.getIsShared()) {
            if (qrcode.getDirection() != null) {
                throw new IllegalArgumentException("共用二维码时方向必须为空");
            }
        } else {
            if (qrcode.getDirection() == null) {
                throw new IllegalArgumentException("非共用二维码时方向不能为空");
            }
        }

        qrcodeRepository.save(qrcode);

        // 5. 删除旧的关联文件
        if (fileVO != null && oldFileId != null && !oldFileId.equals(fileVO.getId())) {
            try {
                fileRepository.deleteFileById(oldFileId);
                log.info("更新考核群二维码成功，id={}, oldFileId={}, newFileId={}", id,
                        oldFileId, fileVO.getId());
            } catch (Exception e) {
                log.warn("删除旧关联文件失败: oldFileId={}, error={}", oldFileId,
                        e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void deleteAssessmentQrcode(Long id) {
        // 1. 获取二维码记录
        Qrcode qrcode = qrcodeRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("二维码不存在: " + id));

        // 2. 验证类型
        if (qrcode.getType() != QrcodeType.ASSESSMENT) {
            throw new IllegalArgumentException("只能删除考核群二维码");
        }

        // 3. 保存文件ID用于删除
        Long fileId = qrcode.getFileId();

        // 4. 删除二维码记录
        qrcodeRepository.deleteById(id);

        // 5. 删除关联文件
        if (fileId != null) {
            try {
                fileRepository.deleteFileById(fileId);
                log.info("删除考核群二维码成功，id={}, fileId={}", id, fileId);
            } catch (Exception e) {
                log.warn("删除关联文件失败: fileId={}, error={}", fileId, e.getMessage());
            }
        }
    }
}
