package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.vo.ConfirmUploadVO;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.PresignedUploadVO;
import org.springframework.core.io.Resource;

import java.io.InputStream;

public interface FileDomainService {
    /**
     * 根据文件ID获取文件信息
     *
     * @param fileId
     *            文件ID
     * @return 文件VO
     */
    FileVO getFileById(Long fileId);

    /**
     * 根据文件的答题ID获取答题信息
     *
     * @param fileId
     *            文件ID
     * @return 答题VO
     */
    AssessmentAnswer getAnswerByFileId(Long fileId);

    /**
     * 根据题目的附件ID获取题目信息
     *
     * @param attachmentId
     *            附件ID
     * @return 题目VO
     */
    AssessmentQuestion getQuestionByAttachmentId(Long attachmentId);

    /**
     * 根据ID获取考试时间信息
     *
     * @param id
     *            考试时间ID
     * @return 考试时间VO
     */
    AssessmentTime getAssessmentTimeById(Long id);

    /**
     * 生成唯一文件名
     *
     * @param fileType
     *            文件类型
     * @param fileExtension
     *            文件扩展名（不带点，例如 "jpg"、"pdf"）
     * @return 生成的文件名，包含扩展名
     */
    String generateFilename(FileType fileType, String fileExtension);

    /**
     * 保存文件
     *
     * @param fileType
     *            文件类型枚举
     * @param filename
     *            文件名（包含扩展名）
     * @param inputStream
     *            文件输入流
     * @return 保存后的文件信息，包括文件ID、URL等
     */
    FileVO saveFile(FileType fileType, String filename, InputStream inputStream);

    /**
     * 加载文件
     *
     * @param fileType
     *            文件类型枚举
     * @param filename
     *            文件名（包含扩展名）
     * @return 文件资源，调用方可以通过 Resource 获取输入流等信息
     */
    Resource loadFile(FileType fileType, String filename);

    /**
     * 预签名上传准备。
     *
     * @param fileType
     *            文件类型
     * @param originalFilename
     *            原始文件名
     * @param contentType
     *            文件 Content-Type
     * @param size
     *            文件大小
     * @return 准备结果，包含文件 ID、上传 URL 和回调令牌
     */
    PresignedUploadVO prepareUpload(FileType fileType, String originalFilename, String contentType, long size);

    /**
     * 预签名上传确认。
     *
     * @param fileId
     *            文件 ID
     * @param callbackToken
     *            回调令牌
     * @param expectedMd5
     *            预期的 MD5
     * @param expectedSize
     *            预期的大小
     * @return 确认结果
     */
    ConfirmUploadVO confirmUpload(Long fileId, String callbackToken, String expectedMd5, long expectedSize);

    /**
     * 生成预签名下载 URL。
     *
     * @param fileType
     *            文件类型
     * @param filename
     *            文件名
     * @return 预签名 GET URL
     */
    String getPresignedDownloadUrl(FileType fileType, String filename);

    /**
     * 校验当前用户是否有指定文件的下载权限。
     *
     * @param fileVO
     *            文件 VO
     * @param currentUser
     *            当前用户实体（可为 null，表示未登录）
     * @throws com.bluenet.web.domain.exception.Forbidden
     *             权限不足时抛出
     */
    void checkDownloadPermission(FileVO fileVO, User currentUser);
}
