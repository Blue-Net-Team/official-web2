package com.bluenet.judge.application.dto;

import com.bluenet.judge.infrastructure.repository.dataobject.JudgeProblemConfigRecord;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeStandardSolutionRecord;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

/**
 * 判题 manifest 和关联源码资产加载结果。
 *
 * @param config
 *            判题配置记录。
 * @param manifest
 *            manifest JSON 内容。
 * @param generatorSource
 *            generator 源码字节内容。
 * @param standardSolutionSources
 *            按语言索引的标准解源码字节内容。
 * @param standardSolutions
 *            标准解文件记录列表。
 */
public record JudgeManifestBundle(
        JudgeProblemConfigRecord config,
        JsonNode manifest,
        byte[] generatorSource,
        Map<String, byte[]> standardSolutionSources,
        List<JudgeStandardSolutionRecord> standardSolutions) {
}
