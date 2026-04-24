import { Spin } from 'antd'

export default function MembersLoading() {
  return (
    <div className="min-h-screen bg-[#0a0a0a] flex items-center justify-center">
      <Spin size="large" tip="加载成员列表..." />
    </div>
  )
}
