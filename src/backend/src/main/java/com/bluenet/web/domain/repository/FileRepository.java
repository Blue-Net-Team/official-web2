package com.bluenet.web.domain.repository;

import java.io.InputStream;
import java.util.Optional;

import org.springframework.core.io.Resource;

import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.AssessmentAnswerVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import com.bluenet.web.domain.model.vo.FileVO;

public interface FileRepository {
    /**
     * 根据ID查询文件
     *
     * @param id
     *            文件ID
     * @return 文件VO
     */
    Optional<FileVO> findById(Long id);

    /**
     * 根据答题的文件ID查询答题
     *
     * @param fileId
     *            文件ID
     * @return 答题VO
     */
    Optional<AssessmentAnswerVO> findAnswerByFileId(Long fileId);

    /**
     * 根据题目的附件ID查询题目
     *
     * @param attachmentId
     *            附件ID
     * @return 题目VO
     */
    Optional<AssessmentQuestionVO> findQuestionByAttachmentId(Long attachmentId);

    /**
     * 根据ID查询考试时间
     *
     * @param id
     *            考试时间ID
     * @return 考试时间VO
     */
    AssessmentTimeVO findTimeById(Long id);

    /**
     * 保存文件
     *
     * @param inputStream
     *            文件输入流
     * @param file
     *            文件实体，包含文件名和文件类型等信息
     */
    FileVO saveFile(InputStream inputStream, File file);

    /**
     * 保存文件（重载方法，支持直接使用 Resource）
     *
     * @param resource
     *            文件资源
     * @param file
     *            文件实体
     */
    default FileVO saveFile(Resource resource, File file) {
        try (InputStream inputStream = resource.getInputStream()) {
            return saveFile(inputStream, file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save file: " + file.getName(), e);
        }
    }

    /**
     * 加载文件
     *
     * @param filename
     *            文件名
     * @param fileType
     *            文件类型
     * @return 文件资源
     */
    Resource loadFile(String filename, FileType fileType);

    /**
     * 加载文件（重载方法，支持直接使用 File 实体）
     *
     * @param file
     *            文件实体
     * @return 文件资源
     */
    default Resource loadFile(File file) {
        return loadFile(file.getName(), file.getType());
    }

    /**
     * 删除文件
     *
     * @param filename
     *            文件名
     * @param fileType
     *            文件类型
     */
    void deleteFile(String filename, FileType fileType);

    /**
     * 删除文件（重载方法，支持直接使用 File 实体）
     *
     * @param file
     *            文件实体
     */
    default void deleteFile(File file) {
        deleteFile(file.getName(), file.getType());
    }

    /**
     * 根据文件ID删除文件（包括数据库记录和存储对象）
     *
     * @param id
     *            文件ID
     */
    void deleteFileById(Long id);
}
