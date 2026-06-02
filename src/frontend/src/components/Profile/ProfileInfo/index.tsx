/**
 * 个人信息Tab组件 - 客户端组件
 *
 * 功能：
 * - 展示用户基本信息（姓名、年级、学院、专业等）
 * - 支持编辑模式切换
 * - 表单验证和提交
 * - 根据用户角色控制字段可编辑性（MEMBER 及以上可修改更多字段）
 * - 邮箱设置展示（只读，修改按钮禁用）
 *
 * @author BlueNet Team
 */
'use client'

import { useState } from 'react'
import type { UserInfo } from '@/apis/schema/type'
import type { UpdateProfileRequestDTO } from '@/apis/schema/profile.dto'
import {
  DIRECTION_LABELS,
  GENDER_LABELS,
  ROLE_LABELS,
  getRoleTagColor,
} from '@/apis/schema/enumerate'
import {
  EditOutlined,
  SaveOutlined,
  CloseOutlined,
  UploadOutlined,
  DeleteOutlined,
  QrcodeOutlined,
} from '@ant-design/icons'
import { Form, Input, Button, message, Select, Tag, Upload } from 'antd'
import type { UploadFile } from 'antd/es/upload/interface'
import { userService } from '@/apis/services/user.service'
import { fileService } from '@/apis/services/file.service'
import { useAuth } from '@/hooks'
import Image from 'next/image'
import { API_BASE_URL } from '@/apis/config'
import GitHubBinding from './GitHubBinding'
import ChangeEmailModal from './ChangeEmailModal'
import EmailSettings from './EmailSettings'

interface ProfileInfoProps {
  profile: UserInfo
  onUpdate?: () => void
}

/** 判断是否为 MEMBER 及以上角色 */
function isMemberOrAbove(roleName: string | undefined): boolean {
  if (!roleName) return false
  const memberRoles = ['member', 'direction_admin', 'super_admin']
  return memberRoles.includes(roleName.toLowerCase())
}

export default function ProfileInfo({ profile, onUpdate }: ProfileInfoProps) {
  const [isEditing, setIsEditing] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [changeEmailOpen, setChangeEmailOpen] = useState(false)
  const [form] = Form.useForm()

  // 判断当前用户是否可以修改扩展字段
  const { userInfo: currentUser } = useAuth()
  const canEditExtendedFields = isMemberOrAbove(currentUser?.roleName)

  const [qrcodeFileId, setQrcodeFileId] = useState<number | null>(profile.qrcodeFileId)
  const [qrcodeUploading, setQrcodeUploading] = useState(false)

  const handleEdit = () => {
    form.setFieldsValue({
      username: profile.username,
      nickname: profile.nickname,
      bio: profile.bio,
      gender: profile.gender,
      college: profile.college,
      major: profile.major,
      direction: profile.direction,
      qrcodeFileId: profile.qrcodeFileId,
    })
    setQrcodeFileId(profile.qrcodeFileId)
    setIsEditing(true)
  }

  const handleCancel = () => {
    form.resetFields()
    setIsEditing(false)
  }

  const handleSubmit = async (values: UpdateProfileRequestDTO) => {
    setIsSubmitting(true)
    try {
      const payload: UpdateProfileRequestDTO = {
        ...values,
        qrcodeFileId: qrcodeFileId,
      }
      const res = await userService.updateProfile(payload)
      if (res.code === 200) {
        message.success('保存成功')
        setIsEditing(false)
        onUpdate?.()
      } else {
        message.error(res.msg || '保存失败，请重试')
      }
    } catch {
      message.error('保存失败，请重试')
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleQrcodeUpload = async (file: File) => {
    setQrcodeUploading(true)
    try {
      const res = await fileService.upload(file, 'QRCODE')
      if (res.code === 200 && res.data) {
        setQrcodeFileId(res.data.id)
        message.success('二维码上传成功')
      } else {
        message.error(res.msg || '二维码上传失败')
      }
    } catch {
      message.error('二维码上传失败，请重试')
    } finally {
      setQrcodeUploading(false)
    }
    return false
  }

  const handleQrcodeRemove = () => {
    setQrcodeFileId(null)
  }

  const directionLabel = profile.direction ? DIRECTION_LABELS[profile.direction] : '-'
  const genderLabel = profile.gender ? GENDER_LABELS[profile.gender] : '-'

  return (
    <div className="bg-white/[0.03] backdrop-blur-[20px] border border-white/[0.05] rounded-2xl p-8 max-[640px]:p-5">
      <div className="text-lg font-semibold text-white mb-6 flex items-center gap-[10px] [&>svg]:w-5 [&>svg]:h-5 [&>svg]:text-[#6677ff]">
        <EditOutlined />
        基本信息
      </div>

      {isEditing ? (
        <Form form={form} layout="vertical" onFinish={handleSubmit} className="mt-6">
          <div className="grid grid-cols-2 gap-5 mb-6 max-lg:grid-cols-1">
            {/* 用户名 - MEMBER 及以上可编辑 */}
            <Form.Item
              name={canEditExtendedFields ? 'username' : undefined}
              label={<span className="text-sm font-medium text-white/80">用户名</span>}
            >
              {canEditExtendedFields ? (
                <Input placeholder="请输入用户名" className="!rounded-[10px]" />
              ) : (
                <>
                  <Input value={profile.username} disabled className="!cursor-not-allowed" />
                  <div className="text-xs text-[rgba(140,140,141,0.8)] mt-1">
                    仅成员及以上可修改
                  </div>
                </>
              )}
            </Form.Item>

            {/* 昵称 - 所有人可编辑 */}
            <Form.Item
              name="nickname"
              label={<span className="text-sm font-medium text-white/80">昵称</span>}
            >
              <Input placeholder="请输入昵称" className="!rounded-[10px]" />
            </Form.Item>

            {/* 年级 - 不可编辑 */}
            <Form.Item label={<span className="text-sm font-medium text-white/80">年级</span>}>
              <Input value={profile.grade} disabled className="!cursor-not-allowed" />
            </Form.Item>

            {/* 学院 - MEMBER 及以上可编辑 */}
            <Form.Item
              name={canEditExtendedFields ? 'college' : undefined}
              label={<span className="text-sm font-medium text-white/80">学院</span>}
            >
              {canEditExtendedFields ? (
                <Input placeholder="请输入学院" className="!rounded-[10px]" />
              ) : (
                <Input value={profile.college} disabled className="!cursor-not-allowed" />
              )}
            </Form.Item>

            {/* 专业 - MEMBER 及以上可编辑 */}
            <Form.Item
              name={canEditExtendedFields ? 'major' : undefined}
              label={<span className="text-sm font-medium text-white/80">专业</span>}
            >
              {canEditExtendedFields ? (
                <Input placeholder="请输入专业" className="!rounded-[10px]" />
              ) : (
                <Input value={profile.major} disabled className="!cursor-not-allowed" />
              )}
            </Form.Item>

            {/* 报名方向 - MEMBER 及以上可编辑 */}
            <Form.Item
              name={canEditExtendedFields ? 'direction' : undefined}
              label={<span className="text-sm font-medium text-white/80">报名方向</span>}
            >
              {canEditExtendedFields ? (
                <Select
                  placeholder="请选择方向"
                  className="w-full"
                  options={[
                    { value: 'COMPUTER_VISION', label: '计算机视觉' },
                    { value: 'STRUCTURAL_DESIGN', label: '结构设计' },
                    { value: 'EMBEDDED', label: '嵌入式开发' },
                  ]}
                />
              ) : (
                <Input value={directionLabel} disabled className="!cursor-not-allowed" />
              )}
            </Form.Item>

            {/* 性别 - MEMBER 及以上可编辑 */}
            <Form.Item
              name={canEditExtendedFields ? 'gender' : undefined}
              label={<span className="text-sm font-medium text-white/80">性别</span>}
            >
              {canEditExtendedFields ? (
                <Select
                  placeholder="请选择性别"
                  className="w-full"
                  options={[
                    { value: 'MALE', label: '男' },
                    { value: 'FEMALE', label: '女' },
                    { value: 'UNKNOWN', label: '未知' },
                  ]}
                />
              ) : (
                <Input value={genderLabel} disabled className="!cursor-not-allowed" />
              )}
            </Form.Item>

            {/* 个人简介 - 所有人可编辑 */}
            <Form.Item
              name="bio"
              label={<span className="text-sm font-medium text-white/80">个人简介</span>}
              className="col-span-full"
            >
              <Input.TextArea
                placeholder="介绍一下你自己..."
                rows={4}
                className="!rounded-[10px]"
              />
            </Form.Item>

            {/* 微信二维码 - 所有人可编辑 */}
            <Form.Item
              className="col-span-full"
              label={<span className="text-sm font-medium text-white/80">微信二维码</span>}
            >
              {qrcodeFileId ? (
                <div className="flex items-center gap-4">
                  <div className="w-[120px] h-[120px] rounded-[10px] bg-white/[0.02] p-2 overflow-hidden">
                    <Image
                      src={`${API_BASE_URL}/file/download/${qrcodeFileId}`}
                      alt="微信二维码"
                      width={120}
                      height={120}
                      className="w-full h-full object-contain"
                    />
                  </div>
                  <Button
                    danger
                    icon={<DeleteOutlined />}
                    onClick={handleQrcodeRemove}
                    className="!rounded-[10px]"
                  >
                    删除二维码
                  </Button>
                </div>
              ) : (
                <Upload
                  beforeUpload={handleQrcodeUpload}
                  showUploadList={false}
                  accept="image/jpeg,image/png"
                >
                  <Button
                    icon={<UploadOutlined />}
                    loading={qrcodeUploading}
                    className="!rounded-[10px]"
                  >
                    上传微信二维码
                  </Button>
                </Upload>
              )}
            </Form.Item>
          </div>

          <div className="flex justify-end gap-3 mt-8 pt-6 border-t border-white/[0.05] max-[640px]:flex-col">
            <Button
              className="px-6 py-3 !rounded-[10px] text-sm font-medium cursor-pointer transition-all duration-300 border border-white/10 bg-white/[0.05] text-white/80 hover:bg-white/10 flex items-center gap-2 max-[640px]:w-full max-[640px]:justify-center"
              onClick={handleCancel}
              disabled={isSubmitting}
              icon={<CloseOutlined />}
            >
              取消
            </Button>
            <Button
              type="primary"
              htmlType="submit"
              className="px-6 py-3 !rounded-[10px] text-sm font-medium cursor-pointer transition-all duration-300 !border-none !bg-[linear-gradient(135deg,#6677ff_0%,#2f27b0_100%)] text-white shadow-[0_4px_16px_rgba(102,119,255,0.3)] hover:-translate-y-0.5 hover:shadow-[0_8px_24px_rgba(102,119,255,0.4)] disabled:opacity-60 disabled:cursor-not-allowed disabled:translate-y-0 flex items-center gap-2 max-[640px]:w-full max-[640px]:justify-center"
              loading={isSubmitting}
              icon={<SaveOutlined />}
            >
              保存修改
            </Button>
          </div>
        </Form>
      ) : (
        <>
          <div className="grid grid-cols-2 gap-5 mb-6">
            <div className="flex flex-col gap-1.5">
              <div className="text-xs font-medium text-[rgba(140,140,141,1)] uppercase tracking-[0.5px]">
                用户名
              </div>
              <div className="text-sm text-white">{profile.username}</div>
            </div>
            <div className="flex flex-col gap-1.5">
              <div className="text-xs font-medium text-[rgba(140,140,141,1)] uppercase tracking-[0.5px]">
                昵称
              </div>
              <div className="text-sm text-white">{profile.nickname || '-'}</div>
            </div>
            <div className="flex flex-col gap-1.5">
              <div className="text-xs font-medium text-[rgba(140,140,141,1)] uppercase tracking-[0.5px]">
                年级
              </div>
              <div className="text-sm text-white">{profile.grade}</div>
            </div>
            <div className="flex flex-col gap-1.5">
              <div className="text-xs font-medium text-[rgba(140,140,141,1)] uppercase tracking-[0.5px]">
                学院
              </div>
              <div className="text-sm text-white">{profile.college}</div>
            </div>
            <div className="flex flex-col gap-1.5">
              <div className="text-xs font-medium text-[rgba(140,140,141,1)] uppercase tracking-[0.5px]">
                专业
              </div>
              <div className="text-sm text-white">{profile.major}</div>
            </div>
            <div className="flex flex-col gap-1.5">
              <div className="text-xs font-medium text-[rgba(140,140,141,1)] uppercase tracking-[0.5px]">
                报名方向
              </div>
              <div className="text-sm text-white">{directionLabel}</div>
            </div>
            <div className="flex flex-col gap-1.5">
              <div className="text-xs font-medium text-[rgba(140,140,141,1)] uppercase tracking-[0.5px]">
                性别
              </div>
              <div className="text-sm text-white">{genderLabel}</div>
            </div>
            <div className="flex flex-col gap-1.5">
              <div className="text-xs font-medium text-[rgba(140,140,141,1)] uppercase tracking-[0.5px]">
                角色
              </div>
              <div className="text-sm text-white">
                <Tag color={getRoleTagColor(profile.roleName)}>
                  {ROLE_LABELS[profile.roleName] ?? profile.roleName}
                </Tag>
              </div>
            </div>
            <div className="flex flex-col gap-1.5 col-span-full">
              <div className="text-xs font-medium text-[rgba(140,140,141,1)] uppercase tracking-[0.5px]">
                个人简介
              </div>
              <div className="text-sm text-white">{profile.bio || '-'}</div>
            </div>
            {profile.wechatQrcode && (
              <div className="flex flex-col gap-1.5 col-span-full">
                <div className="text-xs font-medium text-[rgba(140,140,141,1)] uppercase tracking-[0.5px]">
                  微信二维码
                </div>
                <div className="w-[120px] h-[120px] rounded-[10px] bg-white/[0.02] p-2 overflow-hidden">
                  <Image
                    src={profile.wechatQrcode}
                    alt="微信二维码"
                    width={120}
                    height={120}
                    className="w-full h-full object-contain"
                  />
                </div>
              </div>
            )}
          </div>

          <EmailSettings email={profile.email} onChangeEmail={() => setChangeEmailOpen(true)} />

          <GitHubBinding initialGithubUsername={profile.githubUsername} />

          <ChangeEmailModal
            open={changeEmailOpen}
            currentEmail={profile.email}
            onSuccess={() => {
              setChangeEmailOpen(false)
              onUpdate?.()
            }}
            onCancel={() => setChangeEmailOpen(false)}
          />

          <div className="flex justify-end gap-3 mt-8 pt-6 border-t border-white/[0.05]">
            <Button
              type="primary"
              className="px-6 py-3"
              onClick={handleEdit}
              icon={<EditOutlined />}
            >
              编辑信息
            </Button>
          </div>
        </>
      )}
    </div>
  )
}
