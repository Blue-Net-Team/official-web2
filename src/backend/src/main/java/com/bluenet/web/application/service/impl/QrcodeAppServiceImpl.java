package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.QrcodeResult;
import com.bluenet.web.application.command.qrcode.QrcodeCommands;
import com.bluenet.web.application.service.QrcodeAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Qrcode;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.domain.service.QrcodeDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 二维码应用服务实现。
 * <p>
 * 实现二维码聚合在应用层的业务逻辑编排。
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QrcodeAppServiceImpl implements QrcodeAppService {

    private final QrcodeDomainService qrcodeDomainService;
    private final FileDomainService fileDomainService;

    /**
     * 查询咨询二维码列表。
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
     * 创建咨询二维码。
     *
     * @param command
     *            创建咨询二维码命令
     */
    @Override
    @Transactional
    public void createConsultationQrcode(QrcodeCommands.CreateConsultationQrcodeCommand command) {
        FileVO fileVO = fileDomainService.getFileById(command.fileId());
        if (fileVO == null) {
            throw new DataNotFound("文件不存在");
        }
        if (fileVO.getType() != FileType.QRCODE) {
            throw new BadRequest("文件类型不匹配，期望 QRCODE");
        }

        qrcodeDomainService.saveQrcode(fileVO, com.bluenet.web.domain.model.enumerate.QrcodeType.CONSULTATION);
        log.info("二维码创建成功，fileId={}", command.fileId());
    }

    /**
     * 删除咨询二维码。
     *
     * @param command
     *            删除咨询二维码命令
     */
    @Override
    @Transactional
    public void deleteConsultationQrcode(QrcodeCommands.DeleteConsultationQrcodeCommand command) {
        qrcodeDomainService.deleteConsultationQrcode(command.id());
        log.info("咨询群二维码删除成功，id={}", command.id());
    }
}
