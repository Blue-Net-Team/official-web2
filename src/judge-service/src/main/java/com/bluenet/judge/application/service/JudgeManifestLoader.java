package com.bluenet.judge.application.service;

import com.bluenet.judge.application.dto.JudgeManifestBundle;
import com.bluenet.judge.infrastructure.repository.JudgeMetadataRepository;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeProblemConfigRecord;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeStandardSolutionRecord;
import com.bluenet.judge.infrastructure.storage.JudgeAssetStorage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 判题 manifest 和相关源码资产加载器。
 */
@Service
@RequiredArgsConstructor
public class JudgeManifestLoader {
    private final JudgeMetadataRepository judgeMetadataRepository;
    private final JudgeAssetStorage judgeAssetStorage;
    private final ObjectMapper objectMapper;

    /**
     * 加载判题配置、manifest、generator 和标准解源码。
     *
     * @param configId
     *            判题配置主键。
     * @return manifest 加载结果。
     */
    public JudgeManifestBundle load(Long configId) {
        JudgeProblemConfigRecord config = judgeMetadataRepository.findConfig(configId)
                .orElseThrow(() -> new IllegalArgumentException("判题配置不存在：" + configId));
        if (config.getManifestObjectKey() == null || config.getManifestObjectKey().isBlank()) {
            throw new IllegalStateException("判题配置尚未生成清单文件：" + configId);
        }

        JsonNode manifest = readManifest(judgeAssetStorage.get(config.getManifestObjectKey()));
        byte[] generatorSource = judgeAssetStorage.get(config.getGeneratorObjectKey());
        List<JudgeStandardSolutionRecord> standards = judgeMetadataRepository.findStandardSolutions(configId);
        Map<String, byte[]> standardSources = new LinkedHashMap<>();
        for (JudgeStandardSolutionRecord standard : standards) {
            // 按语言索引标准解源码，便于后续 benchmark 和标准输出生成稳定选择。
            standardSources.put(standard.getLanguage(), judgeAssetStorage.get(standard.getObjectKey()));
        }
        return new JudgeManifestBundle(config, manifest, generatorSource, standardSources, standards);
    }

    /**
     * 解析 manifest JSON。
     *
     * @param manifestBytes
     *            manifest 文件字节内容。
     * @return manifest JSON 节点。
     */
    private JsonNode readManifest(byte[] manifestBytes) {
        try {
            return objectMapper.readTree(manifestBytes);
        } catch (Exception ex) {
            throw new IllegalStateException("解析判题清单文件失败", ex);
        }
    }
}
