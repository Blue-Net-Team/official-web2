'use client'

import { Button, ConfigProvider, Flex } from 'antd'
import { DoubleRightOutlined, RightOutlined } from '@ant-design/icons'
import { useRouter } from 'next/navigation'
import styles from './styles.module.css'
import ProcessCard from './ProcessCard'
import registerIcon from '@/assets/HomeRecruitmentProcess/register_icon.png'
import assessmentIcon from '@/assets/HomeRecruitmentProcess/assessment_icon.png'
import admissionIcon from '@/assets/HomeRecruitmentProcess/admission_icon.png'

interface ProcessStep {
  id: string
  icon: string
  title: string
  description: string
  btn?: React.ReactNode
}

const btnTheme = {
  token: {
    borderRadius: 64,
    controlHeight: 44,
  },
  components: {
    Button: {
      defaultBg: '#fff',
      defaultHoverBg: '#fff',
      defaultHoverBorderColor: '#e89c6a',
      defaultHoverColor: '#d46b08',
      defaultColor: 'rgba(0,0,0,0.88)',
      defaultActiveBg: '#fff',
      defaultActiveBorderColor: '#e89c6a',
      defaultActiveColor: '#d46b08',
      controlHeight: 44,
    },
  },
}

const RecruitmentProcess = () => {
  const router = useRouter()

  const handleJoinClick = () => {
    router.push('/enroll')
  }

  const processSteps: ProcessStep[] = [
    {
      id: 'register',
      icon: registerIcon.src,
      title: '报名加入',
      description: '报名现已启动，每学年第一学期开始招新，不限制专业，仅对大一和大二的同学开放。',
      btn: (
        <ConfigProvider theme={btnTheme}>
          <Button
            color="default"
            onClick={handleJoinClick}
            icon={<RightOutlined />}
            iconPlacement="end"
            className={styles.processButton}
          >
            立即加入
          </Button>
        </ConfigProvider>
      ),
    },
    {
      id: 'assessment',
      icon: assessmentIcon.src,
      title: '参加考核',
      description:
        '考核流程将会在报名结束后的两周内陆续启动，每个方向的考核时间和轮次都有差异，难度大体相同',
    },
    {
      id: 'admission',
      icon: admissionIcon.src,
      title: '正式录用',
      description:
        '考核流程将会在报名结束后的两周内陆续启动，每个方向的考核时间和轮次都有差异，难度大体相同',
    },
  ]

  return (
    <Flex vertical align="start" className={styles.container}>
      <h1 className={styles.mainTitle}>
        现在<span className={styles.highlightTitle}>招新流程</span>已启动，欢迎加入我们
      </h1>

      <p className={styles.text}>
        任何专业都可以加入我们，技术不是门槛，我们更在乎学习态度与自学能力
      </p>
      <p className={styles.text}>新人录用分为以下3步，完成招新流程后即可加入团队</p>

      <div className={styles.cardsContainer}>
        <ProcessCard
          icon={processSteps[0].icon}
          title={processSteps[0].title}
          description={processSteps[0].description}
          btn={processSteps[0].btn}
        />
        <DoubleRightOutlined className={`${styles.arrowIcon} ${styles.arrowDesktop}`} />
        <DoubleRightOutlined className={`${styles.arrowIcon} ${styles.arrowMobile}`} />
        <ProcessCard
          icon={processSteps[1].icon}
          title={processSteps[1].title}
          description={processSteps[1].description}
        />
        <DoubleRightOutlined className={`${styles.arrowIcon} ${styles.arrowDesktop}`} />
        <DoubleRightOutlined className={`${styles.arrowIcon} ${styles.arrowMobile}`} />
        <ProcessCard
          icon={processSteps[2].icon}
          title={processSteps[2].title}
          description={processSteps[2].description}
        />
      </div>
    </Flex>
  )
}

export default RecruitmentProcess
