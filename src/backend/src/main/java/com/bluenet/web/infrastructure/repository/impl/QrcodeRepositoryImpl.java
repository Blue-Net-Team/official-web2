package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.Qrcode;
import com.bluenet.web.domain.model.vo.QrcodeVO;
import com.bluenet.web.domain.repository.QrcodeRepository;
import com.bluenet.web.infrastructure.repository.mapper.QrcodeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 二维码 Repository 实现
 */
@Repository
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "minio.enabled", havingValue = "true")
public class QrcodeRepositoryImpl implements QrcodeRepository {

    private final QrcodeMapper qrcodeMapper;

    @Override
    public void save(QrcodeVO qrcodeVO) {
        log.info("保存二维码: {}", qrcodeVO);
        Qrcode qrcode = Qrcode.builder()
                .id(qrcodeVO.getId())
                .fileId(qrcodeVO.getFileId())
                .type(qrcodeVO.getType())
                .build();
        if (qrcode.getId() == null) {
            qrcodeMapper.insert(qrcode);
        } else {
            qrcodeMapper.updateById(qrcode);
        }
    }

    @Override
    public Optional<Qrcode> findById(Long id) {
        Qrcode qrcode = qrcodeMapper.selectById(id);
        if (qrcode == null) {
            log.warn("二维码不存在: id={}", id);
            return Optional.empty();
        }
        return Optional.of(qrcode);
    }

    @Override
    public Optional<Qrcode> findByFileId(Long fileId) {
        Qrcode qrcode = qrcodeMapper.selectByFileId(fileId);
        if (qrcode == null) {
            log.warn("二维码不存在: fileId={}", fileId);
            return Optional.empty();
        }
        return Optional.of(qrcode);
    }

    private QrcodeVO convertToVO(Qrcode qrcode) {
        return QrcodeVO.builder().id(qrcode.getId()).fileId(qrcode.getFileId()).type(qrcode.getType()).build();
    }

    private Qrcode convertToEntity(QrcodeVO qrcodeVO) {
        return Qrcode.builder().id(qrcodeVO.getId()).fileId(qrcodeVO.getFileId()).type(qrcodeVO.getType()).build();
    }
}
