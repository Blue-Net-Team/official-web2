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

    /**
     * 检查考核群二维码 epoch/direction 组合是否冲突。
     *
     * @param epoch
     *            考核轮次
     * @param direction
     *            方向
     * @param isShared
     *            是否共用
     * @param excludeId
     *            排除的记录 ID（更新时排除自身）
     */
    private void checkAssessmentConflict(Integer epoch, String direction, Boolean isShared, Long excludeId) {
        if (epoch == null) {
            return;
        }
        List<Qrcode> existing = qrcodeRepository.findAssessmentByEpoch(epoch);
        for (Qrcode qr : existing) {
            if (excludeId != null && excludeId.equals(qr.getId())) {
                continue;
            }
            if (Boolean.TRUE.equals(isShared) || Boolean.TRUE.equals(qr.getIsShared())) {
                throw new IllegalArgumentException("该轮次已存在二维码");
            }
            if (direction != null && direction.equals(qr.getDirection())) {
                throw new IllegalArgumentException("该轮次和方向组合已存在二维码");
            }
        }
    }

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
                log.info(
                        "更新咨询群二维码成功，id={}, oldFileId={}, newFileId={}",
                        id,
                        oldFileId,
                        fileVO.getId());
            } catch (Exception e) {
                log.warn(
                        "删除旧关联文件失败: oldFileId={}, error={}",
                        oldFileId,
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
        // 校验 epoch 为正整数
        if (qrcode.getEpoch() != null && qrcode.getEpoch() <= 0) {
            throw new IllegalArgumentException("考核轮次必须为正整数");
        }
        // 检查重复
        checkAssessmentConflict(qrcode.getEpoch(), qrcode.getDirection(), qrcode.getIsShared(), null);

        qrcodeRepository.save(qrcode);
        log.info(
                "考核群二维码保存成功，id={}, fileId={}",
                qrcode.getId(),
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

        // 3. epoch 校验
        if (epoch != null && epoch <= 0) {
            throw new IllegalArgumentException("考核轮次必须为正整数");
        }

        // 4. 保存旧的文件ID用于删除
        Long oldFileId = qrcode.getFileId();

        // 5. 确定用于重复检查的新值
        Integer checkEpoch = epoch != null ? epoch : qrcode.getEpoch();
        String checkDirection = direction != null ? direction : qrcode.getDirection();
        Boolean checkIsShared = isShared != null ? isShared : qrcode.getIsShared();
        checkAssessmentConflict(checkEpoch, checkDirection, checkIsShared, id);

        // 6. 更新二维码记录
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

        // 7. 删除旧的关联文件
        if (fileVO != null && oldFileId != null && !oldFileId.equals(fileVO.getId())) {
            try {
                fileRepository.deleteFileById(oldFileId);
                log.info(
                        "更新考核群二维码成功，id={}, oldFileId={}, newFileId={}",
                        id,
                        oldFileId,
                        fileVO.getId());
            } catch (Exception e) {
                log.warn(
                        "删除旧关联文件失败: oldFileId={}, error={}",
                        oldFileId,
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
