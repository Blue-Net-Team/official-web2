'use client'

import { Button, ConfigProvider } from 'antd'
import { RightOutlined } from '@ant-design/icons'
import { useRouter } from 'next/navigation'
import Image from 'next/image'
import equipmentBg from '@/assets/equipment_bg.png'
import printerIcon from '@/assets/3dPrinter.png'

const desktopButtonTheme = {
  components: {
    Button: {
      defaultBg: '#ffffff',
      defaultColor: '#000000',
      defaultHoverBg: '#f0f0f0',
      defaultHoverColor: '#000000',
      defaultActiveBg: '#e0e0e0',
      defaultActiveColor: '#000000',
      borderRadius: 20,
      controlHeight: 44,
      fontSize: 16,
      fontWeight: 400,
      paddingInline: 15,
      primaryShadow: 'none',
      borderWidth: 0,
    },
  },
}

export default function FeaturedEquipment() {
  const router = useRouter()

  const handleBrowseMore = () => {
    router.push('/lab-environment')
  }

  return (
    <div className="w-full min-h-screen md:min-h-screen box-border mx-auto py-10 bg-[linear-gradient(180deg,rgba(0,0,0,0)_0%,rgba(29,3,1,0.57)_32.3%,rgba(51,41,206,1)_75.2%,rgba(51,41,206,1)_100%)] flex flex-col justify-center items-center">
      <div className="relative w-[90%] md:w-[70%] h-auto min-h-[300px] sm:min-h-[350px] md:h-[407px] rounded-[36px] sm:rounded-[48px] md:rounded-[72px] border-[3px] border-[#2f27b0] bg-black p-[30px_24px] sm:p-9 md:p-[42.5px_48px] box-border overflow-hidden flex items-start">
        <div
          className="absolute inset-0 opacity-50 z-[1]"
          style={{
            backgroundImage: `url(${equipmentBg.src})`,
            backgroundSize: 'cover',
            backgroundPosition: 'right center',
            backgroundRepeat: 'no-repeat',
          }}
        />
        <div className="relative z-[2] w-full md:max-w-[500px] pl-0 md:pl-[21px] flex flex-col justify-center gap-3 sm:gap-[18px] md:gap-[18px]">
          <div className="w-[44px] h-[44px] sm:w-[52px] sm:h-[52px] md:w-[52px] md:h-[52px] flex items-center justify-center">
            <Image
              src={printerIcon}
              alt="3D打印"
              width={52}
              height={52}
              className="w-[44px] h-[44px] sm:w-[52px] sm:h-[52px] md:w-[52px] md:h-[52px]"
            />
          </div>
          <h2 className="text-[24px] sm:text-[28px] md:text-[35px] font-bold text-white m-0 font-[Microsoft_YaHei] leading-[1.3]">
            3D打印与3轴数铣
          </h2>
          <p className="text-base sm:text-[18px] md:text-[20px] font-normal text-white m-0 font-[Microsoft_YaHei] leading-[1.5]">
            团队配备先进3D打印机和3轴数控铣床，为机器人开发深度赋能，帮我团队开发者更高效搭建机器人
          </p>
          <ConfigProvider theme={desktopButtonTheme}>
            <Button
              type="default"
              className="!w-fit !h-auto !min-h-[44px] !rounded-[20px] !bg-white !text-black !text-sm sm:!text-base !font-normal !border-none !flex !items-center !gap-2 !px-3 sm:!px-[15px] !py-2 sm:!py-[9px] !shadow-none"
              onClick={handleBrowseMore}
            >
              浏览更多团队装备
              <RightOutlined />
            </Button>
          </ConfigProvider>
        </div>
      </div>
    </div>
  )
}
