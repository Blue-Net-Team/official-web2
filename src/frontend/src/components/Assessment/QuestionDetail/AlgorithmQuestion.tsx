'use client'

import { Select } from 'antd'
import { ExperimentOutlined } from '@ant-design/icons'
import { MarkdownRenderer } from '@/components/Assessment'
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

function renderTextCell(label: string, value?: string | null, emptyText = '空') {
  return (
    <div className="min-w-0">
      <div className="px-3 py-2 text-[11px] font-semibold text-white/45 bg-white/[0.04] border border-white/[0.08] rounded-t-md">
        {label}
      </div>
      <pre className="m-0 min-h-16 whitespace-pre-wrap break-words rounded-b-md bg-black/30 border-x border-b border-white/[0.08] p-3 text-xs text-white/65">
        {value || emptyText}
      </pre>
    </div>
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
    <section className="bg-white/[0.06] border border-white/[0.08] rounded-xl p-7 h-fit flex flex-col gap-6">
      <div className="flex items-center justify-between gap-4">
        <div className="flex items-center gap-2.5">
          <ExperimentOutlined className="text-xl text-[#6677ff]" />
          <h2 className="text-base font-semibold text-white m-0">算法题</h2>
        </div>
        <div className="flex items-center gap-2 text-xs text-white/45">
          {algorithmContent?.timeLimit && <span>{algorithmContent.timeLimit} ms</span>}
          {algorithmContent?.memoryLimit && <span>{algorithmContent.memoryLimit} KB</span>}
        </div>
      </div>
      <hr className="w-full h-px bg-white/[0.04] border-none m-0" />
      <MarkdownRenderer content={algorithmContent?.content} emptyText="暂无题目描述" />
      {(algorithmContent?.inputDescription ||
        algorithmContent?.outputDescription ||
        algorithmContent?.constraints) && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          {algorithmContent.inputDescription && (
            <div className="rounded-lg bg-white/[0.04] p-4">
              <p className="text-xs text-white/35 mb-2">输入说明</p>
              <p className="text-sm text-white/65 whitespace-pre-wrap m-0">
                {algorithmContent.inputDescription}
              </p>
            </div>
          )}
          {algorithmContent.outputDescription && (
            <div className="rounded-lg bg-white/[0.04] p-4">
              <p className="text-xs text-white/35 mb-2">输出说明</p>
              <p className="text-sm text-white/65 whitespace-pre-wrap m-0">
                {algorithmContent.outputDescription}
              </p>
            </div>
          )}
          {algorithmContent.constraints && (
            <div className="rounded-lg bg-white/[0.04] p-4">
              <p className="text-xs text-white/35 mb-2">数据范围</p>
              <p className="text-sm text-white/65 whitespace-pre-wrap m-0">
                {algorithmContent.constraints}
              </p>
            </div>
          )}
        </div>
      )}
      {algorithmContent?.examples?.map((example, index) => (
        <div key={index} className="rounded-lg bg-white/[0.04] border border-white/[0.08] p-4">
          <p className="text-sm font-semibold text-white mb-3">样例 {index + 1}</p>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {renderTextCell('样例输入 stdin', example.input, '无输入')}
            {renderTextCell('样例输出 stdout', example.expectedOutput, '空输出')}
          </div>
          <p className="text-[11px] text-white/30 mt-3 mb-0">
            换行会按原样保留显示；判题会忽略首尾空白，但中间换行和内容顺序需要一致。
          </p>
          {example.explanation && (
            <p className="text-xs text-white/45 mt-3 mb-0 whitespace-pre-wrap">
              {example.explanation}
            </p>
          )}
        </div>
      ))}

      <div className="rounded-xl bg-white/[0.04] border border-white/[0.08] p-5 flex flex-col gap-4">
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
        <textarea
          value={algorithmCode}
          disabled={isExpired}
          onChange={(event) => onCodeChange(event.target.value)}
          className="w-full min-h-80 resize-y rounded-lg bg-black/30 border border-white/[0.08] p-4 text-sm text-white/80 font-mono outline-none focus:border-[#6677ff]/60 disabled:opacity-60"
          spellCheck={false}
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
    </section>
  )
}
