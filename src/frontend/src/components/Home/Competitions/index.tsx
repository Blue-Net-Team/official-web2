import { Col, Flex, Row, ConfigProvider } from 'antd'
import CompetitionCard from './CompetitionCard'
import styles from './styles.module.css'
import Link from 'next/link'
import { RightOutlined } from '@ant-design/icons'
import { CompetitionBriefDTO } from '@/apis/schema/type'
import { competitionTitle, moreDescription, title } from './constant'

// Ant Design 主题配置
const themeConfig = {
  token: {
    fontSize: 14,
  },
}

/**
 * 竞赛组件
 * @param competitions 竞赛信息数组
 * @returns 渲染后的竞赛组件
 */
const Competitions = ({ competitions }: { competitions: CompetitionBriefDTO[] }) => {
  // 只取前6个
  const competitionsToShow = competitions.slice(0, 6)

  return (
    <ConfigProvider theme={themeConfig}>
      <div className={`${styles.contentPage} ${styles.fullWidth}`}>
        <Flex vertical gap={48} className={styles.fullWidth}>
          <h1 className={styles.title}>{title}</h1>
          <Flex className={`${styles.fullWidth} ${styles.competitionContainer}`} gap={18} vertical>
            <h2 className={styles.competitionTitle}>{competitionTitle}</h2>
            {/* 使用Ant Design Grid响应式属性 */}
            <Row justify="space-evenly" gutter={[15, 15]} style={{ marginInline: '0' }}>
              {competitionsToShow.map((competition, index) => (
                <Col
                  xs={24} // 移动端：单列
                  md={12} // 平板：双列
                  lg={8} // 桌面：三列
                  key={index}
                >
                  <CompetitionCard competition={competition} />
                </Col>
              ))}
            </Row>
            <Link href="/competitions" className={styles.moreLink}>
              <Flex gap={8} align="center">
                <span className={styles.moreDescription}>{moreDescription}</span>
                <RightOutlined className={styles.moreIcon} />
              </Flex>
            </Link>
          </Flex>
        </Flex>
      </div>
    </ConfigProvider>
  )
}

export default Competitions
