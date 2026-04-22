package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.infrastructure.repository.dataobject.FileDO;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.infrastructure.storage.ObjectStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileRepositoryImplTest {

    @Mock
    private ObjectStorage objectStorage;

    @Mock
    private FileMapper fileMapper;

    private FileRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        // 文件仓储已收敛为文件元数据 + 对象存储边界，测试只注入这两个依赖。
        repository = new FileRepositoryImpl(objectStorage, fileMapper);
    }

    @Test
    @DisplayName("saveFile persists metadata and delegates object storage")
    void saveFile_ShouldPersistMetadataAndDelegateObjectStorage() {
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
        File file = File.builder().name("test.jpg").type(FileType.NORMAL_IMG).build();

        repository.saveFile(inputStream, file);

        verify(fileMapper).insert(org.mockito.ArgumentMatchers.any(FileDO.class));
        verify(objectStorage).put(FileType.NORMAL_IMG, "test.jpg", inputStream);
    }

    @Test
    @DisplayName("loadFile delegates object storage")
    void loadFile_ShouldDelegateObjectStorage() {
        repository.loadFile("avatar.jpg", FileType.AVATAR);

        verify(objectStorage).get(FileType.AVATAR, "avatar.jpg");
    }

    @Test
    @DisplayName("saveFile rejects empty filename")
    void saveFile_EmptyFilename_ShouldThrowException() {
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        File file = File.builder().name("").type(FileType.AVATAR).build();

        assertThatThrownBy(() -> repository.saveFile(inputStream, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Filename cannot be null or empty");
    }

    @Test
    @DisplayName("saveFile rejects null input stream")
    void saveFile_NullInputStream_ShouldThrowException() {
        File file = File.builder().name("test.jpg").type(FileType.AVATAR).build();

        assertThatThrownBy(() -> repository.saveFile((InputStream) null, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("InputStream cannot be null");
    }

    @Test
    @DisplayName("saveFile rejects null file type")
    void saveFile_NullFileType_ShouldThrowException() {
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        File file = File.builder().name("test.jpg").type(null).build();

        assertThatThrownBy(() -> repository.saveFile(inputStream, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FileType cannot be null");
    }
}
