package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.model.enumerate.QrcodeType;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.QrcodeVO;
import com.bluenet.web.domain.repository.QrcodeRepository;
import com.bluenet.web.domain.service.QrcodeDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 二维码领域服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QrcodeDomainServiceImpl implements QrcodeDomainService {

    private final QrcodeRepository qrcodeRepository;

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
}
