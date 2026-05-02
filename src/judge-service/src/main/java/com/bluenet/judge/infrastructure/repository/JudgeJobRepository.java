package com.bluenet.judge.infrastructure.repository;

import com.bluenet.judge.infrastructure.repository.dataobject.JudgeJobRecord;
import com.bluenet.judge.infrastructure.repository.mapper.JudgeJobMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 判题任务持久化访问入口。
 * <p>
 * SQL 统一放在 MyBatis mapper XML 中，本类只保留仓储语义。
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class JudgeJobRepository {
    /** 判题任务 MyBatis mapper。 */
    private final JudgeJobMapper judgeJobMapper;

    /**
     * 查询判题任务（不限类型）。
     *
     * @param jobId
     *            判题任务主键。
     * @return 判题任务记录；不存在时返回空。
     */
    public Optional<JudgeJobRecord> findById(Long jobId) {
        return Optional.ofNullable(judgeJobMapper.selectById(jobId));
    }

    /**
     * 查询正式判题任务。
     *
     * @param jobId
     *            判题任务主键。
     * @return 正式判题任务记录；不存在时返回空。
     */
    public Optional<JudgeJobRecord> findFormalJob(Long jobId) {
        // The worker reads the immutable source_code snapshot instead of the mutable
        // answer content.
        return Optional.ofNullable(judgeJobMapper.selectFormalJob(jobId));
    }

    /**
     * 标记判题任务为运行中。
     *
     * @param jobId
     *            判题任务主键。
     * @return 无返回值。
     */
    public void markRunning(Long jobId) {
        judgeJobMapper.markRunning(jobId);
    }

    /**
     * 标记判题任务为成功完成。
     *
     * @param jobId
     *            判题任务主键。
     * @return 无返回值。
     */
    public void markSucceeded(Long jobId) {
        judgeJobMapper.markSucceeded(jobId);
    }

    /**
     * 标记判题任务为需要人工复核。
     *
     * @param jobId
     *            判题任务主键。
     * @param message
     *            复核原因。
     * @return 无返回值。
     */
    public void markReviewRequired(Long jobId, String message) {
        judgeJobMapper.markReviewRequired(jobId, message);
    }
}
