package com.bluenet.web.infrastructure.storage;

import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.infrastructure.config.properties.StorageProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectLocationResolverTest {

    @ParameterizedTest
    @EnumSource(FileType.class)
    @DisplayName("all FileTypes resolve to configured bucket and type prefix")
    void allFileTypes_ShouldResolveConfiguredBucketAndTypePrefix(FileType fileType) {
        StorageProperties properties = new StorageProperties();
        properties.setBucket("bluenet");
        ObjectLocationResolver resolver = new ObjectLocationResolver(properties);

        ObjectLocation location = resolver.resolve(fileType, "test.dat");

        assertThat(location.bucket()).isEqualTo("bluenet");
        assertThat(location.objectKey()).isEqualTo(fileType.getValue() + "/test.dat");
    }
}
