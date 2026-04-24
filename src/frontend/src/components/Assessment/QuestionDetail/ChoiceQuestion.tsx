'use client'

import { CheckSquareOutlined, CheckCircleOutlined, RedoOutlined } from '@ant-design/icons'
import { OPTION_LABELS } from './constants'
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

  return (
    <section className="bg-white/[0.06] border border-white/[0.08] rounded-xl p-7 h-fit">
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
      <div className="flex flex-col gap-3 mt-4">
        {options.map((option, index) => {
          const label = OPTION_LABELS[index] || `${index + 1}`
          const isSelected = isSingleChoice
            ? selectedOption === option
            : selectedOptions.includes(option)
          const isLocked = isAnswered && !isResubmitting

          return (
            <div
              key={index}
              onClick={() => {
                if (isLocked || isExpired) return
                if (isSingleChoice) {
                  onSelectOption(option)
                } else {
                  onToggleOption(option)
                }
              }}
              className={`flex items-center gap-4 p-4 rounded-xl transition-all duration-200 border ${
                isSelected
                  ? 'bg-[#6677ff]/[0.08] border-[#6677ff]/[0.3]'
                  : 'bg-white/[0.04] border-white/[0.08]'
              } ${
                !isLocked && !isExpired ? 'cursor-pointer hover:bg-white/[0.06]' : ''
              } ${isLocked ? 'opacity-90' : ''}`}
            >
              <span
                className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-semibold flex-shrink-0 ${
                  isSelected ? 'bg-[#6677ff] text-white' : 'bg-white/[0.08] text-white/45'
                }`}
              >
                {label}
              </span>
              <span
                className={`text-sm ${isSelected ? 'text-white font-medium' : 'text-white/65'}`}
              >
                {option}
              </span>
            </div>
          )
        })}
      </div>
    </section>
  )
}
