package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.AssessmentAnswerVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.repository.FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Repository
@ConditionalOnProperty(name = "storage.enabled", havingValue = "false", matchIfMissing = false)
public class MockFileRepository implements FileRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Optional<FileVO> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Optional<AssessmentAnswerVO> findAnswerByFileId(Long fileId) {
        return Optional.empty();
    }

    @Override
    public Optional<AssessmentQuestionVO> findQuestionByAttachmentId(Long attachmentId) {
        return Optional.empty();
    }

    @Override
    public AssessmentTimeVO findTimeById(Long id) {
        return null;
    }

    @Override
    public FileVO saveFile(InputStream inputStream, File file) {
        log.debug("Mock saving file: {}", file.getName());

        file.setId(idGenerator.getAndIncrement());

        return FileVO.builder().id(file.getId()).name(file.getName()).type(file.getType()).url(file.getUrl()).build();
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
}
