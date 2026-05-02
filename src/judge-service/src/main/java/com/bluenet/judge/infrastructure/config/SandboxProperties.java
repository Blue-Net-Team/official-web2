package com.bluenet.judge.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 判题沙箱默认资源限制配置。
 *
 * @param engine
 *            沙箱引擎，可选值为 isolate、nsjail。
 * @param cpuTimeSeconds
 *            CPU 时间限制，单位秒。
 * @param wallTimeSeconds
 *            墙钟时间限制，单位秒。
 * @param memoryLimitMb
 *            内存限制，单位 MB。
 * @param processLimit
 *            进程数量限制。
 * @param outputLimitKb
 *            输出限制，单位 KB。
 * @param generatedFileLimitMb
 *            generator 生成文件大小限制，单位 MB。
 * @param networkDisabled
 *            是否禁用网络。
 * @param workRoot
 *            判题临时工作目录根路径。
 */
@Validated
@ConfigurationProperties(prefix = "judge.sandbox")
public record SandboxProperties(
        @NotBlank String engine,
        int cpuTimeSeconds,
        int wallTimeSeconds,
        int memoryLimitMb,
        int processLimit,
        int outputLimitKb,
        int generatedFileLimitMb,
        boolean networkDisabled,
        @NotBlank String workRoot) {
}
