package com.bluenet.web.infrastructure.judge.sandbox;

import com.bluenet.web.domain.model.enumerate.JudgeCaseStatus;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class DockerSandboxExecutor implements SandboxExecutor {
    private static final int DEFAULT_TIME_LIMIT_MS = 1000;
    private static final int DEFAULT_MEMORY_LIMIT_KB = 262144;
    private static final int OUTPUT_LIMIT_BYTES = 65536;

    @Override
    public SandboxExecutionResult execute(SandboxExecutionRequest request) {
        List<SandboxCaseResult> results = new ArrayList<>();
        for (SandboxTestcase testcase : request.getTestcases()) {
            try {
                results.add(executeOne(request, testcase));
            } catch (SandboxInfrastructureException e) {
                return SandboxExecutionResult.builder()
                        .infrastructureFailure(true)
                        .infrastructureMessage(e.getMessage())
                        .caseResults(List.of())
                        .build();
            }
        }
        return SandboxExecutionResult.builder()
                .caseResults(results)
                .infrastructureFailure(false)
                .build();
    }

    private static final long PROCESS_DESTROY_TIMEOUT_MS = 5000;

    private SandboxCaseResult executeOne(SandboxExecutionRequest request, SandboxTestcase testcase) {
        Path workspace = null;
        Process process = null;
        try {
            workspace = Files.createTempDirectory("bluenet-judge-");
            writeSource(workspace, request.getLanguage(), request.getSourceCode());

            long started = System.nanoTime();
            process = new ProcessBuilder(buildDockerCommand(request, workspace))
                    .redirectErrorStream(false)
                    .start();
            try (OutputStream stdin = process.getOutputStream()) {
                stdin.write((testcase.getInput() == null ? "" : testcase.getInput()).getBytes(StandardCharsets.UTF_8));
            }

            boolean finished = process.waitFor(resolveTimeout(request).toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                killProcess(process);
                return buildResult(testcase, JudgeCaseStatus.TLE, "", "", elapsedMs(started), "运行超时");
            }

            String stdout = limitedRead(process.getInputStream().readAllBytes());
            String stderr = limitedRead(process.getErrorStream().readAllBytes());
            int elapsedMs = elapsedMs(started);
            if (process.exitValue() != 0) {
                JudgeCaseStatus status = isCompileError(stderr) ? JudgeCaseStatus.CE : JudgeCaseStatus.RE;
                return buildResult(testcase, status, stdout, stderr, elapsedMs, stderr);
            }

            // 自定义运行没有期望输出，进程正常退出即视为运行成功，避免前端误显示"答案错误"。
            JudgeCaseStatus status = testcase.getExpectedOutput() == null
                    ? JudgeCaseStatus.AC
                    : normalize(stdout).equals(normalize(testcase.getExpectedOutput()))
                            ? JudgeCaseStatus.AC
                            : JudgeCaseStatus.WA;
            return buildResult(testcase, status, stdout, stderr, elapsedMs, null);
        } catch (IOException e) {
            throw new SandboxInfrastructureException("沙盒基础设施异常：" + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SandboxInfrastructureException("沙盒执行被中断");
        } catch (RuntimeException e) {
            return buildResult(testcase, JudgeCaseStatus.RE, "", e.getMessage(), null, e.getMessage());
        } finally {
            killProcess(process);
            deleteWorkspace(workspace);
        }
    }

    private void killProcess(Process process) {
        if (process == null || !process.isAlive()) {
            return;
        }
        process.destroyForcibly();
        try {
            process.waitFor(PROCESS_DESTROY_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private List<String> buildDockerCommand(SandboxExecutionRequest request, Path workspace) {
        int memoryMb = Math.max(16, resolveMemoryKb(request) / 1024);
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("run");
        command.add("--rm");
        command.add("-i");
        command.add("--network");
        command.add("none");
        command.add("--memory");
        command.add(memoryMb + "m");
        command.add("--cpus");
        command.add("1");
        command.add("--pids-limit");
        command.add("64");
        command.add("--read-only");
        command.add("--tmpfs");
        command.add("/tmp:rw,noexec,nosuid,size=64m");
        command.add("-v");
        command.add(workspace.toAbsolutePath() + ":/workspace");
        command.add("-w");
        command.add("/workspace");
        command.add(resolveImage(request.getLanguage()));
        command.add("sh");
        command.add("-c");
        command.add(resolveRunCommand(request.getLanguage()));
        return command;
    }

    private void writeSource(Path workspace, ProgrammingLanguage language, String sourceCode) throws IOException {
        Files.writeString(workspace.resolve(resolveFileName(language)), sourceCode, StandardCharsets.UTF_8);
    }

    private String resolveFileName(ProgrammingLanguage language) {
        return switch (language) {
            case PYTHON -> "main.py";
            case JAVASCRIPT -> "main.js";
            case JAVA -> "Main.java";
            case C -> "main.c";
            case CPP -> "main.cpp";
        };
    }

    private String resolveImage(ProgrammingLanguage language) {
        return switch (language) {
            case PYTHON -> "python:3.12-alpine";
            case JAVASCRIPT -> "node:22-alpine";
            case JAVA -> "eclipse-temurin:21-jdk-alpine";
            case C, CPP -> "gcc:14";
        };
    }

    private String resolveRunCommand(ProgrammingLanguage language) {
        return switch (language) {
            case PYTHON -> "python3 main.py";
            case JAVASCRIPT -> "node main.js";
            case JAVA -> "javac Main.java && java Main";
            case C -> "gcc main.c -O2 -o main && ./main";
            case CPP -> "g++ main.cpp -O2 -std=c++17 -o main && ./main";
        };
    }

    private Duration resolveTimeout(SandboxExecutionRequest request) {
        int timeLimit = request.getTimeLimitMs() == null ? DEFAULT_TIME_LIMIT_MS : request.getTimeLimitMs();
        // 编译语言需要给编译阶段少量余量，但最终用例仍按 Worker 结果记录。
        return Duration.ofMillis(Math.max(timeLimit + 1000L, 1500L));
    }

    private int resolveMemoryKb(SandboxExecutionRequest request) {
        return request.getMemoryLimitKb() == null ? DEFAULT_MEMORY_LIMIT_KB : request.getMemoryLimitKb();
    }

    private SandboxCaseResult buildResult(
            SandboxTestcase testcase,
            JudgeCaseStatus status,
            String stdout,
            String stderr,
            Integer timeUsedMs,
            String message) {
        return SandboxCaseResult.builder()
                .caseNo(testcase.getCaseNo())
                .status(status)
                .input(testcase.getInput())
                .expectedOutput(testcase.getExpectedOutput())
                .actualOutput(stdout)
                .stdout(stdout)
                .stderr(stderr)
                .timeUsedMs(timeUsedMs)
                .message(message)
                .build();
    }

    private String limitedRead(byte[] bytes) {
        int length = Math.min(bytes.length, OUTPUT_LIMIT_BYTES);
        return new String(bytes, 0, length, StandardCharsets.UTF_8);
    }

    private String normalize(String value) {
        return value == null ? "" : value.strip();
    }

    private boolean isCompileError(String stderr) {
        String lower = stderr == null ? "" : stderr.toLowerCase();
        return lower.contains("compile") || lower.contains("syntax") || lower.contains("javac")
                || lower.contains("gcc") || lower.contains("g++");
    }

    private int elapsedMs(long started) {
        return (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
    }

    private void deleteWorkspace(Path workspace) {
        if (workspace == null || !Files.exists(workspace)) {
            return;
        }
        try (var stream = Files.walk(workspace)) {
            stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }

    private static class SandboxInfrastructureException extends RuntimeException {
        SandboxInfrastructureException(String message) {
            super(message);
        }
    }
}
