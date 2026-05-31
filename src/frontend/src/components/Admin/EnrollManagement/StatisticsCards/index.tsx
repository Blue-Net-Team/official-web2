'use client'

import { Card, Statistic } from 'antd'
import {
  ClockCircleOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  TeamOutlined,
} from '@ant-design/icons'
import type { EnrollmentStatisticsDTO } from '@/apis/schema/type'

interface StatisticsCardsProps {
  statistics: EnrollmentStatisticsDTO | null
}

const cardStyle: React.CSSProperties = { borderColor: 'rgba(255,255,255,0.12)' }

export const StatisticsCards: React.FC<StatisticsCardsProps> = ({ statistics }) => {
  const byStatus = statistics?.byStatus ?? {}
  const items = [
    {
      title: '总报名',
      value: statistics?.total ?? 0,
      icon: <TeamOutlined />,
      contentStyle: { color: 'rgba(255,255,255,0.85)' },
    },
    {
      title: '待审核',
      value: byStatus['pending'] ?? byStatus['PENDING'] ?? 0,
      icon: <ClockCircleOutlined />,
      contentStyle: { color: '#1677ff' },
    },
    {
      title: '已通过',
      value: byStatus['approved'] ?? byStatus['APPROVED'] ?? 0,
      icon: <CheckCircleOutlined />,
      contentStyle: { color: '#52c41a' },
    },
    {
      title: '已拒绝',
      value: byStatus['rejected'] ?? byStatus['REJECTED'] ?? 0,
      icon: <CloseCircleOutlined />,
      contentStyle: { color: '#ff4d4f' },
    },
  ]

  return (
    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
      {items.map((item) => (
        <Card key={item.title} style={cardStyle} styles={{ body: { padding: '16px 20px' } }}>
          <Statistic
            title={item.title}
            value={item.value}
            prefix={item.icon}
            styles={{ content: item.contentStyle }}
          />
        </Card>
      ))}
    </div>
  )
}
