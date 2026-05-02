package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.judge.JudgeProblemConfigDTO;
import com.bluenet.web.api.dto.judge.JudgeStandardSolutionDTO;
import com.bluenet.web.api.dto.judge.JudgeTestcaseConfigDTO;
import com.bluenet.web.api.dto.judge.ConfirmJudgeLanguageLimitRequestDTO;
import com.bluenet.web.api.dto.judge.UpsertJudgeProblemConfigRequestDTO;
import com.bluenet.web.application.service.JudgeProblemConfigAdminService;
import com.bluenet.web.infrastructure.judge.JudgeTestDataGenerationPublisher;
import com.bluenet.web.infrastructure.repository.dataobject.JudgeProblemConfigDO;
import com.bluenet.web.infrastructure.repository.dataobject.JudgeStandardSolutionDO;
import com.bluenet.web.infrastructure.repository.dataobject.JudgeTestcaseConfigDO;
import com.bluenet.web.infrastructure.repository.mapper.JudgeLanguageLimitMapper;
import com.bluenet.web.infrastructure.repository.mapper.JudgeProblemConfigMapper;
import com.bluenet.web.infrastructure.repository.mapper.JudgeStandardSolutionMapper;
import com.bluenet.web.infrastructure.repository.mapper.JudgeTestcaseConfigMapper;
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

    /** JSON 序列化工具。 */
    private final ObjectMapper objectMapper;
    /** 判题资产对象存储。 */
    private final JudgeAssetStorage judgeAssetStorage;
    /** 测试数据生成消息发布器。 */
    private final JudgeTestDataGenerationPublisher testDataGenerationPublisher;
    /** 判题配置 mapper。 */
    private final JudgeProblemConfigMapper judgeProblemConfigMapper;
    /** 标准解 mapper。 */
    private final JudgeStandardSolutionMapper judgeStandardSolutionMapper;
    /** 测试用例生成配置 mapper。 */
    private final JudgeTestcaseConfigMapper judgeTestcaseConfigMapper;
    /** 语言资源限制 mapper。 */
    private final JudgeLanguageLimitMapper judgeLanguageLimitMapper;

    /**
     * 新增或替换某道算法题的当前判题配置。
     *
     * @param questionId
     *            算法题目主键。
     * @param request
     *            管理员提交的 generator、标准解、测试用例和 benchmark 配置。
     * @return 保存后的当前判题配置视图。
     */
    @Override
    @Transactional
    public JudgeProblemConfigDTO upsert(Long questionId, UpsertJudgeProblemConfigRequestDTO request) {
        validatePrimarySolution(request);

        byte[] generatorBytes = request.generatorSource().getBytes(StandardCharsets.UTF_8);
        String generatorHash = sha256(generatorBytes);
        String basePrefix = "questions/%d/current".formatted(questionId);
        String generatorKey = "%s/generator/%s.%s"
                .formatted(basePrefix, generatorHash, extension(request.generatorLanguage()));
        judgeAssetStorage.put(generatorKey, generatorBytes, CONTENT_TYPE_TEXT);

        Long configId = judgeProblemConfigMapper.upsertCurrentConfig(
                JudgeProblemConfigDO.builder()
                        .questionId(questionId)
                        .generatorLanguage(request.generatorLanguage())
                        .generatorObjectKey(generatorKey)
                        .generatorObjectHash(generatorHash)
                        .primaryStandardLanguage(request.primaryStandardLanguage())
                        .benchmarkRepeatTimes(request.benchmarkRepeatTimes())
                        .marginMultiplier(request.marginMultiplier())
                        .minExtraMs(request.minExtraMs())
                        .roundToMs(request.roundToMs())
                        .build());
        replaceStandardSolutions(configId, questionId, basePrefix, request.standardSolutions());
        replaceTestcaseConfigs(configId, request.testcases());

        byte[] manifestBytes = buildManifest(questionId, configId, request, generatorKey, generatorHash);
        String manifestHash = sha256(manifestBytes);
        String manifestKey = "%s/manifest/%s.json".formatted(basePrefix, manifestHash);
        judgeAssetStorage.put(manifestKey, manifestBytes, CONTENT_TYPE_JSON);
        judgeProblemConfigMapper.updateManifest(configId, manifestKey, manifestHash);

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
    public Optional<JudgeProblemConfigDTO> findByQuestionId(Long questionId) {
        Optional<JudgeProblemConfigDO> config = Optional
                .ofNullable(judgeProblemConfigMapper.selectByQuestionId(questionId));
        return config.map(
                row -> new JudgeProblemConfigDTO(
                        row.getId(),
                        row.getQuestionId(),
                        row.getGeneratorLanguage(),
                        row.getGeneratorObjectKey(),
                        readSource(row.getGeneratorObjectKey()),
                        row.getManifestObjectKey(),
                        row.getPrimaryStandardLanguage(),
                        row.getStatus(),
                        row.getBenchmarkRepeatTimes(),
                        row.getMarginMultiplier().doubleValue(),
                        row.getMinExtraMs(),
                        row.getRoundToMs(),
                        standardSolutions(row.getId()),
                        testcaseConfigs(row.getId())));
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
        Long configId = Optional.ofNullable(judgeProblemConfigMapper.selectIdByQuestionId(questionId))
                .orElseThrow(() -> new IllegalArgumentException("判题配置不存在"));
        judgeProblemConfigMapper.markGenerating(configId);
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
     * @param request
     *            管理员确认的时间、内存和输出限制。
     * @return 无返回值。
     */
    @Override
    @Transactional
    public void confirmLanguageLimit(
            Long questionId,
            String language,
            ConfirmJudgeLanguageLimitRequestDTO request) {
        Long configId = Optional.ofNullable(judgeProblemConfigMapper.selectIdByQuestionId(questionId))
                .orElseThrow(() -> new IllegalArgumentException("判题配置不存在"));
        judgeLanguageLimitMapper.upsertConfirmedLimit(
                questionId,
                language,
                request.timeLimitMs(),
                request.memoryLimitKb(),
                request.outputLimitKb(),
                configId);
        judgeProblemConfigMapper.markReadyIfGenerated(configId);
    }

    /**
     * 校验用于生成标准输出的主标准解语言是否存在于标准解列表中。
     *
     * @param request
     *            管理员提交的判题配置。
     * @return 无返回值。
     */
    private void validatePrimarySolution(UpsertJudgeProblemConfigRequestDTO request) {
        boolean exists = request.standardSolutions()
                .stream()
                .anyMatch(solution -> request.primaryStandardLanguage().equals(solution.language()));
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
            List<UpsertJudgeProblemConfigRequestDTO.StandardSolutionRequest> solutions) {
        judgeStandardSolutionMapper.deleteByConfigId(configId);
        for (UpsertJudgeProblemConfigRequestDTO.StandardSolutionRequest solution : solutions) {
            byte[] sourceBytes = solution.source().getBytes(StandardCharsets.UTF_8);
            String hash = sha256(sourceBytes);
            String objectKey = "%s/standard/%s-%s.%s"
                    .formatted(basePrefix, solution.language(), hash, extension(solution.language()));
            judgeAssetStorage.put(objectKey, sourceBytes, CONTENT_TYPE_TEXT);
            judgeStandardSolutionMapper.insert(
                    JudgeStandardSolutionDO.builder()
                            .configId(configId)
                            .questionId(questionId)
                            .language(solution.language())
                            .objectKey(objectKey)
                            .objectHash(hash)
                            .primarySolution(Boolean.TRUE.equals(solution.primarySolution()))
                            .benchmarkStatus("PENDING")
                            .build());
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
            List<UpsertJudgeProblemConfigRequestDTO.TestcaseConfigRequest> testcases) {
        judgeTestcaseConfigMapper.deleteByConfigId(configId);
        for (UpsertJudgeProblemConfigRequestDTO.TestcaseConfigRequest testcase : testcases) {
            judgeTestcaseConfigMapper.insertConfig(
                    JudgeTestcaseConfigDO.builder()
                            .configId(configId)
                            .caseNo(testcase.caseNo())
                            .category(testcase.category())
                            .generatorArgs(json(testcase.generatorArgs()))
                            .weight(testcase.weight())
                            .hidden(testcase.hidden() == null || testcase.hidden())
                            .sample(Boolean.TRUE.equals(testcase.sample()))
                            .description(testcase.description())
                            .build());
        }
    }

    /**
     * 根据管理端配置生成系统 manifest 文件内容。
     *
     * @param questionId
     *            算法题目主键。
     * @param configId
     *            判题配置主键。
     * @param request
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
            UpsertJudgeProblemConfigRequestDTO request,
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
                            request.generatorLanguage(),
                            "objectKey",
                            generatorKey,
                            "sha256",
                            generatorHash),
                    "primaryStandardLanguage",
                    request.primaryStandardLanguage(),
                    "benchmark",
                    Map.of(
                            "repeatTimes",
                            request.benchmarkRepeatTimes(),
                            "marginMultiplier",
                            request.marginMultiplier(),
                            "minExtraMs",
                            request.minExtraMs(),
                            "roundToMs",
                            request.roundToMs()),
                    "standardSolutions",
                    request.standardSolutions(),
                    "testcases",
                    request.testcases());
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(manifest);
        } catch (Exception ex) {
            throw new IllegalStateException("生成判题 manifest 失败", ex);
        }
    }

    /**
     * 查询某个判题配置下的标准解响应对象。
     *
     * @param configId
     *            判题配置主键。
     * @return 标准解响应对象列表。
     */
    private List<JudgeStandardSolutionDTO> standardSolutions(Long configId) {
        return judgeStandardSolutionMapper.selectByConfigId(configId)
                .stream()
                .map(
                        solution -> new JudgeStandardSolutionDTO(
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
     * 查询某个判题配置下的测试用例配置响应对象。
     *
     * @param configId
     *            判题配置主键。
     * @return 测试用例配置响应对象列表。
     */
    private List<JudgeTestcaseConfigDTO> testcaseConfigs(Long configId) {
        return judgeTestcaseConfigMapper.selectByConfigId(configId)
                .stream()
                .map(
                        testcase -> new JudgeTestcaseConfigDTO(
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
