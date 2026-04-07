import { Spin } from 'antd'

export default function ProfileLoading() {
  return (
    <div className="w-full min-h-screen bg-[#0a0a0a] text-white relative overflow-x-hidden">
      <div
        className="fixed top-0 left-0 w-full h-full pointer-events-none z-0"
        style={{
          background:
            'radial-gradient(ellipse 80% 50% at 20% 40%, rgba(102, 119, 255, 0.15) 0%, transparent 50%), radial-gradient(ellipse 60% 40% at 80% 60%, rgba(255, 107, 53, 0.1) 0%, transparent 50%)',
        }}
      />
      <div className="flex justify-center items-center min-h-[400px]">
        <Spin size="large" />
      </div>
    </div>
  )
}
