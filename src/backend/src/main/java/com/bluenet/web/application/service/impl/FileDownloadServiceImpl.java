package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.service.FileDownloadService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.AssessmentAnswerVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.policy.RoleHierarchy;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDownloadServiceImpl implements FileDownloadService {

    private final FileDomainService fileDomainService;
    private final FileRepository fileRepository;

    @Override
    public Resource downloadFile(Long fileId) {
        FileVO fileVO = fileDomainService.getFileById(fileId);

        checkDownloadPermission(fileVO);

        Resource resource = fileRepository.loadFile(fileVO.getName(), fileVO.getType());
        if (resource == null || !resource.exists()) {
            log.warn("File resource not found for file: {}", fileVO.getName());
            throw new DataNotFound("文件资源不存在");
        }

        log.info("File downloaded successfully: id={}, type={}, name={}", fileId, fileVO.getType(), fileVO.getName());
        return resource;
    }

    private void checkDownloadPermission(FileVO fileVO) {
        FileType fileType = fileVO.getType();

        switch (fileType) {
            case WORK :
                checkWorkPermission(fileVO);
                break;
            case ASSESSMENT_ATTACHMENT :
                checkAssessmentAttachmentPermission(fileVO);
                break;
            case AVATAR :
                checkAvatarPermission(fileVO);
                break;
            case NORMAL_IMG :
            case QRCODE :
                break;
            default :
                log.warn("Unknown file type: {}", fileType);
                throw new Forbidden("未知的文件类型");
        }
    }

    private void checkWorkPermission(FileVO fileVO) {
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            log.warn("User not authenticated for WORK file download");
            throw new Forbidden("需要登录才能下载作品文件");
        }

        AssessmentAnswerVO answer = fileDomainService.getAnswerByFileId(fileVO.getId());

        if (answer.getUserId().equals(currentUser.getId())) {
            return;
        }

        if (!hasRoleAtLeast(currentUser, RoleType.MEMBER)) {
            log.warn("User {} does not have permission to download work file {}", currentUser.getId(), fileVO.getId());
            throw new Forbidden("权限不足，需要 MEMBER 或更高权限");
        }
    }

    private void checkAssessmentAttachmentPermission(FileVO fileVO) {
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            log.warn("User not authenticated for ASSESSMENT_ATTACHMENT file download");
            throw new Forbidden("需要登录才能下载考题附件");
        }

        AssessmentQuestionVO question = fileDomainService.getQuestionByAttachmentId(fileVO.getId());
        if (question == null) {
            log.warn("No question found for assessment attachment: {}", fileVO.getId());
            throw new Forbidden("考题附件不存在");
        }

        // 获取考试时间信息并检查方向匹配
        AssessmentTimeVO assessmentTime = fileDomainService.getAssessmentTimeById(question.getAssessmentTimeId());
        if (currentUser.getDirection() == null || !currentUser.getDirection().equals(assessmentTime.getDirection())) {
            log.warn(
                    "User direction {} does not match assessment time direction {}",
                    currentUser.getDirection(),
                    assessmentTime.getDirection());
            throw new Forbidden("方向不匹配，无法下载考题附件");
        }
    }

    private void checkAvatarPermission(FileVO fileVO) {
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
