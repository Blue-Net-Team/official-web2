package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
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
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Optional;

/**
 * MinIO文件存储实现类
 */
@Slf4j
@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "minio.enabled", havingValue = "true")
@ConditionalOnBean(MinioClient.class)
public class MinioFileRepository implements FileRepository {

    private final MinioClient minioClient;
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
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AssessmentAnswer>()
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
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AssessmentQuestion>()
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
            throw new DataNotFound("考试时间不存在，ID: " + id);
        }
        return convertToTimeVO(assessmentTime);
    }

    /**
     * 保存文件到MinIO
     *
     * @param inputStream
     *            文件输入流
     * @param file
     *            文件实体，包含文件名和文件类型等信息
     */
    @Override
    @Transactional
    public FileVO saveFile(InputStream inputStream, File file) {
        validateParameters(file.getName(), inputStream, file.getType());

        // 保存到文件表
        fileMapper.insert(file);

        String bucketName = getBucketName(file.getType());

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(file.getName())
                            .stream(inputStream, -1, 10485760)
                            .build());

            log.debug("File saved successfully: {}/{}", bucketName, file.getName());
        } catch (ErrorResponseException e) {
            log.error("MinIO error response while saving file: {}/{}", bucketName, file.getName(), e);
            throw new RuntimeException("Failed to save file to MinIO: " + file.getName(), e);
        } catch (Exception e) {
            log.error("Error saving file to MinIO: {}/{}", bucketName, file.getName(), e);
            throw new RuntimeException("Failed to save file: " + file.getName(), e);
        }

        // 构建VO并返回
        return FileVO.builder()
                .id(file.getId())
                .name(file.getName())
                .type(file.getType())
                .build();
    }

    /**
     * 从MinIO加载文件
     *
     * @param filename
     *            文件名
     * @param fileType
     *            文件类型
     * @return 文件资源
     */
    @Override
    public Resource loadFile(String filename, FileType fileType) {
        validateParameters(filename, fileType);

        String bucketName = getBucketName(fileType);

        try {
            InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build());

            byte[] data = inputStream.readAllBytes();
            inputStream.close();

            log.debug("File loaded successfully: {}/{}", bucketName, filename);
            return new ByteArrayResource(data) {
                @Override
                public @NonNull String getFilename() {
                    return filename;
                }
            };
        } catch (ErrorResponseException e) {
            if (e.response().code() == 404) {
                log.warn("File not found in MinIO: {}/{}", bucketName, filename);
                throw new DataNotFound("File not found: " + filename);
            }
            log.error("MinIO error response while loading file: {}/{}", bucketName, filename, e);
            throw new RuntimeException("Failed to load file from MinIO: " + filename, e);
        } catch (Exception e) {
            log.error("Error loading file from MinIO: {}/{}", bucketName, filename, e);
            throw new RuntimeException("Failed to load file: " + filename, e);
        }
    }

    @Override
    @Transactional
    public void deleteFile(String filename, FileType fileType) {
        // 查找对应的File
        Optional<File> fileOp = fileMapper.selectByNameAndType(filename, fileType);
        if (fileOp.isEmpty()) {
            log.warn("File not found in database for deletion: {} ({})", filename, fileType);
            throw new DataNotFound("File not found for deletion: " + filename);
        }
        // 删除数据库记录
        int influencedRows = fileMapper.deleteById(fileOp.get().getId());
        if (influencedRows == 0) {
            log.warn("Failed to delete file record from database: {} ({})", filename, fileType);
            throw new RuntimeException("Failed to delete file record: " + filename);
        }

        // 删除MinIO对象
        String bucketName = getBucketName(fileType);
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(filename)
                .build();
        try {
            minioClient.removeObject(removeObjectArgs);
            log.debug("File deleted successfully from MinIO: {}/{}", bucketName, filename);
        } catch (Exception e) {
            log.error("Error deleting file from MinIO: {}/{}", bucketName, filename, e);
            throw new RuntimeException("Failed to delete file from MinIO: " + filename, e);
        }
    }

    @Override
    @Transactional
    public void deleteFileById(Long id) {
        // 查找对应的File
        File file = fileMapper.selectById(id);
        if (file == null) {
            log.warn("File not found in database for deletion: id={}", id);
            throw new DataNotFound("File not found for deletion, id: " + id);
        }

        // 删除数据库记录
        int influencedRows = fileMapper.deleteById(id);
        if (influencedRows == 0) {
            log.warn("Failed to delete file record from database: id={}", id);
            throw new RuntimeException("Failed to delete file record, id: " + id);
        }

        // 删除MinIO对象
        String bucketName = getBucketName(file.getType());
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(file.getName())
                            .build());
            log.debug("File deleted successfully from MinIO: {}/{}", bucketName, file.getName());
        } catch (Exception e) {
            log.error("Error deleting file from MinIO: {}/{}", bucketName, file.getName(), e);
            throw new RuntimeException("Failed to delete file from MinIO: " + file.getName(), e);
        }
    }

    /**
     * 根据FileType获取bucket名称
     *
     * @param fileType
     *            文件类型
     * @return bucket名称
     */
    private String getBucketName(FileType fileType) {
        return fileType.getValue();
    }

    /**
     * 验证保存文件方法的参数
     */
    private void validateParameters(String filename, InputStream inputStream, FileType fileType) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        if (fileType == null) {
            throw new IllegalArgumentException("FileType cannot be null");
        }
    }

    /**
     * 验证加载文件方法的参数
     */
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
