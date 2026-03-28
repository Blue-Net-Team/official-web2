package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.core.io.Resource;

/**
 * 文件下载值对象
 */
@Getter
@Builder
@AllArgsConstructor
public class FileDownloadVO {
    /**
     * 文件名
     */
    private String filename;

    /**
     * 文件类型
     */
    private FileType fileType;

    /**
     * 文件资源
     */
    private Resource resource;

    /**
     * Content-Type
     */
    private String contentType;
}
