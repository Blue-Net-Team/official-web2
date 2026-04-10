'use client'

import { useState, useCallback, useEffect } from 'react'
import { Button, Input, App, ConfigProvider, theme, Steps } from 'antd'
import { EyeInvisibleOutlined, EyeOutlined, ArrowLeftOutlined } from '@ant-design/icons'
import Image from 'next/image'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { AxiosError } from 'axios'
import logo from '@/assets/logo.png'
import loginBg from '@/assets/Login/bg.png'
import { authService } from '@/apis/services/auth.service'
import { ResponseMessage } from '@/apis/schema/type'

/** 从 AxiosError 中提取后端返回的错误消息 */
function getErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof AxiosError) {
    const data = error.response?.data as ResponseMessage<unknown> | undefined
    return data?.msg || fallback
  }
  if (error instanceof Error) {
    return error.message
  }
  return fallback
}

const primaryColor = '#fa8c16'
const primaryColorHover = '#ffa940'
const successColor = '#10b981'

const stepTheme = {
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
    Steps: {
      colorFinish: successColor,
      colorWait: 'rgba(255, 255, 255, 0.2)',
      colorProcess: primaryColor,
      colorText: 'rgba(255, 255, 255, 0.65)',
      colorTextDescription: 'rgba(255, 255, 255, 0.35)',
      fontSize: 12,
    },
  },
}

const STEP_LABELS = ['验证学号', '验证邮箱', '输入验证码', '设置密码']
const STEP_DESCRIPTIONS = [
  '请输入你的学号以验证身份',
  '请输入与该学号关联的邮箱地址',
  '验证码已发送至你的邮箱',
  '请设置你的新密码',
]

export default function ForgotPasswordPage() {
  const [currentStep, setCurrentStep] = useState(0)
  const [resetToken, setResetToken] = useState('')
  const [loading, setLoading] = useState(false)
  const [countdown, setCountdown] = useState(0)
  const [showNewPassword, setShowNewPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const router = useRouter()
  const { message: messageApi } = App.useApp()

  // Form values
  const [studentId, setStudentId] = useState('')
  const [email, setEmail] = useState('')
  const [code, setCode] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')

  // Countdown timer
  useEffect(() => {
    if (countdown <= 0) return
    const timer = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          clearInterval(timer)
          return 0
        }
        return prev - 1
      })
    }, 1000)
    return () => clearInterval(timer)
  }, [countdown])

  // Step 1: Verify student ID
  const handleVerifyStudent = useCallback(async () => {
    if (!studentId.trim()) {
      messageApi.error('请输入学号')
      return
    }
    setLoading(true)
    try {
      const res = await authService.resetPasswordVerifyStudent(studentId.trim())
      if (res.data) {
        setResetToken(res.data)
        setCurrentStep(1)
      }
    } catch (error: unknown) {
      messageApi.error(getErrorMessage(error, '验证失败'))
    } finally {
      setLoading(false)
    }
  }, [studentId, messageApi])

  // Step 2: Verify email
  const handleVerifyEmail = useCallback(async () => {
    if (!email.trim()) {
      messageApi.error('请输入邮箱')
      return
    }
    setLoading(true)
    try {
      const res = await authService.resetPasswordVerifyEmail(resetToken, email.trim())
      if (res.data) {
        setResetToken(res.data)
        setCurrentStep(2)
      }
    } catch (error: unknown) {
      messageApi.error(getErrorMessage(error, '验证失败'))
    } finally {
      setLoading(false)
    }
  }, [email, resetToken, messageApi])

  // Step 3: Send code
  const handleSendCode = useCallback(async () => {
    if (countdown > 0) return
    try {
      await authService.resetPasswordSendCode(resetToken)
      setCountdown(60)
      messageApi.success('验证码已发送')
    } catch (error: unknown) {
      messageApi.error(getErrorMessage(error, '发送失败'))
    }
  }, [resetToken, countdown, messageApi])

  // Step 3: Verify code
  const handleVerifyCode = useCallback(async () => {
    if (!code.trim()) {
      messageApi.error('请输入验证码')
      return
    }
    setLoading(true)
    try {
      await authService.resetPasswordVerifyCode(resetToken, code.trim())
      setCurrentStep(3)
    } catch (error: unknown) {
      messageApi.error(getErrorMessage(error, '验证码错误'))
    } finally {
      setLoading(false)
    }
  }, [code, resetToken, messageApi])

  // Step 4: Reset password
  const handleResetPassword = useCallback(async () => {
    if (!newPassword || !confirmPassword) {
      messageApi.error('请填写所有字段')
      return
    }
    if (newPassword !== confirmPassword) {
      messageApi.error('新密码与确认密码不一致')
      return
    }
    if (newPassword.length < 6) {
      messageApi.error('密码长度不能少于6位')
      return
    }
    setLoading(true)
    try {
      await authService.resetPassword(resetToken, newPassword, confirmPassword)
      messageApi.success('密码重置成功，即将跳转到登录页')
      setTimeout(() => router.push('/login'), 3000)
    } catch (error: unknown) {
      messageApi.error(getErrorMessage(error, '重置失败'))
    } finally {
      setLoading(false)
    }
  }, [resetToken, code, newPassword, confirmPassword, router, messageApi])

  const handleNext = () => {
    if (currentStep === 0) handleVerifyStudent()
    else if (currentStep === 1) handleVerifyEmail()
    else if (currentStep === 2) handleVerifyCode()
    else if (currentStep === 3) handleResetPassword()
  }

  return (
    <ConfigProvider theme={stepTheme}>
      <div
        className="relative w-full h-full flex overflow-hidden bg-[length:100%_120%] bg-[center_bottom]"
        style={{ backgroundImage: `url(${loginBg.src})` as string }}
      >
        {/* Left Panel - Form */}
        <div className="w-1/2 min-w-[500px] h-full flex flex-col justify-center items-center relative z-2 bg-[rgba(20,20,25,0.65)] backdrop-blur-[20px] border-r border-white/[0.08] shadow-[0_8px_32px_rgba(0,0,0,0.3),inset_0_1px_0_rgba(255,255,255,0.05)] max-lg:w-full max-lg:min-w-0 max-lg:p-10 max-lg:border-r-0 max-lg:bg-[rgba(20,20,25,0.75)] max-lg:px-6">
          <div className="w-full max-w-[400px] animate-[fadeIn_0.6s_ease-out]">
            {/* Logo */}
            <div className="flex flex-row items-center gap-3 mb-3">
              <div className="w-12 h-12 rounded-xl flex items-center justify-center overflow-hidden">
                <Image src={logo} alt="蓝网Logo" width={48} height={48} priority />
              </div>
              <h1 className="text-[32px] font-bold text-white tracking-[2px] m-0">蓝网</h1>
            </div>
            <p className="text-sm text-white/50 mb-6">全校规模最大的科创团队</p>

            {/* Title */}
            <div className="mb-5 pt-4">
              <h2 className="text-[28px] font-bold text-white m-0">重置密码</h2>
              <p className="text-sm text-white/60 mt-2">{STEP_DESCRIPTIONS[currentStep]}</p>
            </div>

            {/* Step Indicator */}
            <Steps
              current={currentStep}
              size="small"
              items={STEP_LABELS.map((title) => ({ title }))}
            />

            {/* Step Content */}
            <div className="mt-5 pt-2 min-h-[160px]">
              {currentStep === 0 && (
                <div className="space-y-2">
                  <label className="text-sm text-white/60">学号</label>
                  <Input
                    placeholder="请输入你的学号"
                    value={studentId}
                    onChange={(e) => setStudentId(e.target.value)}
                    maxLength={13}
                    disabled={loading}
                    onPressEnter={handleNext}
                  />
                </div>
              )}

              {currentStep === 1 && (
                <div className="space-y-2">
                  <label className="text-sm text-white/60">邮箱地址</label>
                  <Input
                    placeholder="请输入你的邮箱地址"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    type="email"
                    disabled={loading}
                    onPressEnter={handleNext}
                  />
                </div>
              )}

              {currentStep === 2 && (
                <div className="space-y-2">
                  <label className="text-sm text-white/60">验证码</label>
                  <div className="flex gap-3">
                    <Input
                      placeholder="请输入6位验证码"
                      value={code}
                      onChange={(e) => setCode(e.target.value)}
                      maxLength={6}
                      className="flex-1"
                      disabled={loading}
                      onPressEnter={handleNext}
                    />
                    <Button
                      className="whitespace-nowrap !bg-transparent !border-[#fa8c16] !text-[#fa8c16] transition-all hover:!bg-[rgba(250,140,22,0.15)] hover:!border-[#ffa940] hover:!text-[#ffa940] disabled:!border-white/20 disabled:!text-white/30 disabled:!bg-transparent"
                      onClick={handleSendCode}
                      disabled={countdown > 0 || loading}
                    >
                      {countdown > 0 ? `${countdown}s` : '发送验证码'}
                    </Button>
                  </div>
                </div>
              )}

              {currentStep === 3 && (
                <div className="space-y-4">
                  <div className="space-y-2">
                    <label className="text-sm text-white/60">新密码</label>
                    <Input
                      type={showNewPassword ? 'text' : 'password'}
                      placeholder="请输入新密码（6-32位）"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      maxLength={32}
                      disabled={loading}
                      suffix={
                        <span
                          className="cursor-pointer text-white/40 hover:text-white/60 transition-colors"
                          onClick={() => setShowNewPassword(!showNewPassword)}
                        >
                          {showNewPassword ? <EyeInvisibleOutlined /> : <EyeOutlined />}
                        </span>
                      }
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm text-white/60">确认密码</label>
                    <Input
                      type={showConfirmPassword ? 'text' : 'password'}
                      placeholder="请再次输入新密码"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      maxLength={32}
                      disabled={loading}
                      onPressEnter={handleNext}
                      suffix={
                        <span
                          className="cursor-pointer text-white/40 hover:text-white/60 transition-colors"
                          onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                        >
                          {showConfirmPassword ? <EyeInvisibleOutlined /> : <EyeOutlined />}
                        </span>
                      }
                    />
                  </div>
                </div>
              )}
            </div>

            {/* Spacer */}
            <div className="h-2" />

            {/* Next/Submit Button */}
            <Button
              type="primary"
              className="w-full shadow-[0_4px_20px_rgba(250,140,22,0.35)] transition-all hover:shadow-[0_6px_28px_rgba(250,140,22,0.5)]"
              size="large"
              loading={loading}
              disabled={loading}
              onClick={handleNext}
            >
              {currentStep === 3 ? '重置密码' : '下一步'}
            </Button>

            {/* Back to Login */}
            <div className="flex justify-center items-center gap-1 mt-3 pt-1">
              <ArrowLeftOutlined className="text-white/40 text-xs" />
              <Link
                href="/login"
                className="text-[13px] text-white/40 no-underline transition-all hover:text-white/60"
              >
                返回登录
              </Link>
            </div>
          </div>
        </div>

        {/* Right Panel - Background */}
        <div className="flex-1 relative z-0 max-lg:hidden" />
      </div>
    </ConfigProvider>
  )
}
