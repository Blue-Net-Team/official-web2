package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.qrcode.ConsultationQrcodeDTO;
import com.bluenet.web.application.service.QrcodeService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Qrcode;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.QrcodeType;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.domain.service.QrcodeDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 二维码服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QrcodeServiceImpl implements QrcodeService {

    private final QrcodeDomainService qrcodeDomainService;
    private final FileDomainService fileDomainService;

    @Override
    public List<ConsultationQrcodeDTO> getConsultationQrcodes() {
        List<Qrcode> qrcodes = qrcodeDomainService.getConsultationQrcodes();
        return qrcodes.stream()
                .map(
                        qrcode -> ConsultationQrcodeDTO.builder()
                                .id(qrcode.getId())
                                .fileId(qrcode.getFileId())
                                .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createQrcode(Long fileId) {
        FileVO fileVO = fileDomainService.getFileById(fileId);
        if (fileVO == null) {
            throw new DataNotFound("文件不存在");
        }
        if (fileVO.getType() != FileType.QRCODE) {
            throw new BadRequest("文件类型不匹配，期望 QRCODE");
        }

        qrcodeDomainService.saveQrcode(fileVO, QrcodeType.CONSULTATION);
        log.info("二维码创建成功，fileId={}", fileId);
    }

    @Override
    @Transactional
    public void deleteConsultationQrcode(Long id) {
        qrcodeDomainService.deleteConsultationQrcode(id);
        log.info("咨询群二维码删除成功，id={}", id);
    }
}
