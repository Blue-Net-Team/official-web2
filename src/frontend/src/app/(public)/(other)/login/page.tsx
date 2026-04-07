'use client'

import { Suspense, useState, useEffect } from 'react'
import { Button, Input, Tabs, Form, App, ConfigProvider, theme } from 'antd'
import { GithubOutlined } from '@ant-design/icons'
import Image from 'next/image'
import Link from 'next/link'
import { useRouter, useSearchParams } from 'next/navigation'
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
  return (
    <Suspense>
      <LoginPageContent />
    </Suspense>
  )
}

function LoginPageContent() {
  const [activeTab, setActiveTab] = useState<string>('student')
  const [countdown, setCountdown] = useState<number>(0)
  const [form] = Form.useForm()
  const router = useRouter()
  const searchParams = useSearchParams()
  const { message: messageApi } = App.useApp()
  const { isLoading, login, loginWithEmail, sendVerificationCode, checkAuthStatus } = authStore()

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

  const [sendingCode, setSendingCode] = useState(false)

  const handleSendCode = async () => {
    if (countdown > 0 || sendingCode) return

    const email = form.getFieldValue('email')
    if (!email) {
      messageApi.error('请先输入邮箱')
      return
    }

    try {
      setSendingCode(true)
      await sendVerificationCode(email)
      setCountdown(60)
      messageApi.success('验证码已发送')
    } catch (error: unknown) {
      const errorMsg = error instanceof Error ? error.message : '发送失败，请稍后重试'
      messageApi.error(errorMsg)
    } finally {
      setSendingCode(false)
    }

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
      if (!values.email || !values.verifyCode) {
        messageApi.error('请输入邮箱和验证码')
        return
      }

      try {
        await loginWithEmail(values.email, values.verifyCode)
        messageApi.success('登录成功')
        router.push('/')
      } catch (error) {
        const errormessage = error instanceof Error ? error.message : '登录失败，请稍后重试'
        if (errormessage.includes('验证码')) {
          messageApi.error('验证码错误或已过期，请重新获取')
        } else {
          messageApi.error(errormessage)
        }
      }
    }
  }

  const tabItems = [
    {
      key: 'student',
      label: '学号登录',
      children: (
        <Form form={form} onFinish={handleSubmit} className="w-full" autoComplete="off">
          <Form.Item name="studentId" rules={[{ required: true, message: '请输入学号' }]}>
            <Input placeholder="请输入学号" maxLength={13} disabled={isLoading} />
          </Form.Item>

          <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password placeholder="请输入密码" disabled={isLoading} />
          </Form.Item>

          <div className="flex justify-end mb-6">
            <Link
              href="/forgot-password"
              className="text-[13px] text-[#fa8c16] no-underline transition-all hover:text-[#ffc57e] hover:opacity-90"
            >
              忘记密码？
            </Link>
          </div>

          <Button
            type="primary"
            htmlType="submit"
            className="w-full shadow-[0_4px_20px_rgba(250,140,22,0.35)] transition-all hover:shadow-[0_6px_28px_rgba(250,140,22,0.5)]"
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
        <Form form={form} onFinish={handleSubmit} className="w-full" autoComplete="off">
          <Form.Item
            name="email"
            rules={[
              { required: true, message: '请输入邮箱' },
              { type: 'email', message: '请输入有效的邮箱地址' },
            ]}
          >
            <Input placeholder="请输入邮箱" disabled={isLoading} />
          </Form.Item>

          <Form.Item name="verifyCode" rules={[{ required: true, message: '请输入验证码' }]}>
            <div className="flex gap-3">
              <Input
                placeholder="请输入验证码"
                maxLength={6}
                className="flex-1"
                disabled={isLoading}
              />
              <Button
                className="whitespace-nowrap !bg-transparent !border-[#fa8c16] !text-[#fa8c16] transition-all hover:!bg-[rgba(250,140,22,0.15)] hover:!border-[#ffa940] hover:!text-[#ffa940] hover:shadow-[0_0_12px_rgba(250,140,22,0.3)] disabled:!border-white/20 disabled:!text-white/30 disabled:!bg-transparent"
                onClick={handleSendCode}
                disabled={countdown > 0 || isLoading}
              >
                {countdown > 0 ? `${countdown}s` : '获取验证码'}
              </Button>
            </div>
          </Form.Item>

          <div className="flex justify-end mb-6">
            <Link
              href="/forgot-password"
              className="text-[13px] text-[#fa8c16] no-underline transition-all hover:text-[#ffc57e] hover:opacity-90"
            >
              忘记密码？
            </Link>
          </div>

          <Button
            type="primary"
            htmlType="submit"
            className="w-full shadow-[0_4px_20px_rgba(250,140,22,0.35)] transition-all hover:shadow-[0_6px_28px_rgba(250,140,22,0.5)]"
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
        className="relative w-full h-full flex overflow-hidden bg-[length:100%_120%] bg-[center_bottom]"
        style={{ backgroundImage: `url(${loginBg.src})` } as React.CSSProperties}
      >
        <div className="w-1/2 min-w-[500px] h-full flex flex-col justify-center items-center relative z-2 bg-[rgba(20,20,25,0.65)] backdrop-blur-[20px] border-r border-white/[0.08] shadow-[0_8px_32px_rgba(0,0,0,0.3),inset_0_1px_0_rgba(255,255,255,0.05)] max-lg:w-full max-lg:min-w-0 max-lg:p-10 max-lg:border-r-0 max-lg:bg-[rgba(20,20,25,0.75)] max-lg:px-6">
          <div className="w-full max-w-[400px] animate-[fadeIn_0.6s_ease-out]">
            <div className="flex flex-col items-center mb-12">
              <div className="flex flex-row items-center gap-3 mb-3">
                <div className="w-12 h-12 rounded-xl flex items-center justify-center overflow-hidden">
                  <Image src={logo} alt="蓝网Logo" width={48} height={48} priority />
                </div>
                <h1 className="text-[32px] font-bold text-white tracking-[2px] m-0">蓝网</h1>
              </div>
              <p className="text-sm text-white/50 m-0">全校规模最大的科创团队</p>
            </div>

            <Tabs activeKey={activeTab} onChange={setActiveTab} items={tabItems} />

            <div className="flex items-center my-6">
              <div className="flex-1 h-px bg-white/15" />
              <span className="px-4 text-[13px] text-white/40">其他登录方式</span>
              <div className="flex-1 h-px bg-white/15" />
            </div>

            <Button
              className="w-full !bg-white/[0.08] !border-white/15 !text-white transition-all hover:!bg-white/[0.12] hover:!border-white/25 hover:shadow-[0_0_16px_rgba(255,255,255,0.1)]"
              size="large"
              icon={<GithubOutlined />}
              onClick={handleGithubLogin}
              disabled={isLoading}
            >
              使用 GitHub 登录
            </Button>

            <div className="text-center mt-8 text-xs text-white/35 leading-relaxed">
              提示：只有通过报名审核后，才能获得登录账号
            </div>
          </div>
        </div>

        <div className="flex-1 relative z-0 max-lg:hidden" />
      </div>
    </ConfigProvider>
  )
}
