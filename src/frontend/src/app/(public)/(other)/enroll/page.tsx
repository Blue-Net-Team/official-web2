'use client'

import React, { useState, useEffect, useCallback, Suspense } from 'react'
import { Form, Input, Select, Button, App, Upload, Spin, ConfigProvider, ThemeConfig } from 'antd'
import type { MessageInstance } from 'antd/es/message/interface'
import { PlusOutlined, ArrowRightOutlined } from '@ant-design/icons'
import Image from 'next/image'
import Link from 'next/link'
import { useSearchParams } from 'next/navigation'
import styles from './styles.module.css'
import { enrollService } from '@/apis/services/enroll.service'
import { CreateEnrollmentRequestDTO, Direction, Gender } from '@/apis/schema/type'
import { fileService } from '@/apis/services/file.service'
import { collegeService } from '@/apis/services/college.service'
import type { CollegeDTO } from '@/apis/schema/type'
import cvIcon from '@/assets/icon/direction/cv_icon.png'
import structIcon from '@/assets/icon/direction/struct_icon.png'
import embedIcon from '@/assets/icon/direction/embed_icon.png'
import ConsultationQrcode from '@/components/Enroll/ConsultationQrcode'

const { TextArea } = Input

const DIRECTIONS = [
  {
    key: 'COMPUTER_VISION' as Direction,
    name: '计算机视觉',
    desc: 'AI图像识别',
    icon: cvIcon,
    theme: 'computerVision',
  },
  {
    key: 'STRUCTURAL_DESIGN' as Direction,
    name: '结构设计',
    desc: '机械结构设计',
    icon: structIcon,
    theme: 'structuralDesign',
  },
  {
    key: 'EMBEDDED' as Direction,
    name: '嵌入式开发',
    desc: '硬件软件开发',
    icon: embedIcon,
    theme: 'embedded',
  },
]

const GENDER_OPTIONS = [
  { value: 'MALE' as Gender, label: '男' },
  { value: 'FEMALE' as Gender, label: '女' },
]

const customTheme: ThemeConfig = {
  token: {
    colorError: '#FF6B35',
    colorErrorBorder: '#FF6B35',
    colorErrorOutline: 'rgba(255, 107, 53, 0.3)',
    colorPrimary: '#6677FF',
    colorPrimaryHover: '#7a89ff',
    colorPrimaryActive: '#5a6ce0',
    colorBorder: 'rgba(255, 255, 255, 0.1)',
    colorBorderSecondary: 'rgba(255, 255, 255, 0.05)',
    colorBgContainer: 'rgba(255, 255, 255, 0.05)',
    colorBgElevated: '#1a1a2e',
    colorText: '#ffffff',
    colorTextPlaceholder: 'rgba(255, 255, 255, 0.4)',
    colorTextDisabled: 'rgba(255, 255, 255, 0.3)',
    borderRadius: 10,
  },
  components: {
    Input: {
      colorBgContainer: 'rgba(255, 255, 255, 0.05)',
      colorBorder: 'rgba(255, 255, 255, 0.1)',
      colorInfoBorderHover: '#6677FF',
      colorError: '#FF6B35',
      colorErrorBorder: '#FF6B35',
      colorText: '#ffffff',
      colorTextPlaceholder: 'rgba(255, 255, 255, 0.4)',
      activeShadow: '0 0 0 2px rgba(102, 119, 255, 0.2)',
    },
    Select: {
      colorBgContainer: 'rgba(255, 255, 255, 0.05)',
      colorBorder: 'rgba(255, 255, 255, 0.1)',
      colorInfoBorderHover: '#6677FF',
      colorError: '#FF6B35',
      colorErrorBorder: '#FF6B35',
      colorText: '#ffffff',
      colorTextPlaceholder: 'rgba(255, 255, 255, 0.4)',
      colorBgElevated: '#1a1a2e',
      optionSelectedBg: 'rgba(102, 119, 255, 0.2)',
      optionActiveBg: 'rgba(102, 119, 255, 0.1)',
    },
    Button: {
      colorPrimary: '#6677FF',
      colorPrimaryHover: '#7a89ff',
      colorPrimaryActive: '#5a6ce0',
      primaryShadow: '0 0 20px rgba(102, 119, 255, 0.4)',
      defaultBg: 'rgba(255, 255, 255, 0.05)',
      colorBorder: 'rgba(255, 255, 255, 0.1)',
      defaultColor: '#ffffff',
    },
    Upload: {
      colorBgContainer: 'rgba(255, 255, 255, 0.05)',
      colorBorder: 'rgba(102, 119, 255, 0.4)',
      colorInfoBorderHover: '#6677FF',
    },
  },
}

interface DirectionSidebarProps {
  selected: Direction
  onSelect: (direction: Direction) => void
}

const DirectionSidebar: React.FC<DirectionSidebarProps> = ({ selected, onSelect }) => {
  return (
    <aside className="flex flex-col gap-4 mt-0 animate-[fadeInLeft_0.8s_cubic-bezier(0.4,0,0.2,1)_0.2s_both]">
      <div className="text-sm font-semibold text-white/50 mb-2 pl-2 uppercase tracking-[2px] font-['Orbitron']">
        选择方向
      </div>
      {DIRECTIONS.map((dir) => (
        <div
          key={dir.key}
          className={`${styles.directionItem} flex items-center gap-[14px] p-[18px] bg-[rgba(20,20,30,0.6)] border border-white/[0.08] rounded-2xl cursor-pointer transition-all duration-[400ms] cubic-bezier(0.4,0,0.2,1) relative overflow-hidden hover:translate-x-2 hover:border-[rgba(102,119,255,0.3)] hover:shadow-[0_0_30px_rgba(102,119,255,0.2)] ${
            selected === dir.key
              ? dir.theme === 'computerVision'
                ? 'border-[#6677ff] bg-gradient-to-br from-[rgba(102,119,255,0.15)] to-[rgba(47,39,176,0.1)] shadow-[0_0_30px_rgba(102,119,255,0.3),inset_0_0_20px_rgba(102,119,255,0.1)]'
                : dir.theme === 'structuralDesign'
                  ? 'border-[#ff6b35] bg-gradient-to-br from-[rgba(255,107,53,0.15)] to-[rgba(255,140,66,0.1)] shadow-[0_0_30px_rgba(255,107,53,0.3),inset_0_0_20px_rgba(255,107,53,0.1)]'
                  : 'border-[#2ecc71] bg-gradient-to-br from-[rgba(46,204,113,0.15)] to-[rgba(39,174,96,0.1)] shadow-[0_0_30px_rgba(46,204,113,0.3),inset_0_0_20px_rgba(46,204,113,0.1)]'
              : ''
          }`}
          onClick={() => onSelect(dir.key)}
        >
          <div
            className={`w-11 h-11 rounded-xl flex items-center justify-center shrink-0 transition-transform overflow-hidden hover:scale-110 hover:rotate-[5deg] ${
              dir.theme === 'computerVision'
                ? 'bg-gradient-to-br from-[rgba(102,119,255,0.3)] to-[rgba(47,39,176,0.3)] shadow-[0_0_20px_rgba(102,119,255,0.3)]'
                : dir.theme === 'structuralDesign'
                  ? 'bg-gradient-to-br from-[rgba(255,107,53,0.3)] to-[rgba(255,140,66,0.3)] shadow-[0_0_20px_rgba(255,107,53,0.3)]'
                  : 'bg-gradient-to-br from-[rgba(46,204,113,0.3)] to-[rgba(39,174,96,0.3)] shadow-[0_0_20px_rgba(46,204,113,0.3)]'
            }`}
          >
            <Image src={dir.icon} alt={dir.name} width={44} height={44} />
          </div>
          <div className="flex flex-col gap-1">
            <span className="text-[15px] font-semibold text-white/95">{dir.name}</span>
            <span className="text-xs text-white/50">{dir.desc}</span>
          </div>
        </div>
      ))}
    </aside>
  )
}

interface MobileDirectionSelectorProps {
  selected: Direction
  onSelect: (direction: Direction) => void
}

const MobileDirectionSelector: React.FC<MobileDirectionSelectorProps> = ({
  selected,
  onSelect,
}) => {
  return (
    <div className="hidden max-lg:flex flex-col gap-[6px] animate-[slideIn_0.6s_cubic-bezier(0.4,0,0.2,1)_0.4s_both]">
      <label className="text-[13px] font-medium text-white/70 flex items-center gap-1">
        报名方向 <span className="text-[#ff6b35]">*</span>
      </label>
      <div className="grid grid-cols-3 max-sm:grid-cols-1 gap-3">
        {DIRECTIONS.map((dir) => (
          <label key={dir.key} className="cursor-pointer">
            <input
              type="radio"
              name="direction_mobile"
              value={dir.key}
              checked={selected === dir.key}
              onChange={() => onSelect(dir.key)}
              className="hidden"
            />
            <div
              className={`flex flex-col max-sm:flex-row items-center max-sm:justify-start gap-2 max-sm:gap-[14px] p-4 max-sm:p-[14px_16px] bg-white/[0.03] border rounded-xl transition-all ${
                selected === dir.key
                  ? dir.theme === 'computerVision'
                    ? 'border-[#6677ff] bg-gradient-to-br from-[rgba(102,119,255,0.15)] to-[rgba(47,39,176,0.1)] shadow-[0_0_20px_rgba(102,119,255,0.2)]'
                    : dir.theme === 'structuralDesign'
                      ? 'border-[#ff6b35] bg-gradient-to-br from-[rgba(255,107,53,0.15)] to-[rgba(255,140,66,0.1)] shadow-[0_0_20px_rgba(255,107,53,0.2)]'
                      : 'border-[#2ecc71] bg-gradient-to-br from-[rgba(46,204,113,0.15)] to-[rgba(39,174,96,0.1)] shadow-[0_0_20px_rgba(46,204,113,0.2)]'
                  : 'border-white/[0.08] hover:border-[rgba(102,119,255,0.3)] hover:bg-[rgba(102,119,255,0.05)]'
              }`}
            >
              <div
                className={`w-12 max-sm:w-11 h-12 max-sm:h-11 rounded-xl flex items-center justify-center overflow-hidden ${
                  dir.theme === 'computerVision'
                    ? 'bg-gradient-to-br from-[rgba(102,119,255,0.3)] to-[rgba(47,39,176,0.3)] shadow-[0_0_15px_rgba(102,119,255,0.3)]'
                    : dir.theme === 'structuralDesign'
                      ? 'bg-gradient-to-br from-[rgba(255,107,53,0.3)] to-[rgba(255,140,66,0.3)] shadow-[0_0_15px_rgba(255,107,53,0.3)]'
                      : 'bg-gradient-to-br from-[rgba(46,204,113,0.3)] to-[rgba(39,174,96,0.3)] shadow-[0_0_15px_rgba(46,204,113,0.3)]'
                }`}
              >
                <Image src={dir.icon} alt={dir.name} width={48} height={48} />
              </div>
              <span className="text-[13px] font-medium text-white/90">{dir.name}</span>
            </div>
          </label>
        ))}
      </div>
    </div>
  )
}

interface AvatarUploadProps {
  previewUrl?: string
  uploading?: boolean
  uploadProgress?: number
  onFileSelect?: (file: File) => void
  messageApi: MessageInstance
}

const AvatarUpload: React.FC<AvatarUploadProps> = ({
  previewUrl,
  uploading,
  uploadProgress,
  onFileSelect,
  messageApi,
}) => {
  const handleUpload = useCallback(
    (file: File) => {
      const isImage = file.type.startsWith('image/')
      if (!isImage) {
        messageApi.error('请选择图片文件')
        return false
      }

      const isLt2M = file.size / 1024 / 1024 < 2
      if (!isLt2M) {
        messageApi.error('图片大小不能超过2MB')
        return false
      }

      onFileSelect?.(file)
      return false
    },
    [onFileSelect, messageApi]
  )

  return (
    <div className="flex flex-col items-center gap-[10px] shrink-0">
      <Upload
        accept="image/*"
        showUploadList={false}
        beforeUpload={handleUpload}
        disabled={uploading}
      >
        <div
          className={`w-[100px] max-sm:w-[110px] h-[100px] max-sm:h-[110px] border-2 border-dashed border-[rgba(102,119,255,0.4)] rounded-full flex flex-col items-center justify-center cursor-pointer transition-all bg-[rgba(102,119,255,0.05)] relative overflow-hidden hover:border-[#6677ff] hover:shadow-[0_0_20px_rgba(102,119,255,0.4)] ${
            previewUrl ? 'border-solid border-transparent' : ''
          } ${uploading ? 'border-[#6677ff] bg-[rgba(102,119,255,0.1)] cursor-not-allowed' : ''}`}
        >
          {uploading ? (
            <div className="flex flex-col items-center justify-center gap-1 z-1">
              <div className="relative w-10 h-10 flex items-center justify-center">
                <svg viewBox="0 0 36 36" className="w-full h-full -rotate-90">
                  <path
                    className="fill-none stroke-[rgba(102,119,255,0.2)]"
                    strokeWidth="3"
                    d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                  />
                  <path
                    className="fill-none stroke-[#6677ff]"
                    strokeWidth="3"
                    strokeLinecap="round"
                    strokeDasharray={`${uploadProgress || 0}, 100`}
                    d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                  />
                </svg>
                <span className="absolute text-[10px] text-[#6677ff] font-semibold">
                  {uploadProgress || 0}%
                </span>
              </div>
              <span className="text-[11px] text-white/40">上传中...</span>
            </div>
          ) : previewUrl ? (
            <Image
              src={previewUrl}
              alt="avatar"
              width={120}
              height={120}
              className="absolute inset-[2px] w-[calc(100%-4px)] h-[calc(100%-4px)] object-cover rounded-full"
            />
          ) : (
            <div className="flex flex-col items-center justify-center gap-1 z-1">
              <PlusOutlined style={{ fontSize: '28px', color: 'rgba(102, 119, 255, 0.6)' }} />
              <span className="text-[11px] text-white/40">点击上传</span>
            </div>
          )}
        </div>
      </Upload>
      <div className="text-xs text-white/50 font-medium">
        头像<span className="text-[#ff6b35] ml-[2px]">*</span>
      </div>
    </div>
  )
}

const EnrollPageContent: React.FC = () => {
  const { message: messageApi, modal } = App.useApp()
  const [form] = Form.useForm()
  const searchParams = useSearchParams()
  const [selectedDirection, setSelectedDirection] = useState<Direction>('COMPUTER_VISION')

  useEffect(() => {
    const directionFromUrl = searchParams.get('direction') as Direction
    if (directionFromUrl && DIRECTIONS.some((d) => d.key === directionFromUrl)) {
      setSelectedDirection(directionFromUrl)
      form.setFieldsValue({ direction: directionFromUrl })
    }
  }, [searchParams, form])
  const [avatarPreview, setAvatarPreview] = useState<string>('')
  const [avatarId, setAvatarId] = useState<number | null>(null)
  const [introLength, setIntroLength] = useState(0)
  const [submitting, setSubmitting] = useState(false)
  const [uploadingAvatar, setUploadingAvatar] = useState(false)
  const [uploadProgress, setUploadProgress] = useState(0)
  const [colleges, setColleges] = useState<CollegeDTO[]>([])
  const [loadingColleges, setLoadingColleges] = useState(true)

  useEffect(() => {
    const fetchColleges = async () => {
      try {
        const response = await collegeService.getColleges()
        if (response.code === 200 && response.data) {
          setColleges(response.data)
        }
      } catch {
        messageApi.error('获取学院列表失败')
      } finally {
        setLoadingColleges(false)
      }
    }
    fetchColleges()
  }, [messageApi])

  const handleDirectionSelect = useCallback(
    (direction: Direction) => {
      setSelectedDirection(direction)
      form.setFieldsValue({ direction })
    },
    [form]
  )

  const handleAvatarSelect = useCallback(
    async (file: File) => {
      const previewUrl = URL.createObjectURL(file)
      setAvatarPreview(previewUrl)
      setAvatarId(null)
      setUploadProgress(0)

      setUploadingAvatar(true)
      try {
        const response = await fileService.upload(file, 'AVATAR')
        if (response.code === 200 && response.data) {
          setAvatarId(response.data.id)
          setUploadProgress(100)
          messageApi.success('头像上传成功')
        } else {
          messageApi.error(response.msg || '头像上传失败')
          setAvatarPreview('')
        }
      } catch {
        messageApi.error('头像上传失败，请稍后重试')
        setAvatarPreview('')
      } finally {
        setUploadingAvatar(false)
      }
    },
    [messageApi]
  )

  const handleIntroChange = useCallback((e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const value = e.target.value
    setIntroLength(value.length)
  }, [])

  const submitEnrollment = useCallback(
    async (forceUpdate = false) => {
      const values = form.getFieldsValue()

      if (introLength < 100) {
        messageApi.error('自我介绍至少需要100字')
        return
      }

      setSubmitting(true)

      try {
        const data: CreateEnrollmentRequestDTO = {
          avatarId: avatarId!,
          username: values.username,
          studentId: values.studentId,
          email: values.email,
          collegeId: values.collegeId,
          major: values.major,
          gender: values.gender,
          direction: selectedDirection,
          introduction: values.introduction,
          internalReferralCode: values.internalReferralCode,
          forceUpdate,
        }

        const response = forceUpdate
          ? await enrollService.updateEnrollment(data)
          : await enrollService.submitEnrollment(data)

        if (response.code === 201 || response.code === 200) {
          messageApi.success(forceUpdate ? '报名信息更新成功！' : '报名成功！')
          form.resetFields()
          setAvatarPreview('')
          setAvatarId(null)
          setIntroLength(0)
          setSelectedDirection('COMPUTER_VISION')
        } else {
          messageApi.error(response.msg || '报名失败，请稍后重试')
        }
      } catch (error: unknown) {
        if (error && typeof error === 'object' && 'response' in error) {
          const err = error as {
            response?: {
              data?: {
                code?: number
                msg?: string
                data?: { status?: string }
              }
            }
          }
          if (err.response?.data?.code === 409) {
            if (err.response.data.data?.status !== 'PENDING') {
              messageApi.error(
                err.response.data.data?.status
                  ? '该报名已审核，无法更新报名信息'
                  : err.response.data.msg || '该报名已审核，无法更新报名信息'
              )
              return
            }
            modal.confirm({
              title: '该学号已报名',
              content: '是否更新报名信息？',
              okText: '更新',
              cancelText: '取消',
              onOk: () => submitEnrollment(true),
            })
            return
          }
        }
        messageApi.error('网络错误，请稍后重试')
      } finally {
        setSubmitting(false)
      }
    },
    [avatarId, introLength, selectedDirection, form, messageApi, modal]
  )

  const handleSubmit = useCallback(async () => {
    if (!avatarId) {
      messageApi.error('请上传头像')
      return
    }

    await submitEnrollment(false)
  }, [avatarId, submitEnrollment, messageApi])

  return (
    <div className="w-full min-h-screen bg-[#0a0a0a] text-white relative overflow-x-hidden">
      <ConfigProvider theme={customTheme}>
        <div className="fixed top-0 left-0 w-full h-full bg-[radial-gradient(ellipse_80%_50%_at_20%_40%,rgba(102,119,255,0.15)_0%,transparent_50%),radial-gradient(ellipse_60%_40%_at_80%_60%,rgba(255,107,53,0.1)_0%,transparent_50%),radial-gradient(ellipse_50%_30%_at_50%_100%,rgba(47,39,176,0.2)_0%,transparent_50%)] z-0 pointer-events-none" />

        <main className="w-full min-h-screen flex justify-center items-start pt-[100px] max-lg:pt-[100px] px-10 max-lg:px-5 pb-[60px] gap-[30px] max-lg:flex-col max-lg:items-center relative z-1">
          <div className="flex flex-col gap-6 shrink-0 w-[220px] max-lg:hidden animate-[fadeInLeft_0.8s_cubic-bezier(0.4,0,0.2,1)_0.2s_both]">
            <DirectionSidebar selected={selectedDirection} onSelect={handleDirectionSelect} />
            <ConsultationQrcode />
          </div>

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
                        style={{ width: '100%' }}
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
                      style={{ width: '100%' }}
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

              <MobileDirectionSelector
                selected={selectedDirection}
                onSelect={handleDirectionSelect}
              />

              <div className="flex flex-col gap-[6px] animate-[slideIn_0.6s_cubic-bezier(0.4,0,0.2,1)_0.5s_both]">
                <div className="flex justify-between items-center">
                  <label className="text-[13px] font-medium text-white/70 flex items-center gap-1">
                    自我介绍 <span className="text-[#ff6b35]">*</span>
                  </label>
                  <span
                    className={`text-xs ${introLength < 100 ? 'text-[#ff6b35]' : 'text-white/40'}`}
                  >
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

          <div className="hidden max-lg:block w-full max-w-[600px]">
            <ConsultationQrcode popoverPlacement="top" />
          </div>
        </main>
      </ConfigProvider>
    </div>
  )
}

export default function EnrollPage() {
  return (
    <Suspense
      fallback={
        <div className="flex justify-center items-center min-h-screen">
          <Spin size="large" />
        </div>
      }
    >
      <EnrollPageContent />
    </Suspense>
  )
}
