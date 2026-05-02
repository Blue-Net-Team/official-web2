package com.bluenet.judge.application.service;

import com.bluenet.judge.application.dto.SandboxExecutionResult;
import com.bluenet.judge.infrastructure.config.SandboxProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于 isolate 的单文件源码编译和运行器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SandboxCodeRunner {
    /** isolate box 编号序列。 */
    private static final AtomicInteger BOX_SEQUENCE = new AtomicInteger(1);

    /** 沙箱默认资源限制配置。 */
    private final SandboxProperties sandboxProperties;

    /**
     * 编译并运行单文件源码。
     *
     * @param language
     *            源码语言。
     * @param source
     *            UTF-8 源码字节内容。
     * @param stdin
     *            标准输入字节内容。
     * @return 沙箱执行结果。
     */
    public SandboxExecutionResult run(String language, byte[] source, byte[] stdin) {
        return run(language, source, stdin, defaultLimits());
    }

    /**
     * 按指定资源限制编译并运行单文件源码。
     *
     * @param language
     *            源码语言。
     * @param source
     *            UTF-8 源码字节内容。
     * @param stdin
     *            标准输入字节内容。
     * @param timeLimitMs
     *            时间限制，单位毫秒。
     * @param memoryLimitKb
     *            内存限制，单位 KB。
     * @param outputLimitKb
     *            输出限制，单位 KB。
     * @return 沙箱执行结果。
     */
    public SandboxExecutionResult run(
            String language,
            byte[] source,
            byte[] stdin,
            int timeLimitMs,
            int memoryLimitKb,
            int outputLimitKb) {
        return run(language, source, stdin, new ExecutionLimits(timeLimitMs, memoryLimitKb, outputLimitKb));
    }

    /**
     * 按资源限制编译并运行单文件源码。
     *
     * @param language
     *            源码语言。
     * @param source
     *            UTF-8 源码字节内容。
     * @param stdin
     *            标准输入字节内容。
     * @param limits
     *            执行资源限制。
     * @return 沙箱执行结果。
     */
    private SandboxExecutionResult run(String language, byte[] source, byte[] stdin, ExecutionLimits limits) {
        if (!"isolate".equalsIgnoreCase(sandboxProperties.engine())) {
            throw new IllegalStateException("当前仅支持 isolate 沙箱引擎：" + sandboxProperties.engine());
        }
        int boxId = nextBoxId();
        Path boxPath = null;
        try {
            boxPath = initializeBox(boxId);
            RuntimeSpec runtimeSpec = writeSource(boxPath, language, source);
            if (!runtimeSpec.compileCommand().isEmpty()) {
                SandboxExecutionResult compile = execute(
                        boxId,
                        runtimeSpec.compileCommand(),
                        new byte[0],
                        defaultLimits());
                if (compile.exitCode() != 0 || compile.timedOut()) {
                    throw new IllegalStateException("源码编译失败：" + new String(compile.stderr(), StandardCharsets.UTF_8));
                }
            }
            return execute(boxId, runtimeSpec.runCommand(), stdin == null ? new byte[0] : stdin, limits);
        } catch (IOException ex) {
            throw new IllegalStateException("写入沙箱源码失败", ex);
        } finally {
            cleanupBox(boxId);
        }
    }

    /**
     * 获取下一个 isolate box 编号。
     *
     * @return isolate box 编号。
     */
    private int nextBoxId() {
        return Math.floorMod(BOX_SEQUENCE.getAndIncrement(), 900) + 1;
    }

    /**
     * 初始化 isolate box。
     *
     * @param boxId
     *            isolate box 编号。
     * @return box 根目录。
     */
    private Path initializeBox(int boxId) {
        SandboxExecutionResult result = runHostCommand(
                List.of("isolate", "--box-id=%d".formatted(boxId), "--init"),
                Duration.ofSeconds(10));
        if (result.exitCode() != 0) {
            throw new IllegalStateException("初始化 isolate 沙箱失败：" + new String(result.stderr(), StandardCharsets.UTF_8));
        }
        return Path.of(new String(result.stdout(), StandardCharsets.UTF_8).trim());
    }

    /**
     * 清理 isolate box。
     *
     * @param boxId
     *            isolate box 编号。
     * @return 无返回值。
     */
    private void cleanupBox(int boxId) {
        runHostCommand(List.of("isolate", "--box-id=%d".formatted(boxId), "--cleanup"), Duration.ofSeconds(10));
    }

    /**
     * 写入源码文件并生成语言运行规格。
     *
     * @param boxPath
     *            isolate box 根目录。
     * @param language
     *            源码语言。
     * @param source
     *            源码字节内容。
     * @return 语言运行规格。
     * @throws IOException
     *             写入源码文件失败时抛出。
     */
    private RuntimeSpec writeSource(Path boxPath, String language, byte[] source) throws IOException {
        String normalized = language.toLowerCase(Locale.ROOT);
        Path workDir = boxPath.resolve("box");
        Files.createDirectories(workDir);
        if ("python".equals(normalized) || "python3".equals(normalized)) {
            Files.write(workDir.resolve("main.py"), source);
            return new RuntimeSpec(List.of(), List.of("/usr/bin/python3", "main.py"));
        }
        if ("javascript".equals(normalized) || "js".equals(normalized)) {
            Files.write(workDir.resolve("main.js"), source);
            return new RuntimeSpec(List.of(), List.of("/usr/bin/node", "main.js"));
        }
        if ("cpp".equals(normalized) || "cxx".equals(normalized) || "c_plus_plus".equals(normalized)) {
            Files.write(workDir.resolve("main.cpp"), source);
            return new RuntimeSpec(List.of("/usr/bin/g++", "-std=c++17", "-O2", "main.cpp", "-o", "main"),
                    List.of("./main"));
        }
        if ("c".equals(normalized)) {
            Files.write(workDir.resolve("main.c"), source);
            return new RuntimeSpec(List.of("/usr/bin/gcc", "-O2", "main.c", "-o", "main"), List.of("./main"));
        }
        if ("java".equals(normalized)) {
            Files.write(workDir.resolve("Main.java"), source);
            return new RuntimeSpec(List.of("/usr/bin/javac", "Main.java"), List.of("/usr/bin/java", "Main"));
        }
        throw new IllegalArgumentException("不支持的判题语言：" + language);
    }

    /**
     * 在 isolate box 中执行命令。
     *
     * @param boxId
     *            isolate box 编号。
     * @param command
     *            box 内执行命令。
     * @param stdin
     *            标准输入字节内容。
     * @param limits
     *            执行资源限制。
     * @return 沙箱执行结果。
     */
    private SandboxExecutionResult execute(int boxId, List<String> command, byte[] stdin, ExecutionLimits limits) {
        List<String> args = new ArrayList<>();
        args.add("isolate");
        args.add("--box-id=%d".formatted(boxId));
        args.add("--time=%.3f".formatted(Math.max(0.001, limits.timeLimitMs() / 1000.0)));
        args.add("--wall-time=%.3f".formatted(Math.max(1.0, limits.timeLimitMs() / 1000.0 + 1.0)));
        int virtualMemLimitKb = Math.max(limits.memoryLimitKb() * 2, 512 * 1024);
        args.add("--mem=%d".formatted(virtualMemLimitKb));
        args.add("--processes=%d".formatted(sandboxProperties.processLimit()));
        args.add("--fsize=%d".formatted(limits.outputLimitKb()));
        if (!sandboxProperties.networkDisabled()) {
            args.add("--share-net");
        }
        args.add("--dir=/usr:maybe");
        args.add("--dir=/lib:maybe");
        args.add("--dir=/lib64:maybe");
        args.add("--dir=/bin:maybe");
        String metaPath = "/tmp/isolate-meta-" + boxId + ".txt";
        args.add("--meta=" + metaPath);
        args.add("--run");
        args.add("--");
        args.addAll(command);
        SandboxExecutionResult raw = runHostCommand(
                args,
                stdin,
                Duration.ofMillis(Math.max(1000L, limits.timeLimitMs() + 2000L)),
                limits.outputLimitKb() * 1024);
        return parseMetaAndBuildResult(raw, metaPath);
    }

    /**
     * 读取默认沙箱执行限制。
     *
     * @return 默认执行限制。
     */
    private ExecutionLimits defaultLimits() {
        return new ExecutionLimits(
                sandboxProperties.wallTimeSeconds() * 1000,
                sandboxProperties.memoryLimitMb() * 1024,
                sandboxProperties.outputLimitKb());
    }

    /**
     * 在宿主容器中执行命令。
     *
     * @param command
     *            命令参数。
     * @param timeout
     *            超时时间。
     * @return 命令执行结果。
     */
    private SandboxExecutionResult runHostCommand(List<String> command, Duration timeout) {
        return runHostCommand(command, new byte[0], timeout, sandboxProperties.outputLimitKb() * 1024);
    }

    /**
     * 在宿主容器中执行命令并写入标准输入。
     *
     * @param command
     *            命令参数。
     * @param stdin
     *            标准输入字节内容。
     * @param timeout
     *            超时时间。
     * @return 命令执行结果。
     */
    private SandboxExecutionResult runHostCommand(List<String> command, byte[] stdin, Duration timeout) {
        return runHostCommand(command, stdin, timeout, sandboxProperties.outputLimitKb() * 1024);
    }

    /**
     * 在宿主容器中执行命令并写入标准输入。
     *
     * @param command
     *            命令参数。
     * @param stdin
     *            标准输入字节内容。
     * @param timeout
     *            超时时间。
     * @param outputLimitBytes
     *            输出限制，单位字节。
     * @return 命令执行结果。
     */
    private SandboxExecutionResult runHostCommand(List<String> command, byte[] stdin, Duration timeout,
            int outputLimitBytes) {
        try {
            long startedAt = System.nanoTime();
            Process process = new ProcessBuilder(command).start();
            StreamCollector stdoutCollector = new StreamCollector(process.getInputStream(), outputLimitBytes);
            StreamCollector stderrCollector = new StreamCollector(process.getErrorStream(), outputLimitBytes);
            Thread stdoutThread = new Thread(stdoutCollector);
            Thread stderrThread = new Thread(stderrCollector);
            stdoutThread.start();
            stderrThread.start();
            process.getOutputStream().write(stdin);
            process.getOutputStream().close();
            boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            int timeUsedMs = (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
            if (!finished) {
                process.destroyForcibly();
                return new SandboxExecutionResult(-1, new byte[0], "执行超时".getBytes(StandardCharsets.UTF_8), true,
                        timeUsedMs, 0, "TO");
            }
            stdoutThread.join(5000);
            stderrThread.join(5000);
            return new SandboxExecutionResult(process.exitValue(), stdoutCollector.result(), stderrCollector.result(),
                    false, timeUsedMs, 0, null);
        } catch (Exception ex) {
            throw new IllegalStateException("执行沙箱命令失败", ex);
        }
    }

    /**
     * 限制读取进程输出。
     *
     * @param inputStream
     *            进程输出流。
     * @param maxBytes
     *            最大读取字节数。
     * @return 输出字节内容。
     * @throws java.io.IOException
     *             读取输出流失败时抛出。
     */
    private static byte[] readWithLimit(java.io.InputStream inputStream, int maxBytes) throws java.io.IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int total = 0;
        int read;
        while ((read = inputStream.read(buffer)) >= 0) {
            total += read;
            if (total > maxBytes) {
                throw new IllegalStateException("沙箱输出超过限制");
            }
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    /**
     * 后台线程收集进程输出流内容。
     */
    private static class StreamCollector implements Runnable {
        private final java.io.InputStream inputStream;
        private final int maxBytes;
        private byte[] result;

        StreamCollector(java.io.InputStream inputStream, int maxBytes) {
            this.inputStream = inputStream;
            this.maxBytes = maxBytes;
        }

        @Override
        public void run() {
            try {
                result = readWithLimit(inputStream, maxBytes);
            } catch (java.io.IOException ex) {
                result = new byte[0];
            }
        }

        byte[] result() {
            return result != null ? result : new byte[0];
        }
    }

    /**
     * 解析 isolate meta 文件并构造执行结果。
     *
     * @param raw
     *            原始命令执行结果。
     * @param metaPath
     *            meta 文件路径。
     * @return 包含精确 CPU 时间和内存峰值的执行结果。
     */
    private SandboxExecutionResult parseMetaAndBuildResult(SandboxExecutionResult raw, String metaPath) {
        int cpuTimeMs = raw.timeUsedMs();
        int memoryUsedKb = 0;
        String isolateStatus = null;
        try {
            Path metaFile = Path.of(metaPath);
            if (Files.exists(metaFile)) {
                List<String> lines = Files.readAllLines(metaFile);
                log.debug("isolate meta file contents: {}", lines);
                for (String line : lines) {
                    int colon = line.indexOf(':');
                    if (colon < 0) {
                        continue;
                    }
                    String key = line.substring(0, colon).trim();
                    String value = line.substring(colon + 1).trim();
                    if ("time".equals(key)) {
                        cpuTimeMs = (int) Math.round(Double.parseDouble(value) * 1000.0);
                    } else if ("cg-mem".equals(key)) {
                        memoryUsedKb = Integer.parseInt(value);
                    } else if ("max-rss".equals(key) && memoryUsedKb == 0) {
                        memoryUsedKb = Integer.parseInt(value);
                    } else if ("status".equals(key)) {
                        isolateStatus = value;
                    }
                }
                Files.deleteIfExists(metaFile);
            }
        } catch (Exception ex) {
            // Meta 解析失败不影响整体执行结果，回退到粗略值
        }
        return new SandboxExecutionResult(raw.exitCode(), raw.stdout(), raw.stderr(), raw.timedOut(), cpuTimeMs,
                memoryUsedKb, isolateStatus);
    }

    /**
     * 语言运行规格。
     *
     * @param compileCommand
     *            编译命令；解释型语言为空。
     * @param runCommand
     *            运行命令。
     */
    private record RuntimeSpec(List<String> compileCommand, List<String> runCommand) {
    }

    /**
     * 单次沙箱执行资源限制。
     *
     * @param timeLimitMs
     *            时间限制，单位毫秒。
     * @param memoryLimitKb
     *            内存限制，单位 KB。
     * @param outputLimitKb
     *            输出限制，单位 KB。
     */
    private record ExecutionLimits(int timeLimitMs, int memoryLimitKb, int outputLimitKb) {
    }
}
