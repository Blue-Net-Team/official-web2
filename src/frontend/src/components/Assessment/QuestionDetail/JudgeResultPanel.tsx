'use client'

import { useState } from 'react'
import { RESULT_LABELS, RESULT_COLOR_CLASSES } from './constants'
import type { JudgeResultPanelProps } from './types'
import type { JudgeCaseResultDTO } from '@/apis/schema/assessment.dto'

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

function TextCell({
  label,
  value,
  emptyText = '空',
}: {
  label: string
  value?: string | null
  emptyText?: string
}) {
  const [expanded, setExpanded] = useState(false)
  const text = value || emptyText
  const shouldCollapse = text.length > 300 || text.split('\n').length > 8

  return (
    <div className="min-w-0">
      <div className="px-3 py-2 text-[11px] font-semibold text-white/45 bg-white/[0.04] border border-white/[0.08] rounded-t-md">
        {label}
      </div>
      <pre
        className={`m-0 whitespace-pre-wrap break-words bg-black/30 border-x border-white/[0.08] p-3 text-xs text-white/65 ${
          shouldCollapse && !expanded ? 'max-h-40 overflow-hidden' : 'min-h-16'
        } ${!shouldCollapse || expanded ? 'rounded-b-md border-b' : ''}`}
      >
        {text}
      </pre>
      {shouldCollapse && (
        <button
          onClick={() => setExpanded(!expanded)}
          className="w-full text-center text-[11px] text-[#6677ff] hover:text-[#9aa6ff] py-1 bg-white/[0.02] cursor-pointer border-x border-b border-white/[0.08] rounded-b-md"
        >
          {expanded ? '收起' : '展开'}
        </button>
      )}
    </div>
  )
}

function renderJudgeCase(caseResult: JudgeCaseResultDTO) {
  return (
    <div
      key={`${caseResult.caseNo}-${caseResult.testcaseType}`}
      className="rounded-lg bg-white/[0.04] border border-white/[0.08] p-4 flex flex-col gap-3"
    >
      <div className="flex items-center justify-between gap-3">
        <span className="text-sm font-semibold text-white">用例 {caseResult.caseNo}</span>
        {renderResultBadge(caseResult.status)}
      </div>
      <p className="text-[11px] text-white/30 m-0">
        换行会按原样保留显示；判题会忽略首尾空白，但中间换行和内容顺序需要一致。
      </p>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
        <TextCell label="输入 stdin" value={caseResult.input} emptyText="无输入" />
        {caseResult.expectedOutput !== null ? (
          <TextCell label="期望输出 target" value={caseResult.expectedOutput} emptyText="空输出" />
        ) : (
          <TextCell
            label="标准输出 stdout"
            value={caseResult.stdout || caseResult.actualOutput}
            emptyText="无输出"
          />
        )}
        {caseResult.expectedOutput !== null && (
          <TextCell
            label="实际输出 stdout"
            value={caseResult.actualOutput || caseResult.stdout}
            emptyText="无输出"
          />
        )}
      </div>
      {caseResult.stderr && (
        <TextCell label="标准错误 stderr" value={caseResult.stderr} emptyText="无错误输出" />
      )}
    </div>
  )
}

export default function JudgeResultPanel({
  judgeResult,
  visibleCaseResults,
}: JudgeResultPanelProps) {
  if (!judgeResult) return null

  return (
    <div className="rounded-xl bg-white/[0.04] border border-white/[0.08] p-5 flex flex-col gap-4">
      <div className="flex items-center justify-between gap-3">
        <span className="text-sm font-semibold text-white">
          判题状态：{judgeResult.statusMessage || judgeResult.status}
        </span>
        <span className="text-xs text-white/35">
          {judgeResult.testcaseType === 'FORMAL' ? '正式提交' : '运行调试'}
        </span>
        {judgeResult.judgement ? renderResultBadge(judgeResult.judgement.resultCode) : null}
      </div>
      {visibleCaseResults.length > 0 && (
        <div className="flex flex-col gap-3">{visibleCaseResults.map(renderJudgeCase)}</div>
      )}
    </div>
  )
}
