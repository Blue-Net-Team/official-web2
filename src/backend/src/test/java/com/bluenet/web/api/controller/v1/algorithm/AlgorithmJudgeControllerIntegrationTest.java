package com.bluenet.web.api.controller.v1.algorithm;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.algorithm_judge.AlgorithmRunRequestDTO;
import com.bluenet.web.api.dto.algorithm_judge.AlgorithmSubmitResponseDTO;
import com.bluenet.web.api.dto.algorithm_judge.JudgeJobPollingResponseDTO;
import com.bluenet.web.api.dto.assessment_answer.CreateAnswerRequestDTO;
import com.bluenet.web.api.converter.algorithm_judge.AlgorithmJudgeResponseConverter;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AlgorithmJudgeController 集成测试")
class AlgorithmJudgeControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AlgorithmJudgeAppService algorithmJudgeAppService;

    @MockitoBean
    private AlgorithmJudgeResponseConverter responseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    @Test
    @DisplayName("run: 已登录用户运行算法题代码应成功")
    @WithSecurityPrincipal(userId = 1L, roleType = "CANDIDATE", roleId = 4L)
    void run_authenticated_shouldReturnSubmitResponse() throws Exception {
        AlgorithmJudgeResult.SubmitResult submitResult = new AlgorithmJudgeResult.SubmitResult(
                100L,
                50L,
                AlgorithmTestcaseType.DEFAULT_RUN);
        AlgorithmSubmitResponseDTO dto = AlgorithmSubmitResponseDTO.builder()
                .judgeJobId(100L)
                .answerId(50L)
                .testcaseType(AlgorithmTestcaseType.DEFAULT_RUN)
                .build();
        when(algorithmJudgeAppService.run(any())).thenReturn(submitResult);
        when(responseConverter.toSubmitDTO(any(AlgorithmJudgeResult.SubmitResult.class))).thenReturn(dto);

        AlgorithmRunRequestDTO request = AlgorithmRunRequestDTO.builder()
                .questionId(1L)
                .language(ProgrammingLanguage.JAVA)
                .sourceCode("public class Main { public static void main(String[] args) {} }")
                .testcaseType(AlgorithmTestcaseType.DEFAULT_RUN)
                .build();

        mockMvc.perform(
                post("/api/v1/algorithm-judge/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.judgeJobId").value(100))
                .andExpect(jsonPath("$.data.answerId").value(50));
    }

    @Test
    @DisplayName("run: 未登录用户应返回 401")
    void run_anonymous_shouldReturnUnauthorized() throws Exception {
        AlgorithmRunRequestDTO request = AlgorithmRunRequestDTO.builder()
                .questionId(1L)
                .language(ProgrammingLanguage.JAVA)
                .sourceCode("public class Main {}")
                .build();

        mockMvc.perform(
                post("/api/v1/algorithm-judge/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("run: 源代码为空应返回 400")
    @WithSecurityPrincipal(userId = 1L, roleType = "CANDIDATE", roleId = 4L)
    void run_blankSourceCode_shouldReturnBadRequest() throws Exception {
        AlgorithmRunRequestDTO request = AlgorithmRunRequestDTO.builder()
                .questionId(1L)
                .language(ProgrammingLanguage.JAVA)
                .sourceCode(" ")
                .build();

        mockMvc.perform(
                post("/api/v1/algorithm-judge/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("submit: 已登录用户提交算法题答案应成功")
    @WithSecurityPrincipal(userId = 1L, roleType = "CANDIDATE", roleId = 4L)
    void submit_authenticated_shouldReturnSubmitResponse() throws Exception {
        AlgorithmJudgeResult.SubmitResult submitResult = new AlgorithmJudgeResult.SubmitResult(
                200L,
                51L,
                AlgorithmTestcaseType.FORMAL);
        AlgorithmSubmitResponseDTO dto = AlgorithmSubmitResponseDTO.builder()
                .judgeJobId(200L)
                .answerId(51L)
                .testcaseType(AlgorithmTestcaseType.FORMAL)
                .build();
        when(algorithmJudgeAppService.submit(any())).thenReturn(submitResult);
        when(responseConverter.toSubmitDTO(any(AlgorithmJudgeResult.SubmitResult.class))).thenReturn(dto);

        CreateAnswerRequestDTO request = CreateAnswerRequestDTO.builder()
                .questionId(1L)
                .language(ProgrammingLanguage.JAVA)
                .content("public class Main {}")
                .build();

        mockMvc.perform(
                post("/api/v1/algorithm-judge/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.judgeJobId").value(200))
                .andExpect(jsonPath("$.data.testcaseType").value("FORMAL"));
    }

    @Test
    @DisplayName("submit: 未登录用户应返回 401")
    void submit_anonymous_shouldReturnUnauthorized() throws Exception {
        CreateAnswerRequestDTO request = CreateAnswerRequestDTO.builder()
                .questionId(1L)
                .language(ProgrammingLanguage.JAVA)
                .content("public class Main {}")
                .build();

        mockMvc.perform(
                post("/api/v1/algorithm-judge/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("getJob: 已登录用户轮询判题任务应成功")
    @WithSecurityPrincipal(userId = 1L, roleType = "CANDIDATE", roleId = 4L)
    void getJob_authenticated_shouldReturnPollingResponse() throws Exception {
        AlgorithmJudgeResult.PollResult pollResult = new AlgorithmJudgeResult.PollResult(
                100L,
                AlgorithmTestcaseType.FORMAL,
                JudgeJobStatus.PENDING,
                "等待中",
                List.of(),
                null);
        JudgeJobPollingResponseDTO dto = JudgeJobPollingResponseDTO.builder()
                .judgeJobId(100L)
                .testcaseType(AlgorithmTestcaseType.FORMAL)
                .status(JudgeJobStatus.PENDING)
                .statusMessage("等待中")
                .caseResults(List.of())
                .build();
        when(algorithmJudgeAppService.getJob(100L)).thenReturn(pollResult);
        when(responseConverter.toPollingDTO(any(AlgorithmJudgeResult.PollResult.class))).thenReturn(dto);

        mockMvc.perform(get("/api/v1/algorithm-judge/jobs/{jobId}", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.judgeJobId").value(100))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("getJob: 未登录用户应返回 401")
    void getJob_anonymous_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/algorithm-judge/jobs/{jobId}", 100L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }
}
