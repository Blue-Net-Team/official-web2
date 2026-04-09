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
import type { UserInfo, UpdateProfileRequest } from '@/types/profile'
import { DIRECTION_LABELS, GENDER_LABELS } from '@/apis/schema/enumerate'
import { EditOutlined, SaveOutlined, CloseOutlined } from '@ant-design/icons'
import { Form, Input, Button, message, Select } from 'antd'
import { userService } from '@/apis/services/user.service'
import authStore from '@/stores/authStore'
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
  const currentUser = authStore((state) => state.userInfo)
  const canEditExtendedFields = isMemberOrAbove(currentUser?.roleName)

  const handleEdit = () => {
    form.setFieldsValue({
      username: profile.username,
      nickname: profile.nickname,
      bio: profile.bio,
      gender: profile.gender,
      college: profile.college,
      major: profile.major,
      direction: profile.direction,
    })
    setIsEditing(true)
  }

  const handleCancel = () => {
    form.resetFields()
    setIsEditing(false)
  }

  const handleSubmit = async (values: UpdateProfileRequest) => {
    setIsSubmitting(true)
    try {
      const res = await userService.updateProfile(values)
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

  const directionLabel = DIRECTION_LABELS[profile.direction] || profile.direction
  const genderLabel = GENDER_LABELS[profile.gender] || profile.gender

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
                    { value: 'computer_vision', label: '计算机视觉' },
                    { value: 'structural_design', label: '结构设计' },
                    { value: 'embedded', label: '嵌入式开发' },
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
                    { value: 'male', label: '男' },
                    { value: 'female', label: '女' },
                    { value: 'unknown', label: '未知' },
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
                {profile.roleName === 'candidate' ? '考生' : '成员'}
              </div>
            </div>
            <div className="flex flex-col gap-1.5 col-span-full">
              <div className="text-xs font-medium text-[rgba(140,140,141,1)] uppercase tracking-[0.5px]">
                个人简介
              </div>
              <div className="text-sm text-white">{profile.bio || '-'}</div>
            </div>
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
