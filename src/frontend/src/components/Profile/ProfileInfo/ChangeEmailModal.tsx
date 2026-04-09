'use client'

import { useState, useEffect, useCallback, useRef } from 'react'
import { Modal, Steps, Input, Button, App } from 'antd'
import { MailOutlined, SendOutlined, CheckCircleOutlined } from '@ant-design/icons'
import { userService } from '@/apis/services/user.service'

interface ChangeEmailModalProps {
  open: boolean
  currentEmail: string
  onSuccess: () => void
  onCancel: () => void
}

type StepStatus = 'wait' | 'process' | 'finish' | 'error'

export default function ChangeEmailModal({
  open,
  currentEmail,
  onSuccess,
  onCancel,
}: ChangeEmailModalProps) {
  const [current, setCurrent] = useState(0)
  const [originalCode, setOriginalCode] = useState('')
  const [newEmail, setNewEmail] = useState('')
  const [newCode, setNewCode] = useState('')
  const [originalCountdown, setOriginalCountdown] = useState(0)
  const [newCountdown, setNewCountdown] = useState(0)
  const [submitting, setSubmitting] = useState(false)
  const [sendingOriginal, setSendingOriginal] = useState(false)
  const [sendingNew, setSendingNew] = useState(false)
  const originalTimerRef = useRef<ReturnType<typeof setInterval> | null>(null)
  const newTimerRef = useRef<ReturnType<typeof setInterval> | null>(null)
  const { message: messageApi } = App.useApp()

  useEffect(() => {
    const origTimer = originalTimerRef.current
    const newTimer = newTimerRef.current
    return () => {
      if (origTimer) clearInterval(origTimer)
      if (newTimer) clearInterval(newTimer)
    }
  }, [])

  const startCountdown = useCallback((type: 'original' | 'new') => {
    const setCountdown = type === 'original' ? setOriginalCountdown : setNewCountdown
    const timerRef = type === 'original' ? originalTimerRef : newTimerRef
    setCountdown(60)
    if (timerRef.current) clearInterval(timerRef.current)
    timerRef.current = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          if (timerRef.current) clearInterval(timerRef.current)
          return 0
        }
        return prev - 1
      })
    }, 1000)
  }, [])

  const handleSendOriginalCode = useCallback(async () => {
    setSendingOriginal(true)
    try {
      const res = await userService.sendEmailVerificationCode(currentEmail, 'change-email-original')
      if (res.code === 200) {
        messageApi.success('验证码已发送至原邮箱')
        startCountdown('original')
      } else {
        messageApi.error(res.msg || '发送验证码失败')
      }
    } catch {
      messageApi.error('发送验证码失败，请重试')
    } finally {
      setSendingOriginal(false)
    }
  }, [currentEmail, messageApi, startCountdown])

  const handleSendNewCode = useCallback(async () => {
    if (!newEmail) {
      messageApi.warning('请输入新邮箱地址')
      return
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(newEmail)) {
      messageApi.warning('请输入有效的邮箱地址')
      return
    }
    if (newEmail === currentEmail) {
      messageApi.warning('新邮箱不能与当前邮箱相同')
      return
    }
    setSendingNew(true)
    try {
      const res = await userService.sendEmailVerificationCode(newEmail, 'change-email-new')
      if (res.code === 200) {
        messageApi.success('验证码已发送至新邮箱')
        startCountdown('new')
      } else {
        messageApi.error(res.msg || '发送验证码失败')
      }
    } catch {
      messageApi.error('发送验证码失败，请重试')
    } finally {
      setSendingNew(false)
    }
  }, [newEmail, currentEmail, messageApi, startCountdown])

  const handleNextToStep2 = useCallback(() => {
    if (!originalCode.trim()) {
      messageApi.warning('请输入原邮箱验证码')
      return
    }
    setCurrent(1)
  }, [originalCode, messageApi])

  const handleNextToStep3 = useCallback(() => {
    if (!newEmail.trim()) {
      messageApi.warning('请输入新邮箱地址')
      return
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(newEmail)) {
      messageApi.warning('请输入有效的邮箱地址')
      return
    }
    if (newEmail === currentEmail) {
      messageApi.warning('新邮箱不能与当前邮箱相同')
      return
    }
    setCurrent(2)
  }, [newEmail, currentEmail, messageApi])

  const handleReset = useCallback(() => {
    setCurrent(0)
    setOriginalCode('')
    setNewEmail('')
    setNewCode('')
    setOriginalCountdown(0)
    setNewCountdown(0)
    if (originalTimerRef.current) clearInterval(originalTimerRef.current)
    if (newTimerRef.current) clearInterval(newTimerRef.current)
  }, [])

  const handleSubmit = useCallback(async () => {
    if (!newCode.trim()) {
      messageApi.warning('请输入新邮箱验证码')
      return
    }
    setSubmitting(true)
    try {
      const res = await userService.changeEmail({
        originalEmailVerifyCode: originalCode.trim(),
        newEmail: newEmail.trim(),
        newEmailVerifyCode: newCode.trim(),
      })
      if (res.code === 200) {
        messageApi.success('邮箱修改成功')
        onSuccess()
        handleReset()
      } else {
        messageApi.error(res.msg || '修改邮箱失败')
      }
    } catch {
      messageApi.error('修改邮箱失败，请重试')
    } finally {
      setSubmitting(false)
    }
  }, [originalCode, newEmail, newCode, messageApi, onSuccess, handleReset])

  const handleCancel = useCallback(() => {
    handleReset()
    onCancel()
  }, [handleReset, onCancel])

  const stepsStatus: StepStatus = 'process'

  const stepItems = [
    {
      title: '验证原邮箱',
      icon: <MailOutlined />,
      status:
        current === 0
          ? stepsStatus
          : current > 0
            ? ('finish' as StepStatus)
            : ('wait' as StepStatus),
    },
    {
      title: '填写新邮箱',
      icon: <MailOutlined />,
      status:
        current === 1
          ? stepsStatus
          : current > 1
            ? ('finish' as StepStatus)
            : ('wait' as StepStatus),
    },
    {
      title: '验证新邮箱',
      icon: <CheckCircleOutlined />,
      status: current === 2 ? stepsStatus : ('wait' as StepStatus),
    },
  ]

  const renderStepContent = () => {
    switch (current) {
      case 0:
        return (
          <div className="space-y-5">
            <div className="p-4 bg-white/[0.02] rounded-[10px] border border-white/[0.05]">
              <div className="text-xs text-white/50 mb-2">当前邮箱</div>
              <div className="text-sm text-white font-medium">{currentEmail}</div>
            </div>
            <div>
              <div className="text-sm text-white/80 mb-3">向当前邮箱发送验证码以确认身份</div>
              <div className="flex gap-3">
                <Input
                  placeholder="请输入原邮箱验证码"
                  value={originalCode}
                  onChange={(e) => setOriginalCode(e.target.value)}
                  className="flex-1 !rounded-[10px]"
                  maxLength={6}
                />
                <Button
                  onClick={handleSendOriginalCode}
                  loading={sendingOriginal}
                  disabled={originalCountdown > 0}
                  className="!rounded-[10px] shrink-0"
                  icon={<SendOutlined />}
                >
                  {originalCountdown > 0 ? `${originalCountdown}s` : '发送验证码'}
                </Button>
              </div>
            </div>
          </div>
        )
      case 1:
        return (
          <div className="space-y-5">
            <div>
              <div className="text-sm text-white/80 mb-3">请输入新的邮箱地址</div>
              <Input
                placeholder="请输入新邮箱地址"
                value={newEmail}
                onChange={(e) => setNewEmail(e.target.value)}
                className="!rounded-[10px]"
                type="email"
              />
            </div>
          </div>
        )
      case 2:
        return (
          <div className="space-y-5">
            <div className="p-4 bg-white/[0.02] rounded-[10px] border border-white/[0.05]">
              <div className="text-xs text-white/50 mb-2">新邮箱地址</div>
              <div className="text-sm text-white font-medium">{newEmail}</div>
            </div>
            <div>
              <div className="text-sm text-white/80 mb-3">向新邮箱发送验证码以完成绑定</div>
              <div className="flex gap-3">
                <Input
                  placeholder="请输入新邮箱验证码"
                  value={newCode}
                  onChange={(e) => setNewCode(e.target.value)}
                  className="flex-1 !rounded-[10px]"
                  maxLength={6}
                />
                <Button
                  onClick={handleSendNewCode}
                  loading={sendingNew}
                  disabled={newCountdown > 0}
                  className="!rounded-[10px] shrink-0"
                  icon={<SendOutlined />}
                >
                  {newCountdown > 0 ? `${newCountdown}s` : '发送验证码'}
                </Button>
              </div>
            </div>
          </div>
        )
      default:
        return null
    }
  }

  const footerButtons = () => {
    switch (current) {
      case 0:
        return (
          <div className="flex justify-end gap-3">
            <Button onClick={handleCancel} className="!rounded-[10px]">
              取消
            </Button>
            <Button
              type="primary"
              onClick={handleNextToStep2}
              disabled={!originalCode.trim()}
              className="!rounded-[10px]"
            >
              下一步
            </Button>
          </div>
        )
      case 1:
        return (
          <div className="flex justify-end gap-3">
            <Button onClick={() => setCurrent(0)} className="!rounded-[10px]">
              上一步
            </Button>
            <Button
              type="primary"
              onClick={handleNextToStep3}
              disabled={!newEmail.trim()}
              className="!rounded-[10px]"
            >
              下一步
            </Button>
          </div>
        )
      case 2:
        return (
          <div className="flex justify-end gap-3">
            <Button onClick={() => setCurrent(1)} className="!rounded-[10px]">
              上一步
            </Button>
            <Button
              type="primary"
              onClick={handleSubmit}
              loading={submitting}
              disabled={!newCode.trim()}
              className="!rounded-[10px]"
            >
              确认修改
            </Button>
          </div>
        )
      default:
        return null
    }
  }

  return (
    <Modal
      title="修改邮箱"
      open={open}
      onCancel={handleCancel}
      footer={footerButtons()}
      width={520}
      centered
      destroyOnHidden
    >
      <div className="mb-8">
        <Steps current={current} items={stepItems} size="small" />
      </div>
      <div className="min-h-[180px]">{renderStepContent()}</div>
    </Modal>
  )
}
