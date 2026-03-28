package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.AssessmentAnswerVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import com.bluenet.web.domain.model.vo.FileVO;
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
    AssessmentAnswerVO getAnswerByFileId(Long fileId);

    /**
     * 根据题目的附件ID获取题目信息
     *
     * @param attachmentId
     *            附件ID
     * @return 题目VO
     */
    AssessmentQuestionVO getQuestionByAttachmentId(Long attachmentId);

    /**
     * 根据ID获取考试时间信息
     *
     * @param id
     *            考试时间ID
     * @return 考试时间VO
     */
    AssessmentTimeVO getAssessmentTimeById(Long id);

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
}
