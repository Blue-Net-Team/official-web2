package com.bluenet.web.infrastructure.storage;

import com.bluenet.web.domain.model.enumerate.FileType;
import org.springframework.core.io.Resource;

import java.io.InputStream;

public interface ObjectStorage {

    String providerName();

    void ensureBucket();

    void put(FileType fileType, String filename, InputStream inputStream);

    Resource get(FileType fileType, String filename);

    void delete(FileType fileType, String filename);

    void checkHealth();
}
