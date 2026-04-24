import { Spin } from 'antd'

export default function CompetitionsLoading() {
  return (
    <div className="min-h-screen bg-black flex items-center justify-center">
      <Spin size="large" tip="加载竞赛数据..." />
    </div>
  )
}
