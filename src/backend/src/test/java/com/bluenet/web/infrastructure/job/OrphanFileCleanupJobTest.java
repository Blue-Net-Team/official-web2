package com.bluenet.web.infrastructure.job;

import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.infrastructure.storage.ObjectStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrphanFileCleanupJobTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private ObjectStorage objectStorage;

    private OrphanFileCleanupJob job;

    @BeforeEach
    void setUp() {
        job = new OrphanFileCleanupJob(fileRepository, objectStorage);
    }

    @Test
    @DisplayName("cleanup deletes orphan files from database and object storage")
    void cleanup_ShouldDeleteOrphanFiles() {
        File orphan1 = File.builder()
                .id(1L)
                .name("orphan1.jpg")
                .type(FileType.NORMAL_IMG)
                .status(FileStatus.ACTIVE)
                .build();
        File orphan2 = File.builder()
                .id(2L)
                .name("orphan2.jpg")
                .type(FileType.AVATAR)
                .status(FileStatus.REJECTED)
                .build();

        when(fileRepository.findOrphanFiles()).thenReturn(List.of(orphan1, orphan2));

        job.cleanup();

        verify(fileRepository).deleteFileById(1L);
        verify(fileRepository).deleteFileById(2L);
        verify(objectStorage).delete(FileType.NORMAL_IMG, "orphan1.jpg");
        verify(objectStorage).delete(FileType.AVATAR, "orphan2.jpg");
    }

    @Test
    @DisplayName("cleanup handles empty orphan file list")
    void cleanup_EmptyList_ShouldDoNothing() {
        when(fileRepository.findOrphanFiles()).thenReturn(List.of());

        job.cleanup();

        verify(fileRepository, never()).deleteFileById(anyLong());
        verify(objectStorage, never()).delete(any(), anyString());
    }

    @Test
    @DisplayName("cleanup continues when single file deletion fails")
    void cleanup_SingleFailure_ShouldContinue() {
        File orphan1 = File.builder()
                .id(1L)
                .name("orphan1.jpg")
                .type(FileType.NORMAL_IMG)
                .status(FileStatus.ACTIVE)
                .build();
        File orphan2 = File.builder()
                .id(2L)
                .name("orphan2.jpg")
                .type(FileType.AVATAR)
                .status(FileStatus.REJECTED)
                .build();

        when(fileRepository.findOrphanFiles()).thenReturn(List.of(orphan1, orphan2));
        doThrow(new RuntimeException("DB error")).when(fileRepository).deleteFileById(1L);

        job.cleanup();

        verify(fileRepository).deleteFileById(1L);
        verify(fileRepository).deleteFileById(2L);
        verify(objectStorage).delete(FileType.AVATAR, "orphan2.jpg");
        verify(objectStorage, never()).delete(FileType.NORMAL_IMG, "orphan1.jpg");
    }

    @Test
    @DisplayName("cleanup handles object storage deletion failure after db record removed")
    void cleanup_ObjectStorageFailure_ShouldLogAndContinue() {
        File orphan = File.builder()
                .id(1L)
                .name("orphan.jpg")
                .type(FileType.NORMAL_IMG)
                .status(FileStatus.ACTIVE)
                .build();

        when(fileRepository.findOrphanFiles()).thenReturn(List.of(orphan));
        doThrow(new RuntimeException("OSS error")).when(objectStorage).delete(FileType.NORMAL_IMG, "orphan.jpg");

        job.cleanup();

        verify(fileRepository).deleteFileById(1L);
        verify(objectStorage).delete(FileType.NORMAL_IMG, "orphan.jpg");
    }

    @Test
    @DisplayName("cleanup handles query failure gracefully")
    void cleanup_QueryFailure_ShouldReturnEarly() {
        when(fileRepository.findOrphanFiles()).thenThrow(new RuntimeException("Query failed"));

        job.cleanup();

        verify(fileRepository, never()).deleteFileById(anyLong());
        verify(objectStorage, never()).delete(any(), anyString());
    }
}
