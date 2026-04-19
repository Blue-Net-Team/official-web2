package com.bluenet.web.infrastructure.config.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoragePropertiesTest {

    @Test
    @DisplayName("unsupported provider fails validation")
    void unsupportedProvider_ShouldFailValidation() {
        StorageProperties properties = new StorageProperties();
        properties.setProvider("unsupported");

        assertThatThrownBy(properties::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unsupported storage.provider");
    }
}
