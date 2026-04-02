package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.Qrcode;
import com.bluenet.web.domain.model.enumerate.QrcodeType;
import com.bluenet.web.domain.model.vo.QrcodeVO;
import com.bluenet.web.domain.repository.QrcodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Repository
@Primary
@ConditionalOnProperty(name = "minio.enabled", havingValue = "false")
public class MockQrcodeRepository implements QrcodeRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public void save(QrcodeVO qrcodeVO) {
        log.debug("Mock saving QRCode: {}", qrcodeVO);
    }

    @Override
    public Optional<Qrcode> findById(Long id) {
        log.debug("Mock finding QRCode by id: {}", id);
        return Optional.empty();
    }

    @Override
    public Optional<Qrcode> findByFileId(Long fileId) {
        log.debug("Mock finding QRCode by fileId: {}", fileId);
        return Optional.empty();
    }

    @Override
    public List<Qrcode> findByType(QrcodeType type) {
        log.debug("Mock finding QRCodes by type: {}", type);
        return List.of();
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Mock deleting QRCode by id: {}", id);
    }
}
