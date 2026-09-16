'use client'

import { useCallback, useEffect, useMemo, useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { Button, Card, Empty, Spin, Tag } from 'antd'
import { ArrowLeftOutlined } from '@ant-design/icons'
import { aiTraceService } from '@/apis/services/ai-trace.service'
import type { AiConversationDetailDTO } from '@/apis/schema/ai-trace.dto'
import ErrorState from '@/components/Admin/AiTrace/ErrorState'
import TraceDetailView from '@/components/Admin/AiTrace/TraceDetailView'
import { actionLabel } from '@/components/Admin/AiTrace/actionMeta'
import { formatDuration, formatMinute, formatTime } from '@/components/Admin/AiTrace/format'
import { intentLabel } from '@/components/Admin/AiTrace/intentLabel'

/**
 * 从地址栏读取 `?seq=`。
 *
 * 不用 useSearchParams：在 Next.js 15 中它会要求调用方包一层 Suspense 边界，
 * 而这里只需要在客户端挂载后读取一次。
 */
function readSeqFromLocation(): number | null {
  if (typeof window === 'undefined') return null
  const raw = new URLSearchParams(window.location.search).get('seq')
  if (!raw) return null
  const parsed = Number(raw)
  return Number.isFinite(parsed) ? parsed : null
}

export default function AiTraceConversationDetailPage() {
  const params = useParams()
  const router = useRouter()
  const conversationId = typeof params.id === 'string' ? params.id : ''

  const [detail, setDetail] = useState<AiConversationDetailDTO | null>(null)
  const [loading, setLoading] = useState(false)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [activeSeq, setActiveSeq] = useState<number | null>(null)

  const fetchDetail = useCallback(async () => {
    if (!conversationId) return
    setLoading(true)
    setLoadError(null)
    try {
      const res = await aiTraceService.getConversationDetail(conversationId)
      if (res.code === 200) {
        // 会话不存在时后端返回成功状态码与 null 数据，这里按"未找到"呈现
        setDetail(res.data ?? null)
      } else {
        setLoadError(res.msg || '加载失败')
      }
    } catch {
      setLoadError('网络异常，请稍后重试')
    } finally {
      setLoading(false)
    }
  }, [conversationId])

  useEffect(() => {
    fetchDetail()
  }, [fetchDetail])

  // 初始定位：优先用地址栏的 seq，否则取第一个提问
  const turns = useMemo(() => detail?.turns ?? [], [detail])
  useEffect(() => {
    if (turns.length === 0) return
    const fromUrl = readSeqFromLocation()
    const matched = fromUrl != null ? turns.find((turn) => turn.seq === fromUrl) : undefined
    setActiveSeq(matched?.seq ?? turns[0].seq)
  }, [turns])

  const activeTurn = useMemo(
    () => turns.find((turn) => turn.seq === activeSeq) ?? turns[0] ?? null,
    [turns, activeSeq]
  )

  const timeRange = useMemo(() => {
    if (!detail) return ''
    const start = formatMinute(detail.createdAt)
    const end = formatMinute(detail.lastActiveAt)
    return start === end ? start : `${start} – ${end}`
  }, [detail])

  if (loadError) {
    return <ErrorState description={loadError} onRetry={fetchDetail} />
  }

  return (
    <div className="flex flex-col gap-4">
      {/* 面包屑 */}
      <div className="flex flex-wrap items-center gap-2.5">
        <Button
          type="link"
          size="small"
          icon={<ArrowLeftOutlined />}
          onClick={() => router.push('/admin/ai-traces/conversations')}
          style={{ padding: 0 }}
        >
          返回列表
        </Button>
        <span className="text-white/25">|</span>
        <span className="font-mono text-sm text-white/90">{conversationId}</span>
        {detail && (
          <Tag bordered={false} style={{ margin: 0 }}>
            {detail.messageCount ?? 0} 次提问
          </Tag>
        )}
        {timeRange && <span className="text-xs text-white/45">{timeRange}</span>}
      </div>

      <Spin spinning={loading}>
        {!loading && !detail ? (
          <Card>
            <Empty description="会话不存在或已被清理" />
            <div className="mt-3 text-center">
              <Button onClick={() => router.push('/admin/ai-traces/conversations')}>
                返回列表
              </Button>
            </div>
          </Card>
        ) : (
          <div className="flex items-stretch gap-4">
            {/* 左栏：会话内的提问导航 */}
            <Card
              size="small"
              title={<span className="text-xs font-semibold text-white/45">本会话消息</span>}
              style={{ width: 264, flex: '0 0 264px' }}
              styles={{ body: { padding: 12, display: 'flex', flexDirection: 'column', gap: 8 } }}
            >
              {turns.length === 0 ? (
                <span className="text-xs text-white/25">该会话暂无提问记录</span>
              ) : (
                turns.map((turn) => {
                  const active = turn.seq === activeSeq
                  return (
                    <button
                      key={turn.seq}
                      type="button"
                      onClick={() => setActiveSeq(turn.seq)}
                      className={`w-full rounded-md border px-3 py-2.5 text-left transition-colors ${
                        active
                          ? 'border-[#fa8c1659] bg-[#fa8c162e]'
                          : 'border-white/[0.08] hover:bg-white/[0.04]'
                      }`}
                    >
                      <div className="flex items-center justify-between gap-2">
                        <span
                          className={`text-xs font-semibold ${active ? 'text-[#fa8c16]' : 'text-white/60'}`}
                        >
                          第 {turn.seq} 次提问
                        </span>
                        <span className="font-mono text-[11px] text-white/25">
                          {formatTime(turn.createdAt)}
                        </span>
                      </div>
                      <div className="mt-1 text-xs break-words text-white/90">
                        {turn.userInput || '（空）'}
                      </div>
                      <div className="mt-1 text-[11px] text-white/25">
                        {[
                          intentLabel(turn.intent),
                          turn.action ? actionLabel(turn.action) : null,
                          formatDuration(turn.durationMs),
                        ]
                          .filter(Boolean)
                          .join(' · ')}
                      </div>
                    </button>
                  )
                })
              )}
            </Card>

            {/* 右栏：完整原始事件流 */}
            <Card size="small" className="min-w-0 flex-1" styles={{ body: { padding: 20 } }}>
              {activeTurn ? (
                <TraceDetailView turn={activeTurn} />
              ) : (
                <Empty description="无可展示的轨迹" image={Empty.PRESENTED_IMAGE_SIMPLE} />
              )}
            </Card>
          </div>
        )}
      </Spin>
    </div>
  )
}
