'use client'

/**
 * 考核列表组件 - 客户端组件
 *
 * 功能：
 * - 展示考核列表（进行中/已结束/未开始三种状态）
 * - 使用与 assessment 页面一致的卡片样式
 * - 支持点击进入考核详情（进行中/已结束）
 * - 空状态展示
 *
 * @author BlueNet Team
 */
import type { AssessmentTimeDTO } from '@/apis/schema/assessment.dto'
import { FileTextOutlined } from '@ant-design/icons'
import { AssessmentCard } from '@/components/Assessment'

interface AssessmentListProps {
  assessments: AssessmentTimeDTO[]
}

export default function AssessmentList({ assessments }: AssessmentListProps) {
  if (assessments.length === 0) {
    return (
      <div className="text-center py-[60px] px-5 bg-white/[0.03] backdrop-blur-[20px] border border-white/[0.05] rounded-2xl">
        <div className="w-20 h-20 mx-auto mb-5 rounded-full bg-[rgba(102,119,255,0.1)] flex items-center justify-center [&>svg]:w-10 [&>svg]:h-10 [&>svg]:text-[#6677ff]">
          <FileTextOutlined />
        </div>
        <h3 className="text-lg font-semibold text-white mb-2">暂无考核记录</h3>
        <p className="text-sm text-[rgba(140,140,141,1)]">您还没有参与任何考核</p>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-4">
      {assessments.map((assessment) => (
        <AssessmentCard key={assessment.id} assessment={assessment} />
      ))}
    </div>
  )
}
