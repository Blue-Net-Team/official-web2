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
     * 按主键查询文件 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的文件 结果；不存在时为空。
     */
    Optional<FileVO> findById(Long id);

    /**
     * 查询符合条件的文件 记录。
     *
     * @param fileId
     *            文件主键。
     * @return 查询到的文件 结果；不存在时为空。
     */
    Optional<AssessmentAnswerVO> findAnswerByFileId(Long fileId);

    /**
     * 查询符合条件的文件 记录。
     *
     * @param attachmentId
     *            附件文件主键。
     * @return 查询到的文件 结果；不存在时为空。
     */
    Optional<AssessmentQuestionVO> findQuestionByAttachmentId(Long attachmentId);

    /**
     * 查询符合条件的文件 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询或处理得到的文件 结果。
     */
    AssessmentTimeVO findTimeById(Long id);

    /**
     * 处理文件 仓储职责中的业务数据访问逻辑。
     *
     * @param inputStream
     *            待保存文件的输入流。
     * @param file
     *            文件领域对象或文件视图对象。
     * @return 查询或处理得到的文件 结果。
     */
    FileVO saveFile(InputStream inputStream, File file);

    /**
     * 处理文件 仓储职责中的业务数据访问逻辑。
     *
     * @param resource
     *            文件下载资源句柄。
     * @param file
     *            文件领域对象或文件视图对象。
     * @return 查询或处理得到的文件 结果。
     */
    default FileVO saveFile(Resource resource, File file) {
        try (InputStream inputStream = resource.getInputStream()) {
            return saveFile(inputStream, file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save file: " + file.getName(), e);
        }
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
    Resource loadFile(String filename, FileType fileType);

    /**
     * 从对象存储加载指定文件资源。
     *
     * @param file
     *            文件领域对象或文件视图对象。
     * @return 查询或处理得到的文件 结果。
     */
    default Resource loadFile(File file) {
        return loadFile(file.getName(), file.getType());
    }

    /**
     * 按文件名和类型删除文件元数据和对象存储内容。
     *
     * @param filename
     *            对象存储中的文件名。
     * @param fileType
     *            文件业务类型。
     */
    void deleteFile(String filename, FileType fileType);

    /**
     * 按文件名和类型删除文件元数据和对象存储内容。
     *
     * @param file
     *            文件领域对象或文件视图对象。
     */
    default void deleteFile(File file) {
        deleteFile(file.getName(), file.getType());
    }

    /**
     * 按文件主键删除文件元数据和对象存储内容。
     *
     * @param id
     *            业务记录主键。
     */
    void deleteFileById(Long id);
}
