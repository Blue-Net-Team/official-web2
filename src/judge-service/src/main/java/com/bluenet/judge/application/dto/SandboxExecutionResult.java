package com.bluenet.judge.application.dto;

/**
 * 沙箱执行结果。
 *
 * @param exitCode
 *            进程退出码。
 * @param stdout
 *            标准输出字节内容。
 * @param stderr
 *            标准错误字节内容。
 * @param timedOut
 *            是否因超时被终止。
 * @param timeUsedMs
 *            CPU 执行耗时，单位毫秒（由 isolate meta 提供）。
 * @param memoryUsedKb
 *            内存峰值，单位 KB（由 isolate meta 提供）。
 */
public record SandboxExecutionResult(
        int exitCode,
        byte[] stdout,
        byte[] stderr,
        boolean timedOut,
        int timeUsedMs,
        int memoryUsedKb,
        String isolateStatus) {
}
