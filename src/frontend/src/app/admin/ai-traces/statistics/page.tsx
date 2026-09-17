'use client'

import { useCallback, useEffect, useMemo, useState } from 'react'
import { Button, Card, Empty, Select, Spin } from 'antd'
import { Column } from '@ant-design/charts'
import { ReloadOutlined } from '@ant-design/icons'
import { aiTraceService } from '@/apis/services/ai-trace.service'
import type {
  AiCountItemDTO,
  AiTracePeriod,
  AiTraceStatisticsDTO,
} from '@/apis/schema/ai-trace.dto'
import ErrorState from '@/components/Admin/AiTrace/ErrorState'
import { ACTION_META, ACTION_ORDER } from '@/components/Admin/AiTrace/actionMeta'
import { formatPercent } from '@/components/Admin/AiTrace/format'
import { intentLabel } from '@/components/Admin/AiTrace/intentLabel'

const PERIOD_OPTIONS = [
  { value: '24h', label: '最近 24 小时' },
  { value: '7d', label: '最近 7 天' },
  { value: '30d', label: '最近 30 天' },
]

/** 意图条的颜色：主意图用主色，无关问题用红色，问候等低频项做弱化 */
function intentColor(intent: string): string {
  if (intent.startsWith('BLOCKED_') && intent === 'BLOCKED_IRRELEVANT') return '#ff4d4f'
  if (intent === 'GREETING' || intent === 'CLARIFY') return 'rgba(255,255,255,0.25)'
  if (intent === 'TEAM_INTRODUCTION' || intent === 'LAB_INTRODUCTION') return '#13c2c2'
  return '#fa8c16'
}

/** 概览指标卡 */
function StatCard({
  title,
  value,
  hint,
  accent,
}: {
  title: string
  value: string
  hint: string
  accent?: boolean
}) {
  return (
    <Card
      size="small"
      className="flex-1"
      styles={{ body: { padding: '18px 20px' } }}
      style={accent ? { borderColor: '#faad1459' } : undefined}
    >
      <div className="text-xs text-white/45">{title}</div>
      <div
        className="mt-1.5 text-2xl font-bold"
        style={{ color: accent ? '#faad14' : 'rgba(255,255,255,0.88)' }}
      >
        {value}
      </div>
      <div className="mt-1 text-[11px] text-white/45">{hint}</div>
    </Card>
  )
}

/** 横向条形列表（检索工具使用） */
function BarList({
  items,
  maxWidth,
  emptyText,
  warnLabel,
}: {
  items: AiCountItemDTO[]
  maxWidth: number
  emptyText: string
  warnLabel?: string
}) {
  const max = Math.max(...items.map((item) => item.count), 1)
  if (items.length === 0) {
    return <span className="text-xs text-white/25">{emptyText}</span>
  }
  return (
    <div className="flex flex-col gap-2">
      {items.map((item) => {
        const isWarn = warnLabel != null && item.label === warnLabel
        return (
          <div key={item.label} className="flex items-center gap-3">
            <span
              className="w-24 shrink-0 truncate font-mono text-xs"
              style={{ color: isWarn ? '#faad14' : 'rgba(255,255,255,0.6)' }}
              title={item.label}
            >
              {item.label}
            </span>
            <span
              className="h-2 shrink-0 overflow-hidden rounded"
              style={{ width: maxWidth, background: 'rgba(255,255,255,0.1)' }}
            >
              <span
                className="block h-full rounded"
                style={{
                  width: Math.max((item.count / max) * maxWidth, 2),
                  background: isWarn ? '#faad14' : '#fa8c16',
                }}
              />
            </span>
            <span className="font-mono text-xs text-white/45">{item.count}</span>
          </div>
        )
      })}
    </div>
  )
}

export default function AiTraceStatisticsPage() {
  const [period, setPeriod] = useState<AiTracePeriod>('7d')
  const [stats, setStats] = useState<AiTraceStatisticsDTO | null>(null)
  const [loading, setLoading] = useState(false)
  const [loadError, setLoadError] = useState<string | null>(null)

  const fetchStats = useCallback(async () => {
    setLoading(true)
    setLoadError(null)
    try {
      const res = await aiTraceService.getStatistics(period)
      if (res.code === 200 && res.data) {
        setStats(res.data)
      } else {
        setLoadError(res.msg || '加载失败')
      }
    } catch {
      setLoadError('网络异常，请稍后重试')
    } finally {
      setLoading(false)
    }
  }, [period])

  useEffect(() => {
    fetchStats()
  }, [fetchStats])

  const overview = stats?.overview
  const questionCount = overview?.questionCount ?? 0
  const refusedCount = overview?.refusedCount ?? 0

  const refusalRate = questionCount > 0 ? refusedCount / questionCount : null

  /** 动作分布：按固定顺序补齐缺失项，保证堆叠条与图例稳定 */
  const actionRows = useMemo(() => {
    const map = new Map((stats?.actions ?? []).map((item) => [item.label, item]))
    return ACTION_ORDER.map((key) => ({
      key,
      ...ACTION_META[key],
      count: map.get(key)?.count ?? 0,
    }))
  }, [stats])

  const actionTotal = actionRows.reduce((sum, row) => sum + row.count, 0)

  /** 趋势柱：高度按最大值归一 */
  const trend = stats?.trend ?? []
  const trendMax = Math.max(...trend.map((point) => point.count), 1)
  const TREND_HEIGHT = 140

  /** 拒答原因取前三 */
  const refusalTop = (stats?.refusals ?? []).slice(0, 3)

  const hasAnyData = questionCount > 0 || (overview?.conversationCount ?? 0) > 0

  if (loadError) {
    return <ErrorState description={loadError} onRetry={fetchStats} />
  }

  return (
    <div className="flex flex-col gap-4">
      {/* 标题行 */}
      <div className="flex items-end justify-between">
        <div>
          <h2 className="m-0 text-lg font-medium text-white/90">AI 对话分析</h2>
          <div className="mt-1 text-xs text-white/45">趋势按会话统计 · 意图与动作按提问统计</div>
        </div>
        <div className="flex items-center gap-2">
          <Select
            value={period}
            onChange={(value) => setPeriod(value)}
            options={PERIOD_OPTIONS}
            style={{ width: 150 }}
          />
          <Button icon={<ReloadOutlined />} onClick={fetchStats} loading={loading}>
            刷新
          </Button>
        </div>
      </div>

      <Spin spinning={loading}>
        {/* 概览指标 */}
        <div className="flex gap-4">
          <StatCard
            title="会话总数"
            value={String(overview?.conversationCount ?? 0)}
            hint="该区间内新建的会话数"
          />
          <StatCard
            title="消息数"
            value={String(questionCount)}
            hint="消息数 = 用户提问数（不含助手回复）"
          />
          <StatCard
            title="拒答率"
            value={refusalRate == null ? '—' : formatPercent(refusalRate)}
            hint={`共拒答 ${refusedCount} 次`}
          />
          <StatCard
            title="兜底检索率"
            value={overview?.fallbackRate == null ? '—' : formatPercent(overview.fallbackRate)}
            hint="越高说明标签覆盖越差"
            accent
          />
        </div>

        {/* 中部：意图分布 + 动作分布 */}
        <div className="mt-4 flex gap-4">
          <Card
            size="small"
            title={<span className="text-sm font-semibold text-white/90">意图分布</span>}
            className="min-w-0 flex-1"
            styles={{ body: { padding: 20 } }}
          >
            <div className="mb-3 text-[11px] text-white/25">
              共 {questionCount} 次提问 · 分类器输出结果
            </div>
            {(stats?.intents ?? []).length === 0 ? (
              <Empty description="该区间内没有提问" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            ) : (
              <div className="flex flex-col gap-2">
                {(stats?.intents ?? []).map((item) => {
                  const max = Math.max(...(stats?.intents ?? []).map((i) => i.count), 1)
                  return (
                    <div key={item.label} className="flex items-center gap-3">
                      <span
                        className="w-[88px] shrink-0 truncate text-xs text-white/60"
                        title={item.label}
                      >
                        {intentLabel(item.label)}
                      </span>
                      <span
                        className="h-2 shrink-0 overflow-hidden rounded"
                        style={{ width: 480, background: 'rgba(255,255,255,0.1)' }}
                      >
                        <span
                          className="block h-full rounded"
                          style={{
                            width: Math.max((item.count / max) * 480, 2),
                            background: intentColor(item.label),
                          }}
                        />
                      </span>
                      <span className="font-mono text-xs text-white/45">{item.count}</span>
                    </div>
                  )
                })}
              </div>
            )}
          </Card>

          <Card
            size="small"
            title={<span className="text-sm font-semibold text-white/90">处理动作分布</span>}
            style={{ width: 400, flex: '0 0 400px' }}
            styles={{ body: { padding: 20 } }}
          >
            {/* 堆叠横条 */}
            <div className="flex h-2.5 w-full overflow-hidden rounded-full bg-white/10">
              {actionTotal > 0 &&
                actionRows.map((row) => (
                  <span
                    key={row.key}
                    style={{
                      width: `${(row.count / actionTotal) * 100}%`,
                      background: row.color,
                    }}
                  />
                ))}
            </div>

            {/* 图例 */}
            <div className="mt-4 flex flex-col gap-2">
              {actionRows.map((row) => (
                <div key={row.key} className="flex items-center gap-2.5">
                  <span className="h-2 w-2 shrink-0 rounded-sm" style={{ background: row.color }} />
                  <span className="flex-1 text-xs text-white/60">
                    {row.label} {row.key}
                  </span>
                  <span className="font-mono text-xs text-white/45">{row.count}</span>
                  <span className="w-14 text-right font-mono text-xs text-white/90">
                    {actionTotal > 0 ? formatPercent(row.count / actionTotal) : '—'}
                  </span>
                </div>
              ))}
            </div>

            <div className="my-4 h-px bg-white/[0.08]" />

            <div className="text-[11px] font-semibold text-white/45">拒答原因 TOP 3</div>
            <div className="mt-2.5 flex flex-col gap-1.5">
              {refusalTop.length === 0 ? (
                <span className="text-xs text-white/25">该区间内没有拒答记录</span>
              ) : (
                refusalTop.map((item, index) => (
                  <div key={item.label} className="flex items-center gap-2.5">
                    <span
                      className="h-1.5 w-1.5 shrink-0 rounded-sm"
                      style={{ background: index === 0 ? '#ff4d4f' : 'rgba(255,255,255,0.25)' }}
                    />
                    <span className="flex-1 truncate font-mono text-[11px] text-white/45">
                      {item.label}
                    </span>
                    <span className="font-mono text-[11px] text-white/60">{item.count}</span>
                  </div>
                ))
              )}
            </div>
          </Card>
        </div>

        {/* 底部：趋势 + 工具使用 */}
        <div className="mt-4 flex gap-4">
          <Card
            size="small"
            title={<span className="text-sm font-semibold text-white/90">会话量趋势</span>}
            className="min-w-0 flex-1"
            styles={{ body: { padding: 20 } }}
          >
            <div className="mb-3 text-[11px] text-white/25">
              每日新增会话数
              {trend.length > 0
                ? ` · 峰值在 ${trend.reduce((a, b) => (b.count > a.count ? b : a)).time.slice(5, 10)}`
                : ''}
            </div>
            {trend.length === 0 ? (
              <Empty description="该区间内没有会话" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            ) : (
              <Column
                data={trend.map((point) => ({
                  time: point.time.slice(5, 10),
                  count: point.count,
                }))}
                xField="time"
                yField="count"
                height={TREND_HEIGHT + 24}
                style={{
                  fill: (datum: { count: number }) =>
                    datum.count === trendMax && datum.count > 0
                      ? '#fa8c16'
                      : 'rgba(250,140,22,0.35)',
                }}
                tooltip={{ title: (datum: { time: string }) => datum.time }}
              />
            )}
          </Card>

          <Card
            size="small"
            title={<span className="text-sm font-semibold text-white/90">检索工具使用</span>}
            style={{ width: 440, flex: '0 0 440px' }}
            styles={{ body: { padding: 20 } }}
          >
            <div className="mb-3 text-[11px] text-white/25">
              调用次数 · 兜底检索越高说明标签覆盖越差
            </div>
            <BarList
              items={stats?.tools ?? []}
              maxWidth={280}
              emptyText="该区间内没有工具调用"
              warnLabel="chunk_search"
            />
          </Card>
        </div>

        {!hasAnyData && (
          <div className="mt-4 text-center text-xs text-white/25">
            该区间内没有任何对话数据。所有指标显示为 0 表示区间内无数据，而非查询失败。
          </div>
        )}
      </Spin>
    </div>
  )
}
