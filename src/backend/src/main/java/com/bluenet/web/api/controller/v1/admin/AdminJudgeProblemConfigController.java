package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.judge.ConfirmJudgeLanguageLimitRequestDTO;
import com.bluenet.web.api.dto.judge.JudgeProblemConfigDTO;
import com.bluenet.web.api.dto.judge.UpsertJudgeProblemConfigRequestDTO;
import com.bluenet.web.application.service.JudgeProblemConfigAdminService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端算法判题配置控制器。
 * <p>
 * 提供 generator、标准解、测试用例配置、测试数据生成任务和语言资源限制确认接口。
 * </p>
 */
@RestController
@RequestMapping("/api/v1/admin/judge/questions/{questionId}/config")
@RequiredArgsConstructor
public class AdminJudgeProblemConfigController {
    /** 管理端判题配置应用服务。 */
    private final JudgeProblemConfigAdminService judgeProblemConfigAdminService;

    /**
     * 新增或替换指定算法题的当前判题配置。
     *
     * @param questionId
     *            算法题目主键。
     * @param request
     *            管理员提交的 generator、标准解、测试用例和 benchmark 配置。
     * @return 保存后的当前判题配置。
     */
    @PutMapping
    @RequiresPermission(name = "保存算法判题配置", value = "judge-problem-config:upsert", access = AccessLevel.PROTECTED)
    public ResponseMessage<JudgeProblemConfigDTO> upsert(
            @PathVariable Long questionId,
            @Valid @RequestBody UpsertJudgeProblemConfigRequestDTO request) {
        // 管理端只提交配置和源码，manifest 由后端生成并保存到独立判题 bucket。
        return ResponseMessage.success(judgeProblemConfigAdminService.upsert(questionId, request));
    }

    /**
     * 查询指定算法题的当前判题配置。
     *
     * @param questionId
     *            算法题目主键。
     * @return 当前判题配置；不存在时返回 404。
     */
    @GetMapping
    @RequiresPermission(name = "查询算法判题配置", value = "judge-problem-config:read", access = AccessLevel.PROTECTED)
    public ResponseMessage<JudgeProblemConfigDTO> get(@PathVariable Long questionId) {
        return judgeProblemConfigAdminService.findByQuestionId(questionId)
                .map(ResponseMessage::success)
                .orElseGet(() -> ResponseMessage.error(404, "判题配置不存在"));
    }

    /**
     * 请求 Judge Service 根据当前配置生成测试数据。
     *
     * @param questionId
     *            算法题目主键。
     * @return 空响应，表示生成任务已入队或已记录。
     */
    @PostMapping("/generation-tasks")
    @RequiresPermission(name = "生成算法测试数据", value = "judge-problem-config:generate-test-data", access = AccessLevel.PROTECTED)
    public ResponseMessage<Void> requestGeneration(@PathVariable Long questionId) {
        judgeProblemConfigAdminService.requestGeneration(questionId);
        return ResponseMessage.success();
    }

    /**
     * 确认指定算法题某个语言的正式判题资源限制。
     *
     * @param questionId
     *            算法题目主键。
     * @param language
     *            编程语言值。
     * @param request
     *            管理员确认的时间、内存和输出限制。
     * @return 空响应，表示确认成功。
     */
    @PutMapping("/language-limits/{language}")
    @RequiresPermission(name = "确认算法语言限制", value = "judge-problem-config:confirm-language-limit", access = AccessLevel.PROTECTED)
    public ResponseMessage<Void> confirmLanguageLimit(
            @PathVariable Long questionId,
            @PathVariable String language,
            @Valid @RequestBody ConfirmJudgeLanguageLimitRequestDTO request) {
        // 管理员确认后，候选人的正式提交才允许使用该语言进入判题队列。
        judgeProblemConfigAdminService.confirmLanguageLimit(questionId, language, request);
        return ResponseMessage.success();
    }
}
