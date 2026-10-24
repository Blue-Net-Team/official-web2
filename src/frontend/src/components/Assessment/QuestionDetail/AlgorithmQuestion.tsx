'use client'

import type { CSSProperties } from 'react'
import { Select } from 'antd'
import { ExperimentOutlined } from '@ant-design/icons'
import SmokedGlassCard from '@/components/SmokedGlassCard'
import CodeEditor from '@/components/CodeEditor'
import { AlgorithmStatement } from '@/components/Assessment/QuestionStatement'
import { RESULT_LABELS, RESULT_COLOR_CLASSES } from './constants'
import type { AlgorithmQuestionProps } from './types'
import type { AlgorithmContent } from '@/apis/schema/assessment.dto'

function renderResultBadge(resultCode?: string | null) {
  if (!resultCode) return null
  return (
    <span
      className={`inline-flex items-center px-2.5 py-1 rounded-md border text-xs font-semibold ${
        RESULT_COLOR_CLASSES[resultCode] ?? 'text-white/65 bg-white/[0.08] border-white/[0.12]'
      }`}
    >
      {resultCode} · {RESULT_LABELS[resultCode] ?? resultCode}
    </span>
  )
}

export default function AlgorithmQuestion({
  question,
  answer,
  isExpired,
  algorithmLanguage,
  algorithmCode,
  algorithmRunMode,
  customInput,
  algorithmLanguageOptions,
  onLanguageChange,
  onCodeChange,
  onRunModeChange,
  onCustomInputChange,
}: AlgorithmQuestionProps) {
  const algorithmContent = question.content as AlgorithmContent | null

  return (
    <SmokedGlassCard
      radius={12}
      className="h-fit flex flex-col gap-6"
      style={{ '--sgc-padding': '28px' } as CSSProperties}
    >
      <div className="flex items-center justify-between gap-4">
        <div className="flex items-center gap-2.5">
          <ExperimentOutlined className="text-xl text-[#6677ff]" />
          <h2 className="text-base font-semibold text-white m-0">代码作答</h2>
        </div>
        <div className="text-xs text-white/45">正式资源限制由判题配置决定</div>
      </div>
      <hr className="w-full h-px bg-white/[0.04] border-none m-0" />
      <AlgorithmStatement
        inputDescription={algorithmContent?.inputDescription}
        outputDescription={algorithmContent?.outputDescription}
        constraints={algorithmContent?.constraints}
        examples={algorithmContent?.examples}
      />

      <div className="rounded-xl border border-white/[0.15] p-5 flex flex-col gap-4">
        <div className="flex flex-col sm:flex-row gap-3 sm:items-center sm:justify-between">
          <div className="flex items-center gap-3">
            <span className="text-sm text-white/65">语言</span>
            <Select
              className="min-w-36"
              value={algorithmLanguage ?? undefined}
              options={algorithmLanguageOptions}
              disabled={isExpired || algorithmLanguageOptions.length === 0}
              onChange={(value) => {
                onLanguageChange(value, algorithmContent?.starterCode?.[value])
              }}
            />
          </div>
          {answer?.judgement && renderResultBadge(answer.judgement.resultCode)}
        </div>
        <CodeEditor
          value={algorithmCode}
          onChange={onCodeChange}
          language={algorithmLanguage}
          readOnly={isExpired}
          height={320}
          placeholder="在这里编写标准输入输出代码"
        />
        <div className="flex flex-col gap-3">
          <div className="flex flex-wrap gap-2">
            <button
              className={`px-3 py-2 rounded-lg text-xs border ${
                algorithmRunMode === 'DEFAULT_RUN'
                  ? 'bg-[#6677ff]/[0.16] border-[#6677ff]/[0.35] text-[#9aa6ff]'
                  : 'bg-white/[0.04] border-white/[0.08] text-white/45'
              }`}
              onClick={() => onRunModeChange('DEFAULT_RUN')}
              disabled={isExpired}
            >
              默认用例
            </button>
            <button
              className={`px-3 py-2 rounded-lg text-xs border ${
                algorithmRunMode === 'CUSTOM_RUN'
                  ? 'bg-[#6677ff]/[0.16] border-[#6677ff]/[0.35] text-[#9aa6ff]'
                  : 'bg-white/[0.04] border-white/[0.08] text-white/45'
              }`}
              onClick={() => onRunModeChange('CUSTOM_RUN')}
              disabled={isExpired}
            >
              自定义输入
            </button>
          </div>
          {algorithmRunMode === 'CUSTOM_RUN' && (
            <textarea
              value={customInput}
              disabled={isExpired}
              onChange={(event) => onCustomInputChange(event.target.value)}
              className="w-full min-h-28 resize-y rounded-lg bg-black/30 border border-white/[0.08] p-3 text-xs text-white/70 font-mono outline-none focus:border-[#6677ff]/60 disabled:opacity-60"
              spellCheck={false}
              placeholder="输入自定义 stdin"
            />
          )}
        </div>
      </div>
    </SmokedGlassCard>
  )
}
