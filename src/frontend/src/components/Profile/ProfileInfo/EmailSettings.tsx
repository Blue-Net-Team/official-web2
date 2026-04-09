import { MailOutlined, EditOutlined } from '@ant-design/icons'
import { Button } from 'antd'

interface EmailSettingsProps {
  email: string
  onChangeEmail: () => void
}

export default function EmailSettings({ email, onChangeEmail }: EmailSettingsProps) {
  return (
    <div className="mt-8 pt-8 border-t border-white/[0.05]">
      <div className="text-lg font-semibold text-white mb-6 flex items-center gap-[10px] [&>svg]:w-5 [&>svg]:h-5 [&>svg]:text-[#6677ff]">
        <MailOutlined />
        邮箱设置
      </div>
      <div className="flex items-center justify-between p-4 bg-white/[0.02] rounded-[10px] mb-4 max-[640px]:flex-col max-[640px]:gap-3 max-[640px]:items-start">
        <div className="flex items-center gap-3 [&>svg]:w-5 [&>svg]:h-5 [&>svg]:text-[#6677ff]">
          <MailOutlined />
          <span className="text-sm text-white">{email}</span>
        </div>
        <div className="flex items-center gap-3">
          <Button className="px-4 py-2 rounded-lg bg-transparent" onClick={onChangeEmail}>
            <EditOutlined />
            修改邮箱
          </Button>
        </div>
      </div>
    </div>
  )
}
