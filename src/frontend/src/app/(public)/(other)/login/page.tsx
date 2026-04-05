'use client'

import { useState, useEffect } from 'react'
import { Button, Input, Tabs, Form, App, ConfigProvider, theme } from 'antd'
import { GithubOutlined } from '@ant-design/icons'
import Image from 'next/image'
import Link from 'next/link'
import { useRouter, useSearchParams } from 'next/navigation'
import styles from './styles.module.css'
import logo from '@/assets/logo.png'
import loginBg from '@/assets/Login/bg.png'
import authStore from '@/stores/authStore'
import { authService } from '@/apis/services/auth.service'

const primaryColor = '#fa8c16'
const primaryColorHover = '#ffa940'

const loginTheme = {
  algorithm: theme.darkAlgorithm,
  token: {
    colorPrimary: primaryColor,
    borderRadius: 12,
  },
  components: {
    Input: {
      colorBgContainer: 'rgba(255, 255, 255, 0.08)',
      colorBorder: 'rgba(255, 255, 255, 0.12)',
      colorText: '#ffffff',
      colorTextPlaceholder: 'rgba(255, 255, 255, 0.35)',
      controlHeight: 52,
      fontSize: 15,
      activeBorderColor: primaryColor,
      hoverBorderColor: primaryColorHover,
      activeShadow: `0 0 0 3px ${primaryColor}26`,
      colorBgContainerDisabled: 'rgba(255, 255, 255, 0.04)',
    },
    Button: {
      controlHeight: 52,
      borderRadius: 12,
      colorPrimary: primaryColor,
      colorPrimaryHover: primaryColorHover,
      primaryShadow: `0 4px 20px ${primaryColor}40`,
    },
    Tabs: {
      colorText: 'rgba(255, 255, 255, 0.5)',
      colorTextHeading: '#ffffff',
      inkBarColor: primaryColor,
      itemActiveColor: primaryColor,
      itemHoverColor: primaryColorHover,
      itemSelectedColor: primaryColor,
    },
    Form: {
      marginLG: 20,
    },
  },
}

interface LoginFormValues {
  studentId?: string
  password?: string
  email?: string
  verifyCode?: string
}

export default function LoginPage() {
  const [activeTab, setActiveTab] = useState<string>('student')
  const [countdown, setCountdown] = useState<number>(0)
  const [form] = Form.useForm()
  const router = useRouter()
  const searchParams = useSearchParams()
  const { message: messageApi } = App.useApp()
  const { isLoading, login, checkAuthStatus } = authStore()

  // Handle GitHub OAuth callback params
  useEffect(() => {
    const githubStatus = searchParams.get('github')
    if (!githubStatus) return
    if (githubStatus === 'success') {
      checkAuthStatus().then(() => {
        messageApi.success('GitHub 登录成功')
        router.push('/')
      })
    } else if (githubStatus === 'unbound') {
      messageApi.warning('请先使用学号登录，然后在个人设置中绑定 GitHub 账号')
    } else if (githubStatus === 'error') {
      messageApi.error('GitHub 登录失败，请稍后重试')
    }
  }, [searchParams])

  const handleGithubLogin = async () => {
    try {
      const res = await authService.getGithubAuthorizeUrl()
      if (res.data) {
        window.location.href = res.data
      }
    } catch {
      messageApi.error('获取 GitHub 授权链接失败')
    }
  }

  const handleSendCode = () => {
    if (countdown > 0) return

    const email = form.getFieldValue('email')
    if (!email) {
      messageApi.error('请先输入邮箱')
      return
    }

    setCountdown(60)
    messageApi.success('验证码已发送')

    const timer = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          clearInterval(timer)
          return 0
        }
        return prev - 1
      })
    }, 1000)
  }

  const handleSubmit = async (values: LoginFormValues) => {
    if (activeTab === 'student') {
      if (!values.studentId || !values.password) {
        messageApi.error('请输入学号和密码')
        return
      }

      try {
        await login({
          studentId: values.studentId,
          password: values.password,
        })
        messageApi.success('登录成功')
        router.push('/')
      } catch (error) {
        const errormessage = error instanceof Error ? error.message : '登录失败，请稍后重试'
        if (errormessage.includes('408') || errormessage.includes('超时')) {
          messageApi.error('网络超时，请稍后重试')
        } else if (errormessage.includes('5') || errormessage.includes('服务器')) {
          messageApi.error('服务器错误，请稍后重试')
        } else {
          messageApi.error(errormessage)
        }
      }
    } else {
      messageApi.info('邮箱登录功能暂未开放')
    }
  }

  const tabItems = [
    {
      key: 'student',
      label: '学号登录',
      children: (
        <Form form={form} onFinish={handleSubmit} className={styles.form} autoComplete="off">
          <Form.Item name="studentId" rules={[{ required: true, message: '请输入学号' }]}>
            <Input
              placeholder="请输入学号"
              maxLength={13}
              className={styles.input}
              disabled={isLoading}
            />
          </Form.Item>

          <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password
              placeholder="请输入密码"
              className={styles.input}
              disabled={isLoading}
            />
          </Form.Item>

          <div className={styles.formOptions}>
            <Link href="/forgot-password" className={styles.forgotPassword}>
              忘记密码？
            </Link>
          </div>

          <Button
            type="primary"
            htmlType="submit"
            className={styles.loginBtn}
            size="large"
            loading={isLoading}
            disabled={isLoading}
          >
            登录
          </Button>
        </Form>
      ),
    },
    {
      key: 'email',
      label: '邮箱登录',
      children: (
        <Form form={form} onFinish={handleSubmit} className={styles.form} autoComplete="off">
          <Form.Item
            name="email"
            rules={[
              { required: true, message: '请输入邮箱' },
              { type: 'email', message: '请输入有效的邮箱地址' },
            ]}
          >
            <Input placeholder="请输入邮箱" className={styles.input} disabled={isLoading} />
          </Form.Item>

          <Form.Item name="verifyCode" rules={[{ required: true, message: '请输入验证码' }]}>
            <div className={styles.verifyCodeGroup}>
              <Input
                placeholder="请输入验证码"
                maxLength={6}
                className={styles.verifyCodeInput}
                disabled={isLoading}
              />
              <Button
                className={styles.sendCodeBtn}
                onClick={handleSendCode}
                disabled={countdown > 0 || isLoading}
              >
                {countdown > 0 ? `${countdown}s` : '获取验证码'}
              </Button>
            </div>
          </Form.Item>

          <div className={styles.formOptions}>
            <Link href="/forgot-password" className={styles.forgotPassword}>
              忘记密码？
            </Link>
          </div>

          <Button
            type="primary"
            htmlType="submit"
            className={styles.loginBtn}
            size="large"
            loading={isLoading}
            disabled={isLoading}
          >
            登录
          </Button>
        </Form>
      ),
    },
  ]

  return (
    <ConfigProvider theme={loginTheme}>
      <div
        className={styles.container}
        style={{ '--login-bg': `url(${loginBg.src})` } as React.CSSProperties}
      >
        <div className={styles.formSection}>
          <div className={styles.content}>
            <div className={styles.logoSection}>
              <div className={styles.logoBrand}>
                <div className={styles.logo}>
                  <Image src={logo} alt="蓝网Logo" width={48} height={48} priority />
                </div>
                <h1 className={styles.logoTitle}>蓝网</h1>
              </div>
              <p className={styles.logoSubtitle}>全校规模最大的科创团队</p>
            </div>

            <Tabs activeKey={activeTab} onChange={setActiveTab} items={tabItems} />

            <div className={styles.divider}>
              <div className={styles.dividerLine} />
              <span className={styles.dividerText}>其他登录方式</span>
              <div className={styles.dividerLine} />
            </div>

            <Button
              className={styles.githubLogin}
              size="large"
              icon={<GithubOutlined />}
              onClick={handleGithubLogin}
              disabled={isLoading}
            >
              使用 GitHub 登录
            </Button>

            <div className={styles.loginTips}>提示：只有通过报名审核后，才能获得登录账号</div>
          </div>
        </div>

        <div className={styles.rightSection} />
      </div>
    </ConfigProvider>
  )
}
