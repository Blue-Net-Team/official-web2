'use client'

import { useState, useCallback } from 'react'
import { Button, Input, App, ConfigProvider, theme, Steps } from 'antd'
import {
  EyeInvisibleOutlined,
  EyeOutlined,
  LockOutlined,
  InfoCircleOutlined,
  SafetyCertificateOutlined,
} from '@ant-design/icons'
import { useRouter } from 'next/navigation'
import { AxiosError } from 'axios'
import { userService } from '@/apis/services/user.service'
import { hashPassword } from '@/utils/passwordHash'
import authStore from '@/stores/authStore'
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
      controlHeight: 44,
      fontSize: 14,
      activeBorderColor: primaryColor,
      hoverBorderColor: primaryColorHover,
      activeShadow: `0 0 0 3px ${primaryColor}26`,
      colorBgContainerDisabled: 'rgba(255, 255, 255, 0.04)',
    },
    Button: {
      controlHeight: 44,
      borderRadius: 8,
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

const STEP_LABELS = ['验证原密码', '设置新密码']

export default function ChangePasswordPage() {
  const [currentStep, setCurrentStep] = useState(0)
  const [token, setToken] = useState('')
  const [loading, setLoading] = useState(false)
  const [showCurrentPassword, setShowCurrentPassword] = useState(false)
  const [showNewPassword, setShowNewPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const router = useRouter()
  const { message: messageApi } = App.useApp()

  // Form values
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')

  // Step 1: Verify current password
  const handleVerifyPassword = useCallback(async () => {
    if (!currentPassword) {
      messageApi.error('请输入当前密码')
      return
    }
    setLoading(true)
    try {
      const hashedPassword = await hashPassword(currentPassword)
      const res = await userService.verifyPassword(hashedPassword)
      if (res.data) {
        setToken(res.data)
        setCurrentStep(1)
      }
    } catch (error: unknown) {
      messageApi.error(getErrorMessage(error, '验证失败'))
    } finally {
      setLoading(false)
    }
  }, [currentPassword, messageApi])

  // Step 2: Change password
  const handleChangePassword = useCallback(async () => {
    if (!newPassword || !confirmPassword) {
      messageApi.error('请填写所有字段')
      return
    }
    if (newPassword.length < 6) {
      messageApi.error('密码长度不能少于6位')
      return
    }
    if (newPassword !== confirmPassword) {
      messageApi.error('两次输入的密码不一致')
      return
    }
    setLoading(true)
    try {
      const hashedNew = await hashPassword(newPassword)
      const hashedConfirm = await hashPassword(confirmPassword)
      await userService.changePassword(token, hashedNew, hashedConfirm)
      // 后端已吊销所有 JWT Token，清除前端认证状态
      await authStore.getState().logout()
      messageApi.success('密码修改成功，即将跳转到登录页')
      setTimeout(() => router.push('/login'), 2000)
    } catch (error: unknown) {
      const msg = getErrorMessage(error, '修改失败')
      messageApi.error(msg)
      // Token expired - go back to step 1
      if (msg.includes('过期') || msg.includes('重新开始')) {
        setCurrentStep(0)
        setToken('')
      }
    } finally {
      setLoading(false)
    }
  }, [token, newPassword, confirmPassword, router, messageApi])

  const handleNext = () => {
    if (currentStep === 0) handleVerifyPassword()
    else if (currentStep === 1) handleChangePassword()
  }

  return (
    <ConfigProvider theme={stepTheme}>
      <div className="min-h-screen bg-[#0a0a0a] flex flex-col">
        {/* Content */}
        <div
          className="flex-1 flex items-center justify-center px-10 py-12"
          style={{
            background:
              'radial-gradient(ellipse 120% 80% at 30% 40%, rgba(102, 119, 255, 0.08), transparent)',
          }}
        >
          <div
            className="w-full max-w-[480px] rounded-xl p-10 pb-8"
            style={{
              background: '#19191c',
              border: '1px solid rgba(255, 255, 255, 0.12)',
              boxShadow: '0 4px 24px rgba(0, 0, 0, 0.3)',
            }}
          >
            {/* Title */}
            <h2 className="text-2xl font-semibold text-white m-0">修改密码</h2>
            <p className="text-sm mt-2 mb-0" style={{ color: 'rgba(255,255,255,0.4)' }}>
              {currentStep === 0 ? '请验证当前密码以继续' : '请设置您的新密码'}
            </p>

            {/* Step Indicator */}
            <div className="mt-6">
              <Steps
                current={currentStep}
                size="small"
                items={STEP_LABELS.map((title) => ({ title }))}
              />
            </div>

            {/* Step Content */}
            <div className="mt-6">
              {currentStep === 0 && (
                <div className="space-y-5">
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-white">当前密码</label>
                    <Input
                      type={showCurrentPassword ? 'text' : 'password'}
                      placeholder="请输入当前密码"
                      prefix={<LockOutlined style={{ color: 'rgba(255,255,255,0.4)' }} />}
                      value={currentPassword}
                      onChange={(e) => setCurrentPassword(e.target.value)}
                      disabled={loading}
                      onPressEnter={handleNext}
                      suffix={
                        <span
                          className="cursor-pointer text-white/40 hover:text-white/60 transition-colors"
                          onClick={() => setShowCurrentPassword(!showCurrentPassword)}
                        >
                          {showCurrentPassword ? <EyeInvisibleOutlined /> : <EyeOutlined />}
                        </span>
                      }
                    />
                  </div>

                  <Button
                    type="primary"
                    className="w-full"
                    loading={loading}
                    onClick={handleVerifyPassword}
                  >
                    下一步
                  </Button>
                </div>
              )}

              {currentStep === 1 && (
                <div className="space-y-5">
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-white">新密码</label>
                    <Input
                      type={showNewPassword ? 'text' : 'password'}
                      placeholder="请输入新密码（6位+）"
                      prefix={<LockOutlined style={{ color: 'rgba(255,255,255,0.4)' }} />}
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
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
                    <label className="text-sm font-medium text-white">确认新密码</label>
                    <Input
                      type={showConfirmPassword ? 'text' : 'password'}
                      placeholder="请再次输入新密码"
                      prefix={<LockOutlined style={{ color: 'rgba(255,255,255,0.4)' }} />}
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
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

                  <Button
                    type="primary"
                    className="w-full"
                    loading={loading}
                    onClick={handleChangePassword}
                  >
                    确认修改
                  </Button>
                </div>
              )}
            </div>

            {/* Footer hint */}
            <div
              className="flex items-center justify-center gap-1.5 mt-4 pt-4"
              style={{ borderTop: '1px solid rgba(255,255,255,0.08)' }}
            >
              {currentStep === 0 ? (
                <>
                  <InfoCircleOutlined style={{ color: 'rgba(255,255,255,0.4)', fontSize: 13 }} />
                  <span className="text-xs" style={{ color: 'rgba(255,255,255,0.4)' }}>
                    忘记密码？请退出登录后在登录页点击「忘记密码」
                  </span>
                </>
              ) : (
                <>
                  <SafetyCertificateOutlined
                    style={{ color: 'rgba(255,255,255,0.4)', fontSize: 13 }}
                  />
                  <span className="text-xs" style={{ color: 'rgba(255,255,255,0.4)' }}>
                    密码修改成功后将自动退出，需重新登录
                  </span>
                </>
              )}
            </div>
          </div>
        </div>
      </div>
    </ConfigProvider>
  )
}
