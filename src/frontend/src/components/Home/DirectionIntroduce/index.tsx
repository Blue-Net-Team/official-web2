import { Row, Col, ConfigProvider } from 'antd'
import embed_cover from '@/assets/HomeDirectionIntroduce/embed_cover.png'
import embed_icon from '@/assets/icon/direction/embed_icon.png'
import cv_cover from '@/assets/HomeDirectionIntroduce/cv_cover.png'
import cv_icon from '@/assets/icon/direction/cv_icon.png'
import struct_cover from '@/assets/HomeDirectionIntroduce/struct_cover.png'
import struct_icon from '@/assets/icon/direction/struct_icon.png'
import DirectionCard from './DirectionCard'

const themeConfig = {
  token: {
    fontSize: 16,
  },
}

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
      <div className="min-h-screen w-full box-border p-[40px_20px] sm:p-[60px_40px] md:p-[84px_93px] flex flex-col gap-6 sm:gap-9 md:gap-9">
        <h1 className="font-[Microsoft_YaHei] text-[28px] sm:text-[36px] md:text-[43px] font-bold leading-[1.3] md:leading-[57px] text-left text-white m-0">
          专业的工作区分，为团队<span className="text-[#6677ff]">高效工作</span>提供保障
        </h1>

        <div className="w-full box-border flex flex-col justify-start items-start p-5 sm:p-6 md:p-[27px_39px] rounded-[24px] bg-[linear-gradient(0deg,rgba(53,91,205,0.48)_33.337%,rgba(30,61,154,0)_83.842%)]">
          <div className="max-w-[726px] text-white text-base sm:text-lg md:text-xl font-normal leading-[1.5] sm:leading-[1.44] md:leading-[26px] text-left sm:max-w-full">
            团队分为
            <span className="text-[36px] sm:text-[28px] md:text-[36px] text-[rgba(102,119,255,0.85)]">
              3个工作方向
            </span>
            ，各方向互相独立但又互相依赖，独立的工作内容在最大程度上减少团队摩擦，互相联合的方式为小队搭建优秀作品提供保障
          </div>

          <Row
            gutter={[20, 20]}
            className="ml-0 py-3 sm:py-4 md:py-[19px] w-full justify-start sm:justify-center"
          >
            {directionContent.map((item, index) => (
              <Col key={index} xs={24} md={12} lg={8}>
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
          <div className="pl-0 sm:pl-[25px] w-fit flex items-center gap-[5px]">
            <div className="w-[3px] min-w-[3px] h-[21px] bg-[rgba(119,108,230,1)]" />
            <span className="w-auto sm:w-[128px] h-[21px] text-white font-[Microsoft_YaHei] text-sm sm:text-base font-normal leading-[21px] text-left">
              点击卡片了解更多
            </span>
          </div>
        </div>
      </div>
    </ConfigProvider>
  )
}

export default DirectionIntroduce
