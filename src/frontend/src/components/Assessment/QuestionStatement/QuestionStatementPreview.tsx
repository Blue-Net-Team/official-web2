'use client'

import type {
  AlgorithmContent,
  BaseQuestionContent,
  MultipleChoiceContent,
  QuestionType,
  SingleChoiceContent,
} from '@/apis/schema/assessment.dto'
import StemCard from './StemCard'
import AlgorithmStatement from './AlgorithmStatement'
import ChoiceStatement from './ChoiceStatement'

/**
 * 预览所需的题面数据。
 *
 * 只取考生端 DTO 中与题面相关的部分（题型、标题、内容），故意不包含 id、
 * 分值、判题配置等作答态字段 —— 预览组件在类型层面就拿不到不可见内容。
 */
export interface PreviewStatement {
  questionType: QuestionType
  title: string
  content: BaseQuestionContent | null
}

/**
 * 考生端题面的只读预览。
 *
 * 复用考生答题页的同一批展示组件（`StemCard` / `AlgorithmStatement` /
 * `ChoiceStatement`），因此预览与考生端不会漂移。管理端与考生端看到的
 * 是同一份渲染实现，而不是两套长得像的 JSX。
 */
export default function QuestionStatementPreview({ statement }: { statement: PreviewStatement }) {
  const content = statement.content

  return (
    <div className="flex flex-col gap-6">
      <StemCard
        title={statement.title || '未命名题目'}
        content={content?.content}
        emptyText="暂无题目描述"
      />

      {statement.questionType === 'ALGORITHM' && content && (
        <div className="rounded-xl border border-white/[0.15] p-5 flex flex-col gap-6">
          <AlgorithmStatement
            inputDescription={(content as AlgorithmContent).inputDescription}
            outputDescription={(content as AlgorithmContent).outputDescription}
            constraints={(content as AlgorithmContent).constraints}
            examples={(content as AlgorithmContent).examples}
          />
        </div>
      )}

      {(statement.questionType === 'SINGLE_CHOICE' ||
        statement.questionType === 'MULTIPLE_CHOICE') &&
        content && (
          <div className="rounded-xl border border-white/[0.15] p-5">
            <div className="flex items-center justify-between mb-4">
              <span className="text-base font-semibold text-white">
                {statement.questionType === 'SINGLE_CHOICE'
                  ? '选择答案（单选）'
                  : '选择答案（多选）'}
              </span>
              <span className="text-xs text-white/35">预览：未选中态</span>
            </div>
            <ChoiceStatement
              options={(content as SingleChoiceContent | MultipleChoiceContent).options ?? []}
            />
          </div>
        )}
    </div>
  )
}
