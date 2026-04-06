'use client'

import { Button, ConfigProvider } from 'antd'
import { DoubleRightOutlined, RightOutlined } from '@ant-design/icons'
import { useRouter } from 'next/navigation'
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
            className="min-h-[44px] flex items-center justify-center"
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
    <div className="w-full min-h-screen p-[40px_20px] sm:p-[60px_40px] md:p-[60px_40px] lg:p-[70.5px_93px] flex flex-col gap-3 sm:gap-4 md:gap-5 box-border items-start">
      <h1 className="font-[Microsoft_YaHei] text-2xl sm:text-[28px] md:text-9 font-bold text-white text-left m-0">
        现在
        <span className="text-[40px] sm:text-[48px] md:text-[60px] lg:text-[79px] font-bold text-left text-[rgba(255,111,60,1)]">
          招新流程
        </span>
        已启动，欢迎加入我们
      </h1>

      <p className="text-white text-sm sm:text-base md:text-lg lg:text-xl font-normal leading-[1.5] sm:leading-[26px] text-left m-0">
        任何专业都可以加入我们，技术不是门槛，我们更在乎学习态度与自学能力
      </p>
      <p className="text-white text-sm sm:text-base md:text-lg lg:text-xl font-normal leading-[1.5] sm:leading-[26px] text-left m-0">
        新人录用分为以下3步，完成招新流程后即可加入团队
      </p>

      <div className="w-full flex flex-col items-center py-5 sm:py-6 md:py-7 lg:py-[23px_60px] gap-4 lg:flex-row lg:justify-between lg:items-center lg:gap-4">
        <ProcessCard
          icon={processSteps[0].icon}
          title={processSteps[0].title}
          description={processSteps[0].description}
          btn={processSteps[0].btn}
        />
        <DoubleRightOutlined className="rotate-90 lg:rotate-0 text-[32px] sm:text-[40px] md:text-[40px] lg:text-[64px] text-[rgba(255,111,60,0.6)]! min-w-[44px] min-h-[44px]" />
        <ProcessCard
          icon={processSteps[1].icon}
          title={processSteps[1].title}
          description={processSteps[1].description}
        />
        <DoubleRightOutlined className="rotate-90 lg:rotate-0 text-[32px] sm:text-[40px] md:text-[40px] lg:text-[64px] text-[rgba(255,111,60,0.6)]! min-w-[44px] min-h-[44px]" />
        <ProcessCard
          icon={processSteps[2].icon}
          title={processSteps[2].title}
          description={processSteps[2].description}
        />
      </div>
    </div>
  )
}

export default RecruitmentProcess
