package com.bluenet.judge.application.service;

import com.bluenet.judge.application.dto.FormalTestcaseBundle;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeTestCaseRecord;
import com.bluenet.judge.infrastructure.storage.JudgeAssetStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 正式判题测试用例文件加载器。
 */
@Service
@RequiredArgsConstructor
public class FormalTestcaseLoader {
    /** 判题资产对象存储。 */
    private final JudgeAssetStorage judgeAssetStorage;

    /**
     * 从判题 bucket 加载一组正式测试用例的输入和期望输出文件。
     *
     * @param testcases
     *            测试用例索引记录列表。
     * @return 带文件内容的测试用例包列表。
     */
    public List<FormalTestcaseBundle> load(List<JudgeTestCaseRecord> testcases) {
        return testcases.stream()
                .map(
                        testcase -> new FormalTestcaseBundle(
                                testcase,
                                judgeAssetStorage.get(testcase.getInputObjectKey()),
                                judgeAssetStorage.get(testcase.getOutputObjectKey())))
                .toList();
    }
}
