package com.bluenet.web.infrastructure.storage;

import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.infrastructure.config.properties.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class ObjectLocationResolver {

    private final StorageProperties storageProperties;

    public ObjectLocation resolve(FileType fileType, String filename) {
        if (fileType == null) {
            throw new IllegalArgumentException("FileType cannot be null");
        }
        if (!StringUtils.hasText(filename)) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }
        String bucket = storageProperties.getBucket();
        if (!StringUtils.hasText(bucket)) {
            throw new IllegalStateException("storage.bucket must not be empty");
        }
        return new ObjectLocation(bucket, fileType.getValue() + "/" + filename);
    }
}
