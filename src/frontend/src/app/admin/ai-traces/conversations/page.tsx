'use client'

import { useCallback, useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { Button, Empty, Input, Pagination, Select, Spin, Table, Tooltip } from 'antd'
import { ReloadOutlined, SearchOutlined, SubnodeOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { aiTraceService } from '@/apis/services/ai-trace.service'
import type { AiConversationSummaryDTO, AiTurnSummaryDTO } from '@/apis/schema/ai-trace.dto'
import ActionTag from '@/components/Admin/AiTrace/ActionTag'
import ErrorState from '@/components/Admin/AiTrace/ErrorState'
import { INTENT_OPTIONS, intentLabel } from '@/components/Admin/AiTrace/intentLabel'
import {
  formatDateTime,
  formatDuration,
  formatTime,
  toLocalIso,
} from '@/components/Admin/AiTrace/format'

/** 时间范围选项，值为向前追溯的小时数；null 表示不限 */
const RANGE_OPTIONS = [
  { value: 24, label: '最近 24 小时' },
  { value: 24 * 7, label: '最近 7 天' },
  { value: 24 * 30, label: '最近 30 天' },
  { value: 0, label: '全部时间' },
]

const ACTION_OPTIONS = [
  { value: 'RETRIEVE', label: '检索' },
  { value: 'REFUSE', label: '拒答' },
  { value: 'DIRECT', label: '直接回复' },
]

/** 各列宽度，父表与内嵌子表共用，保证纵向对齐 */
const W = {
  expand: 40,
  time: 76,
  intent: 136,
  action: 88,
  rounds: 56,
  messages: 68,
  duration: 76,
}

/** 会话列表中的提问子行。列数与父表一一对应（含首列缩进占位与空的「消息数」列），以保持纵向对齐。 */
const turnColumns: ColumnsType<AiTurnSummaryDTO> = [
  {
    key: 'indent',
    width: W.expand,
    render: () => <SubnodeOutlined style={{ color: 'rgba(255,255,255,0.25)' }} />,
  },
  {
    title: '时间',
    dataIndex: 'createdAt',
    width: W.time,
    render: (value: string) => (
      <span className="font-mono text-[11px] text-white/25">{formatDateTime(value)}</span>
    ),
  },
  {
    title: '用户问题',
    dataIndex: 'userInput',
    render: (value: string) => <span className="text-white/90">{value || '（空）'}</span>,
  },
  {
    title: '意图',
    dataIndex: 'intent',
    width: W.intent,
    render: (value: string | null) => (
      <span className="text-xs text-white/60">{intentLabel(value)}</span>
    ),
  },
  {
    title: '动作',
    dataIndex: 'action',
    width: W.action,
    render: (value: string | null) => <ActionTag action={value} />,
  },
  {
    title: '轮次',
    dataIndex: 'toolRounds',
    width: W.rounds,
    render: (value: number | null) => (
      <span className="text-xs text-white/60">{value ? `${value} 轮` : '—'}</span>
    ),
  },
  { title: '消息数', key: 'messages', width: W.messages, render: () => null },
  {
    title: '耗时',
    dataIndex: 'durationMs',
    width: W.duration,
    render: (value: number | null) => (
      <span className="text-xs text-white/45">{formatDuration(value)}</span>
    ),
  },
]

export default function AiTraceConversationsPage() {
  const router = useRouter()

  const [conversations, setConversations] = useState<AiConversationSummaryDTO[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(20)
  const [loading, setLoading] = useState(false)
  const [loadError, setLoadError] = useState<string | null>(null)

  // 待提交的筛选条件（点「查询」才生效）
  const [rangeHours, setRangeHours] = useState<number>(24 * 7)
  const [intent, setIntent] = useState<string | undefined>(undefined)
  const [action, setAction] = useState<string | undefined>(undefined)
  const [keywordInput, setKeywordInput] = useState('')
  // 已提交的筛选条件
  const [applied, setApplied] = useState<{ keyword?: string }>({})

  const fetchConversations = useCallback(async () => {
    setLoading(true)
    setLoadError(null)
    try {
      const params: Parameters<typeof aiTraceService.listConversations>[0] = {
        page: page - 1,
        size: pageSize,
      }
      if (intent) params.intent = intent
      if (action) params.action = action
      if (applied.keyword) params.keyword = applied.keyword
      if (rangeHours > 0) {
        const from = new Date(Date.now() - rangeHours * 3600 * 1000)
        params.startTime = toLocalIso(from)
      }

      const res = await aiTraceService.listConversations(params)
      if (res.code === 200 && res.data) {
        setConversations(res.data.content ?? [])
        setTotal(res.data.totalElements ?? 0)
      } else {
        setLoadError(res.msg || '加载失败')
      }
    } catch {
      setLoadError('网络异常，请稍后重试')
    } finally {
      setLoading(false)
    }
  }, [page, pageSize, intent, action, applied.keyword, rangeHours])

  useEffect(() => {
    fetchConversations()
  }, [fetchConversations])

  const handleQuery = () => {
    setPage(1)
    setApplied({ keyword: keywordInput.trim() || undefined })
  }

  const handleReset = () => {
    setRangeHours(24 * 7)
    setIntent(undefined)
    setAction(undefined)
    setKeywordInput('')
    setApplied({})
    setPage(1)
  }

  const columns: ColumnsType<AiConversationSummaryDTO> = [
    {
      title: '时间',
      dataIndex: 'lastActiveAt',
      width: W.time,
      render: (value: string) => <span className="text-white/60">{formatTime(value)}</span>,
    },
    {
      title: '会话 / 用户问题',
      key: 'summary',
      render: (_, record) => (
        <div className="flex items-center gap-2 min-w-0">
          <Tooltip title={record.id}>
            <span className="font-mono text-xs text-white/45 shrink-0">
              {record.id.slice(0, 8)}…
            </span>
          </Tooltip>
          <span className="text-white/90 truncate">{record.turns?.[0]?.userInput || '（空）'}</span>
        </div>
      ),
    },
    { title: '意图', key: 'intent', width: W.intent, render: () => null },
    { title: '动作', key: 'action', width: W.action, render: () => null },
    { title: '轮次', key: 'rounds', width: W.rounds, render: () => null },
    {
      title: '消息数',
      dataIndex: 'messageCount',
      width: W.messages,
      render: (value: number) => <span className="text-xs text-white/60">{value ?? 0}</span>,
    },
    {
      title: '耗时',
      key: 'duration',
      width: W.duration,
      render: (_, record) => (
        <span className="text-xs text-white/45">
          {formatDuration(
            (record.turns ?? []).reduce<number>((sum, turn) => sum + (turn.durationMs ?? 0), 0)
          )}
        </span>
      ),
    },
  ]

  const renderExpanded = (record: AiConversationSummaryDTO) => (
    <div className="flex flex-col gap-2">
      <Table<AiTurnSummaryDTO>
        columns={turnColumns}
        dataSource={record.turns ?? []}
        rowKey={(turn) => `${record.id}-${turn.seq}`}
        size="small"
        pagination={false}
        showHeader={false}
        locale={{ emptyText: '该会话暂无提问记录' }}
        scroll={{ x: 'max-content' }}
        rowClassName="cursor-pointer"
        onRow={(turn) => ({
          onClick: () => router.push(`/admin/ai-traces/conversations/${record.id}?seq=${turn.seq}`),
        })}
      />
      {(record.hiddenTurnCount ?? 0) > 0 && (
        <div className="pl-12 text-xs text-white/45">
          还有 {record.hiddenTurnCount} 个提问未内联展示，点击查看完整会话
        </div>
      )}
    </div>
  )

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-end justify-between">
        <div>
          <h2 className="m-0 text-lg font-medium text-white/90">AI 对话记录</h2>
          <div className="mt-1 text-xs text-white/45">
            共 {total} 个会话 · 最近 7 天 · 全部原始事件已存档
          </div>
        </div>
        <Button icon={<ReloadOutlined />} onClick={fetchConversations} loading={loading}>
          刷新
        </Button>
      </div>

      {/* 筛选栏 */}
      <div className="flex flex-wrap items-center gap-3 rounded-xl bg-white/[0.03] px-5 py-3">
        <Select
          value={rangeHours}
          onChange={setRangeHours}
          options={RANGE_OPTIONS}
          style={{ width: 140 }}
        />
        <Select
          value={intent}
          onChange={setIntent}
          options={INTENT_OPTIONS}
          placeholder="全部意图"
          allowClear
          showSearch
          optionFilterProp="label"
          style={{ width: 160 }}
        />
        <Select
          value={action}
          onChange={setAction}
          options={ACTION_OPTIONS}
          placeholder="全部动作"
          allowClear
          style={{ width: 140 }}
        />
        <Input
          value={keywordInput}
          onChange={(e) => setKeywordInput(e.target.value)}
          onPressEnter={handleQuery}
          placeholder="搜索用户问题关键词"
          prefix={<SearchOutlined />}
          allowClear
          style={{ width: 260 }}
        />
        <Button type="primary" onClick={handleQuery}>
          查询
        </Button>
        <Button onClick={handleReset}>重置</Button>
      </div>

      {loadError ? (
        <ErrorState description={loadError} onRetry={fetchConversations} />
      ) : (
        <>
          <Spin spinning={loading}>
            <Table<AiConversationSummaryDTO>
              dataSource={conversations}
              columns={columns}
              rowKey={(record) => record.id}
              size="small"
              pagination={false}
              scroll={{ x: 'max-content' }}
              onRow={(record) => ({
                onClick: () => router.push(`/admin/ai-traces/conversations/${record.id}`),
              })}
              rowClassName="cursor-pointer"
              locale={{
                emptyText: <Empty description="该筛选区间内暂无对话记录" />,
              }}
              expandable={{
                expandedRowRender: renderExpanded,
                rowExpandable: () => true,
                columnWidth: W.expand,
              }}
            />
          </Spin>

          <div className="flex justify-end">
            <Pagination
              current={page}
              pageSize={pageSize}
              total={total}
              showSizeChanger
              showTotal={(count) => `共 ${count} 条`}
              onChange={(p, ps) => {
                setPage(p)
                if (ps) setPageSize(ps)
              }}
            />
          </div>
        </>
      )}
    </div>
  )
}
