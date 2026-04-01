'use client'

import { Flex, Button, ConfigProvider } from 'antd'
import { useRouter } from 'next/navigation'
import styles from './styles.module.css'

// Ant Design 主题配置 - 移动端适配
const themeConfig = {
  token: {
    borderRadius: 42,
    controlHeight: 50,
  },
  components: {
    Button: {
      defaultBg: '#ffffff',
      defaultColor: '#000000',
      defaultHoverBg: '#f0f0f0',
      defaultHoverColor: '#000000',
      defaultActiveBg: '#e0e0e0',
      defaultActiveColor: '#000000',
      controlHeight: 50,
    },
  },
}

// 移动端主题配置
const mobileThemeConfig = {
  token: {
    borderRadius: 42,
    controlHeight: 44,
  },
  components: {
    Button: {
      defaultBg: '#ffffff',
      defaultColor: '#000000',
      defaultHoverBg: '#f0f0f0',
      defaultHoverColor: '#000000',
      defaultActiveBg: '#e0e0e0',
      defaultActiveColor: '#000000',
      controlHeight: 44,
    },
  },
}

export default function TopContent() {
  const router = useRouter()

  const handleJoinClick = () => {
    router.push('/enroll')
  }

  return (
    <div className={styles.contentPage}>
      <Flex vertical align="flex-start" gap={42} className={styles.heroSection}>
        <h1 className={styles.heroTitle}>
          <span className={styles.heroTitleHighlight}>蓝网</span>招新流程已启动
        </h1>
        <p className={styles.heroSubtitle}>
          从竞赛实战的技术锤炼到师生同研的机器人项目，科创之路的奖学金与就业赋能，我们全程护航
        </p>
        <ConfigProvider theme={themeConfig}>
          <Button
            type="primary"
            className={`${styles.heroButton} ${styles.heroButtonDesktop}`}
            onClick={handleJoinClick}
          >
            立即加入
          </Button>
        </ConfigProvider>
        <ConfigProvider theme={mobileThemeConfig}>
          <Button
            type="primary"
            className={`${styles.heroButton} ${styles.heroButtonMobile}`}
            onClick={handleJoinClick}
          >
            立即加入
          </Button>
        </ConfigProvider>
      </Flex>
    </div>
  )
}
