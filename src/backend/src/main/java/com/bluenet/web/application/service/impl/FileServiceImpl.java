package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.file.FileInfo;
import com.bluenet.web.application.service.FileService;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.ImageType;
import com.bluenet.web.domain.model.enumerate.QrcodeType;
import com.bluenet.web.domain.model.vo.AssessmentAnswerVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.service.AssessmentAnswerDomainService;
import com.bluenet.web.domain.service.AssessmentQuestionDomainService;
import com.bluenet.web.domain.service.CompetitionDomainService;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.domain.service.IntroduceImageDomainService;
import com.bluenet.web.domain.service.QrcodeDomainService;
import com.bluenet.web.domain.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {
    private static final int MAX_IMAGES_PER_COMPETITION = 20;

    private final FileDomainService fileDomainService;
    private final UserDomainService userDomainService;
    private final AssessmentQuestionDomainService assessmentQuestionDomainService;
    private final AssessmentAnswerDomainService assessmentAnswerDomainService;
    private final QrcodeDomainService qrcodeDomainService;
    private final IntroduceImageDomainService introduceImageDomainService;
    private final CompetitionDomainService competitionDomainService;

    @Override
    @Transactional
    public FileInfo updateUserAvatar(Long userId, MultipartFile file) {
        FileVO fileVO = saveFile(file, FileType.AVATAR);

        userDomainService.updateUserAvatar(userId, fileVO);

        return convertToFileInfo(fileVO);
    }

    @Override
    public FileInfo updateEnrollAvatar(MultipartFile file) {
        FileVO fileVO = saveFile(file, FileType.AVATAR);
        return convertToFileInfo(fileVO);
    }

    @Override
    @Transactional
    public FileInfo uploadAssessmentAttachment(Long questionId, MultipartFile file) {
        AssessmentQuestionVO question = assessmentQuestionDomainService.getQuestionById(questionId);

        FileVO fileVO = saveFile(file, FileType.ASSESSMENT_ATTACHMENT);

        assessmentQuestionDomainService.updateAttachment(question, fileVO);

        return convertToFileInfo(fileVO);
    }

    @Override
    @Transactional
    public FileInfo uploadAssessmentWork(Long answerId, MultipartFile file) {
        AssessmentAnswerVO answer = assessmentAnswerDomainService.getAnswerById(answerId);

        FileVO fileVO = saveFile(file, FileType.WORK);

        assessmentAnswerDomainService.updateWorkFile(answer, fileVO);

        return convertToFileInfo(fileVO);
    }

    @Override
    @Transactional
    public FileInfo uploadQrcode(String qrcodeType, MultipartFile file) {
        QrcodeType type;
        try {
            type = QrcodeType.valueOf(qrcodeType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new GlobalException("无效的二维码类型: " + qrcodeType);
        }

        FileVO fileVO = saveFile(file, FileType.QRCODE);

        qrcodeDomainService.saveQrcode(fileVO, type);

        return convertToFileInfo(fileVO);
    }

    @Override
    @Transactional
    public FileInfo uploadIntroduceImage(ImageType type, Direction direction, String description, MultipartFile file) {
        // 参数验证：direction 仅在 type=DIRECTION 时有效
        if (direction != null && type != ImageType.DIRECTION) {
            throw new IllegalArgumentException("direction 参数仅在 type=DIRECTION 时有效");
        }

        FileVO fileVO = saveFile(file, FileType.NORMAL_IMG);

        introduceImageDomainService.addIntroduceImage(type, fileVO.getId(), direction, description);

        log.info("介绍图片上传成功，文件id: {}, 类型: {}", fileVO.getId(), type);
        return convertToFileInfo(fileVO);
    }

    @Override
    @Transactional
    public FileInfo uploadCompetitionImage(Long competitionId, String description, MultipartFile file) {
        // 校验竞赛存在性
        if (!competitionDomainService.existsById(competitionId)) {
            throw new DataNotFound("竞赛不存在");
        }

        // 校验图片数量限制
        int currentCount = introduceImageDomainService.countCompetitionImages(competitionId);
        if (currentCount >= MAX_IMAGES_PER_COMPETITION) {
            throw new DataConflict("竞赛图片数量已达上限（最多" + MAX_IMAGES_PER_COMPETITION + "张）");
        }

        FileVO fileVO = saveFile(file, FileType.NORMAL_IMG);

        introduceImageDomainService.addCompetitionImage(competitionId, fileVO.getId(), description);

        log.info("竞赛图片上传成功，文件id: {}, 竞赛id: {}", fileVO.getId(), competitionId);
        return convertToFileInfo(fileVO);
    }

    @Override
    @Transactional
    public FileInfo uploadCompetitionLogo(Long competitionId, MultipartFile file) {
        // 校验竞赛存在性
        if (!competitionDomainService.existsById(competitionId)) {
            throw new DataNotFound("竞赛不存在");
        }

        FileVO fileVO = saveFile(file, FileType.NORMAL_IMG);

        competitionDomainService.updateLogo(competitionId, fileVO.getId());

        log.info("竞赛Logo上传成功，文件id: {}, 竞赛id: {}", fileVO.getId(), competitionId);
        return convertToFileInfo(fileVO);
    }

    @NotNull
    private FileVO saveFile(MultipartFile file, FileType fileType) {
        String filename = fileDomainService.generateFilename(fileType, file.getOriginalFilename());
        FileVO fileVO;
        try {
            fileVO = fileDomainService.saveFile(fileType, filename, file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + filename, e);
        }
        log.info("文件保存成功，文件id {}", fileVO.getId());
        return fileVO;
    }

    private FileInfo convertToFileInfo(FileVO fileVO) {
        return FileInfo.builder()
                .id(fileVO.getId())
                .url(fileVO.getUrl())
                .type(fileVO.getType())
                .name(fileVO.getName())
                .build();
    }
}
