import { Col, Flex, Row, Skeleton } from 'antd'
import competitionStyles from './styles.module.css'
import cardStyles from './styles.module.css'
import styles from './styles.module.css'
import { competitionTitle, moreDescription, title } from './constant'
import Link from 'next/link'
import { RightOutlined } from '@ant-design/icons'

/**
 * 竞赛卡片骨架屏
 */
function CompetitionCardSkeleton() {
  return (
    <div className={cardStyles.card} style={{ minHeight: 175, padding: '30px 35px' }}>
      <Flex align="start" gap={10}>
        <div style={{ width: 40, height: 40, flexShrink: 0 }}>
          <Skeleton active avatar={{ size: 40, shape: 'square' }} paragraph={false} title={false} />
        </div>
        <Flex vertical style={{ flex: 1, gap: 10 }}>
          <Skeleton active title={{ width: '80%', style: { margin: 0 } }} paragraph={false} />
          <Skeleton active title={{ width: '60%', style: { margin: 0 } }} paragraph={false} />
        </Flex>
      </Flex>
    </div>
  )
}

/**
 * 竞赛区域骨架屏
 */
export default function CompetitionsSkeleton() {
  return (
    <div className={`${competitionStyles.contentPage} ${competitionStyles.fullWidth}`}>
      <Flex vertical gap={48} className={competitionStyles.fullWidth}>
        <h1 className={styles.title}>{title}</h1>
        <Flex
          className={`${competitionStyles.fullWidth} ${competitionStyles.competitionContainer}`}
          gap={18}
          vertical
        >
          <h2 className={styles.competitionTitle}>{competitionTitle}</h2>
          <Row justify="space-evenly" gutter={[15, 15]}>
            {Array.from({ length: 6 }).map((_, index) => (
              <Col span={8} key={index}>
                <CompetitionCardSkeleton />
              </Col>
            ))}
          </Row>
          <Link href="/competitions" className={styles.moreLink}>
            <Flex gap={8}>
              <span className={styles.moreDescription}>{moreDescription}</span>
              <RightOutlined />
            </Flex>
          </Link>
        </Flex>
      </Flex>
    </div>
  )
}
