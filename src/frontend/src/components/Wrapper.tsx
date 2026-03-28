'use client'

import { ResponseMessage } from '@/apis/schema/type'
import { App } from 'antd'
import { useEffect } from 'react'

/**
 * 包装组件，用于处理 API 响应，响应失败会显示错误消息
 * @param children 子组件
 * @param apiResponse API 响应
 * @returns 包装后的组件
 */
const Wrapper = ({
  children,
  apiResponse,
}: {
  children: React.ReactNode
  apiResponse: ResponseMessage<unknown>
}) => {
  const { message: messageApi } = App.useApp()

  useEffect(() => {
    console.log(apiResponse)

    if (apiResponse.code >= 400) {
      messageApi.error(apiResponse.msg)
    }
  }, [apiResponse, messageApi])

  return <>{children}</>
}

export default Wrapper
