package com.bluenet.web.api.converter.file;

import com.bluenet.web.api.dto.file.FileInfo;
import com.bluenet.web.application.FileResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文件响应转换器
 * <p>
 * 负责将应用层结果转换为 API 响应 DTO
 * </p>
 */
@Component
public class FileResponseConverter {

    /**
     * 将应用层结果转换为 API 响应 DTO
     */
    public FileInfo toDTO(FileResult result) {
        return FileInfo.builder()
                .id(result.id())
                .name(result.name())
                .type(result.type())
                .url(result.url())
                .build();
    }

    /**
     * 将应用层结果列表转换为 API 响应 DTO 列表
     */
    public List<FileInfo> toDTOList(List<FileResult> results) {
        return results.stream()
                .map(this::toDTO)
                .toList();
    }
}
