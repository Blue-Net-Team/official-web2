'use client'

import type { CSSProperties } from 'react'
import { CheckSquareOutlined, CheckCircleOutlined, RedoOutlined } from '@ant-design/icons'
import SmokedGlassCard from '@/components/SmokedGlassCard'
import { ChoiceStatement } from '@/components/Assessment/QuestionStatement'
import type { ChoiceQuestionProps } from './types'

export default function ChoiceQuestion({
  question,
  isAnswered,
  isResubmitting,
  isExpired,
  selectedOption,
  selectedOptions,
  onSelectOption,
  onToggleOption,
  onResubmit,
}: ChoiceQuestionProps) {
  const isSingleChoice = question.questionType === 'SINGLE_CHOICE'
  const options = (question.content as { options?: string[] } | null)?.options ?? []
  const isLocked = isAnswered && !isResubmitting

  return (
    <SmokedGlassCard
      radius={12}
      className="h-fit"
      style={{ '--sgc-padding': '28px' } as CSSProperties}
    >
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2.5">
          <CheckSquareOutlined className="text-xl text-[#6677ff]" />
          <h2 className="text-base font-semibold text-white m-0">
            {isSingleChoice ? '选择答案（单选）' : '选择答案（多选）'}
          </h2>
        </div>
        <div className="flex items-center gap-3">
          {isAnswered && !isResubmitting && (
            <span className="inline-flex items-center gap-1 text-xs text-[#07c160]">
              <CheckCircleOutlined /> 已提交
            </span>
          )}
          {isAnswered && !isResubmitting && !isExpired && (
            <button
              className="inline-flex items-center gap-1.5 px-4 py-2 rounded-lg bg-[#fa8c16]/[0.1] border border-[#fa8c16]/[0.19] text-[#fa8c16] text-xs font-medium cursor-pointer transition-all duration-200 hover:bg-[#fa8c16]/[0.19]"
              onClick={onResubmit}
            >
              <RedoOutlined className="text-sm" />
              重新提交
            </button>
          )}
        </div>
      </div>
      <hr className="w-full h-px bg-white/[0.04] border-none m-0" />
      <div className="mt-4">
        <ChoiceStatement
          options={options}
          selectedValues={
            isSingleChoice ? (selectedOption ? [selectedOption] : []) : selectedOptions
          }
          disabled={isLocked || isExpired}
          onSelect={(option) => {
            if (isSingleChoice) {
              onSelectOption(option)
            } else {
              onToggleOption(option)
            }
          }}
        />
      </div>
    </SmokedGlassCard>
  )
}
