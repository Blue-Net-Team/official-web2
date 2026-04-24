'use client'

import React from 'react'
import { Form, Input, Select, Button, Spin } from 'antd'
import type { MessageInstance } from 'antd/es/message/interface'
import type { FormInstance } from 'antd/es/form'
import { ArrowRightOutlined } from '@ant-design/icons'
import Link from 'next/link'
import { Direction } from '@/apis/schema/type'
import type { CollegeDTO } from '@/apis/schema/type'
import { GENDER_OPTIONS } from './constants'
import AvatarUpload from './AvatarUpload'
import MobileDirectionSelector from './MobileDirectionSelector'
import styles from '@/app/(public)/(other)/enroll/styles.module.css'

const { TextArea } = Input

interface EnrollFormProps {
  form: FormInstance
  selectedDirection: Direction
  handleDirectionSelect: (direction: Direction) => void
  avatarPreview: string
  uploadingAvatar: boolean
  uploadProgress: number
  handleAvatarSelect: (file: File) => void
  introLength: number
  handleIntroChange: (e: React.ChangeEvent<HTMLTextAreaElement>) => void
  colleges: CollegeDTO[]
  loadingColleges: boolean
  submitting: boolean
  handleSubmit: () => Promise<void>
  messageApi: MessageInstance
}

const EnrollForm: React.FC<EnrollFormProps> = ({
  form,
  selectedDirection,
  handleDirectionSelect,
  avatarPreview,
  uploadingAvatar,
  uploadProgress,
  handleAvatarSelect,
  introLength,
  handleIntroChange,
  colleges,
  loadingColleges,
  submitting,
  handleSubmit,
  messageApi,
}) => {
  return (
    <div className="w-full max-w-[600px] bg-[rgba(20,20,30,0.6)] border border-[rgba(102,119,255,0.15)] rounded-3xl p-10 max-sm:p-7 max-sm:rounded-2xl backdrop-blur-[20px] relative overflow-hidden shadow-[0_0_60px_rgba(102,119,255,0.1),inset_0_0_60px_rgba(102,119,255,0.02)] animate-[fadeInUp_0.8s_cubic-bezier(0.4,0,0.2,1)]">
      <div className="absolute top-0 left-0 right-0 h-[3px] bg-gradient-to-r from-[#6677ff] via-[#ff6b35] to-[#2f27b0] shadow-[0_0_20px_#6677ff]" />

      <div className="absolute w-[60px] max-sm:w-10 h-[60px] max-sm:h-10 border-2 border-[rgba(102,119,255,0.3)] top-[15px] left-[15px] border-r-0 border-b-0 rounded-tl-xl" />
      <div className="absolute w-[60px] max-sm:w-10 h-[60px] max-sm:h-10 border-2 border-[rgba(102,119,255,0.3)] top-[15px] right-[15px] border-l-0 border-b-0 rounded-tr-xl" />
      <div className="absolute w-[60px] max-sm:w-10 h-[60px] max-sm:h-10 border-2 border-[rgba(102,119,255,0.3)] bottom-[15px] left-[15px] border-r-0 border-t-0 rounded-bl-xl" />
      <div className="absolute w-[60px] max-sm:w-10 h-[60px] max-sm:h-10 border-2 border-[rgba(102,119,255,0.3)] bottom-[15px] right-[15px] border-l-0 border-t-0 rounded-br-xl" />

      <div className="text-center mb-8 relative">
        <h1 className="text-[36px] max-sm:text-[28px] font-bold text-white mb-3 font-['Orbitron'] tracking-[4px] max-sm:tracking-[2px] bg-gradient-to-br from-white via-[#6677ff] to-[#ff6b35] bg-clip-text text-transparent animate-[titleGlow_3s_ease-in-out_infinite]">
          加入蓝网
        </h1>
        <p className="text-sm text-white/50 leading-relaxed tracking-[1px]">
          填写以下信息完成报名，开启你的科技创新之旅
        </p>
      </div>

      <Form
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
        className="flex flex-col gap-[18px]"
        initialValues={{ direction: selectedDirection }}
      >
        <div className="flex items-start gap-6 max-sm:flex-col max-sm:items-center mb-8 p-6 max-sm:p-5 bg-white/[0.03] rounded-2xl border border-white/[0.05] animate-[slideIn_0.6s_cubic-bezier(0.4,0,0.2,1)_0.1s_both]">
          <AvatarUpload
            previewUrl={avatarPreview}
            uploading={uploadingAvatar}
            uploadProgress={uploadProgress}
            onFileSelect={handleAvatarSelect}
            messageApi={messageApi}
          />
          <div className="flex-1 max-sm:w-full flex flex-col gap-[14px]">
            <div className="flex flex-col gap-[6px]">
              <label className="text-[13px] font-medium text-white/70 flex items-center gap-1">
                姓名 <span className="text-[#ff6b35]">*</span>
              </label>
              <Form.Item
                name="username"
                rules={[{ required: true, message: '请输入姓名' }]}
                className="mb-0"
              >
                <Input placeholder="请输入真实姓名" />
              </Form.Item>
            </div>
            <div className="flex flex-col gap-[6px]">
              <label className="text-[13px] font-medium text-white/70 flex items-center gap-1">
                学号 <span className="text-[#ff6b35]">*</span>
              </label>
              <Form.Item
                name="studentId"
                rules={[
                  { required: true, message: '请输入学号' },
                  {
                    pattern: /^\d{12,13}$/,
                    message: '请输入正确的学号格式（12-13位数字）',
                  },
                ]}
                className="mb-0"
              >
                <Input placeholder="12-13位数字" maxLength={13} />
              </Form.Item>
            </div>
            <div className="flex flex-col gap-[6px]">
              <label className="text-[13px] font-medium text-white/70 flex items-center gap-1">
                性别 <span className="text-[#ff6b35]">*</span>
              </label>
              <Form.Item
                name="gender"
                rules={[{ required: true, message: '请选择性别' }]}
                className="mb-0"
              >
                <Select
                  placeholder="请选择性别"
                  options={GENDER_OPTIONS.map((opt) => ({
                    key: opt.value,
                    value: opt.value,
                    label: opt.label,
                  }))}
                  className="w-full"
                />
              </Form.Item>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-2 max-sm:grid-cols-1 gap-4 animate-[slideIn_0.6s_cubic-bezier(0.4,0,0.2,1)_0.2s_both]">
          <div className="flex flex-col gap-[6px]">
            <label className="text-[13px] font-medium text-white/70 flex items-center gap-1">
              邮箱 <span className="text-[#ff6b35]">*</span>
            </label>
            <Form.Item
              name="email"
              rules={[
                { required: true, message: '请输入邮箱' },
                { type: 'email', message: '请输入正确的邮箱格式' },
              ]}
              className="mb-0"
            >
              <Input placeholder="用于接收通知" />
            </Form.Item>
          </div>
          <div className="flex flex-col gap-[6px]">
            <label className="text-[13px] font-medium text-white/70 flex items-center gap-1">
              学院 <span className="text-[#ff6b35]">*</span>
            </label>
            <Form.Item
              name="collegeId"
              rules={[{ required: true, message: '请选择学院' }]}
              className="mb-0"
            >
              <Select
                placeholder="请选择学院"
                loading={loadingColleges}
                options={colleges.map((college) => ({
                  key: college.id,
                  value: college.id,
                  label: college.name,
                }))}
                className="w-full"
              />
            </Form.Item>
          </div>
        </div>

        <div className="animate-[slideIn_0.6s_cubic-bezier(0.4,0,0.2,1)_0.3s_both]">
          <div className="flex flex-col gap-[6px]">
            <label className="text-[13px] font-medium text-white/70 flex items-center gap-1">
              专业 <span className="text-[#ff6b35]">*</span>
            </label>
            <Form.Item
              name="major"
              rules={[{ required: true, message: '请输入专业' }]}
              className="mb-0"
            >
              <Input placeholder="请输入专业名称" />
            </Form.Item>
          </div>
        </div>

        <MobileDirectionSelector selected={selectedDirection} onSelect={handleDirectionSelect} />

        <div className="flex flex-col gap-[6px] animate-[slideIn_0.6s_cubic-bezier(0.4,0,0.2,1)_0.5s_both]">
          <div className="flex justify-between items-center">
            <label className="text-[13px] font-medium text-white/70 flex items-center gap-1">
              自我介绍 <span className="text-[#ff6b35]">*</span>
            </label>
            <span className={`text-xs ${introLength < 100 ? 'text-[#ff6b35]' : 'text-white/40'}`}>
              {introLength}/500
            </span>
          </div>
          <Form.Item
            name="introduction"
            rules={[
              { required: true, message: '请输入自我介绍' },
              {
                validator: (_, value) => {
                  if (value && value.length < 100) {
                    return Promise.reject('自我介绍至少需要100字')
                  }
                  return Promise.resolve()
                },
              },
            ]}
            className="mb-0"
          >
            <TextArea
              placeholder="请简单介绍你自己，包括你的兴趣爱好、技能特长、为什么想加入蓝网等等..."
              maxLength={500}
              rows={6}
              onChange={handleIntroChange}
            />
          </Form.Item>
          <div className="text-xs text-white/30 mt-1">建议字数：100-500字</div>
        </div>

        <div className="grid grid-cols-1 gap-4 animate-[slideIn_0.6s_cubic-bezier(0.4,0,0.2,1)_0.6s_both]">
          <div className="flex flex-col gap-[6px]">
            <label className="text-[13px] font-medium text-white/70 flex items-center gap-1">
              内推码
              <span className="text-white/40 text-[11px] ml-1">（选填）</span>
            </label>
            <Form.Item name="internalReferralCode" className="mb-0">
              <Input placeholder="如有内推码请填写" maxLength={8} />
            </Form.Item>
          </div>
        </div>

        <div className="mt-2 animate-[slideIn_0.6s_cubic-bezier(0.4,0,0.2,1)_0.6s_both]">
          <Button
            type="primary"
            htmlType="submit"
            className={`${styles.submitBtn} w-full h-[52px] bg-gradient-to-br from-[#6677ff] to-[#2f27b0] border-none rounded-xl text-white text-base font-semibold flex items-center justify-center gap-[10px] transition-all shadow-[0_0_30px_rgba(102,119,255,0.3)] relative overflow-hidden hover:-translate-y-0.5 hover:shadow-[0_5px_30px_rgba(102,119,255,0.5)] disabled:opacity-70 disabled:cursor-not-allowed disabled:transform-none`}
            disabled={submitting || uploadingAvatar}
            icon={submitting ? <Spin size="small" /> : <ArrowRightOutlined />}
          >
            {submitting ? '提交中...' : '提交报名'}
          </Button>
        </div>

        <div className="text-center text-[13px] text-white/40 leading-relaxed animate-[slideIn_0.6s_cubic-bezier(0.4,0,0.2,1)_0.7s_both]">
          提交即表示您同意我们的
          <Link
            href="#"
            className={`${styles.link} text-[#6677ff]! hover:text-[#6677ff]! no-underline relative transition-all`}
          >
            报名须知
          </Link>
          和
          <Link
            href="#"
            className={`${styles.link} text-[#6677ff]! hover:text-[#6677ff]! no-underline relative transition-all`}
          >
            隐私政策
          </Link>
          <br />
          已有账号？
          <Link
            href="/login"
            className={`${styles.link} text-[#6677ff]! hover:text-[#6677ff]! no-underline relative transition-all`}
          >
            立即登录
          </Link>
        </div>
      </Form>
    </div>
  )
}

export default EnrollForm
