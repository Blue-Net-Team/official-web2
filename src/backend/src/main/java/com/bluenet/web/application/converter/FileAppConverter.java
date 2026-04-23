package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.file.FileInfo;
import com.bluenet.web.application.FileResult;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文件应用层转换器
 * <p>
 * 将应用层 Result 与 API 层 DTO 之间做转换
 * </p>
 */
@Component
public class FileAppConverter {

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

    /**
     * 根据文件名确定 Content-Type
     */
    public MediaType determineMediaType(String filename) {
        String lowerFilename = filename.toLowerCase();

        if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        } else if (lowerFilename.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (lowerFilename.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        } else if (lowerFilename.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        } else if (lowerFilename.endsWith(".svg")) {
            return MediaType.parseMediaType("image/svg+xml");
        } else if (lowerFilename.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF;
        } else if (lowerFilename.endsWith(".doc") || lowerFilename.endsWith(".docx")) {
            return MediaType.parseMediaType("application/msword");
        } else if (lowerFilename.endsWith(".xls") || lowerFilename.endsWith(".xlsx")) {
            return MediaType.parseMediaType("application/vnd.ms-excel");
        } else if (lowerFilename.endsWith(".ppt") || lowerFilename.endsWith(".pptx")) {
            return MediaType.parseMediaType("application/vnd.ms-powerpoint");
        } else if (lowerFilename.endsWith(".txt")) {
            return MediaType.TEXT_PLAIN;
        } else if (lowerFilename.endsWith(".zip")) {
            return MediaType.parseMediaType("application/zip");
        } else if (lowerFilename.endsWith(".rar")) {
            return MediaType.parseMediaType("application/vnd.rar");
        } else if (lowerFilename.endsWith(".7z")) {
            return MediaType.parseMediaType("application/x-7z-compressed");
        } else if (lowerFilename.endsWith(".mp4")) {
            return MediaType.parseMediaType("video/mp4");
        } else if (lowerFilename.endsWith(".mp3")) {
            return MediaType.parseMediaType("audio/mpeg");
        } else if (lowerFilename.endsWith(".wav")) {
            return MediaType.parseMediaType("audio/wav");
        } else {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
