package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.infrastructure.repository.dataobject.FileDO;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.testsupport.fixture.FileFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FileRepositoryImpl 集成测试。
 */
@DisplayName("FileRepositoryImpl 集成测试")
class FileRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileMapper fileMapper;

    private final AtomicLong counter = new AtomicLong(1);

    private String nextFileName() {
        return "test-file-" + counter.getAndIncrement() + ".txt";
    }

    private File createFileForUpload(String fileName) {
        File file = File.create(fileName, FileType.WORK);
        file.setUrl("http://localhost/test/" + file.getName());
        return file;
    }

    private File createMetadata(FileType type) {
        File file = File.create(nextFileName(), type);
        file.setUrl("http://localhost/test/" + file.getName());
        return fileRepository.save(file);
    }

    @Test
    @DisplayName("save: 新文件元数据应插入并回写ID")
    void save_newFile_shouldInsertAndReturnId() {
        File file = createMetadata(FileType.NORMAL_IMG);

        assertThat(file.getId()).isNotNull();
        FileDO dataObject = fileMapper.selectById(file.getId());
        assertThat(dataObject).isNotNull();
        assertThat(dataObject.getName()).isEqualTo(file.getName());
        assertThat(dataObject.getType()).isEqualTo(FileType.NORMAL_IMG);
    }

    @Test
    @DisplayName("save: 已有文件元数据应更新状态")
    void save_existingFile_shouldUpdateStatus() {
        File file = createMetadata(FileType.NORMAL_IMG);
        File activeFile = FileFixture.withStatus(file, FileStatus.ACTIVE);

        fileRepository.save(activeFile);

        FileDO updated = fileMapper.selectById(file.getId());
        assertThat(updated.getStatus()).isEqualTo(FileStatus.ACTIVE);
    }

    @Test
    @DisplayName("saveFileMetadata: 应仅保存元数据")
    void saveFileMetadata_shouldSaveMetadataOnly() {
        File file = File.create(nextFileName(), FileType.WORK);
        file.setUrl("http://localhost/test/" + file.getName());

        File saved = fileRepository.saveFileMetadata(file);

        assertThat(saved.getId()).isNotNull();
        FileDO dataObject = fileMapper.selectById(saved.getId());
        assertThat(dataObject.getName()).isEqualTo(file.getName());
    }

    @Test
    @DisplayName("saveFile: 应保存文件元数据并上传对象存储")
    void saveFile_shouldUploadAndSaveMetadata() {
        String fileName = nextFileName();
        File file = createFileForUpload(fileName);
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes(StandardCharsets.UTF_8));

        File saved = fileRepository.saveFile(inputStream, file);

        assertThat(saved.getId()).isNotNull();
        FileDO dataObject = fileMapper.selectById(saved.getId());
        assertThat(dataObject.getName()).isEqualTo(fileName);
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        File file = createMetadata(FileType.AVATAR);

        Optional<File> found = fileRepository.findById(file.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(file.getName());

        assertThat(fileRepository.findById(-1L)).isEmpty();
    }

    @Test
    @DisplayName("loadFile: 应从对象存储加载资源")
    void loadFile_shouldLoadResource() {
        String fileName = nextFileName();
        File file = createFileForUpload(fileName);
        InputStream inputStream = new ByteArrayInputStream("loadable content".getBytes(StandardCharsets.UTF_8));
        fileRepository.saveFile(inputStream, file);

        org.springframework.core.io.Resource resource = fileRepository.loadFile(fileName, FileType.WORK);

        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
    }

    @Test
    @DisplayName("deleteFileById: 应删除元数据和对象存储内容")
    void deleteFileById_shouldRemoveFile() {
        String fileName = nextFileName();
        File file = createFileForUpload(fileName);
        InputStream inputStream = new ByteArrayInputStream("deletable content".getBytes(StandardCharsets.UTF_8));
        File saved = fileRepository.saveFile(inputStream, file);
        Long fileId = saved.getId();

        fileRepository.deleteFileById(fileId);

        assertThat(fileMapper.selectById(fileId)).isNull();
    }

    @Test
    @DisplayName("findOrphanFiles: 应查询待上传超时的孤儿文件")
    void findOrphanFiles_shouldReturnOrphanFiles() {
        FileDO orphanFile = new FileDO();
        orphanFile.setName(nextFileName());
        orphanFile.setType(FileType.WORK);
        orphanFile.setUrl("http://localhost/test/" + orphanFile.getName());
        orphanFile.setStatus(FileStatus.PENDING);
        orphanFile.setCreatedAt(LocalDateTime.now().minus(120, ChronoUnit.MINUTES));
        fileMapper.insert(orphanFile);

        List<File> orphanFiles = fileRepository.findOrphanFiles();

        assertThat(orphanFiles).anyMatch(f -> f.getId().equals(orphanFile.getId()));
    }

    @Test
    @DisplayName("findLatestByType: 多条同类型 ACTIVE 记录应返回主键最大的一条")
    void findLatestByType_multipleActive_shouldReturnMaxId() {
        File first = createMetadata(FileType.ENROLL_FORM);
        fileRepository.save(FileFixture.withStatus(first, FileStatus.ACTIVE));
        File second = createMetadata(FileType.ENROLL_FORM);
        fileRepository.save(FileFixture.withStatus(second, FileStatus.ACTIVE));

        Optional<File> latest = fileRepository.findLatestByType(FileType.ENROLL_FORM);

        assertThat(latest).isPresent();
        assertThat(latest.get().getId()).isEqualTo(second.getId());
    }

    @Test
    @DisplayName("findLatestByType: PENDING 与 REJECTED 记录不应返回")
    void findLatestByType_nonActive_shouldNotReturn() {
        File pending = createMetadata(FileType.ENROLL_FORM);
        fileRepository.save(FileFixture.withStatus(pending, FileStatus.PENDING));
        File rejected = createMetadata(FileType.ENROLL_FORM);
        fileRepository.save(FileFixture.withStatus(rejected, FileStatus.REJECTED));

        Optional<File> latest = fileRepository.findLatestByType(FileType.ENROLL_FORM);

        assertThat(latest).isEmpty();
    }

    @Test
    @DisplayName("findLatestByType: 其他类型记录不应返回，无记录时返回空")
    void findLatestByType_otherTypeAndEmpty_shouldReturnEmpty() {
        File other = createMetadata(FileType.NORMAL_IMG);
        fileRepository.save(FileFixture.withStatus(other, FileStatus.ACTIVE));

        assertThat(fileRepository.findLatestByType(FileType.ENROLL_FORM)).isEmpty();
    }

    @Test
    @DisplayName("findLatestByTypeExcludingId: 应返回排除指定主键后的最新 ACTIVE 记录")
    void findLatestByTypeExcludingId_shouldReturnLatestExcludingId() {
        File first = createMetadata(FileType.ENROLL_FORM);
        fileRepository.save(FileFixture.withStatus(first, FileStatus.ACTIVE));
        File second = createMetadata(FileType.ENROLL_FORM);
        fileRepository.save(FileFixture.withStatus(second, FileStatus.ACTIVE));

        Optional<File> latest = fileRepository.findLatestByTypeExcludingId(
                FileType.ENROLL_FORM,
                second.getId());

        assertThat(latest).isPresent();
        assertThat(latest.get().getId()).isEqualTo(first.getId());
        assertThat(fileRepository.findLatestByTypeExcludingId(FileType.ENROLL_FORM, first.getId()))
                .isPresent()
                .get()
                .extracting(File::getId)
                .isEqualTo(second.getId());
    }
}
