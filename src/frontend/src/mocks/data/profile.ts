/**
 * 个人主页 Mock 数据 - 考核部分
 *
 * 仅保留考核相关的 Mock 数据（后端考核 API 尚未实现）
 *
 * @author BlueNet Team
 */
import type { Assessment } from '@/types/profile'

// ==================== 考核数据 ====================

/** Mock 考核列表 */
export const mockAssessments: Assessment[] = [
  {
    id: '1',
    title: '计算机视觉方向考核',
    round: '第二轮考核',
    status: 'in-progress',
    startDate: '2025-03-01',
    endDate: '2025-03-15',
    totalQuestions: 5,
    completedQuestions: 3,
    remainingTime: '5 天 12 小时',
  },
  {
    id: '2',
    title: '计算机视觉方向考核',
    round: '第一轮考核',
    status: 'ended',
    startDate: '2025-02-15',
    endDate: '2025-02-28',
    totalQuestions: 3,
    completedQuestions: 3,
    score: 88,
  },
  {
    id: '3',
    title: '计算机视觉方向考核',
    round: '第三轮考核',
    status: 'not-started',
    startDate: '2025-03-20',
    endDate: '2025-04-05',
    totalQuestions: 0,
    completedQuestions: 0,
    daysUntilStart: 10,
  },
]
