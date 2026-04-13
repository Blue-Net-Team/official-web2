package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.file.FileInfo;
import com.bluenet.web.application.service.FileService;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.service.FileDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件上传服务实现
 * <p>
 * 纯粹的文件存储操作，仅依赖 FileDomainService。 不涉及任何业务逻辑（用户头像更新、二维码创建等）。
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    private final FileDomainService fileDomainService;

    @Override
    public FileInfo uploadFile(MultipartFile file, FileType type) {
        String filename = fileDomainService.generateFilename(type, file.getOriginalFilename());
        FileVO fileVO = saveFile(type, filename, file);
        log.info("文件上传成功，文件id: {}, 类型: {}", fileVO.getId(), type);
        return convertToFileInfo(fileVO);
    }

    @NotNull
    private FileVO saveFile(FileType type, String filename, MultipartFile file) {
        try {
            return fileDomainService.saveFile(type, filename, file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + filename, e);
        }
    }

    private FileInfo convertToFileInfo(FileVO fileVO) {
        return FileInfo.builder()
                .id(fileVO.getId())
                .url(fileVO.getUrl())
                .type(fileVO.getType())
                .name(fileVO.getName())
                .build();
    }
}
