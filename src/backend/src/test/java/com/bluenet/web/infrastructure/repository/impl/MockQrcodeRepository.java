package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.Qrcode;
import com.bluenet.web.domain.model.enumerate.QrcodeType;
import com.bluenet.web.domain.repository.QrcodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@Primary
@ConditionalOnProperty(name = "storage.enabled", havingValue = "false")
public class MockQrcodeRepository implements QrcodeRepository {

    @Override
    public void save(Qrcode qrcode) {
        log.debug("Mock saving QRCode: {}", qrcode);
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
