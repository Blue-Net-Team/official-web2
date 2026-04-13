package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.file.FileInfo;
import com.bluenet.web.domain.model.enumerate.FileType;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务
 * <p>
 * 纯粹的文件存储操作，不涉及任何业务逻辑。 文件上传后返回 FileInfo，业务关联由各自领域的接口负责。
 * </p>
 */
public interface FileService {
    /**
     * 统一文件上传
     *
     * @param file
     *            上传的文件
     * @param type
     *            文件类型
     * @return 文件信息（包含 fileId、文件名、类型）
     */
    FileInfo uploadFile(MultipartFile file, FileType type);
}
