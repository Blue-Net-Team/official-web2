package com.bluenet.web.api.controller.v1;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.algorithm_judge.AlgorithmRunRequestDTO;
import com.bluenet.web.api.dto.algorithm_judge.AlgorithmSubmitResponseDTO;
import com.bluenet.web.api.dto.algorithm_judge.JudgeJobPollingResponseDTO;
import com.bluenet.web.api.dto.assessment_answer.CreateAnswerRequestDTO;
import com.bluenet.web.application.service.AlgorithmJudgeService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "算法判题", description = "算法题运行、提交和轮询接口")
@RestController
@RequestMapping("/api/v1/algorithm-judge")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AlgorithmJudgeController {
    private final AlgorithmJudgeService algorithmJudgeService;

    @Operation(summary = "运行算法题代码", description = "创建不计分运行任务，可使用默认运行用例或自定义输入。")
    @RequiresPermission(name = "运行算法题", value = "algorithm-judge:run", access = AccessLevel.AUTHENTICATED)
    @PostMapping("/run")
    public ResponseMessage<AlgorithmSubmitResponseDTO> run(@Valid @RequestBody AlgorithmRunRequestDTO request) {
        return ResponseMessage.success(algorithmJudgeService.run(request));
    }

    @Operation(summary = "提交算法题答案", description = "保存正式答案，创建判题任务并投递队列。")
    @RequiresPermission(name = "提交算法题", value = "algorithm-judge:submit", access = AccessLevel.AUTHENTICATED)
    @PostMapping("/submit")
    public ResponseMessage<AlgorithmSubmitResponseDTO> submit(@Valid @RequestBody CreateAnswerRequestDTO request) {
        return ResponseMessage.success(algorithmJudgeService.submit(request));
    }

    @Operation(summary = "轮询算法判题任务", description = "查询当前用户自己的运行或正式提交任务状态。")
    @RequiresPermission(name = "轮询算法判题任务", value = "algorithm-judge:poll", access = AccessLevel.AUTHENTICATED)
    @GetMapping("/jobs/{jobId}")
    public ResponseMessage<JudgeJobPollingResponseDTO> getJob(@PathVariable Long jobId) {
        return ResponseMessage.success(algorithmJudgeService.getJob(jobId));
    }
}
