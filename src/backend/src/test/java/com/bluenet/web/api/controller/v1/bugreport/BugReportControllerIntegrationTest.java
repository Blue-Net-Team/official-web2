package com.bluenet.web.api.controller.v1.bugreport;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.bugreport.BugReportCreatedDTO;
import com.bluenet.web.api.dto.bugreport.CreateBugReportRequestDTO;
import com.bluenet.web.api.converter.bugreport.BugReportRequestConverter;
import com.bluenet.web.api.converter.bugreport.BugReportResponseConverter;
import com.bluenet.web.application.command.bugreport.BugReportCommands;
import com.bluenet.web.application.result.bugreport.BugReportResult;
import com.bluenet.web.application.service.BugReportAppService;
import com.bluenet.web.domain.model.enumerate.BugReportStatus;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("BugReportController 集成测试")
class BugReportControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BugReportAppService bugReportAppService;

    @MockitoBean
    private BugReportRequestConverter requestConverter;

    @MockitoBean
    private BugReportResponseConverter responseConverter;

    @AfterEach
    void tearDown() {
        // 公开接口，不涉及 UserCTX。
    }

    @Test
    @DisplayName("submitBugReport: 有效请求应返回创建结果")
    void submitBugReport_validRequest_shouldReturnCreated() throws Exception {
        CreateBugReportRequestDTO request = CreateBugReportRequestDTO.builder()
                .title("提交按钮无响应")
                .description("点击提交按钮后页面无响应")
                .pageUrl("/home")
                .reporterEmail("user@example.com")
                .fileIds(List.of(1L, 2L))
                .build();
        BugReportCommands.CreateBugReportCommand command = new BugReportCommands.CreateBugReportCommand(
                "提交按钮无响应",
                "点击提交按钮后页面无响应",
                "/home",
                null,
                "user@example.com",
                List.of(1L, 2L));
        BugReportResult.Created result = new BugReportResult.Created(1L, BugReportStatus.PENDING, null);
        BugReportCreatedDTO dto = BugReportCreatedDTO.builder()
                .id(1L)
                .status(BugReportStatus.PENDING)
                .build();
        when(requestConverter.toCommand(any())).thenReturn(command);
        when(bugReportAppService.submitBugReport(command)).thenReturn(result);
        when(responseConverter.toCreatedDTO(result)).thenReturn(dto);

        mockMvc.perform(
                post("/api/v1/bug-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("submitBugReport: 标题为空时应返回 400")
    void submitBugReport_blankTitle_shouldReturnBadRequest() throws Exception {
        CreateBugReportRequestDTO request = CreateBugReportRequestDTO.builder()
                .title("")
                .description("点击提交按钮后页面无响应")
                .build();

        mockMvc.perform(
                post("/api/v1/bug-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("submitBugReport: 截图超过 3 张时应返回 400")
    void submitBugReport_tooManyFiles_shouldReturnBadRequest() throws Exception {
        CreateBugReportRequestDTO request = CreateBugReportRequestDTO.builder()
                .title("提交按钮无响应")
                .description("点击提交按钮后页面无响应")
                .fileIds(List.of(1L, 2L, 3L, 4L))
                .build();

        mockMvc.perform(
                post("/api/v1/bug-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
