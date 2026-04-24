'use client'

import { Button, Flex, Typography } from 'antd'
import { ErrorPageConfig } from './configs'
import { useRouter } from 'next/navigation'

const { Title, Text } = Typography

export default function ErrorPage({ config }: { config: ErrorPageConfig }) {
  const router = useRouter()
  return (
    <Flex vertical align="center" justify="center" className="min-h-[calc(100vh-64px)]" gap={16}>
      <div className="text-7xl text-[#fa8c16]">{config.icon}</div>
      <Title level={1} className="!m-0">
        {config.statusCode}
      </Title>
      <Text type="secondary" className="text-base">
        {config.description}
      </Text>
      <Button type="primary" onClick={() => router.push('/')}>
        返回首页
      </Button>
    </Flex>
  )
}
