import { Col, Flex, Row, ConfigProvider } from 'antd'
import CompetitionCard from './CompetitionCard'
import Link from 'next/link'
import { RightOutlined } from '@ant-design/icons'
import { CompetitionBriefDTO } from '@/apis/schema/type'
import { competitionTitle, moreDescription, title } from './constant'

const themeConfig = {
  token: {
    fontSize: 14,
  },
}

const Competitions = ({ competitions }: { competitions: CompetitionBriefDTO[] }) => {
  const competitionsToShow = competitions.slice(0, 6)

  return (
    <ConfigProvider theme={themeConfig}>
      <div className="flex flex-col items-center w-full p-[32px_20px_40px_20px] sm:p-[32px_40px_60px_40px] md:p-[32px_147px_96px_147px] box-border">
        <Flex vertical gap={48} className="w-full">
          <h1 className="text-[28px] sm:text-[36px] md:text-[48px] font-bold text-white m-0">
            {title}
          </h1>
          <Flex
            className="w-full p-5 sm:p-[24px_30px] md:p-[34px_54px_24px_54px] rounded-[16px] sm:rounded-[24px] md:rounded-[24px]"
            gap={18}
            vertical
          >
            <h2 className="font-[Microsoft_YaHei] text-[20px] sm:text-[24px] md:text-[30px] font-bold leading-[1.4] sm:leading-[40px] md:leading-[40px] text-white m-0">
              {competitionTitle}
            </h2>
            <Row justify="space-evenly" gutter={[15, 15]} style={{ marginInline: '0' }}>
              {competitionsToShow.map((competition, index) => (
                <Col xs={24} md={12} lg={8} key={index}>
                  <CompetitionCard competition={competition} />
                </Col>
              ))}
            </Row>
            <Link
              href="/competitions"
              className="text-white! no-underline inline-flex mt-5 w-fit hover:text-[#ffc93c]!"
            >
              <Flex gap={8} align="center">
                <span className="text-inherit font-[Microsoft_YaHei] text-sm font-bold leading-[18px]">
                  {moreDescription}
                </span>
                <RightOutlined className="text-sm flex items-center justify-center min-w-[44px] min-h-[44px]" />
              </Flex>
            </Link>
          </Flex>
        </Flex>
      </div>
    </ConfigProvider>
  )
}

export default Competitions
