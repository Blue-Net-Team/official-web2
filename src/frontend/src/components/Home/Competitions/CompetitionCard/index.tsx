import { Card, Flex } from 'antd'
import { CompetitionResponseDTO } from '@/apis/schema/type'
import { FireOutlined } from '@ant-design/icons'
import CompetitionLogo from './CompetitionLogo'
import { PUBLIC_API_BASE_URL } from '@/apis/config'

const CompetitionCard = ({ competition }: { competition: CompetitionResponseDTO }) => {
  const logoImageUrl = competition.logoFileId
    ? `${PUBLIC_API_BASE_URL}/file/download/${competition.logoFileId}`
    : null

  return (
    <Card
      className="min-h-[100%] !rounded-[8px] !bg-[linear-gradient(135deg,#e86835_0%,#f08040_100%)] !border !border-[rgba(213,171,118,0.62)] !shadow-[0_4px_12px_rgba(232,104,53,0.25)]"
      styles={{
        body: {
          padding: '30px 35px',
        },
      }}
    >
      <Flex align="start" gap={10}>
        <div className="shrink-0 w-fit h-11 mr-0 ">
          {logoImageUrl ? (
            <CompetitionLogo src={logoImageUrl} alt={competition.name} />
          ) : (
            <FireOutlined className="text-[28px] text-white" />
          )}
        </div>
        <Flex vertical className="gap-2.5 flex-1">
          <div className="text-white text-base font-bold font-[Microsoft_YaHei] leading-[1.3]">
            {competition.name}
          </div>
          <div className="text-white text-xs font-bold font-[Microsoft_YaHei] leading-[1.4]">
            {competition.summary}
          </div>
        </Flex>
      </Flex>
    </Card>
  )
}

export default CompetitionCard
