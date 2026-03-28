import { Card, Flex } from 'antd'
import styles from './styles.module.css'
import { CompetitionBriefDTO } from '@/apis/schema/type'
import { FireOutlined } from '@ant-design/icons'
import CompetitionLogo from './CompetitionLogo'
import { API_BASE_URL } from '@/apis/config'

const CompetitionCard = ({ competition }: { competition: CompetitionBriefDTO }) => {
  const logoImageUrl = competition.logoFileId
    ? `${API_BASE_URL}/file/download/${competition.logoFileId}`
    : null

  return (
    <Card
      className={styles.card}
      styles={{
        body: {
          padding: '30px 35px',
        },
      }}
    >
      <Flex align="start" gap={10}>
        <div className={styles.logoWrapper}>
          {logoImageUrl ? (
            <CompetitionLogo src={logoImageUrl} alt={competition.name} />
          ) : (
            <FireOutlined style={{ fontSize: 28, color: 'white' }} />
          )}
        </div>
        <Flex vertical className={styles.textContainer}>
          <div className={styles.title}>{competition.name}</div>
          <div className={styles.description}>{competition.summary}</div>
        </Flex>
      </Flex>
    </Card>
  )
}

export default CompetitionCard
