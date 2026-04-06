'use client'

import { Flex, Button, ConfigProvider } from 'antd'
import { useRouter } from 'next/navigation'

const desktopButtonTheme = {
  components: {
    Button: {
      defaultBg: '#ffffff',
      defaultColor: '#000000',
      defaultHoverBg: '#f0f0f0',
      defaultHoverColor: '#000000',
      defaultActiveBg: '#e0e0e0',
      defaultActiveColor: '#000000',
      borderRadius: 42,
      controlHeight: 50,
      fontSize: 20,
      fontWeight: 400,
      paddingInline: 26,
      primaryShadow: 'none',
      borderWidth: 0,
    },
  },
}

const mobileButtonTheme = {
  components: {
    Button: {
      defaultColor: '#000000',
      defaultBg: '#ffffff',
      defaultHoverColor: '#000000',
      defaultHoverBg: '#f0f0f0',
      defaultActiveColor: '#000000',
      defaultActiveBg: '#e0e0e0',
      borderRadius: 42,
      controlHeight: 44,
      fontSize: 16,
      fontWeight: 400,
      paddingInline: 20,
      defaultShadow: 'none',
      borderWidth: 0,
    },
  },
}

export default function TopContent() {
  const router = useRouter()

  const handleJoinClick = () => {
    router.push('/enroll')
  }

  return (
    <div className="flex flex-col items-center w-full h-auto p-[60px_20px] sm:p-[100px_40px] md:p-[147px_152px] md:min-h-[105vh] box-border">
      <Flex vertical align="flex-start" gap={24} className="w-full sm:gap-[42px]">
        <h1 className="text-[32px] font-bold text-white m-0 text-left leading-[1.3] sm:text-[40px] sm:leading-[1.32] md:text-[48px]">
          <span className="text-[#ffc93c] [text-shadow:0_2px_4px_rgba(0,0,0,0.5)]">蓝网</span>
          招新流程已启动
        </h1>
        <p className="text-[20px] font-normal text-white m-0 text-left leading-[1.5] w-full sm:text-[24px] sm:leading-[1.31] md:text-[32px] md:w-[882px] md:max-w-full">
          从竞赛实战的技术锤炼到师生同研的机器人项目，科创之路的奖学金与就业赋能，我们全程护航
        </p>
        <ConfigProvider theme={desktopButtonTheme}>
          <Button
            color="default"
            variant="solid"
            type="primary"
            className="!w-[132px] !hidden md:!flex"
            onClick={handleJoinClick}
          >
            立即加入
          </Button>
        </ConfigProvider>
        <ConfigProvider theme={mobileButtonTheme}>
          <Button
            color="default"
            variant="solid"
            type="primary"
            className="!w-full !hidden max-md:!flex"
            onClick={handleJoinClick}
          >
            立即加入
          </Button>
        </ConfigProvider>
      </Flex>
    </div>
  )
}
