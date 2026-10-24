'use client'

import MarkdownRenderer from '../MarkdownRenderer'
import type { AlgorithmExample } from '@/apis/schema/assessment.dto'

interface AlgorithmStatementProps {
  inputDescription?: string | null
  outputDescription?: string | null
  constraints?: string | null
  examples?: AlgorithmExample[] | null
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

/**
 * 算法题的题面陈述部分：输入说明 / 输出说明 / 数据范围三栏 + 题面样例。
 *
 * 从考生答题卡里原样抽出。只接受题面数据，不接触语言模板、默认运行用例、
 * 正式用例或判题配置 —— 因此管理端预览复用它时天然不会泄漏考生不可见内容。
 */
export default function AlgorithmStatement({
  inputDescription,
  outputDescription,
  constraints,
  examples,
}: AlgorithmStatementProps) {
  return (
    <>
      {(inputDescription || outputDescription || constraints) && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          {inputDescription && (
            <div className="rounded-lg border border-white/[0.15] p-4">
              <p className="text-xs text-white/35 mb-2">输入说明</p>
              <MarkdownRenderer
                content={inputDescription}
                compact
                className="text-sm text-white/65"
              />
            </div>
          )}
          {outputDescription && (
            <div className="rounded-lg border border-white/[0.15] p-4">
              <p className="text-xs text-white/35 mb-2">输出说明</p>
              <MarkdownRenderer
                content={outputDescription}
                compact
                className="text-sm text-white/65"
              />
            </div>
          )}
          {constraints && (
            <div className="rounded-lg border border-white/[0.15] p-4">
              <p className="text-xs text-white/35 mb-2">数据范围</p>
              <MarkdownRenderer content={constraints} compact className="text-sm text-white/65" />
            </div>
          )}
        </div>
      )}
      {examples?.map((example, index) => (
        <div key={index} className="rounded-lg border border-white/[0.15] p-4">
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
    </>
  )
}
