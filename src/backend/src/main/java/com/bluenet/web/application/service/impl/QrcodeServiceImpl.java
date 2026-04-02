package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.file.FileInfo;
import com.bluenet.web.api.dto.qrcode.ConsultationQrcodeDTO;
import com.bluenet.web.application.service.FileService;
import com.bluenet.web.application.service.QrcodeService;
import com.bluenet.web.domain.model.entity.Qrcode;
import com.bluenet.web.domain.service.QrcodeDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final FileService fileService;

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
    public FileInfo uploadConsultationQrcode(MultipartFile file) {
        // 调用 FileService 的 uploadQrcode 方法，它会同时保存文件和二维码记录
        FileInfo fileInfo = fileService.uploadQrcode("CONSULTATION", file);
        log.info("咨询群二维码上传成功，fileId={}", fileInfo.getId());
        return fileInfo;
    }

    @Override
    @Transactional
    public void deleteConsultationQrcode(Long id) {
        qrcodeDomainService.deleteConsultationQrcode(id);
        log.info("咨询群二维码删除成功，id={}", id);
    }
}
