'use client'

import { useState, useEffect, useCallback } from 'react'
import { Button, Modal, App, Spin } from 'antd'
import { GithubOutlined, LinkOutlined, DisconnectOutlined } from '@ant-design/icons'
import { authService } from '@/apis/services/auth.service'
import { useSearchParams, useRouter } from 'next/navigation'
import styles from './styles.module.css'

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

  // Handle OAuth callback params
  useEffect(() => {
    const githubParam = searchParams.get('github')
    if (githubParam === 'binding_success') {
      messageApi.success('GitHub 账号绑定成功')
      // Refresh binding status
      refreshStatus()
      // Clean up URL
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
    <div className={styles.emailSection}>
      <div className={styles.formSectionTitle}>
        <GithubOutlined />
        GitHub 账号
      </div>
      <div className={styles.emailDisplay}>
        <div className={styles.emailInfo}>
          <GithubOutlined />
          {githubUsername ? (
            <span className={styles.emailText}>{githubUsername}</span>
          ) : (
            <span className={styles.emailText} style={{ opacity: 0.5 }}>
              未绑定
            </span>
          )}
        </div>
        <div className={styles.emailRight}>
          {githubUsername ? (
            <>
              <span className={styles.emailStatus}>
                <LinkOutlined />
                已绑定
              </span>
              <Button
                className={styles.changeEmailBtn}
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
              className={styles.changeEmailBtn}
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
