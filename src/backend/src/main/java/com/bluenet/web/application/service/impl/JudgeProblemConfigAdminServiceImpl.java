package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.result.judge.JudgeLanguageLimitResult;
import com.bluenet.web.application.result.judge.JudgeProblemConfigResult;
import com.bluenet.web.application.result.judge.JudgeStandardSolutionResult;
import com.bluenet.web.application.result.judge.JudgeTestcaseConfigResult;
import com.bluenet.web.application.command.judge.JudgeProblemConfigCommands;
import com.bluenet.web.application.service.JudgeProblemConfigAdminService;
import com.bluenet.web.domain.model.entity.JudgeLanguageLimit;
import com.bluenet.web.domain.model.entity.JudgeProblemConfig;
import com.bluenet.web.domain.model.entity.JudgeStandardSolution;
import com.bluenet.web.domain.model.entity.JudgeTestcaseConfig;
import com.bluenet.web.domain.model.policy.JudgeConfigContentPolicy;
import com.bluenet.web.domain.repository.JudgeLanguageLimitRepository;
import com.bluenet.web.domain.repository.JudgeProblemConfigRepository;
import com.bluenet.web.domain.repository.JudgeStandardSolutionRepository;
import com.bluenet.web.domain.repository.JudgeTestcaseConfigRepository;
import com.bluenet.web.infrastructure.judge.JudgeTestDataGenerationPublisher;
import com.bluenet.web.infrastructure.storage.JudgeAssetStorage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * 管理端算法判题配置应用服务实现。
 * <p>
 * 负责保存 generator/标准解源码到判题 bucket，生成 manifest，并通过 mapper 持久化判题配置元数据。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class JudgeProblemConfigAdminServiceImpl implements JudgeProblemConfigAdminService {
    /** 文本源码文件内容类型。 */
    private static final String CONTENT_TYPE_TEXT = "text/plain; charset=utf-8";
    /** JSON manifest 文件内容类型。 */
    private static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";
    /** 新建或判题内容变更后的配置状态。 */
    private static final String STATUS_DRAFT = "DRAFT";

    /** JSON 序列化工具。 */
    private final ObjectMapper objectMapper;
    /** 判题资产对象存储。 */
    private final JudgeAssetStorage judgeAssetStorage;
    /** 测试数据生成消息发布器。 */
    private final JudgeTestDataGenerationPublisher testDataGenerationPublisher;
    /** 判题配置仓储。 */
    private final JudgeProblemConfigRepository judgeProblemConfigRepository;
    /** 标准解仓储。 */
    private final JudgeStandardSolutionRepository judgeStandardSolutionRepository;
    /** 测试用例生成配置仓储。 */
    private final JudgeTestcaseConfigRepository judgeTestcaseConfigRepository;
    /** 语言资源限制仓储。 */
    private final JudgeLanguageLimitRepository judgeLanguageLimitRepository;

    /**
     * 新增或替换某道算法题的当前判题配置。
     *
     * @param questionId
     *            算法题目主键。
     * @param command
     *            管理员提交的 generator、标准解、测试用例和 benchmark 配置。
     * @return 保存后的当前判题配置结果。
     */
    @Override
    @Transactional
    public JudgeProblemConfigResult upsert(Long questionId, JudgeProblemConfigCommands.UpsertCommand command) {
        validatePrimarySolution(command);

        byte[] generatorBytes = command.generatorSource().getBytes(StandardCharsets.UTF_8);
        String generatorHash = sha256(generatorBytes);

        // 判题相关内容（generator/标准解/用例配置/benchmark 参数）未改动时保留既有状态，
        // 否则普通保存会把已生成的配置打回草稿，使题目在无需重新生成时失去可判题资格。
        Optional<JudgeProblemConfig> previousConfig = judgeProblemConfigRepository.findByQuestionId(questionId);
        boolean contentUnchanged = previousConfig.isPresent()
                && isJudgeContentUnchanged(previousConfig.get(), command, generatorHash);
        String status = STATUS_DRAFT;
        if (contentUnchanged) {
            status = Optional.ofNullable(previousConfig.get().getStatus()).orElse(STATUS_DRAFT);
        }

        String basePrefix = "questions/%d/current".formatted(questionId);
        String generatorKey = "%s/generator/%s.%s"
                .formatted(basePrefix, generatorHash, extension(command.generatorLanguage()));
        judgeAssetStorage.put(generatorKey, generatorBytes, CONTENT_TYPE_TEXT);

        JudgeProblemConfig config = JudgeProblemConfig.create(
                questionId,
                command.generatorLanguage(),
                generatorKey,
                generatorHash,
                command.primaryStandardLanguage(),
                command.benchmarkRepeatTimes(),
                command.marginMultiplier(),
                command.minExtraMs(),
                command.roundToMs(),
                status);
        Long configId = judgeProblemConfigRepository.upsertCurrentConfig(config);
        if (!contentUnchanged) {
            // 只在判题内容真的变化时重建子表，保留未变配置已有的 benchmark 结果。
            replaceStandardSolutions(configId, questionId, basePrefix, command.standardSolutions());
            replaceTestcaseConfigs(configId, command.testcases());
        }

        byte[] manifestBytes = buildManifest(questionId, configId, command, generatorKey, generatorHash);
        String manifestHash = sha256(manifestBytes);
        String manifestKey = "%s/manifest/%s.json".formatted(basePrefix, manifestHash);
        judgeAssetStorage.put(manifestKey, manifestBytes, CONTENT_TYPE_JSON);
        judgeProblemConfigRepository.updateManifest(configId, manifestKey, manifestHash);

        return findByQuestionId(questionId)
                .orElseThrow(() -> new IllegalStateException("判题配置保存后查询失败"));
    }

    /**
     * 查询某道算法题的当前判题配置。
     *
     * @param questionId
     *            算法题目主键。
     * @return 当前判题配置；不存在时返回空。
     */
    @Override
    public Optional<JudgeProblemConfigResult> findByQuestionId(Long questionId) {
        return judgeProblemConfigRepository.findByQuestionId(questionId)
                .map(
                        config -> new JudgeProblemConfigResult(
                                config.getId(),
                                config.getQuestionId(),
                                config.getGeneratorLanguage(),
                                config.getGeneratorObjectKey(),
                                readSource(config.getGeneratorObjectKey()),
                                config.getManifestObjectKey(),
                                config.getPrimaryStandardLanguage(),
                                config.getStatus(),
                                config.getBenchmarkRepeatTimes(),
                                config.getMarginMultiplier().doubleValue(),
                                config.getMinExtraMs(),
                                config.getRoundToMs(),
                                standardSolutions(config.getId()),
                                testcaseConfigs(config.getId()),
                                languageLimits(config.getQuestionId())));
    }

    /**
     * 请求 Judge Service 生成测试数据。
     *
     * @param questionId
     *            算法题目主键。
     * @return 无返回值。
     */
    @Override
    @Transactional
    public void requestGeneration(Long questionId) {
        Long configId = judgeProblemConfigRepository.findIdByQuestionId(questionId)
                .orElseThrow(() -> new IllegalArgumentException("判题配置不存在"));
        judgeProblemConfigRepository.markGenerating(configId);
        // 生成任务只传 configId，Judge Service 自行读取 manifest、generator 和标准解元数据。
        testDataGenerationPublisher.publish(configId);
    }

    /**
     * 确认某道算法题指定语言的正式判题资源限制。
     *
     * @param questionId
     *            算法题目主键。
     * @param language
     *            编程语言值。
     * @param command
     *            管理员确认的时间、内存和输出限制。
     * @return 无返回值。
     */
    @Override
    @Transactional
    public void confirmLanguageLimit(
            Long questionId,
            String language,
            JudgeProblemConfigCommands.ConfirmLanguageLimitCommand command) {
        Long configId = judgeProblemConfigRepository.findIdByQuestionId(questionId)
                .orElseThrow(() -> new IllegalArgumentException("判题配置不存在"));
        JudgeLanguageLimit limit = JudgeLanguageLimit.createConfirmed(
                questionId,
                language,
                command.timeLimitMs(),
                command.memoryLimitKb(),
                command.outputLimitKb(),
                configId);
        judgeLanguageLimitRepository.upsertConfirmedLimit(limit);
        judgeProblemConfigRepository.markReadyIfGenerated(configId);
    }

    /**
     * 判断本次提交是否与已保存的判题内容完全一致。
     *
     * @param previousConfig
     *            数据库中当前的判题配置。
     * @param command
     *            本次提交的判题配置。
     * @param generatorHash
     *            本次 generator 源码的 SHA-256 哈希。
     * @return true 表示判题内容未变化。
     */
    private boolean isJudgeContentUnchanged(
            JudgeProblemConfig previousConfig,
            JudgeProblemConfigCommands.UpsertCommand command,
            String generatorHash) {
        String previousFingerprint = JudgeConfigContentPolicy.fingerprint(
                previousConfig.getGeneratorLanguage(),
                previousConfig.getGeneratorObjectHash(),
                previousConfig.getPrimaryStandardLanguage(),
                previousConfig.getBenchmarkRepeatTimes(),
                previousConfig.getMarginMultiplier(),
                previousConfig.getMinExtraMs(),
                previousConfig.getRoundToMs(),
                standardSolutionContents(previousConfig.getId()),
                testcaseContents(previousConfig.getId()));
        String submittedFingerprint = JudgeConfigContentPolicy.fingerprint(
                command.generatorLanguage(),
                generatorHash,
                command.primaryStandardLanguage(),
                command.benchmarkRepeatTimes(),
                command.marginMultiplier(),
                command.minExtraMs(),
                command.roundToMs(),
                command.standardSolutions()
                        .stream()
                        .map(
                                solution -> new JudgeConfigContentPolicy.StandardSolutionContent(
                                        solution.language(),
                                        sha256(solution.source().getBytes(StandardCharsets.UTF_8)),
                                        Boolean.TRUE.equals(solution.primarySolution())))
                        .toList(),
                command.testcases()
                        .stream()
                        .map(
                                testcase -> new JudgeConfigContentPolicy.TestcaseContent(
                                        testcase.caseNo(),
                                        testcase.category(),
                                        canonicalArgs(testcase.generatorArgs()),
                                        testcase.weight(),
                                        testcase.hidden() == null || testcase.hidden(),
                                        Boolean.TRUE.equals(testcase.sample()),
                                        testcase.description()))
                        .toList());
        return previousFingerprint.equals(submittedFingerprint);
    }

    /**
     * 读取当前配置已持久化的标准解内容指纹项。
     *
     * @param configId
     *            判题配置主键。
     * @return 标准解内容列表。
     */
    private List<JudgeConfigContentPolicy.StandardSolutionContent> standardSolutionContents(Long configId) {
        return judgeStandardSolutionRepository.findByConfigId(configId)
                .stream()
                .map(
                        solution -> new JudgeConfigContentPolicy.StandardSolutionContent(
                                solution.getLanguage(),
                                solution.getObjectHash(),
                                Boolean.TRUE.equals(solution.getPrimarySolution())))
                .toList();
    }

    /**
     * 读取当前配置已持久化的测试用例生成配置指纹项。
     *
     * @param configId
     *            判题配置主键。
     * @return 测试用例生成配置列表。
     */
    private List<JudgeConfigContentPolicy.TestcaseContent> testcaseContents(Long configId) {
        return judgeTestcaseConfigRepository.findByConfigId(configId)
                .stream()
                .map(
                        testcase -> new JudgeConfigContentPolicy.TestcaseContent(
                                testcase.getCaseNo(),
                                testcase.getCategory(),
                                canonicalArgs(testcase.getGeneratorArgs()),
                                testcase.getWeight(),
                                Boolean.TRUE.equals(testcase.getHidden()),
                                Boolean.TRUE.equals(testcase.getSample()),
                                testcase.getDescription()))
                .toList();
    }

    /**
     * 将 JSON 参数字符串归一化为对象键有序的稳定文本。
     *
     * <p>
     * PostgreSQL 的 jsonb 会重排对象键，若直接比较原始文本，同一份参数在读写一轮后会被误判为已变更。
     * </p>
     *
     * @param json
     *            数据库中的 JSON 字符串。
     * @return 归一化后的 JSON 文本。
     */
    private String canonicalArgs(String json) {
        try {
            return canonicalArgs(objectMapper.readTree(json == null ? "{}" : json));
        } catch (Exception ex) {
            // 无法解析时退回原文，宁可变更为草稿也不要误判为未变。
            return json == null ? "{}" : json;
        }
    }

    /**
     * 将 JSON 节点归一化为对象键有序的稳定文本。
     *
     * @param node
     *            请求中的 JSON 参数节点。
     * @return 归一化后的 JSON 文本。
     */
    private String canonicalArgs(JsonNode node) {
        StringBuilder builder = new StringBuilder();
        appendCanonicalJson(builder, node);
        return builder.toString();
    }

    private void appendCanonicalJson(StringBuilder builder, JsonNode node) {
        if (node == null || node.isNull()) {
            builder.append("null");
            return;
        }
        if (node.isObject()) {
            TreeMap<String, JsonNode> sorted = new TreeMap<>();
            node.fields().forEachRemaining(entry -> sorted.put(entry.getKey(), entry.getValue()));
            builder.append('{');
            boolean first = true;
            for (Map.Entry<String, JsonNode> entry : sorted.entrySet()) {
                if (!first) {
                    builder.append(',');
                }
                first = false;
                appendJsonString(builder, entry.getKey());
                builder.append(':');
                appendCanonicalJson(builder, entry.getValue());
            }
            builder.append('}');
            return;
        }
        if (node.isArray()) {
            builder.append('[');
            for (int i = 0; i < node.size(); i++) {
                if (i > 0) {
                    builder.append(',');
                }
                appendCanonicalJson(builder, node.get(i));
            }
            builder.append(']');
            return;
        }
        if (node.isNumber()) {
            // jsonb 会将 1.0 归一为数字值，这里统一去掉无意义的尾随零。
            builder.append(node.decimalValue().stripTrailingZeros().toPlainString());
            return;
        }
        if (node.isBoolean()) {
            builder.append(node.booleanValue());
            return;
        }
        appendJsonString(builder, node.asText());
    }

    private void appendJsonString(StringBuilder builder, String value) {
        builder.append('"');
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '"' || ch == '\\') {
                builder.append('\\').append(ch);
            } else if (ch == '\n') {
                builder.append("\\n");
            } else if (ch == '\r') {
                builder.append("\\r");
            } else if (ch == '\t') {
                builder.append("\\t");
            } else {
                builder.append(ch);
            }
        }
        builder.append('"');
    }

    /**
     * 校验用于生成标准输出的主标准解语言是否存在于标准解列表中。
     *
     * @param command
     *            管理员提交的判题配置。
     * @return 无返回值。
     */
    private void validatePrimarySolution(JudgeProblemConfigCommands.UpsertCommand command) {
        boolean exists = command.standardSolutions()
                .stream()
                .anyMatch(solution -> command.primaryStandardLanguage().equals(solution.language()));
        if (!exists) {
            throw new IllegalArgumentException("主标准解语言必须存在于标准解列表中");
        }
    }

    /**
     * 替换某个判题配置下的标准解文件和元数据。
     *
     * @param configId
     *            判题配置主键。
     * @param questionId
     *            算法题目主键。
     * @param basePrefix
     *            判题资产对象键基础目录。
     * @param solutions
     *            管理员提交的标准解源码列表。
     * @return 无返回值。
     */
    private void replaceStandardSolutions(
            Long configId,
            Long questionId,
            String basePrefix,
            List<JudgeProblemConfigCommands.StandardSolutionCommand> solutions) {
        judgeStandardSolutionRepository.deleteByConfigId(configId);
        for (JudgeProblemConfigCommands.StandardSolutionCommand solution : solutions) {
            byte[] sourceBytes = solution.source().getBytes(StandardCharsets.UTF_8);
            String hash = sha256(sourceBytes);
            String objectKey = "%s/standard/%s-%s.%s"
                    .formatted(basePrefix, solution.language(), hash, extension(solution.language()));
            judgeAssetStorage.put(objectKey, sourceBytes, CONTENT_TYPE_TEXT);
            JudgeStandardSolution entity = JudgeStandardSolution.create(
                    configId,
                    questionId,
                    solution.language(),
                    objectKey,
                    hash,
                    Boolean.TRUE.equals(solution.primarySolution()),
                    "PENDING");
            judgeStandardSolutionRepository.save(entity);
        }
    }

    /**
     * 替换某个判题配置下的测试用例生成配置。
     *
     * @param configId
     *            判题配置主键。
     * @param testcases
     *            管理员提交的测试用例配置列表。
     * @return 无返回值。
     */
    private void replaceTestcaseConfigs(
            Long configId,
            List<JudgeProblemConfigCommands.TestcaseConfigCommand> testcases) {
        judgeTestcaseConfigRepository.deleteByConfigId(configId);
        for (JudgeProblemConfigCommands.TestcaseConfigCommand testcase : testcases) {
            JudgeTestcaseConfig entity = JudgeTestcaseConfig.create(
                    configId,
                    testcase.caseNo(),
                    testcase.category(),
                    json(testcase.generatorArgs()),
                    testcase.weight(),
                    testcase.hidden() == null || testcase.hidden(),
                    Boolean.TRUE.equals(testcase.sample()),
                    testcase.description());
            judgeTestcaseConfigRepository.save(entity);
        }
    }

    /**
     * 根据管理端配置生成系统 manifest 文件内容。
     *
     * @param questionId
     *            算法题目主键。
     * @param configId
     *            判题配置主键。
     * @param command
     *            管理员提交的判题配置。
     * @param generatorKey
     *            generator OSS 对象键。
     * @param generatorHash
     *            generator SHA-256 哈希。
     * @return UTF-8 JSON manifest 字节内容。
     */
    private byte[] buildManifest(
            Long questionId,
            Long configId,
            JudgeProblemConfigCommands.UpsertCommand command,
            String generatorKey,
            String generatorHash) {
        try {
            Map<String, Object> manifest = Map.of(
                    "questionId",
                    questionId,
                    "configId",
                    configId,
                    "generator",
                    Map.of(
                            "language",
                            command.generatorLanguage(),
                            "objectKey",
                            generatorKey,
                            "sha256",
                            generatorHash),
                    "primaryStandardLanguage",
                    command.primaryStandardLanguage(),
                    "benchmark",
                    Map.of(
                            "repeatTimes",
                            command.benchmarkRepeatTimes(),
                            "marginMultiplier",
                            command.marginMultiplier(),
                            "minExtraMs",
                            command.minExtraMs(),
                            "roundToMs",
                            command.roundToMs()),
                    "standardSolutions",
                    command.standardSolutions(),
                    "testcases",
                    command.testcases());
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(manifest);
        } catch (Exception ex) {
            throw new IllegalStateException("生成判题 manifest 失败", ex);
        }
    }

    /**
     * 查询某个判题配置下的标准解结果对象。
     *
     * @param configId
     *            判题配置主键。
     * @return 标准解结果对象列表。
     */
    private List<JudgeStandardSolutionResult> standardSolutions(Long configId) {
        return judgeStandardSolutionRepository.findByConfigId(configId)
                .stream()
                .map(
                        solution -> new JudgeStandardSolutionResult(
                                solution.getLanguage(),
                                solution.getObjectKey(),
                                readSource(solution.getObjectKey()),
                                solution.getObjectHash(),
                                solution.getPrimarySolution(),
                                solution.getBenchmarkStatus(),
                                solution.getP95TimeMs(),
                                solution.getMaxTimeMs(),
                                solution.getPeakMemoryKb(),
                                solution.getSuggestedTimeLimitMs(),
                                solution.getBenchmarkMessage()))
                .toList();
    }

    /**
     * 查询题目全部语言资源限制结果。
     *
     * @param questionId
     *            算法题目主键。
     * @return 按语言名升序排列的语言资源限制结果列表。
     */
    private List<JudgeLanguageLimitResult> languageLimits(Long questionId) {
        return judgeLanguageLimitRepository.findByQuestionId(questionId)
                .stream()
                .map(
                        limit -> new JudgeLanguageLimitResult(
                                limit.getLanguage(),
                                limit.getTimeLimitMs(),
                                limit.getMemoryLimitKb(),
                                limit.getOutputLimitKb(),
                                limit.getConfirmed(),
                                limit.getConfirmedAt()))
                .toList();
    }

    /**
     * 从判题资产存储读取源码内容。
     *
     * @param objectKey
     *            OSS 对象键；为空时返回空字符串。
     * @return UTF-8 解码后的源码文本。
     */
    private String readSource(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return "";
        }
        try {
            return new String(judgeAssetStorage.get(objectKey), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * 查询某个判题配置下的测试用例配置结果对象。
     *
     * @param configId
     *            判题配置主键。
     * @return 测试用例配置结果对象列表。
     */
    private List<JudgeTestcaseConfigResult> testcaseConfigs(Long configId) {
        return judgeTestcaseConfigRepository.findByConfigId(configId)
                .stream()
                .map(
                        testcase -> new JudgeTestcaseConfigResult(
                                testcase.getCaseNo(),
                                testcase.getCategory(),
                                readJson(testcase.getGeneratorArgs()),
                                testcase.getWeight(),
                                testcase.getHidden(),
                                testcase.getSample(),
                                testcase.getDescription()))
                .toList();
    }

    /**
     * 将可空 JSON 节点转换为数据库可存储的 JSON 字符串。
     *
     * @param node
     *            请求中的 JSON 参数节点。
     * @return JSON 字符串，空值返回空对象。
     */
    private String json(JsonNode node) {
        return node == null ? "{}" : node.toString();
    }

    /**
     * 将数据库 JSON 字符串解析为响应 JSON 节点。
     *
     * @param json
     *            数据库中的 JSON 字符串。
     * @return 解析后的 JSON 节点。
     */
    private JsonNode readJson(String json) {
        try {
            return objectMapper.readTree(json == null ? "{}" : json);
        } catch (Exception ex) {
            throw new IllegalStateException("读取测试用例参数失败", ex);
        }
    }

    /**
     * 根据语言值推断源码文件扩展名。
     *
     * @param language
     *            编程语言值。
     * @return 用于 OSS 对象键的文件扩展名。
     */
    private String extension(String language) {
        return switch (language.toUpperCase()) {
            case "JAVA" -> "java";
            case "PYTHON", "PYTHON3" -> "py";
            case "CPP", "CXX", "C_PLUS_PLUS" -> "cpp";
            case "C" -> "c";
            case "JAVASCRIPT", "JS" -> "js";
            default -> "txt";
        };
    }

    /**
     * 计算文件内容 SHA-256 哈希。
     *
     * @param content
     *            文件字节内容。
     * @return 十六进制 SHA-256 哈希。
     */
    private String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (Exception ex) {
            throw new IllegalStateException("计算文件哈希失败", ex);
        }
    }
}
