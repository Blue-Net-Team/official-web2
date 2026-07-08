package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.readmodel.AssessmentCandidateScoreRowReadModel;
import com.bluenet.web.application.result.assessment.AssessmentQuestionScoreboard;
import com.bluenet.web.domain.model.readmodel.AssessmentQuestionSubmissionHistoryReadModel;
import com.bluenet.web.domain.model.readmodel.AssessmentQuestionSubmissionReadModel;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.infrastructure.repository.converter.AssessmentJudgementRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentJudgementDO;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentJudgementMapper;
import com.bluenet.web.infrastructure.repository.dataobject.query.AssessmentCandidateScoreQueryDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AssessmentQuestionScoreboardQueryDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AssessmentQuestionSubmissionQueryDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class AssessmentJudgementRepositoryImpl implements AssessmentJudgementRepository {
    private final AssessmentJudgementMapper assessmentJudgementMapper;
    private final AssessmentJudgementRepositoryConverter converter;

    /**
     * 保存新的考核评审结果 记录。
     *
     * @param judgement
     *            考核评审结果实体。
     */
    @Override
    public void save(AssessmentJudgement judgement) {
        AssessmentJudgementDO dataObject = converter.toDataObject(judgement);
        if (dataObject.getId() == null) {
            assessmentJudgementMapper.insert(dataObject);
            judgement.setId(dataObject.getId());
        } else {
            int influence = assessmentJudgementMapper.updateById(dataObject);
            if (influence == 0) {
                log.warn("更新考核评判记录失败，judgementId {}", judgement.getId());
                throw new GlobalException("更新考核评判记录失败");
            }
        }
    }

    /**
     * 按主键查询考核评审结果 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的考核评审结果 实体；不存在时为空。
     */
    @Override
    public Optional<AssessmentJudgement> findById(Long id) {
        AssessmentJudgementDO dataObject = assessmentJudgementMapper.selectById(id);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    /**
     * 更新已有考核评审结果 记录。
     *
     * @param judgement
     *            考核评审结果实体。
     */
    /**
     * 查询指定作答的最新评审结果。
     *
     * @param answerId
     *            考核作答主键。
     * @return 查询到的考核评审结果 实体；不存在时为空。
     */
    @Override
    public Optional<AssessmentJudgement> findLatestByAnswerId(Long answerId) {
        AssessmentJudgementDO dataObject = assessmentJudgementMapper.selectLatestByAnswerId(answerId);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    /**
     * 查询指定作答的指定来源最新评审结果。
     *
     * @param answerId
     *            考核作答主键。
     * @param source
     *            评判来源。
     * @return 查询到的考核评审结果 实体；不存在时为空。
     */
    @Override
    public Optional<AssessmentJudgement> findLatestByAnswerIdAndSource(Long answerId, JudgementSource source) {
        AssessmentJudgementDO dataObject = assessmentJudgementMapper.selectLatestByAnswerIdAndSource(answerId, source);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    /**
     * 批量查询哪些作答已有指定来源的评审结果。
     *
     * @param answerIds
     *            作答主键集合。
     * @param source
     *            评判来源。
     * @return 已有指定来源评审结果的作答主键集合。
     */
    @Override
    public List<Long> findAnswerIdsBySource(List<Long> answerIds, JudgementSource source) {
        if (answerIds == null || answerIds.isEmpty()) {
            return List.of();
        }
        return assessmentJudgementMapper.selectAnswerIdsBySource(answerIds, source);
    }

    /**
     * 查询用户在指定题目上的最新评审结果。
     *
     * @param questionId
     *            考核题目主键。
     * @param userId
     *            用户主键，用于限定用户范围。
     * @return 查询到的考核评审结果 实体；不存在时为空。
     */
    @Override
    public Optional<AssessmentJudgement> findLatestByQuestionIdAndUserId(Long questionId, Long userId) {
        AssessmentJudgementDO dataObject = assessmentJudgementMapper
                .selectLatestByQuestionIdAndUserId(questionId, userId);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    /**
     * 查询指定题目的全部评审记录。
     *
     * @param questionId
     *            考核题目主键。
     * @return 满足条件的考核评审结果 实体集合。
     */
    @Override
    public List<AssessmentJudgement> findAllByQuestionId(Long questionId) {
        List<AssessmentJudgementDO> dataObjects = assessmentJudgementMapper.selectAllByQuestionId(questionId);
        return converter.toEntityList(dataObjects);
    }

    /**
     * 查询指定题目最新的客观题评审结果。
     *
     * @param questionId
     *            考核题目主键。
     * @return 满足条件的考核评审结果 实体集合。
     */
    @Override
    public List<AssessmentJudgement> findLatestObjectiveByQuestionId(Long questionId) {
        List<AssessmentJudgementDO> dataObjects = assessmentJudgementMapper
                .selectLatestObjectiveByQuestionId(questionId);
        return converter.toEntityList(dataObjects);
    }

    /**
     * 查询符合条件的考核评审结果 记录。
     *
     * @param assessmentTimeId
     *            考核场次主键。
     * @param questionType
     *            题目类型过滤条件。
     * @param keyword
     *            搜索关键字。
     * @return 满足条件的考核评审结果 结果集合。
     */
    @Override
    public List<AssessmentQuestionScoreboard> findQuestionScoreboard(Long assessmentTimeId,
            QuestionType questionType, String keyword) {
        return assessmentJudgementMapper.selectQuestionScoreboard(assessmentTimeId, questionType, keyword)
                .stream()
                .map(this::convertScoreboardToVO)
                .toList();
    }

    /**
     * 查询符合条件的考核评审结果 记录。
     *
     * @param questionId
     *            考核题目主键。
     * @param keyword
     *            搜索关键字。
     * @param status
     *            业务状态过滤条件。
     * @return 满足条件的考核评审结果 结果集合。
     */
    @Override
    public List<AssessmentQuestionSubmissionReadModel> findQuestionSubmissions(Long questionId, String keyword,
            String status) {
        return assessmentJudgementMapper.selectQuestionSubmissions(questionId, keyword, status)
                .stream()
                .map(this::convertSubmissionToVO)
                .toList();
    }

    /**
     * 查询符合条件的考核评审结果 记录。
     *
     * @param questionId
     *            考核题目主键。
     * @param userIds
     *            候选用户主键集合。
     * @return 满足条件的考核评审结果 结果集合。
     */
    @Override
    public List<AssessmentQuestionSubmissionHistoryReadModel> findQuestionSubmissionHistories(Long questionId,
            List<Long> userIds) {
        return assessmentJudgementMapper.selectQuestionSubmissionHistories(questionId, userIds)
                .stream()
                .map(this::convertSubmissionHistoryToVO)
                .toList();
    }

    /**
     * 查询符合条件的考核评审结果 记录。
     *
     * @param assessmentTimeId
     *            考核场次主键。
     * @param keyword
     *            搜索关键字。
     * @return 满足条件的考核评审结果 结果集合。
     */
    @Override
    public List<AssessmentCandidateScoreRowReadModel> findCandidateScoreRows(Long assessmentTimeId, String keyword) {
        return assessmentJudgementMapper.selectCandidateScoreRows(assessmentTimeId, keyword)
                .stream()
                .map(this::convertCandidateScoreRowToVO)
                .toList();
    }

    @Override
    public void deleteByAnswerIds(List<Long> answerIds) {
        if (answerIds == null || answerIds.isEmpty()) {
            return;
        }
        assessmentJudgementMapper.deleteByAnswerIds(answerIds);
    }

    @Override
    public void batchInsert(List<com.bluenet.web.domain.model.entity.AssessmentJudgement> judgements) {
        if (judgements == null || judgements.isEmpty()) {
            return;
        }
        List<AssessmentJudgementDO> dataObjects = converter.toDataObjectList(judgements);
        assessmentJudgementMapper.batchInsert(dataObjects);
        // 回写主键到实体（PostgreSQL foreach 批量插入可能只回写部分，驱动支持时生效）
        for (int i = 0; i < dataObjects.size(); i++) {
            judgements.get(i).setId(dataObjects.get(i).getId());
        }
    }

    @Override
    public void upsertAdminFinalized(AssessmentJudgement judgement) {
        AssessmentJudgementDO dataObject = converter.toDataObject(judgement);
        assessmentJudgementMapper.upsertAdminFinalized(dataObject);
    }

    /**
     * 处理考核评审结果 仓储职责中的业务数据访问逻辑。
     *
     * @param row
     *            Mapper 返回的投影数据行。
     * @return 转换后的目标模型对象。
     */
    private AssessmentQuestionSubmissionHistoryReadModel convertSubmissionHistoryToVO(
            AssessmentQuestionSubmissionQueryDO row) {
        AssessmentJudgement judgement = null;
        if (row.getJudgementId() != null) {
            judgement = AssessmentJudgement.reconstruct(
                    row.getJudgementId(),
                    row.getAnswerId(),
                    row.getQuestionId(),
                    row.getAssessmentTimeId(),
                    row.getCandidateUserId(),
                    row.getJudgementScore(),
                    row.getJudgementMaxScore(),
                    row.getJudgementStatus(),
                    row.getResultCode(),
                    row.getSource(),
                    row.getReviewerId(),
                    row.getReviewerType(),
                    row.getJudgedAt(),
                    null,
                    null);
        }
        return AssessmentQuestionSubmissionHistoryReadModel.builder()
                .judgement(judgement)
                .selectedBest(Boolean.TRUE.equals(row.getSelectedBest()))
                .build();
    }

    /**
     * 将题目评审看板投影转换为领域视图对象。
     *
     * @param row
     *            Mapper 返回的投影数据行。
     * @return 转换后的目标模型对象。
     */
    private AssessmentQuestionScoreboard convertScoreboardToVO(AssessmentQuestionScoreboardQueryDO row) {
        return AssessmentQuestionScoreboard.builder()
                .questionId(row.getQuestionId())
                .assessmentTimeId(row.getAssessmentTimeId())
                .questionNo(row.getQuestionNo())
                .questionType(row.getQuestionType())
                .title(row.getTitle())
                .maxScore(row.getMaxScore())
                .submittedCount(row.getSubmittedCount())
                .judgedCount(row.getJudgedCount())
                .pendingCount(row.getPendingCount())
                .averageScore(row.getAverageScore())
                .build();
    }

    /**
     * 将题目提交明细投影转换为领域视图对象。
     *
     * @param row
     *            Mapper 返回的投影数据行。
     * @return 转换后的目标模型对象。
     */
    private AssessmentQuestionSubmissionReadModel convertSubmissionToVO(AssessmentQuestionSubmissionQueryDO row) {
        return AssessmentQuestionSubmissionReadModel.builder()
                .answerId(row.getAnswerId())
                .questionId(row.getQuestionId())
                .assessmentTimeId(row.getAssessmentTimeId())
                .questionNo(row.getQuestionNo())
                .questionTitle(row.getQuestionTitle())
                .questionType(row.getQuestionType())
                .maxScore(row.getMaxScore())
                .candidateUserId(row.getCandidateUserId())
                .studentId(row.getStudentId())
                .username(row.getUsername())
                .nickname(row.getNickname())
                .fileId(row.getFileId())
                .content(row.getContent())
                .language(row.getLanguage())
                .submitTime(row.getSubmitTime())
                .judgementId(row.getJudgementId())
                .judgementScore(row.getJudgementScore())
                .judgementMaxScore(row.getJudgementMaxScore())
                .judgementStatus(row.getJudgementStatus())
                .resultCode(row.getResultCode())
                .source(row.getSource())
                .reviewerId(row.getReviewerId())
                .reviewerType(row.getReviewerType())
                .judgedAt(row.getJudgedAt())
                .selectedBest(row.getSelectedBest())
                .teamId(row.getTeamId())
                .teamName(row.getTeamName())
                .isLeader(row.getIsLeader())
                .build();
    }

    /**
     * 将候选人得分明细投影转换为领域视图对象。
     *
     * @param row
     *            Mapper 返回的投影数据行。
     * @return 转换后的目标模型对象。
     */
    private AssessmentCandidateScoreRowReadModel convertCandidateScoreRowToVO(AssessmentCandidateScoreQueryDO row) {
        return AssessmentCandidateScoreRowReadModel.builder()
                .candidateUserId(row.getCandidateUserId())
                .studentId(row.getStudentId())
                .username(row.getUsername())
                .nickname(row.getNickname())
                .questionId(row.getQuestionId())
                .questionNo(row.getQuestionNo())
                .questionTitle(row.getQuestionTitle())
                .questionType(row.getQuestionType())
                .maxScore(row.getMaxScore())
                .answerId(row.getAnswerId())
                .submitTime(row.getSubmitTime())
                .judgementId(row.getJudgementId())
                .judgementScore(row.getJudgementScore())
                .judgementMaxScore(row.getJudgementMaxScore())
                .judgementStatus(row.getJudgementStatus())
                .resultCode(row.getResultCode())
                .source(row.getSource())
                .reviewerId(row.getReviewerId())
                .reviewerType(row.getReviewerType())
                .judgedAt(row.getJudgedAt())
                .teamId(row.getTeamId())
                .teamName(row.getTeamName())
                .isLeader(row.getIsLeader())
                .build();
    }
}
