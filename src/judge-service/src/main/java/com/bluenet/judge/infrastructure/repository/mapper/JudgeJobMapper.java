package com.bluenet.judge.infrastructure.repository.mapper;

import com.bluenet.judge.infrastructure.repository.dataobject.JudgeJobRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface JudgeJobMapper {
    /**
     * 查询判题任务（不限类型）。
     *
     * @param jobId
     *            判题任务主键。
     * @return 判题任务记录；不存在时为 null。
     */
    JudgeJobRecord selectById(@Param("jobId") Long jobId);

    /**
     * 查询正式判题任务。
     *
     * @param jobId
     *            判题任务主键。
     * @return 正式判题任务记录；不存在时为 null。
     */
    JudgeJobRecord selectFormalJob(@Param("jobId") Long jobId);

    /**
     * 标记判题任务为运行中。
     *
     * @param jobId
     *            判题任务主键。
     * @return 无返回值。
     */
    void markRunning(@Param("jobId") Long jobId);

    /**
     * 标记判题任务为成功完成。
     *
     * @param jobId
     *            判题任务主键。
     * @return 无返回值。
     */
    void markSucceeded(@Param("jobId") Long jobId);

    /**
     * 标记判题任务为需要人工复核。
     *
     * @param jobId
     *            判题任务主键。
     * @param message
     *            复核原因。
     * @return 无返回值。
     */
    void markReviewRequired(@Param("jobId") Long jobId, @Param("message") String message);
}
