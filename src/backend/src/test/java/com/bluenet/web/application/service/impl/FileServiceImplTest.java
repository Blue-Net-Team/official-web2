package com.bluenet.web.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.DataNotFound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.bluenet.web.api.dto.file.FileInfo;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.ImageType;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.service.CompetitionDomainService;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.domain.service.IntroduceImageDomainService;

/**
 * FileServiceImpl 单元测试
 * <p>
 * 测试介绍图片上传接口相关的业务逻辑
 * </p>
 */
@DisplayName("FileServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private FileDomainService fileDomainService;

    @Mock
    private IntroduceImageDomainService introduceImageDomainService;

    @Mock
    private CompetitionDomainService competitionDomainService;

    @InjectMocks
    private FileServiceImpl fileService;

    private static final Long TEST_FILE_ID = 100L;
    private static final String TEST_FILE_NAME = "test.jpg";
    private static final String TEST_FILE_URL = "http://example.com/test.jpg";
    private static final Long TEST_COMPETITION_ID = 1L;
    private static final String TEST_DESCRIPTION = "测试图片描述";

    private MultipartFile mockFile;
    private FileVO testFileVO;

    @BeforeEach
    void setUp() throws IOException {
        // 创建模拟的MultipartFile
        mockFile = mock(MultipartFile.class);
        // 使用lenient()放宽stubbing限制，因为某些测试用例不会调用这些方法
        lenient().when(mockFile.getOriginalFilename()).thenReturn(TEST_FILE_NAME);
        lenient().when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));

        // 创建测试用的FileVO
        testFileVO = FileVO.builder()
                .id(TEST_FILE_ID)
                .name(TEST_FILE_NAME)
                .url(TEST_FILE_URL)
                .type(FileType.NORMAL_IMG)
                .build();
    }

    // ==================== uploadIntroduceImage 测试 ====================

    @Nested
    @DisplayName("uploadIntroduceImage 方法测试")
    class UploadIntroduceImageTests {

        /**
         * 上传竞赛介绍图片：应成功上传并返回文件信息
         */
        @Test
        @DisplayName("上传竞赛介绍图片：应成功上传并返回文件信息")
        void uploadIntroduceImage_competitionType_shouldUploadSuccessfully() throws IOException {
            // 准备
            when(fileDomainService.generateFilename(eq(FileType.NORMAL_IMG), any())).thenReturn(TEST_FILE_NAME);
            when(fileDomainService.saveFile(eq(FileType.NORMAL_IMG), any(), any())).thenReturn(testFileVO);
            when(introduceImageDomainService.addIntroduceImage(eq(ImageType.COMPETITION), eq(TEST_FILE_ID), isNull()))
                    .thenReturn(1L);

            // 执行
            FileInfo result = fileService.uploadIntroduceImage(ImageType.COMPETITION, null, null, mockFile);

            // 验证
            assertNotNull(result);
            assertEquals(TEST_FILE_ID, result.getId());
            assertEquals(TEST_FILE_NAME, result.getName());
            assertEquals(FileType.NORMAL_IMG, result.getType());
            assertEquals(TEST_FILE_URL, result.getUrl());

            verify(fileDomainService).saveFile(eq(FileType.NORMAL_IMG), any(), any());
            verify(introduceImageDomainService).addIntroduceImage(ImageType.COMPETITION, TEST_FILE_ID, null);
        }

        /**
         * 上传介绍图片带描述：应成功上传并保存描述
         */
        @Test
        @DisplayName("上传介绍图片带描述：应成功上传并保存描述")
        void uploadIntroduceImage_withDescription_shouldUploadWithDescription() throws IOException {
            // 准备
            when(fileDomainService.generateFilename(eq(FileType.NORMAL_IMG), any())).thenReturn(TEST_FILE_NAME);
            when(fileDomainService.saveFile(eq(FileType.NORMAL_IMG), any(), any())).thenReturn(testFileVO);
            when(
                    introduceImageDomainService.addIntroduceImage(
                            eq(ImageType.COMPETITION),
                            eq(TEST_FILE_ID),
                            eq(TEST_DESCRIPTION)))
                                    .thenReturn(1L);

            // 执行
            FileInfo result = fileService.uploadIntroduceImage(ImageType.COMPETITION, null, TEST_DESCRIPTION, mockFile);

            // 验证
            assertNotNull(result);
            assertEquals(TEST_FILE_ID, result.getId());

            verify(introduceImageDomainService)
                    .addIntroduceImage(ImageType.COMPETITION, TEST_FILE_ID, TEST_DESCRIPTION);
        }
    }

    // ==================== uploadCompetitionImage 测试 ====================

    @Nested
    @DisplayName("uploadCompetitionImage 方法测试")
    class UploadCompetitionImageTests {

        /**
         * 上传竞赛图片：应成功上传并返回文件信息
         */
        @Test
        @DisplayName("上传竞赛图片：应成功上传并返回文件信息")
        void uploadCompetitionImage_validCompetition_shouldUploadSuccessfully() throws IOException {
            // 准备
            when(competitionDomainService.existsById(TEST_COMPETITION_ID)).thenReturn(true);
            when(introduceImageDomainService.countCompetitionImages(TEST_COMPETITION_ID)).thenReturn(0);
            when(fileDomainService.generateFilename(eq(FileType.NORMAL_IMG), any())).thenReturn(TEST_FILE_NAME);
            when(fileDomainService.saveFile(eq(FileType.NORMAL_IMG), any(), any())).thenReturn(testFileVO);
            when(introduceImageDomainService.addCompetitionImage(eq(TEST_COMPETITION_ID), eq(TEST_FILE_ID), isNull()))
                    .thenReturn(1L);

            // 执行
            FileInfo result = fileService.uploadCompetitionImage(TEST_COMPETITION_ID, null, mockFile);

            // 验证
            assertNotNull(result);
            assertEquals(TEST_FILE_ID, result.getId());
            assertEquals(TEST_FILE_NAME, result.getName());
            assertEquals(FileType.NORMAL_IMG, result.getType());

            verify(competitionDomainService).existsById(TEST_COMPETITION_ID);
            verify(introduceImageDomainService).countCompetitionImages(TEST_COMPETITION_ID);
            verify(introduceImageDomainService).addCompetitionImage(TEST_COMPETITION_ID, TEST_FILE_ID, null);
        }

        /**
         * 上传竞赛图片带描述：应成功上传并保存描述
         */
        @Test
        @DisplayName("上传竞赛图片带描述：应成功上传并保存描述")
        void uploadCompetitionImage_withDescription_shouldUploadWithDescription() throws IOException {
            // 准备
            when(competitionDomainService.existsById(TEST_COMPETITION_ID)).thenReturn(true);
            when(introduceImageDomainService.countCompetitionImages(TEST_COMPETITION_ID)).thenReturn(5);
            when(fileDomainService.generateFilename(eq(FileType.NORMAL_IMG), any())).thenReturn(TEST_FILE_NAME);
            when(fileDomainService.saveFile(eq(FileType.NORMAL_IMG), any(), any())).thenReturn(testFileVO);
            when(
                    introduceImageDomainService
                            .addCompetitionImage(eq(TEST_COMPETITION_ID), eq(TEST_FILE_ID), eq(TEST_DESCRIPTION)))
                                    .thenReturn(1L);

            // 执行
            FileInfo result = fileService.uploadCompetitionImage(TEST_COMPETITION_ID, TEST_DESCRIPTION, mockFile);

            // 验证
            assertNotNull(result);
            assertEquals(TEST_FILE_ID, result.getId());

            verify(introduceImageDomainService)
                    .addCompetitionImage(TEST_COMPETITION_ID, TEST_FILE_ID, TEST_DESCRIPTION);
        }

        /**
         * 上传竞赛图片：竞赛不存在应抛出异常
         */
        @Test
        @DisplayName("上传竞赛图片：竞赛不存在应抛出异常")
        void uploadCompetitionImage_nonExistentCompetition_shouldThrowException() {
            // 准备
            when(competitionDomainService.existsById(TEST_COMPETITION_ID)).thenReturn(false);

            // 执行 & 验证
            DataNotFound exception = assertThrows(
                    DataNotFound.class,
                    () -> fileService.uploadCompetitionImage(TEST_COMPETITION_ID, null, mockFile));

            assertEquals("竞赛不存在", exception.getMessage());

            verify(competitionDomainService).existsById(TEST_COMPETITION_ID);
            verify(introduceImageDomainService, never()).countCompetitionImages(any());
            verify(fileDomainService, never()).saveFile(any(), any(), any());
        }

        /**
         * 上传竞赛图片：图片数量已达上限应抛出异常
         */
        @Test
        @DisplayName("上传竞赛图片：图片数量已达上限应抛出异常")
        void uploadCompetitionImage_exceedLimit_shouldThrowException() {
            // 准备
            when(competitionDomainService.existsById(TEST_COMPETITION_ID)).thenReturn(true);
            when(introduceImageDomainService.countCompetitionImages(TEST_COMPETITION_ID)).thenReturn(20);

            // 执行 & 验证
            DataConflict exception = assertThrows(
                    DataConflict.class,
                    () -> fileService.uploadCompetitionImage(TEST_COMPETITION_ID, null, mockFile));

            assertEquals("竞赛图片数量已达上限（最多20张）", exception.getMessage());

            verify(competitionDomainService).existsById(TEST_COMPETITION_ID);
            verify(introduceImageDomainService).countCompetitionImages(TEST_COMPETITION_ID);
            verify(fileDomainService, never()).saveFile(any(), any(), any());
        }

        /**
         * 上传竞赛图片：已有19张图片时应允许上传
         */
        @Test
        @DisplayName("上传竞赛图片：已有19张图片时应允许上传")
        void uploadCompetitionImage_19Images_shouldAllowUpload() throws IOException {
            // 准备
            when(competitionDomainService.existsById(TEST_COMPETITION_ID)).thenReturn(true);
            when(introduceImageDomainService.countCompetitionImages(TEST_COMPETITION_ID)).thenReturn(19);
            when(fileDomainService.generateFilename(eq(FileType.NORMAL_IMG), any())).thenReturn(TEST_FILE_NAME);
            when(fileDomainService.saveFile(eq(FileType.NORMAL_IMG), any(), any())).thenReturn(testFileVO);
            when(introduceImageDomainService.addCompetitionImage(eq(TEST_COMPETITION_ID), eq(TEST_FILE_ID), isNull()))
                    .thenReturn(1L);

            // 执行
            FileInfo result = fileService.uploadCompetitionImage(TEST_COMPETITION_ID, null, mockFile);

            // 验证
            assertNotNull(result);
            assertEquals(TEST_FILE_ID, result.getId());
        }
    }

    // ==================== uploadCompetitionLogo 测试 ====================

    @Nested
    @DisplayName("uploadCompetitionLogo 方法测试")
    class UploadCompetitionLogoTests {

        /**
         * 上传竞赛Logo：应成功上传并更新竞赛Logo
         */
        @Test
        @DisplayName("上传竞赛Logo：应成功上传并更新竞赛Logo")
        void uploadCompetitionLogo_validCompetition_shouldUploadSuccessfully() throws IOException {
            // 准备
            when(competitionDomainService.existsById(TEST_COMPETITION_ID)).thenReturn(true);
            when(fileDomainService.generateFilename(eq(FileType.NORMAL_IMG), any())).thenReturn(TEST_FILE_NAME);
            when(fileDomainService.saveFile(eq(FileType.NORMAL_IMG), any(), any())).thenReturn(testFileVO);
            doNothing().when(competitionDomainService).updateLogo(TEST_COMPETITION_ID, TEST_FILE_ID);

            // 执行
            FileInfo result = fileService.uploadCompetitionLogo(TEST_COMPETITION_ID, mockFile);

            // 验证
            assertNotNull(result);
            assertEquals(TEST_FILE_ID, result.getId());
            assertEquals(TEST_FILE_NAME, result.getName());
            assertEquals(FileType.NORMAL_IMG, result.getType());

            verify(competitionDomainService).existsById(TEST_COMPETITION_ID);
            verify(fileDomainService).saveFile(eq(FileType.NORMAL_IMG), any(), any());
            verify(competitionDomainService).updateLogo(TEST_COMPETITION_ID, TEST_FILE_ID);
        }

        /**
         * 上传竞赛Logo：竞赛不存在应抛出异常
         */
        @Test
        @DisplayName("上传竞赛Logo：竞赛不存在应抛出异常")
        void uploadCompetitionLogo_nonExistentCompetition_shouldThrowException() {
            // 准备
            when(competitionDomainService.existsById(TEST_COMPETITION_ID)).thenReturn(false);

            // 执行 & 验证
            DataNotFound exception = assertThrows(
                    DataNotFound.class,
                    () -> fileService.uploadCompetitionLogo(TEST_COMPETITION_ID, mockFile));

            assertEquals("竞赛不存在", exception.getMessage());

            verify(competitionDomainService).existsById(TEST_COMPETITION_ID);
            verify(fileDomainService, never()).saveFile(any(), any(), any());
            verify(competitionDomainService, never()).updateLogo(any(), any());
        }
    }
}
