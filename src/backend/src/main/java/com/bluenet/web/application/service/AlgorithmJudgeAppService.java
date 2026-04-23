package com.bluenet.web.application.service;

import com.bluenet.web.application.AlgorithmJudgeResult;
import com.bluenet.web.application.command.algorithm_judge.AlgorithmJudgeCommands;

/**
 * 算法判题应用服务接口。
 * <p>
 * 定义了算法判题聚合在应用层的所有业务操作。
 * </p>
 */
public interface AlgorithmJudgeAppService {

    /**
     * 运行算法题代码
     *
     * @param command
     *            运行命令
     * @return 提交结果
     */
    AlgorithmJudgeResult.SubmitResult run(AlgorithmJudgeCommands.RunCommand command);

    /**
     * 提交算法题答案
     *
     * @param command
     *            提交命令
     * @return 提交结果
     */
    AlgorithmJudgeResult.SubmitResult submit(AlgorithmJudgeCommands.SubmitCommand command);

    /**
     * 轮询算法判题任务
     *
     * @param jobId
     *            任务ID
     * @return 轮询结果
     */
    AlgorithmJudgeResult.PollResult getJob(Long jobId);
}
