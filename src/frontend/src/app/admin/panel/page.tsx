'use client'

import { useCallback, useEffect, useState } from 'react'
import { Card, Grid, Segmented, Spin, Table, Tag, Typography } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { Line } from '@ant-design/charts'
import { auditStatisticsService } from '@/apis/services/audit-statistics.service'
import type { TrendPointDTO, EndpointRankingDTO, EndpointLatencyDTO } from '@/apis/schema/type'

const { useBreakpoint } = Grid

const { Title } = Typography

type Period = '24h' | '7d' | '30d'

export default function MonitoringDashboard() {
  const screens = useBreakpoint()
  const isMobile = !screens.md
  const [period, setPeriod] = useState<Period>('7d')
  const [trends, setTrends] = useState<TrendPointDTO[]>([])
  const [endpoints, setEndpoints] = useState<EndpointRankingDTO[]>([])
  const [latency, setLatency] = useState<EndpointLatencyDTO[]>([])
  const [loading, setLoading] = useState(true)

  const fetchData = useCallback(async (p: Period) => {
    setLoading(true)
    try {
      const [trendsRes, endpointsRes, latencyRes] = await Promise.all([
        auditStatisticsService.getTrends(p),
        auditStatisticsService.getEndpointRanking(p),
        auditStatisticsService.getEndpointLatencyRanking(p),
      ])
      setTrends(trendsRes.data ?? [])
      setEndpoints(endpointsRes.data ?? [])
      setLatency(latencyRes.data ?? [])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchData(period)
  }, [period, fetchData])

  const periodLabel = (v: string) => {
    switch (v) {
      case '24h':
        return '近 24 小时'
      case '7d':
        return '近 7 天'
      case '30d':
        return '近 30 天'
      default:
        return v
    }
  }

  const chartData = trends.map((t) => ({
    time: new Date(t.time)
      .toLocaleString('zh-CN', {
        month: period === '24h' ? undefined : '2-digit',
        day: period === '24h' ? undefined : '2-digit',
        hour: period === '24h' ? '2-digit' : undefined,
        minute: period === '24h' ? '2-digit' : undefined,
      })
      .replace(/^\//, ''),
    count: t.count,
  }))

  const endpointColumns: ColumnsType<EndpointRankingDTO> = [
    { title: '接口路径', dataIndex: 'pattern', key: 'pattern', ellipsis: true },
    {
      title: '请求次数',
      dataIndex: 'count',
      key: 'count',
      width: 100,
      sorter: (a, b) => a.count - b.count,
    },
    {
      title: '平均耗时',
      dataIndex: 'avgDurationMs',
      key: 'avgDurationMs',
      width: 110,
      render: (v: number) => `${v.toFixed(1)} ms`,
    },
    {
      title: '错误数',
      dataIndex: 'errorCount',
      key: 'errorCount',
      width: 90,
      render: (v: number) => (v > 0 ? <Tag color="red">{v}</Tag> : v),
    },
  ]

  const latencyColumns: ColumnsType<EndpointLatencyDTO> = [
    { title: '接口路径', dataIndex: 'pattern', key: 'pattern', ellipsis: true },
    {
      title: '平均耗时',
      dataIndex: 'avgDurationMs',
      key: 'avgDurationMs',
      width: 110,
      render: (v: number) => `${v.toFixed(1)} ms`,
      sorter: (a, b) => a.avgDurationMs - b.avgDurationMs,
    },
    {
      title: '最大耗时',
      dataIndex: 'maxDurationMs',
      key: 'maxDurationMs',
      width: 110,
      render: (v: number) => `${v} ms`,
    },
    { title: '请求次数', dataIndex: 'count', key: 'count', width: 100 },
  ]

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: isMobile ? 16 : 24 }}>
      <div
        style={{
          display: 'flex',
          flexDirection: isMobile ? 'column' : 'row',
          justifyContent: 'space-between',
          alignItems: isMobile ? 'flex-start' : 'center',
          gap: isMobile ? 12 : 0,
        }}
      >
        <Title level={4} style={{ margin: 0 }}>
          API 监控仪表盘
        </Title>
        <Segmented
          options={[
            { label: '24 小时', value: '24h' },
            { label: '7 天', value: '7d' },
            { label: '30 天', value: '30d' },
          ]}
          value={period}
          onChange={(v) => setPeriod(v as Period)}
        />
      </div>

      <Spin spinning={loading}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: isMobile ? 16 : 24 }}>
          <Card
            title={`请求量趋势 — ${periodLabel(period)}`}
            styles={{ body: { padding: isMobile ? 12 : 24 } }}
          >
            {chartData.length > 0 ? (
              <Line
                data={chartData}
                xField="time"
                yField="count"
                smooth
                point={{ size: 3 }}
                height={300}
              />
            ) : (
              <div style={{ textAlign: 'center', padding: '60px 0', color: '#999' }}>暂无数据</div>
            )}
          </Card>

          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 400px), 1fr))',
              gap: 24,
            }}
          >
            <Card
              title={`接口访问排名 — ${periodLabel(period)}`}
              styles={{ body: { overflow: 'hidden', padding: isMobile ? 12 : 24 } }}
            >
              <Table
                dataSource={endpoints}
                columns={endpointColumns}
                rowKey="pattern"
                size="small"
                pagination={false}
                scroll={{ x: 'max-content', y: 480 }}
              />
            </Card>

            <Card
              title={`接口响应时间排名 — ${periodLabel(period)}`}
              styles={{ body: { overflow: 'hidden', padding: isMobile ? 12 : 24 } }}
            >
              <Table
                dataSource={latency}
                columns={latencyColumns}
                rowKey="pattern"
                size="small"
                pagination={false}
                scroll={{ x: 'max-content', y: 480 }}
              />
            </Card>
          </div>
        </div>
      </Spin>
    </div>
  )
}
