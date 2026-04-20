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
import com.bluenet.web.infrastructure.repository.mapper.AssessmentJudgementMapper;
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

    @Override
    public void save(AssessmentJudgement judgement) {
        log.info("save assessment judgement {}", judgement);
        assessmentJudgementMapper.insert(judgement);
    }

    @Override
    public Optional<AssessmentJudgementVO> findById(Long id) {
        AssessmentJudgement judgement = assessmentJudgementMapper.selectById(id);
        if (judgement == null) {
            log.warn("assessment judgement not found id {}", id);
            return Optional.empty();
        }
        return Optional.of(convertToVO(judgement));
    }

    @Override
    public void update(AssessmentJudgementVO judgement) {
        AssessmentJudgement entity = convertToEntity(judgement);
        int influence = assessmentJudgementMapper.updateById(entity);
        if (influence == 0) {
            log.warn("更新考核评判记录失败，judgementId {}", judgement.getId());
            throw new GlobalException("更新考核评判记录失败");
        }
    }

    @Override
    public Optional<AssessmentJudgementVO> findLatestByAnswerId(Long answerId) {
        AssessmentJudgement judgement = assessmentJudgementMapper.selectLatestByAnswerId(answerId);
        if (judgement == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(judgement));
    }

    @Override
    public Optional<AssessmentJudgementVO> findLatestByQuestionIdAndUserId(Long questionId, Long userId) {
        AssessmentJudgement judgement = assessmentJudgementMapper.selectLatestByQuestionIdAndUserId(questionId, userId);
        if (judgement == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(judgement));
    }

    @Override
    public List<AssessmentJudgementVO> findAllByQuestionId(Long questionId) {
        return assessmentJudgementMapper.selectAllByQuestionId(questionId)
                .stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public List<AssessmentJudgementVO> findLatestObjectiveByQuestionId(Long questionId) {
        return assessmentJudgementMapper.selectLatestObjectiveByQuestionId(questionId)
                .stream()
                .map(this::convertToVO)
                .toList();
    }

    private AssessmentJudgement convertToEntity(AssessmentJudgementVO judgement) {
        AssessmentJudgement entity = new AssessmentJudgement();
        entity.setId(judgement.getId());
        entity.setAnswerId(judgement.getAnswerId());
        entity.setQuestionId(judgement.getQuestionId());
        entity.setAssessmentTimeId(judgement.getAssessmentTimeId());
        entity.setUserId(judgement.getUserId());
        entity.setScore(judgement.getScore());
        entity.setMaxScore(judgement.getMaxScore());
        entity.setStatus(judgement.getStatus());
        entity.setResultCode(judgement.getResultCode());
        entity.setSource(judgement.getSource());
        entity.setReviewerId(judgement.getReviewerId());
        entity.setReviewerType(judgement.getReviewerType());
        entity.setComment(judgement.getComment());
        entity.setJudgedAt(judgement.getJudgedAt());
        entity.setCreatedAt(judgement.getCreatedAt());
        entity.setUpdatedAt(judgement.getUpdatedAt());
        return entity;
    }

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
     * 查询题目评分汇总，Mapper 直接返回 VO。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param questionType
     *            题型筛选
     * @param keyword
     *            题目关键词
     * @return 题目评分汇总 VO 列表
     */
    @Override
    public List<AssessmentQuestionScoreboardVO> findQuestionScoreboard(Long assessmentTimeId,
            QuestionType questionType, String keyword) {
        return assessmentJudgementMapper.selectQuestionScoreboard(assessmentTimeId, questionType, keyword);
    }

    /**
     * 查询题目提交列表，Mapper 直接返回 VO。
     *
     * @param questionId
     *            题目ID
     * @param keyword
     *            考生关键词
     * @param status
     *            评判状态筛选
     * @return 提交 VO 列表
     */
    @Override
    public List<AssessmentQuestionSubmissionVO> findQuestionSubmissions(Long questionId, String keyword,
            String status) {
        return assessmentJudgementMapper.selectQuestionSubmissions(questionId, keyword, status);
    }

    /**
     * 查询评判历史，将扁平 VO 转换为嵌套的历史 VO（含评判记录和最佳标记）。
     *
     * @param questionId
     *            题目ID
     * @param userIds
     *            考生用户ID列表
     * @return 评判历史 VO 列表
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
     * 查询考生评分矩阵行，Mapper 直接返回 VO。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param keyword
     *            考生关键词
     * @return 考生评分矩阵行 VO 列表
     */
    @Override
    public List<AssessmentCandidateScoreRowVO> findCandidateScoreRows(Long assessmentTimeId, String keyword) {
        return assessmentJudgementMapper.selectCandidateScoreRows(assessmentTimeId, keyword);
    }

    /**
     * 扁平提交 VO → 嵌套历史 VO，仅在 judgementId 非空时构建评判 VO。
     *
     * @param row
     *            Mapper 返回的扁平提交行
     * @return 评判历史 VO
     */
    private AssessmentQuestionSubmissionHistoryVO convertSubmissionHistoryToVO(AssessmentQuestionSubmissionVO row) {
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
}
