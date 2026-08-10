package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.repository.FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Repository
@ConditionalOnProperty(name = "storage.enabled", havingValue = "false", matchIfMissing = false)
public class MockFileRepository implements FileRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Optional<File> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public File saveFile(InputStream inputStream, File file) {
        log.debug("Mock saving file: {}", file.getName());

        file.setId(idGenerator.getAndIncrement());

        return file;
    }

    @Override
    public Resource loadFile(String filename, FileType fileType) {
        log.debug("Mock loading file: {} ({})", filename, fileType);
        return new ByteArrayResource(new byte[0]);
    }

    @Override
    public void deleteFile(String filename, FileType fileType) {
        log.debug("Mock deleting file: {} ({})", filename, fileType);
    }

    @Override
    public void deleteFileById(Long id) {
        log.debug("Mock deleting file by id: {}", id);
    }

    @Override
    public File saveFileMetadata(File file) {
        log.debug("Mock saving file metadata: {}", file.getName());
        file.setId(idGenerator.getAndIncrement());
        return file;
    }

    @Override
    public File save(File file) {
        log.debug("Mock saving file metadata: {}", file.getId());
        if (file.getId() == null) {
            file.setId(idGenerator.getAndIncrement());
        }
        return file;
    }

    @Override
    public List<File> findOrphanFiles() {
        log.debug("Mock findOrphanFiles called");
        return Collections.emptyList();
    }

    @Override
    public Optional<File> findLatestByType(FileType type) {
        log.debug("Mock findLatestByType called: {}", type);
        return Optional.empty();
    }

    @Override
    public Optional<File> findLatestByTypeExcludingId(FileType type, Long excludeId) {
        log.debug("Mock findLatestByTypeExcludingId called: {}, excludeId={}", type, excludeId);
        return Optional.empty();
    }
}
