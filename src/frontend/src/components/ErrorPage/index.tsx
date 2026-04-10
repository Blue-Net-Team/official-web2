'use client'

import { Flex, Typography } from 'antd'
import { ErrorPageConfig } from './configs'

const { Title, Text } = Typography

export default function ErrorPage({ config }: { config: ErrorPageConfig }) {
  return (
    <Flex
      vertical
      align="center"
      justify="center"
      style={{ minHeight: 'calc(100vh - 64px)' }}
      gap={16}
    >
      <div style={{ fontSize: 72, color: '#fa8c16' }}>{config.icon}</div>
      <Title level={1} style={{ margin: 0 }}>
        {config.statusCode}
      </Title>
      <Text type="secondary" style={{ fontSize: 16 }}>
        {config.description}
      </Text>
    </Flex>
  )
}
