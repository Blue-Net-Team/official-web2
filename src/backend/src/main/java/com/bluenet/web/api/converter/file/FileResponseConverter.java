package com.bluenet.web.api.converter.file;

import com.bluenet.web.api.dto.file.ConfirmUploadResponse;
import com.bluenet.web.api.dto.file.FileInfo;
import com.bluenet.web.api.dto.file.PrepareUploadResponse;
import com.bluenet.web.application.ConfirmUploadResult;
import com.bluenet.web.application.FileResult;
import com.bluenet.web.application.PresignedUploadResult;
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
                .status(result.status())
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

    /**
     * 将预签名上传准备结果转换为 API 响应 DTO
     */
    public PrepareUploadResponse toPrepareUploadDTO(PresignedUploadResult result) {
        return PrepareUploadResponse.builder()
                .fileId(result.fileId())
                .uploadUrl(result.uploadUrl())
                .callbackToken(result.callbackToken())
                .filename(result.filename())
                .type(result.type())
                .build();
    }

    /**
     * 将预签名上传确认结果转换为 API 响应 DTO
     */
    public ConfirmUploadResponse toConfirmUploadDTO(ConfirmUploadResult result) {
        return ConfirmUploadResponse.builder()
                .fileId(result.fileId())
                .filename(result.filename())
                .type(result.type())
                .status(result.status())
                .build();
    }
}
