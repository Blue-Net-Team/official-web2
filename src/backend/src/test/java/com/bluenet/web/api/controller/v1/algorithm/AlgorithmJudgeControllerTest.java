package com.bluenet.web.api.controller.v1.algorithm;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.converter.algorithm_judge.AlgorithmJudgeRequestConverter;
import com.bluenet.web.api.converter.algorithm_judge.AlgorithmJudgeResponseConverter;
import com.bluenet.web.api.dto.algorithm_judge.AlgorithmRunRequestDTO;
import com.bluenet.web.api.dto.algorithm_judge.AlgorithmSubmitResponseDTO;
import com.bluenet.web.api.dto.algorithm_judge.JudgeJobPollingResponseDTO;
import com.bluenet.web.api.dto.assessment_answer.CreateAnswerRequestDTO;
import com.bluenet.web.application.command.algorithm_judge.AlgorithmJudgeCommands;
import com.bluenet.web.application.result.algorithm_judge.AlgorithmJudgeResult;
import com.bluenet.web.application.service.AlgorithmJudgeAppService;
import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.JudgeJobStatus;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AlgorithmJudgeController 集成测试。
 *
 * <p>
 * 验证算法判题 Controller 的运行、提交及轮询接口的 HTTP 契约。
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AlgorithmJudgeController 集成测试")
class AlgorithmJudgeControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AlgorithmJudgeAppService algorithmJudgeAppService;

    @MockitoBean
    private AlgorithmJudgeRequestConverter requestConverter;

    @MockitoBean
    private AlgorithmJudgeResponseConverter responseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    @Test
    @DisplayName("run: 已登录用户运行算法题应返回提交响应")
    @WithSecurityPrincipal(userId = 100L)
    void run_shouldReturnSubmitResponse() throws Exception {
        AlgorithmRunRequestDTO request = AlgorithmRunRequestDTO.builder()
                .questionId(1L)
                .language(ProgrammingLanguage.CPP)
                .sourceCode("int main() { return 0; }")
                .build();
        AlgorithmJudgeCommands.RunCommand command = new AlgorithmJudgeCommands.RunCommand(
                1L, ProgrammingLanguage.CPP, "int main() { return 0; }", null, null);
        AlgorithmJudgeResult.SubmitResult result = new AlgorithmJudgeResult.SubmitResult(
                10L, null, AlgorithmTestcaseType.DEFAULT_RUN);
        AlgorithmSubmitResponseDTO dto = AlgorithmSubmitResponseDTO.builder()
                .judgeJobId(10L)
                .testcaseType(AlgorithmTestcaseType.DEFAULT_RUN)
                .build();

        when(requestConverter.toCommand(request)).thenReturn(command);
        when(algorithmJudgeAppService.run(command)).thenReturn(result);
        when(responseConverter.toSubmitDTO(result)).thenReturn(dto);

        mockMvc.perform(
                post("/api/v1/algorithm-judge/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.judgeJobId").value(10))
                .andExpect(jsonPath("$.data.testcaseType").value("DEFAULT_RUN"));
    }

    @Test
    @DisplayName("run: 参数校验失败应返回 400")
    @WithSecurityPrincipal(userId = 100L)
    void run_withInvalidRequest_shouldReturn400() throws Exception {
        AlgorithmRunRequestDTO request = AlgorithmRunRequestDTO.builder().build();

        mockMvc.perform(
                post("/api/v1/algorithm-judge/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("submit: 已登录用户提交算法题应返回提交响应")
    @WithSecurityPrincipal(userId = 100L)
    void submit_shouldReturnSubmitResponse() throws Exception {
        CreateAnswerRequestDTO request = CreateAnswerRequestDTO.builder()
                .questionId(1L)
                .content("int main() { return 0; }")
                .language(ProgrammingLanguage.CPP)
                .build();
        AlgorithmJudgeCommands.SubmitCommand command = new AlgorithmJudgeCommands.SubmitCommand(
                1L, ProgrammingLanguage.CPP, "int main() { return 0; }");
        AlgorithmJudgeResult.SubmitResult result = new AlgorithmJudgeResult.SubmitResult(
                10L, 20L, AlgorithmTestcaseType.FORMAL);
        AlgorithmSubmitResponseDTO dto = AlgorithmSubmitResponseDTO.builder()
                .judgeJobId(10L)
                .answerId(20L)
                .testcaseType(AlgorithmTestcaseType.FORMAL)
                .build();

        when(requestConverter.toCommand(request)).thenReturn(command);
        when(algorithmJudgeAppService.submit(command)).thenReturn(result);
        when(responseConverter.toSubmitDTO(result)).thenReturn(dto);

        mockMvc.perform(
                post("/api/v1/algorithm-judge/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.judgeJobId").value(10))
                .andExpect(jsonPath("$.data.answerId").value(20))
                .andExpect(jsonPath("$.data.testcaseType").value("FORMAL"));
    }

    @Test
    @DisplayName("submit: 参数校验失败应返回 400")
    @WithSecurityPrincipal(userId = 100L)
    void submit_withInvalidRequest_shouldReturn400() throws Exception {
        CreateAnswerRequestDTO request = CreateAnswerRequestDTO.builder().build();

        mockMvc.perform(
                post("/api/v1/algorithm-judge/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getJob: 已登录用户轮询任务应返回轮询响应")
    @WithSecurityPrincipal(userId = 100L)
    void getJob_shouldReturnPollingResponse() throws Exception {
        AlgorithmJudgeResult.PollResult result = new AlgorithmJudgeResult.PollResult(
                10L,
                AlgorithmTestcaseType.DEFAULT_RUN,
                JudgeJobStatus.PENDING,
                "等待判题",
                List.of(),
                null);
        JudgeJobPollingResponseDTO dto = JudgeJobPollingResponseDTO.builder()
                .judgeJobId(10L)
                .testcaseType(AlgorithmTestcaseType.DEFAULT_RUN)
                .status(JudgeJobStatus.PENDING)
                .statusMessage("等待判题")
                .caseResults(List.of())
                .judgement(null)
                .build();

        when(algorithmJudgeAppService.getJob(10L)).thenReturn(result);
        when(responseConverter.toPollingDTO(result)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/algorithm-judge/jobs/{jobId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.judgeJobId").value(10))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }
}
