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
import { DirectionLabels, GenderLabels } from '@/types/profile'
import styles from './styles.module.css'
import {
  EditOutlined,
  MailOutlined,
  CheckCircleOutlined,
  SaveOutlined,
  CloseOutlined,
} from '@ant-design/icons'
import { Form, Input, Button, message, Select } from 'antd'
import { userService } from '@/apis/services/user.service'
import authStore from '@/stores/authStore'

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

  const directionLabel = DirectionLabels[profile.direction] || profile.direction
  const genderLabel = GenderLabels[profile.gender] || profile.gender

  return (
    <div className={styles.profileForm}>
      <div className={styles.formSectionTitle}>
        <EditOutlined />
        基本信息
      </div>

      {isEditing ? (
        <Form form={form} layout="vertical" onFinish={handleSubmit} className={styles.editForm}>
          <div className={styles.formGrid}>
            {/* 用户名 - MEMBER 及以上可编辑 */}
            <Form.Item
              name={canEditExtendedFields ? 'username' : undefined}
              label={<span className={styles.formLabel}>用户名</span>}
            >
              {canEditExtendedFields ? (
                <Input placeholder="请输入用户名" className={styles.formInput} />
              ) : (
                <>
                  <Input value={profile.username} disabled className={styles.formInputDisabled} />
                  <div className={styles.formHint}>仅成员及以上可修改</div>
                </>
              )}
            </Form.Item>

            {/* 昵称 - 所有人可编辑 */}
            <Form.Item name="nickname" label={<span className={styles.formLabel}>昵称</span>}>
              <Input placeholder="请输入昵称" className={styles.formInput} />
            </Form.Item>

            {/* 年级 - 不可编辑 */}
            <Form.Item label={<span className={styles.formLabel}>年级</span>}>
              <Input value={profile.grade} disabled className={styles.formInputDisabled} />
            </Form.Item>

            {/* 学院 - MEMBER 及以上可编辑 */}
            <Form.Item
              name={canEditExtendedFields ? 'college' : undefined}
              label={<span className={styles.formLabel}>学院</span>}
            >
              {canEditExtendedFields ? (
                <Input placeholder="请输入学院" className={styles.formInput} />
              ) : (
                <Input value={profile.college} disabled className={styles.formInputDisabled} />
              )}
            </Form.Item>

            {/* 专业 - MEMBER 及以上可编辑 */}
            <Form.Item
              name={canEditExtendedFields ? 'major' : undefined}
              label={<span className={styles.formLabel}>专业</span>}
            >
              {canEditExtendedFields ? (
                <Input placeholder="请输入专业" className={styles.formInput} />
              ) : (
                <Input value={profile.major} disabled className={styles.formInputDisabled} />
              )}
            </Form.Item>

            {/* 报名方向 - MEMBER 及以上可编辑 */}
            <Form.Item
              name={canEditExtendedFields ? 'direction' : undefined}
              label={<span className={styles.formLabel}>报名方向</span>}
            >
              {canEditExtendedFields ? (
                <Select
                  placeholder="请选择方向"
                  className={styles.formSelect}
                  options={[
                    { value: 'computer_vision', label: '计算机视觉' },
                    { value: 'structural_design', label: '结构设计' },
                    { value: 'embedded', label: '嵌入式开发' },
                  ]}
                />
              ) : (
                <Input value={directionLabel} disabled className={styles.formInputDisabled} />
              )}
            </Form.Item>

            {/* 性别 - MEMBER 及以上可编辑 */}
            <Form.Item
              name={canEditExtendedFields ? 'gender' : undefined}
              label={<span className={styles.formLabel}>性别</span>}
            >
              {canEditExtendedFields ? (
                <Select
                  placeholder="请选择性别"
                  className={styles.formSelect}
                  options={[
                    { value: 'male', label: '男' },
                    { value: 'female', label: '女' },
                    { value: 'unknown', label: '未知' },
                  ]}
                />
              ) : (
                <Input value={genderLabel} disabled className={styles.formInputDisabled} />
              )}
            </Form.Item>

            {/* 个人简介 - 所有人可编辑 */}
            <Form.Item
              name="bio"
              label={<span className={styles.formLabel}>个人简介</span>}
              className={styles.formGroupFullWidth}
            >
              <Input.TextArea
                placeholder="介绍一下你自己..."
                rows={4}
                className={styles.formTextarea}
              />
            </Form.Item>
          </div>

          <div className={styles.formActions}>
            <Button
              className={`${styles.btn} ${styles.btnSecondary}`}
              onClick={handleCancel}
              disabled={isSubmitting}
              icon={<CloseOutlined />}
            >
              取消
            </Button>
            <Button
              type="primary"
              htmlType="submit"
              className={`${styles.btn} ${styles.btnPrimary}`}
              loading={isSubmitting}
              icon={<SaveOutlined />}
            >
              保存修改
            </Button>
          </div>
        </Form>
      ) : (
        <>
          <div className={styles.infoGrid}>
            <div className={styles.infoGridItem}>
              <div className={styles.infoGridLabel}>用户名</div>
              <div className={styles.infoGridValue}>{profile.username}</div>
            </div>
            <div className={styles.infoGridItem}>
              <div className={styles.infoGridLabel}>昵称</div>
              <div className={styles.infoGridValue}>{profile.nickname || '-'}</div>
            </div>
            <div className={styles.infoGridItem}>
              <div className={styles.infoGridLabel}>年级</div>
              <div className={styles.infoGridValue}>{profile.grade}</div>
            </div>
            <div className={styles.infoGridItem}>
              <div className={styles.infoGridLabel}>学院</div>
              <div className={styles.infoGridValue}>{profile.college}</div>
            </div>
            <div className={styles.infoGridItem}>
              <div className={styles.infoGridLabel}>专业</div>
              <div className={styles.infoGridValue}>{profile.major}</div>
            </div>
            <div className={styles.infoGridItem}>
              <div className={styles.infoGridLabel}>报名方向</div>
              <div className={styles.infoGridValue}>{directionLabel}</div>
            </div>
            <div className={styles.infoGridItem}>
              <div className={styles.infoGridLabel}>性别</div>
              <div className={styles.infoGridValue}>{genderLabel}</div>
            </div>
            <div className={styles.infoGridItem}>
              <div className={styles.infoGridLabel}>角色</div>
              <div className={styles.infoGridValue}>
                {profile.roleName === 'candidate' ? '考生' : '成员'}
              </div>
            </div>
            <div className={`${styles.infoGridItem} ${styles.infoGridFullWidth}`}>
              <div className={styles.infoGridLabel}>个人简介</div>
              <div className={styles.infoGridValue}>{profile.bio || '-'}</div>
            </div>
          </div>

          <div className={styles.emailSection}>
            <div className={styles.formSectionTitle}>
              <MailOutlined />
              邮箱设置
            </div>
            <div className={styles.emailDisplay}>
              <div className={styles.emailInfo}>
                <MailOutlined />
                <span className={styles.emailText}>{profile.email}</span>
              </div>
              <div className={styles.emailRight}>
                <span className={styles.emailStatus}>
                  <CheckCircleOutlined />
                  已验证
                </span>
                <Button className={styles.changeEmailBtn} disabled>
                  <EditOutlined />
                  修改邮箱
                </Button>
              </div>
            </div>
          </div>

          <div className={styles.formActions}>
            <Button
              type="primary"
              className={`${styles.btn} ${styles.btnPrimary}`}
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
