package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.result.qrcode.QrcodeResult;
import com.bluenet.web.application.command.qrcode.QrcodeCommands;
import com.bluenet.web.application.service.QrcodeAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.Qrcode;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.domain.service.QrcodeDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 二维码应用服务实现
 * <p>
 * 实现二维码聚合在应用层的业务逻辑编排
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QrcodeAppServiceImpl implements QrcodeAppService {

    private final QrcodeDomainService qrcodeDomainService;
    private final FileDomainService fileDomainService;

    /**
     * 查询咨询群二维码列表
     *
     * @return 二维码结果列表
     */
    @Override
    public List<QrcodeResult> getConsultationQrcodes() {
        List<Qrcode> qrcodes = qrcodeDomainService.getConsultationQrcodes();
        return qrcodes.stream()
                .map(qrcode -> new QrcodeResult(qrcode.getId(), qrcode.getFileId()))
                .toList();
    }

    /**
     * 创建咨询群二维码
     *
     * @param command
     *            创建咨询群二维码命令
     */
    @Override
    @Transactional
    public void createConsultationQrcode(QrcodeCommands.CreateConsultationQrcodeCommand command) {
        File file = fileDomainService.getFileById(command.fileId());
        if (file == null) {
            throw new DataNotFound("文件不存在");
        }
        if (file.getType() != FileType.QRCODE) {
            throw new BadRequest("文件类型不匹配，期望 QRCODE");
        }

        qrcodeDomainService.saveQrcode(file, com.bluenet.web.domain.model.enumerate.QrcodeType.CONSULTATION);
        log.info("二维码创建成功，fileId={}", command.fileId());
    }

    /**
     * 更新咨询群二维码
     *
     * @param command
     *            更新咨询群二维码命令
     */
    @Override
    @Transactional
    public void updateConsultationQrcode(QrcodeCommands.UpdateConsultationQrcodeCommand command) {
        File file = fileDomainService.getFileById(command.fileId());
        if (file == null) {
            throw new DataNotFound("文件不存在");
        }
        if (file.getType() != FileType.QRCODE) {
            throw new BadRequest("文件类型不匹配，期望 QRCODE");
        }

        qrcodeDomainService.updateConsultationQrcode(command.id(), file);
        log.info("二维码更新成功，id={}, fileId={}", command.id(), command.fileId());
    }

    /**
     * 删除咨询群二维码
     *
     * @param command
     *            删除咨询群二维码命令
     */
    @Override
    @Transactional
    public void deleteConsultationQrcode(QrcodeCommands.DeleteConsultationQrcodeCommand command) {
        qrcodeDomainService.deleteConsultationQrcode(command.id());
        log.info("咨询群二维码删除成功，id={}", command.id());
    }

    /**
     * 查询考核群二维码列表
     *
     * @param direction
     *            方向
     * @param epoch
     *            考核轮次
     * @return 二维码结果列表
     */
    @Override
    public List<QrcodeResult> getAssessmentQrcodes(String direction, Integer epoch) {
        List<Qrcode> qrcodes = qrcodeDomainService.getAssessmentQrcodes(direction, epoch);
        return qrcodes.stream()
                .map(
                        qrcode -> new QrcodeResult(qrcode.getId(), qrcode.getFileId(),
                                qrcode.getDirection(), qrcode.getEpoch(), qrcode.getIsShared()))
                .toList();
    }

    /**
     * 创建考核群二维码
     *
     * @param command
     *            创建考核群二维码命令
     */
    @Override
    @Transactional
    public void createAssessmentQrcode(QrcodeCommands.CreateAssessmentQrcodeCommand command) {
        File file = fileDomainService.getFileById(command.fileId());
        if (file == null) {
            throw new DataNotFound("文件不存在");
        }
        if (file.getType() != FileType.QRCODE) {
            throw new BadRequest("文件类型不匹配，期望 QRCODE");
        }

        // 构建二维码实体
        Qrcode qrcode = Qrcode.forAssessment(
                command.fileId(),
                command.epoch(),
                command.direction(),
                command.isShared());
        qrcodeDomainService.saveAssessmentQrcode(qrcode);
        log.info("考核群二维码创建成功，id={}, fileId={}", qrcode.getId(), command.fileId());
    }

    /**
     * 更新考核群二维码
     *
     * @param command
     *            更新考核群二维码命令
     */
    @Override
    @Transactional
    public void updateAssessmentQrcode(QrcodeCommands.UpdateAssessmentQrcodeCommand command) {
        File file = null;
        if (command.fileId() != null) {
            file = fileDomainService.getFileById(command.fileId());
            if (file == null) {
                throw new DataNotFound("文件不存在");
            }
            if (file.getType() != FileType.QRCODE) {
                throw new BadRequest("文件类型不匹配，期望 QRCODE");
            }
        }

        qrcodeDomainService.updateAssessmentQrcode(
                command.id(),
                file,
                command.direction(),
                command.epoch(),
                command.isShared());
        log.info("考核群二维码更新成功，id={}", command.id());
    }

    /**
     * 删除考核群二维码
     *
     * @param command
     *            删除考核群二维码命令
     */
    @Override
    @Transactional
    public void deleteAssessmentQrcode(QrcodeCommands.DeleteAssessmentQrcodeCommand command) {
        qrcodeDomainService.deleteAssessmentQrcode(command.id());
        log.info("考核群二维码删除成功，id={}", command.id());
    }
}
