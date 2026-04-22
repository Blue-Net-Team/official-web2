package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.AssessmentAnswerVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.service.FileDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.UUID;

import static com.google.common.io.Files.getFileExtension;

@Service
@RequiredArgsConstructor
public class FileDomainServiceImpl implements FileDomainService {
    private final FileRepository fileRepository;
    private final AssessmentAnswerRepository assessmentAnswerRepository;
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final AssessmentTimeRepository assessmentTimeRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public FileVO getFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new DataNotFound("文件不存在，ID: " + fileId));
    }

    @Override
    public AssessmentAnswerVO getAnswerByFileId(Long fileId) {
        // 作答关联查询回归考核作答仓储，文件仓储只保留文件边界。
        return assessmentAnswerRepository.findByFileId(fileId)
                .orElseThrow(() -> new DataNotFound("答题不存在，文件ID: " + fileId));
    }

    @Override
    public AssessmentQuestionVO getQuestionByAttachmentId(Long attachmentId) {
        // 附件关联查询回归考核题目仓储，避免文件仓储依赖题目 mapper。
        return assessmentQuestionRepository.findByAttachmentId(attachmentId)
                .orElseThrow(() -> new DataNotFound("题目不存在，附件ID: " + attachmentId));
    }

    @Override
    public AssessmentTimeVO getAssessmentTimeById(Long id) {
        return assessmentTimeRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("Assessment time not found, ID: " + id));
    }

    @Override
    public String generateFilename(FileType fileType, String fileExtension) {
        String uuidPart = UUID.randomUUID().toString();
        return String.format("%s-%s.%s", fileType.name().toLowerCase(), uuidPart, fileExtension);
    }

    /**
     * 生成文件url
     *
     * @deprecated url 字段已废弃，此方法仅用于向后兼容
     * @param fileType
     *            文件类型枚举
     * @return 生成的url
     */
    @Deprecated
    private String generateFileUrl(FileType fileType) {
        String uuidPart = UUID.randomUUID().toString();
        return String.format("%s/%s", fileType.name().toLowerCase(), uuidPart);
    }

    @Override
    @Transactional
    public FileVO saveFile(FileType fileType, String filename, InputStream inputStream) {
        // 生成随机文件名
        String newFilename = generateFilename(fileType, getFileExtension(filename));

        // 构建File对象
        File file = File.builder().name(newFilename).type(fileType).url(generateFileUrl(fileType)).build();
        // 保存到文件表
        fileRepository.saveFile(inputStream, file);

        return convertToVO(file);
    }

    @Override
    public Resource loadFile(FileType fileType, String filename) {
        return fileRepository.loadFile(filename, fileType);
    }

    private FileVO convertToVO(File file) {
        return FileVO.builder().id(file.getId()).name(file.getName()).type(file.getType()).url(file.getUrl()).build();
    }
}
