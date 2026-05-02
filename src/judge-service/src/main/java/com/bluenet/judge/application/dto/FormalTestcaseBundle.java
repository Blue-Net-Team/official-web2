package com.bluenet.judge.application.dto;

import com.bluenet.judge.infrastructure.repository.dataobject.JudgeTestCaseRecord;

/**
 * 正式判题测试用例文件内容包。
 *
 * @param testcase
 *            测试用例索引记录。
 * @param input
 *            输入文件字节内容。
 * @param expectedOutput
 *            期望输出文件字节内容。
 */
public record FormalTestcaseBundle(
        JudgeTestCaseRecord testcase,
        byte[] input,
        byte[] expectedOutput) {
}
