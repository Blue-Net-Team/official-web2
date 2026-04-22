package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.vo.AssessmentCandidateScoreRowVO;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionScoreboardVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionSubmissionHistoryVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionSubmissionVO;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentJudgementDO;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentJudgementMapper;
import com.bluenet.web.infrastructure.repository.dataobject.query.AssessmentCandidateScoreQueryDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AssessmentQuestionScoreboardQueryDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AssessmentQuestionSubmissionQueryDO;
import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;
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

    /**
     * 保存新的考核评审结果 记录。
     *
     * @param judgement
     *            考核评审结果对象。
     */
    @Override
    public void save(AssessmentJudgement judgement) {
        log.info("save assessment judgement {}", judgement);
        RepositoryObjectConverter.insert(assessmentJudgementMapper, judgement, AssessmentJudgementDO.class);
    }

    /**
     * 按主键查询考核评审结果 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的考核评审结果 结果；不存在时为空。
     */
    @Override
    public Optional<AssessmentJudgementVO> findById(Long id) {
        AssessmentJudgement judgement = RepositoryObjectConverter
                .toDomain(assessmentJudgementMapper.selectById(id), AssessmentJudgement.class);
        if (judgement == null) {
            log.warn("assessment judgement not found id {}", id);
            return Optional.empty();
        }
        return Optional.of(convertToVO(judgement));
    }

    /**
     * 更新已有考核评审结果 记录。
     *
     * @param judgement
     *            考核评审结果对象。
     */
    @Override
    public void update(AssessmentJudgementVO judgement) {
        AssessmentJudgement entity = convertToEntity(judgement);
        int influence = RepositoryObjectConverter
                .updateById(assessmentJudgementMapper, entity, AssessmentJudgementDO.class);
        if (influence == 0) {
            log.warn("更新考核评判记录失败，judgementId {}", judgement.getId());
            throw new GlobalException("更新考核评判记录失败");
        }
    }

    /**
     * 查询指定作答的最新评审结果。
     *
     * @param answerId
     *            考核作答主键。
     * @return 查询到的考核评审结果 结果；不存在时为空。
     */
    @Override
    public Optional<AssessmentJudgementVO> findLatestByAnswerId(Long answerId) {
        AssessmentJudgement judgement = RepositoryObjectConverter
                .toDomain(assessmentJudgementMapper.selectLatestByAnswerId(answerId), AssessmentJudgement.class);
        if (judgement == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(judgement));
    }

    /**
     * 查询用户在指定题目上的最新评审结果。
     *
     * @param questionId
     *            考核题目主键。
     * @param userId
     *            用户主键，用于限定用户范围。
     * @return 查询到的考核评审结果 结果；不存在时为空。
     */
    @Override
    public Optional<AssessmentJudgementVO> findLatestByQuestionIdAndUserId(Long questionId, Long userId) {
        AssessmentJudgement judgement = RepositoryObjectConverter.toDomain(
                assessmentJudgementMapper.selectLatestByQuestionIdAndUserId(questionId, userId),
                AssessmentJudgement.class);
        if (judgement == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(judgement));
    }

    /**
     * 查询指定题目的全部评审记录。
     *
     * @param questionId
     *            考核题目主键。
     * @return 满足条件的考核评审结果 结果集合。
     */
    @Override
    public List<AssessmentJudgementVO> findAllByQuestionId(Long questionId) {
        // Mapper 层返回评判 DO，RepositoryImpl 在这里统一转换为领域对象。
        return RepositoryObjectConverter.toDomainList(
                assessmentJudgementMapper.selectAllByQuestionId(questionId),
                AssessmentJudgement.class)
                .stream()
                .map(this::convertToVO)
                .toList();
    }

    /**
     * 查询指定题目最新的客观题评审结果。
     *
     * @param questionId
     *            考核题目主键。
     * @return 满足条件的考核评审结果 结果集合。
     */
    @Override
    public List<AssessmentJudgementVO> findLatestObjectiveByQuestionId(Long questionId) {
        // Mapper 层返回评判 DO，RepositoryImpl 在这里统一转换为领域对象。
        return RepositoryObjectConverter.toDomainList(
                assessmentJudgementMapper.selectLatestObjectiveByQuestionId(questionId),
                AssessmentJudgement.class)
                .stream()
                .map(this::convertToVO)
                .toList();
    }

    /**
     * 在考核评审结果 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param judgement
     *            考核评审结果对象。
     * @return 转换后的目标模型对象。
     */
    private AssessmentJudgement convertToEntity(AssessmentJudgementVO judgement) {
        AssessmentJudgement entity = AssessmentJudgement.builder()
                .id(judgement.getId())
                .answerId(judgement.getAnswerId())
                .questionId(judgement.getQuestionId())
                .assessmentTimeId(judgement.getAssessmentTimeId())
                .userId(judgement.getUserId())
                .createdAt(judgement.getCreatedAt())
                .build();
        entity.applyJudgementResult(
                judgement.getScore(),
                judgement.getMaxScore(),
                judgement.getStatus(),
                judgement.getResultCode(),
                judgement.getSource(),
                judgement.getReviewerId(),
                judgement.getReviewerType(),
                judgement.getComment(),
                judgement.getJudgedAt(),
                judgement.getUpdatedAt());
        return entity;
    }

    /**
     * 在考核评审结果 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param judgement
     *            考核评审结果对象。
     * @return 转换后的目标模型对象。
     */
    private AssessmentJudgementVO convertToVO(AssessmentJudgement judgement) {
        return AssessmentJudgementVO.builder()
                .id(judgement.getId())
                .answerId(judgement.getAnswerId())
                .questionId(judgement.getQuestionId())
                .assessmentTimeId(judgement.getAssessmentTimeId())
                .userId(judgement.getUserId())
                .score(judgement.getScore())
                .maxScore(judgement.getMaxScore())
                .status(judgement.getStatus())
                .resultCode(judgement.getResultCode())
                .source(judgement.getSource())
                .reviewerId(judgement.getReviewerId())
                .reviewerType(judgement.getReviewerType())
                .comment(judgement.getComment())
                .judgedAt(judgement.getJudgedAt())
                .createdAt(judgement.getCreatedAt())
                .updatedAt(judgement.getUpdatedAt())
                .build();
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
    public List<AssessmentQuestionScoreboardVO> findQuestionScoreboard(Long assessmentTimeId,
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
    public List<AssessmentQuestionSubmissionVO> findQuestionSubmissions(Long questionId, String keyword,
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
    public List<AssessmentQuestionSubmissionHistoryVO> findQuestionSubmissionHistories(Long questionId,
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
    public List<AssessmentCandidateScoreRowVO> findCandidateScoreRows(Long assessmentTimeId, String keyword) {
        return assessmentJudgementMapper.selectCandidateScoreRows(assessmentTimeId, keyword)
                .stream()
                .map(this::convertCandidateScoreRowToVO)
                .toList();
    }

    /**
     * 处理考核评审结果 仓储职责中的业务数据访问逻辑。
     *
     * @param row
     *            Mapper 返回的投影数据行。
     * @return 转换后的目标模型对象。
     */
    private AssessmentQuestionSubmissionHistoryVO convertSubmissionHistoryToVO(
            AssessmentQuestionSubmissionQueryDO row) {
        AssessmentJudgementVO judgement = null;
        if (row.getJudgementId() != null) {
            judgement = AssessmentJudgementVO.builder()
                    .id(row.getJudgementId())
                    .answerId(row.getAnswerId())
                    .questionId(row.getQuestionId())
                    .assessmentTimeId(row.getAssessmentTimeId())
                    .userId(row.getCandidateUserId())
                    .score(row.getJudgementScore())
                    .maxScore(row.getJudgementMaxScore())
                    .status(row.getJudgementStatus())
                    .resultCode(row.getResultCode())
                    .source(row.getSource())
                    .reviewerId(row.getReviewerId())
                    .reviewerType(row.getReviewerType())
                    .comment(row.getJudgementComment())
                    .judgedAt(row.getJudgedAt())
                    .build();
        }
        return AssessmentQuestionSubmissionHistoryVO.builder()
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
    private AssessmentQuestionScoreboardVO convertScoreboardToVO(AssessmentQuestionScoreboardQueryDO row) {
        return AssessmentQuestionScoreboardVO.builder()
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
    private AssessmentQuestionSubmissionVO convertSubmissionToVO(AssessmentQuestionSubmissionQueryDO row) {
        return AssessmentQuestionSubmissionVO.builder()
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
                .judgementComment(row.getJudgementComment())
                .judgedAt(row.getJudgedAt())
                .selectedBest(row.getSelectedBest())
                .build();
    }

    /**
     * 将候选人得分明细投影转换为领域视图对象。
     *
     * @param row
     *            Mapper 返回的投影数据行。
     * @return 转换后的目标模型对象。
     */
    private AssessmentCandidateScoreRowVO convertCandidateScoreRowToVO(AssessmentCandidateScoreQueryDO row) {
        return AssessmentCandidateScoreRowVO.builder()
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
                .judgementComment(row.getJudgementComment())
                .judgedAt(row.getJudgedAt())
                .build();
    }
}
