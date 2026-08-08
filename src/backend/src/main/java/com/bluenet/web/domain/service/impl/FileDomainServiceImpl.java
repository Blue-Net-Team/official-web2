package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.policy.RoleHierarchy;
import com.bluenet.web.domain.model.result.ConfirmUploadResult;
import com.bluenet.web.domain.model.result.PresignedUploadResult;
import com.bluenet.web.infrastructure.security.principal.RoleTypeResolver;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.infrastructure.config.properties.StorageProperties;
import com.bluenet.web.infrastructure.security.jwt.PresignedUploadTokenService;
import com.bluenet.web.infrastructure.storage.FileMagicChecker;
import com.bluenet.web.infrastructure.storage.ObjectStorage;
import com.bluenet.web.infrastructure.storage.StorageObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.UUID;

import static com.google.common.io.Files.getFileExtension;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileDomainServiceImpl implements FileDomainService {
    private final FileRepository fileRepository;
    private final AssessmentAnswerRepository assessmentAnswerRepository;
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final AssessmentTimeRepository assessmentTimeRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectStorage objectStorage;
    private final PresignedUploadTokenService presignedUploadTokenService;
    private final FileMagicChecker fileMagicChecker;
    private final StorageProperties storageProperties;
    private final RoleTypeResolver roleTypeResolver;

    @Override
    public File getFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new DataNotFound("文件不存在，ID: " + fileId));
    }

    @Override
    public AssessmentAnswer getAnswerByFileId(Long fileId) {
        return assessmentAnswerRepository.findByFileId(fileId)
                .orElseThrow(() -> new DataNotFound("答题不存在，文件ID: " + fileId));
    }

    @Override
    public AssessmentQuestion getQuestionByAttachmentId(Long attachmentId) {
        return assessmentQuestionRepository.findByAttachmentId(attachmentId)
                .orElseThrow(() -> new DataNotFound("题目不存在，附件ID: " + attachmentId));
    }

    @Override
    public AssessmentTime getAssessmentTimeById(Long id) {
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
     */
    @Deprecated
    private String generateFileUrl(FileType fileType) {
        String uuidPart = UUID.randomUUID().toString();
        return String.format("%s/%s", fileType.name().toLowerCase(), uuidPart);
    }

    @Override
    @Transactional
    public File saveFile(FileType fileType, String filename, InputStream inputStream) {
        String newFilename = generateFilename(fileType, getFileExtension(filename));

        File file = File.reconstruct(
                null,
                newFilename,
                fileType,
                generateFileUrl(fileType),
                FileStatus.ACTIVE,
                java.time.LocalDateTime.now());
        File savedFile = fileRepository.saveFile(inputStream, file);

        return savedFile;
    }

    @Override
    public Resource loadFile(FileType fileType, String filename) {
        return fileRepository.loadFile(filename, fileType);
    }

    @Override
    @Transactional
    public PresignedUploadResult prepareUpload(FileType fileType, String originalFilename, String contentType,
            long size) {
        String extension = getFileExtension(originalFilename);
        String filename = generateFilename(fileType, extension);

        File file = File.reconstruct(
                null,
                filename,
                fileType,
                generateFileUrl(fileType),
                FileStatus.PENDING,
                java.time.LocalDateTime.now());

        File savedFile = fileRepository.saveFileMetadata(file);
        Long fileId = savedFile.getId();

        String uploadUrl = objectStorage.getPresignedUploadUrl(
                fileType,
                filename,
                contentType,
                size,
                storageProperties.getPresignedUploadExpiry());

        String callbackToken = presignedUploadTokenService.generateToken(
                fileId,
                "",
                storageProperties.getPresignedUploadExpiry());

        log.info("预签名上传准备完成，fileId={}, type={}, filename={}", fileId, fileType, filename);
        return new PresignedUploadResult(fileId, uploadUrl, callbackToken, filename, fileType);
    }

    @Override
    @Transactional
    public ConfirmUploadResult confirmUpload(Long fileId, String callbackToken, String expectedMd5, long expectedSize) {
        Long tokenFileId = presignedUploadTokenService.getFileId(callbackToken);
        if (tokenFileId == null || !tokenFileId.equals(fileId)) {
            log.warn("预签名上传确认失败，Token 无效或 fileId 不匹配: fileId={}", fileId);
            throw new Forbidden("无效的回调令牌");
        }

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new DataNotFound("文件不存在，ID: " + fileId));

        if (file.getStatus() == FileStatus.ACTIVE) {
            log.info("预签名上传确认幂等，文件已是 ACTIVE 状态: fileId={}", fileId);
            return new ConfirmUploadResult(fileId, file.getName(), file.getType(), FileStatus.ACTIVE);
        }

        if (file.getStatus() != FileStatus.PENDING) {
            log.warn("预签名上传确认失败，文件状态不是 PENDING: fileId={}, status={}", fileId, file.getStatus());
            throw new Forbidden("文件状态无效");
        }

        StorageObjectMetadata metadata;
        try {
            metadata = objectStorage.headObject(file.getType(), file.getName());
        } catch (DataNotFound e) {
            log.warn("预签名上传确认失败，OSS 对象不存在: fileId={}, filename={}", fileId, file.getName());
            updateFileStatus(file, FileStatus.REJECTED);
            return new ConfirmUploadResult(fileId, file.getName(), file.getType(), FileStatus.REJECTED);
        }

        String actualEtag = sanitizeEtag(metadata.etag());
        boolean md5Match = expectedMd5 == null || expectedMd5.isBlank() || expectedMd5.equalsIgnoreCase(actualEtag);
        boolean sizeMatch = expectedSize <= 0 || expectedSize == metadata.size();

        boolean magicMatch = true;
        if (metadata.contentType() != null && !metadata.contentType().isBlank()) {
            try {
                byte[] header = objectStorage.getObjectHeader(file.getType(), file.getName(), 8);
                magicMatch = fileMagicChecker.isValid(metadata.contentType(), header);
            } catch (Exception e) {
                log.warn("魔数检查失败，跳过: fileId={}, filename={}", fileId, file.getName());
            }
        }

        if (md5Match && sizeMatch && magicMatch) {
            updateFileStatus(file, FileStatus.ACTIVE);
            log.info("预签名上传确认成功，fileId={}, filename={}, etag={}", fileId, file.getName(), actualEtag);
            return new ConfirmUploadResult(fileId, file.getName(), file.getType(), FileStatus.ACTIVE);
        } else {
            log.warn(
                    "预签名上传确认失败，校验不通过: fileId={}, expectedMd5={}, actualEtag={}, expectedSize={}, actualSize={}, magicMatch={}",
                    fileId,
                    expectedMd5,
                    actualEtag,
                    expectedSize,
                    metadata.size(),
                    magicMatch);
            try {
                objectStorage.delete(file.getType(), file.getName());
            } catch (Exception e) {
                log.error("清理 OSS 对象失败: fileId={}, filename={}", fileId, file.getName(), e);
            }
            updateFileStatus(file, FileStatus.REJECTED);
            return new ConfirmUploadResult(fileId, file.getName(), file.getType(), FileStatus.REJECTED);
        }
    }

    @Override
    public String getPresignedDownloadUrl(FileType fileType, String filename) {
        return objectStorage
                .getPresignedDownloadUrl(fileType, filename, storageProperties.getPresignedDownloadExpiry());
    }

    private void updateFileStatus(File file, FileStatus status) {
        file.setStatus(status);
        fileRepository.save(file);
    }

    private String sanitizeEtag(String etag) {
        if (etag == null) {
            return null;
        }
        String sanitized = etag.trim();
        if (sanitized.startsWith("\"") && sanitized.endsWith("\"")) {
            sanitized = sanitized.substring(1, sanitized.length() - 1);
        }
        return sanitized;
    }

    @Override
    public void checkDownloadPermission(File file, User currentUser) {
        FileType fileType = file.getType();

        switch (fileType) {
            case WORK -> checkWorkPermission(file, currentUser);
            case ASSESSMENT_ATTACHMENT -> checkAssessmentAttachmentPermission(file, currentUser);
            case AVATAR -> {
            }
            case NORMAL_IMG, QRCODE, ENROLL_FORM -> {
            }
            default -> {
                log.warn("Unknown file type: {}", fileType);
                throw new Forbidden("未知文件类型");
            }
        }
    }

    private void checkWorkPermission(File file, User currentUser) {
        if (currentUser == null) {
            log.warn("User not authenticated for WORK file download");
            throw new Forbidden("需要登录才能下载作品文件");
        }

        // 校验答案存在（不存在时抛 DataNotFound）
        getAnswerByFileId(file.getId());

        // 组队场景下同一 fileId 对应队长与多名队员的多条答案记录，
        // 只要其中任意一条属于当前用户即视为所有者，不能仅判断单条记录的归属
        if (assessmentAnswerRepository.existsByFileIdAndUserId(file.getId(), currentUser.getId())) {
            return;
        }

        if (!hasRoleAtLeast(currentUser, RoleType.MEMBER)) {
            log.warn("User {} does not have permission to download work file {}", currentUser.getId(), file.getId());
            throw new Forbidden("权限不够，需要 MEMBER 及以上权限");
        }
    }

    private void checkAssessmentAttachmentPermission(File file, User currentUser) {
        if (currentUser == null) {
            log.warn("User not authenticated for ASSESSMENT_ATTACHMENT file download");
            throw new Forbidden("需要登录才能下载考题附件");
        }

        AssessmentQuestion question = getQuestionByAttachmentId(file.getId());
        if (question == null) {
            log.warn("No question found for assessment attachment: {}", file.getId());
            throw new Forbidden("考题附件不存在");
        }

        AssessmentTime assessmentTime = getAssessmentTimeById(question.getAssessmentTimeId());
        if (currentUser.getDirection() == null || !currentUser.getDirection().equals(assessmentTime.getDirection())) {
            log.warn(
                    "User direction {} does not match assessment time direction {}",
                    currentUser.getDirection(),
                    assessmentTime.getDirection());
            throw new Forbidden("方向不匹配，无法下载考题附件");
        }
    }

    private boolean hasRoleAtLeast(User user, RoleType minRole) {
        RoleType userRole = roleTypeResolver.resolve(user.getRoleId());
        if (userRole == null || minRole == null) {
            return false;
        }

        return RoleHierarchy.hasRoleLevel(userRole, minRole);
    }
}
