import { apiClient } from '../client'
import { ResponseMessage } from '../schema/type'
import type {
  AssessmentDecisionDTO,
  AssessmentDecisionRequestDTO,
  AssessmentDecisionWorkspaceDTO,
  AssessmentCandidateScoreboardDTO,
  AssessmentJudgementDTO,
  AssessmentQuestionScoreboardDTO,
  AssessmentQuestionSubmissionDTO,
  ManualReviewRequestDTO,
  QuestionType,
} from '@/apis/schema/assessment.dto'

/**
 * 管理端评判 API
 * 对应后端 /api/v1/admin/assessment-judgements/* 接口
 */
export const adminAssessmentJudgementService = {
  /** 查询指定题目的评判记录列表。 */
  async getByQuestion(questionId: number): Promise<ResponseMessage<AssessmentJudgementDTO[]>> {
    const response = await apiClient.get<ResponseMessage<AssessmentJudgementDTO[]>>(
      '/admin/assessment-judgements',
      { params: { questionId } }
    )
    return response.data
  },

  /** 提交文件上传题的人工评分和评论。 */
  async manualReview(
    request: ManualReviewRequestDTO
  ): Promise<ResponseMessage<AssessmentJudgementDTO>> {
    const response = await apiClient.post<ResponseMessage<AssessmentJudgementDTO>>(
      '/admin/assessment-judgements/manual-review',
      request
    )
    return response.data
  },

  /** 保存考生本轮考核的通过或淘汰决策。 */
  async decide(
    request: AssessmentDecisionRequestDTO
  ): Promise<ResponseMessage<AssessmentDecisionDTO>> {
    const response = await apiClient.post<ResponseMessage<AssessmentDecisionDTO>>(
      '/admin/assessment-judgements/decisions',
      request
    )
    return response.data
  },

  /** 查询题目维度的提交、待评和均分汇总。 */
  async getQuestionScoreboard(params: {
    assessmentTimeId: number
    questionType?: QuestionType
    keyword?: string
  }): Promise<ResponseMessage<AssessmentQuestionScoreboardDTO[]>> {
    const response = await apiClient.get<ResponseMessage<AssessmentQuestionScoreboardDTO[]>>(
      '/admin/assessment-judgements/scoreboard/questions',
      { params }
    )
    return response.data
  },

  /** 查询某道题下所有考生提交及当前展示评判结果。 */
  async getQuestionSubmissions(
    questionId: number,
    params: { keyword?: string; status?: 'JUDGED' | 'PENDING' } = {}
  ): Promise<ResponseMessage<AssessmentQuestionSubmissionDTO[]>> {
    const response = await apiClient.get<ResponseMessage<AssessmentQuestionSubmissionDTO[]>>(
      `/admin/assessment-judgements/scoreboard/questions/${questionId}/submissions`,
      { params }
    )
    return response.data
  },

  /** 查询人员维度的各题评分矩阵。 */
  async getCandidateScoreboard(params: {
    assessmentTimeId: number
    keyword?: string
  }): Promise<ResponseMessage<AssessmentCandidateScoreboardDTO[]>> {
    const response = await apiClient.get<ResponseMessage<AssessmentCandidateScoreboardDTO[]>>(
      '/admin/assessment-judgements/scoreboard/candidates',
      { params }
    )
    return response.data
  },

  /** 查询录用决策页面的统计、候选人和已有决策。 */
  async getDecisionWorkspace(params: {
    assessmentTimeId: number
    keyword?: string
    decisionStatus?: 'PENDING' | 'PASSED' | 'ELIMINATED'
  }): Promise<ResponseMessage<AssessmentDecisionWorkspaceDTO>> {
    const response = await apiClient.get<ResponseMessage<AssessmentDecisionWorkspaceDTO>>(
      '/admin/assessment-judgements/decisions',
      { params }
    )
    return response.data
  },

  /** 发布本轮考核决策结果邮件通知。 */
  async publishDecisions(assessmentTimeId: number): Promise<ResponseMessage<number>> {
    const response = await apiClient.post<ResponseMessage<number>>(
      '/admin/assessment-judgements/decisions/publish',
      null,
      { params: { assessmentTimeId } }
    )
    return response.data
  },
}
