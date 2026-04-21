package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

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

/**
 * 文件仓储实现类。
 * <p>
 * 负责文件元数据和考核业务关联查询，文件对象本身的读写删除委托给当前启用的 {@link ObjectStorage} 适配器。
 * </p>
 */
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

    /**
     * 按主键查询文件 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的文件 结果；不存在时为空。
     */
    @Override
    public Optional<FileVO> findById(Long id) {
        File file = RepositoryObjectConverter.toDomain(fileMapper.selectById(id), File.class);
        if (file == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(file));
    }

    /**
     * 查询符合条件的文件 记录。
     *
     * @param fileId
     *            文件主键。
     * @return 查询到的文件 结果；不存在时为空。
     */
    @Override
    public Optional<AssessmentAnswerVO> findAnswerByFileId(Long fileId) {
        AssessmentAnswer answer = RepositoryObjectConverter.toDomain(
                assessmentAnswerMapper.selectOne(
                        new LambdaQueryWrapper<AssessmentAnswerDO>()
                                .eq(AssessmentAnswerDO::getFileId, fileId)
                                .last("LIMIT 1")),
                AssessmentAnswer.class);
        if (answer == null) {
            return Optional.empty();
        }
        return Optional.of(convertToAnswerVO(answer));
    }

    /**
     * 查询符合条件的文件 记录。
     *
     * @param attachmentId
     *            附件文件主键。
     * @return 查询到的文件 结果；不存在时为空。
     */
    @Override
    public Optional<AssessmentQuestionVO> findQuestionByAttachmentId(Long attachmentId) {
        AssessmentQuestion question = RepositoryObjectConverter.toDomain(
                assessmentQuestionMapper.selectOne(
                        new LambdaQueryWrapper<AssessmentQuestionDO>()
                                .eq(AssessmentQuestionDO::getAttachmentId, attachmentId)
                                .last("LIMIT 1")),
                AssessmentQuestion.class);
        if (question == null) {
            return Optional.empty();
        }
        return Optional.of(convertToQuestionVO(question));
    }

    /**
     * 查询符合条件的文件 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询或处理得到的文件 结果。
     */
    @Override
    public AssessmentTimeVO findTimeById(Long id) {
        AssessmentTime assessmentTime = RepositoryObjectConverter
                .toDomain(assessmentTimeMapper.selectById(id), AssessmentTime.class);
        if (assessmentTime == null) {
            throw new DataNotFound("Assessment time not found, ID: " + id);
        }
        return convertToTimeVO(assessmentTime);
    }

    /**
     * 处理文件 仓储职责中的业务数据访问逻辑。
     *
     * @param inputStream
     *            待保存文件的输入流。
     * @param file
     *            文件领域对象或文件视图对象。
     * @return 查询或处理得到的文件 结果。
     */
    @Override
    @Transactional
    public FileVO saveFile(InputStream inputStream, File file) {
        validateParameters(file.getName(), inputStream, file.getType());

        RepositoryObjectConverter.insert(fileMapper, file, FileDO.class);
        objectStorage.put(file.getType(), file.getName(), inputStream);
        log.debug("File metadata and object saved successfully: id={}, type={}", file.getId(), file.getType());

        return FileVO.builder()
                .id(file.getId())
                .name(file.getName())
                .type(file.getType())
                .build();
    }

    /**
     * 从对象存储加载指定文件资源。
     *
     * @param filename
     *            对象存储中的文件名。
     * @param fileType
     *            文件业务类型。
     * @return 查询或处理得到的文件 结果。
     */
    @Override
    public Resource loadFile(String filename, FileType fileType) {
        validateParameters(filename, fileType);
        return objectStorage.get(fileType, filename);
    }

    /**
     * 按文件名和类型删除文件元数据和对象存储内容。
     *
     * @param filename
     *            对象存储中的文件名。
     * @param fileType
     *            文件业务类型。
     */
    @Override
    @Transactional
    public void deleteFile(String filename, FileType fileType) {
        // 查找对应的文件元数据，避免只删除对象后留下孤立数据库记录。
        Optional<File> fileOp = Optional.ofNullable(
                RepositoryObjectConverter.toDomain(fileMapper.selectByNameAndType(filename, fileType), File.class));
        if (fileOp.isEmpty()) {
            log.warn("File not found in database for deletion: {} ({})", filename, fileType);
            throw new DataNotFound("File not found for deletion: " + filename);
        }
        int influencedRows = fileMapper.deleteById(fileOp.get().getId());
        if (influencedRows == 0) {
            log.warn("Failed to delete file record from database: {} ({})", filename, fileType);
            throw new RuntimeException("Failed to delete file record: " + filename);
        }
        // 删除数据库记录后再删除对象存储中的文件。
        objectStorage.delete(fileType, filename);
    }

    /**
     * 按文件主键删除文件元数据和对象存储内容。
     *
     * @param id
     *            业务记录主键。
     */
    @Override
    @Transactional
    public void deleteFileById(Long id) {
        // 查找对应的文件元数据，拿到对象存储删除所需的文件类型和文件名。
        File file = RepositoryObjectConverter.toDomain(fileMapper.selectById(id), File.class);
        if (file == null) {
            log.warn("File not found in database for deletion: id={}", id);
            throw new DataNotFound("File not found for deletion, id: " + id);
        }
        int influencedRows = fileMapper.deleteById(id);
        if (influencedRows == 0) {
            log.warn("Failed to delete file record from database: id={}", id);
            throw new RuntimeException("Failed to delete file record, id: " + id);
        }
        // 删除数据库记录后再删除对象存储中的文件。
        objectStorage.delete(file.getType(), file.getName());
    }

    /**
     * 校验文件仓储操作所需参数。
     *
     * @param filename
     *            对象存储中的文件名。
     * @param inputStream
     *            待保存文件的输入流。
     * @param fileType
     *            文件业务类型。
     */
    private void validateParameters(String filename, InputStream inputStream, FileType fileType) {
        validateParameters(filename, fileType);
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
    }

    /**
     * 校验文件仓储操作所需参数。
     *
     * @param filename
     *            对象存储中的文件名。
     * @param fileType
     *            文件业务类型。
     */
    private void validateParameters(String filename, FileType fileType) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }
        if (fileType == null) {
            throw new IllegalArgumentException("FileType cannot be null");
        }
    }

    /**
     * 在文件 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param file
     *            文件领域对象或文件视图对象。
     * @return 转换后的目标模型对象。
     */
    private FileVO convertToVO(File file) {
        return FileVO.builder()
                .id(file.getId())
                .name(file.getName())
                .type(file.getType())
                .url(file.getUrl())
                .build();
    }

    /**
     * 处理文件 仓储职责中的业务数据访问逻辑。
     *
     * @param answer
     *            考核作答对象。
     * @return 转换后的目标模型对象。
     */
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

    /**
     * 处理文件 仓储职责中的业务数据访问逻辑。
     *
     * @param question
     *            考核题目对象。
     * @return 转换后的目标模型对象。
     */
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

    /**
     * 处理文件 仓储职责中的业务数据访问逻辑。
     *
     * @param assessmentTime
     *            考核场次对象。
     * @return 转换后的目标模型对象。
     */
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
