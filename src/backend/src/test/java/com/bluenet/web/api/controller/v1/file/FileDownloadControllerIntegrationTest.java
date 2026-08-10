package com.bluenet.web.api.controller.v1.file;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.file.BatchDownloadEntryDTO;
import com.bluenet.web.api.dto.file.BatchDownloadRequestDTO;
import com.bluenet.web.application.command.file.FileCommands;
import com.bluenet.web.application.service.FileAppService;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("FileDownloadController 集成测试")
class FileDownloadControllerIntegrationTest extends BaseIntegrationTest {

    private static final byte[] TEXT_BYTES = "Hello, BlueNet!".getBytes(StandardCharsets.UTF_8);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileAppService fileAppService;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private Long uploadFile() {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                TEXT_BYTES);
        FileCommands.UploadFileCommand command = new FileCommands.UploadFileCommand(multipartFile, FileType.NORMAL_IMG);
        return fileAppService.uploadFile(command).id();
    }

    @Test
    @DisplayName("downloadFile: 已上传文件应返回 302 重定向")
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER", roleId = 3L)
    void downloadFile_existingFile_shouldRedirect() throws Exception {
        Long fileId = uploadFile();

        mockMvc.perform(get("/api/v1/file/download/{fileId}", fileId))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("X-Amz")));
    }

    @Test
    @DisplayName("downloadFile: 文件不存在时应返回 404")
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER", roleId = 3L)
    void downloadFile_nonExistent_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/file/download/{fileId}", 999_999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("downloadBatch: 批量下载应返回 ZIP 流")
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER", roleId = 3L)
    void downloadBatch_existingFiles_shouldReturnZipStream() throws Exception {
        Long fileId = uploadFile();
        BatchDownloadEntryDTO entry = new BatchDownloadEntryDTO();
        entry.setFileId(fileId);
        entry.setFilename("hello.txt");
        BatchDownloadRequestDTO request = new BatchDownloadRequestDTO();
        request.setZipName("batch");
        request.setEntries(List.of(entry));

        mockMvc.perform(
                post("/api/v1/file/download/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("batch.zip")))
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/zip")));
    }
}
