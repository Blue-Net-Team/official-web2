package com.bluenet.web.domain.service.impl;

import com.bluenet.web.infrastructure.repository.impl.QrcodeRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import static org.assertj.core.api.Assertions.assertThat;

class QrcodeStorageWiringTest {

    @Test
    @DisplayName("QR code components are not gated by MinIO properties")
    void qrcodeComponents_ShouldNotBeGatedByMinioProperties() {
        assertThat(QrcodeDomainServiceImpl.class.getAnnotation(ConditionalOnProperty.class)).isNull();
        assertThat(QrcodeRepositoryImpl.class.getAnnotation(ConditionalOnProperty.class)).isNull();
    }
}
