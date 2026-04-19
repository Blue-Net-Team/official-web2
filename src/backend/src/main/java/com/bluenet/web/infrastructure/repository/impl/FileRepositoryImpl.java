package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.AssessmentAnswerVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentAnswerMapper;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentQuestionMapper;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentTimeMapper;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.infrastructure.storage.ObjectStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
@ConditionalOnBean(ObjectStorage.class)
public class FileRepositoryImpl implements FileRepository {

    private final ObjectStorage objectStorage;
    private final FileMapper fileMapper;
    private final AssessmentAnswerMapper assessmentAnswerMapper;
    private final AssessmentQuestionMapper assessmentQuestionMapper;
    private final AssessmentTimeMapper assessmentTimeMapper;

    @Override
    public Optional<FileVO> findById(Long id) {
        File file = fileMapper.selectById(id);
        if (file == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(file));
    }

    @Override
    public Optional<AssessmentAnswerVO> findAnswerByFileId(Long fileId) {
        AssessmentAnswer answer = assessmentAnswerMapper.selectOne(
                new LambdaQueryWrapper<AssessmentAnswer>()
                        .eq(AssessmentAnswer::getFileId, fileId)
                        .last("LIMIT 1"));
        if (answer == null) {
            return Optional.empty();
        }
        return Optional.of(convertToAnswerVO(answer));
    }

    @Override
    public Optional<AssessmentQuestionVO> findQuestionByAttachmentId(Long attachmentId) {
        AssessmentQuestion question = assessmentQuestionMapper.selectOne(
                new LambdaQueryWrapper<AssessmentQuestion>()
                        .eq(AssessmentQuestion::getAttachmentId, attachmentId)
                        .last("LIMIT 1"));
        if (question == null) {
            return Optional.empty();
        }
        return Optional.of(convertToQuestionVO(question));
    }

    @Override
    public AssessmentTimeVO findTimeById(Long id) {
        AssessmentTime assessmentTime = assessmentTimeMapper.selectById(id);
        if (assessmentTime == null) {
            throw new DataNotFound("Assessment time not found, ID: " + id);
        }
        return convertToTimeVO(assessmentTime);
    }

    @Override
    @Transactional
    public FileVO saveFile(InputStream inputStream, File file) {
        validateParameters(file.getName(), inputStream, file.getType());

        fileMapper.insert(file);
        objectStorage.put(file.getType(), file.getName(), inputStream);
        log.debug("File metadata and object saved successfully: id={}, type={}", file.getId(), file.getType());

        return FileVO.builder()
                .id(file.getId())
                .name(file.getName())
                .type(file.getType())
                .build();
    }

    @Override
    public Resource loadFile(String filename, FileType fileType) {
        validateParameters(filename, fileType);
        return objectStorage.get(fileType, filename);
    }

    @Override
    @Transactional
    public void deleteFile(String filename, FileType fileType) {
        Optional<File> fileOp = fileMapper.selectByNameAndType(filename, fileType);
        if (fileOp.isEmpty()) {
            log.warn("File not found in database for deletion: {} ({})", filename, fileType);
            throw new DataNotFound("File not found for deletion: " + filename);
        }
        int influencedRows = fileMapper.deleteById(fileOp.get().getId());
        if (influencedRows == 0) {
            log.warn("Failed to delete file record from database: {} ({})", filename, fileType);
            throw new RuntimeException("Failed to delete file record: " + filename);
        }
        objectStorage.delete(fileType, filename);
    }

    @Override
    @Transactional
    public void deleteFileById(Long id) {
        File file = fileMapper.selectById(id);
        if (file == null) {
            log.warn("File not found in database for deletion: id={}", id);
            throw new DataNotFound("File not found for deletion, id: " + id);
        }
        int influencedRows = fileMapper.deleteById(id);
        if (influencedRows == 0) {
            log.warn("Failed to delete file record from database: id={}", id);
            throw new RuntimeException("Failed to delete file record, id: " + id);
        }
        objectStorage.delete(file.getType(), file.getName());
    }

    private void validateParameters(String filename, InputStream inputStream, FileType fileType) {
        validateParameters(filename, fileType);
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
    }

    private void validateParameters(String filename, FileType fileType) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }
        if (fileType == null) {
            throw new IllegalArgumentException("FileType cannot be null");
        }
    }

    private FileVO convertToVO(File file) {
        return FileVO.builder()
                .id(file.getId())
                .name(file.getName())
                .type(file.getType())
                .url(file.getUrl())
                .build();
    }

    private AssessmentAnswerVO convertToAnswerVO(AssessmentAnswer answer) {
        return AssessmentAnswerVO.builder()
                .id(answer.getId())
                .userId(answer.getUserId())
                .questionId(answer.getQuestionId())
                .content(answer.getContent())
                .language(answer.getLanguage())
                .fileId(answer.getFileId())
                .submitTime(answer.getSubmitTime())
                .build();
    }

    private AssessmentQuestionVO convertToQuestionVO(AssessmentQuestion question) {
        return AssessmentQuestionVO.builder()
                .id(question.getId())
                .assessmentTimeId(question.getAssessmentTimeId())
                .questionNo(question.getQuestionNo())
                .title(question.getTitle())
                .content(question.getContent())
                .attachmentId(question.getAttachmentId())
                .questionType(question.getQuestionType())
                .score(question.getScore())
                .build();
    }

    private AssessmentTimeVO convertToTimeVO(AssessmentTime assessmentTime) {
        return AssessmentTimeVO.builder()
                .id(assessmentTime.getId())
                .direction(assessmentTime.getDirection())
                .epoch(assessmentTime.getEpoch())
                .startTime(assessmentTime.getStartTime())
                .endTime(assessmentTime.getEndTime())
                .timeLimit(assessmentTime.getTimeLimit())
                .timeLimitMinutes(assessmentTime.getTimeLimitMinutes())
                .build();
    }
}
