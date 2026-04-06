import { Col, Flex, Row, Skeleton } from 'antd'
import { competitionTitle, moreDescription, title } from './constant'
import Link from 'next/link'
import { RightOutlined } from '@ant-design/icons'

function CompetitionCardSkeleton() {
  return (
    <div className="min-h-[175px] p-[30px_35px] rounded-[16px] sm:rounded-[24px] md:rounded-[24px]">
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

export default function CompetitionsSkeleton() {
  return (
    <div className="flex flex-col items-center w-full h-[105vh] md:h-[105vh] p-[32px_20px_40px_20px] sm:p-[32px_40px_60px_40px] md:p-[32px_147px_96px_147px] min-h-screen md:min-h-screen box-border">
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
          <Row justify="space-evenly" gutter={[15, 15]}>
            {Array.from({ length: 6 }).map((_, index) => (
              <Col span={8} key={index}>
                <CompetitionCardSkeleton />
              </Col>
            ))}
          </Row>
          <Link
            href="/competitions"
            className="text-white no-underline inline-flex mt-5 w-fit hover:text-[#ffc93c]"
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
  )
}
