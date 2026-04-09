package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.file.FileInfo;
import com.bluenet.web.domain.model.enumerate.ImageType;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * 上传头像（自动判断用户/报名身份）
     *
     * @param userId
     *            当前登录用户ID（可能为null，表示未登录的报名用户）
     * @param file
     *            上传的文件
     * @return 文件信息
     */
    FileInfo uploadAvatar(Long userId, MultipartFile file);

    /**
     * 更新用户头像
     *
     * @param userId
     *            当前用户ID
     * @param file
     *            上传的文件
     * @return 文件信息
     */
    FileInfo updateUserAvatar(Long userId, MultipartFile file);

    /**
     * 更新报名头像，返回文件信息，提交报名信息的时候需要带上这个文件信息
     *
     * @param file
     *            上传的文件
     * @return 文件信息
     */
    FileInfo updateEnrollAvatar(MultipartFile file);

    /**
     * 上传考题附件
     *
     * @param questionId
     *            题目ID
     * @param file
     *            上传的文件
     * @return 文件信息
     */
    FileInfo uploadAssessmentAttachment(Long questionId, MultipartFile file);

    /**
     * 上传考题作品
     *
     * @param questionId
     *            题目ID
     * @param file
     *            上传的文件
     * @return 文件信息
     */
    FileInfo uploadAssessmentWork(Long questionId, MultipartFile file);

    /**
     * 上传二维码
     *
     * @param qrcodeType
     *            二维码类型
     * @param file
     *            上传的文件
     * @return 文件信息
     */
    FileInfo uploadQrcode(String qrcodeType, MultipartFile file);

    /**
     * 上传介绍图片
     *
     * @param type
     *            图片类型
     * @param direction
     *            方向（已弃用，传 null）
     * @param description
     *            图片描述
     * @param file
     *            上传的文件
     * @return 文件信息
     */
    FileInfo uploadIntroduceImage(ImageType type, com.bluenet.web.domain.model.enumerate.Direction direction,
            String description, MultipartFile file);

    /**
     * 上传竞赛合照
     *
     * @param competitionId
     *            竞赛ID
     * @param description
     *            图片描述
     * @param file
     *            上传的文件
     * @return 文件信息
     */
    FileInfo uploadCompetitionImage(Long competitionId, String description, MultipartFile file);

    /**
     * 上传竞赛Logo
     *
     * @param competitionId
     *            竞赛ID
     * @param file
     *            上传的文件
     * @return 文件信息
     */
    FileInfo uploadCompetitionLogo(Long competitionId, MultipartFile file);
}
