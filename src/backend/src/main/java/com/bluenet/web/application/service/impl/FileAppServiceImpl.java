package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.FileDownloadResult;
import com.bluenet.web.application.FileResult;
import com.bluenet.web.application.command.file.FileCommands;
import com.bluenet.web.application.service.FileAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.policy.RoleHierarchy;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.ByteArrayResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件应用服务实现。
 * <p>
 * 实现文件聚合在应用层的业务逻辑编排。
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileAppServiceImpl implements FileAppService {

    private final FileDomainService fileDomainService;
    private final FileRepository fileRepository;

    /**
     * 上传文件。
     *
     * @param command
     *            上传文件命令
     * @return 文件上传结果
     */
    @Override
    public FileResult uploadFile(FileCommands.UploadFileCommand command) {
        MultipartFile file = command.file();
        FileType type = command.type();
        String filename = fileDomainService.generateFilename(type, file.getOriginalFilename());
        FileVO fileVO = saveFile(type, filename, file);
        log.info("文件上传成功，文件id: {}, 类型: {}", fileVO.getId(), type);
        return new FileResult(fileVO.getId(), fileVO.getName(), fileVO.getType(), fileVO.getUrl());
    }

    /**
     * 下载文件。
     *
     * @param command
     *            下载文件命令
     * @return 文件下载结果
     */
    @Override
    public FileDownloadResult downloadFile(FileCommands.DownloadFileCommand command) {
        FileVO fileVO = fileDomainService.getFileById(command.fileId());

        checkDownloadPermission(fileVO);

        Resource resource = fileRepository.loadFile(fileVO.getName(), fileVO.getType());
        if (resource == null || !resource.exists()) {
            log.warn("File resource not found for file: {}", fileVO.getName());
            throw new DataNotFound("文件资源不存在");
        }

        log.info(
                "File downloaded successfully: id={}, type={}, name={}",
                command.fileId(),
                fileVO.getType(),
                fileVO.getName());
        return new FileDownloadResult(resource, resource.getFilename());
    }

    /**
     * 批量下载文件并打包为 ZIP。
     *
     * @param command
     *            批量下载命令
     * @return 文件下载结果（resource 为 ZIP 字节流，filename 为 ZIP 包名）
     */
    @Override
    public FileDownloadResult downloadBatch(FileCommands.BatchDownloadCommand command) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (FileCommands.BatchDownloadEntry entry : command.entries()) {
                FileVO fileVO = fileDomainService.getFileById(entry.fileId());
                checkDownloadPermission(fileVO);

                Resource resource = fileRepository.loadFile(fileVO.getName(), fileVO.getType());
                if (resource == null || !resource.exists()) {
                    log.warn("File resource not found for batch download: {}", fileVO.getName());
                    throw new DataNotFound("文件资源不存在: " + fileVO.getName());
                }

                String entryName = entry.filename();
                if (entryName == null || entryName.isBlank()) {
                    entryName = fileVO.getName();
                } else if (!entryName.contains(".")) {
                    String originalName = fileVO.getName();
                    int lastDot = originalName.lastIndexOf('.');
                    if (lastDot > 0) {
                        entryName = entryName + originalName.substring(lastDot);
                    }
                }

                ZipEntry zipEntry = new ZipEntry(entryName);
                zos.putNextEntry(zipEntry);
                try (InputStream is = resource.getInputStream()) {
                    is.transferTo(zos);
                }
                zos.closeEntry();

                log.info(
                        "Batch download added entry: fileId={}, entryName={}",
                        entry.fileId(),
                        entryName);
            }
        } catch (IOException e) {
            log.error("Failed to create batch download zip", e);
            throw new RuntimeException("批量下载打包失败", e);
        }

        String zipName = command.zipName();
        if (zipName == null || zipName.isBlank()) {
            zipName = "download.zip";
        }
        if (!zipName.endsWith(".zip")) {
            zipName = zipName + ".zip";
        }

        return new FileDownloadResult(new ByteArrayResource(baos.toByteArray()), zipName);
    }

    @NotNull
    private FileVO saveFile(FileType type, String filename, MultipartFile file) {
        try {
            return fileDomainService.saveFile(type, filename, file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + filename, e);
        }
    }

    private void checkDownloadPermission(FileVO fileVO) {
        FileType fileType = fileVO.getType();

        switch (fileType) {
            case WORK -> checkWorkPermission(fileVO);
            case ASSESSMENT_ATTACHMENT -> checkAssessmentAttachmentPermission(fileVO);
            case AVATAR -> {
            }
            case NORMAL_IMG, QRCODE -> {
            }
            default -> {
                log.warn("Unknown file type: {}", fileType);
                throw new Forbidden("未知文件类型");
            }
        }
    }

    private void checkWorkPermission(FileVO fileVO) {
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            log.warn("User not authenticated for WORK file download");
            throw new Forbidden("需要登录才能下载作品文件");
        }

        AssessmentAnswer answer = fileDomainService.getAnswerByFileId(fileVO.getId());

        if (answer.getUserId().equals(currentUser.getId())) {
            return;
        }

        if (!hasRoleAtLeast(currentUser, RoleType.MEMBER)) {
            log.warn("User {} does not have permission to download work file {}", currentUser.getId(), fileVO.getId());
            throw new Forbidden("权限不够，需要 MEMBER 及以上权限");
        }
    }

    private void checkAssessmentAttachmentPermission(FileVO fileVO) {
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            log.warn("User not authenticated for ASSESSMENT_ATTACHMENT file download");
            throw new Forbidden("需要登录才能下载考题附件");
        }

        AssessmentQuestion question = fileDomainService.getQuestionByAttachmentId(fileVO.getId());
        if (question == null) {
            log.warn("No question found for assessment attachment: {}", fileVO.getId());
            throw new Forbidden("考题附件不存在");
        }

        AssessmentTime assessmentTime = fileDomainService.getAssessmentTimeById(question.getAssessmentTimeId());
        if (currentUser.getDirection() == null || !currentUser.getDirection().equals(assessmentTime.getDirection())) {
            log.warn(
                    "User direction {} does not match assessment time direction {}",
                    currentUser.getDirection(),
                    assessmentTime.getDirection());
            throw new Forbidden("方向不匹配，无法下载考题附件");
        }
    }

    private boolean hasRoleAtLeast(UserVO user, RoleType minRole) {
        String userRoleName = user.getRoleName();
        if (userRoleName == null || minRole == null) {
            return false;
        }

        RoleType userRole = RoleType.fromName(userRoleName);
        if (userRole == null) {
            return false;
        }

        return RoleHierarchy.hasRoleLevel(userRole, minRole);
    }
}
