package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.infrastructure.repository.converter.FileRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.FileDO;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.infrastructure.storage.ObjectStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * 文件仓储实现类。
 * <p>
 * 只负责文件元数据和对象存储，考核答案、题目、时间等业务关联查询由对应考核仓储承接。
 * </p>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
@ConditionalOnBean(ObjectStorage.class)
public class FileRepositoryImpl implements FileRepository {

    private final ObjectStorage objectStorage;
    private final FileMapper fileMapper;
    private final FileRepositoryConverter converter;

    /**
     * 按主键查询文件 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的文件 结果；不存在时为空。
     */
    @Override
    public Optional<File> findById(Long id) {
        FileDO dataObject = fileMapper.selectById(id);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    /**
     * 处理文件 仓储职责中的业务数据访问逻辑。
     *
     * @param inputStream
     *            待保存文件的输入流。
     * @param file
     *            文件领域对象。
     * @return 保存后的文件领域对象。
     */
    @Override
    @Transactional
    public File saveFile(InputStream inputStream, File file) {
        validateParameters(file.getName(), inputStream, file.getType());

        FileDO dataObject = converter.toDataObject(file);
        fileMapper.insert(dataObject);
        file.setId(dataObject.getId());
        objectStorage.put(file.getType(), file.getName(), inputStream);
        log.debug("File metadata and object saved successfully: id={}, type={}", file.getId(), file.getType());

        return file;
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
    @Override
    public Resource loadFile(String filename, FileType fileType) {
        validateParameters(filename, fileType);
        return objectStorage.get(fileType, filename);
    }

    /**
     * 按文件名和类型删除文件元数据和对象存储内容。
     *
     * @param filename
     *            对象存储中的文件名。
     * @param fileType
     *            文件业务类型。
     */
    @Override
    @Transactional
    public void deleteFile(String filename, FileType fileType) {
        // 查找对应的文件元数据，避免只删除对象后留下孤立数据库记录。
        Optional<File> fileOp = Optional.ofNullable(
                converter.toEntity(fileMapper.selectByNameAndType(filename, fileType)));
        if (fileOp.isEmpty()) {
            log.warn("File not found in database for deletion: {} ({})", filename, fileType);
            throw new DataNotFound("File not found for deletion: " + filename);
        }
        int influencedRows = fileMapper.deleteById(fileOp.get().getId());
        if (influencedRows == 0) {
            log.warn("Failed to delete file record from database: {} ({})", filename, fileType);
            throw new RuntimeException("Failed to delete file record: " + filename);
        }
        // 删除数据库记录后再删除对象存储中的文件。
        objectStorage.delete(fileType, filename);
    }

    /**
     * 仅保存文件元数据，不操作对象存储。
     *
     * @param file
     *            文件领域对象
     * @return 保存后的文件领域对象
     */
    @Override
    @Transactional
    public File saveFileMetadata(File file) {
        FileDO dataObject = converter.toDataObject(file);
        fileMapper.insert(dataObject);
        file.setId(dataObject.getId());
        log.debug("File metadata saved successfully: id={}, type={}", file.getId(), file.getType());
        return file;
    }

    @Override
    @Transactional
    public void updateFileMetadata(File file) {
        FileDO dataObject = converter.toDataObject(file);
        fileMapper.updateById(dataObject);
        log.debug("File metadata updated successfully: id={}, status={}", file.getId(), file.getStatus());
    }

    @Override
    public List<File> findOrphanFiles() {
        LocalDateTime pendingThreshold = LocalDateTime.now().minus(75, ChronoUnit.MINUTES);
        List<FileDO> orphanFiles = fileMapper.selectOrphanFiles(pendingThreshold);
        return converter.toEntityList(orphanFiles);
    }

    /**
     * 按文件主键删除文件元数据和对象存储内容。
     *
     * @param id
     *            业务记录主键。
     */
    @Override
    @Transactional
    public void deleteFileById(Long id) {
        // 查找对应的文件元数据，拿到对象存储删除所需的文件类型和文件名。
        File file = converter.toEntity(fileMapper.selectById(id));
        if (file == null) {
            log.warn("File not found in database for deletion: id={}", id);
            throw new DataNotFound("File not found for deletion, id: " + id);
        }
        int influencedRows = fileMapper.deleteById(id);
        if (influencedRows == 0) {
            log.warn("Failed to delete file record from database: id={}", id);
            throw new RuntimeException("Failed to delete file record, id: " + id);
        }
        // 删除数据库记录后再删除对象存储中的文件。
        objectStorage.delete(file.getType(), file.getName());
    }

    /**
     * 校验文件仓储操作所需参数。
     *
     * @param filename
     *            对象存储中的文件名。
     * @param inputStream
     *            待保存文件的输入流。
     * @param fileType
     *            文件业务类型。
     */
    private void validateParameters(String filename, InputStream inputStream, FileType fileType) {
        validateParameters(filename, fileType);
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
    }

    /**
     * 校验文件仓储操作所需参数。
     *
     * @param filename
     *            对象存储中的文件名。
     * @param fileType
     *            文件业务类型。
     */
    private void validateParameters(String filename, FileType fileType) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }
        if (fileType == null) {
            throw new IllegalArgumentException("FileType cannot be null");
        }
    }

}
