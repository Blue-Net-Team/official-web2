'use client'

import { useMemo } from 'react'
import { Collapse, Empty, Tag } from 'antd'
import {
  BulbOutlined,
  FlagOutlined,
  NumberOutlined,
  ThunderboltOutlined,
  ToolOutlined,
} from '@ant-design/icons'
import ActionTag from './ActionTag'
import type { AiTraceEvent, AiTurnDetailDTO } from '@/apis/schema/ai-trace.dto'

/**
 * 解析 events 字段。
 *
 * 后端以 JSON 字符串返回事件数组；解析失败时返回空数组，
 * 由调用方呈现空状态，而不是让页面崩溃。
 */
export function parseEvents(raw: string | null | undefined): AiTraceEvent[] {
  if (!raw) return []
  try {
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? (parsed as AiTraceEvent[]) : []
  } catch {
    return []
  }
}

type Block =
  | { kind: 'intent'; event: AiTraceEvent }
  | { kind: 'pre_disclose'; event: AiTraceEvent }
  | { kind: 'reasoning'; text: string }
  | { kind: 'round'; round: number }
  | { kind: 'tool_call'; event: AiTraceEvent }
  | { kind: 'tool_result'; event: AiTraceEvent }
  | { kind: 'answer'; text: string }

/**
 * 把原始事件流整理成可渲染的块。
 *
 * 两个关键处理：
 * 1. 合并连续的 reasoning / content —— 流式输出会产生上千个碎片段，
 *    逐条渲染会拖垮页面。
 * 2. 在每次工具调用前插入「轮次 N」标题，位置放在该轮的前置思考之前，
 *    以还原 agent ⇄ tool 的循环结构。
 */
export function buildBlocks(events: AiTraceEvent[]): Block[] {
  const merged: Block[] = []

  for (const event of events) {
    switch (event.type) {
      case 'intent':
        merged.push({ kind: 'intent', event })
        break
      case 'pre_disclose':
        merged.push({ kind: 'pre_disclose', event })
        break
      case 'reasoning': {
        const last = merged[merged.length - 1]
        if (last?.kind === 'reasoning') {
          last.text += event.content ?? ''
        } else {
          merged.push({ kind: 'reasoning', text: event.content ?? '' })
        }
        break
      }
      case 'tool_call':
        merged.push({ kind: 'tool_call', event })
        break
      case 'tool_result':
        merged.push({ kind: 'tool_result', event })
        break
      case 'content': {
        const last = merged[merged.length - 1]
        if (last?.kind === 'answer') {
          last.text += event.content ?? ''
        } else {
          merged.push({ kind: 'answer', text: event.content ?? '' })
        }
        break
      }
      default:
        break
    }
  }

  const out: Block[] = []
  for (const block of merged) {
    if (block.kind === 'tool_call') {
      const round = block.event.round
      if (typeof round === 'number') {
        // 紧邻的思考属于本轮，标题插在它之前
        const prev = out[out.length - 1]
        const at = prev?.kind === 'reasoning' ? out.length - 1 : out.length
        out.splice(at, 0, { kind: 'round', round })
      }
    }
    out.push(block)
  }
  return out
}

function formatToolArgs(args: Record<string, unknown> | undefined): string {
  if (!args || Object.keys(args).length === 0) return ''
  return Object.entries(args)
    .map(
      ([key, value]) => `${key}=「${typeof value === 'string' ? value : JSON.stringify(value)}」`
    )
    .join('  ')
}

/** 单轮提问的完整执行轨迹 */
export default function TraceDetailView({ turn }: { turn: AiTurnDetailDTO }) {
  const blocks = useMemo(() => buildBlocks(parseEvents(turn.events)), [turn.events])
  const promptText = useMemo(() => {
    if (!turn.prompt) return null
    try {
      return JSON.stringify(JSON.parse(turn.prompt), null, 2)
    } catch {
      return turn.prompt
    }
  }, [turn.prompt])

  const usedFallback = useMemo(
    () =>
      parseEvents(turn.events).some(
        (e) => e.type === 'tool_call' && e.tool_name === 'chunk_search'
      ),
    [turn.events]
  )

  return (
    <div className="flex flex-col gap-5">
      {/* 用户问题 */}
      <section className="flex flex-col gap-2">
        <div className="text-xs font-semibold text-white/45">用户问题</div>
        <div className="rounded-md bg-white/5 px-3 py-2 text-sm text-white/90 whitespace-pre-wrap break-words">
          {turn.userInput || <span className="text-white/25">（空）</span>}
        </div>
      </section>

      {/* 执行轨迹 */}
      {blocks.length === 0 ? (
        <Empty description="该提问未采集到事件流" image={Empty.PRESENTED_IMAGE_SIMPLE} />
      ) : (
        <section className="flex flex-col gap-4">
          {blocks.map((block, index) => {
            switch (block.kind) {
              case 'intent':
                return (
                  <div key={index} className="flex flex-col gap-2">
                    <div className="text-xs font-semibold text-white/45">意图判定</div>
                    <div className="flex flex-wrap items-center gap-2">
                      <Tag bordered={false} color="orange" style={{ margin: 0 }}>
                        {block.event.intent ?? '未知'}
                      </Tag>
                      {typeof block.event.confidence === 'number' && (
                        <span className="text-xs text-white/45">
                          置信度 {block.event.confidence.toFixed(2)}
                        </span>
                      )}
                      <span className="text-xs text-white/25">→</span>
                      <ActionTag action={block.event.action} />
                    </div>
                  </div>
                )

              case 'pre_disclose':
                return (
                  <div key={index} className="flex flex-col gap-2">
                    <div className="flex items-center gap-2">
                      <ThunderboltOutlined style={{ color: '#9254de' }} />
                      <span className="text-sm font-semibold text-white/90">预披露</span>
                      <span className="text-xs text-white/25">代码自动执行</span>
                    </div>
                    <div className="flex flex-wrap items-center gap-2 pl-6">
                      <span className="text-xs text-white/25">自动标签</span>
                      {(block.event.tags ?? []).length === 0 ? (
                        <span className="text-xs text-white/25">无</span>
                      ) : (
                        (block.event.tags ?? []).map((tag) => (
                          <Tag key={tag} bordered={false} color="purple" style={{ margin: 0 }}>
                            {tag}
                          </Tag>
                        ))
                      )}
                    </div>
                    <div className="flex flex-wrap items-center gap-2 pl-6">
                      <span className="text-xs text-white/25">标签命中</span>
                      {(block.event.hits ?? []).length === 0 ? (
                        <span className="text-xs text-white/25">未命中（或未执行标签检索）</span>
                      ) : (
                        <span className="text-xs text-white/60">
                          {(block.event.hits ?? [])
                            .map((h) => `${h.tag_name} ${h.score.toFixed(2)}`)
                            .join(' · ')}
                        </span>
                      )}
                    </div>
                  </div>
                )

              case 'round':
                return (
                  <div key={index} className="flex items-center gap-2 pt-1">
                    <NumberOutlined style={{ color: '#fa8c16' }} />
                    <span className="text-sm font-semibold text-white/90">轮次 {block.round}</span>
                  </div>
                )

              case 'reasoning':
                return (
                  <div key={index} className="flex items-start gap-2 pl-6">
                    <BulbOutlined style={{ color: 'rgba(255,255,255,0.25)', marginTop: 3 }} />
                    <p className="m-0 text-xs italic text-white/45 whitespace-pre-wrap break-words">
                      {block.text}
                    </p>
                  </div>
                )

              case 'tool_call':
                return (
                  <div key={index} className="flex flex-col gap-1 pl-6">
                    <div className="flex flex-wrap items-center gap-2">
                      <ToolOutlined style={{ color: '#fa8c16' }} />
                      <span className="font-mono text-xs text-[#fa8c16]">
                        {block.event.tool_name}
                      </span>
                      {formatToolArgs(block.event.tool_args) && (
                        <span className="font-mono text-[11px] text-white/45 break-all">
                          {formatToolArgs(block.event.tool_args)}
                        </span>
                      )}
                    </div>
                  </div>
                )

              case 'tool_result':
                return (
                  <div key={index} className="flex flex-col gap-1 pl-6">
                    {block.event.blocked && (
                      <div className="text-[11px] text-[#faad14]">
                        已达轮次上限，本轮由代码拦截，未执行真实检索
                      </div>
                    )}
                    <pre className="m-0 max-h-72 overflow-auto rounded-md bg-white/5 px-3 py-2 font-mono text-[11px] leading-relaxed text-white/60 whitespace-pre-wrap break-words">
                      {block.event.content || '（空结果）'}
                    </pre>
                  </div>
                )

              case 'answer':
                return (
                  <div key={index} className="flex flex-col gap-2 pt-2">
                    <div className="flex items-center gap-2">
                      <FlagOutlined style={{ color: '#10b981' }} />
                      <span className="text-sm font-semibold text-[#10b981]">最终答案</span>
                      <span className="text-xs text-white/25">
                        {turn.durationMs != null ? `${(turn.durationMs / 1000).toFixed(1)}s` : ''}
                        {usedFallback ? ' · 走过兜底检索' : ' · 未走兜底'}
                      </span>
                    </div>
                    <p className="m-0 pl-6 text-sm leading-relaxed text-white/90 whitespace-pre-wrap break-words">
                      {block.text}
                    </p>
                  </div>
                )

              default:
                return null
            }
          })}
        </section>
      )}

      {/* 完整 prompt 快照 */}
      <Collapse
        ghost
        items={[
          {
            key: 'prompt',
            label: (
              <span className="text-xs text-white/45">
                完整 prompt 快照
                {promptText ? '（模型实际收到的上下文）' : '（本次未执行状态图，无快照）'}
              </span>
            ),
            children: promptText ? (
              <pre className="m-0 max-h-96 overflow-auto rounded-md bg-white/5 px-3 py-2 font-mono text-[11px] leading-relaxed text-white/60">
                {promptText}
              </pre>
            ) : (
              <span className="text-xs text-white/25">
                被拒答或直接回复的提问不经过状态图，因此没有 prompt 快照。
              </span>
            ),
          },
        ]}
      />
    </div>
  )
}
