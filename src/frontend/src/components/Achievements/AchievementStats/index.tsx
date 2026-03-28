import { Card, Col, Row, Statistic } from 'antd'
import { TrophyOutlined, StarOutlined, CrownOutlined, FlagOutlined } from '@ant-design/icons'
import styles from './styles.module.css'
import { AchievementStatsDTO } from '@/apis/schema/type'

interface AchievementStatsProps {
  stats: AchievementStatsDTO
}

const AchievementStats = ({ stats }: AchievementStatsProps) => {
  const statItems = [
    {
      title: '总成就',
      value: stats.totalAchievements,
      icon: <TrophyOutlined style={{ fontSize: 24, color: '#1890ff' }} />,
      color: '#1890ff',
    },
    {
      title: '国家级',
      value: stats.nationalCount,
      icon: <CrownOutlined style={{ fontSize: 24, color: '#FFD700' }} />,
      color: '#FFD700',
    },
    {
      title: '省级',
      value: stats.provincialCount,
      icon: <StarOutlined style={{ fontSize: 24, color: '#C0C0C0' }} />,
      color: '#C0C0C0',
    },
    {
      title: '校级',
      value: stats.schoolCount,
      icon: <FlagOutlined style={{ fontSize: 24, color: '#CD7F32' }} />,
      color: '#CD7F32',
    },
  ]

  return (
    <Row gutter={[16, 16]} className={styles.statsContainer}>
      {statItems.map((item) => (
        <Col xs={12} sm={6} key={item.title}>
          <Card className={styles.statCard} styles={{ body: { padding: '16px' } }}>
            <div className={styles.statContent}>
              <div className={styles.statIcon}>{item.icon}</div>
              <Statistic
                title={<span className={styles.statTitle}>{item.title}</span>}
                value={item.value}
                valueStyle={{ color: item.color, fontWeight: 600 }}
              />
            </div>
          </Card>
        </Col>
      ))}
    </Row>
  )
}

export default AchievementStats
