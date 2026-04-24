'use client'

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
        {renderTextCell('输入 stdin', caseResult.input, '无输入')}
        {caseResult.expectedOutput !== null
          ? renderTextCell('期望输出 target', caseResult.expectedOutput, '空输出')
          : renderTextCell(
              '标准输出 stdout',
              caseResult.stdout || caseResult.actualOutput,
              '无输出'
            )}
        {caseResult.expectedOutput !== null &&
          renderTextCell('实际输出 stdout', caseResult.actualOutput || caseResult.stdout, '无输出')}
      </div>
      {caseResult.stderr && renderTextCell('标准错误 stderr', caseResult.stderr, '无错误输出')}
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
