package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.entity.AssessmentJudgement;

/**
 * 考核题目评判领域服务接口。
 * <p>
 * 仅保留需要并发原子性 upsert 的最终评分确认逻辑，CRUD 已下沉到实体和仓储。
 * </p>
 */
public interface AssessmentJudgementDomainService {

    /**
     * 方向管理员确认最终评分。
     * <p>
     * 利用数据库唯一索引实现原子性的 upsert，避免并发场景下重复插入。
     * </p>
     *
     * @param judgement
     *            评判记录实体，source 应为 ADMIN_FINALIZED
     * @return 创建或更新后的评判记录
     */
    AssessmentJudgement finalizeJudgement(AssessmentJudgement judgement);
}
