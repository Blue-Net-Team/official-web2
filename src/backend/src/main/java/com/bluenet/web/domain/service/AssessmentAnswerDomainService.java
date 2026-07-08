package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.User;

import java.util.List;

/**
 * 考核答案领域服务。
 * <p>
 * 封装答案提交/更新的跨聚合业务规则。本服务只负责校验、生成/修改领域实体， 不直接调用 Repository
 * 进行持久化；持久化与事务由应用服务控制，以保证原子性。
 * </p>
 */
public interface AssessmentAnswerDomainService {

    /**
     * 校验并创建当前用户的答案实体（未持久化）。
     * <p>
     * 组队文件上传题会校验队长身份并设置 {@code teamId}。
     * </p>
     *
     * @param user
     *            当前用户
     * @param question
     *            目标题目
     * @param content
     *            答案内容
     * @param language
     *            编程语言
     * @param fileId
     *            上传文件 ID
     * @return 创建后的答案实体
     */
    AssessmentAnswer prepareAnswer(User user, AssessmentQuestion question,
            String content, ProgrammingLanguage language, Long fileId);

    /**
     * 组队场景下，为队员生成待持久化的答案实体列表（队长本身已过滤，已有答案的队员已过滤）。
     *
     * @param leaderAnswer
     *            队长已创建/更新的答案实体
     * @param question
     *            目标题目
     * @param content
     *            答案内容
     * @param language
     *            编程语言
     * @param fileId
     *            上传文件 ID
     * @return 需要批量插入的队员答案实体列表
     */
    List<AssessmentAnswer> prepareTeamMemberAnswers(AssessmentAnswer leaderAnswer, AssessmentQuestion question,
            String content, ProgrammingLanguage language, Long fileId);

    /**
     * 客观题自动评判，返回待持久化的评判实体。
     *
     * @param answer
     *            已保存的答案实体
     * @param question
     *            目标题目
     * @return 自动评判实体；非客观题返回 {@code null}
     */
    AssessmentJudgement prepareObjectiveJudgement(AssessmentAnswer answer, AssessmentQuestion question);

    /**
     * 校验并准备更新后的答案实体列表（均未持久化）。
     * <p>
     * 非组队场景返回只包含 {@code existingAnswer} 的单元素列表； 组队场景返回该题目下全队所有答案（含队长），均已调用
     * {@link AssessmentAnswer#update}。
     * </p>
     *
     * @param user
     *            当前用户
     * @param question
     *            目标题目
     * @param existingAnswer
     *            当前用户的已有答案
     * @param content
     *            答案内容
     * @param language
     *            编程语言
     * @param fileId
     *            上传文件 ID
     * @return 需要持久化的答案实体列表
     */
    List<AssessmentAnswer> prepareUpdatedAnswers(User user, AssessmentQuestion question,
            AssessmentAnswer existingAnswer,
            String content, ProgrammingLanguage language, Long fileId);
}
