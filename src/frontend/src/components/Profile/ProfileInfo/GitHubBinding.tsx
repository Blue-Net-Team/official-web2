'use client'

import { useState, useEffect, useCallback } from 'react'
import { Button, App } from 'antd'
import { GithubOutlined, LinkOutlined, DisconnectOutlined } from '@ant-design/icons'
import { authService } from '@/apis/services/auth.service'
import { useSearchParams, useRouter } from 'next/navigation'

interface GitHubBindingProps {
  initialGithubUsername?: string | null
}

export default function GitHubBinding({ initialGithubUsername }: GitHubBindingProps) {
  const [githubUsername, setGithubUsername] = useState<string | null>(initialGithubUsername ?? null)
  const [loading, setLoading] = useState(false)
  const [bindLoading, setBindLoading] = useState(false)
  const { message: messageApi, modal } = App.useApp()
  const searchParams = useSearchParams()
  const router = useRouter()

  useEffect(() => {
    const githubParam = searchParams.get('github')
    if (githubParam === 'binding_success') {
      messageApi.success('GitHub 账号绑定成功')
      refreshStatus()
      router.replace('/profile')
    } else if (githubParam === 'already_bound') {
      messageApi.error('该 GitHub 账号已被其他用户绑定')
      router.replace('/profile')
    } else if (githubParam === 'error') {
      messageApi.error('GitHub 绑定失败，请稍后重试')
      router.replace('/profile')
    }
  }, [searchParams])

  const refreshStatus = useCallback(async () => {
    try {
      const res = await authService.getGithubBindingStatus()
      setGithubUsername(res.data)
    } catch {
      // Ignore
    }
  }, [])

  const handleBind = async () => {
    setBindLoading(true)
    try {
      const res = await authService.getGithubBindUrl()
      if (res.data) {
        window.location.href = res.data
      }
    } catch {
      messageApi.error('获取 GitHub 授权链接失败')
    } finally {
      setBindLoading(false)
    }
  }

  const handleUnbind = () => {
    modal.confirm({
      title: '解绑 GitHub 账号',
      content: '确定要解绑 GitHub 账号吗？解绑后将无法使用 GitHub 登录。',
      okText: '确定解绑',
      cancelText: '取消',
      okType: 'danger',
      onOk: async () => {
        setLoading(true)
        try {
          await authService.unbindGithub()
          setGithubUsername(null)
          messageApi.success('已解绑 GitHub 账号')
        } catch {
          messageApi.error('解绑失败，请稍后重试')
        } finally {
          setLoading(false)
        }
      },
    })
  }

  return (
    <div className="mt-8 pt-8 border-t border-white/[0.05]">
      <div className="text-lg font-semibold text-white mb-6 flex items-center gap-[10px] [&>svg]:w-5 [&>svg]:h-5 [&>svg]:text-[#6677ff]">
        <GithubOutlined />
        GitHub 账号
      </div>
      <div className="flex items-center justify-between p-4 bg-white/[0.02] rounded-[10px] max-[640px]:flex-col max-[640px]:gap-3 max-[640px]:items-start">
        <div className="flex items-center gap-3 [&>svg]:w-5 [&>svg]:h-5 [&>svg]:text-[#6677ff]">
          <GithubOutlined />
          {githubUsername ? (
            <span className="text-sm text-white">{githubUsername}</span>
          ) : (
            <span className="text-sm text-white opacity-50">未绑定</span>
          )}
        </div>
        <div className="flex items-center gap-3">
          {githubUsername ? (
            <>
              <span className="flex items-center gap-1.5 text-xs text-[#07c160] [&>svg]:w-[14px] [&>svg]:h-[14px]">
                <LinkOutlined />
                已绑定
              </span>
              <Button
                className="px-4 py-2 rounded-lg bg-transparent !border-[rgba(255,77,79,0.3)] text-[#ff4d4f] text-[13px] font-medium cursor-pointer transition-all duration-300 flex items-center gap-1.5 hover:enabled:bg-[rgba(255,77,79,0.1)] hover:enabled:!border-[#ff4d4f]"
                danger
                onClick={handleUnbind}
                loading={loading}
                icon={<DisconnectOutlined />}
              >
                解绑
              </Button>
            </>
          ) : (
            <Button
              className="px-4 py-2 rounded-lg bg-transparent !border-[rgba(102,119,255,0.3)] text-[#6677ff] text-[13px] font-medium cursor-pointer transition-all duration-300 flex items-center gap-1.5 hover:enabled:bg-[rgba(102,119,255,0.1)] hover:enabled:!border-[#6677ff]"
              type="primary"
              onClick={handleBind}
              loading={bindLoading}
              icon={<GithubOutlined />}
            >
              绑定 GitHub
            </Button>
          )}
        </div>
      </div>
    </div>
  )
}
