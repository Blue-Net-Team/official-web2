import { Row, Col, ConfigProvider } from 'antd'
import styles from './styles.module.css'
import embed_cover from '@/assets/HomeDirectionIntroduce/embed_cover.png'
import embed_icon from '@/assets/icon/direction/embed_icon.png'
import cv_cover from '@/assets/HomeDirectionIntroduce/cv_cover.png'
import cv_icon from '@/assets/icon/direction/cv_icon.png'
import struct_cover from '@/assets/HomeDirectionIntroduce/struct_cover.png'
import struct_icon from '@/assets/icon/direction/struct_icon.png'
import DirectionCard from './DirectionCard'

// Ant Design 主题配置
const themeConfig = {
  token: {
    fontSize: 16,
  },
}

/**
 * 方向介绍
 */
const DirectionIntroduce = () => {
  const directionContent = [
    {
      title: '计算机视觉',
      desc: '负责让机器人感受世界',
      cover: cv_cover.src,
      icon: cv_icon.src,
      linkTo: '/direction/cv',
    },
    {
      title: '嵌入式开发',
      desc: '为机器人注入灵魂',
      cover: embed_cover.src,
      icon: embed_icon.src,
      linkTo: '/direction/embed',
    },
    {
      title: '结构设计',
      desc: '负责设计机器人外观',
      cover: struct_cover.src,
      icon: struct_icon.src,
      linkTo: '/direction/struct',
    },
  ]

  return (
    <ConfigProvider theme={themeConfig}>
      <div className={styles.container}>
        <h1 className={styles.title}>
          专业的工作区分，为团队<span className={styles.highlightTitle}>高效工作</span>提供保障
        </h1>

        <div className={styles.content}>
          <div className={styles.textContent}>
            团队分为<span className={styles.highlightText}>3个工作方向</span>
            ，各方向互相独立但又互相依赖，独立的工作内容在最大程度上减少团队摩擦，互相联合的方式为小队搭建优秀作品提供保障
          </div>

          {/* 使用Ant Design Grid响应式属性 */}
          <Row gutter={[20, 20]} className={styles.row}>
            {directionContent.map((item, index) => (
              <Col
                key={index}
                xs={24} // 移动端：单列
                md={12} // 平板：双列
                lg={8} // 桌面：三列
              >
                <DirectionCard
                  title={item.title}
                  desc={item.desc}
                  cover={item.cover}
                  icon={item.icon}
                  linkTo={item.linkTo}
                />
              </Col>
            ))}
          </Row>
          <div className={styles.moreLink}>
            <div className={styles.bar} />
            <span className={styles.moreDesc}>点击卡片了解更多</span>
          </div>
        </div>
      </div>
    </ConfigProvider>
  )
}

export default DirectionIntroduce
