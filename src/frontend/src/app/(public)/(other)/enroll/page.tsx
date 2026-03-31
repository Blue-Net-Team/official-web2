'use client'

import React, { useState, useEffect, useCallback, Suspense } from 'react'
import { Form, Input, Select, Button, App, Upload, Spin, ConfigProvider, ThemeConfig } from 'antd'
import type { MessageInstance } from 'antd/es/message/interface'
import { PlusOutlined, ArrowRightOutlined } from '@ant-design/icons'
import Image from 'next/image'
import { useSearchParams } from 'next/navigation'
import styles from './styles.module.css'
import { enrollService } from '@/apis/services/enroll.service'
import { CreateEnrollmentRequestDTO, Direction } from '@/apis/schema/type'
import { fileService } from '@/apis/services/file.service'
import { collegeService } from '@/apis/services/college.service'
import type { CollegeDTO } from '@/apis/schema/type'
import cvIcon from '@/assets/icon/direction/cv_icon.png'
import structIcon from '@/assets/icon/direction/struct_icon.png'
import embedIcon from '@/assets/icon/direction/embed_icon.png'

const { TextArea } = Input
const { Option } = Select

// 方向配置
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

// 学院选项（使用学院ID）
const GRADE_OPTIONS = [
  { value: 1, label: '大一' },
  { value: 2, label: '大二' },
]

// 自定义主题配置
const customTheme: ThemeConfig = {
  token: {
    // 错误状态颜色
    colorError: '#FF6B35',
    colorErrorBorder: '#FF6B35',
    colorErrorOutline: 'rgba(255, 107, 53, 0.3)',
    // 主色调
    colorPrimary: '#6677FF',
    colorPrimaryHover: '#7a89ff',
    colorPrimaryActive: '#5a6ce0',
    // 边框颜色
    colorBorder: 'rgba(255, 255, 255, 0.1)',
    colorBorderSecondary: 'rgba(255, 255, 255, 0.05)',
    // 背景色
    colorBgContainer: 'rgba(255, 255, 255, 0.05)',
    colorBgElevated: '#1a1a2e',
    // 文字颜色
    colorText: '#ffffff',
    colorTextPlaceholder: 'rgba(255, 255, 255, 0.4)',
    colorTextDisabled: 'rgba(255, 255, 255, 0.3)',
    // 圆角
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

// 方向选择器组件（桌面端侧边栏）
interface DirectionSidebarProps {
  selected: Direction
  onSelect: (direction: Direction) => void
}

const DirectionSidebar: React.FC<DirectionSidebarProps> = ({ selected, onSelect }) => {
  return (
    <aside className={styles.directionSidebar}>
      <div className={styles.directionSidebarTitle}>选择方向</div>
      {DIRECTIONS.map((dir) => (
        <div
          key={dir.key}
          className={`${styles.directionSidebarItem} ${
            selected === dir.key ? styles.active : ''
          } ${selected === dir.key ? styles[dir.theme] : ''}`}
          onClick={() => onSelect(dir.key)}
        >
          <div className={`${styles.directionSidebarIcon} ${styles[dir.theme]}`}>
            <Image src={dir.icon} alt={dir.name} width={44} height={44} />
          </div>
          <div className={styles.directionSidebarInfo}>
            <span className={styles.directionSidebarName}>{dir.name}</span>
            <span className={styles.directionSidebarDesc}>{dir.desc}</span>
          </div>
        </div>
      ))}
    </aside>
  )
}

// 移动端方向选择组件
interface MobileDirectionSelectorProps {
  selected: Direction
  onSelect: (direction: Direction) => void
}

const MobileDirectionSelector: React.FC<MobileDirectionSelectorProps> = ({
  selected,
  onSelect,
}) => {
  return (
    <div className={`${styles.formGroup} ${styles.fullWidth} ${styles.mobileDirectionSection}`}>
      <label className={styles.formLabel}>
        报名方向 <span className={styles.required}>*</span>
      </label>
      <div className={styles.directionOptions}>
        {DIRECTIONS.map((dir) => (
          <label key={dir.key} className={styles.directionOption}>
            <input
              type="radio"
              name="direction_mobile"
              value={dir.key}
              checked={selected === dir.key}
              onChange={() => onSelect(dir.key)}
            />
            <div
              className={`${styles.directionCard} ${styles[dir.theme]} ${
                selected === dir.key ? styles.selected : ''
              }`}
            >
              <div className={`${styles.directionIcon} ${styles[dir.theme]}`}>
                <Image src={dir.icon} alt={dir.name} width={48} height={48} />
              </div>
              <span className={styles.directionName}>{dir.name}</span>
            </div>
          </label>
        ))}
      </div>
    </div>
  )
}

// 头像上传组件
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
    <div className={styles.avatarWrapper}>
      <Upload
        accept="image/*"
        showUploadList={false}
        beforeUpload={handleUpload}
        disabled={uploading}
      >
        <div
          className={`${styles.avatarUploadArea} ${
            previewUrl ? styles.hasImage : ''
          } ${uploading ? styles.uploading : ''}`}
        >
          {uploading ? (
            <div className={styles.avatarUploadContent}>
              <div className={styles.progressRing}>
                <svg viewBox="0 0 36 36" className={styles.progressSvg}>
                  <path
                    className={styles.progressBg}
                    d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                  />
                  <path
                    className={styles.progressBar}
                    strokeDasharray={`${uploadProgress || 0}, 100`}
                    d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                  />
                </svg>
                <span className={styles.progressText}>{uploadProgress || 0}%</span>
              </div>
              <span className={styles.avatarUploadText}>上传中...</span>
            </div>
          ) : previewUrl ? (
            <Image
              src={previewUrl}
              alt="avatar"
              width={120}
              height={120}
              className={styles.avatarPreview}
            />
          ) : (
            <div className={styles.avatarUploadContent}>
              <PlusOutlined style={{ fontSize: '28px', color: 'rgba(102, 119, 255, 0.6)' }} />
              <span className={styles.avatarUploadText}>点击上传</span>
            </div>
          )}
        </div>
      </Upload>
      <div className={styles.avatarLabel}>
        头像<span className={styles.required}>*</span>
      </div>
    </div>
  )
}

// 主页面组件
const EnrollPageContent: React.FC = () => {
  const { message: messageApi, modal } = App.useApp()
  const [form] = Form.useForm()
  const searchParams = useSearchParams()
  const [selectedDirection, setSelectedDirection] = useState<Direction>('COMPUTER_VISION')

  // 从URL查询参数读取方向
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

  // 处理头像文件选择和上传
  const handleAvatarSelect = useCallback(
    async (file: File) => {
      // 创建预览URL
      const previewUrl = URL.createObjectURL(file)
      setAvatarPreview(previewUrl)
      // 清除之前上传的avatarId
      setAvatarId(null)
      setUploadProgress(0)

      // 立即上传头像
      setUploadingAvatar(true)
      try {
        const response = await fileService.uploadAvatar(file)
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

  // 处理自我介绍输入
  const handleIntroChange = useCallback((e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const value = e.target.value
    setIntroLength(value.length)
  }, [])

  // 提交报名
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
          grade: values.grade,
          direction: selectedDirection,
          introduction: values.introduction,
          internalReferralCode: values.internalReferralCode,
          forceUpdate,
        }

        const response = forceUpdate
          ? await enrollService.updateEnrollment(data)
          : await enrollService.submitEnrollment(data)

        if (response.code === 201) {
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
          const err = error as { response?: { data?: { code?: number } } }
          if (err.response?.data?.code === 409) {
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

  // 表单提交
  const handleSubmit = useCallback(async () => {
    if (!avatarId) {
      messageApi.error('请上传头像')
      return
    }

    await submitEnrollment(false)
  }, [avatarId, submitEnrollment, messageApi])

  return (
    <div className={styles.pageContainer}>
      <ConfigProvider theme={customTheme}>
        {/* 背景效果 */}
        <div className={styles.pageBg} />

        {/* 主内容 */}
        <main className={styles.mainContent}>
          {/* 桌面端方向选择侧边栏 */}
          <DirectionSidebar selected={selectedDirection} onSelect={handleDirectionSelect} />

          {/* 报名卡片 */}
          <div className={styles.enrollContainer}>
            {/* 角落装饰 */}
            <div className={`${styles.cornerDecoration} ${styles.topLeft}`} />
            <div className={`${styles.cornerDecoration} ${styles.topRight}`} />
            <div className={`${styles.cornerDecoration} ${styles.bottomLeft}`} />
            <div className={`${styles.cornerDecoration} ${styles.bottomRight}`} />

            {/* 页面标题 */}
            <div className={styles.enrollHeader}>
              <h1 className={styles.enrollTitle}>加入蓝网</h1>
              <p className={styles.enrollSubtitle}>填写以下信息完成报名，开启你的科技创新之旅</p>
            </div>

            {/* 报名表单 */}
            <Form
              form={form}
              layout="vertical"
              onFinish={handleSubmit}
              className={styles.enrollForm}
              initialValues={{ direction: selectedDirection }}
            >
              {/* 头像和基本信息区域 */}
              <div className={styles.profileSection}>
                <AvatarUpload
                  previewUrl={avatarPreview}
                  uploading={uploadingAvatar}
                  uploadProgress={uploadProgress}
                  onFileSelect={handleAvatarSelect}
                  messageApi={messageApi}
                />
                <div className={styles.basicInfo}>
                  <div className={styles.infoGroup}>
                    <label className={styles.infoLabel}>
                      姓名 <span className={styles.required}>*</span>
                    </label>
                    <Form.Item name="username" rules={[{ required: true, message: '请输入姓名' }]}>
                      <Input placeholder="请输入真实姓名" className={styles.infoInput} />
                    </Form.Item>
                  </div>
                  <div className={styles.infoGroup}>
                    <label className={styles.infoLabel}>
                      学号 <span className={styles.required}>*</span>
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
                    >
                      <Input
                        placeholder="12-13位数字"
                        maxLength={13}
                        className={styles.infoInput}
                      />
                    </Form.Item>
                  </div>
                </div>
              </div>
              {/* 第一行：邮箱 + 学院 */}
              <div className={styles.formRow}>
                <div className={styles.formGroup}>
                  <label className={styles.formLabel}>
                    邮箱 <span className={styles.required}>*</span>
                  </label>
                  <Form.Item
                    name="email"
                    rules={[
                      { required: true, message: '请输入邮箱' },
                      { type: 'email', message: '请输入正确的邮箱格式' },
                    ]}
                  >
                    <Input placeholder="用于接收通知" />
                  </Form.Item>
                </div>
                <div className={styles.formGroup}>
                  <label className={styles.formLabel}>
                    学院 <span className={styles.required}>*</span>
                  </label>
                  <Form.Item name="collegeId" rules={[{ required: true, message: '请选择学院' }]}>
                    <Select
                      placeholder="请选择学院"
                      loading={loadingColleges}
                      style={{
                        width: '100%',
                      }}
                    >
                      {colleges.map((college) => (
                        <Option key={college.id} value={college.id}>
                          {college.name}
                        </Option>
                      ))}
                    </Select>
                  </Form.Item>
                </div>
              </div>

              {/* 第二行：专业 + 年级 */}
              <div className={styles.formRow}>
                <div className={styles.formGroup}>
                  <label className={styles.formLabel}>
                    专业 <span className={styles.required}>*</span>
                  </label>
                  <Form.Item name="major" rules={[{ required: true, message: '请输入专业' }]}>
                    <Input placeholder="请输入专业名称" />
                  </Form.Item>
                </div>
                <div className={styles.formGroup}>
                  <label className={styles.formLabel}>
                    年级 <span className={styles.required}>*</span>
                  </label>
                  <Form.Item name="grade" rules={[{ required: true, message: '请选择年级' }]}>
                    <Select
                      placeholder="请选择年级"
                      style={{
                        width: '100%',
                      }}
                    >
                      {GRADE_OPTIONS.map((opt) => (
                        <Option key={opt.value} value={opt.value}>
                          {opt.label}
                        </Option>
                      ))}
                    </Select>
                  </Form.Item>
                </div>
              </div>

              {/* 移动端方向选择 */}
              <MobileDirectionSelector
                selected={selectedDirection}
                onSelect={handleDirectionSelect}
              />

              {/* 自我介绍 */}
              <div className={`${styles.formGroup} ${styles.fullWidth} ${styles.introSection}`}>
                <div className={styles.introHeader}>
                  <label className={styles.formLabel}>
                    自我介绍 <span className={styles.required}>*</span>
                  </label>
                  <span
                    className={`${styles.charCount} ${introLength < 100 ? styles.warning : ''}`}
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
                >
                  <TextArea
                    placeholder="请简单介绍你自己，包括你的兴趣爱好、技能特长、为什么想加入蓝网等等..."
                    maxLength={500}
                    rows={6}
                    onChange={handleIntroChange}
                  />
                </Form.Item>
                <div className={styles.introHint}>建议字数：100-500字</div>
              </div>

              {/* 内推码 */}
              <div className={styles.formRow}>
                <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                  <label className={styles.formLabel}>
                    内推码
                    <span
                      style={{
                        color: 'rgba(255,255,255,0.4)',
                        fontSize: '11px',
                        marginLeft: '4px',
                      }}
                    >
                      （选填）
                    </span>
                  </label>
                  <Form.Item name="internalReferralCode">
                    <Input placeholder="如有内推码请填写" maxLength={8} />
                  </Form.Item>
                </div>
              </div>

              {/* 提交按钮 */}
              <div className={styles.submitSection}>
                <Button
                  type="primary"
                  htmlType="submit"
                  className={styles.submitBtn}
                  disabled={submitting || uploadingAvatar}
                  icon={submitting ? <Spin size="small" /> : <ArrowRightOutlined />}
                >
                  {submitting ? '提交中...' : '提交报名'}
                </Button>
              </div>

              {/* 提示信息 */}
              <div className={styles.formTips}>
                提交即表示您同意我们的<a href="#">报名须知</a>和<a href="#">隐私政策</a>
                <br />
                已有账号？<a href="/login">立即登录</a>
              </div>
            </Form>
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
        <div
          style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            minHeight: '100vh',
          }}
        >
          <Spin size="large" />
        </div>
      }
    >
      <EnrollPageContent />
    </Suspense>
  )
}
