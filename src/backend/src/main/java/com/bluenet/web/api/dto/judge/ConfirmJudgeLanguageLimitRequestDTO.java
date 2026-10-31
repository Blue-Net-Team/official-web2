package com.bluenet.web.api.dto.judge;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 管理员确认某题某语言正式判题资源限制的请求。
 *
 * @param timeLimitMs
 *            该语言正式提交的时间限制，单位毫秒。
 * @param memoryLimitKb
 *            该语言正式提交的内存限制，单位 KB。
 * @param outputLimitKb
 *            该语言正式提交允许产生的最大输出，单位 KB。
 */
public record ConfirmJudgeLanguageLimitRequestDTO(
        @NotNull(message = "时间限制不能为空") @Min(value = 1, message = "时间限制必须大于等于1毫秒") Integer timeLimitMs,
        @NotNull(message = "内存限制不能为空") @Min(value = 1, message = "内存限制必须大于等于1KB") Integer memoryLimitKb,
        @NotNull(message = "输出限制不能为空") @Min(value = 1, message = "输出限制必须大于等于1KB") Integer outputLimitKb) {
}
